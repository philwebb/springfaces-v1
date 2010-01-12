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

import java.lang.reflect.Method;

import javax.faces.application.Application;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.FacesEvent;

import org.apache.shale.test.base.AbstractJsfTestCase;
import org.springframework.core.MethodParameter;
import org.springframework.faces.mvc.annotation.support.FacesWebArgumentResolver;
import org.springframework.web.bind.support.WebArgumentResolver;

public class FacesWebArgumentResolverTests extends AbstractJsfTestCase {

	private FacesWebArgumentResolver resolver = new FacesWebArgumentResolver();
	private Method method = getSampleMethod();

	public FacesWebArgumentResolverTests(String name) {
		super(name);
	}

	public void testFacesContext() throws Exception {
		MethodParameter methodParameter = new MethodParameter(method, 0);
		assertSame(facesContext, resolver.resolveArgument(methodParameter, null));
	}

	public void testApplication() throws Exception {
		MethodParameter methodParameter = new MethodParameter(method, 1);
		assertSame(facesContext.getApplication(), resolver.resolveArgument(methodParameter, null));
	}

	public void testExternalContext() throws Exception {
		MethodParameter methodParameter = new MethodParameter(method, 2);
		assertSame(facesContext.getExternalContext(), resolver.resolveArgument(methodParameter, null));
	}

	public void testUnknown() throws Exception {
		MethodParameter methodParameter = new MethodParameter(method, 3);
		assertSame(WebArgumentResolver.UNRESOLVED, resolver.resolveArgument(methodParameter, null));
	}

	private Method getSampleMethod() {
		Method[] methods = getClass().getMethods();
		for (Method method : methods) {
			if ("sample".equals(method.getName())) {
				return method;
			}
		}
		fail("Cannot find sample method");
		return null;
	}

	public void sample(FacesContext facesContext, Application application, ExternalContext externalContext,
			FacesEvent facesEvent) {
	}
}
