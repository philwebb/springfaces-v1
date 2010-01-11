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
package org.springframework.faces.mvc.execution;

import java.io.StringWriter;

import org.apache.shale.test.base.AbstractJsfTestCase;
import org.apache.shale.test.mock.MockResponseWriter;
import org.springframework.faces.mvc.execution.PageEncodedActionUrlMapper;

public class PageEncodedActionUrlMapperTests extends AbstractJsfTestCase {

	private static final String EXPECTED_INPUT_NAME = "org.springframework.faces.mvc.view";

	private PageEncodedActionUrlMapper actionUrlMapper = new PageEncodedActionUrlMapper();

	public PageEncodedActionUrlMapperTests(String name) {
		super(name);
	}

	public void testGetActionUrl() throws Exception {
		request.setPathElements("/context", "/servlet", "/some/path", "test=test");
		String actionUrl = actionUrlMapper.getActionUlr(facesContext, "ignored");
		assertEquals("/context/servlet/some/path", actionUrl);
	}

	public void testWriteState() throws Exception {
		StringWriter writer = new StringWriter();
		facesContext.setResponseWriter(new MockResponseWriter(writer, "test/html", null));
		actionUrlMapper.writeState(facesContext, "/some/viewname");
		facesContext.getResponseWriter().flush();
		assertEquals("<input type=\"hidden\" name=\"" + EXPECTED_INPUT_NAME + "\" value=\"/some/viewname\"/>", writer
				.toString());
	}

	public void testname() throws Exception {
		facesContext.getExternalContext().getRequestParameterMap().put(EXPECTED_INPUT_NAME, "some/viewname");
		String viewName = actionUrlMapper.getViewNameForRestore(facesContext);
		assertEquals("some/viewname", viewName);
	}
}
