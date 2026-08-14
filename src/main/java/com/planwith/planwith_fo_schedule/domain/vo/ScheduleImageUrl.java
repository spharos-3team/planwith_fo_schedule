package com.planwith.planwith_fo_schedule.domain.vo;

import java.net.URI;
import java.util.Locale;

import com.planwith.planwith_fo_schedule.domain.InvalidScheduleException;

public record ScheduleImageUrl(String value) {

	private static final int MAX_LENGTH = 2_048;

	public ScheduleImageUrl {
		value = normalize(value);
	}

	public static ScheduleImageUrl ofNullable(String value) {
		return value == null || value.isBlank() ? null : new ScheduleImageUrl(value);
	}

	public static boolean isValid(String value) {
		try {
			return ofNullable(value) != null;
		} catch (InvalidScheduleException exception) {
			return false;
		}
	}

	private static String normalize(String value) {
		if (value == null || value.isBlank()) {
			throw new InvalidScheduleException("Schedule image URL is required.");
		}
		String trimmedUrl = value.trim();
		if (trimmedUrl.length() > MAX_LENGTH) {
			throw new InvalidScheduleException("Schedule image URL must not exceed 2048 characters.");
		}
		try {
			URI uri = URI.create(trimmedUrl);
			String host = uri.getHost();
			if (!"https".equalsIgnoreCase(uri.getScheme()) || host == null || isBlockedHost(host)) {
				throw new InvalidScheduleException("Schedule image URL must be a public HTTPS URL.");
			}
			return trimmedUrl;
		} catch (IllegalArgumentException exception) {
			throw new InvalidScheduleException("Schedule image URL is invalid.");
		}
	}

	private static boolean isBlockedHost(String host) {
		String normalizedHost = host.toLowerCase(Locale.ROOT);
		if (normalizedHost.equals("localhost") || normalizedHost.endsWith(".localhost")
				|| normalizedHost.equals("::1") || normalizedHost.equals("[::1]")
				|| normalizedHost.startsWith("fc") || normalizedHost.startsWith("fd")
				|| normalizedHost.startsWith("fe80:")) {
			return true;
		}
		return isBlockedIpv4Literal(normalizedHost);
	}

	private static boolean isBlockedIpv4Literal(String host) {
		if (!host.matches("\\d{1,3}(?:\\.\\d{1,3}){3}")) {
			return false;
		}
		String[] parts = host.split("\\.");
		int first = Integer.parseInt(parts[0]);
		int second = Integer.parseInt(parts[1]);
		return first == 0
				|| first == 10
				|| first == 127
				|| (first == 100 && second >= 64 && second <= 127)
				|| (first == 169 && second == 254)
				|| (first == 172 && second >= 16 && second <= 31)
				|| (first == 192 && second == 168)
				|| first >= 224;
	}
}
