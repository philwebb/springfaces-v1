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
package org.springframework.faces.mvc.servlet.support;

import javax.servlet.http.HttpServletRequest;

import junit.framework.TestCase;

import org.easymock.EasyMock;

public class HttpServletRequestEncodingSchemeTests extends TestCase {

	private HttpServletRequestEncodingScheme encodingScheme;

	protected void setUp() throws Exception {
		encodingScheme = new HttpServletRequestEncodingScheme();
	}

	public void testNoneSpecifiedFromRequest() throws Exception {
		HttpServletRequest request = (HttpServletRequest) EasyMock.createMock(HttpServletRequest.class);
		EasyMock.expect(request.getCharacterEncoding()).andReturn("CUSTOM");
		EasyMock.replay(new Object[] { request });
		assertEquals("CUSTOM", encodingScheme.getEncodingScheme(request));
	}

	public void testNoneSpecifiedDefault() throws Exception {
		HttpServletRequest request = (HttpServletRequest) EasyMock.createMock(HttpServletRequest.class);
		EasyMock.expect(request.getCharacterEncoding()).andReturn(null);
		EasyMock.replay(new Object[] { request });
		assertEquals("ISO-8859-1", encodingScheme.getEncodingScheme(request));
	}

	public void testSpecificScheme() throws Exception {
		HttpServletRequest request = (HttpServletRequest) EasyMock.createMock(HttpServletRequest.class);
		EasyMock.expect(request.getCharacterEncoding()).andReturn("CUSTOM");
		EasyMock.replay(new Object[] { request });
		encodingScheme.setEncodingScheme("SPECIFIC");
		assertEquals("SPECIFIC", encodingScheme.getEncodingScheme(request));
	}
}
