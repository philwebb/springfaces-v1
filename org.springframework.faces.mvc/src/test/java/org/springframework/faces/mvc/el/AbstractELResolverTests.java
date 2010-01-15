package org.springframework.faces.mvc.el;

import java.util.HashMap;
import java.util.Map;

import javax.el.ELContext;
import javax.el.PropertyNotWritableException;

import junit.framework.TestCase;

import org.springframework.faces.mvc.test.MvcFacesTestUtils;
import org.springframework.faces.mvc.test.MvcFacesTestUtils.MethodCallAssertor;

public class AbstractELResolverTests extends TestCase {

	private static final String BASE_OBJECT = "baseObject";
	private static final Object PROPERTY_NAME = "myObject";
	private static final Object MISSING_PROPERTY_NAME = "doesNotExist";
	private static final Long PROPERTY_VALUE = new Long(123);
	private static final Long REPLACED_PROPERTY_VALUE = new Long(321);

	private AbstractELResolver resolver;
	private ELContext elContext;
	private Map map = new HashMap();

	protected void setUp() throws Exception {
		super.setUp();
		this.resolver = new MockELResolver();
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

	public void testIsReadOnly() throws Exception {
		assertTrue(resolver.isReadOnly(elContext, null, PROPERTY_NAME));
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
		try {
			resolver.setValue(elContext, null, PROPERTY_NAME, REPLACED_PROPERTY_VALUE);
			fail();
		} catch (PropertyNotWritableException e) {
			assertEquals("The property myObject is not writable.", e.getMessage());
		}
	}

	public void testSetValueNotFound() throws Exception {
		resolver.setValue(elContext, null, MISSING_PROPERTY_NAME, REPLACED_PROPERTY_VALUE);
		((MethodCallAssertor) elContext).assertNotCalled("setPropertyResolved");
		assertFalse(map.containsKey(MISSING_PROPERTY_NAME));
	}

	// FIXME test set

	// FIXME test null return support

	// FIXME test custom isAvailable

	private class MockELResolver extends AbstractELResolver {
		protected Object get(String property) {
			return map.get(property);
		}
	}
}