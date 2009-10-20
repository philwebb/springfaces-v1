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
package org.springframework.faces.mvc.support;

import java.util.HashMap;
import java.util.Map;

import javax.el.ELContext;

import junit.framework.TestCase;

import org.springframework.faces.mvc.support.PageScopeELResolver;
import org.springframework.faces.mvc.support.MvcFacesTestUtils.MethodCallAssertor;

public class PageScopeELResolverTest extends TestCase {

	private class MockPageScopeELResolver extends PageScopeELResolver {
		protected Map getPageScope() {
			return pageScope;
		}
	}

	private static final String BASE_OBJECT = "baseObject";
	private static final Object PROPERTY_NAME = "myObject";
	private static final Object MISSING_PROPERTY_NAME = "doesNotExist";
	private static final Long PROPERTY_VALUE = new Long(123);
	private static final Long REPLACED_PROPERTY_VALUE = new Long(321);

	private PageScopeELResolver resolver;
	private ELContext elContext;
	private Map pageScope = new HashMap();

	protected void setUp() throws Exception {
		super.setUp();
		this.resolver = new MockPageScopeELResolver();
		this.elContext = (ELContext) MvcFacesTestUtils.methodTrackingObject(ELContext.class);
		this.pageScope.put(PROPERTY_NAME, PROPERTY_VALUE);
	}

	public void testGetCommonPropertyType() throws Exception {
		assertEquals(Object.class, resolver.getCommonPropertyType(elContext, null));
		assertEquals(null, resolver.getCommonPropertyType(elContext, BASE_OBJECT));
	}

	public void testGetFeatureDescriptors() throws Exception {
		assertNull(resolver.getFeatureDescriptors(elContext, null));
		assertNull(resolver.getFeatureDescriptors(elContext, BASE_OBJECT));
	}

	public void testGetTypeNonNullBase() throws Exception {
		assertNull(resolver.getType(elContext, BASE_OBJECT, PROPERTY_NAME));
	}

	public void testGetTypeFound() throws Exception {
		assertEquals(Long.class, resolver.getType(elContext, null, PROPERTY_NAME));
		((MethodCallAssertor) elContext).assertCalled("setPropertyResolved");
	}

	public void testGetTypeNotFound() throws Exception {
		assertNull(resolver.getType(elContext, null, MISSING_PROPERTY_NAME));
		((MethodCallAssertor) elContext).assertNotCalled("setPropertyResolved");
	}

	public void testGetValueNonNullBase() throws Exception {
		assertNull(resolver.getValue(elContext, BASE_OBJECT, PROPERTY_NAME));
	}

	public void testGetValueFound() throws Exception {
		assertEquals(PROPERTY_VALUE, resolver.getValue(elContext, null, PROPERTY_NAME));
		((MethodCallAssertor) elContext).assertCalled("setPropertyResolved");
	}

	public void testGetValueNotFound() throws Exception {
		assertNull(resolver.getValue(elContext, null, MISSING_PROPERTY_NAME));
		((MethodCallAssertor) elContext).assertNotCalled("setPropertyResolved");
	}

	public void testIsReadOnlyNonNullBase() throws Exception {
		assertFalse(resolver.isReadOnly(elContext, BASE_OBJECT, PROPERTY_NAME));
	}

	public void testIsReadOnlyFound() throws Exception {
		assertFalse(resolver.isReadOnly(elContext, null, PROPERTY_NAME));
		((MethodCallAssertor) elContext).assertCalled("setPropertyResolved");
	}

	public void testIsReadOnlyNotFound() throws Exception {
		assertFalse(resolver.isReadOnly(elContext, null, MISSING_PROPERTY_NAME));
		((MethodCallAssertor) elContext).assertNotCalled("setPropertyResolved");
	}

	public void testSetValueNonNullBase() throws Exception {
		resolver.setValue(elContext, BASE_OBJECT, PROPERTY_NAME, REPLACED_PROPERTY_VALUE);
		((MethodCallAssertor) elContext).assertNotCalled("setPropertyResolved");
		assertEquals(PROPERTY_VALUE, pageScope.get(PROPERTY_NAME));
	}

	public void testSetValueFound() throws Exception {
		resolver.setValue(elContext, null, PROPERTY_NAME, REPLACED_PROPERTY_VALUE);
		((MethodCallAssertor) elContext).assertCalled("setPropertyResolved");
		assertEquals(REPLACED_PROPERTY_VALUE, pageScope.get(PROPERTY_NAME));
	}

	public void testSetValueNotFound() throws Exception {
		resolver.setValue(elContext, null, MISSING_PROPERTY_NAME, REPLACED_PROPERTY_VALUE);
		((MethodCallAssertor) elContext).assertNotCalled("setPropertyResolved");
		assertFalse(pageScope.containsKey(MISSING_PROPERTY_NAME));
	}
}
