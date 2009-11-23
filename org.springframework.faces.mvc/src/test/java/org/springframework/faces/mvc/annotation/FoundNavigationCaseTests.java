package org.springframework.faces.mvc.annotation;

import java.lang.reflect.Method;

import junit.framework.TestCase;

import org.apache.shale.test.mock.MockFacesContext;
import org.easymock.EasyMock;
import org.springframework.faces.mvc.NavigationRequestEvent;
import org.springframework.faces.mvc.annotation.FoundNavigationCase.FoundNavigationCaseType;
import org.springframework.faces.mvc.annotation.sample.SampleController;
import org.springframework.faces.mvc.bind.annotation.NavigationCase;

public class FoundNavigationCaseTests extends TestCase {

	private void doTest(Object owner, FoundNavigationCaseType type) throws Exception {
		NavigationCase navigationCase = EasyMock.createMock(NavigationCase.class);
		FoundNavigationCase fnc = new FoundNavigationCase(navigationCase, owner);
		assertEquals(type, fnc.getType());
		assertSame(owner, fnc.getOwner());
		assertSame(navigationCase, fnc.getNavigationCase());
	}

	public void testPackage() throws Exception {
		doTest(SampleController.class.getPackage(), FoundNavigationCaseType.PACKAGE);
	}

	public void testClass() throws Exception {
		doTest(SampleController.class, FoundNavigationCaseType.CLASS);
	}

	public void testMethod() throws Exception {
		doTest(SampleController.class.getMethod("withRules", new Class<?>[] {}), FoundNavigationCaseType.METHOD);
	}

	public void testIllegal() throws Exception {
		try {
			doTest("string", null);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals("owner must be a Method, Class or Package", e.getMessage());
		}
	}

	public void testOutcomeMethodCall() throws Exception {
		Method[] methods = SampleController.class.getMethods();
		Method method = null;
		for (Method searchMethod : methods) {
			if ("methodCall".equals(searchMethod.getName())) {
				method = searchMethod;
			}
		}
		NavigationCase navigationCase = method.getAnnotation(NavigationCase.class);
		FoundNavigationCase fnc = new FoundNavigationCase(navigationCase, method);
		SampleController target = new SampleController();
		assertFalse(target.isMethodCalled());
		Object outcome = fnc.getOutcome(new NavigationRequestEvent(new MockFacesContext(), "#{action.test}",
				"methodcall"), target, null);
		assertTrue(target.isMethodCalled());
		assertEquals("someview", outcome);
	}

	public void testOutcomeWithTo() throws Exception {
		Method method = SampleController.class.getMethod("methodCallWithTo", new Class<?>[] {});
		NavigationCase navigationCase = method.getAnnotation(NavigationCase.class);
		FoundNavigationCase fnc = new FoundNavigationCase(navigationCase, method);
		SampleController target = new SampleController();
		Object outcome = fnc.getOutcome(new NavigationRequestEvent(new MockFacesContext(), "#{action.test}",
				"methodcallwithto"), target, null);
		assertEquals("test", outcome);
	}

	public void testOutcomeMethodCallWithRequestMapping() throws Exception {
		Method method = SampleController.class.getMethod("methodCallWithRequestMapping", new Class<?>[] {});
		NavigationCase navigationCase = method.getAnnotation(NavigationCase.class);
		FoundNavigationCase fnc = new FoundNavigationCase(navigationCase, method);
		SampleController target = new SampleController();
		try {
			fnc.getOutcome(new NavigationRequestEvent(new MockFacesContext(), null, "methodcallwithrequestmapping"),
					target, null);
			fail();
		} catch (IllegalStateException e) {
			assertEquals(
					"Unable to call method methodCallWithRequestMapping from class class org.springframework.faces.mvc.annotation.sample.SampleController "
							+ "in order to resolve @NavigationCase for JSF Navigation Request Event (fromAction=\"null\", outcome=\"methodcallwithrequestmapping\", exception=\"null\") as "
							+ "method also includes @RequestMapping annotation", e.getMessage());
		}
	}
}
