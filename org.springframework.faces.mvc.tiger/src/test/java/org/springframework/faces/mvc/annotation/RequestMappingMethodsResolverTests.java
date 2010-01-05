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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import junit.framework.TestCase;

import org.springframework.faces.mvc.annotation.RequestMappingMethodResolver.RequestMappingAnnotation;
import org.springframework.faces.mvc.annotation.RequestMappingMethodResolver.RequestMappingAnnotationMatch;
import org.springframework.faces.mvc.annotation.RequestMappingMethodResolver.RequestMappingAnnotationMatchComparator;
import org.springframework.faces.mvc.stereotype.FacesController;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.multiaction.InternalPathMethodNameResolver;
import org.springframework.web.servlet.mvc.multiaction.MethodNameResolver;
import org.springframework.web.util.UrlPathHelper;

public class RequestMappingMethodsResolverTests extends TestCase {

	private Set<String> getMethodNames(Method[] methods) {
		Set<String> rtn = new HashSet<String>();
		for (Method method : methods) {
			rtn.add(method.getName());
		}
		return rtn;
	}

	private void assertMethods(Method[] methods, String... expected) {
		Set<String> resolvedNames = getMethodNames(methods);
		Set<String> expectedNames = new HashSet<String>(Arrays.asList(expected));
		assertTrue("expected " + expectedNames + " got " + resolvedNames, expectedNames.equals(resolvedNames));
	}

	private void assertMethods(Set<Method> methods, String... expected) {
		assertMethods(methods.toArray(new Method[] {}), expected);
	}

	private RequestMappingAnnotationMatch newMatch(String[] paths, RequestMethod[] requestMethods, String[] params) {
		RequestMappingAnnotation annotation = new RequestMappingAnnotation(paths, new HashSet<String>(Arrays
				.asList(params)), new HashSet<RequestMethod>(Arrays.asList(requestMethods)));
		RequestMappingAnnotationMatch match = new RequestMappingAnnotationMatch(null, annotation);
		match.getPaths().addAll(Arrays.asList(paths));
		return match;
	}

	public void testComparator() throws Exception {
		String lookupPath = "/an/example/path/go.do";
		RequestMappingAnnotationMatch[] m = new RequestMappingAnnotationMatch[7];
		m[6] = newMatch(new String[] { lookupPath }, new RequestMethod[] { RequestMethod.GET }, new String[] {
				"param=value", "other=true" });
		m[5] = newMatch(new String[] { lookupPath }, new RequestMethod[] { RequestMethod.GET },
				new String[] { "param=value" });
		m[4] = newMatch(new String[] { lookupPath }, new RequestMethod[] { RequestMethod.GET }, new String[] {});
		m[3] = newMatch(new String[] { lookupPath }, new RequestMethod[] {}, new String[] {});
		m[2] = newMatch(new String[] { "/an/example/*" }, new RequestMethod[] {}, new String[] {});
		m[1] = newMatch(new String[] { "/an/*" }, new RequestMethod[] {}, new String[] {});
		m[0] = newMatch(new String[] {}, new RequestMethod[] {}, new String[] { "param=value" });

		List<RequestMappingAnnotationMatch> s = new ArrayList<RequestMappingAnnotationMatch>(Arrays.asList(m));
		Collections.sort(s, new RequestMappingAnnotationMatchComparator(lookupPath));
		for (int i = m.length - 1; i >= 0; i--) {
			System.out.println(m[i]);
			System.out.println(s.get(m.length - 1 - i));
			assertSame(m[i], s.get(m.length - 1 - i));
		}
	}

	private RequestMappingMethodResolver newResolver(Class<?> handlerClass) {
		PathMatcher pathMatcher = new AntPathMatcher();
		MethodNameResolver methodNameResolver = new InternalPathMethodNameResolver();
		UrlPathHelper urlPathHelper = new UrlPathHelper();
		RequestMappingMethodResolver resolver = new RequestMappingMethodResolver(handlerClass, urlPathHelper,
				methodNameResolver, pathMatcher);
		return resolver;
	}

