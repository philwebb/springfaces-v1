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
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.faces.mvc.FacesHandler;
import org.springframework.faces.mvc.FacesHandlerAdapter;
import org.springframework.faces.mvc.MvcFacesExceptionHandler;
import org.springframework.faces.mvc.MvcFacesExceptionOutcome;
import org.springframework.faces.mvc.NavigationRequestEvent;
import org.springframework.faces.mvc.RedirectHandler;
import org.springframework.faces.mvc.bind.annotation.NavigationCase;
import org.springframework.faces.mvc.bind.annotation.NavigationRules;
import org.springframework.faces.mvc.stereotype.FacesController;
import org.springframework.faces.mvc.support.MvcFacesRequestContext;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.ClassUtils;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.support.WebArgumentResolver;
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

	private static final WebArgumentResolver[] ARGUMENT_RESOLVERS = new WebArgumentResolver[] { new FacesWebArgumentResolver() };

	private static final String DEFAULT_CONTROLLER_NAME = "controller";

	// FIXME @SessionAttributes support

	private UrlPathHelper urlPathHelper = new UrlPathHelper();

	private MethodNameResolver methodNameResolver = new InternalPathMethodNameResolver();

	private PathMatcher pathMatcher = new AntPathMatcher();

	private FacesHandlerAdapter facesHandlerAdapter;

	// FIXME setter
	private NavigationOutcomeExpressionResolver navigationOutcomeExpressionResolver = new NavigationOutcomeExpressionElResolver();

	private String exposedControllerName = DEFAULT_CONTROLLER_NAME;

	private final NavigationCaseAnnotationLocator navigationCaseAnnotationLocator = new NavigationCaseAnnotationLocator();

	private final Map<Class<?>, NavigationCaseMethodsResolver> methodResolverCache = new ConcurrentHashMap<Class<?>, NavigationCaseMethodsResolver>();

	private Set<BeanFactoryPostProcessor> postProcessors = new HashSet<BeanFactoryPostProcessor>();

	// Always order above other AnnotationMethodHandlerAdapter adapters so that they do not type and process faces
	// requests
	private int order = Ordered.HIGHEST_PRECEDENCE;

	private String beanName;

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

	public boolean supports(Object handler) {
		return supportsFaces(handler) && super.supports(handler);
	}

	protected FacesController getHandlerAnnotation(Object handler) {
		Class<?> handlerClass = ClassUtils.getUserClass(handler);
		return AnnotationUtils.findAnnotation(handlerClass, FacesController.class);
	}

	protected boolean supportsFaces(Object handler) {
		return getHandlerAnnotation(handler) != null;
	}

	protected ModelAndView createView(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		return super.handle(request, response, handler);
	}

	protected Object getNavigationOutcome(HttpServletRequest request, HttpServletResponse response,
			NavigationRequestEvent event, Object handler) throws Exception {
		NavigationCaseMethodsResolver methodResolver = getMethodsResolver(handler);
		ServletWebRequest servletWebRequest = new ServletWebRequest(request, response);
		Method[] navigationMethods = methodResolver.resolveNavigationMethods(request);
		FoundNavigationCase navigationCase = navigationCaseAnnotationLocator.findNavigationCase(navigationMethods,
				event);
		return navigationCase == null ? null : navigationCase.getOutcome(event, handler, servletWebRequest);
	}

	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		FacesHandler facesHandler = new AnnotatedMethodFacesHandlerAdapter(handler);
		return facesHandlerAdapter.handle(request, response, facesHandler);
	}

	/**
	 * Set the {@link FacesHandlerAdapter} that will be used to process the annotated controllers that are handled by
	 * this class. The {@link FacesHandlerAdapter} may be need to be set if additional configuration is required (for
	 * example is a specific {@link RedirectHandler} is required.
	 * <p>
	 * If this property is not injected the {@link ApplicationContext} will be used to locate a single
	 * {@link FacesHandlerAdapter} bean. If no bean is found a new {@link FacesHandlerAdapter} with default settings
	 * will be used.
	 *
	 * @param facesHandlerAdapter
	 */
	public void setFacesHandlerAdapter(FacesHandlerAdapter facesHandlerAdapter) {
		this.facesHandlerAdapter = facesHandlerAdapter;
	}

	private NavigationCaseMethodsResolver getMethodsResolver(Object handler) {
		Class<?> handlerClass = ClassUtils.getUserClass(handler);
		NavigationCaseMethodsResolver resolver = this.methodResolverCache.get(handlerClass);
		if (resolver == null) {
			resolver = new NavigationCaseMethodsResolver(handlerClass, urlPathHelper, methodNameResolver, pathMatcher);
			this.methodResolverCache.put(handlerClass, resolver);
		}
		return resolver;
	}

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

	public void setUrlPathHelper(UrlPathHelper urlPathHelper) {
		super.setUrlPathHelper(urlPathHelper);
		this.urlPathHelper = urlPathHelper;
	}

	public void setMethodNameResolver(MethodNameResolver methodNameResolver) {
		super.setMethodNameResolver(methodNameResolver);
		this.methodNameResolver = methodNameResolver;
	}

	public void setPathMatcher(PathMatcher pathMatcher) {
		super.setPathMatcher(pathMatcher);
		this.pathMatcher = pathMatcher;
	}

	public void setCustomArgumentResolver(WebArgumentResolver argumentResolver) {
		setCustomArgumentResolvers(new WebArgumentResolver[] { argumentResolver });
	}

	public void setCustomArgumentResolvers(WebArgumentResolver[] argumentResolvers) {
		if (argumentResolvers == null) {
			super.setCustomArgumentResolvers(ARGUMENT_RESOLVERS);
		} else {
			WebArgumentResolver[] completeArgumentResolvers = new WebArgumentResolver[ARGUMENT_RESOLVERS.length
					+ argumentResolvers.length];
			System.arraycopy(argumentResolvers, 0, completeArgumentResolvers, 0, argumentResolvers.length);
			System.arraycopy(ARGUMENT_RESOLVERS, 0, completeArgumentResolvers, argumentResolvers.length,
					ARGUMENT_RESOLVERS.length);
			super.setCustomArgumentResolvers(completeArgumentResolvers);
		}
	}

	public int getOrder() {
		return order;
	}

	/**
	 * Set the order of the adapter.
	 * @param order
	 */
	public void setOrder(int order) {
		this.order = order;
	}

	/**
	 * Adapter class to convert the annotated handler into a {@link FacesController}. Also implements
	 * {@link MvcFacesExceptionHandler} to deal with navigation based exception handling.
	 */
	private class AnnotatedMethodFacesHandlerAdapter implements FacesHandler, MvcFacesExceptionHandler {

		private Object handler;
		private String exposedControllerName;

		public AnnotatedMethodFacesHandlerAdapter(Object handler) {
			super();
			this.handler = handler;
			FacesController annotation = FacesAnnotationMethodHandlerAdapter.this.getHandlerAnnotation(handler);
			if (annotation.exposeController()) {
				exposedControllerName = (StringUtils.hasLength(annotation.controllerName()) ? annotation
						.controllerName() : FacesAnnotationMethodHandlerAdapter.this.exposedControllerName);
			}
		}

		public ModelAndView createView(FacesContext facesContext) throws Exception {
			HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
			HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
			return FacesAnnotationMethodHandlerAdapter.this.createView(request, response, handler);
		}

		public Object getNavigationOutcomeLocation(FacesContext facesContext, NavigationRequestEvent event)
				throws Exception {
			HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
			HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
			Object outcome = FacesAnnotationMethodHandlerAdapter.this.getNavigationOutcome(request, response, event,
					handler);
			outcome = navigationOutcomeExpressionResolver.resolveNavigationOutcome(outcome);
			// FIXME expression resolve the outcome
			// FIXME URL encode?
			return outcome;
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
			Object location = FacesAnnotationMethodHandlerAdapter.this.getNavigationOutcome(request, response, event,
					handler);
			if (location != null) {
				outcome.redirect(location);
				return true;
			}
			return false;
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
}
