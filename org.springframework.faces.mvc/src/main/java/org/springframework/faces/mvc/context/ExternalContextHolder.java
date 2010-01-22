package org.springframework.faces.mvc.context;

import org.springframework.faces.mvc.execution.MvcFacesRequestContext;
import org.springframework.faces.mvc.execution.MvcFacesRequestContextHolder;

//FIXME
public class ExternalContextHolder {
	public static ExternalContext getExternalContext() {
		MvcFacesRequestContext requestContext = MvcFacesRequestContextHolder.getRequestContext();
		return (requestContext == null ? null : requestContext.getExternalContext());
	}
}
