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
package org.springframework.faces.mvc.servlet;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.ComparisonFailure;
import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.springframework.faces.mvc.execution.ExecutionContextKey;
import org.springframework.faces.mvc.execution.repository.IntegerExecutionContextKey;
import org.springframework.faces.mvc.navigation.NavigationLocation;
import org.springframework.js.ajax.AjaxHandler;
import org.springframework.web.util.WebUtils;

public class DefaultRedirectHandlerTests extends TestCase {

	private static final String ENCODING = WebUtils.DEFAULT_CHARACTER_ENCODING;

	private static final int TYPE_NORMAL = 1;
	private static final int TYPE_HTTP10 = 2;
	private static final int TYPE_AJAX = 3;

	private void doTestRedirect(NavigationLocation location, final String expectedUrl, int type, ExecutionContextKey key)
			throws Exception {
		DefaultRedirectHandler handler = new DefaultRedirectHandler();
		handler.setRedirectHttp10Compatible(type == TYPE_HTTP10);
		ServletContext context = (ServletContext) EasyMock.createMock(ServletContext.class);
		HttpServletRequest request = (HttpServletRequest) EasyMock.createNiceMock(HttpServletRequest.class);
		HttpServletResponse response = (HttpServletResponse) EasyMock.createMock(HttpServletResponse.class);
		AjaxHandler ajaxHandler = (AjaxHandler) EasyMock.createMock(AjaxHandler.class);
		EasyMock.expect(request.getContextPath()).andReturn("/context");
		EasyMock.expect(request.getServletPath()).andReturn("/servlet");
		ajaxHandler.isAjaxRequest(request, response);
		EasyMock.expectLastCall().andReturn(new Boolean(type == TYPE_AJAX));
		switch (type) {
		case TYPE_NORMAL:
			response.setStatus(303);
			EasyMock.expectLastCall();
			EasyMock.expect(response.encodeRedirectURL((String) EasyMock.eq(expectedUrl))).andReturn(expectedUrl);
			response.setHeader("Location", expectedUrl);
			EasyMock.expectLastCall();
			break;
		case TYPE_HTTP10:
			EasyMock.expect(response.encodeRedirectURL((String) EasyMock.eq(expectedUrl))).andReturn(expectedUrl);
			response.sendRedirect(expectedUrl);
			EasyMock.expectLastCall();
			break;
		case TYPE_AJAX:
			ajaxHandler.sendAjaxRedirect((String) EasyMock.eq(expectedUrl), (HttpServletRequest) EasyMock.eq(request),
					(HttpServletResponse) EasyMock.eq(response), EasyMock.eq(false));
			EasyMock.expectLastCall();
			break;
		default:
			throw new IllegalStateException("Unknown type");
		}

		EasyMock.replay(new Object[] { context, request, response, ajaxHandler });
		handler.handleRedirect(ajaxHandler, ENCODING, request, response, location, key);
		EasyMock.verify(new Object[] { response, ajaxHandler });
	}

	private void doTestRedirects(Object location, final String expectedUrl, ExecutionContextKey key) throws Exception {
		NavigationLocation navigationLocation = new NavigationLocation(location);
		doTestRedirect(navigationLocation, expectedUrl, TYPE_NORMAL, key);
		doTestRedirect(navigationLocation, expectedUrl, TYPE_HTTP10, key);
		doTestRedirect(navigationLocation, expectedUrl, TYPE_AJAX, key);
	}

	public void testAssertsWorks() throws Exception {
		try {
			doTestRedirects("http://localhost", "http://localhost2", null);
			fail();
		} catch (AssertionError e) {
		} catch (ComparisonFailure e) {
		}
	}

	public void testServletRelative() throws Exception {
		doTestRedirects("servletRelative:test", "/context/servlet/test", null);
		doTestRedirects("servletRelative:/test", "/context/servlet/test", null);
	}

	public void testContextRelative() throws Exception {
		doTestRedirects("contextRelative:test", "/context/test", null);
		doTestRedirects("contextRelative:/test", "/context/test", null);
	}

	public void testServerRelative() throws Exception {
		doTestRedirects("serverRelative:test", "/test", null);
		doTestRedirects("serverRelative:/test", "/test", null);
	}

	public void testHttp() throws Exception {
		doTestRedirects("http://localhost", "http://localhost", null);
	}

	public void testHttps() throws Exception {
		doTestRedirects("https://localhost", "https://localhost", null);
	}

	public void testDefault() throws Exception {
		doTestRedirects("test", "/context/servlet/test", null);
		doTestRedirects("/test", "/context/servlet/test", null);
	}

	public void testWithKey() throws Exception {
		ExecutionContextKey key = new IntegerExecutionContextKey(123);
		doTestRedirects("test", "/context/servlet/test?execution=123", key);
		doTestRedirects("test?a=b", "/context/servlet/test?a=b&execution=123", key);
	}

	public void testKeyEncoding() throws Exception {
		ExecutionContextKey key = new ExecutionContextKey() {
			public String toString() {
				return "te&st";
			}

			public boolean equals(Object o) {
				throw new UnsupportedOperationException("Auto-generated method stub");
			}

			public int hashCode() {
				throw new UnsupportedOperationException("Auto-generated method stub");
			}
		};
		doTestRedirects("test", "/context/servlet/test?execution=te%26st", key);
	}

	public void testExtractExecutionKey() throws Exception {
		DefaultRedirectHandler handler = new DefaultRedirectHandler();
		HttpServletRequest requestWithout = (HttpServletRequest) EasyMock.createMock(HttpServletRequest.class);
		EasyMock.expect(requestWithout.getParameter("execution")).andReturn(null);
		HttpServletRequest requestWith = (HttpServletRequest) EasyMock.createMock(HttpServletRequest.class);
		EasyMock.expect(requestWith.getParameter("execution")).andReturn("123");
		EasyMock.replay(new Object[] { requestWithout, requestWith });
		assertNull(handler.getExecutionContextKey(requestWithout));
		assertEquals("123", handler.getExecutionContextKey(requestWith));
	}
}
