package org.springframework.faces.mvc.support;

import javax.el.ELContext;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.springframework.faces.mvc.FacesHandler;
import org.springframework.faces.mvc.MvcFacesTestUtils;
import org.springframework.faces.mvc.MvcFacesTestUtils.MethodCallAssertor;

public class MvcHandlerELResolverTests extends TestCase {

	private static final String PROPERTY_NAME = "myObject";
	private static final Long VALUE = new Long(123);

	private MvcHandlerELResolver resolver;
	private ELContext elContext;
	private MvcFacesRequestContext requestContext;
	private FacesHandler facesHandler;

	protected void setUp() throws Exception {
		super.setUp();
		this.resolver = new MvcHandlerELResolver();
		this.elContext = (ELContext) MvcFacesTestUtils.methodTrackingObject(ELContext.class);
		MvcFacesContext mvcFacesContext = EasyMock.createMock(MvcFacesContext.class);
		this.facesHandler = EasyMock.createMock(FacesHandler.class);
		this.requestContext = new MvcFacesRequestContext(mvcFacesContext, facesHandler);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		this.requestContext.release();
	}

	public void testGetTypeFound() throws Exception {
		EasyMock.expect(facesHandler.resolveVariable(PROPERTY_NAME)).andReturn(VALUE);
		EasyMock.replay(facesHandler);
		Class type = resolver.getType(elContext, null, PROPERTY_NAME);
		assertEquals(VALUE.getClass(), type);
		EasyMock.verify(facesHandler);
		((MethodCallAssertor) elContext).assertCalled("setPropertyResolved");
	}

	public void testGetValueFound() throws Exception {
		EasyMock.expect(facesHandler.resolveVariable(PROPERTY_NAME)).andReturn(VALUE);
		EasyMock.replay(facesHandler);
		Object value = resolver.getValue(elContext, null, PROPERTY_NAME);
		assertSame(VALUE, value);
		EasyMock.verify(facesHandler);
		((MethodCallAssertor) elContext).assertCalled("setPropertyResolved");
	}

	public void testGetTypeNotFound() throws Exception {
		EasyMock.expect(facesHandler.resolveVariable(PROPERTY_NAME)).andReturn(null);
		EasyMock.replay(facesHandler);
		Class type = resolver.getType(elContext, null, PROPERTY_NAME);
		assertNull(type);
		EasyMock.verify(facesHandler);
		((MethodCallAssertor) elContext).assertNotCalled("setPropertyResolved");
	}

	public void testGetValueNotFound() throws Exception {
		EasyMock.expect(facesHandler.resolveVariable(PROPERTY_NAME)).andReturn(null);
		EasyMock.replay(facesHandler);
		Object value = resolver.getValue(elContext, null, PROPERTY_NAME);
		assertNull(value);
		EasyMock.verify(facesHandler);
		((MethodCallAssertor) elContext).assertNotCalled("setPropertyResolved");
	}

	public void testGetValueNonNullBase() throws Exception {
		EasyMock.replay(facesHandler);
		Object value = resolver.getValue(elContext, "base", PROPERTY_NAME);
		assertNull(value);
		EasyMock.verify(facesHandler);
		((MethodCallAssertor) elContext).assertNotCalled("setPropertyResolved");
	}

	public void testIsReadOnly() throws Exception {
		assertTrue(resolver.isReadOnly(elContext, null, null));
	}
}
