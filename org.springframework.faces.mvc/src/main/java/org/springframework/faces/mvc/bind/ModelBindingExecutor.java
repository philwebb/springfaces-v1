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

/**
 * Interface that can be used to execute {@link ModelBinder}s . Often the {@link ModelBinder#bindModel(Map)} method
 * cannot be called at the point that a JSF view is created as it will want to bind elements to view scope . As view
 * scope depends on a valid {@link FacesContext#getViewRoot()} binding can only occur after the view has been created
 * and attached to the FacesContext. In order to overcome this limitation this executor will allow bind operations to
 * occur to two phases. The {@link #storeModelToBind(FacesContext, Map)} method will be called when the view is first
 * created and the {@link #bindStoredModel(FacesContext)} method will be called before the RENDER_RESPONSE phase.
 * 
 * @author Phillip Webb
 */
public interface ModelBindingExecutor {

	/**
	 * Called when a view is created so that the executor can store the model for the
	 * {@link #bindStoredModel(FacesContext)} to later retrieve.
	 * @param facesContext The current FacesContext
	 * @param model The model to store
	 */
	void storeModelToBind(FacesContext facesContext, Map model);

	/**
	 * Called before the RENDER_RESPONSE phase to bind the model that was stored during
	 * {@link #storeModelToBind(FacesContext, Map)}.
	 * @param facesContext The current FacesContext
	 */
	void bindStoredModel(FacesContext facesContext);
}
