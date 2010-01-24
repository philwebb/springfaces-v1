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
import org.springframework.faces.mvc.execution.RequestContextHolder;
import org.springframework.faces.mvc.execution.RequestControlContext;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.core.collection.MutableAttributeMap;

public class RequestScopeTests extends TestCase {

	private RequestControlContext requestContext;
	private MutableAttributeMap requestScope;

	protected void setUp() throws Exception {
		requestScope = new LocalAttributeMap();
		requestContext = (RequestControlContext) EasyMock.createMock(RequestControlContext.class);
		RequestContextHolder.setRequestContext(requestContext);
		EasyMock.expect(requestContext.getRequestScope()).andReturn(requestScope);
		EasyMock.replay(new Object[] { requestContext });
	}

	protected void tearDown() throws Exception {
		RequestContextHolder.setRequestContext(null);
		EasyMock.verify(new Object[] { requestContext });
	}

	public void testGetScope() throws Exception {
		assertSame(requestScope, new RequestScope().getScope());
	}

}
