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

import org.springframework.faces.mvc.annotation.FacesAnnotationMethodHandlerAdapter;
import org.springframework.web.servlet.ModelAndView;

/**
 * Interface that allows first class JSF integration with Spring MVC. In order to handle this interface an instance of
 * {@link FacesHandlerAdapter} must be registered with Spring MVC. This interface provides various hook points that
 * allows for complete JSF integration with Spring MVC. Instead of using this interface directly consider using the
 * {@link FacesAnnotationMethodHandlerAdapter} class.
 * 
 * @see FacesAnnotationMethodHandlerAdapter
 * 
 * @author Phillip Webb
 */
public interface FacesHandler {

	/**
	 * Called to create the JSF view for the first time.
	 * 
	 * @return A {@link ModelAndView} that contains the View ID and the any model data. The view ID must be a view
	 * reference that can be resolved by the {@link FacesHandlerAdapter#getFacesViewIdResolver()} to an actual page
	 * resource. A <tt>null</tt> view ID can be used to indicate that rendering has been completed by the handler
	 * directly.
	 */
	ModelAndView createView(FacesContext facesContext) throws Exception;

	/**
	 * Called to determine the outcome of a navigation.
	 * 
	 * @param fromAction - The action binding expression that was evaluated to retrieve the specified outcome, or
	 * <tt>null</tt> if the outcome was acquired by some other means
	 * @param outcome - The logical outcome returned by a previous invoked application action (which may be
	 * <tt>null</tt>)
	 * @return A location that can the client can be redirected to by the
	 * {@link FacesHandlerAdapter#getRedirectHandler()}. A <tt>null</tt> view ID can be used to indicate that navigation
	 * has been handled directly.
	 */
	Object getNavigationOutcomeLocation(FacesContext facesContext, String fromAction, String outcome) throws Exception;
	// FIXME null support
}
