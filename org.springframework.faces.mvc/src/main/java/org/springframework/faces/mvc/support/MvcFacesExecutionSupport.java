package org.springframework.faces.mvc.support;

import org.springframework.faces.mvc.execution.MvcFacesExecution;
import org.springframework.faces.mvc.execution.RequestContext;
import org.springframework.faces.mvc.execution.RequestContextHolder;
import org.springframework.faces.mvc.execution.RequestControlContext;

public class MvcFacesExecutionSupport {
	public static boolean isMvcFacesRequest() {
		return RequestContextHolder.getRequestContext() != null;
	}

	public static MvcFacesExecution getExecution() {
		RequestContext requestContext = RequestContextHolder.getRequestContext();
		if (requestContext == null) {
			return null;
		}
		return ((RequestControlContext) requestContext).getExecution();
	}

}
