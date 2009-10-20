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

import org.springframework.util.StringUtils;

/**
 * Implementation of {@link ModelScopeProvider} that allows scope to be defined as part of the modelAttribute. Any
 * modelAttribute can defined the scope using '.' notation, for example 'sessionScope.firstName' will result in the
 * attribute 'firstName' bound to 'session' scope.
 * 
 * @author Phillip Webb
 */
public class ImplicitModelScopeProvider implements ModelScopeProvider {

	private ModelScopeProvider parent;

	public ImplicitModelScopeProvider(ModelScopeProvider parent) {
		this.parent = parent;
	}

	public ScopedModelAttribute getModelScope(ScopedModelAttribute scopedModelAttribute, Object modelValue) {
		if (scopeAlreadyDefined(scopedModelAttribute)) {
			return scopedModelAttribute;
		}
		String modelAttribute = scopedModelAttribute.getModelAttribute();
		if (modelAttribute != null && modelAttribute.indexOf(".") > 0) {
			int firstDot = modelAttribute.indexOf(".");
			String scope = modelAttribute.substring(0, firstDot);
			modelAttribute = modelAttribute.substring(firstDot + 1);
			return new ScopedModelAttribute(scope, modelAttribute);
		}
		return parent == null ? scopedModelAttribute : parent.getModelScope(scopedModelAttribute, modelValue);
	}

	private boolean scopeAlreadyDefined(ScopedModelAttribute scopedModelAttribute) {
		return StringUtils.hasText(scopedModelAttribute.getScope());
	}

}
