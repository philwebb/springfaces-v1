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
package org.springframework.faces.mvc.execution.repository;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.springframework.binding.collection.SharedMap;
import org.springframework.binding.collection.SharedMapDecorator;
import org.springframework.faces.mvc.execution.ExecutionContextKey;
import org.springframework.faces.mvc.execution.MvcFacesRequestContext;
import org.springframework.faces.mvc.execution.repository.SessionBindingExecutionContextRepository.StoredExecutionContextContainer;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.core.collection.LocalSharedAttributeMap;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.core.collection.SharedAttributeMap;

public class SessionBindingExecutionContextRepositoryTests extends TestCase {

	private static final String SESSION_KEY = "mvcFacesExecutions";

	private SessionBindingExecutionContextRepository repository;
	private SharedMap sharedSessionMap;
	private HttpServletRequest request;
	private MvcFacesRequestContext requestContext;
	private MutableAttributeMap flashScope;

	protected void setUp() throws Exception {
		sharedSessionMap = new SharedMapDecorator(new HashMap());
		repository = new MockSessionBindingExecutionContextRepository();
		request = (HttpServletRequest) EasyMock.createNiceMock(HttpServletRequest.class);
		requestContext = (MvcFacesRequestContext) EasyMock.createNiceMock(MvcFacesRequestContext.class);
		flashScope = new LocalAttributeMap();
		EasyMock.expect(requestContext.getFlashScope()).andStubReturn(flashScope);
		EasyMock.replay(new Object[] { requestContext, request });
	}

	public void testParseValidKey() throws Exception {
		assertEquals(new IntegerExecutionContextKey(123), repository.parseKey("123"));
	}

	public void testParseWithBindingException() throws Exception {
		try {
			repository.parseKey("bad");
			fail();
		} catch (BadlyFormattedExecutionContextKeyException e) {
			assertEquals("Unable to parse string-encoded execution key + 'bad'", e.getMessage());
		}
	}

	public void testTestWithoutData() throws Exception {
		flashScope.clear();
		repository.save(request, requestContext);
		assertTrue(sharedSessionMap.isEmpty());
	}

	public void testFirstTimeSave() throws Exception {
		flashScope.put("testkey", "testvalue");
		ExecutionContextKey key = repository.save(request, requestContext);
		assertEquals(new IntegerExecutionContextKey(1), key);
		assertTrue(sharedSessionMap.containsKey(SESSION_KEY));
		flashScope.clear();
		repository.restore(key, request, requestContext);
		assertEquals("testvalue", flashScope.get("testkey"));
	}

	public void testLimtedToOneAndCleanup() throws Exception {
		repository.setMaxExecutions(1);
		flashScope.clear().put("k1", "v1");
		ExecutionContextKey key1 = repository.save(request, requestContext);
		flashScope.clear().put("k2", "v2");
		ExecutionContextKey key2 = repository.save(request, requestContext);
		assertEquals(new IntegerExecutionContextKey(1), key1);
		assertEquals(new IntegerExecutionContextKey(2), key2);
		flashScope.clear();
		repository.restore(key2, request, requestContext);
		assertEquals("v2", flashScope.get("k2"));
		try {
			repository.restore(key1, request, requestContext);
			fail();
		} catch (NoSuchExecutionException e) {
			assertEquals("Unable to locate a Faces MVC execution with the key '1'", e.getMessage());
		}
	}

	public void testCustomSessionKey() throws Exception {
		repository.setSessionKey("custom");
		flashScope.clear().put("k1", "v1");
		repository.save(request, requestContext);
		assertTrue(sharedSessionMap.containsKey("custom"));
	}

	public void testDoubleCleanup() throws Exception {
		flashScope.put("k1", "v1");
		ExecutionContextKey key1 = repository.save(request, requestContext);
		repository.restore(key1, request, requestContext);
		try {
			repository.restore(key1, request, requestContext);
			fail();
		} catch (NoSuchExecutionException e) {
			assertEquals("Unable to locate a Faces MVC execution with the key '1'", e.getMessage());
		}
	}

	private void doTestSize(int inserts, int expected) throws Exception {
		flashScope.put("k1", "v1");
		for (int i = 0; i < inserts; i++) {
			repository.save(request, requestContext);
		}
		StoredExecutionContextContainer container = (StoredExecutionContextContainer) sharedSessionMap.get(SESSION_KEY);
		assertEquals(expected, container.getSize());
	}

	public void testMaxout() throws Exception {
		doTestSize(50, 5);
	}

	public void testUnlmited() throws Exception {
		repository.setMaxExecutions(-1);
		doTestSize(50, 50);
	}

	private class MockSessionBindingExecutionContextRepository extends SessionBindingExecutionContextRepository {
		protected SharedAttributeMap createSessionMap(HttpServletRequest request) {
			return new LocalSharedAttributeMap(sharedSessionMap);
		}
	}
}
