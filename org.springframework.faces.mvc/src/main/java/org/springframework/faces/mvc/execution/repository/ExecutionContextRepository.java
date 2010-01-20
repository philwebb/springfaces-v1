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
package org.springframework.faces.mvc.execution.repository;

import org.springframework.faces.mvc.execution.ExecutionContextKey;
import org.springframework.faces.mvc.execution.MvcFacesRequestContext;
import org.springframework.faces.mvc.navigation.RedirectHandler;

/**
 * Central subsystem interface use to save and restore information from {@link MvcFacesRequestContext} so that
 * <tt>flashScope</tt> data remains available across HTTP redirects. Although Spring Faces MVC is primarily RESTful in
 * design a repository is required in order to support <tt>flashScope</tt>. Implementations of interface are responsible
 * for saving data (usually to HTTP session) and returning a {@link ExecutionContextKey} that can be included as part of
 * a {@link RedirectHandler} redirect. The data can later be restored once the redirect completes.
 * <p>
 * Note: This class will be accessed from multiple threads simultaneously, the implementation must be thread safe and
 * must implement a suitable locking strategy.
 * 
 * @see #save(MvcFacesRequestContext)
 * @see #restore(ExecutionContextKey, MvcFacesRequestContext)
 * 
 * @author Phillip Webb
 */
public interface ExecutionContextRepository {

	// FIXME exceptions
	// FIXME test

	/**
	 * Parse the specified string value and return an execution context key.
	 * @param key The encoded key
	 * @return A {@link ExecutionContextKey} instance.
	 */
	ExecutionContextKey parseKey(String key);

	/**
	 * Save <tt>flashScope</tt> data from the specified request context into the repository and return an execution key
	 * that can be used later. If the request context does not include data that needs to be saved a <tt>null</tt> key
	 * should be returned.
	 * @param requestContext The MVC faces request context
	 * @return An execution context key or <tt>null</tt>
	 * @see #restore(ExecutionContextKey, MvcFacesRequestContext)
	 */
	ExecutionContextKey save(MvcFacesRequestContext requestContext);

	/**
	 * Restore <tt>flashScope</tt> data from the repository into the specified context. Any existing context data should
	 * be replaced. Once this method has been called the repository data can be removed.
	 * @param key The execution context key
	 * @param requestContext The MVC faces request context
	 */
	void restore(ExecutionContextKey key, MvcFacesRequestContext requestContext);

	// FIXME throws
}