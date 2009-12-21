package org.springframework.faces.mvc.annotation;

import junit.framework.TestCase;

public class NavigationOutcomeExpressionElResolverTests extends TestCase {

	// FIXME
	public void testname() throws Exception {
		MockNavigationOutcomeExpressionElResolver resolver = new MockNavigationOutcomeExpressionElResolver();
		Object resolved = resolver.resolveNavigationOutcome("/example/#{with encoded}/elements?name=#{te?st}");
		System.out.println(resolved);
	}

	private static class MockNavigationOutcomeExpressionElResolver extends NavigationOutcomeExpressionElResolver {
		protected Object resolve(String expression) {
			return expression.substring(2, expression.length() - 1);
		}
	}

}
