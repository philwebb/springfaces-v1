package org.springframework.faces.mvc.execution;

import org.springframework.faces.mvc.AbstractFacesHandlerAdapter;
import org.springframework.faces.mvc.navigation.NavigationRequestEvent;
import org.springframework.faces.mvc.support.MvcNavigationHandler;

/**
 * Mutable control interface used to manipulate an ongoing MVC JSF execution. This interface is primarily used
 * internally and is not intended to used directly by client code.
 * 
 * @author Phillip Webb
 */
public interface MvcFacesRequestControlContext extends MvcFacesRequestContext {

	/**
	 * Method called during exception handling to store the current exception. This is a framework method called by
	 * {@link AbstractFacesHandlerAdapter} and should not be called directly by developers.
	 * 
	 * @param exception The exception being handled
	 * 
	 * @see #getException()
	 */
	void setException(Exception exception);

	/**
	 * Method called during navigation processing to store the navigation event being processes. This is a framework
	 * method called by {@link MvcNavigationHandler} and should not be called directly by developers.
	 * 
	 * @param lastNavigationRequestEvent The navigation event
	 */
	void setLastNavigationRequestEvent(NavigationRequestEvent lastNavigationRequestEvent);
}
