package org.springframework.faces.mvc.execution;

import org.springframework.core.NamedThreadLocal;
import org.springframework.faces.mvc.context.MvcFacesContext;

/**
 * Simple holder class that associates a {@link MvcFacesRequestContext} instance with the current thread. The
 * MvcFacesRequestContext will not be inherited by any child threads spawned by the current thread.
 * <p>
 * Used as a central holder for the current MvcFacesRequestContext in Spring Faces, wherever necessary. Often used by
 * integration artifacts needing access to the current execution.
 * 
 * @see MvcFacesRequestContext
 * 
 * @author Jeremy Grelle
 * @author Phillip Webb
 */
public class MvcFacesRequestContextHolder {

	private static final ThreadLocal requestContextHolder = new NamedThreadLocal("MvcFacesRequest");

	/**
	 * Associate the given RequestContext with the current thread.
	 * @param requestContext the current MvcFacesRequestContextControl, or <code>null</code> to reset the thread-bound
	 * context
	 */
	public static void setRequestContext(MvcFacesRequestControlContext requestContext) {
		if (requestContext == null) {
			requestContextHolder.remove();
		} else {
			requestContextHolder.set(requestContext);
		}
	}

	/**
	 * Return the current {@link MvcFacesContext} instance or <tt>null</tt> if the current request is not being handled.
	 * 
	 * @return The current {@link MvcFacesContext} by Spring MVC Faces
	 */
	public static MvcFacesRequestContext getRequestContext() {
		return (MvcFacesRequestContext) requestContextHolder.get();
	}
}
