package org.springframework.faces.mvc.support;

import org.springframework.faces.mvc.context.MvcFacesExecution;
import org.springframework.faces.mvc.execution.MvcFacesRequestContext;
import org.springframework.faces.mvc.execution.MvcFacesRequestContextHolder;
import org.springframework.faces.mvc.execution.MvcFacesRequestControlContext;

public class MvcFacesExecutionSupport {
	public static boolean isMvcFacesRequest() {
		return MvcFacesRequestContextHolder.getRequestContext() != null;
	}

	public static MvcFacesExecution getExecution() {
		MvcFacesRequestContext requestContext = MvcFacesRequestContextHolder.getRequestContext();
		if (requestContext == null) {
			return null;
		}
		return ((MvcFacesRequestControlContext) requestContext).getExecution();
	}

}
