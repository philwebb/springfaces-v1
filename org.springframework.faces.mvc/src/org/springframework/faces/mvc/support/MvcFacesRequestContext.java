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

import org.springframework.core.NamedThreadLocal;
import org.springframework.faces.mvc.FacesHandler;
import org.springframework.util.Assert;

/**
 * A context for a single JSF request that is being handled by the Spring MVC framework. The
 * {@link MvcFacesRequestContext} is associated to the current thread and remains available during the execution the
 * request. The context provides access to the {@link FacesHandler} instance is handling the request and a
 * {@link MvcFacesContext}.
 * 
 * @author Phillip Webb
 */
public class MvcFacesRequestContext {

	private static final ThreadLocal currentInstance = new NamedThreadLocal("MvcFacesRequest");

	private boolean released;
	private MvcFacesContext mvcFacesContext;
	private FacesHandler facesHandler;

	/**
	 * Public constructor.
	 * 
	 * @param mvcFacesContext
	 * @param facesHandler
	 */
	public MvcFacesRequestContext(MvcFacesContext mvcFacesContext, FacesHandler facesHandler) {
		Assert.notNull(mvcFacesContext);
		Assert.notNull(facesHandler);
		this.mvcFacesContext = mvcFacesContext;
		this.facesHandler = facesHandler;
		setCurrentInstance(this);
	}

	/**
	 * @return The {@link FacesHandler} that is handling the current request.
	 */
	public FacesHandler getFacesHandler() {
		return facesHandler;
	}

	/**
	 * @return The {@link MvcFacesContext} for the current request.
	 */
	public MvcFacesContext getMvcFacesContext() {
		return mvcFacesContext;
	}

	/**
	 * Lifecycle call that releases the request context. This method should be called in a <tt>finally</tt> block after
	 * construction of the object to ensure that all resources are released.
	 */
	public void release() {
		Assert.isTrue(!released, "The MvcFacesRequest has already been released");
		released = true;
		setCurrentInstance(null);
	}

	/**
	 * @return The current {@link MvcFacesContext} instance or <tt>null</tt> if the current request is not being handled
	 * by Spring MVC Faces.
	 */
	public static MvcFacesRequestContext getCurrentInstance() {
		return (MvcFacesRequestContext) currentInstance.get();
	}

	/**
	 * Protected method to set the current instace.
	 * 
	 * @param mvcFacesRequest
	 */
	protected static void setCurrentInstance(MvcFacesRequestContext mvcFacesRequest) {
		if (mvcFacesRequest == null) {
			currentInstance.remove();
		} else {
			currentInstance.set(mvcFacesRequest);
		}
	}
}
