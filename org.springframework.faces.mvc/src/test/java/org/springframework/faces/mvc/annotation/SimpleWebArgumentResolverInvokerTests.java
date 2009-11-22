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

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.web.bind.support.WebArgumentResolver;
import org.springframework.web.context.request.NativeWebRequest;

public class SimpleWebArgumentResolverInvokerTests extends TestCase {

	private boolean invoked;

	protected void setUp() throws Exception {
		super.setUp();
		this.invoked = false;
	}

	public void testInvoke() throws Exception {
		ParameterNameDiscoverer parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();
		WebArgumentResolver[] webArgumentResolvers = new WebArgumentResolver[] { new MockWebArgumentResolver() };
		SimpleWebArgumentResolverInvoker invoker = new SimpleWebArgumentResolverInvoker(parameterNameDiscoverer,
				webArgumentResolvers);
		NativeWebRequest webRequest = EasyMock.createMock(NativeWebRequest.class);
		Method method = getClass().getMethod("methodToInvoke",
				new Class[] { String.class, Integer.class, String.class });
		invoker.invoke(method, this, webRequest);
		assertTrue(invoked);
	}

	public void testThrows() throws Exception {
		WebArgumentResolver[] webArgumentResolvers = new WebArgumentResolver[] { new MockWebArgumentResolver() };
		SimpleWebArgumentResolverInvoker invoker = new SimpleWebArgumentResolverInvoker(null, webArgumentResolvers);
		NativeWebRequest webRequest = EasyMock.createMock(NativeWebRequest.class);
		Method method = getClass().getMethod("methodThatThrows", new Class[] {});
		try {
			invoker.invoke(method, this, webRequest);
			fail();
		} catch (IllegalStateException e) {
			assertEquals("test", e.getMessage());
		}

	}

	public void methodToInvoke(String a1, Integer a2, String a3) {
		invoked = true;
		assertEquals("test", a1);
		assertNull(a2);
		assertEquals("test", a3);
	}

	public void methodThatThrows() {
		throw new IllegalStateException("test");
	}

	private static class MockWebArgumentResolver implements WebArgumentResolver {
		public Object resolveArgument(MethodParameter methodParameter, NativeWebRequest webRequest) throws Exception {
			if (String.class.equals(methodParameter.getParameterType())) {
				return "test";
			}
			return UNRESOLVED;
		}
	}
}
