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
package org.springframework.faces.mvc.annotation.support;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.MethodParameter;
import org.springframework.core.style.ToStringCreator;
import org.springframework.faces.mvc.navigation.NavigationLocation;
import org.springframework.faces.mvc.navigation.NavigationRequestEvent;
import org.springframework.faces.mvc.navigation.annotation.NavigationCase;
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

	private static final Log logger = LogFactory.getLog(FoundNavigationCase.class);

	/**
	 * The type of navigation case.
	 */
	protected enum FoundNavigationCaseType {
		METHOD, CLASS, PACKAGE
	}

	private NavigationCase navigationCase;
	private Object owner;
	private FoundNavigationCaseType type;

	/**
	 * Constructor.
	 * @param navigationCase The navigation case annotation
	 * @param owner The owner object (either a {@link Package}, {@link Method} or {@link Class})
	 */
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
	 * Returns the {@link NavigationCase} annotation that was located.
	 * @return The navigation case annotation
	 */
	public NavigationCase getNavigationCase() {
		return navigationCase;
	}

	/**
	 * Returns the owner of the item (either a {@link Method}, {@link Class} or {@link Package}).
	 * @return The owner
	 */
	public Object getOwner() {
		return owner;
	}

	/**
	 * Returns the type of navigation case that was found.
	 * @return the type of navigation case
	 */
	protected FoundNavigationCaseType getType() {
		return type;
	}

	/**
	 * Gets the {@link NavigationLocation} outcome for the found navigation case, executing annotated methods as
	 * required.
	 * @param event The navigation request event
	 * @return The outcome of the {@link NavigationCase}. This will either be the value specified in
	 * {@link NavigationCase#to()} or if this is not specified the result of the method call
	 * @throws Exception on error
	 */
	public NavigationLocation getOutcome(NavigationRequestEvent event, Object target, NativeWebRequest request,
			AnnotatedMethodInvokerFactory invokerFactory) throws Exception {
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
				AnnotatedMethodInvoker invoker = invokerFactory
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

	/**
	 * Internal {@link WebArgumentResolver} implementation that supports the {@link NavigationRequestEvent} and
	 * {@link NavigationCase} types as well as resolving <tt>String</tt>s to JSF outcomes.
	 */
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
