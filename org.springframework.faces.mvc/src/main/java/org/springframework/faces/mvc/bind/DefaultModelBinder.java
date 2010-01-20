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
package org.springframework.faces.mvc.bind;

import java.util.Iterator;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.faces.mvc.execution.MvcFacesRequestContext;
import org.springframework.faces.mvc.execution.MvcFacesRequestContextHolder;
import org.springframework.faces.mvc.execution.ScopeType;
import org.springframework.util.Assert;

/**
 * Default implementation of {@link ModelBinder} that exposes MVC Model elements to JSF using MVC {@link ScopeType}s or
 * registered spring {@link Scope}s. By default this binder is configured with {@link ImplicitModelScopeProvider}
 * support, falling back to <tt>viewScope</tt> when an implicit scope name is not found.
 * 
 * @author Phillip Webb
 */
public class DefaultModelBinder implements ModelBinder, BeanFactoryAware, InitializingBean {

	private ConfigurableBeanFactory beanFactory;
	private ModelScopeProvider modelScopeProvider;

	public DefaultModelBinder() {
		this.modelScopeProvider = new ImplicitModelScopeProvider(ScopeType.VIEW);
	}

	public void bindModel(Map model) {
		MvcFacesRequestContext context = MvcFacesRequestContextHolder.getRequestContext();
		Assert.notNull(context, "MvcFacesRequestContext not found");
		for (Iterator iterator = model.entrySet().iterator(); iterator.hasNext();) {
			final Map.Entry modelEntry = (Map.Entry) iterator.next();
			final Object modelValue = modelEntry.getValue();
			ScopedModelAttribute scopedModelAttribute = new ScopedModelAttribute((String) modelEntry.getKey());
			scopedModelAttribute = modelScopeProvider.getModelScope(scopedModelAttribute, modelValue);
			Assert.notNull(scopedModelAttribute.getScope());
			bindModelAttribute(context, scopedModelAttribute, modelValue);
		}
	}

	private void bindModelAttribute(MvcFacesRequestContext context, ScopedModelAttribute scopedModelAttribute,
			final Object modelValue) {
		// Attempt to use the internal Faces MVC scopes
		ScopeType mvcScope = ScopeType.find(scopedModelAttribute.getScope());
		if (mvcScope != null) {
			mvcScope.getScope(context).put(scopedModelAttribute.getModelAttribute(), modelValue);
		} else {
			// Fall back to the spring registered scopes
			Scope scope = beanFactory.getRegisteredScope(scopedModelAttribute.getScope());
			Assert.notNull(scope, "Unable to locate " + scopedModelAttribute.getScope() + " from beanFactory");
			scope.get(scopedModelAttribute.getModelAttribute(), new ObjectFactory() {
				public Object getObject() throws BeansException {
					return modelValue;
				}
			});
		}
	}

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(beanFactory, "Missing required beanFactory");
		Assert.notNull(modelScopeProvider, "Missing required modelScopeProvider");
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		Assert.isInstanceOf(ConfigurableBeanFactory.class, beanFactory,
				"BeanScopeModelBinder can only be used bean factories that "
						+ "implement the ConfigurableBeanFactory interface");
		this.beanFactory = (ConfigurableBeanFactory) beanFactory;
	}

	/**
	 * Set the {@link ModelScopeProvider} that will be used to resolve the scope of each model element. For convenience
	 * the {@link #setScope(String)} can also be used when the same scope should be used for each model element.
	 * @param modelScopeProvider The model scope provider used to determine the scope of each model entry
	 */
	public void setModelScopeProvider(ModelScopeProvider modelScopeProvider) {
		this.modelScopeProvider = modelScopeProvider;
	}

	/**
	 * Convince method that can be used to bind all model elements to a specific scope.
	 * @param scope The scope to bind all model elements to
	 */
	public void setScope(String scope) {
		this.modelScopeProvider = new SpecificModelScopeProvider(scope);
	}
}