	private void doTestLookup(Class<?> controllerClass, boolean expectedTypeLevelMapping) throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/an/example/path/go.do");
		request.setParameter("param", "value");
		request.setParameter("param2", "value2");
		RequestMappingMethodResolver resolver = newResolver(controllerClass);
		assertEquals(expectedTypeLevelMapping, resolver.hasTypeLevelMapping());
		Method[] resolved = resolver.resolveHandlerMethods(request);
		assertEquals(3, resolved.length);
		assertEquals("exact", resolved[0].getName());
		assertEquals("wildcard", resolved[1].getName());
		assertEquals("paramValue", resolved[2].getName());
	}

	public void testLookup() throws Exception {
		doTestLookup(ExampleController.class, false);
	}

	public void testLookupWithTypeLevel() throws Exception {
		doTestLookup(TypeLevelExample.class, true);
	}

	public void testParams() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/an/example/path/go.do");
		request.setParameter("param", "value");
		RequestMappingMethodResolver resolver = newResolver(ParamsExample.class);
		assertMethods(resolver.resolveHandlerMethods(request), "param", "paramValue", "notParam");
	}

	public void testAmbiguous() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/an/example/path/ambiguous.do");
		request.setParameter("param", "value");
		RequestMappingMethodResolver resolver = newResolver(AmbiguousExample.class);
		try {
			resolver.resolveHandlerMethods(request);
			fail();
		} catch (IllegalStateException e) {
			assertEquals(
					"Ambiguous handler methods mapped for HTTP path '/an/example/path/ambiguous.do': "
							+ "{public void org.springframework.faces.mvc.annotation.RequestMappingMethodsResolverTests$AmbiguousExample.ambiguous(), "
							+ "public void org.springframework.faces.mvc.annotation.RequestMappingMethodsResolverTests$AmbiguousExample.ambiguous(javax.servlet.http.HttpServletRequest)}. "
							+ "If you intend to handle the same path in multiple methods, then factor them out into a "
							+ "dedicated handler class with that path mapped at the type level!", e.getMessage());
		}
	}

	public void testRequestMethod() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/an/example/path/go.do");
		RequestMappingMethodResolver resolver = newResolver(RequestMethodExample.class);
		assertTrue(resolver.hasHandlerMethods());
		assertMethods(resolver.resolveHandlerMethods(request), "get", "getAndPost");
	}

	public void testInitBinderMethod() throws Exception {
		RequestMappingMethodResolver resolver = newResolver(InitBinderExample.class);
		assertMethods(resolver.getInitBinderMethods(), "initBinder");
	}

	public void testModelAttributeMethod() throws Exception {
		RequestMappingMethodResolver resolver = newResolver(ModelAttributeExample.class);
		assertMethods(resolver.getModelAttributeMethods(), "getModelAttribute");
	}

	public void testSessionAttributes() throws Exception {
		RequestMappingMethodResolver resolver = newResolver(SessionAttributesExample.class);
		assertTrue(resolver.hasSessionAttributes());
		assertTrue(resolver.isSessionAttribute("sa", String.class));
		assertFalse(resolver.isSessionAttribute("madeup", String.class));
		assertEquals(new HashSet<String>(Arrays.asList("sa")), resolver.getActualSessionAttributeNames());
	}

	@FacesController
	static class ExampleController {
		@RequestMapping(value = "/an/example/path/go.do")
		public void exact() {
		};

		@RequestMapping(value = "/an/example/**")
		public void wildcard() {
		};

		@RequestMapping(value = "/missing/example/**")
		public void miss() {
		};

		@RequestMapping(params = "param=value")
		public void paramValue() {
		};
	}

	@FacesController
	static class ParamsExample {
		@RequestMapping(params = "param")
		public void param() {
		};

		@RequestMapping(params = "param=value")
		public void paramValue() {
		};

		@RequestMapping(params = "!param2")
		public void notParam() {
		};

		@RequestMapping(params = "param2")
		public void missingParam() {
		};

		@RequestMapping(params = "param2=value")
		public void missingParamValue() {
		};

		@RequestMapping(params = "param=value2")
		public void wrongParamValue() {
		};

		@RequestMapping(params = "!param")
		public void missingNotParam() {
		};
	}

	@FacesController
	static class AmbiguousExample {
		@RequestMapping(params = "param")
		public void ambiguous(HttpServletRequest request) {
		};

		@RequestMapping(params = "param")
		public void ambiguous() {
		};
	}

	@FacesController
	static class RequestMethodExample {
		@RequestMapping(method = { RequestMethod.GET })
		public void get() {
		};

		@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST })
		public void getAndPost() {
		};

		@RequestMapping(method = { RequestMethod.POST })
		public void post() {
		};
	}

	@FacesController
	@RequestMapping("/an/example/**")
	static class TypeLevelExample extends ExampleController {
	}

	@FacesController
	static class InitBinderExample {
		@InitBinder
		public void initBinder() {
		}
	}

	@FacesController
	static class ModelAttributeExample {
		@ModelAttribute
		public String getModelAttribute() {
			return "model";
		}
	}

	@FacesController
	@SessionAttributes("sa")
	static class SessionAttributesExample {
	}
}
