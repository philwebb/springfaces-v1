package org.springframework.faces.mvc.el;

import java.util.Iterator;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.PropertyNotWritableException;

/**
 * Abstract base class for an {@link ELResolver} that will resolve elements from a map.
 * 
 * @author Phillip Webb
 */
public abstract class AbstractELResolver extends ELResolver {

	protected boolean isAvailable() {
		return true;
	}

	protected boolean contains(String property) {
		return get(property) != null;
	}

	protected abstract Object get(String property);

	protected void set(String property, Object value) {
		throw new PropertyNotWritableException("The property " + property + " is not writable.");
	}

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
	protected Object handle(ELContext elContext, Object base, Object property, ElOperation operation) {
		if (base != null || !isAvailable()) {
			return null;
		}
		String propertyString = property.toString();
		if (contains(propertyString)) {
			elContext.setPropertyResolved(true);
			return operation.execute(propertyString);
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
		return (Class) handle(elContext, base, property, new ElOperation() {
			public Object execute(String property) {
				return get(property).getClass();
			}
		});
	}

	public Object getValue(ELContext elContext, Object base, Object property) {
		return handle(elContext, base, property, new ElOperation() {
			public Object execute(String property) {
				return get(property);
			}
		});
	}

	public boolean isReadOnly(ELContext elContext, Object base, Object property) {
		handle(elContext, base, property, new ElOperation() {
			public Object execute(String property) {
				return null;
			}
		});
		return false;
	}

	public void setValue(ELContext elContext, Object base, Object property, final Object value) {
		handle(elContext, base, property, new ElOperation() {
			public Object execute(String property) {
				set(property, value);
				return null;
			}
		});
	}

	/**
	 * Internal callback interface used to perform a scope operation.
	 */
	protected static interface ElOperation {
		public Object execute(String property);
	}
}