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

import org.springframework.faces.mvc.annotation.RequestMappingMethodsResolver.RequestMappingAnnotation;
import org.springframework.faces.mvc.annotation.RequestMappingMethodsResolver.RequestMappingAnnotationMatch;
import org.springframework.faces.mvc.annotation.RequestMappingMethodsResolver.RequestMappingAnnotationMatchComparator;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.multiaction.InternalPathMethodNameResolver;
import org.springframework.web.servlet.mvc.multiaction.MethodNameResolver;
import org.springframework.web.util.UrlPathHelper;

public class RequestMappingMethodsResolverTests extends TestCase {

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

	private RequestMappingMethodsResolver newResolver(Class<?> handlerClass) {
		PathMatcher pathMatcher = new AntPathMatcher();
		MethodNameResolver methodNameResolver = new InternalPathMethodNameResolver();
		UrlPathHelper urlPathHelper = new UrlPathHelper();
		RequestMappingMethodsResolver resolver = new RequestMappingMethodsResolver(handlerClass, urlPathHelper,
				methodNameResolver, pathMatcher);
		return resolver;
	}

	public void testLookup() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/an/example/path/go.do");
		request.setParameter("param", "value");
		request.setParameter("param2", "value2");
		RequestMappingMethodsResolver resolver = newResolver(ExampleController.class);
		Method[] resolved = resolver.resolveHandlerMethods(request);
		assertEquals(3, resolved.length);
		assertEquals("exact", resolved[0].getName());
		assertEquals("wildcard", resolved[1].getName());
		assertEquals("paramValue", resolved[2].getName());
	}

	public void testParams() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/an/example/path/go.do");
		request.setParameter("param", "value");
		RequestMappingMethodsResolver resolver = newResolver(ParamsExample.class);
		Method[] resolved = resolver.resolveHandlerMethods(request);
		Set<String> actual = new HashSet<String>();
		for (Method method : resolved) {
			actual.add(method.getName());
		}
		Set<String> expected = new HashSet<String>(Arrays.asList("param", "paramValue", "notParam"));
		assertTrue("expected " + expected + " got " + actual, expected.equals(actual));
	}

	public void testAmbiguous() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/an/example/path/ambiguous.do");
		request.setParameter("param", "value");
		RequestMappingMethodsResolver resolver = newResolver(AmbiguousExample.class);
		try {
			resolver.resolveHandlerMethods(request);
			fail();
		} catch (IllegalStateException e) {
			assertEquals(
					"Ambiguous handler methods mapped for HTTP path '/an/example/path/ambiguous.do': "
							+ "{public void org.springframework.faces.mvc.annotation.RequestMappingMethodsResolverTest$AmbiguousExample.ambiguous(), "
							+ "public void org.springframework.faces.mvc.annotation.RequestMappingMethodsResolverTest$AmbiguousExample.ambiguous(javax.servlet.http.HttpServletRequest)}. "
							+ "If you intend to handle the same path in multiple methods, then factor them out into a "
							+ "dedicated handler class with that path mapped at the type level!", e.getMessage());
		}
	}

	public void testRequestMethod() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/an/example/path/go.do");
		RequestMappingMethodsResolver resolver = newResolver(RequestMethodExample.class);
		Method[] resolved = resolver.resolveHandlerMethods(request);
		Set<String> actual = new HashSet<String>();
		for (Method method : resolved) {
			actual.add(method.getName());
		}
		Set<String> expected = new HashSet<String>(Arrays.asList("get", "getAndPost"));
		assertTrue("expected " + expected + " got " + actual, expected.equals(actual));
	}

	@Controller
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

	@Controller
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

	static class AmbiguousExample {
		@RequestMapping(params = "param")
		public void ambiguous(HttpServletRequest request) {
		};

		@RequestMapping(params = "param")
		public void ambiguous() {
		};
	}

	@Controller
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

}
