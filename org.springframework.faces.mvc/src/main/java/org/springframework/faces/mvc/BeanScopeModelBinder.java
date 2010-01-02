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
package org.springframework.faces.mvc;

import java.util.Iterator;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.util.Assert;

/**
 * Implementation of {@link ModelBinder} that exposes MVC Model elements to JSF by delegating to one of the registered
 * spring {@link Scope} implementations. By default this binder is configured to use a
 * {@link SpecificModelScopeProvider} set to <tt>request</tt> scope with {@link ImplicitModelScopeProvider} support.
 * 
 * @author Phillip Webb
 */
public class BeanScopeModelBinder implements ModelBinder, BeanFactoryAware, InitializingBean {
	// FIXME exclude org.springframework.validation.BindingResult. ?

	private ConfigurableBeanFactory beanFactory;
	private ModelScopeProvider modelScopeProvider;

	public BeanScopeModelBinder() {
		this.modelScopeProvider = new ImplicitModelScopeProvider(SpecificModelScopeProvider.REQUSET);
	}

	public void bindModel(Map model) {
		for (Iterator iterator = model.entrySet().iterator(); iterator.hasNext();) {
			final Map.Entry modelEntry = (Map.Entry) iterator.next();
			final Object modelValue = modelEntry.getValue();
			ScopedModelAttribute scopedModelAttribute = new ScopedModelAttribute((String) modelEntry.getKey());
			scopedModelAttribute = modelScopeProvider.getModelScope(scopedModelAttribute, modelValue);
			Assert.notNull(scopedModelAttribute.getScope());
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
	 * the {@link #setScope(String)} can also be used to always use the same scope for each model element. If not
	 * specified the {@link SpecificModelScopeProvider} (set to <tt>request</tt> scope) with
	 * {@link ImplicitModelScopeProvider} support will be used.
	 * 
	 * @param modelScopeProvider The model scope provider used to determine the scope of each model entry
	 */
	public void setModelScopeProvider(ModelScopeProvider modelScopeProvider) {
		this.modelScopeProvider = modelScopeProvider;
	}

	/**
	 * Convince method that can be used to bind all model elements to a specific scope.
	 * 
	 * @param scope The scope to bind all model elements to
	 */
	public void setScope(String scope) {
		this.modelScopeProvider = new SpecificModelScopeProvider(scope);
	}
}
