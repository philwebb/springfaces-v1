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
package org.springframework.faces.mvc.scope;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.faces.mvc.execution.ScopeType;
import org.springframework.webflow.scope.FlashScope;
import org.springframework.webflow.scope.ViewScope;

/**
 * Registers the Spring Faces MVC Scopes and Spring Web Flow bean scopes with a {@link ConfigurableListableBeanFactory}.
 * Use this registrar when running in a mixed MVC Faces and WebFlow environment in preference to the standard Web Flow
 * {@link org.springframework.webflow.scope.ScopeRegistrar ScopeRegistrar}.
 * 
 * @see Scope
 * 
 * @author Ben Hale
 * @author Phillip Webb
 */
public class ScopeRegistrar extends CompositeScopeRegistrar {

	private static final ScopeAvailabilityFilter WEB_FLOW_FILTER = new ScopeAvailabilityFilter() {
		public boolean isAvailable(Scope scope) {
			return org.springframework.webflow.execution.RequestContextHolder.getRequestContext() != null;
		}
	};

	private static final ScopeAvailabilityFilter MVC_FACES_FILTER = new ScopeAvailabilityFilter() {
		public boolean isAvailable(Scope scope) {
			return org.springframework.faces.mvc.execution.RequestContextHolder.getRequestContext() != null;
		}
	};

	private static final ScopeAvailabilityFilter SPRING_FILTER = new ScopeAvailabilityFilter() {

		public boolean isAvailable(Scope scope) {
			return org.springframework.web.context.request.RequestContextHolder.getRequestAttributes() != null;
		}
	};

	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		// Register WebFlow Scopes
		org.springframework.webflow.scope.ScopeRegistrar webFlowRegistrar = new org.springframework.webflow.scope.ScopeRegistrar();
		webFlowRegistrar.postProcessBeanFactory(newFilteredRegistrationBeanFactory(beanFactory, WEB_FLOW_FILTER));

		// Register MVC Faces Scopes
		FilteredRegistration mvcFacesRegistration = newFilteredRegistration(beanFactory, MVC_FACES_FILTER);
		mvcFacesRegistration.registerScope(ScopeType.REQUEST.getLabel().toLowerCase(), new RequestScope());
		mvcFacesRegistration.registerScope(ScopeType.FLASH.getLabel().toLowerCase(), new FlashScope());
		mvcFacesRegistration.registerScope(ScopeType.VIEW.getLabel().toLowerCase(), new ViewScope());

		// Register standard Spring request scope
		FilteredRegistration springRegistration = newFilteredRegistration(beanFactory, SPRING_FILTER);
		springRegistration.registerScope("request", new org.springframework.web.context.request.RequestScope());
	}
}
