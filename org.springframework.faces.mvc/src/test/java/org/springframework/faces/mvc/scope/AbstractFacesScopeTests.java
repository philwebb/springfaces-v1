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
import org.springframework.faces.mvc.execution.RequestContext;
import org.springframework.faces.mvc.execution.RequestContextHolder;
import org.springframework.faces.mvc.execution.RequestControlContext;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.core.collection.MutableAttributeMap;

public class AbstractFacesScopeTests extends TestCase {

	private MutableAttributeMap map;
	private AbstractFacesScope scope;

	protected void setUp() throws Exception {
		map = new LocalAttributeMap();
		scope = new MockFacesScope();
	}

	public void testGetExisting() throws Exception {
		map.put("test", "value");
		Object actual = scope.get("test", new ObjectFactory() {
			public Object getObject() throws BeansException {
				fail("Should never call factory");
				return null;
			}
		});
		assertEquals("value", actual);
	}

	public void testGetMissing() throws Exception {
		Object actual = scope.get("test", new ObjectFactory() {
			public Object getObject() throws BeansException {
				return "value";
			}
		});
		assertEquals("value", actual);
	}

	public void testRemove() throws Exception {
		map.put("test", "value");
		scope.remove("test");
		assertTrue(map.isEmpty());
	}

	public void testGetConversationId() throws Exception {
		assertNull(scope.getConversationId());
	}

	public void testGetRequiredRequestContextMissing() throws Exception {
		try {
			scope.getRequiredRequestContext();
			fail();
		} catch (IllegalStateException e) {
			assertEquals("No request context bound to this thread; to access Faces MVC scoped beans "
					+ "you must be running in a Faces MVC execution request", e.getMessage());
		}
	}

	public void testGetRequiredRequestContextPresent() throws Exception {
		RequestControlContext requestContext = (RequestControlContext) EasyMock.createMock(RequestControlContext.class);
		RequestContextHolder.setRequestContext(requestContext);
		try {
			RequestContext actual = scope.getRequiredRequestContext();
			assertSame(requestContext, actual);
		} finally {
			RequestContextHolder.setRequestContext(null);
		}
	}

	public void testRegisterCallback() throws Exception {
		// We don't support destruction callbacks but the register method should not fail
		scope.registerDestructionCallback("test", new Runnable() {
			public void run() {
				fail();
			}
		});
	}

	private class MockFacesScope extends AbstractFacesScope {
		protected MutableAttributeMap getScope() throws IllegalStateException {
			return map;
		}
	}

}
