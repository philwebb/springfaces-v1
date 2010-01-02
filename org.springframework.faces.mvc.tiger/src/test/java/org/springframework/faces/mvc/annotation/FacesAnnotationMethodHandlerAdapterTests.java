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
package org.springframework.faces.mvc.annotation;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.faces.FactoryFinder;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.lifecycle.LifecycleFactory;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.shale.test.mock.MockExternalContext;
import org.apache.shale.test.mock.MockFacesContext;
import org.apache.shale.test.mock.MockFacesContextFactory;
import org.apache.shale.test.mock.MockLifecycleFactory;
import org.easymock.EasyMock;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.AopProxy;
import org.springframework.aop.framework.DefaultAopProxyFactory;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.faces.mvc.FacesHandler;
import org.springframework.faces.mvc.FacesHandlerAdapter;
import org.springframework.faces.mvc.NavigationRequestEvent;
import org.springframework.faces.mvc.bind.annotation.NavigationCase;
import org.springframework.faces.mvc.stereotype.FacesController;
import org.springframework.stereotype.Controller;
import org.springframework.util.PathMatcher;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.support.WebArgumentResolver;
import org.springframework.web.bind.support.WebBindingInitializer;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.support.StaticWebApplicationContext;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter;
import org.springframework.web.servlet.mvc.multiaction.MethodNameResolver;
import org.springframework.web.util.UrlPathHelper;

public class FacesAnnotationMethodHandlerAdapterTests extends TestCase {

	private MockFacesHandlerAdapter underlyingAdapter;
	private FacesAnnotationMethodHandlerAdapter adapter;
	private StaticWebApplicationContext context;

	protected void setUp() throws Exception {
		super.setUp();
		this.underlyingAdapter = new MockFacesHandlerAdapter();
		this.context = new StaticWebApplicationContext();
		ServletContext servletContext = EasyMock.createNiceMock(ServletContext.class);
		EasyMock.expect(servletContext.getInitParameter((String) EasyMock.anyObject())).andReturn(null);
		EasyMock.replay(servletContext);
		FactoryFinder.setFactory(FacesContextFactory.class.getName(), MockFacesContextFactory.class.getName());
		FactoryFinder.setFactory(LifecycleFactory.class.getName(), MockLifecycleFactory.class.getName());
		context.setServletContext(servletContext);
		adapter = new FacesAnnotationMethodHandlerAdapter();
		adapter.setFacesHandlerAdapter(underlyingAdapter);
		adapter.setApplicationContext(context);
		adapter.setBeanName("testMethodAdapterBean");
		adapter.afterPropertiesSet();
	}

	private Set<Class<? extends WebArgumentResolver>> assertHasFacesResolvers(
			FacesAnnotationMethodHandlerAdapter adapter) throws Exception {
		Field field = AnnotationMethodHandlerAdapter.class.getDeclaredField("customArgumentResolvers");
		field.setAccessible(true);
		WebArgumentResolver[] resolvers = (WebArgumentResolver[]) field.get(adapter);
		Set<Class<? extends WebArgumentResolver>> requred = new HashSet<Class<? extends WebArgumentResolver>>();
		Set<Class<? extends WebArgumentResolver>> remain = new HashSet<Class<? extends WebArgumentResolver>>();
		requred.add(FacesWebArgumentResolver.class);
		if (resolvers != null) {
			for (WebArgumentResolver resolver : resolvers) {
				if (!requred.remove(resolver.getClass())) {
					remain.add(resolver.getClass());
				}
			}
		}
		if (requred.size() > 0) {
			fail("Resolvers do not contain the following required classes " + requred);
		}
		return remain;
	}

	public void testSupports() throws Exception {
		assertTrue(adapter.supports(new SampleFacesController()));
		assertFalse(adapter.supports(new UnSupportedMissingFacesController()));
		assertFalse(adapter.supports(new UnSupportedNoRequestMapping()));
		Object proxied = createCglibProxy(SampleFacesController.class);
		assertTrue(adapter.supports(proxied));
	}

	public void testHandle() throws Exception {
		HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
		HttpServletResponse response = EasyMock.createMock(HttpServletResponse.class);
		SampleFacesController handler = new SampleFacesController();
		adapter.handle(request, response, handler);
		assertSame(request, underlyingAdapter.getRequest());
		assertSame(response, underlyingAdapter.getResponse());
	}

	public void testHandleBadlyConfigured() throws Exception {
		HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
		HttpServletResponse response = EasyMock.createMock(HttpServletResponse.class);
		SampleFacesController handler = new SampleFacesController();
		underlyingAdapter.setSupports(false);
		try {
			adapter.handle(request, response, handler);
			fail();
		} catch (IllegalStateException e) {
			assertEquals("The facesHandlerAdapter class org.springframework.faces.mvc.annotation."
					+ "FacesAnnotationMethodHandlerAdapterTests$MockFacesHandlerAdapter "
					+ "does not support FacesHandler objects, possible misconfiguration of setFacesHandlerAdapter", e
					.getMessage());
		}
	}

