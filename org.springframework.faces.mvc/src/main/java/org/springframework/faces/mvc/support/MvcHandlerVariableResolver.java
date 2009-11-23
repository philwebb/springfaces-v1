package org.springframework.faces.mvc.support;

import javax.el.CompositeELResolver;
import javax.faces.el.VariableResolver;

import org.springframework.faces.expression.ELDelegatingVariableResolver;
import org.springframework.faces.mvc.FacesHandler;

/**
 * JSF {@link VariableResolver} that delegates to a {@link FacesHandler#resolveVariable(String)} when processing a MVC
 * request.
 * 
 * @author Phillip Webb
 */
public class MvcHandlerVariableResolver extends ELDelegatingVariableResolver {

	private static final CompositeELResolver composite = new CompositeELResolver();
	static {
		composite.add(new MvcHandlerELResolver());
	}

	public MvcHandlerVariableResolver(VariableResolver nextResolver) {
		super(nextResolver, composite);
	}
}
