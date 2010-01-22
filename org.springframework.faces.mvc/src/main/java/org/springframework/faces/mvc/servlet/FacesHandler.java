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
package org.springframework.faces.mvc.servlet;

import javax.faces.context.FacesContext;

import org.springframework.faces.mvc.execution.MvcFacesExceptionHandler;
import org.springframework.faces.mvc.navigation.NavigationLocation;
import org.springframework.faces.mvc.navigation.NavigationRequestEvent;
import org.springframework.web.servlet.ModelAndView;

/**
 * Interface that allows full JSF integration with Spring MVC. In order to handle this interface an instance of
 * {@link FacesHandlerAdapter} must be registered with Spring MVC. This interface provides the various hook points that
 * allow for complete JSF integration with Spring MVC. Instead of using this interface directly consider using the
 * <tt>FacesAnnotationMethodHandlerAdapter</tt> class.
 * 
 * @author Phillip Webb
 */
public interface FacesHandler {

	/**
	 * Called to create the JSF view for the first time. This method should return a {@link ModelAndView} that contains
	 * the View ID and any model data that should be bound to JSF. The view ID must be a view reference that can be
	 * resolved by the {@link FacesHandlerAdapter#getFacesViewIdResolver()} to an actual page resource. A <tt>null</tt>
	 * view ID can be used to indicate that rendering has been completed by the handler directly.
	 * @param facesContext The faces context
	 * @return The model and view data
	 * @throws Exception in the case of an error
	 */
	ModelAndView createView(FacesContext facesContext) throws Exception;

	/**
	 * Called to determine the outcome of a navigation event. This method should return the location that the client
	 * will be redirected to by the {@link FacesHandlerAdapter#getRedirectHandler()}. A <tt>null</tt> response can be
	 * used if the navigation could not be handled, in such cases the standard JSF navigation handlers are called (if no
	 * navigation handler manages the outcome the existing page is re-rendered)
	 * @param facesContext The faces context that requested the navigation
	 * @param event The navigation request event
	 * @return The navigation outcome
	 * @throws Exception in the case of an error
	 */
	NavigationLocation getNavigationOutcomeLocation(FacesContext facesContext, NavigationRequestEvent event)
			throws Exception;

	/**
	 * Called to resolve read-only variables from the handler. This method can be used to expose variables from the
	 * hander to JSF.
	 * @param propertyName The name of the property being resolved.
	 * @return A resolved property or <tt>null</tt> if the propertyName is not recognised by the handler
	 */
	Object resolveVariable(String variableName);

	/**
	 * Called to obtain any specific {@link MvcFacesExceptionHandler}s that should be used to deal with exceptions.
	 * Return <tt>null</tt> if no specific handlers are required. Handlers registered with
	 * {@link AbstractFacesHandlerAdapter} will still be called.
	 * @return specific exception handlers
	 */
	MvcFacesExceptionHandler[] getExceptionHandlers();
}
