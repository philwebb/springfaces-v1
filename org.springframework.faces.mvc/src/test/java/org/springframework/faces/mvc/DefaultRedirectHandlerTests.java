/*
 * Copyright 2004-2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.faces.mvc;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.ComparisonFailure;
import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.springframework.js.ajax.AjaxHandler;

public class DefaultRedirectHandlerTests extends TestCase {

	// FIXME update tests to include ajax handler

	private void doTestRedirect(Object location, final String expectedUrl, boolean redirectHttp10Compatible)
			throws Exception {
		DefaultRedirectHandler handler = new DefaultRedirectHandler();
		handler.setAjaxHandler(new MockAjaxHandler());
		handler.setRedirectHttp10Compatible(redirectHttp10Compatible);
		ServletContext context = EasyMock.createMock(ServletContext.class);
		HttpServletRequest request = EasyMock.createNiceMock(HttpServletRequest.class);
		EasyMock.expect(request.getContextPath()).andReturn("/context");
		EasyMock.expect(request.getServletPath()).andReturn("/servlet");
		HttpServletResponse response = EasyMock.createMock(HttpServletResponse.class);
		if (!redirectHttp10Compatible) {
			response.setStatus(303);
			EasyMock.expectLastCall();
			EasyMock.expect(response.encodeRedirectURL(EasyMock.eq(expectedUrl))).andReturn(expectedUrl);
			response.setHeader("Location", expectedUrl);
			EasyMock.expectLastCall();
		} else {
			EasyMock.expect(response.encodeRedirectURL(EasyMock.eq(expectedUrl))).andReturn(expectedUrl);
			response.sendRedirect(expectedUrl);
			EasyMock.expectLastCall();

		}
		EasyMock.replay(new Object[] { context, request, response });
		handler.handleRedirect(request, response, location);
		EasyMock.verify(new Object[] { response });
	}

	private void doTestRedirects(Object location, final String expectedUrl) throws Exception {
		doTestRedirect(location, expectedUrl, true);
		doTestRedirect(location, expectedUrl, false);
	}

	public void testAssertsWork() throws Exception {
		try {
			doTestRedirects("http://localhost", "http://localhost2");
			fail();
		} catch (AssertionError e) {
		} catch (ComparisonFailure e) {
		}
	}

	public void testServletRelative() throws Exception {
		doTestRedirects("servletRelative:test", "/context/servlet/test");
		doTestRedirects("servletRelative:/test", "/context/servlet/test");
	}

	public void testContextRelative() throws Exception {
		doTestRedirects("contextRelative:test", "/context/test");
		doTestRedirects("contextRelative:/test", "/context/test");
	}

	public void testServerRelative() throws Exception {
		doTestRedirects("serverRelative:test", "/test");
		doTestRedirects("serverRelative:/test", "/test");
	}

	public void testHttp() throws Exception {
		doTestRedirects("http://localhost", "http://localhost");
	}

	public void testHttps() throws Exception {
		doTestRedirects("https://localhost", "https://localhost");
	}

	public void testDefault() throws Exception {
		doTestRedirects("test", "/context/servlet/test");
		doTestRedirects("/test", "/context/servlet/test");
	}

	private static class MockAjaxHandler implements AjaxHandler {
		public boolean isAjaxRequest(HttpServletRequest arg0, HttpServletResponse arg1) {
			return false;
		}

		public void sendAjaxRedirect(String arg0, HttpServletRequest arg1, HttpServletResponse arg2, boolean arg3)
				throws IOException {
		}
	}
}
