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

import javax.servlet.ServletContext;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

public class SimpleFacesViewIdResolverTest extends TestCase {

	private void doTest(SimpleFacesViewIdResolver resolver, String viewName, String expectedViewId) {
		String resolvedViewId = resolver.resolveViewId(viewName);
		assertEquals(expectedViewId, resolvedViewId);
		String resolvedViewName = resolver.resolveViewName(expectedViewId);
		assertEquals(viewName, resolvedViewName);
	}

	public void testEmptyPrefix() throws Exception {
		SimpleFacesViewIdResolver resolver = new SimpleFacesViewIdResolver();
		resolver.setSuffix(".jspx");
		resolver.setPrefix("");
		doTest(resolver, "/sample/page", "/sample/page.jspx");
	}

	public void testNullPrefix() throws Exception {
		SimpleFacesViewIdResolver resolver = new SimpleFacesViewIdResolver();
		resolver.setSuffix(".jspx");
		resolver.setPrefix(null);
		doTest(resolver, "/sample/page", "/sample/page.jspx");
	}

	public void testEmptySuffix() throws Exception {
		SimpleFacesViewIdResolver resolver = new SimpleFacesViewIdResolver();
		resolver.setSuffix("");
		resolver.setPrefix("/WEB-INF/test");
		doTest(resolver, "/sample/page", "/WEB-INF/test/sample/page");
	}

	public void testDefinedPrefixAndSuffix() throws Exception {
		SimpleFacesViewIdResolver resolver = new SimpleFacesViewIdResolver();
		resolver.setPrefix("/WEB-INF/test/");
		resolver.setSuffix(".jspx");
		doTest(resolver, "sample/page", "/WEB-INF/test/sample/page.jspx");
	}

	public void testSuffixFromWebXml() throws Exception {
		SimpleFacesViewIdResolver resolver = new SimpleFacesViewIdResolver();
		WebApplicationContext applicationContext = EasyMock.createMock(WebApplicationContext.class);
		ServletContext servletContext = EasyMock.createMock(ServletContext.class);
		EasyMock.expect(applicationContext.getServletContext()).andReturn(servletContext);
		EasyMock.expect(servletContext.getInitParameter("javax.faces.DEFAULT_SUFFIX")).andReturn(".jspx");
		EasyMock.replay(new Object[] { applicationContext, servletContext });
		resolver.setApplicationContext(applicationContext);
		resolver.afterPropertiesSet();
		resolver.setPrefix("/WEB-INF/test/");
		doTest(resolver, "sample/page", "/WEB-INF/test/sample/page.jspx");
	}

	public void testDefaultSuffixNonWebContext() throws Exception {
		SimpleFacesViewIdResolver resolver = new SimpleFacesViewIdResolver();
		ApplicationContext applicationContext = EasyMock.createMock(ApplicationContext.class);
		EasyMock.replay(new Object[] { applicationContext });
		resolver.setApplicationContext(applicationContext);
		resolver.afterPropertiesSet();
		resolver.setPrefix("/WEB-INF/test/");
		doTest(resolver, "sample/page", "/WEB-INF/test/sample/page.xhtml");

	}

	public void testDefaultPrefix() throws Exception {
		SimpleFacesViewIdResolver resolver = new SimpleFacesViewIdResolver();
		resolver.setSuffix(".jspx");
		doTest(resolver, "sample/page", "/WEB-INF/pages/sample/page.jspx");
	}
}