	private void setupMockRequestUlr(HttpServletRequest request) {
		EasyMock.expect(request.getServletPath()).andReturn("/test");
		EasyMock.expect(request.getContextPath()).andReturn("/test");
		EasyMock.expect(request.getRequestURI()).andReturn("test");
	}

	public void testCreateView() throws Exception {
		HttpServletRequest request = EasyMock.createNiceMock(HttpServletRequest.class);
		HttpServletResponse response = EasyMock.createMock(HttpServletResponse.class);
		setupMockRequestUlr(request);
		SampleFacesController handler = new SampleFacesController();
		EasyMock.replay(request, response);
		adapter.handle(request, response, handler);
		FacesContext facesContext = new MockFacesContext(new MockExternalContext(null, request, response));
		ModelAndView view = underlyingAdapter.getHandler().createView(facesContext);
		assertEquals("testView", view.getViewName());
	}

	private void doTestGetNavigationOutcomeLocation(String eventOutcome, String expected) throws Exception {
		HttpServletRequest request = EasyMock.createNiceMock(HttpServletRequest.class);
		HttpServletResponse response = EasyMock.createMock(HttpServletResponse.class);
		setupMockRequestUlr(request);
		SampleFacesController handler = new SampleFacesController();
		EasyMock.replay(request, response);
		adapter.handle(request, response, handler);
		FacesContext facesContext = new MockFacesContext(new MockExternalContext(null, request, response));
		NavigationRequestEvent event = new NavigationRequestEvent(this, "#{action}", eventOutcome);
		Object outcome = underlyingAdapter.getHandler().getNavigationOutcomeLocation(facesContext, event);
		assertEquals(expected, outcome);
	}

	public void testGetNavigationOutcomeLocation() throws Exception {
		doTestGetNavigationOutcomeLocation("outcome", "testview");
	}

	public void testGetNavigationOutcomeLocationNotFound() throws Exception {
		doTestGetNavigationOutcomeLocation("missingoutcome", null);
	}

	public void testGetNavigationOutcomeLocationWithCustomResolver() throws Exception {
		adapter.setNavigationOutcomeExpressionResolver(new NavigationOutcomeExpressionResolver() {
			public Object resolveNavigationOutcome(NavigationOutcomeExpressionContext context, Object outcome)
					throws Exception {
				return "resolved" + outcome;
			}
		});
		doTestGetNavigationOutcomeLocation("outcome", "resolvedtestview");
	}

	private void doTestHandlerFromContext() throws Exception {
		context.refresh();
		adapter = new FacesAnnotationMethodHandlerAdapter();
		adapter.setApplicationContext(context);
		adapter.setBeanName("testMethodAdapterBean");
		adapter.afterPropertiesSet();
		adapter.postProcessBeanFactory(context.getBeanFactory());
	}

	public void testSingleHandlerFromContext() throws Exception {
		context.registerSingleton("adapter", FacesHandlerAdapter.class);
		doTestHandlerFromContext();
		Object bean = context.getBean("adapter");
		assertSame(bean, adapter.getFacesHandlerAdapter());
	}

	public void testMultipleHandlersFromContextThrows() throws Exception {
		context.registerSingleton("adapter1", FacesHandlerAdapter.class);
		context.registerSingleton("adapter2", FacesHandlerAdapter.class);
		try {
			doTestHandlerFromContext();
			fail();
		} catch (IllegalStateException e) {
			assertEquals("Multiple FacesHandlerAdapters found in application context, "
					+ "please manually inject a FacesHanlderAdapter using the setFacesHandlerAdapter method", e
					.getMessage());
		}
	}

	public void testNoHandlerInContext() throws Exception {
		assertNull(context.getBeanFactory().getRegisteredScope("page"));
		doTestHandlerFromContext();
		assertNotNull(adapter.getFacesHandlerAdapter());
		// Ensure that post process gets called on the FacesHandlerAdapter and page scope is registered
		assertNotNull(context.getBeanFactory().getRegisteredScope("page"));
	}

	public void testSetUrlPathHelper() throws Exception {
		UrlPathHelper urlPathHelper = new UrlPathHelper();
		adapter.setUrlPathHelper(urlPathHelper);
		assertSame(urlPathHelper, adapter.getUrlPathHelper());
		Field field = AnnotationMethodHandlerAdapter.class.getDeclaredField("urlPathHelper");
		field.setAccessible(true);
		assertSame(urlPathHelper, field.get(adapter));
	}

	public void testSetMethodNameResolver() throws Exception {
		MethodNameResolver methodNameResolver = EasyMock.createMock(MethodNameResolver.class);
		adapter.setMethodNameResolver(methodNameResolver);
		assertSame(methodNameResolver, adapter.getMethodNameResolver());
		Field field = AnnotationMethodHandlerAdapter.class.getDeclaredField("methodNameResolver");
		field.setAccessible(true);
		assertSame(methodNameResolver, field.get(adapter));
	}

