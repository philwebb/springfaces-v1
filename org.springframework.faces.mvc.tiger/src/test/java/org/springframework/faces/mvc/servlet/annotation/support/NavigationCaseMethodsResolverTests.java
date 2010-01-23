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
package org.springframework.faces.mvc.servlet.annotation.support;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.springframework.faces.mvc.navigation.annotation.NavigationCase;
import org.springframework.faces.mvc.navigation.annotation.NavigationRules;
import org.springframework.faces.mvc.servlet.annotation.support.NavigationCaseMethodResolver;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.multiaction.InternalPathMethodNameResolver;
import org.springframework.web.servlet.mvc.multiaction.MethodNameResolver;
import org.springframework.web.util.UrlPathHelper;

public class NavigationCaseMethodsResolverTests extends TestCase {

	private NavigationCaseMethodResolver resolver;

	public NavigationCaseMethodsResolverTests() {
		Class<?> handlerType = ExampleController.class;
		UrlPathHelper urlPathHelper = new UrlPathHelper();
		MethodNameResolver methodNameResolver = new InternalPathMethodNameResolver();
		PathMatcher pathMatcher = new AntPathMatcher();
		resolver = new NavigationCaseMethodResolver(handlerType, urlPathHelper, methodNameResolver, pathMatcher);
	}

	private Set<String> getMethodNames(Set<Method> methods) {
		Set<String> rtn = new HashSet<String>();
		for (Method method : methods) {
			rtn.add(method.getName());
		}
		return rtn;
	}

	public void testGlobalNavigationMethods() throws Exception {
		assertTrue(resolver.hasGlobalNavigationMethods());
		Set<Method> globals = resolver.getGlobalNavigationMethods();
		assertEquals(2, globals.size());
		Set<String> expected = new HashSet<String>(Arrays.asList("global1", "global2"));
		assertEquals(expected, getMethodNames(globals));
	}

	public void testResolveHandlerMethods() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/an/example/path/go.do");
		Method[] resolved = resolver.resolveNavigationMethods(request);
		assertEquals(4, resolved.length);
		assertEquals("exact", resolved[0].getName());
		assertEquals("wildcard", resolved[1].getName());
		assertEquals("global1", resolved[2].getName());
		assertEquals("global2", resolved[3].getName());
	}

	@Controller
	static class ExampleController {
		@RequestMapping(value = "/an/example/path/go.do")
		@NavigationRules(@NavigationCase())
		public void exact() {
		};

		@RequestMapping(value = "/an/example/**")
		@NavigationCase
		public void wildcard() {
		};

		@NavigationRules(@NavigationCase())
		public void global1() {
		};

		@NavigationCase
		public void global2() {
		};
	}
}
