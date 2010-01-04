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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.crypto.Cipher;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import junit.framework.TestCase;

import org.apache.shale.test.mock.MockServletOutputStream;
import org.easymock.EasyMock;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.mock.web.DelegatingServletInputStream;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.util.PathMatcher;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.WebArgumentResolver;
import org.springframework.web.bind.support.WebBindingInitializer;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;
import org.springframework.web.servlet.mvc.multiaction.MethodNameResolver;
import org.springframework.web.util.UrlPathHelper;

public class FacesControllerAnnotatedMethodInvokerTests extends TestCase {

	private MockFacesControllerAnnotatedMethodInvoker invoker;
	private WebBindingInitializer bindingInitializer;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private NativeWebRequest webRequest;
	private Object controller;
	private WebArgumentResolver[] customArgumentResolvers;
	private ParameterNameDiscoverer parameterNameDiscoverer;

	protected void setUp() throws Exception {
		super.setUp();
		this.bindingInitializer = null;
		this.request = EasyMock.createNiceMock(HttpServletRequest.class);
		this.response = EasyMock.createMock(HttpServletResponse.class);
		this.webRequest = new ServletWebRequest(request, response);
		this.controller = new SampleController();
		this.customArgumentResolvers = null;
		this.parameterNameDiscoverer = null;
	}

	private void setupInvoker() throws Exception {
		Class<?> handlerType = controller.getClass();
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
		invoker.initBinder(controller, null, binder, webRequest);
		EasyMock.verify(bindingInitializer);
	}

	public void testSimpleInitBinder() throws Exception {
		setupInvoker();
		WebDataBinder binder = new WebDataBinder(null);
		invoker.initBinder(controller, null, binder, webRequest);
		((AbstractController) controller).assertCalled(SampleControllerMethod.INIT);
		((AbstractController) controller).assertNotCalled(SampleControllerMethod.INIT_FOR_ATTRIBUTE);
	}

	public void testInitBinderWithSpecificAttributeName() throws Exception {
		setupInvoker();
		WebDataBinder binder = new WebDataBinder(null);
		invoker.initBinder(controller, "attribute", binder, webRequest);
		((AbstractController) controller).assertCalled(SampleControllerMethod.INIT,
				SampleControllerMethod.INIT_FOR_ATTRIBUTE);
	}

	public void testInitBinderWithIllegalReturn() throws Exception {
		try {
			this.controller = new IllegalInitBinderReturnController();
			setupInvoker();
			WebDataBinder binder = new WebDataBinder(null);
			invoker.initBinder(controller, "attribute", binder, webRequest);
			fail();
		} catch (IllegalStateException e) {
			assertEquals("InitBinder methods must not have a return value: "
					+ "public java.lang.String org.springframework.faces.mvc.annotation."
					+ "FacesControllerAnnotatedMethodInvokerTests$IllegalInitBinderReturnController.initBinder("
					+ "org.springframework.web.bind.WebDataBinder)", e.getMessage());
		}
	}

	public void testInitBinderWithRequestParam() throws Exception {
		EasyMock.expect(request.getParameterValues("requestParam")).andReturn(new String[] { "requestParamValue" });
		EasyMock.replay(request, response);
		controller = new ParamsController();
		setupInvoker();
		WebDataBinder binder = new WebDataBinder(null);
		invoker.initBinder(controller, null, binder, webRequest);
		((AbstractController) controller).assertCalled(SampleControllerMethod.INIT);
	}

	public void testInitBinderWithMissingRequestParam() throws Exception {
		EasyMock.replay(request, response);
		controller = new ParamsController();
		setupInvoker();
		WebDataBinder binder = new WebDataBinder(null);
		try {
			invoker.initBinder(controller, null, binder, webRequest);
			fail();
		} catch (MissingServletRequestParameterException e) {
			assertEquals("Required java.lang.String parameter 'requestParam' is not present", e.getMessage());
		}
	}

	public void testInitBinderWithMissingNativeRequestParam() throws Exception {
		EasyMock.replay(request, response);
		controller = new NativeParamsController();
		setupInvoker();
		WebDataBinder binder = new WebDataBinder(null);
		try {
			invoker.initBinder(controller, null, binder, webRequest);
			fail();
		} catch (IllegalStateException e) {
			assertEquals("Optional int parameter 'requestParam' is not present but "
					+ "cannot be translated into a null value due to being declared as a primitive "
					+ "type. Consider declaring it as object wrapper for the corresponding primitive type.", e
					.getMessage());
		}
	}

