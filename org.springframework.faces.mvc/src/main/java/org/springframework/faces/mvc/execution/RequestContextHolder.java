package org.springframework.faces.mvc.execution;

import org.springframework.core.NamedThreadLocal;

/**
 * Simple holder class that associates a {@link RequestContext} instance with the current thread. The
 * {@link RequestContext} will not be inherited by any child threads spawned by the current thread.
 * <p>
 * Used as a central holder for the current {@link RequestContext} in Spring Faces MVC, wherever necessary. Often used
 * by integration artifacts needing access to the current execution.
 * 
 * @see RequestContext
 * 
 * @author Jeremy Grelle
 * @author Phillip Webb
 */
public class RequestContextHolder {

	private static final ThreadLocal requestContextHolder = new NamedThreadLocal("RequestContext");

	/**
	 * Associate the given RequestContext with the current thread.
	 * @param requestContext the current {@link RequestControlContext}, or <code>null</code> to reset the thread-bound
	 * context
	 */
	public static void setRequestContext(RequestControlContext requestContext) {
		if (requestContext == null) {
			requestContextHolder.remove();
		} else {
			requestContextHolder.set(requestContext);
		}
	}

	/**
	 * Return the current {@link RequestContext} instance or <tt>null</tt> if the current request is not being handled.
	 * @return The current {@link RequestContext}
	 */
	public static RequestContext getRequestContext() {
		return (RequestContext) requestContextHolder.get();
	}
}
