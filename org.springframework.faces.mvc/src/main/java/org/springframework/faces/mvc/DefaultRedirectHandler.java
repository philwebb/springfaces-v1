package org.springframework.faces.mvc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Default implementation of {@link RedirectHandler}. The class is based heavily on FlowHandlerAdapter from the webflow
 * project.
 * 
 * @author Phillip Webb
 * @author Keith Donald
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

	private boolean redirectHttp10Compatible = true;

	/**
	 * Sends a redirect to the requested url.
	 * @param facesContext the faces context.
	 * @param url the url to redirect to
	 * @throws IOException an exception occurred
	 */
	protected void sendRedirect(HttpServletResponse httpServletResponse, String url) throws IOException {
		if (redirectHttp10Compatible) {
			// Always send status code 302.
			httpServletResponse.sendRedirect(url);
		} else {
			// Correct HTTP status code is 303, in particular for POST requests.
			httpServletResponse.setStatus(303);
			httpServletResponse.setHeader("Location", httpServletResponse.encodeRedirectURL(url));
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

	public void handleRedirect(Object request, Object response, Object location) throws IOException {
		if (!(request instanceof HttpServletRequest && response instanceof HttpServletResponse)) {
			throw new IllegalStateException("Only servlet environments are currently supported");
		}
		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		HttpServletResponse httpServletResponse = (HttpServletResponse) response;
		if (location != null) {
			String url = getLocationUrl(httpServletRequest, location.toString());
			sendRedirect(httpServletResponse, url);
		}
	}

	public void setRedirectHttp10Compatible(boolean redirectHttp10Compatible) {
		this.redirectHttp10Compatible = redirectHttp10Compatible;
	}
}
