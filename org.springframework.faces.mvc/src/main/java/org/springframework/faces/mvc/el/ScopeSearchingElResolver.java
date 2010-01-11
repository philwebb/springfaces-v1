package org.springframework.faces.mvc.el;

import org.springframework.faces.mvc.execution.MvcFacesRequestContext;
import org.springframework.faces.mvc.execution.MvcFacesRequestContextHolder;
import org.springframework.faces.mvc.execution.ScopeType;
import org.springframework.webflow.core.collection.MutableAttributeMap;

public class ScopeSearchingElResolver extends AbstractELResolver {

	private static ScopeType[] ORDERED_SCOPES = { ScopeType.REQUEST, ScopeType.FLASH, ScopeType.VIEW };

	protected boolean isAvailable() {
		return getRequestContext() != null;
	}

	protected boolean contains(String property) {
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

	protected MvcFacesRequestContext getRequestContext() {
		return MvcFacesRequestContextHolder.getRequestContext();
	}

}
