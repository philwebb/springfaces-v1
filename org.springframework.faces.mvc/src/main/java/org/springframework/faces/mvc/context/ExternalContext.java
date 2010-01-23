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

import java.security.Principal;
import java.util.Locale;

import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.core.collection.ParameterMap;
import org.springframework.webflow.core.collection.SharedAttributeMap;

/**
 * A facade that provides normalized access to an external system that has called into the Faces MVC system.
 * <p>
 * This context object provides a normalized interface for internal MVC artifacts to use to reason on and manipulate the
 * state of an external actor calling into MVC. It represents the context about a single, <i>external</i> client request
 * to manipulate a flow execution.
 * <p>
 * The design of this interface was inspired by the WebFlow and JSF ExternalContext abstraction and shares the same name
 * for consistency.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 * @author Jeremy Grelle
 * @author Scott Andrews
 */
public interface ExternalContext {

	/**
	 * Returns the logical path to the application hosting this external context.
	 * @return the context path
	 */
	public String getContextPath();

	/**
	 * Provides access to the parameters associated with the user request that led to SWF being called. This map is
	 * expected to be immutable and cannot be changed.
	 * @return the immutable request parameter map
	 */
	public ParameterMap getRequestParameterMap();

	/**
	 * Provides access to the external request attribute map, providing a storage for data local to the current user
	 * request and accessible to both internal and external SWF artifacts.
	 * @return the mutable request attribute map
	 */
	public MutableAttributeMap getRequestMap();

	/**
	 * Provides access to the external session map, providing a storage for data local to the current user session and
	 * accessible to both internal and external SWF artifacts.
	 * @return the mutable session attribute map
	 */
	public SharedAttributeMap getSessionMap();

	/**
	 * Provides access to the <i>global</i> external session map, providing a storage for data globally accross the user
	 * session and accessible to both internal and external SWF artifacts.
	 * <p>
	 * Note: most external context implementations do not distinguish between the concept of a "local" user session
	 * scope and a "global" session scope. The Portlet world does, but not the Servlet for example. In those cases
	 * calling this method returns the same map as calling {@link #getSessionMap()}.
	 * @return the mutable global session attribute map
	 */
	public SharedAttributeMap getGlobalSessionMap();

	/**
	 * Provides access to the external application map, providing a storage for data local to the current user
	 * application and accessible to both internal and external SWF artifacts.
	 * @return the mutable application attribute map
	 */
	public SharedAttributeMap getApplicationMap();

	/**
	 * Returns true if the current request is an asynchronous Ajax request.
	 * @return true if the current request is an Ajax request
	 */
	public boolean isAjaxRequest();

	/**
	 * Provides access to the user's principal security object.
	 * @return the user principal
	 */
	public Principal getCurrentUser();

	/**
	 * Returns the client locale.
	 * @return the locale
	 */
	public Locale getLocale();

	/**
	 * Provides access to the context object for the current environment.
	 * @return the environment specific context object
	 */
	public Object getNativeContext();

	/**
	 * Provides access to the request object for the current environment.
	 * @return the environment specific request object.
	 */
	public Object getNativeRequest();

	/**
	 * Provides access to the response object for the current environment.
	 * @return the environment specific response object.
	 */
	public Object getNativeResponse();
}