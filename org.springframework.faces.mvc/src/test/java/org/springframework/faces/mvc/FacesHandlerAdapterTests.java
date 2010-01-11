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
package org.springframework.faces.mvc;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.commons.collections.EnumerationUtils;
import org.apache.shale.test.mock.MockServlet;
import org.easymock.EasyMock;
import org.springframework.faces.mvc.support.MvcFacesContext;
import org.springframework.faces.mvc.support.MvcFacesRequestContext;
import org.springframework.faces.mvc.support.MvcFacesRequestControlContextImpl;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.StaticWebApplicationContext;

public class FacesHandlerAdapterTests extends TestCase {

	private FacesHandlerAdapter adapter;
	private StaticWebApplicationContext context;

	protected void setUp() throws Exception {
		super.setUp();
		this.adapter = new FacesHandlerAdapter();
		this.adapter.setBeanName("testAdapterBean");
		this.context = new StaticWebApplicationContext();
		adapter.setApplicationContext(context);
		adapter.setFacesServletClass(TrackingMockServlet.class);
	}

	public void testSupports() throws Exception {
		FacesHandler facesHandler = (FacesHandler) EasyMock.createMock(FacesHandler.class);
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

	public void testDelegatingServletConfig() throws Exception {
		Properties initParameters = new Properties();
		initParameters.setProperty("testkey", "testvalue");
		ServletContext servletContext = (ServletContext) EasyMock.createMock(ServletContext.class);
		adapter.setInitParameters(initParameters);
		adapter.setServletContext(servletContext);
		adapter.setOverrideInitParameters(false);
		adapter.setFacesServletClass(MockServlet.class);
		adapter.afterPropertiesSet();
		MockServlet servlet = (MockServlet) adapter.getFacesServlet();
		ServletConfig config = servlet.getServletConfig();
		assertTrue(EnumerationUtils.toList(config.getInitParameterNames()).contains("testkey"));
		assertEquals("testvalue", config.getInitParameter("testkey"));
		assertEquals("testAdapterBean", config.getServletName());
		assertSame(servletContext, config.getServletContext());
	}

	public void testDoHandle() throws Exception {
		adapter.afterPropertiesSet();
		HttpServletRequest request = (HttpServletRequest) EasyMock.createMock(HttpServletRequest.class);
		HttpServletResponse response = (HttpServletResponse) EasyMock.createMock(HttpServletResponse.class);
		FacesHandler handler = (FacesHandler) EasyMock.createMock(FacesHandler.class);
		MvcFacesContext mvcFacesContext = (MvcFacesContext) EasyMock.createMock(MvcFacesContext.class);
		MvcFacesRequestContext mvcFacesRequestContext = new MvcFacesRequestControlContextImpl(mvcFacesContext, handler);
		adapter.doHandle(mvcFacesRequestContext, request, response);
		((TrackingMockServlet) adapter.getFacesServlet()).assertSame(request, response);
	}

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
		assertEquals(BeanScopeModelBinder.class,
				((RequestMappedModelBindingExecutor) adapter.getModelBindingExecutor()).getModelBinder().getClass());
		// Quick test that the beanFactory has been injected
		((BeanScopeModelBinder) ((RequestMappedModelBindingExecutor) adapter.getModelBindingExecutor())
				.getModelBinder()).afterPropertiesSet();
	}

	public void testFacesServletInit() throws Exception {
		Properties initParameters = new Properties();
		initParameters.put("javax.faces.STATE_SAVING_METHOD", "client");
		adapter.setInitParameters(initParameters);
		adapter.afterPropertiesSet();
		TrackingMockServlet servlet = (TrackingMockServlet) adapter.getFacesServlet();
		assertEquals("client", servlet.getConfig().getInitParameter("javax.faces.STATE_SAVING_METHOD"));
	}

	public void testCustomActionUrlMapper() throws Exception {
		ActionUrlMapper actionUrlMapper = (ActionUrlMapper) EasyMock.createMock(ActionUrlMapper.class);
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
		ModelBinder modelBinder = (ModelBinder) EasyMock.createMock(ModelBinder.class);
		adapter.setModelBinder(modelBinder);
		adapter.afterPropertiesSet();
		assertSame(modelBinder, ((RequestMappedModelBindingExecutor) adapter.getModelBindingExecutor())
				.getModelBinder());
	}

	public void testCustomModelBinderNull() throws Exception {
		try {
			adapter.setModelBinder(null);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals("The modelBinder is required", e.getMessage());
		}
	}

	public void testCustomRedirectHandler() throws Exception {
		RedirectHandler redirectHandler = (RedirectHandler) EasyMock.createMock(RedirectHandler.class);
		adapter.setRedirectHandler(redirectHandler);
		adapter.afterPropertiesSet();
		assertSame(redirectHandler, adapter.getRedirectHandler());
	}

	public void testCustomRedirectHandlerNull() throws Exception {
		try {
			adapter.setRedirectHandler(null);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals("The redirectHandler is required", e.getMessage());
		}
	}

	public void testCustomFacesViewIdResolver() throws Exception {
		FacesViewIdResolver facesViewIdResolver = (FacesViewIdResolver) EasyMock.createMock(FacesViewIdResolver.class);
		adapter.setFacesViewIdResolver(facesViewIdResolver);
		adapter.afterPropertiesSet();
		assertSame(facesViewIdResolver, adapter.getFacesViewIdResolver());
	}

	public void testCustomFacesViewIdResolverNull() throws Exception {
		try {
			adapter.setFacesViewIdResolver(null);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals("The facesViewIdResolver is required", e.getMessage());
		}

	}

	private void doTestOverrideInitParams(boolean override) throws Exception {
		adapter.setOverrideInitParameters(override);
		ServletContext servletContext = new MockServletContext();
		adapter.setServletContext(servletContext);
		ServletContext facesServletContext = adapter.getFacesServletContext();
		assertEquals((override ? "false" : null), facesServletContext
				.getInitParameter("org.apache.myfaces.ERROR_HANDLING"));
	}

	public void testOverrideInitParamsFalse() throws Exception {
		doTestOverrideInitParams(false);
	}

	public void testOverrideInitParamsTrue() throws Exception {
		doTestOverrideInitParams(true);
	}

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
}
