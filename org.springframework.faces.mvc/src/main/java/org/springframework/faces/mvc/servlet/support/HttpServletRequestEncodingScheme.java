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

import org.springframework.web.util.WebUtils;

/**
 * Delegate class that can be used to support user defined character encoding schemes, falling back to the character
 * encoding of the HTTP request when no explicit scheme is defined.
 * 
 * @author Keith Donald
 * @author Jeremy Grelle
 * @author Phillip Webb
 */
public class HttpServletRequestEncodingScheme {

	private String encodingScheme;

	/**
	 * Set the character encoding scheme for URLs. Default is the request's encoding scheme (which is ISO-8859-1 if not
	 * specified otherwise).
	 * @param encodingScheme The encoding scheme
	 */
	public void setEncodingScheme(String encodingScheme) {
		this.encodingScheme = encodingScheme;
	}

	/**
	 * @return The encoding scheme as specified by the user
	 */
	public String getEncodingScheme() {
		return encodingScheme;
	}

	/**
	 * Get the encoding scheme for the specified HTTP request.
	 * @param request The HTTP request
	 * @return The encoding scheme as set from {@link #setEncodingScheme(String)} falling back to the request encoding
	 * scheme
	 */
	public String getEncodingScheme(HttpServletRequest request) {
		if (encodingScheme != null) {
			return encodingScheme;
		} else {
			String encodingScheme = request.getCharacterEncoding();
			if (encodingScheme == null) {
				encodingScheme = WebUtils.DEFAULT_CHARACTER_ENCODING;
			}
			return encodingScheme;
		}
	}
}
