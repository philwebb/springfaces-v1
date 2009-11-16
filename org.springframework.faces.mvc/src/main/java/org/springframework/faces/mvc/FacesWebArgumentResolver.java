package org.springframework.faces.mvc;

import javax.faces.context.FacesContext;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebArgumentResolver;
import org.springframework.web.context.request.NativeWebRequest;

public class FacesWebArgumentResolver implements WebArgumentResolver {

	// FIXME implement this

	public Object resolveArgument(MethodParameter methodParameter, NativeWebRequest webRequest) throws Exception {

		FacesContext facesContext = FacesContext.getCurrentInstance();
		facesContext.getApplication();
		facesContext.getExternalContext();
		facesContext.getELContext();

		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Auto-generated method stub");
	}

}
