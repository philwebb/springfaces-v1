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

import java.util.Arrays;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.springframework.web.bind.support.WebArgumentResolver;

public class FacesWebArgumentResolversTests extends TestCase {

	private void assertMergeResolvers(WebArgumentResolver[] expected, WebArgumentResolver[] r1, WebArgumentResolver[] r2)
			throws Exception {
		WebArgumentResolver[] actual = FacesWebArgumentResolvers.mergeResolvers(r1, r2);
		assertTrue(Arrays.equals(expected, actual));
	}

	public void testMergeResolvers() throws Exception {
		WebArgumentResolver a1 = EasyMock.createMock(WebArgumentResolver.class);
		WebArgumentResolver a2 = EasyMock.createMock(WebArgumentResolver.class);
		WebArgumentResolver a3 = EasyMock.createMock(WebArgumentResolver.class);
		WebArgumentResolver a4 = EasyMock.createMock(WebArgumentResolver.class);

		WebArgumentResolver[] r1 = { a1, a2 };
		WebArgumentResolver[] r2 = { a3, a4 };

		assertMergeResolvers(new WebArgumentResolver[] { a1, a2, a3, a4 }, r1, r2);
		assertMergeResolvers(new WebArgumentResolver[] { a3, a4 }, null, r2);
		assertMergeResolvers(new WebArgumentResolver[] { a1, a2 }, r1, null);
		assertMergeResolvers(new WebArgumentResolver[] {}, null, null);
	}

	public void testMergeWithFacesResolvers() throws Exception {
		WebArgumentResolver a1 = EasyMock.createMock(WebArgumentResolver.class);
		WebArgumentResolver a2 = EasyMock.createMock(WebArgumentResolver.class);
		WebArgumentResolver[] resolvers = { a1, a2 };
		WebArgumentResolver[] merged = FacesWebArgumentResolvers.mergeWithFacesResolvers(resolvers);
		assertSame(a1, merged[0]);
		assertSame(a2, merged[1]);
		assertTrue(merged[2] instanceof FacesWebArgumentResolver);
	}
}