	public void testSetPathMatcher() throws Exception {
		PathMatcher pathMatcher = EasyMock.createMock(PathMatcher.class);
		adapter.setPathMatcher(pathMatcher);
		assertSame(pathMatcher, adapter.getPathMatcher());
		Field field = AnnotationMethodHandlerAdapter.class.getDeclaredField("pathMatcher");
		field.setAccessible(true);
		assertSame(pathMatcher, field.get(adapter));
	}

	public void testSetWebBindingInitializer() throws Exception {
		WebBindingInitializer webBindingInitializer = EasyMock.createMock(WebBindingInitializer.class);
		adapter.setWebBindingInitializer(webBindingInitializer);
		assertSame(webBindingInitializer, adapter.getWebBindingInitializer());
		Field field = AnnotationMethodHandlerAdapter.class.getDeclaredField("webBindingInitializer");
		field.setAccessible(true);
		assertSame(webBindingInitializer, field.get(adapter));
	}

	public void testSetParameterNameDiscoverer() throws Exception {
		ParameterNameDiscoverer parameterNameDiscoverer = EasyMock.createMock(ParameterNameDiscoverer.class);
		adapter.setParameterNameDiscoverer(parameterNameDiscoverer);
		assertSame(parameterNameDiscoverer, adapter.getParameterNameDiscoverer());
		Field field = AnnotationMethodHandlerAdapter.class.getDeclaredField("parameterNameDiscoverer");
		field.setAccessible(true);
		assertSame(parameterNameDiscoverer, field.get(adapter));
	}

	public void testSetCustomArgumentResolverDefaults() throws Exception {
		assertHasFacesResolvers(adapter);
	}

	public void testSetCustomArgumentResolvers() throws Exception {
		adapter.setCustomArgumentResolvers(new WebArgumentResolver[] { new MockWebArgumentResolver() });
		Set<Class<? extends WebArgumentResolver>> remain = assertHasFacesResolvers(adapter);
		assertEquals(Collections.singleton(MockWebArgumentResolver.class), remain);
	}

	public void testSetCustomArgumentResolversNull() throws Exception {
		adapter.setCustomArgumentResolvers(null);
		assertHasFacesResolvers(adapter);
	}

	public void testSetCustomArgumentResolver() throws Exception {
		adapter.setCustomArgumentResolver(new MockWebArgumentResolver());
		Set<Class<? extends WebArgumentResolver>> remain = assertHasFacesResolvers(adapter);
		assertEquals(Collections.singleton(MockWebArgumentResolver.class), remain);
	}

	public void testSetCustomArgumentResolverNull() throws Exception {
		adapter.setCustomArgumentResolver(null);
		assertHasFacesResolvers(adapter);
	}

	public void testOrdered() throws Exception {
		assertEquals(Ordered.HIGHEST_PRECEDENCE, adapter.getOrder());
		adapter.setOrder(123);
		assertEquals(123, adapter.getOrder());
	}

	private Object createCglibProxy(Class<?> targetClass) {
		AdvisedSupport aopConfig = new AdvisedSupport();
		aopConfig.setTargetClass(targetClass);
		aopConfig.setProxyTargetClass(true);
		aopConfig.addAdvice(new MethodInterceptor() {
			public Object invoke(MethodInvocation invocation) throws Throwable {
				return invocation.proceed();
			}
		});

		DefaultAopProxyFactory aopProxyFactory = new DefaultAopProxyFactory();
		AopProxy proxy = aopProxyFactory.createAopProxy(aopConfig);
		return proxy.getProxy();
	}

	@FacesController
	public static class SampleFacesController {
		@RequestMapping("/test")
		@NavigationCase(on = "outcome", to = "testview")
		public ModelAndView handle() {
			ModelAndView modelAndView = new ModelAndView();
			modelAndView.setViewName("testView");
			return modelAndView;
		}
	}

	@Controller
	public static class UnSupportedMissingFacesController {
		@RequestMapping("/test")
		public String handle() {
			return "";
		}
	}

	@FacesController
	public static class UnSupportedNoRequestMapping {
	}

	public static class MockFacesHandlerAdapter implements HandlerAdapter {

		private HttpServletRequest request;
		private HttpServletResponse response;
		private FacesHandler handler;
		private boolean supports = true;

		public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler)
				throws Exception {
			this.request = request;
			this.response = response;
			this.handler = (FacesHandler) handler;
			return new ModelAndView();
		}

		public boolean supports(Object handler) {
			return supports && (handler instanceof FacesHandler);
		}

		public long getLastModified(HttpServletRequest request, Object handler) {
			return -1;
		}

		public void setSupports(boolean supports) {
			this.supports = supports;
		}

		public HttpServletRequest getRequest() {
			return request;
		}

		public HttpServletResponse getResponse() {
			return response;
		}

		public FacesHandler getHandler() {
			return handler;
		}
	}

	private static class MockWebArgumentResolver implements WebArgumentResolver {
		public Object resolveArgument(MethodParameter methodParameter, NativeWebRequest webRequest) throws Exception {
			return UNRESOLVED;
		}
	}
}
