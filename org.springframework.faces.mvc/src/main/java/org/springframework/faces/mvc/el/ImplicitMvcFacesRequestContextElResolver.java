package org.springframework.faces.mvc.el;

import org.springframework.faces.mvc.support.MvcFacesRequestContextHolder;

public class ImplicitMvcFacesRequestContextElResolver extends BeanBackedElResolver {

	public ImplicitMvcFacesRequestContextElResolver() {
		map("requestScope", "requestScope");
		map("flashScope", "flashScope");
		map("viewScope", "viewScope");
	}

	protected Object getBean() {
		return MvcFacesRequestContextHolder.getRequestContext();
	}

}
