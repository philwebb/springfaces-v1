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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.faces.mvc.stereotype.FacesController;
import org.springframework.util.PathMatcher;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.WebArgumentResolver;
import org.springframework.web.bind.support.WebBindingInitializer;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.multiaction.MethodNameResolver;
import org.springframework.web.util.UrlPathHelper;

public class FacesControllerAnnotatedMethodInvokerTests extends TestCase {

	private MockFacesControllerAnnotatedMethodInvoker invoker;
	private WebBindingInitializer bindingInitializer;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private ServletWebRequest webRequest;
	private Object handler;

	protected void setUp() throws Exception {
		super.setUp();
		this.bindingInitializer = null;
		this.request = EasyMock.createNiceMock(HttpServletRequest.class);
		this.response = EasyMock.createMock(HttpServletResponse.class);
		EasyMock.expect(request.getParameterValues("requestParam")).andReturn(new String[] { "requestParamValue" });
		EasyMock.replay(request, response);
		this.webRequest = new ServletWebRequest(request, response);
		this.handler = new SampleController();
	}

	private void setupInvoker() throws Exception {
		WebArgumentResolver[] customArgumentResolvers = null;
		ParameterNameDiscoverer parameterNameDiscoverer = null;
		Class<?> handlerType = handler.getClass();
		UrlPathHelper urlPathHelper = null;
		MethodNameResolver methodNameResolver = null;
		PathMatcher pathMatcher = null;
		RequestMappingMethodResolver resolver = new RequestMappingMethodResolver(handlerType, urlPathHelper,
				methodNameResolver, pathMatcher);
		this.invoker = new MockFacesControllerAnnotatedMethodInvoker(resolver, bindingInitializer,
				parameterNameDiscoverer, customArgumentResolvers);
	}

	public void testInitBinderWithCustomBinder() throws Exception {
		this.bindingInitializer = EasyMock.createMock(WebBindingInitializer.class);
		setupInvoker();
		WebDataBinder binder = new WebDataBinder(null);
		bindingInitializer.initBinder((WebDataBinder) EasyMock.anyObject(), (WebRequest) EasyMock.anyObject());
		EasyMock.expectLastCall();
		EasyMock.replay(bindingInitializer);
		invoker.initBinder(handler, null, binder, webRequest);
		EasyMock.verify(bindingInitializer);
	}

	public void testSimpleInitBinder() throws Exception {
		setupInvoker();
		WebDataBinder binder = new WebDataBinder(null);
		invoker.initBinder(handler, null, binder, webRequest);
		((AbstractController) handler).assertCalled(SampleControllerMethod.INIT);
		((AbstractController) handler).assertNotCalled(SampleControllerMethod.INIT_FOR_ATTRIBUTE);
	}

	public void testInitBinderWithSpecificAttributeName() throws Exception {
		setupInvoker();
		WebDataBinder binder = new WebDataBinder(null);
		invoker.initBinder(handler, "attribute", binder, webRequest);
		((AbstractController) handler).assertCalled(SampleControllerMethod.INIT,
				SampleControllerMethod.INIT_FOR_ATTRIBUTE);
	}

	public void testInitBinderWithIllegalReturn() throws Exception {
		try {
			this.handler = new IllegalInitBinderReturnController();
			setupInvoker();
			WebDataBinder binder = new WebDataBinder(null);
			invoker.initBinder(handler, "attribute", binder, webRequest);
			fail();
		} catch (IllegalStateException e) {
			assertEquals("InitBinder methods must not have a return value: "
					+ "public java.lang.String org.springframework.faces.mvc.annotation."
					+ "FacesControllerAnnotatedMethodInvokerTests$IllegalInitBinderReturnController.initBinder("
					+ "org.springframework.web.bind.WebDataBinder)", e.getMessage());
		}
	}

	public void testInitBinderWithRequestParam() throws Exception {
		handler = new ParamsController();
		setupInvoker();
		WebDataBinder binder = new WebDataBinder(null);
		invoker.initBinder(handler, null, binder, webRequest);
		((AbstractController) handler).assertCalled(SampleControllerMethod.INIT);
	}

	public void testIllegalInitBinderModelAttribute() throws Exception {
		handler = new IllegalInitBinderModelAttributeController();
		setupInvoker();
		WebDataBinder binder = new WebDataBinder(null);
		try {
			invoker.initBinder(handler, null, binder, webRequest);
			fail();
		} catch (IllegalStateException e) {
			assertEquals(
					"@ModelAttribute is not supported on @InitBinder methods: "
							+ "public void org.springframework.faces.mvc.annotation.FacesControllerAnnotatedMethodInvokerTests$"
							+ "IllegalInitBinderModelAttributeController.initBinder("
							+ "org.springframework.web.bind.WebDataBinder,java.lang.String)", e.getMessage());
		}
	}

	// FIXME more tests

	private enum SampleControllerMethod {
		INIT, INIT_FOR_ATTRIBUTE
	}

	public static abstract class AbstractController {
		private Set<SampleControllerMethod> called = new HashSet<SampleControllerMethod>();

		protected void called(SampleControllerMethod method) {
			this.called.add(method);
		}

		public void assertCalled(SampleControllerMethod... methods) {
			assertTrue(called.containsAll(Arrays.asList(methods)));
		}

		public void assertNotCalled(SampleControllerMethod... methods) {
			assertFalse(new HashSet<SampleControllerMethod>(called).removeAll(Arrays.asList(methods)));
		}
	}

	@FacesController
	public static class SampleController extends AbstractController {

		@InitBinder
		public void initForAll(WebDataBinder binder) {
			called(SampleControllerMethod.INIT);
		}

		@InitBinder("attribute")
		public void initForAttribute(WebDataBinder binder) {
			called(SampleControllerMethod.INIT_FOR_ATTRIBUTE);
		}
	}

	@FacesController
	public static class ParamsController extends AbstractController {

		@InitBinder
		public void initBinder(WebDataBinder binder, @RequestParam("requestParam") String requestParam) {
			assertEquals(requestParam, "requestParamValue");
			called(SampleControllerMethod.INIT);
		}
	}

	@FacesController
	public static class IllegalInitBinderReturnController {
		@InitBinder
		public String initBinder(WebDataBinder binder) {
			return "";
		}
	}

	@FacesController
	public static class IllegalInitBinderModelAttributeController {
		@InitBinder
		public void initBinder(WebDataBinder binder, @ModelAttribute("modelAttribute") String modelAttribute) {
		}
	}

	@FacesController
	public static class CustomArgumentResolverController extends AbstractController {
		@InitBinder
		public void initBinder(WebDataBinder binder) {
		}
	}

	private static class MockFacesControllerAnnotatedMethodInvoker extends FacesControllerAnnotatedMethodInvoker {
		public MockFacesControllerAnnotatedMethodInvoker(RequestMappingMethodResolver resolver,
				WebBindingInitializer bindingInitializer, ParameterNameDiscoverer parameterNameDiscoverer,
				WebArgumentResolver[] customArgumentResolvers) {
			super(resolver, bindingInitializer, parameterNameDiscoverer, customArgumentResolvers);
		}

		protected WebDataBinder createBinder(NativeWebRequest webRequest, Object target, String objectName)
				throws Exception {
			return new WebDataBinder(target, objectName);
		}
	}
}
