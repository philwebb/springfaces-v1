package org.springframework.faces.mvc.context;

import org.springframework.webflow.context.ExternalContext;
import org.springframework.webflow.core.collection.SharedAttributeMap;

public class WebFlowExternalContextAdapter implements org.springframework.faces.mvc.context.ExternalContext {

	private ExternalContext externalContext;

	public WebFlowExternalContextAdapter(org.springframework.webflow.context.ExternalContext externalContext) {
		this.externalContext = externalContext;
	}

	public SharedAttributeMap getSessionMap() {
		return externalContext.getSessionMap();
	}

}
