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

import org.easymock.EasyMock;
import org.springframework.faces.mvc.execution.RequestContextHolder;
import org.springframework.faces.mvc.execution.RequestControlContext;

public class RequestContextELResolverTests extends TestCase {

	private RequestContextELResolver resolver;
	private RequestControlContext requestContext;

	protected void setUp() throws Exception {
		requestContext = (RequestControlContext) EasyMock.createMock(RequestControlContext.class);
		resolver = new RequestContextELResolver();
	}

	private void setupHolder() {
		RequestContextHolder.setRequestContext(requestContext);
	}

	protected void tearDown() throws Exception {
		RequestContextHolder.setRequestContext(null);
	}

	public void testname() throws Exception {
		assertFalse(resolver.isAvailable());
		setupHolder();
		assertTrue(resolver.isAvailable());
	}

	public void testHandles() throws Exception {
		assertTrue(resolver.handles("mvcFacesRequestContext"));
		assertFalse(resolver.handles("doesNotExist"));
	}

	public void testGet() throws Exception {
		assertNull(resolver.get("mvcFacesRequestContext"));
		setupHolder();
		assertNotNull(resolver.get("mvcFacesRequestContext"));
		assertNull(resolver.get("doesNotExist"));
	}
}
