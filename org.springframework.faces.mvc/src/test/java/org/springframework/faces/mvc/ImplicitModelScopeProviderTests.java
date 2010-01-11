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

public class ImplicitModelScopeProviderTests extends TestCase {

	private static final Object MODEL_VALUE = new Object();

	private ModelScopeProvider provider = new ImplicitModelScopeProvider(ScopeType.VIEW);

	public void testDefinedScope() throws Exception {
		ScopedModelAttribute result = provider.getModelScope(new ScopedModelAttribute(null, "sessionScope.name"),
				MODEL_VALUE);
		assertEquals("session", result.getScope());
		assertEquals("name", result.getModelAttribute());
	}

	public void testNoDefinedScope() throws Exception {
		ScopedModelAttribute result = provider.getModelScope(new ScopedModelAttribute(null, "name"), MODEL_VALUE);
		assertEquals("view", result.getScope());
		assertEquals("name", result.getModelAttribute());
	}

	public void testDotAtStart() throws Exception {
		ScopedModelAttribute result = provider.getModelScope(new ScopedModelAttribute(null, ".name"), MODEL_VALUE);
		assertEquals("view", result.getScope());
		assertEquals(".name", result.getModelAttribute());
	}

	public void testScopeAlreadyDefined() throws Exception {
		ScopedModelAttribute result = provider.getModelScope(new ScopedModelAttribute("request", "session.name"),
				MODEL_VALUE);
		assertEquals("request", result.getScope());
		assertEquals("session.name", result.getModelAttribute());
	}

	private void doTestDoesNotMatch(String attribute) throws Exception {
		ScopedModelAttribute result = provider.getModelScope(new ScopedModelAttribute(null, attribute), MODEL_VALUE);
		assertEquals("view", result.getScope());
		assertEquals(attribute, result.getModelAttribute());
	}

	public void testExpectedNotToMatch() throws Exception {
		doTestDoesNotMatch("sessionScope.name.test");
		doTestDoesNotMatch("org.springframework.mvc.ModelName");
		doTestDoesNotMatch("Scope.test");
		doTestDoesNotMatch("sessionScope.");
		doTestDoesNotMatch("sessionScop.test");
	}

}
