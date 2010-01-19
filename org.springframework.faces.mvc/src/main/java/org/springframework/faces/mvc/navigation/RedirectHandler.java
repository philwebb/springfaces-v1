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
package org.springframework.faces.mvc.navigation;

import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.faces.mvc.FacesHandler;
import org.springframework.faces.mvc.execution.ExecutionContextKey;
import org.springframework.faces.mvc.execution.MvcFacesExceptionOutcome;
import org.springframework.js.ajax.AjaxHandler;

/**
 * Interface that is used to redirect the client to a specific location following a navigation outcome. Note:
 * Implementations of this interface should not rely on a {@link FacesContext} being available.
 * 
 * @author Phillip Webb
 */
public interface RedirectHandler {

	/**
	 * Redirect the client to the specified location.
	 * @param ajaxHandler The AJAX handler that is being used to handle the request. This will never be <tt>null</tt>
	 * @param request The native request
	 * @param response The native response
	 * @param location The redirect location, this will be the result of
	 * {@link FacesHandler#getNavigationOutcomeLocation(javax.faces.context.FacesContext, NavigationRequestEvent)} or
	 * {@link MvcFacesExceptionOutcome#redirect(NavigationLocation)}
	 * @param key An optional context key that should be encoded as part of the redirect such that it can be later
	 * retrieved using {@link #getExecutionContextKey(HttpServletRequest)}.
	 * @throws IOException
	 */
	public void handleRedirect(AjaxHandler ajaxHandler, HttpServletRequest request, HttpServletResponse response,
			NavigationLocation location, ExecutionContextKey key) throws IOException;

	/**
	 * Returns the flow execution context key encoded as part of the request or <tt>null</tt> if not present.
	 * @return The flow execution context key or <tt>null</tt>.
	 */
	public String getExecutionContextKey(HttpServletRequest request);
}
