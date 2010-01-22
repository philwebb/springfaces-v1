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
import org.springframework.faces.mvc.execution.ScopeType;
import org.springframework.webflow.core.collection.MutableAttributeMap;

/**
 * Custom EL resolver that searches the current MVC faces request context for variables to resolve. The search algorithm
 * looks in request scope first, then flash scope, then view scope.
 * 
 * Suitable for use along side other variable resolvers to support EL binding expressions like "#{bean.property}" where
 * "bean" could be a property in any supported scope.
 * 
 * @author Jeremy Grelle
 * @author Phillip Webb
 */
public class ScopeSearchingElResolver extends AbstractELResolver {
	// FIXME test
	/**
	 * Scopes that we support, in the order that we support them.
	 */
	private static ScopeType[] ORDERED_SCOPES = { ScopeType.REQUEST, ScopeType.FLASH, ScopeType.VIEW };

	protected boolean isAvailable() {
		return getRequestContext() != null;
	}

	protected boolean handles(String property) {
		for (int i = 0; i < ORDERED_SCOPES.length; i++) {
			if (ORDERED_SCOPES[i].getScope(getRequestContext()).contains(property)) {
				return true;
			}
		}
		return false;
	}

	protected Object get(String property) {
		for (int i = 0; i < ORDERED_SCOPES.length; i++) {
			MutableAttributeMap scopeAttributes = ORDERED_SCOPES[i].getScope(getRequestContext());
			if (scopeAttributes.contains(property)) {
				return scopeAttributes.get(property);
			}
		}
		return null;
	}

	protected void set(String property, Object value) {
		for (int i = 0; i < ORDERED_SCOPES.length; i++) {
			MutableAttributeMap scopeAttributes = ORDERED_SCOPES[i].getScope(getRequestContext());
			if (scopeAttributes.contains(property)) {
				scopeAttributes.put(property, value);
				return;
			}
		}
	}

	protected RequestContext getRequestContext() {
		return RequestContextHolder.getRequestContext();
	}
}
