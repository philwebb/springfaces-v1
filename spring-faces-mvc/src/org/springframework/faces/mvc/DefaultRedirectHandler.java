package org.springframework.faces.mvc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

/**
 * Default implementation of {@link RedirectHandler}.
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

		public String buildUrl(FacesContext facesContext, String location) {
			ExternalContext externalContext = facesContext.getExternalContext();
			StringBuffer url = new StringBuffer();
			url.append(hasFlag(CONTEXT) ? externalContext.getRequestContextPath() : "");
			url.append(hasFlag(SERVLET) ? externalContext.getRequestServletPath() : "");
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
	protected void sendRedirect(FacesContext facesContext, String url) throws IOException {
		if (redirectHttp10Compatible
				|| !(facesContext.getExternalContext().getResponse() instanceof HttpServletResponse)) {
			// Always send status code 302.
			facesContext.getExternalContext().redirect(url);
		} else {
			// Correct HTTP status code is 303, in particular for POST requests.
			HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
			response.setStatus(303);
			response.setHeader("Location", response.encodeRedirectURL(url));
		}
	}

	protected String getLocationUrl(FacesContext facesContext, String location) {
		for (Iterator iterator = URL_BUILDERS.iterator(); iterator.hasNext();) {
			UrlBuilder urlBuilder = (UrlBuilder) iterator.next();
			if (urlBuilder.isSuitable(location)) {
				return urlBuilder.buildUrl(facesContext, location);
			}
		}
		return location;
	}

	public void handleRedirect(FacesContext facesContext, Object location) throws IOException {

		if (location != null) {
			String url = getLocationUrl(facesContext, location.toString());
			sendRedirect(facesContext, url);
		}
	}

	public void setRedirectHttp10Compatible(boolean redirectHttp10Compatible) {
		this.redirectHttp10Compatible = redirectHttp10Compatible;
	}
}
