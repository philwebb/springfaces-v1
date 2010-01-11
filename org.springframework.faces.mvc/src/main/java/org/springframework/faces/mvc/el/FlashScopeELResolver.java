package org.springframework.faces.mvc.el;

import java.util.Map;

import org.springframework.faces.mvc.execution.MvcFacesRequestContext;
import org.springframework.faces.mvc.execution.MvcFacesRequestContextHolder;

//FIXME DC
public class FlashScopeELResolver extends MapBackedELResolver {

	protected Map getMap() {
		MvcFacesRequestContext requestContext = MvcFacesRequestContextHolder.getRequestContext();
		if (requestContext != null) {
			return requestContext.getFlashScope().asMap();
		}
		return null;
	}
}
