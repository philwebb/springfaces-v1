package org.springframework.faces.mvc;

import javax.faces.event.PhaseId;

/**
 * Callback interface that can be used by {@link MvcFacesExceptionHandler} implementations to request that a specific
 * action is taken after the exception has been handled.
 * 
 * @author Phillip Webb
 */
public interface MvcFacesExceptionOutcome {

	/**
	 * Issue a redirect to the specific location. The redirect will be handled using the {@link RedirectHandler} from
	 * the {@link AbstractFacesHandlerAdapter} that is processing the request.
	 * 
	 * @param location The redirect location.
	 */
	public void redirect(Object location);

	/**
	 * Re-rendered the current view up to the point of {@link PhaseId#PROCESS_VALIDATIONS}.
	 */
	public void redisplay();
}
