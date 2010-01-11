package org.springframework.faces.mvc.support;

import javax.el.CompositeELResolver;
import javax.faces.el.VariableResolver;

import org.springframework.faces.expression.ELDelegatingVariableResolver;
import org.springframework.faces.mvc.el.ImplicitMvcFacesRequestContextElResolver;
import org.springframework.faces.mvc.el.MvcFacesRequestContextELResolver;
import org.springframework.faces.mvc.el.ScopeSearchingElResolver;

public class MvcFacesVariableResolver extends ELDelegatingVariableResolver {

	private static final CompositeELResolver composite = new CompositeELResolver();
	static {
		composite.add(new MvcHandlerELResolver());
		composite.add(new MvcFacesRequestContextELResolver());
		composite.add(new ImplicitMvcFacesRequestContextElResolver());
		composite.add(new ScopeSearchingElResolver());
	}

	public MvcFacesVariableResolver(VariableResolver nextResolver) {
		super(nextResolver, composite);
	}
}
