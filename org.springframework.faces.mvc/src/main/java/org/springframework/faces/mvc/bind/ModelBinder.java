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
package org.springframework.faces.mvc.bind;

import java.util.Map;

import javax.faces.context.FacesContext;

import org.springframework.web.servlet.ModelAndView;

/**
 * Interface that can be used to bind Spring MVC Models (from {@link ModelAndView}) to faces.
 * 
 * @see DefaultModelBinder
 * 
 * @author Phillip Webb
 */
public interface ModelBinder {

	/**
	 * Bind the specified MVC model in such a way that it can be accessed from JSF. This method can safely access the
	 * {@link FacesContext#getViewRoot()} if access to the current view is required.
	 * 
	 * @param facesHandler
	 * @param model
	 */
	void bindModel(Map model);
}
