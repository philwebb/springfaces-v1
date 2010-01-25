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

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.springframework.faces.mvc.context.ExternalContext;
import org.springframework.faces.mvc.execution.MvcFacesExecution;
import org.springframework.faces.mvc.execution.RequestContextHolder;
import org.springframework.faces.mvc.execution.RequestControlContextImpl;
import org.springframework.faces.mvc.servlet.FacesHandler;

public class MvcHandlerELResolverTests extends TestCase {

	private static final String PROPERTY_NAME = "myObject";
	private static final Long VALUE = new Long(123);

	private MvcHandlerELResolver resolver;
	private FacesHandler facesHandler;
	private RequestControlContextImpl requestContext;

	protected void setUp() throws Exception {
		super.setUp();
		this.resolver = new MvcHandlerELResolver();
	}

	private void createRequestContext() {
		MvcFacesExecution execution = (MvcFacesExecution) EasyMock.createMock(MvcFacesExecution.class);
		ExternalContext externalContext = (ExternalContext) EasyMock.createMock(ExternalContext.class);
		this.facesHandler = (FacesHandler) EasyMock.createMock(FacesHandler.class);
		this.requestContext = new RequestControlContextImpl(externalContext, execution, facesHandler);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		if (requestContext != null) {
			this.requestContext.release();
		}
		RequestContextHolder.setRequestContext(null);
		System.out.println("tear down");
	}

	public void testIsAvailable() throws Exception {
		System.out.println("1" + RequestContextHolder.getRequestContext());
		assertFalse("resolver is available and should not be", resolver.isAvailable());
		createRequestContext();
		assertTrue(resolver.isAvailable());
	}

	public void testGet() throws Exception {
		createRequestContext();
		EasyMock.expect(facesHandler.resolveVariable(PROPERTY_NAME)).andReturn(VALUE);
		EasyMock.replay(new Object[] { facesHandler });
		assertEquals(VALUE, resolver.get(PROPERTY_NAME));
		EasyMock.verify(new Object[] { facesHandler });
	}
}
