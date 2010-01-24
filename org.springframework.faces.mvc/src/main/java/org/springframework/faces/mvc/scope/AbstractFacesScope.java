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
package org.springframework.faces.mvc.scope;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.faces.mvc.execution.RequestContext;
import org.springframework.faces.mvc.execution.RequestContextHolder;
import org.springframework.webflow.core.collection.MutableAttributeMap;

/**
 * Base class for {@link Scope} implementations that access a MVC Faces scope from the current request.
 * 
 * @author Keith Donald
 * @author Phillip Webb
 */
public abstract class AbstractFacesScope implements Scope {

	/**
	 * Logger, usable by subclasses.
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	public Object get(String name, ObjectFactory objectFactory) {
		MutableAttributeMap scope = getScope();
		Object scopedObject = scope.get(name);
		if (scopedObject == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("No scoped instance '" + name + "' found; creating new instance");
			}
			scopedObject = objectFactory.getObject();
			scope.put(name, scopedObject);
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("Returning scoped instance '" + name + "'");
			}
		}
		return scopedObject;
	}

	public Object remove(String name) {
		return getScope().remove(name);
	}

	/**
	 * Template method that returns the target scope map.
	 * @throws IllegalStateException if the scope could not be accessed
	 */
	protected abstract MutableAttributeMap getScope() throws IllegalStateException;

	/**
	 * Always returns <code>null</code> as most MVC Faces scopes do not have obvious conversation ids. Subclasses should
	 * override this method where conversation ids can be intelligently returned.
	 * @return always returns <code>null</code>
	 */
	public String getConversationId() {
		return null;
	}

	/**
	 * Will not register a destruction callback as Spring Faces MVC does not support destruction of scoped beans.
	 * Subclasses should override this method where where destruction can adequately be accomplished.
	 * @param name the name of the bean to register the callback for
	 * @param callback the callback to execute
	 */
	public void registerDestructionCallback(String name, Runnable callback) {
		logger.warn("Destruction callback for '" + name + "' was not registered. Spring Faces MVC does not "
				+ "support destruction of scoped beans.");
	}

	/**
	 * Returns the current {@link RequestContext} or throws an exception if the context is unavailable.
	 * @return The request context
	 * @throws IllegalStateException if the context is unavailable
	 */
	protected RequestContext getRequiredRequestContext() {
		RequestContext context = RequestContextHolder.getRequestContext();
		if (context == null) {
			throw new IllegalStateException(
					"No request context bound to this thread; to access Faces MVC scoped beans "
							+ "you must be running in a Faces MVC execution request");
		}
		return context;
	}

}
