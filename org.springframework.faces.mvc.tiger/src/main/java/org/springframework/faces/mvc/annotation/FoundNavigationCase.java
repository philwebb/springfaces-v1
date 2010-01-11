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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.MethodParameter;
import org.springframework.core.style.ToStringCreator;
import org.springframework.faces.mvc.bind.annotation.NavigationCase;
import org.springframework.faces.mvc.navigation.NavigationLocation;
import org.springframework.faces.mvc.navigation.NavigationRequestEvent;
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
final class FoundNavigationCase {

	private static final Log logger = LogFactory.getLog(FoundNavigationCase.class);

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
		if (owner instanceof Class<?>) {
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
	public NavigationLocation getOutcome(NavigationRequestEvent event, Object target, NativeWebRequest request,
			FacesControllerAnnotatedMethodInvokerFactory invokerFactory) throws Exception {
		Object location = null;
		if (StringUtils.hasText(navigationCase.to())) {
			location = navigationCase.to();
		}
		if (FoundNavigationCaseType.METHOD.equals(type)) {
			Method method = (Method) owner;
			if (method.getAnnotation(RequestMapping.class) != null) {
				if (location == null) {
					throw new IllegalStateException("Unable to call method " + method.getName() + " from class "
							+ method.getDeclaringClass() + " in order to resolve empty @NavigationCase.to() for "
							+ event + " as method also includes @RequestMapping annotation");
				}
				if (logger.isDebugEnabled()) {
					logger.debug("@NavigationCase method will not be "
							+ "called as @RequestMapping annotation also present");
				}
			} else {
				FacesControllerAnnotatedMethodInvoker invoker = invokerFactory
						.newInvoker(new NavigationRequestEventWebArgumentResolver(event));
				Object methodResult = invoker.invokeOnActiveHandler(method, target, request);
				location = methodResult != null ? methodResult : location;
			}
		}
		return new NavigationLocation(location, navigationCase.popup(), navigationCase.fragments());
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
				return event.getOutcome();
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
