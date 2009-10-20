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

import org.springframework.util.Assert;

public class SpecificModelScopeProvider implements ModelScopeProvider {

	private String scope;

	public SpecificModelScopeProvider(String scope) {
		Assert.notNull(scope);
		this.scope = scope;
	}

	public ScopedModelAttribute getModelScope(ScopedModelAttribute modelAttribute, Object modelValue) {
		return modelAttribute.newScope(scope);
	}

	public static final ModelScopeProvider REQUSET = new SpecificModelScopeProvider("request");
	public static final ModelScopeProvider PAGE = new SpecificModelScopeProvider("page");
	public static final ModelScopeProvider SESSION = new SpecificModelScopeProvider("session");
}
