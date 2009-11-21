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
package org.springframework.faces.mvc;

import javax.faces.context.FacesContext;

import junit.framework.TestCase;

import org.apache.shale.test.mock.MockFacesContext;

public class NavigationRequestEventTests extends TestCase {

	private static void assertHashCodeAndEquals(NavigationRequestEvent e1, NavigationRequestEvent e2) {
		assertNotSame(e1, e2);
		assertEquals(e1.hashCode(), e2.hashCode());
		assertEquals(e1, e2);
	}

	private static void assertNotHashCodeAndEquals(NavigationRequestEvent e1, NavigationRequestEvent e2) {
		assertNotSame(e1, e2);
		assertFalse(e1.equals(e2));
	}

	public void testEqualsAndHashCode() throws Exception {

		FacesContext facesContext = new MockFacesContext();
		NavigationRequestEvent e1a = new NavigationRequestEvent(facesContext, null, null);
		NavigationRequestEvent e1b = new NavigationRequestEvent(new MockFacesContext(), null, null);

		NavigationRequestEvent e2a = new NavigationRequestEvent(facesContext, null, "run");
		NavigationRequestEvent e2b = new NavigationRequestEvent(facesContext, null, "run");

		NavigationRequestEvent e3a = new NavigationRequestEvent(facesContext, null, "runx");

		NavigationRequestEvent e4a = new NavigationRequestEvent(facesContext, "#{action.test}", "run");
		NavigationRequestEvent e4b = new NavigationRequestEvent(facesContext, "#{action.test}", "run");

		NavigationRequestEvent e5a = new NavigationRequestEvent(facesContext, "#{action.test}", "runx");

		NavigationRequestEvent e6a = new NavigationRequestEvent(facesContext, "#{action.testx}", "run");

		assertEquals(e1a, e1a);
		assertFalse(e1a.equals(null));
		assertFalse(e1a.equals("test"));
		assertHashCodeAndEquals(e1a, e1b);
		assertHashCodeAndEquals(e1a, e1b);
		assertNotHashCodeAndEquals(e1a, e2a);
		assertNotHashCodeAndEquals(e1a, e3a);
		assertNotHashCodeAndEquals(e1a, e4a);
		assertHashCodeAndEquals(e2a, e2b);
		assertNotHashCodeAndEquals(e2a, e3a);
		assertHashCodeAndEquals(e4a, e4b);
		assertNotHashCodeAndEquals(e4a, e5a);
		assertNotHashCodeAndEquals(e4a, e6a);
		assertNotHashCodeAndEquals(e5a, e6a);
	}

	public void testToString() throws Exception {
		NavigationRequestEvent event = new NavigationRequestEvent(new MockFacesContext(), "#{action.test}", "run");
		assertEquals("JSF Navigation Request Event (fromAction=\"#{action.test}\", outcome=\"run\")", event.toString());
	}

	public void testValues() throws Exception {
		NavigationRequestEvent event = new NavigationRequestEvent(new MockFacesContext(), "#{action.test}", "run");
		assertEquals("#{action.test}", event.fromAction());
		assertEquals("run", event.outcome());
	}
}
