package org.springframework.faces.mvc.annotation;

import java.lang.reflect.Method;

import junit.framework.TestCase;

import org.springframework.faces.mvc.annotation.sample.SampleController;

public class NavigationCaseAnnotationLocatorTest extends TestCase {

	public void testLocate() throws Exception {
		Method method = SampleController.class.getMethod("withRules", new Class<?>[] {});
		Method[] methods = new Method[] { method };
		NavigationCaseAnnotationLocator locator = new NavigationCaseAnnotationLocator();
		assertEquals("mto1", locator.findNavigationCase(methods, "mon1", "mon1").getNavigationCase().to());
		assertEquals("mto2", locator.findNavigationCase(methods, "mon2", "mon2").getNavigationCase().to());
		assertEquals("cto1", locator.findNavigationCase(methods, "con1", "con1").getNavigationCase().to());
		assertEquals("cto2", locator.findNavigationCase(methods, "con2", "con2").getNavigationCase().to());
		assertEquals("pto1", locator.findNavigationCase(methods, "pon1", "pon1").getNavigationCase().to());
		assertEquals("pto2", locator.findNavigationCase(methods, "pon2", "pon2").getNavigationCase().to());
		assertNull(locator.findNavigationCase(methods, "missing", ""));
	}

	public void testLocateWithAction() throws Exception {
		Method method = SampleController.class.getMethod("withRules", new Class<?>[] {});
		Method[] methods = new Method[] { method };
		NavigationCaseAnnotationLocator locator = new NavigationCaseAnnotationLocator();
		assertEquals("mato1", locator.findNavigationCase(methods, "#{bean.action1}", "maon1").getNavigationCase().to());
		assertEquals("mato2", locator.findNavigationCase(methods, "#{bean.action2}", "maon1").getNavigationCase().to());
		assertNull(locator.findNavigationCase(methods, "maon1", "#{bean.missing}"));
		assertEquals("cato1", locator.findNavigationCase(methods, "#{bean.action1}", "caon1").getNavigationCase().to());
		assertEquals("cato2", locator.findNavigationCase(methods, "#{bean.action2}", "caon1").getNavigationCase().to());
		assertNull(locator.findNavigationCase(methods, "caon1", "#{bean.missing}"));
		assertEquals("pato1", locator.findNavigationCase(methods, "#{bean.action1}", "paon1").getNavigationCase().to());
		assertEquals("pato2", locator.findNavigationCase(methods, "#{bean.action2}", "paon1").getNavigationCase().to());
		assertNull(locator.findNavigationCase(methods, "paon1", "#{bean.missing}"));
	}

	public void testLocateWithoutMethods() throws Exception {
		NavigationCaseAnnotationLocator locator = new NavigationCaseAnnotationLocator();
		assertNull(locator.findNavigationCase(null, "mon1", "mon1"));
		assertNull(locator.findNavigationCase(new Method[] {}, "mon1", "mon1"));
	}

	public void testLocateNoNavigationRules() throws Exception {
		Method method = SampleController.class.getMethod("noRules", new Class<?>[] {});
		Method[] methods = new Method[] { method };
		NavigationCaseAnnotationLocator locator = new NavigationCaseAnnotationLocator();
		assertEquals("mto2", locator.findNavigationCase(methods, "mon2", "mon2").getNavigationCase().to());
		assertEquals("cto2", locator.findNavigationCase(methods, "con2", "con2").getNavigationCase().to());
	}

	public void testLocateWithDefault() throws Exception {
		Method method = SampleController.class.getMethod("defaultOn", new Class<?>[] {});
		Method[] methods = new Method[] { method };
		NavigationCaseAnnotationLocator locator = new NavigationCaseAnnotationLocator();
		assertEquals("dto1", locator.findNavigationCase(methods, "mon1", "mon1").getNavigationCase().to());
		assertEquals("dto1", locator.findNavigationCase(methods, "con1", "con1").getNavigationCase().to());
		assertEquals("dto1", locator.findNavigationCase(methods, "pon1", "pon1").getNavigationCase().to());
	}

}
