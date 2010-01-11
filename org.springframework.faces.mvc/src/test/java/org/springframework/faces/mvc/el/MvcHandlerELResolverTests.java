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

import javax.el.ELContext;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.springframework.faces.mvc.FacesHandler;
import org.springframework.faces.mvc.context.MvcFacesContext;
import org.springframework.faces.mvc.el.MvcHandlerELResolver;
import org.springframework.faces.mvc.execution.MvcFacesRequestControlContextImpl;
import org.springframework.faces.mvc.test.MvcFacesTestUtils;
import org.springframework.faces.mvc.test.MvcFacesTestUtils.MethodCallAssertor;

public class MvcHandlerELResolverTests extends TestCase {

	private static final String PROPERTY_NAME = "myObject";
	private static final Long VALUE = new Long(123);

	private MvcHandlerELResolver resolver;
	private ELContext elContext;
	private MvcFacesRequestControlContextImpl requestContext;
	private FacesHandler facesHandler;

	protected void setUp() throws Exception {
		super.setUp();
		this.resolver = new MvcHandlerELResolver();
		this.elContext = (ELContext) MvcFacesTestUtils.methodTrackingObject(ELContext.class);
		MvcFacesContext mvcFacesContext = (MvcFacesContext) EasyMock.createMock(MvcFacesContext.class);
		this.facesHandler = (FacesHandler) EasyMock.createMock(FacesHandler.class);
		this.requestContext = new MvcFacesRequestControlContextImpl(mvcFacesContext, facesHandler);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		this.requestContext.release();
	}

	public void testGetTypeFound() throws Exception {
		EasyMock.expect(facesHandler.resolveVariable(PROPERTY_NAME)).andReturn(VALUE);
		EasyMock.replay(new Object[] { facesHandler });
		Class type = resolver.getType(elContext, null, PROPERTY_NAME);
		assertEquals(VALUE.getClass(), type);
		EasyMock.verify(new Object[] { facesHandler });
		((MethodCallAssertor) elContext).assertCalled("setPropertyResolved");
	}

	public void testGetValueFound() throws Exception {
		EasyMock.expect(facesHandler.resolveVariable(PROPERTY_NAME)).andReturn(VALUE);
		EasyMock.replay(new Object[] { facesHandler });
		Object value = resolver.getValue(elContext, null, PROPERTY_NAME);
		assertSame(VALUE, value);
		EasyMock.verify(new Object[] { facesHandler });
		((MethodCallAssertor) elContext).assertCalled("setPropertyResolved");
	}

	public void testGetTypeNotFound() throws Exception {
		EasyMock.expect(facesHandler.resolveVariable(PROPERTY_NAME)).andReturn(null);
		EasyMock.replay(new Object[] { facesHandler });
		Class type = resolver.getType(elContext, null, PROPERTY_NAME);
		assertNull(type);
		EasyMock.verify(new Object[] { facesHandler });
		((MethodCallAssertor) elContext).assertNotCalled("setPropertyResolved");
	}

	public void testGetValueNotFound() throws Exception {
		EasyMock.expect(facesHandler.resolveVariable(PROPERTY_NAME)).andReturn(null);
		EasyMock.replay(new Object[] { facesHandler });
		Object value = resolver.getValue(elContext, null, PROPERTY_NAME);
		assertNull(value);
		EasyMock.verify(new Object[] { facesHandler });
		((MethodCallAssertor) elContext).assertNotCalled("setPropertyResolved");
	}

	public void testGetValueNonNullBase() throws Exception {
		EasyMock.replay(new Object[] { facesHandler });
		Object value = resolver.getValue(elContext, "base", PROPERTY_NAME);
		assertNull(value);
		EasyMock.verify(new Object[] { facesHandler });
		((MethodCallAssertor) elContext).assertNotCalled("setPropertyResolved");
	}

	public void testIsReadOnly() throws Exception {
		assertTrue(resolver.isReadOnly(elContext, null, null));
	}

	public void testGetFeatureDescriptorsIsNull() throws Exception {
		assertNull(resolver.getFeatureDescriptors(elContext, "base"));
	}

	public void testGetCommonPropertyTypeNonNullBase() throws Exception {
		assertEquals(Object.class, resolver.getCommonPropertyType(elContext, null));
	}

	public void testGetCommonPropertyTypeNullBase() throws Exception {
		assertEquals(null, resolver.getCommonPropertyType(elContext, "base"));
	}

	public void testSetValueDoesNothing() throws Exception {
		resolver.setValue(elContext, "base", "property", "value");
	}
}
