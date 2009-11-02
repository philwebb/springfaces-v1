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

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.faces.bind.annotation.FacesController;
import org.springframework.faces.mvc.DefaultRedirectHandler;
import org.springframework.faces.mvc.FacesHandler;
import org.springframework.faces.mvc.FacesHandlerAdapter;
import org.springframework.faces.mvc.RedirectHandler;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.ClassUtils;
import org.springframework.util.PathMatcher;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter;
import org.springframework.web.servlet.mvc.multiaction.InternalPathMethodNameResolver;
import org.springframework.web.servlet.mvc.multiaction.MethodNameResolver;
import org.springframework.web.util.UrlPathHelper;

/**
 * Extension of {@link AnnotationMethodHandlerAdapter} to support JSF controllers.
 * 
 * @author Phillip Webb
 */
public class FacesAnnotationMethodHandlerAdapter extends AnnotationMethodHandlerAdapter {

	// FIXME doccomments
	// FIXME support FaceContext param injection?
	// FIXME expose controller
	// FIXME @SessionAttributes support
	// FIXME support @ModelAttribute on navigation

	private UrlPathHelper urlPathHelper = new UrlPathHelper();

	private MethodNameResolver methodNameResolver = new InternalPathMethodNameResolver();

	private PathMatcher pathMatcher = new AntPathMatcher();

	private RedirectHandler redirectHandler = new DefaultRedirectHandler();
	// FIXME setter

	private FacesHandlerAdapter facesHandlerAdapter;

	private final NavigationCaseAnnotationLocator navigationCaseAnnotationLocator = new NavigationCaseAnnotationLocator();

	private final Map<Class<?>, NavigationCaseMethodsResolver> methodResolverCache = new ConcurrentHashMap<Class<?>, NavigationCaseMethodsResolver>();

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

	protected Object getNavigationOutcome(FacesContext facesContext, String fromAction, String outcome, Object handler)
			throws Exception {
		NavigationCaseMethodsResolver methodResolver = getMethodsResolver(handler);
		HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
		Method[] navigationMethods = methodResolver.resolveNavigationMethods(request);
		FoundNavigationCase navigationCase = navigationCaseAnnotationLocator.findNavigationCase(navigationMethods,
				outcome);
		// FIXME execute the case to get an outcome
		Object location = null;
		redirectHandler.handleRedirect(facesContext, location);
		return null;
	}

	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		FacesHandler facesHandler = new AnnotatedMethodFacesHandlerAdapter(handler);
		return facesHandlerAdapter.handle(request, response, facesHandler);
	}

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

		public Object getNavigationOutcomeLocation(FacesContext facesContext, String fromAction, String outcome)
				throws Exception {
			return FacesAnnotationMethodHandlerAdapter.this.getNavigationOutcome(facesContext, fromAction, outcome,
					handler);
		}
	}

}
