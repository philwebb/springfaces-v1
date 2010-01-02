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
package org.springframework.faces.mvc.support;

import javax.faces.context.FacesContext;
import javax.faces.el.VariableResolver;

import junit.framework.TestCase;

import org.apache.shale.test.mock.MockFacesContext12;
import org.easymock.EasyMock;
import org.springframework.faces.mvc.FacesHandler;
import org.springframework.faces.mvc.MvcFacesTestUtils;

public class MvcHandlerVariableResolverTest extends TestCase {
	public void testResolve() throws Exception {
		FacesContext facesContext = new MockFacesContext12();
		VariableResolver nextResolver = (VariableResolver) MvcFacesTestUtils.nullImplementation(VariableResolver.class);
		MvcHandlerVariableResolver resolver = new MvcHandlerVariableResolver(nextResolver);
		MvcFacesContext mvcFacesContext = (MvcFacesContext) EasyMock.createMock(MvcFacesContext.class);
		FacesHandler facesHandler = (FacesHandler) EasyMock.createMock(FacesHandler.class);
		EasyMock.expect(facesHandler.resolveVariable("test")).andReturn(null);
		EasyMock.replay(new Object[] { facesHandler });
		MvcFacesRequestContext requestContext = new MvcFacesRequestContext(mvcFacesContext, facesHandler);
		resolver.resolveVariable(facesContext, "test");
		EasyMock.verify(new Object[] { facesHandler });
	}
}
