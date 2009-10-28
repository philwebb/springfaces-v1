package org.springframework.faces.mvc.annotation;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.springframework.faces.bind.annotation.NavigationCase;
import org.springframework.faces.mvc.annotation.FoundNavigationCase.FoundNavigationCaseType;
import org.springframework.faces.mvc.annotation.sample.SampleController;

public class FoundNavigationCaseTest extends TestCase {

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
}
