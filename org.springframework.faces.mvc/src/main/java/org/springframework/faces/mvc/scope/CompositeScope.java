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
package org.springframework.faces.mvc.scope;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.util.Assert;

/**
 * Scope implementation that can be used to combine several existing implementations so that they can all be registered
 * with the same name. Use the {@link #add(Scope, ScopeAvailabilityFilter)} method to add scopes to the composite.
 * 
 * @author Phillip Webb
 */
public class CompositeScope implements Scope {

	/**
	 * Logger, usable by subclasses.
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * A <tt>null</tt> Scope implementation that can be used when no active delegate can be found.
	 */
	protected final Scope nullScope = new Scope() {

		public Object get(String name, ObjectFactory objectFactory) {
			throw new IllegalStateException("Unable to get value for '" + name
					+ "' as no active delegate scope was located.  Ensure that you are attempting "
					+ "to access the bean from a valid execution context");
		}

		public Object remove(String name) {
			return null;
		}

		public String getConversationId() {
			return null;
		}

		public void registerDestructionCallback(String name, Runnable callback) {
			logger.warn("Destruction callback for '" + name
					+ "' was not registered as a delegate scope could not be found.");
		}
	};

	/**
	 * List of {@link ScopeAndAvailability} objects that provide potential candidates for the scope.
	 */
	private List candiateScopes = new ArrayList();

	/**
	 * Add a scope to the composite using the specified filter to determine when the scope is available. Scope are
	 * tested in the order that they are added, stopping at the first available scope.
	 * @param scope The scope to add
	 * @param availabilityFilter A filter used to determine if the scope is available or <tt>null</tt> if the scope is
	 * always available
	 */
	public void add(Scope scope, ScopeAvailabilityFilter availabilityFilter) {
		Assert.notNull(scope, "The scope is required");
		candiateScopes.add(new ScopeAndAvailability(scope, availabilityFilter));
	}

	/**
	 * Returns the first available scope from the previously {@link #add(Scope, ScopeAvailabilityFilter) added} list. If
	 * not scope is available a stub implementation is returned. This method never returns <tt>null</tt>.
	 * @return A scope implementation (never <tt>null</tt>)
	 */
	protected Scope getScope() {
		for (Iterator iterator = candiateScopes.iterator(); iterator.hasNext();) {
			ScopeAndAvailability candiate = (ScopeAndAvailability) iterator.next();
			if (candiate.isAvailable()) {
				return candiate.getScope();
			}
		}
		return nullScope;
	}

	public Object get(String name, ObjectFactory objectFactory) {
		return getScope().get(name, objectFactory);
	}

	public Object remove(String name) {
		return getScope().remove(name);
	}

	public String getConversationId() {
		return getScope().getConversationId();
	}

	public void registerDestructionCallback(String name, Runnable callback) {
		getScope().registerDestructionCallback(name, callback);
	}

	/**
	 * Internal class used to manage added scopes and filters.
	 */
	protected static class ScopeAndAvailability {
		private Scope scope;
		private ScopeAvailabilityFilter availabilityFilter;

		public ScopeAndAvailability(Scope scope, ScopeAvailabilityFilter availabilityFilter) {
			this.scope = scope;
			this.availabilityFilter = availabilityFilter;
		}

		public boolean isAvailable() {
			return (availabilityFilter == null || availabilityFilter.isAvailable(scope));
		}

		public Scope getScope() {
			return scope;
		}
	}
}
