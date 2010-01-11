package org.springframework.faces.mvc.support;

import javax.faces.context.FacesContext;

import org.springframework.faces.mvc.FacesHandler;
import org.springframework.faces.mvc.NavigationRequestEvent;
import org.springframework.util.Assert;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.core.collection.MutableAttributeMap;

/**
 * Default implementation of {@link MvcFacesRequestControlContext}.
 * 
 * @author Phillip Webb
 */
public class MvcFacesRequestControlContextImpl implements MvcFacesRequestControlContext {

	private boolean released;
	private MvcFacesContext mvcFacesContext;
	private FacesHandler facesHandler;
	private Exception exception;
	private NavigationRequestEvent lastNavigationRequestEvent;

	private MutableAttributeMap requestScope = new LocalAttributeMap();

	// FIXME how to do flash scope
	private static MutableAttributeMap flashScope = new LocalAttributeMap();

	// Late binding
	private MutableAttributeMap viewScope = null;

	/**
	 * Public constructor.
	 * 
	 * @param mvcFacesContext
	 * @param facesHandler
	 * 
	 * @see #release()
	 */
	public MvcFacesRequestControlContextImpl(MvcFacesContext mvcFacesContext, FacesHandler facesHandler) {
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

	public MutableAttributeMap getRequestScope() {
		return requestScope;
	}

	public MutableAttributeMap getFlashScope() {
		return flashScope;
	}

	public MutableAttributeMap getViewScope() throws IllegalStateException {
		if (viewScope == null) {
			FacesContext facesContext = FacesContext.getCurrentInstance();
			Assert.notNull(facesContext, "Faces context is not active");
			viewScope = MvcFacesStateHolderComponent.locate(facesContext, true).getViewScope();
		}
		return viewScope;
	}
}
