package org.springframework.faces.mvc.navigation;

import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.faces.mvc.FacesHandler;
import org.springframework.faces.mvc.execution.MvcFacesExceptionOutcome;
import org.springframework.js.ajax.AjaxHandler;

/**
 * Interface that is used to redirect the client to a specific location following a navigation outcome. Note:
 * Implementations of this interface should not rely on a {@link FacesContext} being available.
 * 
 * @author Phillip Webb
 */
public interface RedirectHandler {

	/**
	 * Redirect the client to the specified location.
	 * 
	 * @param ajaxHandler The AJAX handler that is being used to handle the request. This will never be <tt>null</tt>
	 * @param request The native request
	 * @param response The native response
	 * @param location The redirect location, this will be the result of
	 * {@link FacesHandler#getNavigationOutcomeLocation(javax.faces.context.FacesContext, NavigationRequestEvent)} or
	 * {@link MvcFacesExceptionOutcome#redirect(NavigationLocation)}
	 * @throws IOException
	 */
	public void handleRedirect(AjaxHandler ajaxHandler, HttpServletRequest request, HttpServletResponse response,
			NavigationLocation location) throws IOException;
}
