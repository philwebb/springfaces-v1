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
package org.springframework.faces.mvc.servlet;

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

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.OrderComparator;
import org.springframework.faces.mvc.bind.ModelBindingExecutor;
import org.springframework.faces.mvc.context.ExternalContext;
import org.springframework.faces.mvc.execution.ActionUrlMapper;
import org.springframework.faces.mvc.execution.ExecutionContextKey;
import org.springframework.faces.mvc.execution.MvcFacesExceptionHandler;
import org.springframework.faces.mvc.execution.MvcFacesExceptionOutcome;
import org.springframework.faces.mvc.execution.MvcFacesExecution;
import org.springframework.faces.mvc.execution.RequestContext;
import org.springframework.faces.mvc.execution.RequestControlContext;
import org.springframework.faces.mvc.execution.RequestControlContextImpl;
import org.springframework.faces.mvc.execution.repository.ExecutionContextRepository;
import org.springframework.faces.mvc.execution.repository.NoSuchExecutionException;
import org.springframework.faces.mvc.navigation.NavigationLocation;
import org.springframework.faces.mvc.servlet.support.HttpServletRequestEncodingScheme;
import org.springframework.faces.mvc.support.MvcFacesStateHolderComponent;
import org.springframework.faces.mvc.support.WebFlowExternalContextAdapter;
import org.springframework.faces.mvc.view.FacesViewIdResolver;
import org.springframework.js.ajax.AjaxHandler;
import org.springframework.js.ajax.SpringJavascriptAjaxHandler;
import org.springframework.util.Assert;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.WebContentGenerator;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.mvc.servlet.MvcExternalContext;

/**
 * Abstract base implementation of a MVC {@link HandlerAdapter} that can be used to process {@link FacesHandler}s.
 * 
 * @see FacesHandlerAdapter
 * 
 * @author Phillip Webb
 */
