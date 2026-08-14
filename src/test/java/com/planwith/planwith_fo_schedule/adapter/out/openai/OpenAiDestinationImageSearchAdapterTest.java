package com.planwith.planwith_fo_schedule.adapter.out.openai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.planwith.planwith_fo_schedule.config.OpenAiProperties;

class OpenAiDestinationImageSearchAdapterTest {

	private MockRestServiceServer server;
	private OpenAiDestinationImageSearchAdapter adapter;

	@BeforeEach
	void setUp() {
		RestClient.Builder builder = RestClient.builder().baseUrl("https://api.openai.com");
		server = MockRestServiceServer.bindTo(builder).build();
		OpenAiProperties properties = new OpenAiProperties();
		properties.setApiKey("test-api-key");
		properties.setImageSearchModel("gpt-5.6");
		adapter = new OpenAiDestinationImageSearchAdapter(
				builder.build(),
				properties,
				new OpenAiDestinationImagePromptFactory()
		);
	}

	@Test
	void searchesExactlyOneImageAndReturnsCanonicalUrl() {
		String response = """
				{
				  "output": [{
				    "type": "web_search_call",
				    "results": [{
				      "type": "image_result",
				      "image_url": "https://images.example.com/tokyo.jpg",
				      "source_website_url": "https://example.com/tokyo",
				      "thumbnail_url": "https://images.example.com/tokyo-thumb.jpg",
				      "caption": "도쿄의 대표적인 도심 전경"
				    }]
				  }]
				}
				""";

		server.expect(requestTo("https://api.openai.com/v1/responses"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer test-api-key"))
				.andExpect(jsonPath("$.model").value("gpt-5.6"))
				.andExpect(jsonPath("$.tools[0].type").value("web_search"))
				.andExpect(jsonPath("$.tools[0].search_content_types[0]").value("image"))
				.andExpect(jsonPath("$.tools[0].image_settings.max_results").value(1))
				.andExpect(jsonPath("$.include[0]").value("web_search_call.results"))
				.andExpect(jsonPath("$.input").value(org.hamcrest.Matchers.containsString("일본, 도쿄")))
				.andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

		assertThat(adapter.searchRepresentativeImage("일본, 도쿄"))
				.contains("https://images.example.com/tokyo.jpg");
		server.verify();
	}

	@Test
	void rejectsNonHttpsImageResult() {
		String response = """
				{
				  "output": [{
				    "type": "web_search_call",
				    "results": [{
				      "type": "image_result",
				      "image_url": "http://localhost/private.jpg"
				    }]
				  }]
				}
				""";

		server.expect(requestTo("https://api.openai.com/v1/responses"))
				.andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

		assertThat(adapter.searchRepresentativeImage("서울")).isEmpty();
		server.verify();
	}
}
