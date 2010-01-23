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
package org.springframework.faces.mvc.support;

import java.security.Principal;
import java.util.Locale;

import org.springframework.util.Assert;
import org.springframework.webflow.context.ExternalContext;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.core.collection.ParameterMap;
import org.springframework.webflow.core.collection.SharedAttributeMap;

/**
 * Simple adapter class that allows a WebFlow {@link org.springframework.webflow.context.ExternalContext} to be exposed
 * as a Faces MVC {@link org.springframework.faces.mvc.context.ExternalContext}.
 * 
 * @author Phillip Webb
 */
public class WebFlowExternalContextAdapter implements org.springframework.faces.mvc.context.ExternalContext {

	private ExternalContext externalContext;

	public WebFlowExternalContextAdapter(org.springframework.webflow.context.ExternalContext externalContext) {
		Assert.notNull(externalContext, "The externalContext is required");
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

	public boolean isAjaxRequest() {
		return externalContext.isAjaxRequest();
	}
}
