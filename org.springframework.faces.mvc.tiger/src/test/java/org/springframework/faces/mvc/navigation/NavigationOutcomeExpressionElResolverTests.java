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
package org.springframework.faces.mvc.navigation;

import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.VariableResolver;

import junit.framework.TestCase;

import org.apache.shale.test.mock.MockApplication12;
import org.apache.shale.test.mock.MockFacesContext12;
import org.easymock.EasyMock;
import org.springframework.faces.mvc.navigation.NavigationOutcomeExpressionElResolver.Position;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.util.WebUtils;

public class NavigationOutcomeExpressionElResolverTests extends TestCase {

	private static final Map<String, Object> VARIABLES;
	static {
		VARIABLES = new HashMap<String, Object>();
		VARIABLES.put("i1", new Integer(123));
		VARIABLES.put("i2", new Integer(456));
		VARIABLES.put("e1", new ExpandableObject("one", "t w&o"));
	}

	private NavigationOutcomeExpressionElResolver resolver;
	private NavigationOutcomeExpressionContext context;

	protected void setUp() throws Exception {
		resolver = new MockNavigationOutcomeExpressionElResolver();
		context = EasyMock.createMock(NavigationOutcomeExpressionContext.class);
	}

	public void testNull() throws Exception {
		assertEquals(null, resolver.resolveNavigationOutcome(context, null));
	}

	public void testNonString() throws Exception {
		assertEquals(new NavigationLocation(new Long(123)), resolver.resolveNavigationOutcome(context,
				new NavigationLocation(new Long(123))));
	}

	public void testNoExpression() throws Exception {
		assertEquals(new NavigationLocation("contextRelative:/test"), resolver.resolveNavigationOutcome(context,
				new NavigationLocation("contextRelative:/test")));
	}

	public void testSimpleQuery() throws Exception {
		WebDataBinder dataBinder = new WebDataBinder(null);
		EasyMock.expect(context.getEncoding()).andStubReturn(WebUtils.DEFAULT_CHARACTER_ENCODING);
		EasyMock.expect(context.createDataBinder(null, null, null)).andReturn(dataBinder);
		EasyMock.expect(context.createDataBinder("value", null, null)).andReturn(dataBinder);
		EasyMock.replay(context);
		assertEquals(new NavigationLocation("contextRelative:/test/123/x?value=456"), resolver
				.resolveNavigationOutcome(context, new NavigationLocation("contextRelative:/test/#{i1}/x?value=#{i2}")));
	}

	public void testNullResovle() throws Exception {
		try {
			resolver.resolveNavigationOutcome(context, new NavigationLocation("contextRelative:/test/#{xxx}"));
		} catch (IllegalStateException e) {
			assertEquals("Unable resolve and convert expression '#{xxx}' "
					+ "for outcome 'contextRelative:/test/#{xxx}'", e.getMessage());
		}
	}

	public void testExpandQuery() throws Exception {
		Object e1 = VARIABLES.get("e1");
		WebDataBinder dataBinder = new WebDataBinder(e1);
		EasyMock.expect(context.getEncoding()).andStubReturn(WebUtils.DEFAULT_CHARACTER_ENCODING);
		EasyMock.expect(context.createDataBinder(null, e1, null)).andReturn(dataBinder);
		EasyMock.replay(context);
		assertEquals(new NavigationLocation("contextRelative:/test?a=one&b=t+w%26o"), resolver
				.resolveNavigationOutcome(context, new NavigationLocation("contextRelative:/test?#{e1}")));
	}

	public void testRealResolve() throws Exception {
		MockFacesContext12 facesContext = new MockFacesContext12();
		MockFacesContext12.setCurrentInstance(facesContext);
		MockApplication12 application = new MockApplication12();
		application.setVariableResolver(new VariableResolver() {
			public Object resolveVariable(FacesContext context, String name) throws EvaluationException {
				assertEquals("expression", name);
				return "test";
			}
		});
		facesContext.setApplication(application);
		resolver = new NavigationOutcomeExpressionElResolver();
		Object resolved = resolver.resolve(context, Position.QUERY, "att", "#{expression}");
		assertEquals("test", resolved);
	}

	private static class MockNavigationOutcomeExpressionElResolver extends NavigationOutcomeExpressionElResolver {
		protected Object resolve(NavigationOutcomeExpressionContext context, Position position, String attribute,
				String expression) throws Exception {
			assertTrue(expression.startsWith("#{"));
			assertTrue(expression.endsWith("}"));
			String key = expression.substring(2, expression.length() - 1);
			return VARIABLES.get(key);
		}
	}

	private static class ExpandableObject {
		private String a;
		private String b;

		public ExpandableObject(String a, String b) {
			this.a = a;
			this.b = b;
		}

		public String getA() {
			return a;
		}

		public void setA(String a) {
			this.a = a;
		}

		public String getB() {
			return b;
		}

		public void setB(String b) {
			this.b = b;
		}
	}

}
