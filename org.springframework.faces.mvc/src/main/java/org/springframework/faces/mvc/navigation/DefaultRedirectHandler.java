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
package org.springframework.faces.mvc.navigation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.js.ajax.AjaxHandler;

/**
 * Default implementation of {@link RedirectHandler}. The class is based heavily on FlowHandlerAdapter from Spring Web
 * Flow.
 * 
 * @author Keith Donald
 * @author Phillip Webb
 */
public class DefaultRedirectHandler implements RedirectHandler {

	private static final int NONE = 0x00;
	private static final int STRIP_PREFIX = 0x01;
	private static final int SLASH = 0x02;
	private static final int CONTEXT = 0x04;
	private static final int SERVLET = 0x08;

	private static class UrlBuilder {
		private String prefix;
		private int flags;

		public UrlBuilder(String prefix, int flags) {
			this.prefix = prefix;
			this.flags = flags;
		}

		public boolean isSuitable(String location) {
			return location.startsWith(prefix);
		}

		private boolean hasFlag(int context) {
			return ((flags & context) != 0);
		}

		public String buildUrl(HttpServletRequest httpServletRequest, String location) {
			StringBuffer url = new StringBuffer();
			url.append(hasFlag(CONTEXT) ? httpServletRequest.getContextPath() : "");
			url.append(hasFlag(SERVLET) ? httpServletRequest.getServletPath() : "");
			location = (hasFlag(STRIP_PREFIX) ? location.substring(prefix.length()) : location);
			if (hasFlag(SLASH) && !location.startsWith("/")) {
				url.append("/");
			}
			url.append(location);
			return url.toString();
		}
	}

	private static final List URL_BUILDERS;
	static {
		URL_BUILDERS = new ArrayList();
		URL_BUILDERS.add(new UrlBuilder("servletRelative:", STRIP_PREFIX | SLASH | CONTEXT | SERVLET));
		URL_BUILDERS.add(new UrlBuilder("contextRelative:", STRIP_PREFIX | SLASH | CONTEXT));
		URL_BUILDERS.add(new UrlBuilder("serverRelative:", STRIP_PREFIX | SLASH));
		URL_BUILDERS.add(new UrlBuilder("http://", NONE));
		URL_BUILDERS.add(new UrlBuilder("https://", NONE));
		URL_BUILDERS.add(new UrlBuilder("", SLASH | CONTEXT | SERVLET));
	}

	private boolean redirectHttp10Compatible = false;

	/**
	 * Sends a redirect to the requested url.
	 * 
	 * @param ajaxHandler The active ajax handler
	 * @param url the redirect URL
	 * @param request The request
	 * @param response The response
	 * @param popup whether the redirect should be sent from a new popup dialog window
	 * @throws IOException
	 */
	protected void sendRedirect(AjaxHandler ajaxHandler, String url, HttpServletRequest request,
			HttpServletResponse response, boolean popup) throws IOException {
		if (ajaxHandler.isAjaxRequest(request, response)) {
			ajaxHandler.sendAjaxRedirect(url, request, response, popup);
		} else {
			if (redirectHttp10Compatible) {
				// Always send status code 302.
				response.sendRedirect(response.encodeRedirectURL(url));
			} else {
				// Correct HTTP status code is 303, in particular for POST requests.
				response.setStatus(303);
				response.setHeader("Location", response.encodeRedirectURL(url));
			}
		}
	}

	/**
	 * Get the actual URL that should be used for the specified location. This method will expand servletRelative,
	 * contextRelative and serverRelative prefixes.
	 * 
	 * @param request The request
	 * @param location The location string
	 * @return The URL The final URL with all prefixes expanded
	 */
	protected String getLocationUrl(HttpServletRequest request, String location) {
		for (Iterator iterator = URL_BUILDERS.iterator(); iterator.hasNext();) {
			UrlBuilder urlBuilder = (UrlBuilder) iterator.next();
			if (urlBuilder.isSuitable(location)) {
				return urlBuilder.buildUrl(request, location);
			}
		}
		return location;
	}

	public void handleRedirect(AjaxHandler ajaxHandler, HttpServletRequest request, HttpServletResponse response,
			NavigationLocation location) throws IOException {
		if (location != null && location.getLocation() != null) {
			String url = getLocationUrl(request, location.getLocation().toString());
			sendRedirect(ajaxHandler, url, request, response, location.getPopup());
		}
	}

	/**
	 * Set if redirects should be HTTP 1.0 compatible. When this property is <tt>true</tt> redirects are handled using a
	 * HTTP 302 header, otherwise a HTTP 303 header is used. If not set the default value of <tt>false</tt> will be
	 * used.
	 * 
	 * @param redirectHttp10Compatible If redirects are HTTP 1.0 compatible.
	 */
	public void setRedirectHttp10Compatible(boolean redirectHttp10Compatible) {
		this.redirectHttp10Compatible = redirectHttp10Compatible;
	}
}
