package org.springframework.faces.mvc.execution;

import org.springframework.faces.mvc.navigation.NavigationRequestEvent;

/**
 * Mutable control interface used to manipulate an ongoing MVC JSF execution. This interface is primarily used
 * internally and is not intended to used directly by client code.
 * 
 * @author Phillip Webb
 */
public interface MvcFacesRequestControlContext extends MvcFacesRequestContext {

	/**
	 * Method called during exception handling to store the current exception.
	 * 
	 * @param exception The exception being handled
	 * 
	 * @see #getException()
	 */
	void setException(Exception exception);

	/**
	 * Method called during navigation processing to store the navigation event being processes.
	 * 
	 * @param lastNavigationRequestEvent The navigation event
	 */
	void setLastNavigationRequestEvent(NavigationRequestEvent lastNavigationRequestEvent);
}
