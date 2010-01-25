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
package org.springframework.faces.mvc.annotation.support;

import org.springframework.web.bind.support.WebArgumentResolver;

/**
 * Utility class that can be used to manipulate MVC Faces {@link WebArgumentResolver}s.
 * 
 * @author Phillip Webb
 */
public class FacesWebArgumentResolvers {

	private static final WebArgumentResolver[] ARGUMENT_RESOLVERS = new WebArgumentResolver[] { new FacesWebArgumentResolver() };

	/**
	 * Merge the specified {@link WebArgumentResolver} arrays into a single result.
	 * @param r1 array of {@link WebArgumentResolver} items (can be null)
	 * @param r2 array of {@link WebArgumentResolver} items (can be null)
	 * @return An array containing all items from <tt>r1</tt> and <tt>r2</tt>
	 */
	public static WebArgumentResolver[] mergeResolvers(WebArgumentResolver[] r1, WebArgumentResolver[] r2) {
		r1 = (r1 == null ? new WebArgumentResolver[] {} : r1);
		r2 = (r2 == null ? new WebArgumentResolver[] {} : r2);
		WebArgumentResolver[] rtn = new WebArgumentResolver[r1.length + r2.length];
		System.arraycopy(r1, 0, rtn, 0, r1.length);
		System.arraycopy(r2, 0, rtn, r1.length, r2.length);
		return rtn;
	}

	/**
	 * Merge the specified resolves with the standard Faces MVC resolvers.
	 * @param resolvers The resolvers to merge (can be null)
	 * @return An array containing the resolvers merged with the standard Faces MVC resolvers
	 */
	public static WebArgumentResolver[] mergeWithFacesResolvers(WebArgumentResolver[] resolvers) {
		return mergeResolvers(resolvers, ARGUMENT_RESOLVERS);
	}
}
