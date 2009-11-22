package org.springframework.faces.mvc.support;

import java.util.Iterator;

import javax.el.ELContext;
import javax.el.ELResolver;

//FIXME doccomment
//FIXME test
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
		if (base != null || MvcFacesRequestContext.getCurrentInstance() == null) {
			return null;
		}
		String propertyName = property.toString();
		MvcFacesRequestContext mvcFacesRequestContext = MvcFacesRequestContext.getCurrentInstance();
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
