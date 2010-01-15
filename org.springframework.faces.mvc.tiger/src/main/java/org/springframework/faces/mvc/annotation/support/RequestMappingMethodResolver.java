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
package org.springframework.faces.mvc.annotation.support;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.PathMatcher;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter;
import org.springframework.web.servlet.mvc.multiaction.MethodNameResolver;
import org.springframework.web.servlet.mvc.multiaction.NoSuchRequestHandlingMethodException;
import org.springframework.web.util.UrlPathHelper;
import org.springframework.web.util.WebUtils;

/**
 * Support class for resolving web method annotations in a handler type. This class can be used to determine what
 * methods from a {@link RequestMapping} annotated class could be used to process a given request. The class is based
 * heavily on the <tt>ServletHandlerMethodResolver</tt> internal class used by {@link AnnotationMethodHandlerAdapter}s.
 * 
 * @author Juergen Hoeller
 * @author Arjen Poutsma
 * @author Phillip Webb
 */
public class RequestMappingMethodResolver {

	private final Set<Method> handlerMethods = new LinkedHashSet<Method>();
	private final Set<Method> initBinderMethods = new LinkedHashSet<Method>();
	private final Set<Method> modelAttributeMethods = new LinkedHashSet<Method>();
	private RequestMapping typeLevelMapping;
	private boolean sessionAttributesFound;
	private final Set<String> sessionAttributeNames = new HashSet<String>();
	private final Set<Class<?>> sessionAttributeTypes = new HashSet<Class<?>>();
	private final Set<String> actualSessionAttributeNames = Collections.synchronizedSet(new HashSet<String>(4));
	private MethodNameResolver methodNameResolver;
	private Map<Method, RequestMappingAnnotation> methodAnnotations;
	private UrlPathHelper urlPathHelper;
	private PathMatcher pathMatcher;

	/**
	 * Constructor.
	 * @param handlerType The handler type
	 * @param urlPathHelper The URL path helper
	 * @param methodNameResolver The method name resolver
	 * @param pathMatcher The path matcher
	 */
	public RequestMappingMethodResolver(Class<?> handlerType, UrlPathHelper urlPathHelper,
			MethodNameResolver methodNameResolver, PathMatcher pathMatcher) {
		init(handlerType);
		this.urlPathHelper = urlPathHelper;
		this.methodNameResolver = methodNameResolver;
		this.pathMatcher = pathMatcher;
		RequestMappingAnnotation typeLevelAnnotation = null;
		if (hasTypeLevelMapping()) {
			typeLevelAnnotation = new RequestMappingAnnotation(null, getTypeLevelMapping());
		}
		methodAnnotations = new HashMap<Method, RequestMappingAnnotation>();
		for (Method method : getHandlerMethods()) {
			methodAnnotations.put(method, new RequestMappingAnnotation(typeLevelAnnotation, method));
		}
	}

	/**
	 * Initialize a new HandlerMethodResolver for the specified handler type.
	 * @param handlerType the handler class to introspect
	 */
	public void init(Class<?> handlerType) {
		Assert.notNull(handlerType, "handlerType is required");
		Class<?>[] handlerTypes = Proxy.isProxyClass(handlerType) ? handlerType.getInterfaces()
				: new Class<?>[] { handlerType };
		for (final Class<?> currentHandlerType : handlerTypes) {
			ReflectionUtils.doWithMethods(currentHandlerType, new ReflectionUtils.MethodCallback() {
				public void doWith(Method method) {
					Method specificMethod = ClassUtils.getMostSpecificMethod(method, currentHandlerType);
					if (isHandlerMethod(method)) {
						handlerMethods.add(specificMethod);
					} else if (method.isAnnotationPresent(InitBinder.class)) {
						initBinderMethods.add(specificMethod);
					} else if (method.isAnnotationPresent(ModelAttribute.class)) {
						modelAttributeMethods.add(specificMethod);
					}
				}
			});
		}
		this.typeLevelMapping = AnnotationUtils.findAnnotation(handlerType, RequestMapping.class);
		SessionAttributes sessionAttributes = handlerType.getAnnotation(SessionAttributes.class);
		this.sessionAttributesFound = (sessionAttributes != null);
		if (this.sessionAttributesFound) {
			this.sessionAttributeNames.addAll(Arrays.asList(sessionAttributes.value()));
			this.sessionAttributeTypes.addAll(Arrays.asList((Class<?>[]) sessionAttributes.types()));
		}
	}

