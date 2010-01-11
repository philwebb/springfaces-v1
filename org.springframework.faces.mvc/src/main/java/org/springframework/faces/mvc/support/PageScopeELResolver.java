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
package org.springframework.faces.mvc.support;

import java.util.Map;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.faces.context.FacesContext;

import org.springframework.faces.mvc.PageScope;

/**
 * {@link ELResolver} that will resolve elements in page scope.
 * 
 * @see PageScope
 * @see MvcFacesStateHolderComponent
 * @see PageScopeVariableResolver
 * 
 * @author Phillip Webb
 */
public class PageScopeELResolver extends MapBackedELResolver {

	// FIXME rename to viewscope
	// FIXME refactor the EL resolvers to be like SWF

	private static final String PREFIX = "pageScope.";

	protected Object handle(ELContext elContext, Object base, Object property, ScopeOperation operation) {
		return super.handle(elContext, base, stripPrefix(property), operation);
	}

	private Object stripPrefix(Object property) {
		// FIXME should we do this using scope searching? perhaps with pageScope/requestScope exposed on context
		if ((property != null) && (property instanceof String) && (property.toString().startsWith(PREFIX))) {
			return property.toString().substring(PREFIX.length());
		}
		return property;
	}

	protected Map getMap() {
		MvcFacesStateHolderComponent stateHolder = MvcFacesStateHolderComponent.locate(FacesContext
				.getCurrentInstance(), false);
		return stateHolder == null ? null : stateHolder.getViewScope().asMap();
	}
}
