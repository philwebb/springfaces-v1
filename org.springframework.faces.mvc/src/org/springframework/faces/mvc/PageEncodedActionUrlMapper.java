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

import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

/**
 * Implementation of {@link ActionUrlMapper} that stores the view name as a hidden form input inside the rendered HTML.
 * 
 * @author Phillip Webb
 */
public class PageEncodedActionUrlMapper implements ActionUrlMapper {

	private static final String ENCODED_FIELD_NAME = "org.springframework.faces.mvc.view";

	private static interface HTML {
		public static final String INPUT_ELEM = "input";
		public static final String TYPE_ATTR = "type";
		public static final String NAME_ATTR = "name";
		public static final String VALUE_ATTR = "value";
	}

	private String buildActionUrl(FacesContext facesContext) {
		return facesContext.getExternalContext().getRequestContextPath()
				+ facesContext.getExternalContext().getRequestServletPath()
				+ facesContext.getExternalContext().getRequestPathInfo();
	}

	public String getActionUlr(FacesContext facesContext, String viewName) {
		return buildActionUrl(facesContext);
	}

	public void writeState(FacesContext facesContext, String viewName) throws IOException {
		ResponseWriter responseWriter = facesContext.getResponseWriter();
		responseWriter.startElement(HTML.INPUT_ELEM, null);
		responseWriter.writeAttribute(HTML.TYPE_ATTR, "hidden", null);
		responseWriter.writeAttribute(HTML.NAME_ATTR, ENCODED_FIELD_NAME, null);
		responseWriter.writeAttribute(HTML.VALUE_ATTR, viewName, null);
		responseWriter.endElement(HTML.INPUT_ELEM);
	}

	public String getViewNameForRestore(FacesContext context) {
		return context.getExternalContext().getRequestParameterMap().get(ENCODED_FIELD_NAME);
	}

}
