package com.planwith.planwith_fo_schedule.adapter.out.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.planwith.planwith_fo_schedule.application.model.AiOperationType;
import com.planwith.planwith_fo_schedule.application.model.AiUsageReportEvent;
import com.planwith.planwith_fo_schedule.application.port.out.AiUsageEventPublisher;
import com.planwith.planwith_fo_schedule.config.AiUsageReportProperties;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

@SpringBootTest(properties = {
		"ai.usage-report.enabled=true",
		"ai.usage-report.relay-initial-delay=1h",
		"ai.usage-report.relay-interval=1h",
		"ai.usage-report.initial-retry-delay=1ms",
		"ai.usage-report.send-timeout=5s",
		"openai.api-key=test-api-key",
		"openai.model=gpt-schedule",
		"openai.image-search-model=gpt-image"
})
@ActiveProfiles("test")
@EmbeddedKafka(
		partitions = 1,
		topics = AiUsageEndToEndIntegrationTest.TOPIC,
		bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
class AiUsageEndToEndIntegrationTest {

	static final String TOPIC = "planwith.ai-usage.reported";
	private static final ObjectMapper HTTP_MAPPER = new ObjectMapper().findAndRegisterModules();
	private static final HttpServer OPEN_AI_SERVER = startOpenAiServer();

	@Autowired
	private WebApplicationContext applicationContext;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private SpringDataAiUsageOutboxRepository outboxRepository;

	@Autowired
	private AiUsageEventPublisher kafkaPublisher;

	@Autowired
	private AiUsageReportProperties properties;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Autowired
	private EmbeddedKafkaBroker embeddedKafka;

	private MockMvc mockMvc;
	private UUID memberUuid;

	@DynamicPropertySource
	static void openAiProperties(DynamicPropertyRegistry registry) {
		registry.add("openai.base-url", () -> "http://localhost:" + OPEN_AI_SERVER.getAddress().getPort());
	}

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
		memberUuid = UUID.randomUUID();
		outboxRepository.deleteAll();
	}

	@AfterAll
	static void stopOpenAiServer() {
		OPEN_AI_SERVER.stop(0);
	}

	@Test
	void verifiesGenerateRegenerateReviseUsageReportingRetryAndIdempotency() throws Exception {
		try (Consumer<String, AiUsageReportEvent> consumer = tokenServiceConsumer()) {
			InMemoryTokenService tokenService = new InMemoryTokenService();

			JsonNode generatedUsage = requestAiDraft("/api/v1/schedules/ai/generate").path("usage");
			UUID generateRequestId = assertAggregatedUsage(generatedUsage, AiOperationType.GENERATE);
			assertPendingOutbox(generateRequestId);
			relay(kafkaPublisher);
			AiUsageReportEvent generateEvent = consume(consumer, generateRequestId);
			tokenService.consume(generateEvent);

			JsonNode regeneratedUsage = requestAiDraft("/api/v1/schedules/ai/regenerate").path("usage");
			UUID regenerateRequestId = assertAggregatedUsage(regeneratedUsage, AiOperationType.REGENERATE);
			assertThat(regenerateRequestId).isNotEqualTo(generateRequestId);
			AtomicBoolean failFirstAttempt = new AtomicBoolean(true);
			AiUsageEventPublisher failOncePublisher = event -> failFirstAttempt.getAndSet(false)
					? CompletableFuture.failedFuture(new IllegalStateException("Token delivery unavailable"))
					: kafkaPublisher.publish(event);
			relay(failOncePublisher);
			AiUsageOutboxJpaEntity failedOutbox = outbox(regenerateRequestId);
			assertThat(failedOutbox.status()).isEqualTo(AiUsageOutboxStatus.PENDING);
			assertThat(failedOutbox.attemptCount()).isEqualTo(1);
			Thread.sleep(20);
			relay(failOncePublisher);
			AiUsageReportEvent regenerateEvent = consume(consumer, regenerateRequestId);
			tokenService.consume(regenerateEvent);
			tokenService.consume(regenerateEvent);

			UUID scheduleUuid = createSchedule();
			JsonNode revisedUsage = reviseSchedule(scheduleUuid).path("usage");
			UUID reviseRequestId = UUID.fromString(revisedUsage.path("requestId").asText());
			assertThat(revisedUsage.path("operationType").asText()).isEqualTo("REVISE");
			assertThat(revisedUsage.path("model").asText()).isEqualTo("gpt-schedule-response");
			assertThat(revisedUsage.path("inputTokens").asLong()).isEqualTo(1_000);
			assertThat(revisedUsage.path("outputTokens").asLong()).isEqualTo(500);
			assertThat(revisedUsage.path("totalTokens").asLong()).isEqualTo(1_500);
			assertThat(reviseRequestId).isNotIn(generateRequestId, regenerateRequestId);
			relay(kafkaPublisher);
			AiUsageReportEvent reviseEvent = consume(consumer, reviseRequestId);
			tokenService.consume(reviseEvent);

			assertThat(outbox(generateRequestId).status()).isEqualTo(AiUsageOutboxStatus.PUBLISHED);
			assertThat(outbox(regenerateRequestId).status()).isEqualTo(AiUsageOutboxStatus.PUBLISHED);
			assertThat(outbox(reviseRequestId).status()).isEqualTo(AiUsageOutboxStatus.PUBLISHED);
			assertThat(tokenService.processedRequestIds())
					.containsExactlyInAnyOrder(generateRequestId, regenerateRequestId, reviseRequestId);
			assertThat(tokenService.ledger()).hasSize(3);
			assertThat(tokenService.ledger().get(regenerateRequestId)).isEqualTo(5_700);
		}
	}

	private UUID assertAggregatedUsage(JsonNode usage, AiOperationType operationType) {
		assertThat(usage.path("memberUuid").asText()).isEqualTo(memberUuid.toString());
		assertThat(usage.path("operationType").asText()).isEqualTo(operationType.name());
		assertThat(usage.path("model").asText()).isEqualTo("gpt-schedule-response,gpt-image-response");
		assertThat(usage.path("inputTokens").asLong()).isEqualTo(2_500);
		assertThat(usage.path("outputTokens").asLong()).isEqualTo(3_200);
		assertThat(usage.path("totalTokens").asLong()).isEqualTo(5_700);
		return UUID.fromString(usage.path("requestId").asText());
	}

	private JsonNode requestAiDraft(String path) throws Exception {
		MvcResult result = mockMvc.perform(post(path)
						.header("X-Member-UUID", memberUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "destination": "부산",
								  "startDate": "2026-09-01",
								  "endDate": "2026-09-03",
								  "participantCount": 2,
								  "estimatedBudget": 500000,
								  "transportation": "TRAIN_PUBLIC_TRANSIT",
								  "travelStyle": "TOUR_LANDMARK",
								  "additionalRequest": "바다 중심 일정"
								}
								"""))
				.andExpect(status().isOk())
				.andReturn();
		return responseData(result);
	}

	private UUID createSchedule() throws Exception {
		MvcResult result = mockMvc.perform(post("/api/v1/schedules")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "memberUuid": "%s",
								  "title": "부산 여행",
								  "destination": "부산",
								  "startDate": "2026-10-01",
								  "endDate": "2026-10-03",
								  "headcount": 2,
								  "expectedCost": 500000,
								  "content": "1일차 해운대, 2일차 자유 일정, 3일차 광안리"
								}
								""".formatted(memberUuid)))
				.andExpect(status().isCreated())
				.andReturn();
		return UUID.fromString(responseData(result).path("scheduleUuid").asText());
	}

	private JsonNode reviseSchedule(UUID scheduleUuid) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/v1/schedules/{scheduleUuid}/ai/revise", scheduleUuid)
						.header("X-Member-UUID", memberUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"additionalRequest": "맛집 중심으로 수정해줘"}
								"""))
				.andExpect(status().isOk())
				.andReturn();
		return responseData(result);
	}

	private JsonNode responseData(MvcResult result) throws Exception {
		return objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
	}

	private void assertPendingOutbox(UUID requestId) {
		assertThat(outbox(requestId).status()).isEqualTo(AiUsageOutboxStatus.PENDING);
	}

	private AiUsageOutboxJpaEntity outbox(UUID requestId) {
		return outboxRepository.findByRequestId(requestId).orElseThrow();
	}

	private void relay(AiUsageEventPublisher publisher) {
		AiUsageOutboxRelay relay = new AiUsageOutboxRelay(outboxRepository, publisher, properties);
		new TransactionTemplate(transactionManager).executeWithoutResult(status -> relay.relayPendingEvents());
	}

	private Consumer<String, AiUsageReportEvent> tokenServiceConsumer() {
		Map<String, Object> consumerProperties = KafkaTestUtils.consumerProps(
				embeddedKafka,
				"token-service-" + UUID.randomUUID(),
				true
		);
		JacksonJsonDeserializer<AiUsageReportEvent> valueDeserializer =
				new JacksonJsonDeserializer<>(AiUsageReportEvent.class, false);
		Consumer<String, AiUsageReportEvent> consumer =
				new DefaultKafkaConsumerFactory<>(
						consumerProperties,
						new StringDeserializer(),
						valueDeserializer
				).createConsumer();
		embeddedKafka.consumeFromAnEmbeddedTopic(consumer, TOPIC);
		return consumer;
	}

	private AiUsageReportEvent consume(Consumer<String, AiUsageReportEvent> consumer, UUID requestId) {
		long deadline = System.nanoTime() + Duration.ofSeconds(10).toNanos();
		while (System.nanoTime() < deadline) {
			ConsumerRecords<String, AiUsageReportEvent> records = consumer.poll(Duration.ofMillis(200));
			for (ConsumerRecord<String, AiUsageReportEvent> record : records) {
				if (requestId.toString().equals(record.key())) {
					assertThat(record.value().requestId()).isEqualTo(requestId);
					return record.value();
				}
			}
		}
		throw new AssertionError("Kafka event was not received for requestId=" + requestId);
	}

	private static HttpServer startOpenAiServer() {
		try {
			HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
			server.createContext("/v1/responses", AiUsageEndToEndIntegrationTest::handleOpenAiRequest);
			server.start();
			return server;
		} catch (IOException exception) {
			throw new ExceptionInInitializerError(exception);
		}
	}

	private static void handleOpenAiRequest(HttpExchange exchange) throws IOException {
		try {
			JsonNode request = HTTP_MAPPER.readTree(exchange.getRequestBody());
			String response;
			if (request.has("tools")) {
				response = imageResponse();
			} else if ("planwith_schedule_revision".equals(
					request.path("text").path("format").path("name").asText())) {
				response = textResponse(
						"{\"content\":\"맛집 중심으로 수정한 부산 일정입니다.\"}",
						"gpt-schedule-response",
						1_000,
						500,
						1_500
				);
			} else {
				response = textResponse(schedulePayload(), "gpt-schedule-response", 2_000, 3_000, 5_000);
			}
			byte[] body = response.getBytes(StandardCharsets.UTF_8);
			exchange.getResponseHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
			exchange.sendResponseHeaders(200, body.length);
			exchange.getResponseBody().write(body);
		} catch (RuntimeException exception) {
			exchange.sendResponseHeaders(500, -1);
		} finally {
			exchange.close();
		}
	}

	private static String textResponse(
			String outputText,
			String model,
			long inputTokens,
			long outputTokens,
			long totalTokens
	) {
		ObjectNode root = usageEnvelope(model, inputTokens, outputTokens, totalTokens);
		ObjectNode output = HTTP_MAPPER.createObjectNode();
		ArrayNode content = output.putArray("content");
		content.addObject().put("type", "output_text").put("text", outputText);
		root.putArray("output").add(output);
		return root.toString();
	}

	private static String imageResponse() {
		ObjectNode root = usageEnvelope("gpt-image-response", 500, 200, 700);
		ObjectNode output = root.putArray("output").addObject();
		output.put("type", "web_search_call");
		output.putArray("results").addObject()
				.put("type", "image_result")
				.put("image_url", "https://images.example.com/busan.jpg");
		return root.toString();
	}

	private static ObjectNode usageEnvelope(
			String model,
			long inputTokens,
			long outputTokens,
			long totalTokens
	) {
		ObjectNode root = HTTP_MAPPER.createObjectNode();
		root.put("model", model);
		root.putObject("usage")
				.put("input_tokens", inputTokens)
				.put("output_tokens", outputTokens)
				.put("total_tokens", totalTokens);
		return root;
	}

	private static String schedulePayload() {
		return """
				{
				  "title": "부산 AI 여행",
				  "content": null,
				  "items": [
				    {
				      "dayNumber": 1,
				      "scheduleTime": "10:00:00",
				      "subtitle": "해운대 산책",
				      "scheduleType": "TOUR",
				      "description": "해변 산책",
				      "estimatedCost": 0,
				      "placeName": "해운대",
				      "placeAddress": "부산광역시",
				      "latitude": null,
				      "longitude": null
				    },
				    {
				      "dayNumber": 2,
				      "scheduleTime": "10:00:00",
				      "subtitle": "광안리 관광",
				      "scheduleType": "TOUR",
				      "description": "광안리 관광",
				      "estimatedCost": 0,
				      "placeName": "광안리",
				      "placeAddress": "부산광역시",
				      "latitude": null,
				      "longitude": null
				    },
				    {
				      "dayNumber": 3,
				      "scheduleTime": "10:00:00",
				      "subtitle": "부산역 이동",
				      "scheduleType": "MOVE",
				      "description": "귀가",
				      "estimatedCost": 0,
				      "placeName": "부산역",
				      "placeAddress": "부산광역시",
				      "latitude": null,
				      "longitude": null
				    }
				  ]
				}
				""";
	}

	private static final class InMemoryTokenService {

		private final Set<UUID> processedRequestIds = new HashSet<>();
		private final Map<UUID, Long> ledger = new HashMap<>();

		void consume(AiUsageReportEvent event) {
			if (processedRequestIds.add(event.requestId())) {
				ledger.put(event.requestId(), event.totalTokens());
			}
		}

		Set<UUID> processedRequestIds() {
			return Set.copyOf(processedRequestIds);
		}

		Map<UUID, Long> ledger() {
			return Map.copyOf(ledger);
		}
	}
}
