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
package org.springframework.faces.mvc.annotation;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.faces.mvc.annotation.support.FacesControllerAnnotatedMethodInvoker;
import org.springframework.faces.mvc.annotation.support.FacesControllerAnnotatedMethodInvokerFactory;
import org.springframework.faces.mvc.annotation.support.FacesWebArgumentResolvers;
import org.springframework.faces.mvc.annotation.support.FoundNavigationCase;
import org.springframework.faces.mvc.annotation.support.NavigationCaseAnnotationLocator;
import org.springframework.faces.mvc.annotation.support.NavigationCaseMethodResolver;
import org.springframework.faces.mvc.annotation.support.RequestMappingMethodResolver;
import org.springframework.faces.mvc.execution.MvcFacesExceptionHandler;
import org.springframework.faces.mvc.execution.MvcFacesExceptionOutcome;
import org.springframework.faces.mvc.execution.MvcFacesRequestContext;
import org.springframework.faces.mvc.navigation.NavigationLocation;
import org.springframework.faces.mvc.navigation.NavigationOutcomeExpressionContext;
import org.springframework.faces.mvc.navigation.NavigationOutcomeExpressionElResolver;
import org.springframework.faces.mvc.navigation.NavigationOutcomeExpressionResolver;
import org.springframework.faces.mvc.navigation.NavigationRequestEvent;
import org.springframework.faces.mvc.navigation.RedirectHandler;
import org.springframework.faces.mvc.navigation.annotation.NavigationCase;
import org.springframework.faces.mvc.navigation.annotation.NavigationRules;
import org.springframework.faces.mvc.servlet.FacesHandler;
import org.springframework.faces.mvc.servlet.FacesHandlerAdapter;
import org.springframework.faces.mvc.stereotype.FacesController;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.support.WebArgumentResolver;
import org.springframework.web.bind.support.WebBindingInitializer;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter;
import org.springframework.web.servlet.mvc.multiaction.InternalPathMethodNameResolver;
import org.springframework.web.servlet.mvc.multiaction.MethodNameResolver;
import org.springframework.web.util.UrlPathHelper;

/**
 * Implementation of the {@link HandlerAdapter} interface that maps JSF requests to {@link FacesController} annotated
 * classes using methods based on HTTP paths, HTTP methods and request parameters expressed through the
 * {@link RequestMapping} annotation. This adapter will also handle JSF navigation outcomes using the
 * {@link NavigationCase} and {@link NavigationRules} annotations.
 * <p>
 * Supports the {@link ModelAttribute} annotation for exposing model attribute values to JSF.
 * 
 * @author Phillip Webb
 * 
 * @see #setPathMatcher
 * @see #setMethodNameResolver
 * @see #setWebBindingInitializer
 * @see #setSessionAttributeStore
 * @see FacesHandlerAdapter
 */
