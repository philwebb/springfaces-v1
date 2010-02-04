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
package org.springframework.faces.mvc.annotation.support;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.apache.shale.test.mock.MockFacesContext;
import org.easymock.EasyMock;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.faces.mvc.annotation.sample.SampleController;
import org.springframework.faces.mvc.annotation.support.FoundNavigationCase.FoundNavigationCaseType;
import org.springframework.faces.mvc.navigation.NavigationLocation;
import org.springframework.faces.mvc.navigation.NavigationRequestEvent;
import org.springframework.faces.mvc.navigation.annotation.NavigationCase;
import org.springframework.faces.mvc.servlet.annotation.support.RequestMappingMethodResolver;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebArgumentResolver;
import org.springframework.web.bind.support.WebBindingInitializer;
import org.springframework.web.context.request.NativeWebRequest;

public class FoundNavigationCaseTests extends TestCase {

	private AnnotatedMethodInvokerFactory invokerFactory;
	private NativeWebRequest nativeWebRequest;

	protected void setUp() throws Exception {
		this.invokerFactory = new AnnotatedMethodInvokerFactory() {
			public AnnotatedMethodInvoker newInvoker(WebArgumentResolver... additionalArgumentResolvers) {
				return new MockFacesControllerAnnotatedMethodInvoker(null, null, null, additionalArgumentResolvers);
			}
		};
		nativeWebRequest = EasyMock.createNiceMock(NativeWebRequest.class);

	}

	private FoundNavigationCase doTest(Object owner, FoundNavigationCaseType type) throws Exception {
		NavigationCase navigationCase = EasyMock.createMock(NavigationCase.class);
		FoundNavigationCase fnc = new FoundNavigationCase(navigationCase, owner);
		assertEquals(type, fnc.getType());
		assertSame(owner, fnc.getOwner());
		assertSame(navigationCase, fnc.getNavigationCase());
		return fnc;
	}

	public void testPackage() throws Exception {
		doTest(SampleController.class.getPackage(), FoundNavigationCaseType.PACKAGE);
	}

	public void testClass() throws Exception {
		doTest(SampleController.class, FoundNavigationCaseType.CLASS);
	}

	public void testMethod() throws Exception {
		doTest(SampleController.class.getMethod("withRules", new Class<?>[] {}), FoundNavigationCaseType.METHOD);
	}

	public void testToString() throws Exception {
		FoundNavigationCase found = doTest(SampleController.class, FoundNavigationCaseType.CLASS);
		System.out.println(found.toString());
		Pattern pattern = Pattern.compile("\\Q[FoundNavigationCase@\\E\\S+\\Q navigationCase = EasyMock "
				+ "for interface org.springframework.faces.mvc.navigation.annotation.NavigationCase, "
				+ "type = CLASS, owner = SampleController]\\E");
		assertTrue(pattern.matcher(found.toString()).matches());
	}

	public void testIllegal() throws Exception {
		try {
			doTest("string", null);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals("owner must be a Method, Class or Package", e.getMessage());
		}
	}

	public void testOutcomeMethodCall() throws Exception {
		Method[] methods = SampleController.class.getMethods();
		Method method = null;
		for (Method searchMethod : methods) {
			if ("methodCall".equals(searchMethod.getName())) {
				method = searchMethod;
			}
		}
		NavigationCase navigationCase = method.getAnnotation(NavigationCase.class);
		FoundNavigationCase fnc = new FoundNavigationCase(navigationCase, method);
		SampleController target = new SampleController();
		assertFalse(target.isMethodCalled());
		Object outcome = fnc.getOutcome(new NavigationRequestEvent(new MockFacesContext(), "#{action.test}",
				"methodcall"), target, nativeWebRequest, invokerFactory);
		assertTrue(target.isMethodCalled());
		assertEquals(new NavigationLocation("someview"), outcome);
	}

	private void doTestOutcomeWithToAndReturn(String methodName, String returnOutcome,
			NavigationLocation expectedOutcome) throws Exception {
		Method method = SampleController.class.getMethod(methodName, new Class<?>[] {});
		NavigationCase navigationCase = method.getAnnotation(NavigationCase.class);
		FoundNavigationCase fnc = new FoundNavigationCase(navigationCase, method);
		SampleController target = new SampleController();
		target.setOutcome(returnOutcome);
		Object outcome = fnc.getOutcome(new NavigationRequestEvent(new MockFacesContext(), "#{action.test}",
				"methodcallwithto"), target, nativeWebRequest, invokerFactory);
		assertEquals(expectedOutcome, outcome);
		assertTrue(target.isMethodCalled());
	}

