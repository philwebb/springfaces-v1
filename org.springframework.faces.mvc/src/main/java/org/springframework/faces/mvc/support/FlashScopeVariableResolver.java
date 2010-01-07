package org.springframework.faces.mvc.support;

import javax.el.CompositeELResolver;
import javax.faces.el.VariableResolver;

import org.springframework.faces.expression.ELDelegatingVariableResolver;

//FIXME DC
public class FlashScopeVariableResolver extends ELDelegatingVariableResolver {

	private static final CompositeELResolver composite = new CompositeELResolver();
	static {
		composite.add(new FlashScopeELResolver());
	}

	public FlashScopeVariableResolver(VariableResolver nextResolver) {
		super(nextResolver, composite);
	}
}
