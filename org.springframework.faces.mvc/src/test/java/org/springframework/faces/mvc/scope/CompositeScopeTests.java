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
package org.springframework.faces.mvc.scope;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.faces.mvc.test.MvcFacesTestUtils;
import org.springframework.faces.mvc.test.MvcFacesTestUtils.MethodCallAssertor;

public class CompositeScopeTests extends TestCase {

	private CompositeScope compositeScope;

	protected void setUp() throws Exception {
		compositeScope = new CompositeScope();
	}

	public void testAddNullScope() throws Exception {
		try {
			compositeScope.add(null, new MockScopeAvailabilityFilter());
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals("The scope is required", e.getMessage());
		}
	}

	public void testAddAndGetScope() throws Exception {
		MockScopeAvailabilityFilter filter1 = new MockScopeAvailabilityFilter();
		MockScopeAvailabilityFilter filter2 = new MockScopeAvailabilityFilter();
		Scope scope1 = (Scope) EasyMock.createMock(Scope.class);
		Scope scope2 = (Scope) EasyMock.createMock(Scope.class);
		compositeScope.add(scope1, filter1);
		compositeScope.add(scope2, filter2);

		// Test none found
		assertSame(compositeScope.nullScope, compositeScope.getScope());

		// Test filter one
		filter1.setAvailable(true);
		assertSame(scope1, compositeScope.getScope());

		// Test filter two
		filter1.setAvailable(false);
		filter2.setAvailable(true);
		assertSame(scope2, compositeScope.getScope());

		// Test filter one and two
		filter1.setAvailable(true);
		filter2.setAvailable(true);
		assertSame(scope1, compositeScope.getScope());
	}

	public void testGet() throws Exception {
		Scope scope = (Scope) MvcFacesTestUtils.methodTrackingObject(MockScope.class);
		compositeScope.add(scope, new MockScopeAvailabilityFilter(true));
		compositeScope.get("test", new ObjectFactory() {
			public Object getObject() throws BeansException {
				return "value";
			}
		});
		((MethodCallAssertor) scope).assertCalled("get");
	}

	public void testRemove() throws Exception {
		Scope scope = (Scope) MvcFacesTestUtils.methodTrackingObject(MockScope.class);
		compositeScope.add(scope, new MockScopeAvailabilityFilter(true));
		compositeScope.remove("test");
		((MethodCallAssertor) scope).assertCalled("remove");
	}

	public void testGetConversationId() throws Exception {
		Scope scope = (Scope) MvcFacesTestUtils.methodTrackingObject(MockScope.class);
		compositeScope.add(scope, new MockScopeAvailabilityFilter(true));
		compositeScope.getConversationId();
		((MethodCallAssertor) scope).assertCalled("getConversationId");
	}

	public void testRegisterDestructionCallback() throws Exception {
		Scope scope = (Scope) MvcFacesTestUtils.methodTrackingObject(MockScope.class);
		compositeScope.add(scope, new MockScopeAvailabilityFilter(true));
		compositeScope.registerDestructionCallback("test", new Runnable() {
			public void run() {
			}
		});
		((MethodCallAssertor) scope).assertCalled("registerDestructionCallback");
	}

	public void testGetWhenNullScope() throws Exception {
		try {
			compositeScope.get("test", new ObjectFactory() {
				public Object getObject() throws BeansException {
					fail("Should not call getObject");
					return null;
				}
			});
			fail();
		} catch (IllegalStateException e) {
			assertEquals(
					"Unable to get value for 'test' as no active delegate scope was "
							+ "located.  Ensure that you are attempting to access the bean from a "
							+ "valid execution context", e.getMessage());
		}

	}

	public void testRemoveWhenNullScope() throws Exception {
		assertNull(compositeScope.remove("test"));
	}

	public void testGetConversationIdWhenNullScope() throws Exception {
		assertNull(compositeScope.getConversationId());
	}

	public void testRegisterDestructionCallbackWhenNullScope() throws Exception {
		// Should not throw or call
		compositeScope.registerDestructionCallback("test", new Runnable() {
			public void run() {
				fail();
			}
		});
	}

	public static class MockScopeAvailabilityFilter implements ScopeAvailabilityFilter {
		private boolean available;

		public MockScopeAvailabilityFilter() {
			this(false);
		}

		public MockScopeAvailabilityFilter(boolean available) {
			this.available = available;
		}

		public boolean isAvailable(Scope scope) {
			return available;
		}

		public void setAvailable(boolean available) {
			this.available = available;
		}
	}

	public static abstract class MockScope implements Scope {
	}

}
