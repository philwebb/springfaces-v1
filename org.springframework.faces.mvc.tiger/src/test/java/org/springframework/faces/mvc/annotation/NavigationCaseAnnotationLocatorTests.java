package org.springframework.faces.mvc.annotation;

import java.lang.reflect.Method;

import javax.faces.context.FacesContext;

import junit.framework.TestCase;

import org.apache.shale.test.mock.MockFacesContext;
import org.springframework.faces.mvc.NavigationRequestEvent;
import org.springframework.faces.mvc.annotation.sample.SampleController;

public class NavigationCaseAnnotationLocatorTests extends TestCase {

	private FacesContext facesContext = new MockFacesContext();

	private NavigationRequestEvent event(String fromAction, String outcome) {
		return new NavigationRequestEvent(facesContext, fromAction, outcome);
	}

	private NavigationRequestEvent event(String fromAction, String outcome, Exception exception) {
		NavigationRequestEvent event = new NavigationRequestEvent(this, fromAction, outcome);
		return new NavigationRequestEvent(this, event, exception);
	}

	public void testLocate() throws Exception {
		Method method = SampleController.class.getMethod("withRules", new Class<?>[] {});
		Method[] methods = new Method[] { method };
		NavigationCaseAnnotationLocator locator = new NavigationCaseAnnotationLocator();
		assertEquals("mto1", locator.findNavigationCase(methods, event("mon1", "mon1")).getNavigationCase().to());
		assertEquals("mto2", locator.findNavigationCase(methods, event("mon2", "mon2")).getNavigationCase().to());
		assertEquals("cto1", locator.findNavigationCase(methods, event("con1", "con1")).getNavigationCase().to());
		assertEquals("cto2", locator.findNavigationCase(methods, event("con2", "con2")).getNavigationCase().to());
		assertEquals("pto1", locator.findNavigationCase(methods, event("pon1", "pon1")).getNavigationCase().to());
		assertEquals("pto2", locator.findNavigationCase(methods, event("pon2", "pon2")).getNavigationCase().to());
		assertNull(locator.findNavigationCase(methods, event("missing", "")));
	}

	public void testLocateWithAction() throws Exception {
		Method method = SampleController.class.getMethod("withRules", new Class<?>[] {});
		Method[] methods = new Method[] { method };
		NavigationCaseAnnotationLocator locator = new NavigationCaseAnnotationLocator();
		assertEquals("mato1", locator.findNavigationCase(methods, event("#{bean.action1}", "maon1"))
				.getNavigationCase().to());
		assertEquals("mato2", locator.findNavigationCase(methods, event("#{bean.action2}", "maon1"))
				.getNavigationCase().to());
		assertNull(locator.findNavigationCase(methods, event("maon1", "#{bean.missing}")));
		assertEquals("cato1", locator.findNavigationCase(methods, event("#{bean.action1}", "caon1"))
				.getNavigationCase().to());
		assertEquals("cato2", locator.findNavigationCase(methods, event("#{bean.action2}", "caon1"))
				.getNavigationCase().to());
		assertNull(locator.findNavigationCase(methods, event("caon1", "#{bean.missing}")));
		assertEquals("pato1", locator.findNavigationCase(methods, event("#{bean.action1}", "paon1"))
				.getNavigationCase().to());
		assertEquals("pato2", locator.findNavigationCase(methods, event("#{bean.action2}", "paon1"))
				.getNavigationCase().to());
		assertNull(locator.findNavigationCase(methods, event("paon1", "#{bean.missing}")));
	}

	public void testLocateWithoutMethods() throws Exception {
		NavigationCaseAnnotationLocator locator = new NavigationCaseAnnotationLocator();
		assertNull(locator.findNavigationCase(null, event("mon1", "mon1")));
		assertNull(locator.findNavigationCase(new Method[] {}, event("mon1", "mon1")));
	}

	public void testLocateNoNavigationRules() throws Exception {
		Method method = SampleController.class.getMethod("noRules", new Class<?>[] {});
		Method[] methods = new Method[] { method };
		NavigationCaseAnnotationLocator locator = new NavigationCaseAnnotationLocator();
		assertEquals("mto2", locator.findNavigationCase(methods, event("mon2", "mon2")).getNavigationCase().to());
		assertEquals("cto2", locator.findNavigationCase(methods, event("con2", "con2")).getNavigationCase().to());
	}

	public void testLocateWithDefault() throws Exception {
		Method method = SampleController.class.getMethod("defaultOn", new Class<?>[] {});
		Method[] methods = new Method[] { method };
		NavigationCaseAnnotationLocator locator = new NavigationCaseAnnotationLocator();
		assertEquals("dto1", locator.findNavigationCase(methods, event("mon1", "mon1")).getNavigationCase().to());
		assertEquals("dto1", locator.findNavigationCase(methods, event("con1", "con1")).getNavigationCase().to());
		assertEquals("dto1", locator.findNavigationCase(methods, event("pon1", "pon1")).getNavigationCase().to());
	}

	public void testLocateForException() throws Exception {
		Method method = SampleController.class.getMethod("withRules", new Class<?>[] {});
		Method[] methods = new Method[] { method };
		NavigationCaseAnnotationLocator locator = new NavigationCaseAnnotationLocator();
		assertEquals("ceto1", locator.findNavigationCase(methods, event("ceon1", "ceon1", new IllegalStateException()))
				.getNavigationCase().to());
		assertEquals("ceto1", locator.findNavigationCase(methods,
				event("ceon1", "ceon1", new RuntimeException(new IllegalStateException()))).getNavigationCase().to());
		assertNull(locator.findNavigationCase(methods, event("ceon1", "ceon1", new RuntimeException())));

	}

}
