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
package org.springframework.faces.mvc.context;

import java.io.IOException;
import java.util.Map;

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseListener;

import org.springframework.faces.mvc.execution.ActionUrlMapper;
import org.springframework.faces.mvc.execution.MvcFacesRequestContext;
import org.springframework.faces.mvc.execution.MvcFacesRequestContextHolder;
import org.springframework.faces.mvc.navigation.NavigationLocation;
import org.springframework.faces.mvc.navigation.RedirectHandler;
import org.springframework.faces.mvc.support.MvcNavigationHandler;
import org.springframework.faces.mvc.support.MvcPhaseListener;
import org.springframework.faces.mvc.support.MvcStateManager;
import org.springframework.faces.mvc.support.MvcViewHandler;
import org.springframework.faces.mvc.view.FacesViewIdResolver;
import org.springframework.web.servlet.HandlerAdapter;

/**
 * A MVC Faces Context object that acts as a central facade for various the various MVC/JSF integration classes. This
 * interface is exposed via the {@link MvcFacesRequestContext} object and provides a unified facade for the
 * {@link MvcNavigationHandler}, {@link MvcPhaseListener}, {@link MvcStateManager} and {@link MvcViewHandler} JSF
 * support classes. The context will most likely delegate to a MVC {@link HandlerAdapter} and the
 * {@link FacesViewIdResolver}, {@link RedirectHandler} & {@link ActionUrlMapper} interfaces. Note: This interface will
 * only be called for MVC faces requests (that is when {@link MvcFacesRequestContextHolder#getRequestContext()} does not
 * return <tt>null</tt>).
 * 
 * @see MvcFacesRequestContext
 * @see MvcFacesRequestContextHolder
 * @see HandlerAdapter
 * @see FacesViewIdResolver
 * @see RedirectHandler
 * @see ActionUrlMapper
 * 
 * @author Phillip Webb
 */
public interface MvcFacesContext {

	/**
	 * Called to resolve the view ID. This method usually delegates to a {@link FacesViewIdResolver}.
	 * 
	 * @param viewName The view name to resolve
	 * 
	 * @see FacesViewIdResolver#resolveViewId(String)
	 */
	String resolveViewId(String viewName);

	/**
	 * Called to obtain the Action URL for the specified view ID. This method usually delegates to an
	 * {@link ActionUrlMapper}.
	 * 
	 * @param facesContext The faces context
	 * @param viewId The current view ID
	 * @returns The action URL
	 * 
	 * @see FacesViewIdResolver#resolveViewName(String)
	 * @see ActionUrlMapper#getActionUlr(FacesContext, String)
	 */
	String getActionUlr(FacesContext facesContext, String viewId);

	/**
	 * Called to get the view ID that should be restored following a post-back. This method usually delegates to an
	 * {@link ActionUrlMapper}.
	 * 
	 * @param facesContext The faces context
	 * @param viewId The view ID to restore
	 * @param The actual view ID
	 * 
	 * @see ActionUrlMapper#getViewNameForRestore(FacesContext)
	 */
	String getViewIdForRestore(FacesContext facesContext, String viewId);

	/**
	 * Called after a new JSF view has been created. This method can be used to bind the MVC model to faces and perform
	 * any post processing on the created view.
	 * 
	 * @param facesContext The faces context
	 * @param mvcFacesRequestContext The MVC faces request context
	 * @param view The view that has been created
	 * @param model The MVC model obtained from the handler
	 */
	void viewCreated(FacesContext facesContext, MvcFacesRequestContext mvcFacesRequestContext, UIViewRoot view,
			Map model);

	/**
	 * Called during the encode of the JSF page. This method can render additional HTML by using
	 * {@link FacesContext#getResponseWriter()}.
	 * 
	 * @param facesContext The faces context
	 * @throws IOException
	 */
	void writeState(FacesContext facesContext) throws IOException;

	/**
	 * Called before a phase event. Equivalent to {@link PhaseListener#beforePhase(PhaseEvent)} but only called for MVC
	 * faces requests.
	 * 
	 * @param mvcFacesRequestContext The MVC faces request context
	 * @param event The phase event
	 */
	void beforePhase(MvcFacesRequestContext mvcFacesRequestContext, PhaseEvent event);

	/**
	 * Called after a phase event. Equivalent to {@link PhaseListener#afterPhase(PhaseEvent)} but only called for MVC
	 * faces requests.
	 * 
	 * @param mvcFacesRequestContext The MVC faces request context
	 * @param event The phase event
	 */
	void afterPhase(MvcFacesRequestContext mvcFacesRequestContext, PhaseEvent event);

	/**
	 * Called after a navigation outcome has been determined to redirect the browser.
	 * 
	 * @param facesContext The faces context
	 * @param mvcFacesRequestContext The MVC faces request context
	 * @param location The location to redirect to
	 * @throws IOException
	 */
	void redirect(FacesContext facesContext, MvcFacesRequestContext mvcFacesRequestContext, NavigationLocation location)
			throws IOException;
}
