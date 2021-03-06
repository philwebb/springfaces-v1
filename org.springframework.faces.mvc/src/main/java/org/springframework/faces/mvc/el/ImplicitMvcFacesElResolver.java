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

import javax.el.CompositeELResolver;

import org.springframework.faces.mvc.context.ExternalContext;
import org.springframework.faces.mvc.context.ExternalContextHolder;
import org.springframework.faces.mvc.execution.RequestContext;
import org.springframework.faces.mvc.execution.RequestContextHolder;

/**
 * Resolves "implicit" or well-known variables from Faces MVC; for example "viewScope" in an expression like
 * #{viewScope.foo}. The list of implicit flow variables consists of:
 * 
 * <pre>
 * requestScope
 * flashScope
 * viewScope
 * currentUser
 * </pre>
 * 
 * @author Phillip Webb
 */
public class ImplicitMvcFacesElResolver extends CompositeELResolver {

	public ImplicitMvcFacesElResolver() {
		add(new RequestContextElResolver());
		add(new ExternalContextElResolver());
	}

	/**
	 * Resolver for {@link RequestContext} back expressions.
	 */
	private static class RequestContextElResolver extends BeanBackedElResolver {
		public RequestContextElResolver() {
			map("requestScope");
			map("flashScope");
			map("viewScope");
		}

		protected Object getBean() {
			return RequestContextHolder.getRequestContext();
		}
	}

	/**
	 * Resolver for {@link ExternalContext} backed expressions.
	 */
	private static class ExternalContextElResolver extends BeanBackedElResolver {
		public ExternalContextElResolver() {
			map("currentUser");
		}

		protected Object getBean() {
			return ExternalContextHolder.getExternalContext();
		}
	}
}
