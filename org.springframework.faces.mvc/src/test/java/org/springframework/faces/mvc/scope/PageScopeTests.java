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

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import org.apache.shale.test.base.AbstractJsfTestCase;
import org.apache.shale.test.mock.MockFacesContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.faces.mvc.scope.PageScope;
import org.springframework.faces.mvc.support.MvcFacesStateHolderComponent;

public class PageScopeTests extends AbstractJsfTestCase implements ObjectFactory {

	private String object;

	public PageScopeTests(String name) {
		super(name);
	}

	public Object getObject() throws BeansException {
		return object;
	}

	public void testViewScope() throws Exception {
		MvcFacesStateHolderComponent.attach(facesContext, facesContext.getViewRoot());
		PageScope pageScope = new PageScope();
		this.object = "value1";
		assertEquals("value1", pageScope.get("test", this));
		// Change the object factory value and ensure we get back the first one
		this.object = "value2";
		assertEquals("value1", pageScope.get("test", this));

		// Test the removal
		assertTrue(MvcFacesStateHolderComponent.locate(facesContext, true).getViewScope().contains("test"));
		pageScope.remove("test");
		assertFalse(MvcFacesStateHolderComponent.locate(facesContext, true).getViewScope().contains("test"));
	}

	public void testNoPageScopeHolder() throws Exception {
		PageScope pageScope = new PageScope();
		this.object = "value1";
		try {
			pageScope.get("test", this);
			fail("Did not throw IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}
	}

	public void testConverstaionIdNullContext() throws Exception {
		MockFacesContext.getCurrentInstance().release();
		assertNull(FacesContext.getCurrentInstance());
		PageScope pageScope = new PageScope();
		assertNull(pageScope.getConversationId());
	}

	public void testConverstaionIdNullPage() throws Exception {
		facesContext.setViewRoot(null);
		PageScope pageScope = new PageScope();
		assertNull(pageScope.getConversationId());
	}

	public void testConverstaionId() throws Exception {
		PageScope pageScope = new PageScope();
		pageScope.getConversationId();
		UIViewRoot viewRoot = new UIViewRoot();
		viewRoot.setViewId("test");
		facesContext.setViewRoot(viewRoot);
		assertEquals("test", pageScope.getConversationId());
	}

	public void testRegisterCallback() throws Exception {
		// We don't support destruction callbacks but the register method should not fail
		PageScope pageScope = new PageScope();
		pageScope.registerDestructionCallback("test", new Runnable() {
			public void run() {
				fail();
			}
		});
	}

	public void testResolveContextualObject() throws Exception {
		// Spring 3 API change, implemented for compatibility
		PageScope pageScope = new PageScope();
		assertNull(pageScope.resolveContextualObject(""));
	}
}
