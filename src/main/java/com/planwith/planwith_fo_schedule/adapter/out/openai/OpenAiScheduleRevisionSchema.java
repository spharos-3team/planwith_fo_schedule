package com.planwith.planwith_fo_schedule.adapter.out.openai;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class OpenAiScheduleRevisionSchema {

	private OpenAiScheduleRevisionSchema() {
	}

	static Map<String, Object> value() {
		Map<String, Object> properties = new LinkedHashMap<>();
		properties.put("title", Map.of(
				"type", "string",
				"minLength", 1,
				"maxLength", 200
		));
		properties.put("content", Map.of(
				"type", "string",
				"minLength", 1
		));

		Map<String, Object> schema = new LinkedHashMap<>();
		schema.put("type", "object");
		schema.put("additionalProperties", false);
		schema.put("required", List.copyOf(properties.keySet()));
		schema.put("properties", properties);
		return schema;
	}
}