public abstract class AbstractFacesHandlerAdapter extends WebContentGenerator implements HandlerAdapter,
		ApplicationListener, InitializingBean {

	private boolean detectAllExceptionHandlers = true;
	private List userDefinedExceptionHandlers;
	private MvcFacesExceptionHandler[] allExceptionHandlers;
	private AjaxHandler ajaxHandler;
	private HttpServletRequestEncodingScheme urlEncodingScheme = new HttpServletRequestEncodingScheme();

	public long getLastModified(HttpServletRequest request, Object handler) {
		return -1;
	}

	public boolean supports(Object handler) {
		return handler instanceof FacesHandler;
	}

	public final ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		FacesHandler facesHandler = (FacesHandler) handler;
		ExternalContext externalContext = createExternalContext(request, response);
		RequestControlContextImpl requestContext = new RequestControlContextImpl(externalContext, newExecution(),
				facesHandler);
		try {
			restoreExecution(requestContext, request);
			try {
				doHandle(requestContext, request, response);
				return null;
			} catch (Exception e) {
				handleException(requestContext, request, response, e);
				return null;
			}
		} finally {
			requestContext.release();
		}
	}

	/**
	 * Restore the any store state for the flow execution.
	 */
	private void restoreExecution(RequestContext requestContext, HttpServletRequest request) {
		String encodedKey = getRedirectHandler().getExecutionContextKey(request);
		if (encodedKey != null) {
			ExecutionContextKey key = getExecutionContextRepository().parseKey(encodedKey);
			try {
				getExecutionContextRepository().restore(key, requestContext);
			} catch (NoSuchExecutionException e) {
				logger.warn("Unable to restore flashScope for MVC Faces request", e);
			}
		}
	}

	/**
	 * Creates the external context for the current HTTP servlet request.
	 * @param request the current request
	 * @param response the current response
	 */
	protected ExternalContext createExternalContext(HttpServletRequest request, HttpServletResponse response) {
		ServletExternalContext context = new MvcExternalContext(getServletContext(), request, response, null);
		context.setAjaxRequest(ajaxHandler.isAjaxRequest(request, response));
		return new WebFlowExternalContextAdapter(context);
	}

	/**
	 * Internal method called to perform the actual handling of the request. The {@link RequestContext} will be active
	 * when this method is called. This method is expected to completely handle the rendering of a response or throw an
	 * exception.
	 * @param requestContext The MVC Faces Request Context
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @throws Exception in the case of errors
	 * @see HandlerAdapter#handle(HttpServletRequest, HttpServletResponse, Object)
	 */
	protected abstract void doHandle(RequestContext requestContext, HttpServletRequest request,
			HttpServletResponse response) throws Exception;

	/**
	 * Method that is called when the handler throws an exception during processing.
	 * @param requestContext The MVC Faces Request Context
	 * @param request The request
	 * @param response The response
	 * @param exception The exception that was thrown
	 * @throws Exception
	 */
	protected void handleException(RequestContext requestContext, HttpServletRequest request,
			HttpServletResponse response, Exception exception) throws Exception {
		((RequestControlContext) requestContext).setException(exception);
		MvcFacesExceptionOutcomeImpl mvcFacesExceptionOutcome = new MvcFacesExceptionOutcomeImpl();
		// Try the handler specified exception handlers
		boolean handled = handleException(requestContext, request, response, exception, mvcFacesExceptionOutcome,
				requestContext.getFacesHandler().getExceptionHandlers());
		if (!handled) {
			handled = handleException(requestContext, request, response, exception, mvcFacesExceptionOutcome,
					allExceptionHandlers);
		}
		if (!handled) {
			throw exception;
		}
	}

	private boolean handleException(RequestContext requestContext, HttpServletRequest request,
			HttpServletResponse response, Exception exception, MvcFacesExceptionOutcomeImpl mvcFacesExceptionOutcome,
			MvcFacesExceptionHandler[] handlers) throws Exception {
		if (handlers == null || handlers.length == 0) {
			return false;
		}
		for (int i = 0; i < handlers.length; i++) {
			mvcFacesExceptionOutcome.reset();
			if (handlers[i].handleException(exception, requestContext, mvcFacesExceptionOutcome)) {
				mvcFacesExceptionOutcome.complete(requestContext, request, response);
				return true;
			}
		}
		return false;
	}

	protected void storeExecutionInRepositoryAndRedirect(RequestContext requestContext, HttpServletRequest request,
			HttpServletResponse response, NavigationLocation location) throws IOException {
		ExecutionContextKey key = getExecutionContextRepository().save(requestContext);
		String encoding = urlEncodingScheme.getEncodingScheme(request);
		getRedirectHandler().handleRedirect(ajaxHandler, encoding, request, response, location, key);
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
	 * Factory method used to construct the {@link MvcFacesExecution} that will be used during request handling. By
	 * default this method returns an instance of {@link FacesHandlerAdapterExecution}. Subclasses can override this
	 * method if required.
	 * @return The {@link MvcFacesExecution} instance that will be used during request handling
	 */
	protected MvcFacesExecution newExecution() {
		return new FacesHandlerAdapterExecution();
	}

	/**
	 * Returns the {@link FacesViewIdResolver} that will be used to resolve faces view IDs.
	 * @return The faces view ID resolver
	 */
	protected abstract FacesViewIdResolver getFacesViewIdResolver();

	/**
	 * Returns the {@link ModelBindingExecutor} that will be used to bind the model.
	 * @return The model binding executor
	 */
	protected abstract ModelBindingExecutor getModelBindingExecutor();

	/**
	 * Returns the {@link ActionUrlMapper} that will be used to map the action URL.
	 * @return The action URL mapper
	 */
	protected abstract ActionUrlMapper getActionUrlMapper();

	/**
	 * Returns the {@link RedirectHandler} that will be used to issue redirects.
	 * @return The redirect handler
	 */
	protected abstract RedirectHandler getRedirectHandler();

	/**
	 * Returns the {@link ExecutionContextRepository} that is used to store execution details accross redirects.
	 * @return The execution context repository
	 */
	protected abstract ExecutionContextRepository getExecutionContextRepository();

	/**
	 * Returns the configured Ajax handler that should be used to handle all AJAX requests. This will never return
	 * <tt>null</tt>.
	 * @return The non null ajax handler
	 */
	public AjaxHandler getAjaxHandler() {
		return ajaxHandler;
	}

	/**
	 * Set whether to detect all {@link MvcFacesExceptionHandler} beans in the application context. The default is
	 * <tt>true</tt> meaning that all {@link MvcFacesExceptionHandler}s will be dynamically located from the application
	 * context. If this behaviour is not required set this value to <tt>false</tt> and manually inject handlers using
	 * {@link #setExceptionHandlers(List)}.
	 * @param detectAllExceptionHandlers
	 */
	public void setDetectAllHandlerExceptionHandlers(boolean detectAllExceptionHandlers) {
		this.detectAllExceptionHandlers = detectAllExceptionHandlers;
	}

	/**
	 * Set a specific set of {@link MvcFacesExceptionHandler}s that will be used by this bean when
	 * {@link #setDetectAllHandlerExceptionHandlers(boolean)} has been set to <tt>false</tt>. Note: This property will
	 * be ignored when <tt>detectAllExceptionHandlers</tt> is <tt>true</tt>.
	 * @param exceptionHandlers
	 */
	public void setExceptionHandlers(List exceptionHandlers) {
		this.userDefinedExceptionHandlers = exceptionHandlers;
	}

	/**
	 * Sets the configured Ajax handler. This value cannot be <tt>null</tt>.
	 * @param ajaxHandler the ajax handler
	 */
	public void setAjaxHandler(AjaxHandler ajaxHandler) {
		Assert.notNull(ajaxHandler, "The ajaxHandler is required");
		this.ajaxHandler = ajaxHandler;
	}

	/**
	 * Set the character encoding scheme for URLs. Default is the request's encoding scheme (which is ISO-8859-1 if not
	 * specified otherwise).
	 * @param urlEncodingScheme The encoding scheme
	 */
	public void setUrlEncodingScheme(String urlEncodingScheme) {
		this.urlEncodingScheme.setEncodingScheme(urlEncodingScheme);
	}

	/**
	 * Returns the URL encoding as specified by the user or <tt>null</tt> if default encoding is being used.
	 * @return The encoding scheme
	 */
	public String getUrlEncodingScheme() {
		return this.urlEncodingScheme.getEncodingScheme();
	}

	/**
	 * {@link MvcFacesExecution} implementation for the adapter
	 */
	protected class FacesHandlerAdapterExecution implements MvcFacesExecution {

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

		public void viewCreated(FacesContext facesContext, RequestContext requestContext, UIViewRoot view, Map model) {
			AbstractFacesHandlerAdapter.this.getModelBindingExecutor().storeModelToBind(facesContext, model);
			MvcFacesStateHolderComponent.attach(facesContext, view);
		}

		public void writeState(FacesContext facesContext) throws IOException {
			String viewId = facesContext.getViewRoot().getViewId();
			String viewName = AbstractFacesHandlerAdapter.this.getFacesViewIdResolver().resolveViewName(viewId);
			AbstractFacesHandlerAdapter.this.getActionUrlMapper().writeState(facesContext, viewName);
		}

		private void stopAtProcessValidationsWhenHasCurrentException(RequestContext requestContext, PhaseEvent event) {
			if (PhaseId.PROCESS_VALIDATIONS.equals(event.getPhaseId()) && requestContext.getException() != null) {
				clearFlashScope(requestContext);
				event.getFacesContext().renderResponse();
			}
		}

		private void clearFlashScope(RequestContext requestContext) {
			requestContext.getFlashScope().clear();
		}

		public void beforePhase(RequestContext requestContext, PhaseEvent event) {
			if (PhaseId.RENDER_RESPONSE.equals(event.getPhaseId())) {
				AbstractFacesHandlerAdapter.this.getModelBindingExecutor().bindStoredModel(event.getFacesContext());
			}
			stopAtProcessValidationsWhenHasCurrentException(requestContext, event);
		}

		public void afterPhase(RequestContext requestContext, PhaseEvent event) {
			if (PhaseId.RENDER_RESPONSE.equals(event.getPhaseId())) {
				if (requestContext.getLastNavigationRequestEvent() == null) {
					clearFlashScope(requestContext);
				}
			}
		}

		public void redirect(FacesContext facesContext, RequestContext requestContext, NavigationLocation location)
				throws IOException {
			HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
			HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
			storeExecutionInRepositoryAndRedirect(requestContext, request, response, location);
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

		public void complete(RequestContext requestContext, HttpServletRequest request, HttpServletResponse response)
				throws Exception {
			if (redirectLocation != null && redisplay) {
				throw new IllegalStateException(
						"Illegal outcome specified, redirect or redisplay are mutually exclusive");
			}
			if (redirectLocation != null) {
				storeExecutionInRepositoryAndRedirect(requestContext, request, response, redirectLocation);
			}
			if (redisplay) {
				doHandle(requestContext, request, response);
			}
		}
	}
}
