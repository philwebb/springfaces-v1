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

import junit.framework.TestCase;

import org.springframework.util.ObjectUtils;

public class ScopedModelAttributeTests extends TestCase {

	public void testConstructor() throws Exception {
		ScopedModelAttribute scopedModelAttribute = new ScopedModelAttribute("scope", "attribute");
		assertEquals("scope", scopedModelAttribute.getScope());
		assertEquals("attribute", scopedModelAttribute.getModelAttribute());
	}

	public void testConstructorWithoutScope() throws Exception {
		ScopedModelAttribute scopedModelAttribute = new ScopedModelAttribute("attribute");
		assertEquals(null, scopedModelAttribute.getScope());
		assertEquals("attribute", scopedModelAttribute.getModelAttribute());
	}

	public void testEqualsAndHashCode() throws Exception {
		ScopedModelAttribute s1 = new ScopedModelAttribute("scope", "attribute");
		ScopedModelAttribute s2 = new ScopedModelAttribute("scope", "attribute");
		ScopedModelAttribute s3 = new ScopedModelAttribute(null, "attribute");
		ScopedModelAttribute s4 = new ScopedModelAttribute(null, "attribute");

		assertEquals(s1, s1);
		assertEquals(s1, s2);
		assertEquals(s3, s3);
		assertEquals(s3, s4);
		assertEquals(s1.hashCode(), s1.hashCode());
		assertEquals(s1.hashCode(), s2.hashCode());
		assertEquals(s3.hashCode(), s3.hashCode());
		assertEquals(s3.hashCode(), s4.hashCode());
		assertFalse(s1.equals(s3));
		assertFalse(s1.equals(s4));
		assertFalse(s1.equals(null));
		assertFalse(s1.equals(new Long(1234)));
	}

	public void testNewScope() throws Exception {
		ScopedModelAttribute s1 = new ScopedModelAttribute("scope1", "attribute");
		ScopedModelAttribute s2 = s1.newScope("scope2");
		assertFalse(s1.equals(s2));
		assertEquals("scope1", s1.getScope());
		assertEquals("attribute", s1.getModelAttribute());
		assertEquals("scope2", s2.getScope());
		assertEquals("attribute", s2.getModelAttribute());
	}

	public void testToString() throws Exception {
		ScopedModelAttribute s1 = new ScopedModelAttribute("scope1", "attribute");
		assertEquals("[ScopedModelAttribute@" + ObjectUtils.getIdentityHexString(s1)
				+ " scope = 'scope1', modelAttribute = 'attribute']", s1.toString());
	}
}
