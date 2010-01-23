package org.springframework.faces.mvc.el;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.springframework.faces.mvc.execution.RequestContextHolder;
import org.springframework.faces.mvc.execution.RequestControlContext;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.core.collection.MutableAttributeMap;

public class ScopeSearchingElResolverTests extends TestCase {

	private ScopeSearchingElResolver resolver;
	private RequestControlContext requestContext;
	private MutableAttributeMap requestScope;
	private MutableAttributeMap flashScope;
	private MutableAttributeMap viewScope;

	protected void setUp() throws Exception {
		resolver = new ScopeSearchingElResolver();
		requestContext = (RequestControlContext) EasyMock.createMock(RequestControlContext.class);
		requestScope = new LocalAttributeMap();
		flashScope = new LocalAttributeMap();
		viewScope = new LocalAttributeMap();
		EasyMock.expect(requestContext.getRequestScope()).andStubReturn(requestScope);
		EasyMock.expect(requestContext.getFlashScope()).andStubReturn(flashScope);
		EasyMock.expect(requestContext.getViewScope()).andStubReturn(viewScope);
		requestScope.put("request", "requestValue");
		requestScope.put("shared", "requestSharedValue");
		flashScope.put("flash", "flashValue");
		flashScope.put("shared", "flashSharedValue");
		viewScope.put("view", "viewValue");
		viewScope.put("shared", "viewSharedValue");
		RequestContextHolder.setRequestContext(requestContext);
		EasyMock.replay(new Object[] { requestContext });
	}

	protected void tearDown() throws Exception {
		RequestContextHolder.setRequestContext(null);
	}

	public void testHandles() throws Exception {
		assertTrue(resolver.handles("request"));
		assertTrue(resolver.handles("flash"));
		assertTrue(resolver.handles("view"));
		assertTrue(resolver.handles("shared"));
		assertFalse(resolver.handles("missing"));
	}

	public void testGet() throws Exception {
		assertEquals("requestValue", resolver.get("request"));
		assertEquals("flashValue", resolver.get("flash"));
		assertEquals("viewValue", resolver.get("view"));
		assertNull(resolver.get("missing"));
	}

	public void testGetShared() throws Exception {
		assertEquals("requestSharedValue", resolver.get("shared"));
		requestScope.clear();
		assertEquals("flashSharedValue", resolver.get("shared"));
		flashScope.clear();
		assertEquals("viewSharedValue", resolver.get("shared"));
		viewScope.clear();
		assertNull(resolver.get("shared"));
	}

	public void testSet() throws Exception {
		resolver.set("request", "requestValue2");
		assertEquals("requestValue2", requestScope.get("request"));
		resolver.set("flash", "flashValue2");
		assertEquals("flashValue2", flashScope.get("flash"));
		resolver.set("view", "viewValue2");
		assertEquals("viewValue2", viewScope.get("view"));
	}

	public void testSetShared() throws Exception {
		resolver.set("shared", "requestSharedValue2");
		assertEquals("requestSharedValue2", requestScope.get("shared"));
		requestScope.clear();
		resolver.set("shared", "flashSharedValue2");
		assertEquals("flashSharedValue2", flashScope.get("shared"));
		flashScope.clear();
		resolver.set("shared", "viewSharedValue2");
		assertEquals("viewSharedValue2", viewScope.get("shared"));
	}
}
