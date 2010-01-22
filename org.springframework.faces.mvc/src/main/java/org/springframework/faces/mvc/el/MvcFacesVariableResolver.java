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

import javax.el.CompositeELResolver;
import javax.faces.el.VariableResolver;

import org.springframework.faces.expression.ELDelegatingVariableResolver;

/**
 * Custom variabe resolver for resolving properties on MVC faces specific variables with JSF 1.1 or > by delegating to
 * EL resolvers.
 * 
 * @author Phillip Webb
 */
public class MvcFacesVariableResolver extends ELDelegatingVariableResolver {
	// FIXME test
	private static final CompositeELResolver composite = new CompositeELResolver();
	static {
		composite.add(new MvcHandlerELResolver());
		composite.add(new RequestContextELResolver());
		composite.add(new ImplicitMvcFacesElResolver());
		composite.add(new ScopeSearchingElResolver());
	}

	public MvcFacesVariableResolver(VariableResolver nextResolver) {
		super(nextResolver, composite);
	}
}
