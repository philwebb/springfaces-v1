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
package org.springframework.faces.mvc.view;

import org.springframework.web.servlet.ModelAndView;

/**
 * Interface that can be used to resolve logical JSF view names into actual view IDs. Keeping logical names distinct
 * from actual view IDs can help to keep handlers clean and easy to read and provides a level of abstraction from hard
 * coded view IDs. Resolvers must be able to convert MVC view names to JSF view IDs using {@link #resolveViewId(String)}
 * and reverse the process using {@link #resolveViewName(String)}.
 * 
 * @see SimpleFacesViewIdResolver
 * 
 * @author Phillip Webb
 */
public interface FacesViewIdResolver {

	/**
	 * Resolve the logical view name into a valid JSF view ID.
	 * @param viewName The view name as provided from {@link ModelAndView#getViewName()}
	 * @return The JSF view ID. This is generally a reference to the actual resource that will be processed by the JSF
	 * view handler. For example '/WEB-INF/pages/example.xhtml'
	 */
	String resolveViewId(String viewName);

	/**
	 * Resolve the JSF view ID into the logical view name.
	 * @param viewId The JSF view ID. This is generally a reference to the actual resource that will be processed by the
	 * JSF view handler. For example '/WEB-INF/pages/example.xhtml'
	 * @return The view name as referenced in {@link ModelAndView#getViewName()}
	 */
	String resolveViewName(String viewId);
}