	/**
	 * Determine if the specified method is a web request handler.
	 * @param method The method
	 * @return <tt>true</tt> if the method can be used to handler a web request
	 */
	protected boolean isHandlerMethod(Method method) {
		return method.isAnnotationPresent(RequestMapping.class);
	}

	/**
	 * Determine if the handler includes one or more methods that can handle a web request.
	 * @return <tt>true</tt> if handler methods are available
	 * @see #getHandlerMethods()
	 */
	public final boolean hasHandlerMethods() {
		return !this.handlerMethods.isEmpty();
	}

	/**
	 * Returns all methods from the handler that could be used to handle a web request.
	 * @return All handler methods
	 * @see #hasHandlerMethods()
	 */
	public final Set<Method> getHandlerMethods() {
		return this.handlerMethods;
	}

	/**
	 * Returns all handler methods that can be used to initialize {@link DataBinder}s.
	 * @return All init binder methods
	 */
	public final Set<Method> getInitBinderMethods() {
		return this.initBinderMethods;
	}

	/**
	 * Returns all handler methods that are used to setup initial model values.
	 * @return All model attribute methods
	 */
	public final Set<Method> getModelAttributeMethods() {
		return this.modelAttributeMethods;
	}

	/**
	 * Determine if the handler has type level mappings (i.e. the class itself is has a {@link RequestMapping}
	 * annotation)
	 * @return true if the handler as a type level mapping
	 * @see #getTypeLevelMapping()
	 */
	public boolean hasTypeLevelMapping() {
		return (this.typeLevelMapping != null);
	}

	/**
	 * Returns the type level mappings from the handler, or <tt>null</tt> if the handler class does not include a
	 * {@link RequestMapping} annotation.
	 * @return The type level mapping or <tt>null</tt>
	 * @see #hasTypeLevelMapping()
	 */
	public RequestMapping getTypeLevelMapping() {
		return this.typeLevelMapping;
	}

	/**
	 * Determine of the handler includes a {@link SessionAttributes} annotation.
	 * @return <tt>true</tt> if the handler include session attributes
	 * @see #isSessionAttribute(String, Class)
	 */
	public boolean hasSessionAttributes() {
		return this.sessionAttributesFound;
	}

