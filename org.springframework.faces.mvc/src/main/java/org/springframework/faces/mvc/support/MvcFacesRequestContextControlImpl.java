package org.springframework.faces.mvc.support;

import org.springframework.faces.mvc.FacesHandler;
import org.springframework.faces.mvc.NavigationRequestEvent;
import org.springframework.util.Assert;

/**
 * Default implementation of {@link MvcFacesRequestContextControl}.
 * 
 * @author Phillip Webb
 */
public class MvcFacesRequestContextControlImpl implements MvcFacesRequestContextControl {

	private boolean released;
	private MvcFacesContext mvcFacesContext;
	private FacesHandler facesHandler;
	private Exception exception;
	private NavigationRequestEvent lastNavigationRequestEvent;

	/**
	 * Public constructor.
	 * 
	 * @param mvcFacesContext
	 * @param facesHandler
	 * 
	 * @see #release()
	 */
	public MvcFacesRequestContextControlImpl(MvcFacesContext mvcFacesContext, FacesHandler facesHandler) {
		Assert.notNull(mvcFacesContext);
		Assert.notNull(facesHandler);
		this.mvcFacesContext = mvcFacesContext;
		this.facesHandler = facesHandler;
		MvcFacesRequestContextHolder.setRequestContext(this);
	}

	/**
	 * Lifecycle call that releases the request context. This method should be called in a <tt>finally</tt> block after
	 * construction of the object to ensure that all resources are released.
	 */
	public void release() {
		if (released) {
			throw new IllegalStateException("The MvcFacesRequest has already been released");
		}
		released = true;
		MvcFacesRequestContextHolder.setRequestContext(null);
	}

	public FacesHandler getFacesHandler() {
		return facesHandler;
	}

	public MvcFacesContext getMvcFacesContext() {
		return mvcFacesContext;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

	public Exception getException() {
		return exception;
	}

	public void setLastNavigationRequestEvent(NavigationRequestEvent lastNavigationRequestEvent) {
		this.lastNavigationRequestEvent = lastNavigationRequestEvent;
	}

	public NavigationRequestEvent getLastNavigationRequestEvent() {
		return lastNavigationRequestEvent;
	}

}
