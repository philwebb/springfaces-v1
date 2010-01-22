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
package org.springframework.faces.mvc.execution.repository;

import java.io.Serializable;
import java.util.LinkedHashMap;

import org.springframework.faces.mvc.execution.ExecutionContextKey;
import org.springframework.faces.mvc.execution.RequestContext;
import org.springframework.util.Assert;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.core.collection.SharedAttributeMap;

/**
 * Simple implementation of {@link ExecutionContextRepository} that stored execution details in HTTP session.
 * 
 * Using the {@link #setMaxExecutions(int) maxExecutions} property, you can limit the number of concurrently active
 * executions allowed in a single session. If the maximum is exceeded, the repository will automatically cleanup the
 * oldest execution. The default is 5, which should be fine for most situations. Set it to -1 for no limit. Setting
 * maxConversations to 1 allows easy resource cleanup in situations where there should only be one active conversation
 * per session.
 * 
 * @author Erwin Vervaet
 * @author Phillip Webb
 */
public class SessionBindingExecutionContextRepository implements ExecutionContextRepository {

	/**
	 * The session key used to store the executions.
	 */
	private String sessionKey = "mvcFacesExecutions";

	/**
	 * The maximum number of concurrent executions.
	 */
	private int maxExecutions = 5;

	public ExecutionContextKey parseKey(String key) {
		return IntegerExecutionContextKey.parse(key);
	}

	/**
	 * Obtain the container used to store executions in the current session. Create a new empty container and add it to
	 * the session if no existing container can be found.
	 * 
	 * @param request The HTTP request
	 * @return A container (never <tt>null</tt>)
	 */
	private StoredExecutionContextContainer getContainer(SharedAttributeMap sessionMap) {
		synchronized (sessionMap.getMutex()) {
			StoredExecutionContextContainer container = (StoredExecutionContextContainer) sessionMap.get(sessionKey);
			if (container == null) {
				container = new StoredExecutionContextContainer(maxExecutions);
				sessionMap.put(sessionKey, container);
			}
			return container;
		}
	}

	public ExecutionContextKey save(RequestContext requestContext) throws ExecutionContextRepositoryException {
		try {
			if (!StoredExecutionContext.shouldBeSaved(requestContext)) {
				return null;
			}
			SharedAttributeMap sessionMap = requestContext.getExternalContext().getSessionMap();
			return getContainer(sessionMap).save(requestContext);
		} catch (RuntimeException e) {
			if (e instanceof ExecutionContextRepositoryException) {
				throw e;
			}
			throw new ExecutionContextRepositoryException("Unable to save the specified MVC faces request", e);
		}
	}

	public void restore(ExecutionContextKey key, RequestContext requestContext) {
		try {
			SharedAttributeMap sessionMap = requestContext.getExternalContext().getSessionMap();
			getContainer(sessionMap).restore(key, requestContext);
		} catch (RuntimeException e) {
			if (e instanceof ExecutionContextRepositoryException) {
				throw e;
			}
			throw new ExecutionContextRepositoryException(
					"Unable to restore the MVC faces request with the execution key '" + key + "'", e);
		}
	}

	/**
	 * Set the maximum number of allowed concurrent executions that can be stored per session. Set to -1 for no limit.
	 * The default is 5.
	 * @param maxExecutions The maximum number of allowed concurrent executions.
	 */
	public void setMaxExecutions(int maxExecutions) {
		this.maxExecutions = maxExecutions;
	}

	/**
	 * Set the session key that will be used to store active executions. Defaults to "mvcFacesExecutions" if not
	 * specified.
	 * @param sessionKey The session key.
	 */
	public void setSessionKey(String sessionKey) {
		Assert.hasLength(sessionKey, "The sessionKey is required");
		this.sessionKey = sessionKey;
	}

	/**
	 * Class stored in HTTP session to contain all active executions.
	 * @see StoredExecutionContext
	 */
	static class StoredExecutionContextContainer implements Serializable {

		/**
		 * Map of {@link ExecutionContextKey} to {@link StoredExecutionContext} objects stored in key insertion order.
		 */
		private LinkedHashMap stored;

		/**
		 * Sequence number incremented on each save.
		 */
		private int sequence;

		/**
		 * The maximum capacity of the container or -1
		 */
		private int maxCapacity;

		public StoredExecutionContextContainer(int maxCapacity) {
			this.maxCapacity = maxCapacity;
			this.stored = new LinkedHashMap();
		}

		/**
		 * Determine if the capacity of the container has been exceeded.
		 * @return <tt>true</tt> if the capacity has been exceeded
		 */
		private boolean capactityExceeded() {
			return ((maxCapacity > 0) && (getSize() > maxCapacity));
		}

		/**
		 * @return The number of stored items
		 */
		protected int getSize() {
			return stored.size();
		}

		/**
		 * Save data from the specified request, removing old executions as necessary.
		 * @param requestContext The request context
		 * @return The key of the newly saved execution
		 */
		public synchronized ExecutionContextKey save(RequestContext requestContext) {
			IntegerExecutionContextKey key = new IntegerExecutionContextKey(++sequence);
			stored.put(key, new StoredExecutionContext(requestContext));
			if (capactityExceeded() && (getSize() > 1)) {
				stored.remove(stored.keySet().iterator().next());
			}
			return key;
		}

		/**
		 * Restore data to the specified request, removing the conversation.
		 * @param key The execution key
		 * @param requestContext The request to restore
		 */
		public synchronized void restore(ExecutionContextKey key, RequestContext requestContext) {
			StoredExecutionContext storedExecutionContext = (StoredExecutionContext) stored.remove(key);
			if (storedExecutionContext == null) {
				throw new NoSuchExecutionException(key);
			}
			storedExecutionContext.restore(requestContext);
		}
	}

	/**
	 * A single stored execution contained in a {@link StoredExecutionContextContainer}.
	 */
	static class StoredExecutionContext implements Serializable {

		/**
		 * Stored flash scope
		 */
		private MutableAttributeMap flashScope;

		/**
		 * Constructor.
		 * @param requestContext The request context to save data from.
		 */
		public StoredExecutionContext(RequestContext requestContext) {
			this.flashScope = new LocalAttributeMap();
			this.flashScope.putAll(requestContext.getFlashScope());
		}

		/**
		 * Restore data to the specified request context.
		 * @param requestContext
		 */
		public void restore(RequestContext requestContext) {
			requestContext.getFlashScope().replaceWith(this.flashScope);
		}

		/**
		 * Static method that is used to determine if a request context needs to be saved.
		 * @param requestContext
		 * @return <tt>true</tt> if the request should be saved
		 */
		public static boolean shouldBeSaved(RequestContext requestContext) {
			return ((requestContext != null) && (!requestContext.getFlashScope().isEmpty()));
		}
	}
}
