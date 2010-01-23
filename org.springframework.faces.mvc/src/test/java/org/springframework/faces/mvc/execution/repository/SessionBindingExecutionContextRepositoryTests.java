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

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.springframework.faces.mvc.context.ExternalContext;
import org.springframework.faces.mvc.execution.ExecutionContextKey;
import org.springframework.faces.mvc.execution.RequestContext;
import org.springframework.faces.mvc.execution.repository.SessionBindingExecutionContextRepository.StoredExecutionContextContainer;
import org.springframework.faces.mvc.support.WebFlowExternalContextAdapter;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.test.MockExternalContext;

public class SessionBindingExecutionContextRepositoryTests extends TestCase {

	private static final String SESSION_KEY = "mvcFacesExecutions";

	private SessionBindingExecutionContextRepository repository;
	private RequestContext requestContext;
	private MutableAttributeMap flashScope;
	private ExternalContext externalContext;

	protected void setUp() throws Exception {
		repository = new SessionBindingExecutionContextRepository();
		requestContext = (RequestContext) EasyMock.createNiceMock(RequestContext.class);
		flashScope = new LocalAttributeMap();
		externalContext = new WebFlowExternalContextAdapter(new MockExternalContext());
		EasyMock.expect(requestContext.getFlashScope()).andStubReturn(flashScope);
		EasyMock.expect(requestContext.getExternalContext()).andStubReturn(externalContext);
		EasyMock.replay(new Object[] { requestContext });
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
		repository.save(requestContext);
		assertTrue(externalContext.getSessionMap().isEmpty());
	}

	public void testFirstTimeSave() throws Exception {
		flashScope.put("testkey", "testvalue");
		ExecutionContextKey key = repository.save(requestContext);
		assertEquals(new IntegerExecutionContextKey(1), key);
		assertTrue(externalContext.getSessionMap().contains(SESSION_KEY));
		flashScope.clear();
		repository.restore(key, requestContext);
		assertEquals("testvalue", flashScope.get("testkey"));
	}

	public void testLimtedToOneAndCleanup() throws Exception {
		repository.setMaxExecutions(1);
		flashScope.clear().put("k1", "v1");
		ExecutionContextKey key1 = repository.save(requestContext);
		flashScope.clear().put("k2", "v2");
		ExecutionContextKey key2 = repository.save(requestContext);
		assertEquals(new IntegerExecutionContextKey(1), key1);
		assertEquals(new IntegerExecutionContextKey(2), key2);
		flashScope.clear();
		repository.restore(key2, requestContext);
		assertEquals("v2", flashScope.get("k2"));
		try {
			repository.restore(key1, requestContext);
			fail();
		} catch (NoSuchExecutionException e) {
			assertEquals("Unable to locate a Faces MVC execution with the key '1'", e.getMessage());
		}
	}

	public void testCustomSessionKey() throws Exception {
		repository.setSessionKey("custom");
		flashScope.clear().put("k1", "v1");
		repository.save(requestContext);
		assertTrue(externalContext.getSessionMap().contains("custom"));
	}

	public void testDoubleCleanup() throws Exception {
		flashScope.put("k1", "v1");
		ExecutionContextKey key1 = repository.save(requestContext);
		repository.restore(key1, requestContext);
		try {
			repository.restore(key1, requestContext);
			fail();
		} catch (NoSuchExecutionException e) {
			assertEquals("Unable to locate a Faces MVC execution with the key '1'", e.getMessage());
		}
	}

	private void doTestSize(int inserts, int expected) throws Exception {
		flashScope.put("k1", "v1");
		for (int i = 0; i < inserts; i++) {
			repository.save(requestContext);
		}
		StoredExecutionContextContainer container = (StoredExecutionContextContainer) externalContext.getSessionMap()
				.get(SESSION_KEY);
		assertEquals(expected, container.getSize());
	}

	public void testMaxout() throws Exception {
		doTestSize(50, 5);
	}

	public void testUnlmited() throws Exception {
		repository.setMaxExecutions(-1);
		doTestSize(50, 50);
	}
}
