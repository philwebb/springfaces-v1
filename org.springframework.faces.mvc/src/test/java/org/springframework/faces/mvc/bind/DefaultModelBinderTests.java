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
package org.springframework.faces.mvc.bind;

import java.util.Collections;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.faces.mvc.execution.RequestContextHolder;
import org.springframework.faces.mvc.execution.RequestControlContext;
import org.springframework.faces.mvc.execution.ScopeType;
import org.springframework.webflow.core.collection.LocalAttributeMap;

public class DefaultModelBinderTests extends TestCase {

	protected void tearDown() throws Exception {
		super.tearDown();
		RequestContextHolder.setRequestContext(null);
	}

	public void doTestBind(String scopeName, AdditionalBinderConfig additionalBinderConfig, ScopeType scopeType)
			throws Exception {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		Scope scope = null;
		RequestControlContext context = (RequestControlContext) EasyMock.createNiceMock(RequestControlContext.class);
		RequestContextHolder.setRequestContext(context);
		LocalAttributeMap map = new LocalAttributeMap();
		if (scopeType == null) {
			scope = (Scope) EasyMock.createMock(Scope.class);
			EasyMock.expect(scope.get((String) EasyMock.eq("test"), (ObjectFactory) EasyMock.isA(ObjectFactory.class)))
					.andAnswer(new IAnswer() {
						public Object answer() throws Throwable {
							return ((ObjectFactory) EasyMock.getCurrentArguments()[1]).getObject();
						}
					});
			beanFactory.registerScope(scopeName, scope);
			EasyMock.replay(new Object[] { scope });
		} else {
			if (ScopeType.REQUEST.equals(scopeType)) {
				EasyMock.expect(context.getRequestScope()).andStubReturn(map);
			}
			if (ScopeType.FLASH.equals(scopeType)) {
				EasyMock.expect(context.getFlashScope()).andStubReturn(map);
			}
			if (ScopeType.VIEW.equals(scopeType)) {
				EasyMock.expect(context.getViewScope()).andStubReturn(map);
			}
			EasyMock.replay(new Object[] { context });
		}
		DefaultModelBinder binder = new DefaultModelBinder();
		binder.setBeanFactory(beanFactory);
		if (additionalBinderConfig != null) {
			additionalBinderConfig.config(binder);
		}
		binder.afterPropertiesSet();
		binder.bindModel(Collections.singletonMap("test", "result"));
		if (scopeType == null) {
			EasyMock.verify(new Object[] { scope });
		} else {
			assertEquals("result", map.getRequired("test"));
		}
	}

	public void testDefaultsToViewScope() throws Exception {
		doTestBind("view", null, ScopeType.VIEW);
	}

	public void testOnlyWithConfigurableBeanFactory() throws Exception {
		BeanFactory beanFactory = (BeanFactory) EasyMock.createMock(BeanFactory.class);
		DefaultModelBinder binder = new DefaultModelBinder();
		try {
			binder.setBeanFactory(beanFactory);
			fail("Did not throw illeagal argument exception");
		} catch (IllegalArgumentException e) {
		}
	}

	public void testSetSpecificScopeFromBeanFactory() throws Exception {
		doTestBind("session", new AdditionalBinderConfig() {
			public void config(DefaultModelBinder binder) {
				binder.setScope("session");
			}
		}, null);
	}

	public void testSetModelScopeProvider() throws Exception {
		doTestBind("request", new AdditionalBinderConfig() {
			public void config(DefaultModelBinder binder) {
				binder.setModelScopeProvider(ScopeType.REQUEST);
			}
		}, ScopeType.REQUEST);
	}

	private static interface AdditionalBinderConfig {
		public void config(DefaultModelBinder binder);
	}
}
