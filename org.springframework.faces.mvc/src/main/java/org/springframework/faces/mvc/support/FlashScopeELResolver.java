package org.springframework.faces.mvc.support;

import java.util.Map;

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
