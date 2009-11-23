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

import java.io.Serializable;
import java.util.EventObject;

import javax.faces.application.NavigationHandler;

import org.springframework.util.ObjectUtils;

/**
 * An event object that represents a JSF navigation request. This object encapsulates the <tt>fromAction</tt> and
 * <tt>outcome</tt> paramters that are passed to JSF {@link NavigationHandler}s.
 * 
 * @author Phillip Webb
 */
public final class NavigationRequestEvent extends EventObject implements Serializable {

	private String fromAction;
	private String outcome;
	private Exception exception;

	/**
	 * Constructor for a regular navigation event.
	 * 
	 * @param owner The owner.
	 * @param fromAction The from action.
	 * @param outcome The outcome.
	 */
	public NavigationRequestEvent(Object owner, String fromAction, String outcome) {
		super(owner);
		this.fromAction = fromAction;
		this.outcome = outcome;
	}

	/**
	 * Constructor for a navigation event that includes an exception.
	 * @param owner The owner
	 * @param sourceEvent The {@link NavigationRequestEvent} that was being processed when the exception was raised or
	 * <tt>null</tt>.
	 * @param exception The exception.
	 */
	public NavigationRequestEvent(Object owner, NavigationRequestEvent sourceEvent, Exception exception) {
		super(owner);
		if (sourceEvent != null) {
			this.fromAction = sourceEvent.getFromAction();
			this.outcome = sourceEvent.getOutcome();
		}
		this.exception = exception;
	}

	/**
	 * @return The action binding expression that was evaluated to retrieve the specified outcome, or <tt>null</tt> if
	 * the outcome was acquired by some other means
	 */
	public String getFromAction() {
		return fromAction;
	}

	/**
	 * @return The logical outcome returned by a previous invoked application action (which may be <tt>null</tt>)
	 */
	public String getOutcome() {
		return outcome;
	}

	/**
	 * @return Any exception tied to the event.
	 */
	public Exception getException() {
		return exception;
	}

//FIXME to string, hashcode + equals for exception
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof NavigationRequestEvent)) {
			return false;
		}
		NavigationRequestEvent other = (NavigationRequestEvent) obj;
		return ObjectUtils.nullSafeEquals(fromAction, other.fromAction)
				&& ObjectUtils.nullSafeEquals(outcome, other.outcome);
	}

	public int hashCode() {
		return ObjectUtils.nullSafeHashCode(fromAction) + ObjectUtils.nullSafeHashCode(outcome);
	}

	public String toString() {
		return "JSF Navigation Request Event (fromAction=\"" + fromAction + "\", outcome=\"" + outcome + "\")";
	}
}
