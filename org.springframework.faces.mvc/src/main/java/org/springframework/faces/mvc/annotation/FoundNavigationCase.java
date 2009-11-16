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
package org.springframework.faces.mvc.annotation;

import java.lang.reflect.Method;

import org.springframework.core.MethodParameter;
import org.springframework.core.style.ToStringCreator;
import org.springframework.faces.bind.annotation.NavigationCase;
import org.springframework.faces.mvc.NavigationRequestEvent;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.support.WebArgumentResolver;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * A {@link NavigationCase} as found by the {@link NavigationCaseAnnotationLocator} class.
 * 
 * @author Phillip Webb
 */
public final class FoundNavigationCase {

	protected enum FoundNavigationCaseType {
		METHOD, CLASS, PACKAGE
	}

	private NavigationCase navigationCase;
	private Object owner;
	private FoundNavigationCaseType type;

	public FoundNavigationCase(NavigationCase navigationCase, Object owner) {
		Assert.notNull(navigationCase, "navigationCase is required");
		Assert.notNull(owner, "owner is required");
		this.navigationCase = navigationCase;
		this.owner = owner;
		if (owner instanceof Package) {
			this.type = FoundNavigationCaseType.PACKAGE;
		}
		if (owner instanceof Method) {
			this.type = FoundNavigationCaseType.METHOD;
		}
		if (owner instanceof Class) {
			this.type = FoundNavigationCaseType.CLASS;
		}
		if (this.type == null) {
			throw new IllegalArgumentException("owner must be a Method, Class or Package");
		}
	}

	/**
	 * @return The {@link NavigationCase} annotation that was located.
	 */
	public NavigationCase getNavigationCase() {
		return navigationCase;
	}

	/**
	 * @return The owner of the item (either a {@link Method}, {@link Class} or {@link Package})
	 */
	public Object getOwner() {
		return owner;
	}

	/**
	 * @return The type of navigation case that was found.
	 */
	protected FoundNavigationCaseType getType() {
		return type;
	}

	/**
	 * @param event The navigation request event.
	 * @return The outcome of the {@link NavigationCase}. This will either be the value specified in
	 * {@link NavigationCase#to()} or if this is not specified the result of the method call.
	 * @throws Exception
	 */
	public Object getOutcome(NavigationRequestEvent event, Object target, NativeWebRequest request) throws Exception {
		if (StringUtils.hasText(navigationCase.to())) {
			return navigationCase.to();
		}
		if (FoundNavigationCaseType.METHOD.equals(type)) {
			Method method = (Method) owner;
			if (!void.class.equals(method.getReturnType())) {
				if (method.getAnnotation(RequestMapping.class) != null) {
					throw new IllegalStateException("Unable to call method " + method.getName() + " from class "
							+ method.getDeclaringClass() + " in order to resolve @NavigationCase for " + event
							+ " as method also includes @RequestMapping annotation");
				}
				SimpleWebArgumentResolverInvoker invoker = new SimpleWebArgumentResolverInvoker(null,
						new WebArgumentResolver[] { new NavigationRequestEventWebArgumentResolver(event) });
				return invoker.invoke(method, target, request);
			}
		}
		return null;
	}

	public String toString() {
		return new ToStringCreator(this).append("navigationCase", navigationCase).append("type", type).append("owner",
				owner).toString();
	}

	private class NavigationRequestEventWebArgumentResolver implements WebArgumentResolver {

		private NavigationRequestEvent event;

		public NavigationRequestEventWebArgumentResolver(NavigationRequestEvent event) {
			this.event = event;
		}

		public Object resolveArgument(MethodParameter methodParameter, NativeWebRequest webRequest) throws Exception {
			if (String.class.equals(methodParameter.getParameterType())) {
				return event.outcome();
			}
			if (NavigationRequestEvent.class.equals(methodParameter.getParameterType())) {
				return event;
			}
			if (NavigationCase.class.equals(methodParameter.getParameterType())) {
				return FoundNavigationCase.this.navigationCase;
			}
			return UNRESOLVED;
		}
	}
}
