package org.springframework.faces.mvc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.js.ajax.AjaxHandler;
import org.springframework.js.ajax.SpringJavascriptAjaxHandler;

/**
 * Default implementation of {@link RedirectHandler}. The class is based heavily on FlowHandlerAdapter from the webflow
 * project.
 * 
 * @author Phillip Webb
 * @author Keith Donald
 */
public class DefaultRedirectHandler implements RedirectHandler, InitializingBean {

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

	// FIXME test
	private AjaxHandler ajaxHandler;

	private boolean redirectHttp10Compatible = false;

	/**
	 * Sends a redirect to the requested url.
	 * @param request The request
	 * @param response The response
	 * @param url the url to redirect to
	 * @throws IOException an exception occurred
	 */
	protected void sendRedirect(HttpServletRequest request, HttpServletResponse response, String url)
			throws IOException {
		if (ajaxHandler.isAjaxRequest(request, response)) {
			ajaxHandler.sendAjaxRedirect(url, request, response, false);
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

	protected String getLocationUrl(HttpServletRequest httpServletRequest, String location) {
		for (Iterator iterator = URL_BUILDERS.iterator(); iterator.hasNext();) {
			UrlBuilder urlBuilder = (UrlBuilder) iterator.next();
			if (urlBuilder.isSuitable(location)) {
				return urlBuilder.buildUrl(httpServletRequest, location);
			}
		}
		return location;
	}

	public void handleRedirect(HttpServletRequest request, HttpServletResponse response, Object location)
			throws IOException {
		if (location != null) {
			String url = getLocationUrl(request, location.toString());
			sendRedirect(request, response, url);
		}
	}

	public void afterPropertiesSet() throws Exception {
		ajaxHandler = (ajaxHandler == null ? new SpringJavascriptAjaxHandler() : ajaxHandler);
	}

	public void setRedirectHttp10Compatible(boolean redirectHttp10Compatible) {
		this.redirectHttp10Compatible = redirectHttp10Compatible;
	}

	// FIXME do we need this on the adapter like webflow has
	public void setAjaxHandler(AjaxHandler ajaxHandler) {
		this.ajaxHandler = ajaxHandler;
	}
}
