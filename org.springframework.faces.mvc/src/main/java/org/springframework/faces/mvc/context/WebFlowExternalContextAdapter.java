package org.springframework.faces.mvc.context;

import java.io.Writer;
import java.security.Principal;
import java.util.Locale;

import org.springframework.webflow.context.ExternalContext;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.core.collection.ParameterMap;
import org.springframework.webflow.core.collection.SharedAttributeMap;

public class WebFlowExternalContextAdapter implements org.springframework.faces.mvc.context.ExternalContext {

	private ExternalContext externalContext;

	public WebFlowExternalContextAdapter(org.springframework.webflow.context.ExternalContext externalContext) {
		this.externalContext = externalContext;
	}

	public SharedAttributeMap getSessionMap() {
		return externalContext.getSessionMap();
	}

	public SharedAttributeMap getApplicationMap() {
		return externalContext.getApplicationMap();
	}

	public String getContextPath() {
		return externalContext.getContextPath();
	}

	public Principal getCurrentUser() {
		return externalContext.getCurrentUser();
	}

	public SharedAttributeMap getGlobalSessionMap() {
		return externalContext.getGlobalSessionMap();
	}

	public Locale getLocale() {
		return externalContext.getLocale();
	}

	public Object getNativeContext() {
		return externalContext.getNativeContext();
	}

	public Object getNativeRequest() {
		return externalContext.getNativeRequest();
	}

	public Object getNativeResponse() {
		return externalContext.getNativeResponse();
	}

	public MutableAttributeMap getRequestMap() {
		return externalContext.getRequestMap();
	}

	public ParameterMap getRequestParameterMap() {
		return externalContext.getRequestParameterMap();
	}

	public Writer getResponseWriter() throws IllegalStateException {
		return externalContext.getResponseWriter();
	}

	public boolean isAjaxRequest() {
		return externalContext.isAjaxRequest();
	}

}
