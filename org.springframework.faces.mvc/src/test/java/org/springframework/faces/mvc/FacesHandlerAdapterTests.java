package org.springframework.faces.mvc;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.shale.test.mock.MockServlet;
import org.easymock.EasyMock;
import org.springframework.web.context.support.StaticWebApplicationContext;

public class FacesHandlerAdapterTests extends TestCase {

	public static class TrackingMockServlet extends MockServlet {
		private ServletRequest request;
		private ServletResponse response;
		private ServletConfig config;

		public void init(ServletConfig config) throws ServletException {
			this.config = config;
		}

		public void service(ServletRequest request, ServletResponse response) throws IOException, ServletException {
			this.request = request;
			this.response = response;
		}

		public void assertSame(ServletRequest request, ServletResponse response) {
			Assert.assertSame(request, this.request);
			Assert.assertSame(response, this.response);
		}

		public ServletConfig getConfig() {
			return config;
		}
	}

	private FacesHandlerAdapter adapter;
	private StaticWebApplicationContext context;

	protected void setUp() throws Exception {
		super.setUp();
		this.adapter = new FacesHandlerAdapter();
		this.context = new StaticWebApplicationContext();
		adapter.setApplicationContext(context);
		adapter.setFacesServletClass(TrackingMockServlet.class);
	}

	public void testSupports() throws Exception {
		FacesHandler facesHandler = EasyMock.createMock(FacesHandler.class);
		assertTrue(adapter.supports(facesHandler));
		assertFalse(adapter.supports(""));
	}

	public void testCustomFacesServlet() throws Exception {
		adapter.setFacesServletClass(MockServlet.class);
		Servlet servlet = adapter.newFacesServlet();
		assertEquals(MockServlet.class, servlet.getClass());
	}

	public void testCustomFacesServletWrongClass() throws Exception {
		adapter.setFacesServletClass(Long.class);
		try {
			adapter.newFacesServlet();
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals("Unable to instanciate face servlet from the specified class java.lang.Long", e.getMessage());
		}
	}

	public void testCustomFacesServletNull() throws Exception {
		try {
			adapter.setFacesServletClass(null);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals("The facesServletClass is required", e.getMessage());
		}
	}

	public void testDoHandle() throws Exception {
		adapter.afterPropertiesSet();
		HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
		HttpServletResponse response = EasyMock.createMock(HttpServletResponse.class);
		FacesHandler handler = EasyMock.createMock(FacesHandler.class);
		adapter.doHandle(request, response, handler);
		((TrackingMockServlet) adapter.getFacesServlet()).assertSame(request, response);
	}

	// FIXME test do handle with exceptions

	public void testDefaultViewIdResolver() throws Exception {
		adapter.afterPropertiesSet();
		assertEquals(SimpleFacesViewIdResolver.class, adapter.getFacesViewIdResolver().getClass());
		// Quick test of the resolver to ensure afterPropertiesSet etc has been called.
		assertEquals("/WEB-INF/pages/test.xhtml", ((SimpleFacesViewIdResolver) adapter.getFacesViewIdResolver())
				.resolveViewId("test"));
	}

	public void testDefaultActionUrlMapper() throws Exception {
		adapter.afterPropertiesSet();
		assertEquals(PageEncodedActionUrlMapper.class, adapter.getActionUrlMapper().getClass());
	}

	public void testDefaultRedirectHandler() throws Exception {
		adapter.afterPropertiesSet();
		assertEquals(DefaultRedirectHandler.class, adapter.getRedirectHandler().getClass());
	}

	public void testDefaultModelBinder() throws Exception {
		adapter.afterPropertiesSet();
		assertEquals(BeanScopeModelBinder.class, adapter.getModelBindingExecutor().getModelBinder().getClass());
		// Quick test that the beanFactory has been injected
		((BeanScopeModelBinder) adapter.getModelBindingExecutor().getModelBinder()).afterPropertiesSet();
	}

	public void testFacesServletInit() throws Exception {
		Properties initParameters = new Properties();
		initParameters.put("javax.faces.STATE_SAVING_METHOD", "client");
		adapter.setInitParameters(initParameters);
		adapter.afterPropertiesSet();
		TrackingMockServlet servlet = (TrackingMockServlet) adapter.getFacesServlet();
		assertEquals("client", servlet.getConfig().getInitParameter("javax.faces.STATE_SAVING_METHOD"));
	}

	public void testIsPageScopeSupported() throws Exception {
		adapter.afterPropertiesSet();
		assertTrue(adapter.isPageScopeSupported());
		adapter.setPageScopeSupported(false);
		assertFalse(adapter.isPageScopeSupported());
	}

	public void testCustomActionUrlMapper() throws Exception {
		ActionUrlMapper actionUrlMapper = EasyMock.createMock(ActionUrlMapper.class);
		adapter.setActionUrlMapper(actionUrlMapper);
		adapter.afterPropertiesSet();
		assertSame(actionUrlMapper, adapter.getActionUrlMapper());
	}

	public void testCustomActionUrlMapperNull() throws Exception {
		try {
			adapter.setActionUrlMapper(null);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals("The actionUrlMapper is required", e.getMessage());
		}
	}

	public void testCustomModelBinder() throws Exception {
		ModelBinder modelBinder = EasyMock.createMock(ModelBinder.class);
		adapter.setModelBinder(modelBinder);
		adapter.afterPropertiesSet();
		assertSame(modelBinder, adapter.getModelBindingExecutor().getModelBinder());
	}

	public void testCustomModelBinderNull() throws Exception {
		try {
			adapter.setModelBinder(null);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals("The modelBinder is required", e.getMessage());
		}
	}

}
