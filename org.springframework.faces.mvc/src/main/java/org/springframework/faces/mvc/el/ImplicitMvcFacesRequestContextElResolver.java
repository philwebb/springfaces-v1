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

import org.springframework.faces.mvc.execution.MvcFacesRequestContext;
import org.springframework.faces.mvc.execution.MvcFacesRequestContextHolder;

/**
 * Resolves "implicit" or well-known variables from {@link MvcFacesRequestContext}; for example "viewScope" in an
 * expression like #{viewScope.foo}. The list of implicit flow variables consists of:
 * 
 * <pre>
 * requestScope
 * flashScope
 * viewScope
 * </pre>
 * 
 * @author Phillip Webb
 */
public class ImplicitMvcFacesRequestContextElResolver extends BeanBackedElResolver {
	// FIXME test
	public ImplicitMvcFacesRequestContextElResolver() {
		map("requestScope");
		map("flashScope");
		map("viewScope");
	}

	protected Object getBean() {
		return MvcFacesRequestContextHolder.getRequestContext();
	}
}
