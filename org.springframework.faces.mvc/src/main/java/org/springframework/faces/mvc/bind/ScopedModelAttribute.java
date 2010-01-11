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

import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * Simple data holder that represents a model attribute and a scope.
 * 
 * @author Phillip Webb
 */
public final class ScopedModelAttribute {

	private String scope;
	private String modelAttribute;

	/**
	 * @param scope An optional scope
	 * @param modelAttribute The attribute name
	 */
	public ScopedModelAttribute(String scope, String modelAttribute) {
		super();
		Assert.notNull(modelAttribute);
		this.scope = scope;
		this.modelAttribute = modelAttribute;
	}

	public ScopedModelAttribute(String modelAttribute) {
		this(null, modelAttribute);
	}

	public String getScope() {
		return scope;
	}

	public String getModelAttribute() {
		return modelAttribute;
	}

	/**
	 * Create a variant of this attribute in the specified scope.
	 * 
	 * @param scope The new scope
	 * @return A new {@link ScopedModelAttribute} with the same attribute name as this object but in the specified scope
	 */
	public ScopedModelAttribute newScope(String scope) {
		return new ScopedModelAttribute(scope, this.modelAttribute);
	}

	public int hashCode() {
		return (scope == null ? 0 : scope.hashCode()) + modelAttribute.hashCode();
	}

	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj == null)
			return false;
		if (obj instanceof ScopedModelAttribute) {
			ScopedModelAttribute cmp = (ScopedModelAttribute) obj;
			return ObjectUtils.nullSafeEquals(scope, cmp.scope) && modelAttribute.equals(cmp.modelAttribute);
		}
		return super.equals(obj);
	}

	public String toString() {
		return new ToStringCreator(this).append("scope", scope).append("modelAttribute", modelAttribute).toString();
	}

}
