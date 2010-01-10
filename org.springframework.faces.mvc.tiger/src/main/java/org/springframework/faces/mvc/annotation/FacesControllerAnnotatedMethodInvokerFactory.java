package org.springframework.faces.mvc.annotation;

import org.springframework.web.bind.support.WebArgumentResolver;

interface FacesControllerAnnotatedMethodInvokerFactory {
	public FacesControllerAnnotatedMethodInvoker newInvoker(WebArgumentResolver... additionalArgumentResolvers);
}
