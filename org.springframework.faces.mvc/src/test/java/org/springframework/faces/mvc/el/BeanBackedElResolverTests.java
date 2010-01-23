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
package org.springframework.faces.mvc.el;

import junit.framework.TestCase;

public class BeanBackedElResolverTests extends TestCase {

	private BeanBackedElResolver resolver;
	private Object bean;

	protected void setUp() throws Exception {
		super.setUp();
		this.bean = new TestBean();
		this.resolver = new TestBeanBackedElResolver();
	}

	public void testIsReadOnly() throws Exception {
		assertTrue(resolver.isReadOnly("string"));
	}

	public void testAvailable() throws Exception {
		assertTrue(resolver.isAvailable());
		this.bean = null;
		assertFalse(resolver.isAvailable());
	}

	public void testHandlesAndResolve() throws Exception {
		assertTrue(resolver.handles("stringValue"));
		assertEquals("string", resolver.get("stringValue"));
		assertTrue(resolver.handles("longValue"));
		assertEquals(new Long(12345), resolver.get("longValue"));
		assertTrue(resolver.handles("intValue"));
		assertEquals(new Integer(1), resolver.get("intValue"));
		assertTrue(resolver.handles("alias"));
		assertEquals("string", resolver.get("alias"));
		assertFalse(resolver.handles("unmapped"));
	}

	public void testNotMapped() throws Exception {
		assertFalse(resolver.handles("missing"));
		assertNull(resolver.get("missing"));
	}

	private class TestBeanBackedElResolver extends BeanBackedElResolver {

		public TestBeanBackedElResolver() {
			map("stringValue");
			map("longValue");
			map("intValue");
			map("alias", "stringValue");
		}

		protected Object getBean() {
			return bean;
		}
	}

	public static class TestBean {
		private String stringValue = "string";
		private Long longValue = new Long(12345);
		private int intValue = 1;
		private String unmapped = "unmapped";

		public String getStringValue() {
			return stringValue;
		}

		public Long getLongValue() {
			return longValue;
		}

		public int getIntValue() {
			return intValue;
		}

		public String getUnmapped() {
			return unmapped;
		}
	}
}
