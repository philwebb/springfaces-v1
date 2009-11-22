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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.faces.mvc.FacesHandler;
import org.springframework.faces.mvc.FacesHandlerAdapter;
import org.springframework.faces.mvc.NavigationRequestEvent;
import org.springframework.faces.mvc.RedirectHandler;
import org.springframework.faces.mvc.bind.annotation.NavigationCase;
import org.springframework.faces.mvc.bind.annotation.NavigationRules;
import org.springframework.faces.mvc.bind.stereotype.FacesController;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.ClassUtils;
import org.springframework.util.PathMatcher;
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
		Ordered {

	private static final WebArgumentResolver[] ARGUMENT_RESOLVERS = new WebArgumentResolver[] { new FacesWebArgumentResolver() };

	// FIXME @SessionAttributes support

	private UrlPathHelper urlPathHelper = new UrlPathHelper();

	private MethodNameResolver methodNameResolver = new InternalPathMethodNameResolver();

	private PathMatcher pathMatcher = new AntPathMatcher();

	private FacesHandlerAdapter facesHandlerAdapter;

	private final NavigationCaseAnnotationLocator navigationCaseAnnotationLocator = new NavigationCaseAnnotationLocator();

	private final Map<Class<?>, NavigationCaseMethodsResolver> methodResolverCache = new ConcurrentHashMap<Class<?>, NavigationCaseMethodsResolver>();

	// Always order above other AnnotationMethodHandlerAdapter adapters so that they do not type and process faces
	// requests
	private int order = Ordered.HIGHEST_PRECEDENCE;

	public boolean supports(Object handler) {
		return supportsFaces(handler) && super.supports(handler);
	}

	protected boolean supportsFaces(Object handler) {
		Class handlerClass = ClassUtils.getUserClass(handler);
		boolean hasFacesController = AnnotationUtils.findAnnotation(handlerClass, FacesController.class) != null;
		return hasFacesController;
	}

	protected ModelAndView createView(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		return super.handle(request, response, handler);
	}

	protected Object getNavigationOutcome(FacesContext facesContext, NavigationRequestEvent event, Object handler)
			throws Exception {
		NavigationCaseMethodsResolver methodResolver = getMethodsResolver(handler);
		HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
		HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
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
		Class handlerClass = ClassUtils.getUserClass(handler);
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
	 * Adapter class to convert the annotated handler into a {@link FacesController}.
	 */
	private class AnnotatedMethodFacesHandlerAdapter implements FacesHandler {
		private Object handler;

		public AnnotatedMethodFacesHandlerAdapter(Object handler) {
			super();
			this.handler = handler;
		}

		public ModelAndView createView(FacesContext facesContext) throws Exception {
			HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
			HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
			return FacesAnnotationMethodHandlerAdapter.this.createView(request, response, handler);
		}

		public Object getNavigationOutcomeLocation(FacesContext facesContext, NavigationRequestEvent event)
				throws Exception {
			return FacesAnnotationMethodHandlerAdapter.this.getNavigationOutcome(facesContext, event, handler);
		}

		public Object resolveVariable(String variableName) {
			// FIXME expose controller
			if ("controller".equals(variableName)) {
				return handler;
			}
			return null;
		}
	}
}
