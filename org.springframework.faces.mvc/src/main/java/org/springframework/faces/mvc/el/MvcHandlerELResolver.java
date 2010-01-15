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
package org.springframework.faces.mvc.el;

import javax.el.ELResolver;

import org.springframework.faces.mvc.FacesHandler;
import org.springframework.faces.mvc.execution.MvcFacesRequestContext;
import org.springframework.faces.mvc.execution.MvcFacesRequestContextHolder;

/**
 * A read-only {@link ELResolver} that delegates to a {@link FacesHandler#resolveVariable(String)} when processing a MVC
 * request.
 * 
 * @author Phillip Webb
 */
public class MvcHandlerELResolver extends AbstractELResolver {
	// FIXME update test
	protected boolean isAvailable() {
		return MvcFacesRequestContextHolder.getRequestContext() != null;
	}

	protected Object get(String property) {
		MvcFacesRequestContext mvcFacesRequestContext = MvcFacesRequestContextHolder.getRequestContext();
		return mvcFacesRequestContext.getFacesHandler().resolveVariable(property);
	}
}
