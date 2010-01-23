package org.springframework.faces.mvc.annotation.support;

import org.springframework.web.bind.support.WebArgumentResolver;

//FIXME DC
//FIXME Test?
public interface AnnotatedMethodInvokerFactory {
	public AnnotatedMethodInvoker newInvoker(WebArgumentResolver... additionalArgumentResolvers);
}
