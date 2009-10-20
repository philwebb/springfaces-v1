package org.springframework.faces.mvc;

import java.io.IOException;

import javax.faces.context.FacesContext;

/**
 * Interface that is used to redirect the client to a specific location following a navigation outcome.
 * 
 * @author Phillip Webb
 */
public interface RedirectHandler {

	/**
	 * Redirect the client to the specified location.
	 * @param facesContext The faces context.
	 * @param location The redirect location, this will be the result of
	 * {@link FacesHandler#getNavigationOutcomeLocation(String, String)}.
	 * @throws IOException
	 */
	public void handleRedirect(FacesContext facesContext, Object location) throws IOException;
}
