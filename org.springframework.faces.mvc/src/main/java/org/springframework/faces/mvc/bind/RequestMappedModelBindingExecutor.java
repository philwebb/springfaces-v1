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

import java.util.Map;

import javax.faces.context.FacesContext;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * Implementation of {@link ModelBindingExecutor} that stores the model in request scope until it can be bound.
 * 
 * @author Phillip Webb
 */
public class RequestMappedModelBindingExecutor implements ModelBindingExecutor, InitializingBean {

	private static final String MODEL_ATTRIBUTE = RequestMappedModelBindingExecutor.class.getName() + ".MODEL";

	private ModelBinder modelBinder;

	public void storeModelToBind(FacesContext facesContext, Map model) {
		facesContext.getExternalContext().getRequestMap().put(MODEL_ATTRIBUTE, model);
	}

	public void bindStoredModel(FacesContext facesContext) {
		Map model = (Map) facesContext.getExternalContext().getRequestMap().remove(MODEL_ATTRIBUTE);
		if (model != null) {
			modelBinder.bindModel(model);
		}
	}

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(modelBinder, "The modelBinder attribute is required");
	}

	/**
	 * Set the {@link ModelBinder} that will be used to bind the model.
	 * 
	 * @param modelBinder The model binder
	 */
	public void setModelBinder(ModelBinder modelBinder) {
		this.modelBinder = modelBinder;
	}

	/**
	 * Returns the {@link ModelBinder} that will be used to bind the model.
	 * 
	 * @return The model binder
	 */
	public ModelBinder getModelBinder() {
		return modelBinder;
	}
}
