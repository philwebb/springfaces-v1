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
package org.springframework.faces.mvc.navigation;

import java.io.Serializable;
import java.util.EventObject;

import javax.faces.application.NavigationHandler;

import org.springframework.faces.mvc.execution.MvcFacesExceptionHandler;
import org.springframework.util.ObjectUtils;

/**
 * An event object that represents a JSF navigation request. This object encapsulates the <tt>fromAction</tt> and
 * <tt>outcome</tt> parameters that are passed to JSF {@link NavigationHandler}s. The event object can also include an
 * exception if the navigation request is being handled within a {@link MvcFacesExceptionHandler}.
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
	 * @param owner The owner
	 * @param fromAction The from action
	 * @param outcome The outcome
	 */
	public NavigationRequestEvent(Object owner, String fromAction, String outcome) {
		super(owner);
		this.fromAction = fromAction;
		this.outcome = outcome;
	}

	/**
	 * Constructor for a navigation event that includes an exception.
	 * 
	 * @param owner The owner
	 * @param sourceEvent The {@link NavigationRequestEvent} that was being processed when the exception was raised or
	 * <tt>null</tt>
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
	 * Returns the action binding expression that was evaluated to retrieve the specified outcome, or <tt>null</tt> if
	 * the outcome was acquired by some other means.
	 * 
	 * @return The action binding expression or <tt>null</tt>
	 */
	public String getFromAction() {
		return fromAction;
	}

	/**
	 * Returns the logical outcome returned by a previous invoked application action (which may be <tt>null</tt>)
	 * 
	 * @return The outcome or <tt>null</tt>
	 */
	public String getOutcome() {
		return outcome;
	}

	/**
	 * Returns any exception tied to the event.
	 * 
	 * @return The exception or <tt>null</tt>
	 */
	public Exception getException() {
		return exception;
	}

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
				&& ObjectUtils.nullSafeEquals(outcome, other.outcome)
				&& ObjectUtils.nullSafeEquals(exception, exception);
	}

	public int hashCode() {
		int rtn = ObjectUtils.nullSafeHashCode(fromAction);
		rtn = 37 * rtn + ObjectUtils.nullSafeHashCode(outcome);
		rtn = 37 * rtn + ObjectUtils.nullSafeHashCode(exception);
		return rtn;
	}

	public String toString() {
		return "JSF Navigation Request Event (fromAction=\"" + fromAction + "\", outcome=\"" + outcome
				+ "\", exception=\"" + exception + "\")";
	}
}
