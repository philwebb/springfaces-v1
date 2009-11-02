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
package org.springframework.faces.mvc.support;

import javax.faces.FacesException;
import javax.faces.application.NavigationHandler;
import javax.faces.context.FacesContext;

/**
 * JSF {@link NavigationHandler} that provides integration with Spring MVC.
 * 
 * @author Phillip Webb
 */
public class MvcNavigationHandler extends NavigationHandler {

	private NavigationHandler delegate;

	public MvcNavigationHandler(NavigationHandler navigationHandler) {
		super();
		this.delegate = navigationHandler;
	}

	public void handleNavigation(FacesContext facesContext, String fromAction, String outcome) {
		if (MvcFacesRequestContext.getCurrentInstance() != null) {
			// FIXME Where to redirect to?
			// A flow
			// external URL
			// relative URL
			// Another controller
			// A jsf view
			// Itself
			// flowRedirect:
			// externalRedirect:
			// servletRelative: - redirect to a resource relative to the current servlet
			// contextRelative: - redirect to a resource relative to the current web application context path
			// serverRelative: - redirect to a resource relative to the server root
			// http:// or https:// - redirect to a fully-qualified resource URI

			MvcFacesRequestContext requestContext = MvcFacesRequestContext.getCurrentInstance();
			try {
				Object location = requestContext.getFacesHandler().getNavigationOutcomeLocation(facesContext,
						fromAction, outcome);
				requestContext.getMvcFacesContext().redirect(facesContext, location);
			} catch (Exception e) {
				throw new FacesException(e.getMessage(), e);
			}

		}
		this.delegate.handleNavigation(facesContext, fromAction, outcome);
	}
}
