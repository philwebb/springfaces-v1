/*
 * Copyright 2004-2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.faces.mvc;

import java.io.IOException;
import java.util.Map;

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.faces.mvc.support.MvcFacesContext;
import org.springframework.faces.mvc.support.MvcFacesRequestContext;
import org.springframework.faces.mvc.support.PageScopeHolderComponent;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.WebContentGenerator;

/**
 * Abstract base implementation of a MVC {@link HandlerAdapter} that can be used to process {@link FacesHandler}s.
 * 
 * @author Phillip Webb
 */
public abstract class AbstractFacesHandlerAdapter extends WebContentGenerator implements HandlerAdapter,
		BeanFactoryPostProcessor {

	public boolean supports(Object handler) {
		return handler instanceof FacesHandler;
	}

	public final ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		MvcFacesRequestContext mvcFacesRequestContext = new MvcFacesRequestContext(newFacesHandlerAdapterContext(),
				(FacesHandler) handler);
		try {
			return doHandle(request, response, (FacesHandler) handler);
		} finally {
			mvcFacesRequestContext.release();
		}
	}

	public long getLastModified(HttpServletRequest request, Object handler) {
		return -1;
	}

	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		if (isPageScopeSupported()) {
			beanFactory.registerScope("page", new PageScope());
		}
	}

	/**
	 * Internal method called to perform the actual handling of the request. The {@link MvcFacesRequestContext} will be
	 * active when this method is called.
	 * 
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @param handler handler to use. This object must have previously been passed to the <code>supports</code> method
	 * of this interface, which must have returned <code>true</code>.
	 * @throws Exception in case of errors
	 * @return ModelAndView object with the name of the view and the required model data, or <code>null</code> if the
	 * request has been handled directly
	 * 
	 * @see HandlerAdapter#handle(HttpServletRequest, HttpServletResponse, Object)
	 */
	protected abstract ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
			FacesHandler handler) throws Exception;

	/**
	 * Factory method used to construct the {@link MvcFacesContext} that will be used during request handling. By
	 * default this method returns an instance of {@link FacesHandlerAdapterContext}. Subclasses can override this
	 * method if required.
	 * 
	 * @return The {@link MvcFacesContext} instance that will be used during request handling.
	 */
	protected MvcFacesContext newFacesHandlerAdapterContext() {
		return new FacesHandlerAdapterContext();
	}

	/**
	 * Method called to determine if a page scope is supported. When page scope is supported it will be registered with
	 * the bean factory and a {@link PageScopeHolderComponent} will be attached when views are created. By default this
	 * method will return <tt>true</tt> so that {@link PageScope} can be supported.
	 * 
	 * @return <tt>true</tt> if page scope is supported.
	 */
	protected boolean isPageScopeSupported() {
		return true;
	}

	/**
	 * @return The {@link FacesViewIdResolver} that will be used to resolve faces view IDs.
	 */
	protected abstract FacesViewIdResolver getFacesViewIdResolver();

	/**
	 * @return The {@link ModelBindingExecutor} that will be used to bind the model.
	 */
	protected abstract ModelBindingExecutor getModelBindingExecutor();

	/**
	 * @return The {@link ActionUrlMapper} that will be used to map the action URL.
	 */
	protected abstract ActionUrlMapper getActionUrlMapper();

	protected abstract RedirectHandler getRedirectHandler();

	/**
	 * {@link MvcFacesContext} implementation for the adapter.
	 */
	protected class FacesHandlerAdapterContext implements MvcFacesContext {

		public String resolveViewId(String viewName) {
			return AbstractFacesHandlerAdapter.this.getFacesViewIdResolver().resolveViewId(viewName);
		}

		public String getActionUlr(FacesContext facesContext, String viewId) {
			String viewName = AbstractFacesHandlerAdapter.this.getFacesViewIdResolver().resolveViewName(viewId);
			return AbstractFacesHandlerAdapter.this.getActionUrlMapper().getActionUlr(facesContext, viewName);
		}

		public String getViewIdForRestore(FacesContext facesContext, String viewId) {
			String viewName = AbstractFacesHandlerAdapter.this.getActionUrlMapper().getViewNameForRestore(facesContext);
			if (viewName == null) {
				return null;
			}
			return AbstractFacesHandlerAdapter.this.getFacesViewIdResolver().resolveViewId(viewName);
		}

		public void viewCreated(FacesContext facesContext, MvcFacesRequestContext mvcFacesRequestContext,
				UIViewRoot view, Map model) {
			AbstractFacesHandlerAdapter.this.getModelBindingExecutor().storeModelToBind(facesContext, model);
			if (AbstractFacesHandlerAdapter.this.isPageScopeSupported()) {
				PageScopeHolderComponent.attach(facesContext, view);
			}
		}

		public void writeState(FacesContext facesContext) throws IOException {
			String viewId = facesContext.getViewRoot().getViewId();
			String viewName = AbstractFacesHandlerAdapter.this.getFacesViewIdResolver().resolveViewName(viewId);
			AbstractFacesHandlerAdapter.this.getActionUrlMapper().writeState(facesContext, viewName);
		}

		public void beforePhase(MvcFacesRequestContext mvcFacesRequestContext, PhaseEvent event) {
		}

		public void afterPhase(MvcFacesRequestContext mvcFacesRequestContext, PhaseEvent event) {
			if (PhaseId.RENDER_RESPONSE.equals(event.getPhaseId())) {
				AbstractFacesHandlerAdapter.this.getModelBindingExecutor().bindStoredModel(event.getFacesContext());
			}
		}

		public void redirect(FacesContext facesContext, Object location) throws IOException {
			AbstractFacesHandlerAdapter.this.getRedirectHandler().handleRedirect(facesContext, location);
		}
	}

}
