package org.springframework.faces.mvc.context;

import org.springframework.faces.mvc.execution.RequestContext;
import org.springframework.faces.mvc.execution.RequestContextHolder;

//FIXME
public class ExternalContextHolder {
	public static ExternalContext getExternalContext() {
		RequestContext requestContext = RequestContextHolder.getRequestContext();
		return (requestContext == null ? null : requestContext.getExternalContext());
	}
}
