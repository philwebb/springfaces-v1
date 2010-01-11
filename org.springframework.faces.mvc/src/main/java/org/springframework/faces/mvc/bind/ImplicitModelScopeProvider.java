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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

/**
 * Implementation of {@link ModelScopeProvider} that allows scope to be defined as part of the <tt>modelAttribute</tt>.
 * Any <tt>modelAttribute</tt> can define the scope using '.' notation, for example 'sessionScope.firstName' will result
 * in the attribute <tt>'firstName'</tt> bound to <tt>'session'</tt> scope. Note: Any model element that contains multiple '.' characters will not be
 * considered.
 * 
 * @author Phillip Webb
 */
public class ImplicitModelScopeProvider implements ModelScopeProvider {

	private static final Pattern PATTERN = Pattern.compile("(\\w+)Scope\\.([^\\.]+)");

	private ModelScopeProvider parent;

	public ImplicitModelScopeProvider(ModelScopeProvider parent) {
		this.parent = parent;
	}

	public ScopedModelAttribute getModelScope(ScopedModelAttribute scopedModelAttribute, Object modelValue) {
		if (scopeAlreadyDefined(scopedModelAttribute)) {
			return scopedModelAttribute;
		}
		String modelAttribute = scopedModelAttribute.getModelAttribute();
		if (modelAttribute != null) {
			Matcher matcher = PATTERN.matcher(modelAttribute);
			if (matcher.matches()) {
				String scope = matcher.group(1);
				modelAttribute = matcher.group(2);
				return new ScopedModelAttribute(scope, modelAttribute);
			}
		}
		return parent == null ? scopedModelAttribute : parent.getModelScope(scopedModelAttribute, modelValue);
	}

	private boolean scopeAlreadyDefined(ScopedModelAttribute scopedModelAttribute) {
		return StringUtils.hasText(scopedModelAttribute.getScope());
	}
}
