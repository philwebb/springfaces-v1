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

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.faces.mvc.scope.CompositeScopeTests.MockScopeAvailabilityFilter;

public class CompositeScopeRegistrarTests extends TestCase {

	private MockScopeAvailabilityFilter filter1;
	private MockScopeAvailabilityFilter filter2;
	private Scope scope1;
	private Scope scope2;

	protected void setUp() throws Exception {
		filter1 = new MockScopeAvailabilityFilter();
		filter2 = new MockScopeAvailabilityFilter();
		scope1 = (Scope) EasyMock.createMock(Scope.class);
		scope2 = (Scope) EasyMock.createMock(Scope.class);
		EasyMock.expect(scope1.getConversationId()).andStubReturn("scope1");
		EasyMock.expect(scope2.getConversationId()).andStubReturn("scope2");
		EasyMock.replay(new Object[] { scope1, scope2 });
	}

	private void doTestRegister(CompositeScopeRegistrar registrar) throws Exception {
		ConfigurableListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		registrar.postProcessBeanFactory(beanFactory);

		Scope scope = beanFactory.getRegisteredScope("test");
		assertNull(scope.getConversationId());
		filter2.setAvailable(true);
		assertEquals("scope2", scope.getConversationId());
		filter1.setAvailable(true);
		assertEquals("scope1", scope.getConversationId());
	}

	public void testRegister() throws Exception {
		CompositeScopeRegistrar registrar = new CompositeScopeRegistrar() {
			public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
				FilteredRegistration r;
				r = newFilteredRegistration(beanFactory, filter1);
				r.registerScope("test", scope1);
				r = newFilteredRegistration(beanFactory, filter2);
				r.registerScope("test", scope2);
			}
		};
		doTestRegister(registrar);
	}

	public void testRegisterViaBeanFactory() throws Exception {
		CompositeScopeRegistrar registrar = new CompositeScopeRegistrar() {
			public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
				ConfigurableListableBeanFactory f;
				f = newFilteredRegistrationBeanFactory(beanFactory, filter1);
				f.registerScope("test", scope1);
				f = newFilteredRegistrationBeanFactory(beanFactory, filter2);
				f.registerScope("test", scope2);
			}
		};
		doTestRegister(registrar);
	}

	public void testOrder() throws Exception {
		CompositeScopeRegistrar registrar = new CompositeScopeRegistrar() {
			public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

			}
		};
		assertEquals(Ordered.LOWEST_PRECEDENCE, registrar.getOrder());
	}

	public void testIlleagalBeanFactoryCall() throws Exception {
		try {
			CompositeScopeRegistrar registrar = new CompositeScopeRegistrar() {
				public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
					ConfigurableListableBeanFactory f = newFilteredRegistrationBeanFactory(beanFactory, filter1);
					f.getAliases("test");
				}
			};
			doTestRegister(registrar);
			fail();
		} catch (IllegalStateException e) {
			assertEquals("Unexpected method call public abstract java.lang.String[] org.springframework.beans.factory."
					+ "BeanFactory.getAliases(java.lang.String)", e.getMessage());
		}

	}
}
