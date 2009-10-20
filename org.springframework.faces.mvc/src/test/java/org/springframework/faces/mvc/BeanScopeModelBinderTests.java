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
package org.springframework.faces.mvc;

import java.util.Collections;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

public class BeanScopeModelBinderTests extends TestCase {

	private static interface AdditionalBinderConfig {
		public void config(BeanScopeModelBinder binder);
	}

	public void doTestBind(String scopeName, AdditionalBinderConfig additionalBinderConfig) throws Exception {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		Scope scope = (Scope) EasyMock.createMock(Scope.class);
		EasyMock.expect(scope.get((String) EasyMock.eq("test"), (ObjectFactory) EasyMock.isA(ObjectFactory.class)))
				.andAnswer(new IAnswer() {
					public Object answer() throws Throwable {
						return ((ObjectFactory) EasyMock.getCurrentArguments()[1]).getObject();
					}
				});
		beanFactory.registerScope(scopeName, scope);
		BeanScopeModelBinder binder = new BeanScopeModelBinder();
		binder.setBeanFactory(beanFactory);
		EasyMock.replay(new Object[] { scope });
		if (additionalBinderConfig != null) {
			additionalBinderConfig.config(binder);
		}
		binder.afterPropertiesSet();
		binder.bindModel(Collections.singletonMap("test", "result"));
		EasyMock.verify(new Object[] { scope });
	}

	public void testDefaultsToRequestScope() throws Exception {
		doTestBind("request", null);
	}

	public void testOnlyWithConfigurableBeanFactory() throws Exception {
		BeanFactory beanFactory = (BeanFactory) EasyMock.createMock(BeanFactory.class);
		BeanScopeModelBinder binder = new BeanScopeModelBinder();
		try {
			binder.setBeanFactory(beanFactory);
			fail("Did not throw illeagal argument exception");
		} catch (IllegalArgumentException e) {
		}
	}

	public void testSetSpecificScope() throws Exception {
		doTestBind("session", new AdditionalBinderConfig() {
			public void config(BeanScopeModelBinder binder) {
				binder.setScope("session");
			}
		});
	}

	public void testScopeProvider() throws Exception {
		doTestBind("page", new AdditionalBinderConfig() {
			public void config(BeanScopeModelBinder binder) {
				binder.setModelScopeProvider(SpecificModelScopeProvider.PAGE);
			}
		});
	}
}
