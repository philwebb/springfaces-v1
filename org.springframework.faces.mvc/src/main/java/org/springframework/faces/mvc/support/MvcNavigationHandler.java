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

import org.springframework.faces.mvc.execution.RequestContextHolder;
import org.springframework.faces.mvc.execution.RequestControlContext;
import org.springframework.faces.mvc.navigation.NavigationLocation;
import org.springframework.faces.mvc.navigation.NavigationRequestEvent;
import org.springframework.webflow.execution.View;

/**
 * JSF {@link NavigationHandler} that provides integration with Spring MVC.
 * 
 * @author Phillip Webb
 */
public class MvcNavigationHandler extends NavigationHandler {

	private NavigationHandler delegate;

	public MvcNavigationHandler(NavigationHandler delegate) {
		super();
		this.delegate = delegate;
	}

	public void handleNavigation(FacesContext facesContext, String fromAction, String outcome) {
		if (MvcFacesExecutionSupport.isMvcFacesRequest()) {
			RequestControlContext requestContext = (RequestControlContext) RequestContextHolder.getRequestContext();
			NavigationRequestEvent event = new NavigationRequestEvent(this, fromAction, outcome);
			try {
				requestContext.setLastNavigationRequestEvent(event);
				NavigationLocation location = requestContext.getFacesHandler().getNavigationOutcomeLocation(
						facesContext, event);
				if (isStoredFragments(location)) {
					requestContext.getFlashScope().put(View.RENDER_FRAGMENTS_ATTRIBUTE, location.getFragments());
				}
				if (location != null && location.getLocation() != null) {
					requestContext.getExecution().redirect(facesContext, requestContext, location);
					FacesContext.getCurrentInstance().responseComplete();
					return;
				}
			} catch (Exception e) {
				throw new FacesException(e.getMessage(), e);
			}
		}
		this.delegate.handleNavigation(facesContext, fromAction, outcome);
	}

	/**
	 * Determine if fragments need to be stored for the specified location. Fragments only need to be stored for popups
	 * and direct redirects.
	 * 
	 * @param location
	 * @return
	 */
	private boolean isStoredFragments(NavigationLocation location) {
		if (location == null || location.getFragments() == null || location.getFragments().length == 0) {
			// No fragments
			return false;
		}
		// Only store fragments for popups or direct re-renders
		return (location.isPopup() || location.getLocation() == null);
	}
}
