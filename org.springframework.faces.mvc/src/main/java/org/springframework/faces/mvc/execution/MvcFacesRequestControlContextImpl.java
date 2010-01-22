package org.springframework.faces.mvc.execution;

import javax.faces.context.FacesContext;

import org.springframework.faces.mvc.context.ExternalContext;
import org.springframework.faces.mvc.context.MvcFacesExecution;
import org.springframework.faces.mvc.navigation.NavigationRequestEvent;
import org.springframework.faces.mvc.servlet.FacesHandler;
import org.springframework.faces.mvc.support.MvcFacesStateHolderComponent;
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
	private MvcFacesExecution execution;
	private FacesHandler facesHandler;
	private Exception exception;
	private NavigationRequestEvent lastNavigationRequestEvent;
	private MutableAttributeMap requestScope = new LocalAttributeMap();
	private MutableAttributeMap flashScope = new LocalAttributeMap();
	private ExternalContext externalContext;

	// Late binding
	private MutableAttributeMap viewScope = null;

	/**
	 * Constructor.
	 * @param execution
	 * @param facesHandler
	 * @see #release()
	 */
	public MvcFacesRequestControlContextImpl(ExternalContext externalContext, MvcFacesExecution execution,
			FacesHandler facesHandler) {
		Assert.notNull(execution);
		Assert.notNull(facesHandler);
		this.externalContext = externalContext;
		this.execution = execution;
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

	public ExternalContext getExternalContext() {
		return externalContext;
	}

	public FacesHandler getFacesHandler() {
		return facesHandler;
	}

	public MvcFacesExecution getExecution() {
		return execution;
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
