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

import java.util.HashSet;
import java.util.Set;

import javax.faces.application.Application;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebArgumentResolver;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * A {@link WebArgumentResolver} that can be used to resolve common JSF types. This resolver supports the following
 * types:
 * <ul>
 * <li>{@link FacesContext}</li>
 * <li>{@link Application}</li>
 * <li>{@link ExternalContext}</li>
 * </ul>
 * 
 * @see FacesWebArgumentResolvers
 * 
 * @author Phillip Webb
 */
public class FacesWebArgumentResolver implements WebArgumentResolver {

	private static final Set<Class<?>> SUPPORTED;
	static {
		SUPPORTED = new HashSet<Class<?>>();
		SUPPORTED.add(FacesContext.class);
		SUPPORTED.add(Application.class);
		SUPPORTED.add(ExternalContext.class);
	}

	public Object resolveArgument(MethodParameter methodParameter, NativeWebRequest webRequest) throws Exception {
		if (SUPPORTED.contains(methodParameter.getParameterType())) {
			FacesContext facesContext = FacesContext.getCurrentInstance();
			if (facesContext != null) {
				if (FacesContext.class.equals(methodParameter.getParameterType())) {
					return facesContext;
				}
				if (Application.class.equals(methodParameter.getParameterType())) {
					return facesContext.getApplication();
				}
				if (ExternalContext.class.equals(methodParameter.getParameterType())) {
					return facesContext.getExternalContext();
				}
			}
		}
		return UNRESOLVED;
	}
}