	public void testIllegalInitBinderModelAttribute() throws Exception {
		controller = new IllegalInitBinderModelAttributeController();
		setupInvoker();
		WebDataBinder binder = new WebDataBinder(null);
		try {
			invoker.initBinder(controller, null, binder, webRequest);
			fail();
		} catch (IllegalStateException e) {
			assertEquals(
					"@ModelAttribute is not supported on @InitBinder methods: "
							+ "public void org.springframework.faces.mvc.annotation.FacesControllerAnnotatedMethodInvokerTests$"
							+ "IllegalInitBinderModelAttributeController.initBinder("
							+ "org.springframework.web.bind.WebDataBinder,java.lang.String)", e.getMessage());
		}
	}

	public void testCusomArgumentResolver() throws Exception {
		customArgumentResolvers = new WebArgumentResolver[] { new WebArgumentResolver() {
			public Object resolveArgument(MethodParameter methodParameter, NativeWebRequest webRequest)
					throws Exception {
				if (Integer.class.equals(methodParameter.getParameterType())) {
					return new Integer(100);
				}
				return UNRESOLVED;
			}
		} };
		controller = new CustomAndSimpleArgumentResolverController();
		setupInvoker();
		WebDataBinder binder = new WebDataBinder(null);
		invoker.initBinder(controller, null, binder, webRequest);
		((AbstractController) controller).assertCalled(SampleControllerMethod.INIT);
	}

	public void testStandardArguments() throws Exception {
		EasyMock.expect(request.getSession()).andReturn(new MockHttpSession());
		EasyMock.expect(request.getUserPrincipal()).andReturn(EasyMock.createMock(Principal.class));
		EasyMock.expect(request.getLocale()).andReturn(Locale.UK);
		EasyMock.expect(request.getInputStream()).andReturn(
				new DelegatingServletInputStream(new ByteArrayInputStream(new byte[] {})));
		EasyMock.expect(request.getReader()).andReturn(new BufferedReader(new StringReader("")));
		EasyMock.expect(response.getOutputStream()).andReturn(new MockServletOutputStream(new ByteArrayOutputStream()));
		EasyMock.expect(response.getWriter()).andReturn(new PrintWriter(new ByteArrayOutputStream()));
		EasyMock.replay(request, response);
		controller = new StandardArgumentController();
		setupInvoker();
		WebDataBinder binder = new WebDataBinder(null);
		invoker.initBinder(controller, null, binder, webRequest);
		((AbstractController) controller).assertCalled(SampleControllerMethod.INIT);
	}

	public void testTooSpecificStandardArgument() throws Exception {
		EasyMock.expect(request.getReader()).andReturn(new BufferedReader(new StringReader("")));
		EasyMock.replay(request, response);
		controller = new TooSpecificStandardArgumentController();
		setupInvoker();
		WebDataBinder binder = new WebDataBinder(null);
		try {
			invoker.initBinder(controller, null, binder, webRequest);
			fail();
		} catch (IllegalStateException e) {
			assertEquals("Standard argument type [java.io.PipedReader] resolved to incompatible value of type "
					+ "[class java.io.BufferedReader]. Consider declaring the argument type in a "
					+ "less specific fashion.", e.getMessage());
		}
	}

	public void testSimpleArgumentResolver() throws Exception {
		parameterNameDiscoverer = EasyMock.createNiceMock(ParameterNameDiscoverer.class);
		EasyMock.expect(parameterNameDiscoverer.getParameterNames((Method) EasyMock.anyObject())).andReturn(
				new String[] { "", "intValue" });
		EasyMock.expect(request.getParameterValues("intValue")).andReturn(new String[] { "100" });
		EasyMock.replay(parameterNameDiscoverer, request);
		controller = new CustomAndSimpleArgumentResolverController();
		setupInvoker();
		WebDataBinder binder = new WebDataBinder(null);
		invoker.initBinder(controller, null, binder, webRequest);
		((AbstractController) controller).assertCalled(SampleControllerMethod.INIT);
	}

	public void testNoParameterInfo() throws Exception {
		parameterNameDiscoverer = EasyMock.createNiceMock(ParameterNameDiscoverer.class);
		EasyMock.replay(parameterNameDiscoverer);
		controller = new CustomAndSimpleArgumentResolverController();
		setupInvoker();
		WebDataBinder binder = new WebDataBinder(null);
		try {
			invoker.initBinder(controller, null, binder, webRequest);
			fail();
		} catch (IllegalStateException e) {
			assertEquals("No parameter specified for @RequestParam argument of type [java.lang.Integer], "
					+ "and no parameter name information found in class file either.", e.getMessage());
		}
	}

