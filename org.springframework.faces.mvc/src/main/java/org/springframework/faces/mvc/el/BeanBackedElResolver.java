package org.springframework.faces.mvc.el;

import java.util.HashMap;
import java.util.Map;

import javax.el.BeanELResolver;
import javax.el.ELContext;

import org.springframework.binding.expression.el.DefaultELContext;

//FIXME check all DCs here down

public abstract class BeanBackedElResolver extends AbstractELResolver {

	private static final BeanELResolver elPropertyResolver = new BeanELResolver();

	private Map properties = new HashMap();

	protected abstract Object getBean();

	protected void map(String elProperty, String beanProperty) {
		properties.put(elProperty, beanProperty);
	}

	protected boolean isAvailable() {
		return getBean() != null;
	}

	protected boolean handles(String property) {
		return properties.containsKey(property);
	}

	protected Object get(String property) {
		String beanProperty = (String) properties.get(property);
		ELContext elContext = new DefaultELContext(elPropertyResolver, null, null);
		return elContext.getELResolver().getValue(elContext, getBean(), property);
	}
}
