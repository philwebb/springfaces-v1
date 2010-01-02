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

/**
 * Interface that can be used to determine the scope that should be used to expose a specific model attribute.
 * 
 * @author Phillip Webb
 */
public interface ModelScopeProvider {

	/**
	 * Determine the scope that should be used to expose a model attribute.
	 * 
	 * @return The a {@link ScopedModelAttribute} that contains the scope and variable name that should be used to
	 * expose the <tt>model</tt> to JSF. The scope should be one of the extended spring scopes. The values
	 * <tt>request</tt>, <tt>page</tt> or </tt>session</tt> are recommended. The <tt>singleton</tt> and
	 * <tt>prototype</tt> scopes are not supported
	 * 
	 * @param scopedModelAttribute The name of the attribute and the existing scope (if any).
	 */
	ScopedModelAttribute getModelScope(ScopedModelAttribute scopedModelAttribute, Object modelValue);
}
