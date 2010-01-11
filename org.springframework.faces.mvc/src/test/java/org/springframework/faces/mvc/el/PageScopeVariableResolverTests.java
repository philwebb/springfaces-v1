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

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.el.VariableResolver;

import junit.framework.TestCase;

import org.apache.shale.test.mock.MockFacesContext12;
import org.springframework.faces.mvc.el.PageScopeVariableResolver;
import org.springframework.faces.mvc.support.MvcFacesStateHolderComponent;
import org.springframework.faces.mvc.test.MvcFacesTestUtils;

public class PageScopeVariableResolverTests extends TestCase {

	public void testPage() throws Exception {
		UIViewRoot viewRoot = new UIViewRoot();
		FacesContext facesContext = new MockFacesContext12();
		facesContext.setViewRoot(viewRoot);
		MvcFacesStateHolderComponent.attach(facesContext, viewRoot);
		MvcFacesStateHolderComponent.locate(facesContext, true).getViewScope().put("test", "value");
		VariableResolver nextResolver = (VariableResolver) MvcFacesTestUtils.nullImplementation(VariableResolver.class);
		PageScopeVariableResolver resolver = new PageScopeVariableResolver(nextResolver);
		assertEquals("value", resolver.resolveVariable(facesContext, "test"));
		assertNull(resolver.resolveVariable(facesContext, "test2"));
	}
}
