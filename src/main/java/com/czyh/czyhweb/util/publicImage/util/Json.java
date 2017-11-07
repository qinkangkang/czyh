package com.czyh.czyhweb.util.publicImage.util;

import java.util.Map;

import org.springside.modules.mapper.JsonMapper;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JavaType;

public final class Json {

	private static JsonMapper mapper = new JsonMapper(Include.ALWAYS);

	private Json() {
	}

	public static String encode(StringMap map) {
		return mapper.toJson(map.map());
	}

	public static <T> T decode(String json, Class<T> classOfT) {
		return mapper.fromJson(json, classOfT);
	}

	public static StringMap decode(String json) {
		// CHECKSTYLE:OFF
		JavaType jt = mapper.contructMapType(Map.class, String.class, Object.class);
		// CHECKSTYLE:ON
		Map<String, Object> x = mapper.fromJson(json, jt);
		return new StringMap(x);
	}
}
