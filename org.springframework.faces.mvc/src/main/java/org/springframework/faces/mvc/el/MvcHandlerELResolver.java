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
package org.springframework.faces.mvc.el;

import java.util.Iterator;

import javax.el.ELContext;
import javax.el.ELResolver;

import org.springframework.faces.mvc.FacesHandler;
import org.springframework.faces.mvc.execution.MvcFacesRequestContext;
import org.springframework.faces.mvc.execution.MvcFacesRequestContextHolder;

/**
 * {@link ELResolver} that delegates to a {@link FacesHandler#resolveVariable(String)} when processing a MVC request.
 * 
 * @author Phillip Webb
 */
public class MvcHandlerELResolver extends ELResolver {

	public Class getCommonPropertyType(ELContext elContext, Object base) {
		if (base == null) {
			return Object.class;
		}
		return null;
	}

	public Iterator getFeatureDescriptors(ELContext elContext, Object base) {
		return null;
	}

	public Class getType(ELContext elContext, Object base, Object property) {
		Object value = getValue(elContext, base, property);
		return (value == null ? null : value.getClass());
	}

	public Object getValue(ELContext elContext, Object base, Object property) {
		if (base != null || MvcFacesRequestContextHolder.getRequestContext() == null) {
			return null;
		}
		String propertyName = property.toString();
		MvcFacesRequestContext mvcFacesRequestContext = MvcFacesRequestContextHolder.getRequestContext();
		Object value = mvcFacesRequestContext.getFacesHandler().resolveVariable(propertyName);
		if (value != null) {
			elContext.setPropertyResolved(true);
			return value;
		}
		return null;
	}

	public boolean isReadOnly(ELContext elContext, Object base, Object property) {
		return true;
	}

	public void setValue(ELContext elContext, Object base, Object property, final Object value) {
	}
}
