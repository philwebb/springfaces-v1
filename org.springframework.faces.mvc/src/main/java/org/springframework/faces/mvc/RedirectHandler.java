package org.springframework.faces.mvc;

import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Interface that is used to redirect the client to a specific location following a navigation outcome. Note:
 * Implementations of this interface should not rely on a {@link FacesContext} being available.
 * 
 * @author Phillip Webb
 */
public interface RedirectHandler {
	/**
	 * Redirect the client to the specified location.
	 * @param request The native request.
	 * @param response The native response.
	 * @param location The redirect location, this will be the result of
	 * {@link FacesHandler#getNavigationOutcomeLocation(javax.faces.context.FacesContext, NavigationRequestEvent)} or
	 * {@link MvcFacesExceptionOutcome#redirect(Object)}..
	 * @throws IOException
	 */
	public void handleRedirect(HttpServletRequest request, HttpServletResponse response, Object location)
			throws IOException;
}
