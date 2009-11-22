package org.springframework.faces.mvc.support;

import javax.el.CompositeELResolver;
import javax.faces.el.VariableResolver;

import org.springframework.faces.expression.ELDelegatingVariableResolver;

//FIXME doccomment
//FIXME test
public class MvcHandlerVariableResolver extends ELDelegatingVariableResolver {

	private static final CompositeELResolver composite = new CompositeELResolver();
	static {
		composite.add(new MvcHandlerELResolver());
	}

	public MvcHandlerVariableResolver(VariableResolver nextResolver) {
		super(nextResolver, composite);
	}
}
