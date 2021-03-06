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

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import junit.framework.TestCase;

import org.apache.shale.test.mock.MockFacesContext12;

public class MvcFacesStateHolderComponentTests extends TestCase {

	private MvcFacesStateHolderComponent component;

	protected void setUp() throws Exception {
		this.component = new MvcFacesStateHolderComponent();
	}

	public void testId() throws Exception {
		assertEquals(MvcFacesStateHolderComponent.COMPONENT_ID, component.getId());
	}

	public void testIdCannotBeSet() throws Exception {
		component.setId("newId");
		assertEquals(MvcFacesStateHolderComponent.COMPONENT_ID, component.getId());
	}

	public void testGetFamily() throws Exception {
		assertEquals("javax.faces.Parameter", component.getFamily());
	}

	public void testGetRenderer() throws Exception {
		assertNull(component.getRenderer());
	}

	public void testGetClientId() throws Exception {
		assertEquals(MvcFacesStateHolderComponent.COMPONENT_ID, component.getClientId(null));
	}

	public void testTransient() throws Exception {
		assertFalse(component.isTransient());
		component.setTransient(true);
		assertTrue(component.isTransient());
	}

	public void testStateSaveAndRestore() throws Exception {
		component.getViewScope().put("test", "value");
		Object state = component.saveState(null);
		component = new MvcFacesStateHolderComponent();
		component.restoreState(null, state);
		assertEquals("value", component.getViewScope().get("test"));
	}

	public void testAttachAndLocate() throws Exception {
		UIViewRoot viewRoot = new UIViewRoot();
		try {
			MvcFacesStateHolderComponent.locate(null, viewRoot, true);
			fail();
		} catch (IllegalArgumentException e) {
		}
		MvcFacesStateHolderComponent.attach(null, viewRoot);
		assertNotNull(MvcFacesStateHolderComponent.locate(null, viewRoot, true));
	}

	public void testLocateFromFacesContext() throws Exception {
		FacesContext facesContext = new MockFacesContext12();
		UIViewRoot viewRoot = new UIViewRoot();
		facesContext.setViewRoot(viewRoot);
		MvcFacesStateHolderComponent.attach(null, viewRoot);
		assertNotNull(MvcFacesStateHolderComponent.locate(facesContext, true));
		assertNotNull(MvcFacesStateHolderComponent.locate(facesContext, null, true));
	}
}
