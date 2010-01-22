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

import javax.faces.context.FacesContext;

import org.springframework.faces.mvc.servlet.FacesHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * Interface to be implemented by classes that can handle exceptions thrown during execution of a MVC Faces request.
 * Implementors are either registered as beans in the application context or returned from
 * {@link FacesHandler#getExceptionHandlers()}. This interface is similar to the MVC {@link HandlerExceptionResolver}
 * except that it is called when the {@link RequestContext} is still active and that it provides the opportunity
 * to request specific outcomes, such as re-rendering the current request.
 * 
 * @see MvcFacesExceptionOutcome
 * @see RequestContext
 * @see HandlerExceptionResolver
 * 
 * @author Phillip Webb
 */
public interface MvcFacesExceptionHandler {

	/**
	 * Called to allow the exception handler to deal with the specified exception. If the exception is handled by the
	 * method return <tt>true</tt>, otherwise return <tt>false</tt> to allow other handlers to process the error.
	 * <p>
	 * Handlers are free to render a response directly or use the {@link MvcFacesExceptionOutcome} parameter to request
	 * a specific action.
	 * <p>
	 * Note: exception handlers are called outside of the JSF lifecyle and will not have access to a
	 * {@link FacesContext} instance.
	 * @param exception The exception to be handled
	 * @param requestContext The current MVC Faces request context
	 * @param outcome A callback object that can be used to request a specific action (see
	 * {@link MvcFacesExceptionOutcome} for details
	 * @return <tt>true</tt> if the exception was handled (no further processing will occur and no other handlers will
	 * be called) or <tt>false</tt> if the exception could not be handled
	 * @throws Exception An exception during the handling. Note: this exception will simply propagate to MVC, it will
	 * not trigger further handlers
	 */
	boolean handleException(Exception exception, RequestContext requestContext, MvcFacesExceptionOutcome outcome)
			throws Exception;
}
