package org.springframework.faces.mvc.annotation.support;

import org.springframework.web.bind.support.WebArgumentResolver;

public interface FacesControllerAnnotatedMethodInvokerFactory {
	public FacesControllerAnnotatedMethodInvoker newInvoker(WebArgumentResolver... additionalArgumentResolvers);
}
