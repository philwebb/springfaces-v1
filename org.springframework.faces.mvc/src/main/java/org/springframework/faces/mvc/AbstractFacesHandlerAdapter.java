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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.OrderComparator;
import org.springframework.faces.mvc.support.MvcFacesContext;
import org.springframework.faces.mvc.support.MvcFacesRequestContext;
import org.springframework.faces.mvc.support.MvcFacesRequestContextControl;
import org.springframework.faces.mvc.support.MvcFacesRequestContextControlImpl;
import org.springframework.faces.mvc.support.PageScopeHolderComponent;
import org.springframework.js.ajax.AjaxHandler;
import org.springframework.js.ajax.SpringJavascriptAjaxHandler;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.WebContentGenerator;

/**
 * Abstract base implementation of a MVC {@link HandlerAdapter} that can be used to process {@link FacesHandler}s.
 * 
 * @see FacesHandlerAdapter
 * 
 * @author Phillip Webb
 */
public abstract class AbstractFacesHandlerAdapter extends WebContentGenerator implements HandlerAdapter,
		BeanFactoryPostProcessor, ApplicationListener, InitializingBean {

	private boolean detectAllExceptionHandlers = true;
	private List userDefinedExceptionHandlers;
	private MvcFacesExceptionHandler[] allExceptionHandlers;
	private AjaxHandler ajaxHandler;

	public long getLastModified(HttpServletRequest request, Object handler) {
		return -1;
	}

	public boolean supports(Object handler) {
		return handler instanceof FacesHandler;
	}

	public final ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		FacesHandler facesHandler = (FacesHandler) handler;
		MvcFacesRequestContextControlImpl mvcFacesRequestContext = new MvcFacesRequestContextControlImpl(
				newFacesHandlerAdapterContext(), facesHandler);
		try {
			try {
				doHandle(mvcFacesRequestContext, request, response);
				return null;
			} catch (Exception e) {
				handleException(mvcFacesRequestContext, request, response, e);
				return null;
			}
		} finally {
			mvcFacesRequestContext.release();
		}
	}

	/**
	 * Internal method called to perform the actual handling of the request. The {@link MvcFacesRequestContext} will be
	 * active when this method is called. This method is expected to completely handle the rendering of a response or
	 * throw an exception.
	 * 
	 * @param mvcFacesRequestContext The MVC Faces Request Context
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @throws Exception in case of errors
	 * 
	 * @see HandlerAdapter#handle(HttpServletRequest, HttpServletResponse, Object)
	 */
	protected abstract void doHandle(MvcFacesRequestContext mvcFacesRequestContext, HttpServletRequest request,
			HttpServletResponse response) throws Exception;

	/**
	 * Method that is called when the handler throws an exception during processing.
	 * 
	 * @param mvcFacesRequestContext The MVC Faces Request Context
	 * @param request The request
	 * @param response The response
	 * @param exception The exception that was thrown
	 * @throws Exception
	 */
	protected void handleException(MvcFacesRequestContext mvcFacesRequestContext, HttpServletRequest request,
			HttpServletResponse response, Exception exception) throws Exception {
		((MvcFacesRequestContextControl) mvcFacesRequestContext).setException(exception);
		MvcFacesExceptionOutcomeImpl mvcFacesExceptionOutcome = new MvcFacesExceptionOutcomeImpl();
		// Try the handler specified exception handlers
		boolean handled = handleException(mvcFacesRequestContext, request, response, exception,
				mvcFacesExceptionOutcome, mvcFacesRequestContext.getFacesHandler().getExceptionHandlers());
		if (!handled) {
			handled = handleException(mvcFacesRequestContext, request, response, exception, mvcFacesExceptionOutcome,
					allExceptionHandlers);
		}
		if (!handled) {
			throw exception;
		}
	}

	private boolean handleException(MvcFacesRequestContext mvcFacesRequestContext, HttpServletRequest request,
			HttpServletResponse response, Exception exception, MvcFacesExceptionOutcomeImpl mvcFacesExceptionOutcome,
			MvcFacesExceptionHandler[] handlers) throws Exception {
		if (handlers == null || handlers.length == 0) {
			return false;
		}
		for (int i = 0; i < handlers.length; i++) {
			mvcFacesExceptionOutcome.reset();
			if (handlers[i].handleException(exception, mvcFacesRequestContext, request, response,
					mvcFacesExceptionOutcome)) {
				mvcFacesExceptionOutcome.complete(mvcFacesRequestContext, request, response);
				return true;
			}
		}
		return false;
	}

	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		// FIXME don't auto-register scopes create a new registrar
		if (isPageScopeSupported()) {
			beanFactory.registerScope("page", new PageScope());
		}
	}

	public void afterPropertiesSet() throws Exception {
		ajaxHandler = (ajaxHandler == null ? new SpringJavascriptAjaxHandler() : ajaxHandler);
	}

	public void onApplicationEvent(ApplicationEvent event) {
		if (event instanceof ContextRefreshedEvent) {
			onRefresh(((ContextRefreshedEvent) event).getApplicationContext());
		}
	}

	/**
	 * Method called on a {@link ContextRefreshedEvent}.
	 * 
	 * @param context
	 */
	protected void onRefresh(ApplicationContext context) {
		initExceptionHandlers(context);
	}

	private void initExceptionHandlers(ApplicationContext context) {
		allExceptionHandlers = null;
		if (detectAllExceptionHandlers) {
			// Find all HandlerExceptionResolvers in the ApplicationContext, including ancestor contexts.
			Map matchingBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(context, MvcFacesExceptionHandler.class,
					true, false);
			if (!matchingBeans.isEmpty()) {
				ArrayList detectedExceptionHandlers = new ArrayList(matchingBeans.values());
				Collections.sort(detectedExceptionHandlers, new OrderComparator());
				allExceptionHandlers = (MvcFacesExceptionHandler[]) detectedExceptionHandlers
						.toArray(new MvcFacesExceptionHandler[] {});
			}
		} else {
			if (userDefinedExceptionHandlers != null) {
				allExceptionHandlers = (MvcFacesExceptionHandler[]) userDefinedExceptionHandlers
						.toArray(new MvcFacesExceptionHandler[] {});
			}
		}
	}

	/**
	 * Factory method used to construct the {@link MvcFacesContext} that will be used during request handling. By
	 * default this method returns an instance of {@link FacesHandlerAdapterContext}. Subclasses can override this
	 * method if required.
	 * 
	 * @return The {@link MvcFacesContext} instance that will be used during request handling
	 */
	protected MvcFacesContext newFacesHandlerAdapterContext() {
		return new FacesHandlerAdapterContext();
	}

	/**
	 * Method called to determine if a page scope is supported. When page scope is supported it will be registered with
	 * the bean factory and a {@link PageScopeHolderComponent} will be attached when views are created. By default this
	 * method will return <tt>true</tt> so that {@link PageScope} can be supported.
	 * 
	 * @return <tt>true</tt> if page scope is supported
	 */
	protected boolean isPageScopeSupported() {
		// FIXME disable this and always include the PageScope component
		return true;
	}

	/**
	 * @return The {@link FacesViewIdResolver} that will be used to resolve faces view IDs
	 */
	protected abstract FacesViewIdResolver getFacesViewIdResolver();

	/**
	 * @return The {@link ModelBindingExecutor} that will be used to bind the model
	 */
	protected abstract ModelBindingExecutor getModelBindingExecutor();

	/**
	 * @return The {@link ActionUrlMapper} that will be used to map the action URL
	 */
	protected abstract ActionUrlMapper getActionUrlMapper();

	/**
	 * @return The {@link RedirectHandler} that will be used to issue redirects
	 */
	protected abstract RedirectHandler getRedirectHandler();

	/**
	 * @return The configured Ajax handler
	 */
	public AjaxHandler getAjaxHandler() {
		return ajaxHandler;
	}

	/**
	 * Set whether to detect all {@link MvcFacesExceptionHandler} beans in the application context. The default is
	 * <tt>true</tt> meaning that all {@link MvcFacesExceptionHandler}s will be dynamically located from the application
	 * context. If this behaviour is not required set this value to <tt>false</tt> and manually inject handler using
	 * {@link #setExceptionHandlers(List)}.
	 */
	public void setDetectAllHandlerExceptionHandlers(boolean detectAllExceptionHandlers) {
		this.detectAllExceptionHandlers = detectAllExceptionHandlers;
	}

	/**
	 * Set a specific set of {@link MvcFacesExceptionHandler}s that will be used by this bean then
	 * {@link #setDetectAllHandlerExceptionHandlers(boolean)} has been set to <tt>false</tt>. Note: This property will
	 * be ignored when <tt>detectAllExceptionHandlers</tt> is true.
	 * @param exceptionHandlers
	 */
	public void setExceptionHandlers(List exceptionHandlers) {
		this.userDefinedExceptionHandlers = exceptionHandlers;
	}

	/**
	 * Sets the configured Ajax handler.
	 * @param ajaxHandler the ajax handler
	 */
	public void setAjaxHandler(AjaxHandler ajaxHandler) {
		this.ajaxHandler = ajaxHandler;
	}

	/**
	 * {@link MvcFacesContext} implementation for the adapter
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

		private void stopAtProcessValidationsWhenHasCurrentException(MvcFacesRequestContext mvcFacesRequestContext,
				PhaseEvent event) {
			if (PhaseId.PROCESS_VALIDATIONS.equals(event.getPhaseId()) && mvcFacesRequestContext.getException() != null) {
				clearFlashScope(mvcFacesRequestContext);
				event.getFacesContext().renderResponse();
			}
		}

		private void clearFlashScope(MvcFacesRequestContext mvcFacesRequestContext) {
			mvcFacesRequestContext.getFlashScope().clear();
		}

		public void beforePhase(MvcFacesRequestContext mvcFacesRequestContext, PhaseEvent event) {
			if (PhaseId.RENDER_RESPONSE.equals(event.getPhaseId())) {
				AbstractFacesHandlerAdapter.this.getModelBindingExecutor().bindStoredModel(event.getFacesContext());
			}
			stopAtProcessValidationsWhenHasCurrentException(mvcFacesRequestContext, event);
		}

		public void afterPhase(MvcFacesRequestContext mvcFacesRequestContext, PhaseEvent event) {
			if (PhaseId.RENDER_RESPONSE.equals(event.getPhaseId())) {
				if (mvcFacesRequestContext.getLastNavigationRequestEvent() == null) {
					clearFlashScope(mvcFacesRequestContext);
				}
			}
		}

		public void redirect(FacesContext facesContext, MvcFacesRequestContext requestContext,
				NavigationLocation location) throws IOException {
			HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
			HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
			AbstractFacesHandlerAdapter.this.getRedirectHandler().handleRedirect(ajaxHandler, request, response,
					location);
		}
	}

	/**
	 * Internal implementation of {@link MvcFacesExceptionOutcome}.
	 */
	private class MvcFacesExceptionOutcomeImpl implements MvcFacesExceptionOutcome {

		private NavigationLocation redirectLocation;
		private boolean redisplay;

		private void reset() {
			this.redirectLocation = null;
			this.redisplay = false;
		}

		public void redirect(NavigationLocation location) {
			this.redirectLocation = location;
		}

		public void redisplay() {
			this.redisplay = true;
		}

		public void complete(MvcFacesRequestContext mvcFacesRequestContext, HttpServletRequest request,
				HttpServletResponse response) throws Exception {
			if (redirectLocation != null && redisplay) {
				throw new IllegalStateException(
						"Illegal outcome specified, redirect or redisplay are mutually exclusive");
			}
			if (redirectLocation != null) {
				getRedirectHandler().handleRedirect(ajaxHandler, request, response, redirectLocation);
			}
			if (redisplay) {
				doHandle(mvcFacesRequestContext, request, response);
			}
		}
	}
}