	/**
	 * Determine if the specified attribute details have been marked as a session attribute. Session attributes can be
	 * denoted by name or type. Note: this method will also collate session attributes so that they can be return from
	 * {@link #getActualSessionAttributeNames()}.
	 * @param attrName The attribute name
	 * @param attrType The attribute type
	 * @return <tt>true</tt> if the attribute is a session attribue
	 * @see #hasSessionAttributes()
	 * @see #getActualSessionAttributeNames()
	 */
	public boolean isSessionAttribute(String attrName, Class<?> attrType) {
		if (this.sessionAttributeNames.contains(attrName) || this.sessionAttributeTypes.contains(attrType)) {
			this.actualSessionAttributeNames.add(attrName);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Returns all session attributes that have been collated during calls to {@link #isSessionAttribute(String, Class)}
	 * @return All actual session attributes
	 * @see #isSessionAttribute(String, Class)
	 */
	public Set<String> getActualSessionAttributeNames() {
		return this.actualSessionAttributeNames;
	}

	/**
	 * Returns an ordered array of methods that could be used to process the request. The first item in the array is the
	 * method that would be called by the {@link AnnotationMethodHandlerAdapter} class, remaining items are ordered by
	 * their suitability..
	 * 
	 * @param request the request
	 * @return All methods that can process the request in order of suitability
	 * @throws ServletException on error
	 */
	public Method[] resolveHandlerMethods(HttpServletRequest request) throws ServletException {
		return new HandlerMethodsResolver(request).resolve();
	}

	/**
	 * Internal helper class used to perform the resolve of methods.
	 */
	private class HandlerMethodsResolver {

		private HttpServletRequest request;
		private String lookupPath;
		private String resolvedMethodName;

		public HandlerMethodsResolver(HttpServletRequest request) {
			this.request = request;
			this.lookupPath = urlPathHelper.getLookupPathForRequest(request);
		}

		public Method[] resolve() throws ServletException {
			Map<RequestMappingAnnotation, RequestMappingAnnotationMatch> matches = new LinkedHashMap<RequestMappingAnnotation, RequestMappingAnnotationMatch>();
			for (Map.Entry<Method, RequestMappingAnnotation> entry : methodAnnotations.entrySet()) {
				Method method = entry.getKey();
				RequestMappingAnnotation annotation = entry.getValue();
				RequestMappingAnnotationMatch match = getMatch(method, annotation);
				if (match != null) {
					RequestMappingAnnotationMatch previousMatch = matches.get(annotation);
					matches.put(annotation, chooseSingleMatch(annotation, previousMatch, match));
				}
			}
			ArrayList<RequestMappingAnnotationMatch> matchedList = new ArrayList<RequestMappingAnnotationMatch>(matches
					.values());
			Collections.sort(matchedList, new RequestMappingAnnotationMatchComparator(lookupPath));
			Method[] rtn = new Method[matchedList.size()];
			for (int i = 0; i < rtn.length; i++) {
				rtn[i] = matchedList.get(i).getMethod();
			}
			return rtn;
		}

		private RequestMappingAnnotationMatch getMatch(Method method, RequestMappingAnnotation annotation) {
			// If we have a resolved method name, check it matches
			if (resolvedMethodName != null && !resolvedMethodName.equals(method.getName())) {
				return null;
			}
			// Check the HTTP method and the params match
			if ((!isRequestMethodMatch(annotation, request)) || (!isSubmitParamsMatch(annotation, request))) {
				return null;
			}

			RequestMappingAnnotationMatch match = new RequestMappingAnnotationMatch(method, annotation);
			// If the annotation has not specified paths then we are a match
			if (annotation.getPaths().length == 0) {
				return match;
			}

			// Otherwise we match if at least one of the paths matches
			for (String mappedPath : annotation.getPaths()) {
				if (isPathMatch(mappedPath, lookupPath)) {
					match.getPaths().add(mappedPath);
				}
			}
			return match.getPaths().size() == 0 ? null : match;
		}

		private boolean isRequestMethodMatch(RequestMappingAnnotation annotation, HttpServletRequest request) {
			if (annotation.requestMethods.isEmpty()) {
				return true;
			}
			try {
				RequestMethod method = RequestMethod.valueOf(request.getMethod());
				return annotation.requestMethods.contains(method);
			} catch (IllegalArgumentException e) {
				return false;
			}
		}

		private boolean isSubmitParamsMatch(RequestMappingAnnotation annotation, HttpServletRequest request) {
			for (String param : annotation.params) {
				int separator = param.indexOf('=');
				if (separator == -1) {
					if (param.startsWith("!")) {
						if (WebUtils.hasSubmitParameter(request, param.substring(1))) {
							return false;
						}
					} else if (!WebUtils.hasSubmitParameter(request, param)) {
						return false;
					}
				} else {
					String key = param.substring(0, separator);
					String value = param.substring(separator + 1);
					if (!value.equals(request.getParameter(key))) {
						return false;
					}
				}
			}
			return true;
		}

		private boolean isPathMatch(String mappedPath, String lookupPath) {
			if (mappedPath.equals(lookupPath) || pathMatcher.match(mappedPath, lookupPath)) {
				return true;
			}
			boolean hasSuffix = (mappedPath.indexOf('.') != -1);
			if (!hasSuffix && pathMatcher.match(mappedPath + ".*", lookupPath)) {
				return true;
			}
			return (!mappedPath.startsWith("/") && (lookupPath.endsWith(mappedPath)
					|| pathMatcher.match("/**/" + mappedPath, lookupPath) || (!hasSuffix && pathMatcher.match("/**/"
					+ mappedPath + ".*", lookupPath))));
		}

		private RequestMappingAnnotationMatch chooseSingleMatch(RequestMappingAnnotation annotation,
				RequestMappingAnnotationMatch previousMatch, RequestMappingAnnotationMatch currentMatch)
				throws NoSuchRequestHandlingMethodException {
			// Check if we have no previous match, or we are dealing with the same method
			if (previousMatch == null || previousMatch.getMethod().equals(currentMatch.getMethod())) {
				return currentMatch;
			}

			// We have different matches, use the method name resolver to try and limit them
			if (annotation.getPaths().length == 0 && methodNameResolver != null) {
				if (resolvedMethodName == null) {
					resolvedMethodName = methodNameResolver.getHandlerMethodName(request);
				}
				if (!resolvedMethodName.equals(previousMatch.getMethod().getName())) {
					previousMatch = null;
				}
				if (!resolvedMethodName.equals(currentMatch.getMethod().getName())) {
					currentMatch = null;
				}
			}

			// If we still have both matches we have an ambiguous mapping
			if (previousMatch != null && currentMatch != null) {
				throw new IllegalStateException("Ambiguous handler methods mapped for HTTP path '" + lookupPath
						+ "': {" + previousMatch.getMethod() + ", " + currentMatch.getMethod()
						+ "}. If you intend to handle the same path in multiple methods, then factor "
						+ "them out into a dedicated handler class with that path mapped at the type level!");
			}

			// Return the single match, this can be null
			return currentMatch != null ? currentMatch : previousMatch;
		}
	}

	/**
	 * Comparator used to sort matching items.
	 */
	protected static class RequestMappingAnnotationMatchComparator implements Comparator<RequestMappingAnnotationMatch> {

		private String lookupPath;

		public RequestMappingAnnotationMatchComparator(String lookupPath) {
			this.lookupPath = lookupPath;
		}

		public int compare(RequestMappingAnnotationMatch o1, RequestMappingAnnotationMatch o2) {
			int rtn = 0;
			rtn = (rtn != 0 ? rtn : compareZeroPaths(o1.getPaths(), o2.getPaths()));
			for (String p1 : o1.getPaths()) {
				for (String p2 : o2.getPaths()) {
					rtn = (rtn != 0 ? rtn : comparePath(p1, p2));
				}
			}
			rtn = (rtn != 0 ? rtn : compareMethod(o1.getAnnotation().getRequestMethods(), o2.getAnnotation()
					.getRequestMethods()));
			rtn = (rtn != 0 ? rtn : compareParam(o1.getAnnotation().getParams(), o2.getAnnotation().getParams()));
			return rtn;
		}

		private int compareZeroPaths(List<String> o1, List<String> o2) {
			if (o1.size() == 0 && o2.size() > 0) {
				return 1;
			}
			if (o2.size() == 0 && o1.size() > 0) {
				return -1;
			}
			return 0;
		}

		private int comparePath(String o1, String o2) {
			int rtn = 0;
			rtn = (rtn != 0 ? rtn : compareEqualsAndNulls(o1, o2));
			rtn = (rtn != 0 ? rtn : compareToLookupPath(o1, o2));
			rtn = (rtn != 0 ? rtn : compareLength(o1, o2));
			return rtn;
		}

		private int compareEqualsAndNulls(Object o1, Object o2) {
			if (o1 == null && o2 != null) {
				return -1;
			}
			if (o1 != null && o2 == null) {
				return 1;
			}
			return 0;
		}

		private int compareToLookupPath(Object o1, Object o2) {
			if (lookupPath.equals(o1) && lookupPath.equals(o2)) {
				return 0;
			}
			return (lookupPath.equals(o1) ? -1 : (lookupPath.equals(02) ? 1 : 0));
		}

		private int compareLength(String o1, String o2) {
			int l1 = (o1 == null ? 0 : o1.length());
			int l2 = (o2 == null ? 0 : o2.length());
			return (l1 < l2 ? 1 : (l1 == l2 ? 0 : -1));
		}

		private int compareMethod(Set<RequestMethod> o1, Set<RequestMethod> o2) {
			if (o1.size() > 0 && o2.size() == 0) {
				return -1;
			}
			if (o2.size() > 0 && o1.size() == 0) {
				return 1;
			}
			return 0;
		}

		private int compareParam(Set<String> o1, Set<String> o2) {
			int l1 = o1.size();
			int l2 = o2.size();
			return (l1 < l2 ? 1 : (l1 == l2 ? 0 : -1));
		}
	}

	/**
	 * Simple wrapper class that allows data from {@link RequestMapping} annotations to be accessed easily. This class
	 * implements also hashCode and equals so that it can be used inside a a map.
	 */
	protected static class RequestMappingAnnotation {

		private String[] paths;
		private Set<String> params;
		private Set<RequestMethod> requestMethods;

		/**
		 * Constructor.
		 * 
		 * @param typeLevelAnnotation The annotation data that is present on the class.
		 * @param requestMapping The annotation data to process.
		 */
		public RequestMappingAnnotation(RequestMappingAnnotation typeLevelAnnotation, RequestMapping requestMapping) {
			paths = requestMapping.value();
			params = new HashSet<String>(Arrays.asList(requestMapping.params()));
			requestMethods = new HashSet<RequestMethod>(Arrays.asList(requestMapping.method()));
			// We can optimise by removing items covered at the type level, there is not need to check these as they
			// will be covered by DefaultAnnotationHandlerMapping
			clearParamsIfAlreadyHandledAtTypeLevel(typeLevelAnnotation);
			clearMethodsIfAlreadyHandledAtTypeLevel(typeLevelAnnotation);
		}

		/**
		 * Constructor.
		 * 
		 * @param typeLevelAnnotation
		 * @param method
		 */
		public RequestMappingAnnotation(RequestMappingAnnotation typeLevelAnnotation, Method method) {
			this(typeLevelAnnotation, AnnotationUtils.findAnnotation(method, RequestMapping.class));
		}

		/**
		 * Constructor (for testing).
		 * 
		 * @param paths
		 * @param params
		 * @param requestMethods
		 */
		protected RequestMappingAnnotation(String[] paths, Set<String> params, Set<RequestMethod> requestMethods) {
			this.paths = paths;
			this.params = params;
			this.requestMethods = requestMethods;
		}

		private void clearParamsIfAlreadyHandledAtTypeLevel(RequestMappingAnnotation typeLevelAnnotation) {
			if (typeLevelAnnotation != null && typeLevelAnnotation.params.equals(params)) {
				params = Collections.emptySet();
			}
		}

		private void clearMethodsIfAlreadyHandledAtTypeLevel(RequestMappingAnnotation typeLevelAnnotation) {
			if (typeLevelAnnotation != null && typeLevelAnnotation.params.equals(params)) {
				params = Collections.emptySet();
			}
		}

		public boolean equals(Object obj) {
			RequestMappingAnnotation other = (RequestMappingAnnotation) obj;
			return (Arrays.equals(this.paths, other.paths) && this.params.equals(other.params) && this.requestMethods
					.equals(other.requestMethods));
		}

		public int hashCode() {
			return (Arrays.hashCode(this.paths) * 29 + this.params.hashCode() * 31 + this.requestMethods.hashCode());
		}

		public String[] getPaths() {
			return paths;
		}

		public Set<RequestMethod> getRequestMethods() {
			return requestMethods;
		}

		public Set<String> getParams() {
			return params;
		}

		public String toString() {
			return new ToStringCreator(this).append("paths", paths).append("params", params).append("requestMethods",
					requestMethods).toString();
		}
	}

	/**
	 * Class used to contain details of an annotation match.
	 */
	protected static class RequestMappingAnnotationMatch {

		private Method method;
		private RequestMappingAnnotation annotation;
		private List<String> paths = new ArrayList<String>();

		public RequestMappingAnnotationMatch(Method method, RequestMappingAnnotation annotation) {
			this.method = method;
			this.annotation = annotation;
		}

		public Method getMethod() {
			return method;
		}

		/**
		 * @return The paths that match. NOTE: this can be a subset of {@link RequestMappingAnnotation#getPaths()}.
		 */
		public List<String> getPaths() {
			return paths;
		}

		public RequestMappingAnnotation getAnnotation() {
			return annotation;
		}

		public String toString() {
			return new ToStringCreator(this).append("method", method).append("annotation", annotation).append("paths",
					paths).toString();
		}
	}
}
