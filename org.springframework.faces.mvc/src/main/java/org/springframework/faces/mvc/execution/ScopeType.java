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
package org.springframework.faces.mvc.execution;

import java.util.HashMap;
import java.util.Map;

import org.springframework.core.enums.StaticLabeledEnum;
import org.springframework.faces.mvc.bind.ModelScopeProvider;
import org.springframework.faces.mvc.bind.ScopedModelAttribute;
import org.springframework.webflow.core.collection.MutableAttributeMap;

/**
 * An enumeration of the core scope types of Spring Faces MVC. Provides easy access to each scope by <i>type</i> using
 * {@link #getScope(MvcFacesRequestContext)}.
 * <p>
 * A "scope" defines a data structure for storing model data within an MVC JSF execution. Different scope types have
 * different semantics in terms of how long attributes placed in those scope maps remain valid.
 * <p>
 * For convenience this class also implements {@link ModelScopeProvider}, allowing the ScopeType to act a specific model
 * scope.
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 * @author Phillip Webb
 */
public abstract class ScopeType extends StaticLabeledEnum implements ModelScopeProvider {

	/**
	 * The "request" scope type. See {@link MvcFacesRequestContext#getRequestScope()} for details.
	 */
	public static final ScopeType REQUEST = new ScopeType(0, "Request") {
		public MutableAttributeMap getScope(MvcFacesRequestContext context) {
			return context.getRequestScope();
		}
	};

	/**
	 * The "flash" scope type. See {@link MvcFacesRequestContext#getFlashScope()} for details.
	 */
	public static final ScopeType FLASH = new ScopeType(1, "Flash") {
		public MutableAttributeMap getScope(MvcFacesRequestContext context) {
			return context.getFlashScope();
		}
	};

	/**
	 * The "view" scope type. See {@link MvcFacesRequestContext#getViewScope()} for details.
	 */
	public static final ScopeType VIEW = new ScopeType(1, "View") {
		public MutableAttributeMap getScope(MvcFacesRequestContext context) {
			return context.getViewScope();
		}
	};

	/**
	 * Private constructor.
	 */
	private ScopeType(int code, String label) {
		super(code, label);
	}

	public Class getType() {
		return ScopeType.class;
	}

	public ScopedModelAttribute getModelScope(ScopedModelAttribute modelAttribute, Object modelValue) {
		return modelAttribute.newScope(getLabel().toLowerCase());
	}

	/**
	 * Accessor that returns the mutable attribute map for this scope type for a given {@link MvcFacesRequestContext}.
	 * @param context the context representing an executing request
	 * @return the scope map of this type for that request, allowing attributes to be accessed and set
	 */
	public abstract MutableAttributeMap getScope(MvcFacesRequestContext context);

	/**
	 * All known values.
	 */
	private static final ScopeType[] VALUES;
	private static final Map LABEL_SCOPE_TYPES;
	static {
		VALUES = new ScopeType[] { REQUEST, FLASH, VIEW };
		LABEL_SCOPE_TYPES = new HashMap();
		for (int i = 0; i < VALUES.length; i++) {
			LABEL_SCOPE_TYPES.put(VALUES[i].getLabel().toLowerCase(), VALUES[i]);
		}
	}

	/**
	 * @return All known scopes.
	 */
	public static ScopeType[] values() {
		return VALUES;
	}

	/**
	 * Locate the {@link ScopeType} with the specified name or return <tt>null</tt> if the scope type is not known.
	 * 
	 * @param scope The scope to find
	 * @return The scope type or <tt>null</tt>
	 */
	public static ScopeType find(String scope) {
		return (ScopeType) LABEL_SCOPE_TYPES.get(scope);
	}
}