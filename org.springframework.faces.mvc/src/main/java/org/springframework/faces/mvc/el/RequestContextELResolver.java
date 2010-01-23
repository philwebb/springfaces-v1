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
package org.springframework.faces.mvc.el;

import org.springframework.faces.mvc.execution.RequestContext;
import org.springframework.faces.mvc.execution.RequestContextHolder;

/**
 * Custom EL resolver that resolves the current {@link RequestContext} under the variable
 * {@link #REQUEST_CONTEXT_VARIABLE_NAME} . Allows for accessing any property of the RequestContext instance. For
 * example: "#{mvcFacesRequestContext.requestScope.myProperty}".
 * 
 * @author Phillip Webb
 */
public class RequestContextELResolver extends AbstractELResolver {

	/**
	 * Name of the request context variable.
	 */
	public static final String REQUEST_CONTEXT_VARIABLE_NAME = "mvcFacesRequestContext";

	protected boolean isAvailable() {
		return getRequestContext() != null;
	}

	protected boolean handles(String attribute) {
		return REQUEST_CONTEXT_VARIABLE_NAME.equals(attribute);
	}

	protected Object get(String attribute) {
		if (REQUEST_CONTEXT_VARIABLE_NAME.equals(attribute)) {
			return getRequestContext();
		}
		return null;
	}

	protected RequestContext getRequestContext() {
		return RequestContextHolder.getRequestContext();
	}
}