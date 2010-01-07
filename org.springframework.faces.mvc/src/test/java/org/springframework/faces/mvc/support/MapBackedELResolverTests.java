package org.springframework.faces.mvc.support;

import java.util.HashMap;
import java.util.Map;

import javax.el.ELContext;

import junit.framework.TestCase;

import org.springframework.faces.mvc.MvcFacesTestUtils;
import org.springframework.faces.mvc.MvcFacesTestUtils.MethodCallAssertor;

public class MapBackedELResolverTests extends TestCase {

	private class MockMapBackedELResolver extends MapBackedELResolver {
		protected Map getMap() {
			return map;
		}
	}

	private static final String BASE_OBJECT = "baseObject";
	private static final Object PROPERTY_NAME = "myObject";
	private static final Object MISSING_PROPERTY_NAME = "doesNotExist";
	private static final Long PROPERTY_VALUE = new Long(123);
	private static final Long REPLACED_PROPERTY_VALUE = new Long(321);

	private MapBackedELResolver resolver;
	private ELContext elContext;
	private Map map = new HashMap();

	protected void setUp() throws Exception {
		super.setUp();
		this.resolver = new MockMapBackedELResolver();
		this.elContext = (ELContext) MvcFacesTestUtils.methodTrackingObject(ELContext.class);
		this.map.put(PROPERTY_NAME, PROPERTY_VALUE);
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
		assertEquals(PROPERTY_VALUE, map.get(PROPERTY_NAME));
	}

	public void testSetValueFound() throws Exception {
		resolver.setValue(elContext, null, PROPERTY_NAME, REPLACED_PROPERTY_VALUE);
		((MethodCallAssertor) elContext).assertCalled("setPropertyResolved");
		assertEquals(REPLACED_PROPERTY_VALUE, map.get(PROPERTY_NAME));
	}

	public void testSetValueNotFound() throws Exception {
		resolver.setValue(elContext, null, MISSING_PROPERTY_NAME, REPLACED_PROPERTY_VALUE);
		((MethodCallAssertor) elContext).assertNotCalled("setPropertyResolved");
		assertFalse(map.containsKey(MISSING_PROPERTY_NAME));
	}
}
