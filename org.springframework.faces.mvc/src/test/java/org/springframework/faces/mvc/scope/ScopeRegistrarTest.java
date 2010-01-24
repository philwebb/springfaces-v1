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
import org.springframework.beans.factory.config.Scope;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.core.collection.MutableAttributeMap;

public class ScopeRegistrarTest extends TestCase {

	private DefaultListableBeanFactory beanFactory;

	protected void setUp() throws Exception {
		beanFactory = new DefaultListableBeanFactory();
		ScopeRegistrar registrar = new ScopeRegistrar();
		registrar.postProcessBeanFactory(beanFactory);
	}

	public void testMissingScope() throws Exception {
		assertNull(beanFactory.getRegisteredScope("missing"));
	}

	public void testHasAllExpectedScopes() throws Exception {
		assertNotNull(beanFactory.getRegisteredScope("request"));
		assertNotNull(beanFactory.getRegisteredScope("flash"));
		assertNotNull(beanFactory.getRegisteredScope("view"));
		assertNotNull(beanFactory.getRegisteredScope("flow"));
	}

	public void testNoContextRequestScope() throws Exception {
		Scope requestScope = beanFactory.getRegisteredScope("request");
		requestScope.remove("test");
	}

	public void testSpringRequestScope() throws Exception {
		Scope requestScope = beanFactory.getRegisteredScope("request");
		RequestAttributes attributes = (RequestAttributes) EasyMock.createMock(RequestAttributes.class);
		EasyMock.expect(attributes.getAttribute("test", 0)).andReturn(null);
		EasyMock.replay(new Object[] { attributes });
		org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(attributes);
		try {
			requestScope.remove("test");
			EasyMock.verify(new Object[] { attributes });
		} finally {
			org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();
		}
	}

	public void testWebflowRequestScope() throws Exception {
		Scope requestScope = beanFactory.getRegisteredScope("request");
		RequestAttributes attributes = (RequestAttributes) EasyMock.createMock(RequestAttributes.class);
		org.springframework.webflow.execution.RequestContext requestContext = (org.springframework.webflow.execution.RequestContext) EasyMock
				.createMock(org.springframework.webflow.execution.RequestContext.class);
		MutableAttributeMap requestScopeMap = new LocalAttributeMap();
		EasyMock.expect(requestContext.getRequestScope()).andStubReturn(requestScopeMap);
		EasyMock.replay(new Object[] { attributes, requestContext });
		// Setup spring as well as webflow
		org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(attributes);
		org.springframework.webflow.execution.RequestContextHolder.setRequestContext(requestContext);
		try {
			requestScopeMap.put("test", "value");
			requestScope.remove("test");
			EasyMock.verify(new Object[] { requestContext, attributes });
			assertTrue(requestScopeMap.isEmpty());
		} finally {
			org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();
			org.springframework.webflow.execution.RequestContextHolder.setRequestContext(null);
		}
	}

	public void testMvcRequestScope() throws Exception {
		Scope requestScope = beanFactory.getRegisteredScope("request");
		RequestAttributes attributes = (RequestAttributes) EasyMock.createMock(RequestAttributes.class);
		org.springframework.faces.mvc.execution.RequestControlContext requestContext = (org.springframework.faces.mvc.execution.RequestControlContext) EasyMock
				.createMock(org.springframework.faces.mvc.execution.RequestControlContext.class);
		MutableAttributeMap requestScopeMap = new LocalAttributeMap();
		EasyMock.expect(requestContext.getRequestScope()).andStubReturn(requestScopeMap);
		EasyMock.replay(new Object[] { attributes, requestContext });
		// Setup spring as well as MVC
		org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(attributes);
		org.springframework.faces.mvc.execution.RequestContextHolder.setRequestContext(requestContext);
		try {
			requestScopeMap.put("test", "value");
			requestScope.remove("test");
			EasyMock.verify(new Object[] { requestContext, attributes });
			assertTrue(requestScopeMap.isEmpty());
		} finally {
			org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();
			org.springframework.faces.mvc.execution.RequestContextHolder.setRequestContext(null);
		}
	}
}
