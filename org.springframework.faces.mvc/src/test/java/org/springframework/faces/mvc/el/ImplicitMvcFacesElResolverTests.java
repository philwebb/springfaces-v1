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

import java.security.Principal;

import javax.el.ELContext;

import junit.framework.TestCase;

import org.apache.shale.test.el.MockELContext;
import org.apache.shale.test.mock.MockPrincipal;
import org.easymock.EasyMock;
import org.springframework.faces.mvc.context.ExternalContext;
import org.springframework.faces.mvc.execution.RequestContextHolder;
import org.springframework.faces.mvc.execution.RequestControlContext;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.core.collection.MutableAttributeMap;

public class ImplicitMvcFacesElResolverTests extends TestCase {

	private ImplicitMvcFacesElResolver resolver;
	private RequestControlContext requestContext;
	private ExternalContext externalContext;

	protected void setUp() throws Exception {
		resolver = new ImplicitMvcFacesElResolver();
		requestContext = (RequestControlContext) EasyMock.createMock(RequestControlContext.class);
		externalContext = (ExternalContext) EasyMock.createMock(ExternalContext.class);
		EasyMock.expect(requestContext.getExternalContext()).andStubReturn(externalContext);
		RequestContextHolder.setRequestContext(requestContext);
	}

	private void replay() {
		EasyMock.replay(new Object[] { requestContext, externalContext });
	}

	private void doTest(String property, Class expectedType, Object expectedValue) {
		replay();
		ELContext elContext = new MockELContext();
		Class type = resolver.getType(elContext, null, property);
		assertNotNull(type);
		assertTrue(expectedType.isAssignableFrom(type));
		assertSame(expectedValue, resolver.getValue(elContext, null, property));
	}

	public void testRequestScope() throws Exception {
		MutableAttributeMap expected = new LocalAttributeMap();
		EasyMock.expect(requestContext.getRequestScope()).andStubReturn(expected);
		doTest("requestScope", MutableAttributeMap.class, expected);
	}

	public void testFlashScope() throws Exception {
		MutableAttributeMap expected = new LocalAttributeMap();
		EasyMock.expect(requestContext.getFlashScope()).andStubReturn(expected);
		doTest("flashScope", MutableAttributeMap.class, expected);
	}

	public void testViewScope() throws Exception {
		MutableAttributeMap expected = new LocalAttributeMap();
		EasyMock.expect(requestContext.getViewScope()).andStubReturn(expected);
		doTest("viewScope", MutableAttributeMap.class, expected);
	}

	public void testCurrentUser() throws Exception {
		Principal expected = new MockPrincipal();
		EasyMock.expect(externalContext.getCurrentUser()).andStubReturn(expected);
		doTest("currentUser", Principal.class, expected);
	}
}
