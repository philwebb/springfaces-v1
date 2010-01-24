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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.core.Ordered;

/**
 * Abstract base implementation of a {@link BeanFactoryPostProcessor} that registers {@link CompositeScope}s. Use the
 * {@link #newFilteredRegistration(ConfigurableListableBeanFactory, ScopeAvailabilityFilter)} or
 * {@link #newFilteredRegistrationBeanFactory(ConfigurableListableBeanFactory, ScopeAvailabilityFilter)} helper methods
 * when implementing {@link #postProcessBeanFactory(ConfigurableListableBeanFactory)}.
 * 
 * @author Phillip Webb
 */
public abstract class CompositeScopeRegistrar implements BeanFactoryPostProcessor, Ordered {

	/**
	 * The {@link ConfigurableListableBeanFactory#registerScope(String, Scope)} method.
	 */
	private static final Method REGISTER_SCOPE_METHOD;
	static {
		try {
			REGISTER_SCOPE_METHOD = ConfigurableListableBeanFactory.class.getMethod("registerScope", new Class[] {
					String.class, Scope.class });
		} catch (Exception e) {
			throw new IllegalStateException("Unable to locate registerScope method", e);
		}
	}

	/**
	 * Map of scope names to {@link CompositeScope}s.
	 */
	private Map scopeNameToCompositeScope = new HashMap();

	/**
	 * Create a new {@link FilteredRegistration} instance that can be used to register scopes with the specified bean
	 * factory. Each scope registered will automatically be added to an underlying {@link CompositeScope}.
	 * @param beanFactory The bean factory that should have scopes registered
	 * @param availabilityFilter The filter for this registration or <tt>null</tt> if no filter is required
	 * @return A {@link FilteredRegistration} instance that can be used to register the actual {@link Scope}
	 * @see #newFilteredRegistrationBeanFactory(ConfigurableListableBeanFactory, ScopeAvailabilityFilter)
	 */
	protected FilteredRegistration newFilteredRegistration(ConfigurableListableBeanFactory beanFactory,
			ScopeAvailabilityFilter availabilityFilter) {
		return new FilteredRegistration(beanFactory, availabilityFilter);
	}

	/**
	 * Helper function that operates in an identical way to
	 * {@link #newFilteredRegistration(ConfigurableListableBeanFactory, ScopeAvailabilityFilter)} but that returns a
	 * {@link ConfigurableListableBeanFactory} instance rather than a {@link FilteredRegistration}. This method can be
	 * useful when working with an existing scope registrar. The only method from the returned bean factory that can be
	 * called is {@link ConfigurableListableBeanFactory#registerScope(String, Scope)}.
	 * @param beanFactory The bean factory that should have scopes registered
	 * @param availabilityFilter The filter for this registration or <tt>null</tt> if no filter is required
	 * @return A {@link ConfigurableListableBeanFactory} where <tt>registerScope</tt> is the only method that can be
	 * used
	 * @see #newFilteredRegistrationBeanFactory(ConfigurableListableBeanFactory, ScopeAvailabilityFilter)
	 */
	protected ConfigurableListableBeanFactory newFilteredRegistrationBeanFactory(
			ConfigurableListableBeanFactory beanFactory, ScopeAvailabilityFilter availabilityFilter) {
		final FilteredRegistration filteredRegistration = newFilteredRegistration(beanFactory, availabilityFilter);
		InvocationHandler invokationHandler = new InvocationHandler() {
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				if (REGISTER_SCOPE_METHOD.equals(method)) {
					filteredRegistration.registerScope((String) args[0], (Scope) args[1]);
					return null;
				}
				throw new IllegalStateException("Unexpected method call " + method);
			}
		};
		return (ConfigurableListableBeanFactory) Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class[] { ConfigurableListableBeanFactory.class }, invokationHandler);
	}

	public abstract void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException;

	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}

	/**
	 * Internal helper class that automatically wraps registered scopes in a {@link CompositeScope}. This class is a
	 * drop in replacement for {@link ConfigurableListableBeanFactory#registerScope(String, Scope)}.
	 */
	protected class FilteredRegistration {
		private ScopeAvailabilityFilter availabilityFilter;
		private ConfigurableListableBeanFactory beanFactory;

		public FilteredRegistration(ConfigurableListableBeanFactory beanFactory,
				ScopeAvailabilityFilter availabilityFilter) {
			this.beanFactory = beanFactory;
			this.availabilityFilter = availabilityFilter;
		}

		/**
		 * See {@link ConfigurableListableBeanFactory#registerScope(String, Scope)}
		 */
		public void registerScope(String scopeName, Scope scope) {
			CompositeScope compositeScope = (CompositeScope) scopeNameToCompositeScope.get(scopeName);
			if (compositeScope == null) {
				compositeScope = new CompositeScope();
				beanFactory.registerScope(scopeName, compositeScope);
				scopeNameToCompositeScope.put(scopeName, compositeScope);
			}
			compositeScope.add(scope, availabilityFilter);
		}
	}
}
