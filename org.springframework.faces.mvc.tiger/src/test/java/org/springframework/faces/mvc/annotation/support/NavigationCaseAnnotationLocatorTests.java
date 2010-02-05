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
package org.springframework.faces.mvc.annotation.support;

import java.lang.reflect.Method;

import javax.faces.context.FacesContext;

import junit.framework.TestCase;

import org.apache.shale.test.mock.MockFacesContext;
import org.springframework.faces.mvc.annotation.sample.SampleController;
import org.springframework.faces.mvc.navigation.NavigationRequestEvent;
import org.springframework.faces.mvc.navigation.annotation.NavigationCase;
import org.springframework.faces.mvc.stereotype.FacesController;

public class NavigationCaseAnnotationLocatorTests extends TestCase {

	private FacesContext facesContext = new MockFacesContext();
	private NavigationCaseAnnotationLocator locator;
	private Object handler;
	Method[] methods;

	protected void setUp() throws Exception {
		this.locator = new NavigationCaseAnnotationLocator();
		this.handler = new SampleController();
	}

	private void useMethod(String name) throws Exception {
		Method method = SampleController.class.getMethod(name, new Class<?>[] {});
		this.methods = new Method[] { method };
	}

	private NavigationRequestEvent event(String fromAction, String outcome) {
		return new NavigationRequestEvent(facesContext, fromAction, outcome);
	}

	private NavigationRequestEvent event(String fromAction, String outcome, Exception exception) {
		NavigationRequestEvent event = new NavigationRequestEvent(this, fromAction, outcome);
		return new NavigationRequestEvent(this, event, exception);
	}

	private FoundNavigationCase locate(NavigationRequestEvent event) {
		return locator.findNavigationCase(handler, methods, event);
	}

	public void testLocate() throws Exception {
		useMethod("withRules");
		assertEquals("mto1", locate(event("mon1", "mon1")).getNavigationCase().to());
		assertEquals("mto2", locate(event("mon2", "mon2")).getNavigationCase().to());
		assertEquals("cto1", locate(event("con1", "con1")).getNavigationCase().to());
		assertEquals("cto2", locate(event("con2", "con2")).getNavigationCase().to());
		assertEquals("pto1", locate(event("pon1", "pon1")).getNavigationCase().to());
		assertEquals("pto2", locate(event("pon2", "pon2")).getNavigationCase().to());
		assertNull(locate(event("missing", "")));
	}

	public void testLocateWithAction() throws Exception {
		useMethod("withRules");
		assertEquals("mato1", locate(event("#{bean.action1}", "maon1")).getNavigationCase().to());
		assertEquals("mato2", locate(event("#{bean.action2}", "maon1")).getNavigationCase().to());
		assertNull(locate(event("maon1", "#{bean.missing}")));
		assertEquals("cato1", locate(event("#{bean.action1}", "caon1")).getNavigationCase().to());
		assertEquals("cato2", locate(event("#{bean.action2}", "caon1")).getNavigationCase().to());
		assertNull(locate(event("caon1", "#{bean.missing}")));
		assertEquals("pato1", locate(event("#{bean.action1}", "paon1")).getNavigationCase().to());
		assertEquals("pato2", locate(event("#{bean.action2}", "paon1")).getNavigationCase().to());
		assertNull(locate(event("paon1", "#{bean.missing}")));
	}

	public void testLocateWithoutMethods() throws Exception {
		Object h = new Object();
		assertNull(locator.findNavigationCase(h, null, event("mon1", "mon1")));
		assertNull(locator.findNavigationCase(h, new Method[] {}, event("mon1", "mon1")));
	}

	public void testLocateNoNavigationRules() throws Exception {
		useMethod("noRules");
		assertEquals("mto2", locate(event("mon2", "mon2")).getNavigationCase().to());
		assertEquals("cto2", locate(event("con2", "con2")).getNavigationCase().to());
	}

	public void testLocateWithDefault() throws Exception {
		useMethod("defaultOn");
		assertEquals("dto1", locate(event("defaultOn", "defaultOn")).getNavigationCase().to());
	}

	public void testLocateWithCatchAll() throws Exception {
		useMethod("catchAll");
		assertEquals("dto1", locate(event("mon1", "mon1")).getNavigationCase().to());
		assertEquals("dto1", locate(event("con1", "con1")).getNavigationCase().to());
		assertEquals("dto1", locate(event("pon1", "pon1")).getNavigationCase().to());
	}

	public void testLocateForException() throws Exception {
		Method method = SampleController.class.getMethod("withRules", new Class<?>[] {});
		Method[] methods = new Method[] { method };
		assertEquals("ceto1", locator.findNavigationCase(handler, methods,
				event("ceon1", "ceon1", new IllegalStateException())).getNavigationCase().to());
		assertEquals("ceto1", locator.findNavigationCase(handler, methods,
				event("ceon1", "ceon1", new RuntimeException(new IllegalStateException()))).getNavigationCase().to());
		assertNull(locator.findNavigationCase(handler, methods, event("ceon1", "ceon1", new RuntimeException())));
	}

	public void testLocateWithNoAnnotatedMethods() throws Exception {
		handler = new NoAnnotatedMethodsController();
		FoundNavigationCase found = locator.findNavigationCase(handler, methods, event("action", "outcome"));
		assertEquals("nomethods", found.getNavigationCase().to());
	}

	@FacesController
	@NavigationCase(on = "outcome", to = "nomethods")
	public static class NoAnnotatedMethodsController {
		public void someMethod() {
		}
	}
}