public class FacesAnnotationMethodHandlerAdapter extends AnnotationMethodHandlerAdapter implements InitializingBean,
		BeanNameAware, BeanFactoryPostProcessor, Ordered {

	private static final String DEFAULT_CONTROLLER_NAME = "controller";

	private UrlPathHelper urlPathHelper = new UrlPathHelper();

	private MethodNameResolver methodNameResolver = new InternalPathMethodNameResolver();

	private PathMatcher pathMatcher = new AntPathMatcher();

	private HandlerAdapter facesHandlerAdapter;

	private NavigationOutcomeExpressionResolver navigationOutcomeExpressionResolver = new NavigationOutcomeExpressionElResolver();

	private String exposedControllerName = DEFAULT_CONTROLLER_NAME;

	private boolean exposeController = true;

	private final NavigationCaseAnnotationLocator navigationCaseAnnotationLocator = new NavigationCaseAnnotationLocator();

	private final Map<Class<?>, NavigationCaseMethodResolver> methodResolverCache = new ConcurrentHashMap<Class<?>, NavigationCaseMethodResolver>();

	private Set<BeanFactoryPostProcessor> postProcessors = new HashSet<BeanFactoryPostProcessor>();

	// order above other AnnotationMethodHandlerAdapter adapters so that they do not try and process faces requests
	private int order = Ordered.HIGHEST_PRECEDENCE;

	private String beanName;

	private WebBindingInitializer webBindingInitializer;

	private ParameterNameDiscoverer parameterNameDiscoverer;

	private WebArgumentResolver[] completeArgumentResolvers;

	public FacesAnnotationMethodHandlerAdapter() {
		super();
		setCustomArgumentResolvers(null);
	}

	/**
	 * Trigger all post-processors and spring callbacks for internally managed beans.
	 * @param bean The internal bean
	 * @throws Exception
	 */
	private void initializeInternalBean(Object bean) throws Exception {
		getApplicationContext().getAutowireCapableBeanFactory().initializeBean(bean,
				"_" + beanName + "_" + bean.getClass().getSimpleName());
		if (bean instanceof BeanFactoryPostProcessor) {
			postProcessors.add((BeanFactoryPostProcessor) bean);
		}
	}

	/**
	 * Returns the {@link FacesController} annotation from the specified handler or <tt>null</tt> if no annotation can
	 * be found.
	 * @param handler The handler
	 * @return The {@link FacesController} annotation or <tt>null</tt>
	 */
	protected FacesController getHandlerAnnotation(Object handler) {
		Class<?> handlerClass = ClassUtils.getUserClass(handler);
		return AnnotationUtils.findAnnotation(handlerClass, FacesController.class);
	}

	public boolean supports(Object handler) {
		return supportsFaces(handler) && super.supports(handler);
	}

	/**
	 * Determine if the handler can be used with this adapter. By default this method accepts all handlers that contain
	 * the {@link FacesController} annotation.
	 * @param handler The handler
	 * @return <tt>true</tt> if the handler is supported by this adapter.
	 * @see #getHandlerAnnotation(Object)
	 */
	protected boolean supportsFaces(Object handler) {
		return getHandlerAnnotation(handler) != null;
	}

	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		FacesHandler facesHandler = new AnnotatedMethodFacesHandlerAdapter(handler);
		if (!facesHandlerAdapter.supports(facesHandler)) {
			throw new IllegalStateException("The facesHandlerAdapter " + facesHandlerAdapter.getClass()
					+ " does not support FacesHandler objects, possible misconfiguration of setFacesHandlerAdapter");
		}
		return facesHandlerAdapter.handle(request, response, facesHandler);
	}

	/**
	 * Delegate method called from {@link FacesHandler#createView(FacesContext)} in order to create the
	 * {@link ModelAndView} that should be used when rendering the response.
	 * @param request The request.
	 * @param response The response
	 * @param handler The handler
	 * @return The model and view data for this request
	 * @throws Exception on error
	 */
	protected final ModelAndView createView(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		return super.handle(request, response, handler);
	}

	/**
	 * Delegate method called from
	 * {@link FacesHandler#getNavigationOutcomeLocation(FacesContext, NavigationRequestEvent)} in order to determine the
	 * outcome of a navigation event. By default this implementation will use the
	 * {@link NavigationCaseAnnotationLocator} to locate and process {@link NavigationCase} and {@link NavigationRules}
	 * annotations.
	 * @param request The request
	 * @param response The response
	 * @param event The JSF navigation event
	 * @param handler The handler
	 * @return A {@link NavigationLocation} or <tt>null</tt>
	 * @throws Exception on error
	 */
	protected final NavigationLocation getNavigationOutcome(HttpServletRequest request, HttpServletResponse response,
			NavigationRequestEvent event, Object handler) throws Exception {
		NavigationCaseMethodResolver methodResolver = getMethodResolver(handler);
		ServletWebRequest webRequest = new ServletWebRequest(request, response);
		Method[] navigationMethods = methodResolver.resolveNavigationMethods(request);
		FoundNavigationCase navigationCase = navigationCaseAnnotationLocator.findNavigationCase(handler,
				navigationMethods, event);
		NavigationOutcomeExpressionContextImpl context = new NavigationOutcomeExpressionContextImpl(handler,
				webRequest, methodResolver);
		NavigationLocation outcome = navigationCase == null ? null : navigationCase.getOutcome(event, handler,
				webRequest, context);
		outcome = navigationOutcomeExpressionResolver.resolveNavigationOutcome(context, outcome);
		return outcome;
	}

	private NavigationCaseMethodResolver getMethodResolver(Object handler) {
		Class<?> handlerClass = ClassUtils.getUserClass(handler);
		NavigationCaseMethodResolver resolver = this.methodResolverCache.get(handlerClass);
		if (resolver == null) {
			resolver = new NavigationCaseMethodResolver(handlerClass, urlPathHelper, methodNameResolver, pathMatcher);
			this.methodResolverCache.put(handlerClass, resolver);
		}
		return resolver;
	}

	/**
	 * Set the {@link HandlerAdapter} that will be used to process the annotated controllers that are handled by this
	 * class. The {@link FacesHandlerAdapter} may be need to be set if additional configuration is required (for example
	 * is a specific {@link RedirectHandler} is required).
	 * <p>
	 * If this property is not injected the {@link ApplicationContext} will be used to locate a single
	 * {@link FacesHandlerAdapter} bean. If no bean is found a new {@link FacesHandlerAdapter} with default settings
	 * will be used.
	 * <p>
	 * Note: The handler adapter set here must support {@link FacesHandler} instances.
	 * @param facesHandlerAdapter
	 */
	public void setFacesHandlerAdapter(HandlerAdapter facesHandlerAdapter) {
		this.facesHandlerAdapter = facesHandlerAdapter;
	}

	/**
	 * @return The {@link HandlerAdapter} that will be used to process the requests.
	 * @see #setFacesHandlerAdapter(HandlerAdapter)
	 */
	protected final HandlerAdapter getFacesHandlerAdapter() {
		return facesHandlerAdapter;
	}

	@SuppressWarnings("unchecked")
	public void afterPropertiesSet() throws Exception {
		if (facesHandlerAdapter == null) {
			Map<String, FacesHandlerAdapter> facesHandlerAdapters = getApplicationContext().getBeansOfType(
					FacesHandlerAdapter.class);
			if (facesHandlerAdapters.size() > 1) {
				throw new IllegalStateException(
						"Multiple FacesHandlerAdapters found in application context, please manually inject "
								+ "a FacesHanlderAdapter using the setFacesHandlerAdapter method");
			} else if (facesHandlerAdapters.size() == 1) {
				facesHandlerAdapter = facesHandlerAdapters.values().iterator().next();
			} else {
				facesHandlerAdapter = new FacesHandlerAdapter();
				initializeInternalBean(facesHandlerAdapter);
			}
		}
	}

	public void setBeanName(String name) {
		this.beanName = name;
	}

	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		for (BeanFactoryPostProcessor postProcessor : postProcessors) {
			postProcessor.postProcessBeanFactory(beanFactory);
		}
	}

	public void setUrlPathHelper(UrlPathHelper urlPathHelper) {
		super.setUrlPathHelper(urlPathHelper);
		this.urlPathHelper = urlPathHelper;
	}

	protected final UrlPathHelper getUrlPathHelper() {
		return urlPathHelper;
	}

	public void setMethodNameResolver(MethodNameResolver methodNameResolver) {
		super.setMethodNameResolver(methodNameResolver);
		this.methodNameResolver = methodNameResolver;
	}

	protected final MethodNameResolver getMethodNameResolver() {
		return methodNameResolver;
	}

	public void setPathMatcher(PathMatcher pathMatcher) {
		super.setPathMatcher(pathMatcher);
		this.pathMatcher = pathMatcher;
	}

	protected final PathMatcher getPathMatcher() {
		return pathMatcher;
	}

	public void setWebBindingInitializer(WebBindingInitializer webBindingInitializer) {
		super.setWebBindingInitializer(webBindingInitializer);
		this.webBindingInitializer = webBindingInitializer;
	}

	protected final WebBindingInitializer getWebBindingInitializer() {
		return webBindingInitializer;
	}

	public void setParameterNameDiscoverer(ParameterNameDiscoverer parameterNameDiscoverer) {
		super.setParameterNameDiscoverer(parameterNameDiscoverer);
		this.parameterNameDiscoverer = parameterNameDiscoverer;
	}

	protected final ParameterNameDiscoverer getParameterNameDiscoverer() {
		return parameterNameDiscoverer;
	}

	public void setCustomArgumentResolver(WebArgumentResolver argumentResolver) {
		setCustomArgumentResolvers(argumentResolver == null ? null : new WebArgumentResolver[] { argumentResolver });
	}

	public void setCustomArgumentResolvers(WebArgumentResolver[] argumentResolvers) {
		this.completeArgumentResolvers = FacesWebArgumentResolvers.mergeWithFacesResolvers(argumentResolvers);
		super.setCustomArgumentResolvers(completeArgumentResolvers);
	}

	/**
	 * Set a {@link NavigationOutcomeExpressionResolver} that will be used to resolve any expressions contained in the
	 * {@link NavigationCase} annotations.
	 * @param navigationOutcomeExpressionResolver
	 */
	public void setNavigationOutcomeExpressionResolver(
			NavigationOutcomeExpressionResolver navigationOutcomeExpressionResolver) {
		Assert.notNull(navigationOutcomeExpressionResolver, "The navigationOutcomeExpressionResolver is required");
		this.navigationOutcomeExpressionResolver = navigationOutcomeExpressionResolver;
	}

	/**
	 * Set the name of the variable that is used to expose the running {@link FacesController} to JSF. Note: this name
	 * will only be used when the annotation {@link FacesController#controllerName()} is not specified.
	 * <p>
	 * If a controller should not be exposed as a JSF variable the {@link FacesController#exposeController()} annotation
	 * parameter can be used on a single controller, or {@link #setExposeController(boolean)} can be set to effect all
	 * controllers.
	 * <p>
	 * When not specified the controller will be exposed using the variable name <tt>controller<tt>.
	 * @param exposedControllerName The name of variable that will be used to expose the {@link FacesController}
	 * @see FacesController#controllerName()
	 * @see FacesController#exposeController()
	 * @see #setExposeController(boolean)
	 */
	public void setExposedControllerName(String exposedControllerName) {
		Assert.notNull(exposedControllerName, "The exposedControllerName is required");
		this.exposedControllerName = exposedControllerName;
	}

	/**
	 * Determine if the running {@link FacesController} should be exposed as a JSF variable. If either this property or
	 * the {@link FacesController#exposeController()} parameter is <tt>false</tt> the controller will not be exposed as
	 * a JSF variable.
	 * <p>
	 * The default setting is to expose controllers using the variable name <tt>controller</tt>.
	 * @param exposeController If the controller should be exposed as a JSF variable
	 * @see FacesController#exposeController()
	 * @see #setExposedControllerName(String)
	 */
	public void setExposeController(boolean exposeController) {
		this.exposeController = exposeController;
	}

	/**
	 * Set the order of the adapter.
	 * @param order
	 */
	public void setOrder(int order) {
		this.order = order;
	}

	public int getOrder() {
		return order;
	}

	/**
	 * Adapter class to convert the annotated handler into a {@link FacesHandler}. Also implements
	 * {@link MvcFacesExceptionHandler} to deal with navigation based exception handling.
	 */
	private class AnnotatedMethodFacesHandlerAdapter implements FacesHandler, MvcFacesExceptionHandler {

		private Object handler;
		private String exposedControllerName;

		public AnnotatedMethodFacesHandlerAdapter(Object handler) {
			super();
			this.handler = handler;
			FacesController annotation = FacesAnnotationMethodHandlerAdapter.this.getHandlerAnnotation(handler);
			if (FacesAnnotationMethodHandlerAdapter.this.exposeController && annotation.exposeController()) {
				exposedControllerName = (StringUtils.hasLength(annotation.controllerName()) ? annotation
						.controllerName() : FacesAnnotationMethodHandlerAdapter.this.exposedControllerName);
			}
		}

		public ModelAndView createView(FacesContext facesContext) throws Exception {
			HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
			HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
			return FacesAnnotationMethodHandlerAdapter.this.createView(request, response, handler);
		}

		public NavigationLocation getNavigationOutcomeLocation(FacesContext facesContext, NavigationRequestEvent event)
				throws Exception {
			HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
			HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
			return FacesAnnotationMethodHandlerAdapter.this.getNavigationOutcome(request, response, event, handler);
		}

		public Object resolveVariable(String variableName) {
			if (exposedControllerName != null && exposedControllerName.equals(variableName)) {
				return handler;
			}
			return null;
		}

		public MvcFacesExceptionHandler[] getExceptionHandlers() {
			return new MvcFacesExceptionHandler[] { this };
		}

		public boolean handleException(Exception exception, MvcFacesRequestContext requestContext,
				HttpServletRequest request, HttpServletResponse response, MvcFacesExceptionOutcome outcome)
				throws Exception {
			requestContext.getFacesHandler();
			NavigationRequestEvent event = new NavigationRequestEvent(this, requestContext
					.getLastNavigationRequestEvent(), exception);
			NavigationLocation location = FacesAnnotationMethodHandlerAdapter.this.getNavigationOutcome(request,
					response, event, handler);
			if (location != null) {
				outcome.redirect(location);
				return true;
			}
			return false;
		}
	}

	/**
	 * Internal {@link FacesControllerAnnotatedMethodInvoker} implementation.
	 */
	private class AnnotatedMethodInvoker extends FacesControllerAnnotatedMethodInvoker {

		public AnnotatedMethodInvoker(RequestMappingMethodResolver resolver,
				WebArgumentResolver[] additionalArgumentResolvers) {
			super(resolver, webBindingInitializer, parameterNameDiscoverer, FacesWebArgumentResolvers.mergeResolvers(
					completeArgumentResolvers, additionalArgumentResolvers));
		}

		protected WebDataBinder createBinder(NativeWebRequest webRequest, Object target, String objectName)
				throws Exception {
			return FacesAnnotationMethodHandlerAdapter.this.createBinder((HttpServletRequest) webRequest
					.getNativeRequest(), target, objectName);
		}

		public WebDataBinder createDataBinder(Object handler, NativeWebRequest webRequest, String attrName,
				Object target, String objectName) throws Exception {
			WebDataBinder binder = createBinder(webRequest, target, objectName);
			initBinder(handler, attrName, binder, webRequest);
			return binder;
		}
	}

	/**
	 * Internal {@link NavigationOutcomeExpressionContext} implementation.
	 */
	private class NavigationOutcomeExpressionContextImpl implements NavigationOutcomeExpressionContext,
			FacesControllerAnnotatedMethodInvokerFactory {

		private NativeWebRequest webRequest;
		private AnnotatedMethodInvoker dataBinderMethodInvoker;
		private NavigationCaseMethodResolver methodResolver;
		private Object handler;

		public NavigationOutcomeExpressionContextImpl(Object handler, NativeWebRequest webRequest,
				NavigationCaseMethodResolver methodResolver) {
			this.handler = handler;
			this.webRequest = webRequest;
			this.methodResolver = methodResolver;
		}

		public NativeWebRequest getWebRequest() {
			return webRequest;
		}

		public WebDataBinder createDataBinder(String attrName, Object target, String objectName) throws Exception {
			if (dataBinderMethodInvoker == null) {
				dataBinderMethodInvoker = new AnnotatedMethodInvoker(methodResolver, null);
			}
			return dataBinderMethodInvoker.createDataBinder(handler, webRequest, attrName, target, objectName);
		}

		public FacesControllerAnnotatedMethodInvoker newInvoker(WebArgumentResolver... additionalArgumentResolvers) {
			return new AnnotatedMethodInvoker(methodResolver, additionalArgumentResolvers);
		}
	}
}