	public void testMethodCallWithTo() throws Exception {
		doTestOutcomeWithToAndReturn("methodCallWithTo", null, new NavigationLocation("test"));
	}

	public void testMethodCallWithToAndNullReturn() throws Exception {
		doTestOutcomeWithToAndReturn("methodCallWithToAndReturn", null, new NavigationLocation("test"));
	}

	public void testMethodCallWithToAndOverrideReturn() throws Exception {
		doTestOutcomeWithToAndReturn("methodCallWithToAndReturn", "override", new NavigationLocation("override"));
	}

	public void testOutcomeMethodCallWithRequestMappingAndNoTo() throws Exception {
		Method method = SampleController.class.getMethod("methodCallWithRequestMappingAndNoTo", new Class<?>[] {});
		NavigationCase navigationCase = method.getAnnotation(NavigationCase.class);
		FoundNavigationCase fnc = new FoundNavigationCase(navigationCase, method);
		SampleController target = new SampleController();
		try {
			fnc.getOutcome(new NavigationRequestEvent(new MockFacesContext(), null,
					"methodcallwithrequestmappingandnoto"), target, nativeWebRequest, invokerFactory);
			fail();
		} catch (IllegalStateException e) {
			assertFalse(target.isMethodCalled());
			assertEquals(
					"Unable to call method methodCallWithRequestMappingAndNoTo from class class org.springframework.faces.mvc.annotation.sample.SampleController "
							+ "in order to resolve empty @NavigationCase.to() for JSF Navigation Request Event (fromAction=\"null\", "
							+ "outcome=\"methodcallwithrequestmappingandnoto\", exception=\"null\") as "
							+ "method also includes @RequestMapping annotation", e.getMessage());
		}
	}

	public void testOutcomeMethodCallWithRequestMapping() throws Exception {
		Method method = SampleController.class.getMethod("methodCallWithRequestMapping", new Class<?>[] {});
		NavigationCase navigationCase = method.getAnnotation(NavigationCase.class);
		FoundNavigationCase fnc = new FoundNavigationCase(navigationCase, method);
		SampleController target = new SampleController();
		Object outcome = fnc.getOutcome(new NavigationRequestEvent(new MockFacesContext(), null,
				"methodcallwithrequestmapping"), target, nativeWebRequest, invokerFactory);
		assertEquals(new NavigationLocation("test"), outcome);
		assertFalse(target.isMethodCalled());
	}

	public void testPopupAndFragments() throws Exception {
		Method method = SampleController.class.getMethod("popupAndFragment", new Class<?>[] {});
		NavigationCase navigationCase = method.getAnnotation(NavigationCase.class);
		FoundNavigationCase fnc = new FoundNavigationCase(navigationCase, method);
		SampleController target = new SampleController();
		Object outcome = fnc.getOutcome(new NavigationRequestEvent(new MockFacesContext(), null, "action"), target,
				nativeWebRequest, invokerFactory);
		assertEquals(new NavigationLocation("popupandfragment", true, new String[] { "fragments" }), outcome);
		assertFalse(target.isMethodCalled());
	}

	private class MockFacesControllerAnnotatedMethodInvoker extends AnnotatedMethodInvoker {

		public MockFacesControllerAnnotatedMethodInvoker(RequestMappingMethodResolver resolver,
				WebBindingInitializer bindingInitializer, ParameterNameDiscoverer parameterNameDiscoverer,
				WebArgumentResolver[] customArgumentResolvers) {
			super(resolver, bindingInitializer, parameterNameDiscoverer, FacesWebArgumentResolvers
					.mergeWithFacesResolvers(customArgumentResolvers));
		}

		protected WebDataBinder createBinder(NativeWebRequest webRequest, Object target, String objectName)
				throws Exception {
			throw new UnsupportedOperationException();
		}

		protected void raiseMissingParameterException(String paramName, Class paramType) throws Exception {
			throw new UnsupportedOperationException();
		}
	}
}
