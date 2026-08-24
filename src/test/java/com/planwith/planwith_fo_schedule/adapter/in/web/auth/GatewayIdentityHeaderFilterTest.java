package com.planwith.planwith_fo_schedule.adapter.in.web.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.http.HttpServletRequest;

class GatewayIdentityHeaderFilterTest {

	private final GatewayIdentityHeaderFilter filter = new GatewayIdentityHeaderFilter(true);

	@Test
	void replacesClientMemberHeaderWithVerifiedGatewayIdentity() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/schedules/ai/generate");
		request.addHeader(GatewayIdentityHeaderFilter.AUTH_USER_ID, "11111111-1111-1111-1111-111111111111");
		request.addHeader(GatewayIdentityHeaderFilter.MEMBER_UUID_ALIAS, "spoofed");
		MockFilterChain chain = new MockFilterChain();

		filter.doFilter(request, new MockHttpServletResponse(), chain);

		HttpServletRequest trustedRequest = (HttpServletRequest) chain.getRequest();
		assertThat(trustedRequest.getHeader(GatewayIdentityHeaderFilter.MEMBER_UUID_ALIAS))
				.isEqualTo("11111111-1111-1111-1111-111111111111");
	}

	@Test
	void removesUnverifiedClientMemberHeader() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/schedules/calendar");
		request.addHeader(GatewayIdentityHeaderFilter.MEMBER_UUID_ALIAS, "spoofed");
		MockFilterChain chain = new MockFilterChain();

		filter.doFilter(request, new MockHttpServletResponse(), chain);

		HttpServletRequest trustedRequest = (HttpServletRequest) chain.getRequest();
		assertThat(trustedRequest.getHeader(GatewayIdentityHeaderFilter.MEMBER_UUID_ALIAS)).isNull();
	}
}