	public void testUnsupportedArgument() throws Exception {
		controller = new UnsupportedArgumentController();
		setupInvoker();
		WebDataBinder binder = new WebDataBinder(null);
		try {
			invoker.initBinder(controller, null, binder, webRequest);
			fail();
		} catch (IllegalStateException e) {
			assertEquals(
					"Unsupported argument [javax.crypto.Cipher] for "
							+ "@InitBinder method: public void org.springframework.faces.mvc.annotation."
							+ "FacesControllerAnnotatedMethodInvokerTests$UnsupportedArgumentController.initBinder(javax.crypto.Cipher)",
					e.getMessage());
		}
	}

	public void testThrowingController() throws Exception {
		controller = new ThrowingController();
		setupInvoker();
		WebDataBinder binder = new WebDataBinder(null);
		try {
			invoker.initBinder(controller, null, binder, webRequest);
			fail();
		} catch (IOException e) {
			assertEquals("test", e.getMessage());
		}
	}

	public void testMultiPartFile() throws Exception {
		webRequest = EasyMock.createMock(NativeWebRequest.class);
		MultipartRequest multipartRequest = EasyMock.createMock(MultipartRequest.class);
		EasyMock.expect(webRequest.getNativeRequest()).andStubReturn(multipartRequest);
		MultipartFile file = EasyMock.createMock(MultipartFile.class);
		EasyMock.expect(multipartRequest.getFile("file")).andReturn(file);
		EasyMock.replay(webRequest, multipartRequest);
		controller = new MultiPartFileController();
		setupInvoker();
		WebDataBinder binder = new WebDataBinder(null);
		invoker.initBinder(controller, null, binder, webRequest);
		((AbstractController) controller).assertCalled(SampleControllerMethod.INIT);
	}

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

	public static class ParamsController extends AbstractController {
		@InitBinder
		public void initBinder(WebDataBinder binder, @RequestParam(value = "requestParam") String requestParam) {
			assertEquals(requestParam, "requestParamValue");
			called(SampleControllerMethod.INIT);
		}
	}

	public static class NativeParamsController extends AbstractController {
		@InitBinder
		public void initBinder(WebDataBinder binder,
				@RequestParam(value = "requestParam", required = false) int requestParam) {
		}
	}

	public static class IllegalInitBinderReturnController {
		@InitBinder
		public String initBinder(WebDataBinder binder) {
			return "";
		}
	}

	public static class IllegalInitBinderModelAttributeController {
		@InitBinder
		public void initBinder(WebDataBinder binder, @ModelAttribute("modelAttribute") String modelAttribute) {
		}
	}

	public static class CustomAndSimpleArgumentResolverController extends AbstractController {
		@InitBinder
		public void initBinder(WebDataBinder binder, Integer intValue) {
			called(SampleControllerMethod.INIT);
			assertEquals(new Integer(100), intValue);
		}
	}

	public static class StandardArgumentController extends AbstractController {
		@InitBinder
		public void initBinder(WebDataBinder binder,

		ServletRequest servletRequest, ServletResponse servletResponse, HttpSession httpSession, Principal principal,
				Locale locale, InputStream inputStream, Reader reader, OutputStream outputStream, Writer writer,
				WebRequest webRequest) {
			called(SampleControllerMethod.INIT);
			assertNotNull(servletRequest);
			assertNotNull(servletResponse);
			assertNotNull(httpSession);
			assertNotNull(principal);
			assertNotNull(locale);
			assertNotNull(inputStream);
			assertNotNull(reader);
			assertNotNull(outputStream);
			assertNotNull(writer);
			assertNotNull(webRequest);
		}
	}

	public static class TooSpecificStandardArgumentController extends AbstractController {
		@InitBinder
		public void initBinder(WebDataBinder binder, PipedReader reader) {
		}
	}

	public static class UnsupportedArgumentController extends AbstractController {
		@InitBinder
		public void initBinder(Cipher cipher) {
		}
	}

	public static class ThrowingController extends AbstractController {
		@InitBinder
		public void initBinder(WebDataBinder binder) throws IOException {
			throw new IOException("test");
		}
	}

	public static class MultiPartFileController extends AbstractController {
		@InitBinder
		public void initBinder(WebDataBinder binder, @RequestParam("file") MultipartFile file) throws IOException {
			called(SampleControllerMethod.INIT);
			assertNotNull(file);
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
