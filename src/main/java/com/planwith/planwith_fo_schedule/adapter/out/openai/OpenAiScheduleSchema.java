package com.planwith.planwith_fo_schedule.adapter.out.openai;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class OpenAiScheduleSchema {

	private OpenAiScheduleSchema() {
	}

	static Map<String, Object> value() {
		Map<String, Object> itemProperties = new LinkedHashMap<>();
		itemProperties.put("dayNumber", Map.of("type", "integer", "minimum", 1));
		itemProperties.put("scheduleTime", nullableString());
		itemProperties.put("subtitle", Map.of("type", "string", "minLength", 1, "maxLength", 200));
		itemProperties.put("scheduleType", Map.of(
				"type", "string",
				"enum", List.of("MOVE", "FOOD", "TOUR", "STAY", "ACTIVITY", "ETC")
		));
		itemProperties.put("description", nullableString());
		itemProperties.put("estimatedCost", Map.of("type", "integer", "minimum", 0));
		itemProperties.put("placeName", nullableString());
		itemProperties.put("placeAddress", nullableString());
		itemProperties.put("latitude", Map.of("type", "null"));
		itemProperties.put("longitude", Map.of("type", "null"));

		Map<String, Object> itemSchema = new LinkedHashMap<>();
		itemSchema.put("type", "object");
		itemSchema.put("additionalProperties", false);
		itemSchema.put("required", List.copyOf(itemProperties.keySet()));
		itemSchema.put("properties", itemProperties);

		Map<String, Object> properties = new LinkedHashMap<>();
		properties.put("title", Map.of("type", "string", "minLength", 1, "maxLength", 200));
		properties.put("content", Map.of("type", "null"));
		properties.put("items", Map.of("type", "array", "minItems", 1, "items", itemSchema));

		Map<String, Object> schema = new LinkedHashMap<>();
		schema.put("type", "object");
		schema.put("additionalProperties", false);
		schema.put("required", List.copyOf(properties.keySet()));
		schema.put("properties", properties);
		return schema;
	}

	private static Map<String, Object> nullableString() {
		return Map.of("type", List.of("string", "null"));
	}

}
