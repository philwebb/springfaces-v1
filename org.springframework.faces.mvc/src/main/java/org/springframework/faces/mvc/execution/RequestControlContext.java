package org.springframework.faces.mvc.execution;

import org.springframework.faces.mvc.context.MvcFacesExecution;
import org.springframework.faces.mvc.navigation.NavigationRequestEvent;

/**
 * Mutable control interface used to manipulate an ongoing MVC JSF execution. This interface is primarily used
 * internally and is not intended to used directly by client code.
 * 
 * @author Phillip Webb
 */
public interface RequestControlContext extends RequestContext {

	/**
	 * Returns the {@link MvcFacesExecution} for the current request.
	 * @return the active MVC Faces context
	 */
	MvcFacesExecution getExecution();

	// FIXME move out of this class?

	/**
	 * Method called during exception handling to store the current exception.
	 * @param exception The exception being handled
	 * @see #getException()
	 */
	void setException(Exception exception);

	/**
	 * Method called during navigation processing to store the navigation event being processes.
	 * @param lastNavigationRequestEvent The navigation event
	 */
	void setLastNavigationRequestEvent(NavigationRequestEvent lastNavigationRequestEvent);
}
