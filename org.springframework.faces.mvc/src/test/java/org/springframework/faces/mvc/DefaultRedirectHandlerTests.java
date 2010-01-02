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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.ComparisonFailure;
import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.springframework.js.ajax.AjaxHandler;

public class DefaultRedirectHandlerTests extends TestCase {

	private static final int TYPE_NORMAL = 1;
	private static final int TYPE_HTTP10 = 2;
	private static final int TYPE_AJAX = 3;

	private void doTestRedirect(Object location, final String expectedUrl, int type) throws Exception {
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
		handler.handleRedirect(ajaxHandler, request, response, location);
		EasyMock.verify(new Object[] { response, ajaxHandler });
	}

	private void doTestRedirects(Object location, final String expectedUrl) throws Exception {
		doTestRedirect(location, expectedUrl, TYPE_NORMAL);
		doTestRedirect(location, expectedUrl, TYPE_HTTP10);
		doTestRedirect(location, expectedUrl, TYPE_AJAX);
	}

	public void testAssertsWorks() throws Exception {
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
	//
	// private static class MockAjaxHandler implements AjaxHandler {
	// public boolean isAjaxRequest(HttpServletRequest arg0, HttpServletResponse arg1) {
	// return false;
	// }
	//
	// public void sendAjaxRedirect(String arg0, HttpServletRequest arg1, HttpServletResponse arg2, boolean arg3)
	// throws IOException {
	// }
	// }
}
