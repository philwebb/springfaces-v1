package org.springframework.faces.mvc.annotation;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.faces.bind.annotation.NavigationCase;
import org.springframework.faces.bind.annotation.NavigationRules;
import org.springframework.util.ClassUtils;
import org.springframework.util.PathMatcher;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.multiaction.MethodNameResolver;
import org.springframework.web.util.UrlPathHelper;

/**
 * Support class that extends {@link RequestMappingMethodsResolver} to resolve web method and navigation annotations in
 * a handler type. This class can be used to determine what methods from a {@link RequestMapping} annotated class could
 * be used to process a given request and what {@link NavigationCase} or {@link NavigationRules} annotated methods could
 * be used to handle navigation.
 * 
 * @see NavigationCaseAnnotationLocator
 * 
 * @author Phillip Webb
 */
public class NavigationCaseMethodsResolver extends RequestMappingMethodsResolver {

	private final Set<Method> globalNavigationMethods = new LinkedHashSet<Method>();

	public NavigationCaseMethodsResolver(final Class<?> handlerType, UrlPathHelper urlPathHelper,
			MethodNameResolver methodNameResolver, PathMatcher pathMatcher) {
		super(handlerType, urlPathHelper, methodNameResolver, pathMatcher);
		ReflectionUtils.doWithMethods(handlerType, new ReflectionUtils.MethodCallback() {
			public void doWith(Method method) {
				if (!method.isAnnotationPresent(RequestMapping.class) && hasNavigationAnnotation(method)) {
					globalNavigationMethods.add(ClassUtils.getMostSpecificMethod(method, handlerType));
				}
			}
		});
	}

	private boolean hasNavigationAnnotation(Method method) {
		return method.isAnnotationPresent(NavigationCase.class) || method.isAnnotationPresent(NavigationRules.class);
	}

	/**
	 * @return true if the class has global navigation methods (ie. methods annotated with {@link NavigationCase} or
	 * {@link NavigationRules} that are do not also contain {@link RequestMapping} restrictions).
	 */
	public final boolean hasGlobalNavigationMethods() {
		return !this.globalNavigationMethods.isEmpty();
	}

	/**
	 * @return a set of global navigation methods (ie. methods annotated with {@link NavigationCase} or
	 * {@link NavigationRules} that are do not also contain {@link RequestMapping} restrictions).
	 */
	public Set<Method> getGlobalNavigationMethods() {
		return globalNavigationMethods;
	}

	/**
	 * Resolve the methods annotated with {@link NavigationCase} or {@link NavigationRules} and can also process the
	 * specified request.
	 * 
	 * @param request
	 * @return An ordered array of methods that could be used to process the request. The first item in the array is the
	 * most suitable method, remaining items are ordered by their suitability.
	 * @throws ServletException
	 */
	public Method[] resolveNavigationMethods(HttpServletRequest request) throws ServletException {
		Method[] handlerMethods = super.resolveHandlerMethods(request);
		List<Method> navigationMethods = new ArrayList<Method>();
		for (Method method : handlerMethods) {
			if (hasNavigationAnnotation(method)) {
				navigationMethods.add(method);
			}
		}
		navigationMethods.addAll(globalNavigationMethods);
		return navigationMethods.toArray(new Method[] {});
	}

}
