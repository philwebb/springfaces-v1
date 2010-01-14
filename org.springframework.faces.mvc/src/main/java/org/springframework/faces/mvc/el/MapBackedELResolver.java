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
import java.util.Map;

import javax.el.ELContext;
import javax.el.ELResolver;

/**
 * Abstract base class for an {@link ELResolver} that will resolve elements from a map.
 * 
 * @author Phillip Webb
 */
public abstract class MapBackedELResolver extends ELResolver {
	// FIXME delete
	protected abstract Map getMap();

	/**
	 * Template method used to handle searching for the property and invoking some operation on the scope.
	 * 
	 * @param elContext
	 * @param base
	 * @param property
	 * @param operation Callback interface used to execute the operation
	 * 
	 * @return Result of the operation
	 */
	protected Object handle(ELContext elContext, Object base, Object property, ScopeOperation operation) {
		Map map = getMap();
		if (base != null || map == null) {
			return null;
		}
		String attributeName = property.toString();
		if (map.containsKey(attributeName)) {
			elContext.setPropertyResolved(true);
			return operation.execute(map, attributeName);
		}
		return null;
	}

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
		return (Class) handle(elContext, base, property, new ScopeOperation() {
			public Object execute(Map scope, String attributeName) {
				return scope.get(attributeName).getClass();
			}
		});
	}

	public Object getValue(ELContext elContext, Object base, Object property) {
		return handle(elContext, base, property, new ScopeOperation() {
			public Object execute(Map scope, String attributeName) {
				return scope.get(attributeName);
			}
		});
	}

	public boolean isReadOnly(ELContext elContext, Object base, Object property) {
		handle(elContext, base, property, new ScopeOperation() {
			public Object execute(Map scope, String attributeName) {
				return null;
			}
		});
		return false;
	}

	public void setValue(ELContext elContext, Object base, Object property, final Object value) {
		handle(elContext, base, property, new ScopeOperation() {
			public Object execute(Map scope, String attributeName) {
				return scope.put(attributeName, value);
			}
		});
	}

	/**
	 * Internal callback interface used to perform a scope operation.
	 */
	protected static interface ScopeOperation {
		public Object execute(Map scope, String attributeName);
	}
}