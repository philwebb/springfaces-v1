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
package org.springframework.faces.mvc.execution;

import javax.faces.event.PhaseId;

import org.springframework.faces.mvc.context.ExternalContext;
import org.springframework.faces.mvc.navigation.NavigationRequestEvent;
import org.springframework.faces.mvc.servlet.FacesHandler;
import org.springframework.webflow.core.collection.MutableAttributeMap;

/**
 * A context for a single JSF request that is being handled by the Spring MVC framework. The
 * {@link RequestContext} is associated to the current thread and remains available during the execution the
 * request. The context provides access to the {@link FacesHandler} instance is handling the request and an
 * {@link ExternalContext}.
 * 
 * @see RequestContextHolder
 * 
 * @author Phillip Webb
 */
public interface RequestContext {

	// FIXME
	public ExternalContext getExternalContext();

	/**
	 * Returns the active {@link FacesHandler} that is handling the current request.
	 * @return the active faces handler
	 */
	FacesHandler getFacesHandler();

	/**
	 * Returns the current exception that is being handled by MVC or <tt>null</tt> if an exception has not been raised.
	 * Note: When an exception is being processes the JSF lifecycle will stop after {@link PhaseId#PROCESS_VALIDATIONS}.
	 * @return the current exception or <tt>null</tt>
	 */
	Exception getException();

	/**
	 * Returns the last navigation event that was being processed by the system or <tt>null</tt> if a navigation request
	 * has not yet been processed. Note: this value is reset on each request but remains available during exception
	 * handling.
	 * @return The last navigation event or <tt>null</tt>
	 */
	NavigationRequestEvent getLastNavigationRequestEvent();

	/**
	 * Returns a mutable map for accessing and/or setting attributes in request scope. <b>Request scoped attributes
	 * exist for the duration of this request only.</b>
	 * @return the request scope
	 */
	public MutableAttributeMap getRequestScope();

	/**
	 * Returns a mutable map for accessing and/or setting attributes in flash scope. <b>Flash scoped attributes exist
	 * until the after next view is rendered.</b>
	 * @return the flash scope
	 */
	public MutableAttributeMap getFlashScope();

	/**
	 * Returns a mutable map for accessing and/or setting attributes in view scope. <b>View scoped attributes exist for
	 * the life of the current view state, including post-back.</b>
	 * @return the view scope
	 */
	public MutableAttributeMap getViewScope() throws IllegalStateException;
}
