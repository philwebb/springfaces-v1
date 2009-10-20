/*
 * Copyright 2004-2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.faces.mvc;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.faces.FacesMapping;
import org.springframework.util.ClassUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter;

/**
 * Extension of {@link AnnotationMethodHandlerAdapter} to support JSF controllers.
 * 
 * @author Phillip Webb
 */
public class FacesAnnotationMethodHandlerAdapter extends AnnotationMethodHandlerAdapter {

	// FIXME doccomments
	// FIXME support FaceContext param injection?

	/**
	 * Adapter class to convert the annotated handler into a {@link FacesMapping}.
	 */
	private class AnnotatedMethodFacesHandlerAdapter implements FacesHandler {
		private Object handler;

		public AnnotatedMethodFacesHandlerAdapter(Object handler) {
			super();
			this.handler = handler;
		}

		public ModelAndView createView(FacesContext facesContext) throws Exception {

			HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
			HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
			ModelAndView modelAndView = FacesAnnotationMethodHandlerAdapter.this.handleCreateView(request, response,
					handler);
			return modelAndView;

		}

		public Object getNavigationOutcomeLocation(String fromAction, String outcome) {
			System.out.println(fromAction);
			System.out.println(outcome);
			return "/test/test2";
		}
	}

	private FacesHandlerAdapter facesHandlerAdapter;

	protected boolean supportsFaces(Object handler) {
		// FIXME option to disable this?
		Class handlerClass = ClassUtils.getUserClass(handler);
		boolean hasFacesAnnotation = AnnotationUtils.findAnnotation(handlerClass, FacesMapping.class) != null;
		return hasFacesAnnotation;
	}

	public boolean supports(Object handler) {
		return supportsFaces(handler) && super.supports(handler);
	}

	protected ModelAndView handleCreateView(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		return super.handle(request, response, handler);
	}

	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		FacesHandler facesHandler = new AnnotatedMethodFacesHandlerAdapter(handler);
		return facesHandlerAdapter.handle(request, response, facesHandler);
	}

	public void setFacesHandlerAdapter(FacesHandlerAdapter facesHandlerAdapter) {
		this.facesHandlerAdapter = facesHandlerAdapter;
	}

}
