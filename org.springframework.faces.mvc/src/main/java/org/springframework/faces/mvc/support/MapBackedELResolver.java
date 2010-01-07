package org.springframework.faces.mvc.support;

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
	private Object handle(ELContext elContext, Object base, Object property, ScopeOperation operation) {
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
	private static interface ScopeOperation {
		public Object execute(Map scope, String attributeName);
	}
}