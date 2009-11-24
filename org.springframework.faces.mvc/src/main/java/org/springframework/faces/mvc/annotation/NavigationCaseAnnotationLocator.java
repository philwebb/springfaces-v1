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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.faces.mvc.NavigationRequestEvent;
import org.springframework.faces.mvc.bind.annotation.NavigationCase;
import org.springframework.faces.mvc.bind.annotation.NavigationRules;
import org.springframework.util.StringUtils;

/**
 * Support class that can be used to locate {@link NavigationCase} and {@link NavigationRules} annotations.
 * 
 * @author Phillip Webb
 */
public class NavigationCaseAnnotationLocator {

	/**
	 * Find and return the first suitable {@link NavigationCase} annotation by searching the specified <tt>methods</tt>.
	 * This method will search for {@link NavigationCase} and {@link NavigationRules} annotations on the specified
	 * methods array. Methods are searched in order starting at index 0. If not suitable method annotation is found the
	 * declaring class of the method will be searched, followed by the package.
	 * 
	 * @param methods Array of methods to search.
	 * @param event The navigation request event.
	 * @return A {@link FoundNavigationCase} instance or <tt>null</tt> if no suitable annotation is found.
	 */
	public FoundNavigationCase findNavigationCase(Method[] methods, NavigationRequestEvent event) {
		if (methods == null || methods.length == 0) {
			return null;
		}
		NavigationCaseFilter filter = new NavigationCaseFilter(event);

		FoundNavigationCase navigationCase = null;

		// Search methods (collecting classes)
		Set<Class<?>> ownerClasses = new LinkedHashSet<Class<?>>();
		for (Method method : methods) {
			ownerClasses.add(method.getDeclaringClass());
			navigationCase = findNavigationCase(method, filter);
			if (navigationCase != null) {
				return navigationCase;
			}
		}

		// Search classes (collecting packages_
		Set<Package> ownerPackages = new LinkedHashSet<Package>();
		for (Class<?> ownerClass : ownerClasses) {
			ownerPackages.add(ownerClass.getPackage());
			navigationCase = findNavigationCase(ownerClass, filter);
			if (navigationCase != null) {
				return navigationCase;
			}
		}

		// Search package
		for (Package ownerPackage : ownerPackages) {
			navigationCase = findNavigationCase(ownerPackage, filter);
			if (navigationCase != null) {
				return navigationCase;
			}
		}

		return null;
	}

	/**
	 * Find a {@link NavigationCase} by searching annotations on the specified owner.
	 * 
	 * @param owner The owner (either a Method, Class or Package)
	 * @param filter The filter used to limit results
	 * @return A {@link FoundNavigationCase} or <tt>null</tt>
	 */
	private FoundNavigationCase findNavigationCase(Object owner, NavigationCaseFilter filter) {
		NavigationCase navigationCase = findAnnotation(owner, NavigationCase.class);
		if (filter.isSuitable(navigationCase)) {
			return new FoundNavigationCase(navigationCase, owner);
		}
		return findNavigationCase(owner, findAnnotation(owner, NavigationRules.class), filter);
	}

	/**
	 * Find a {@link NavigationCase} by searching a {@link NavigationRules} annotation that has been located on the
	 * specified owner.
	 * 
	 * @param owner The owner (either a Method, Class or Package)
	 * @param filter The filter used to limit results
	 * @return A {@link FoundNavigationCase} or <tt>null</tt>
	 */
	private FoundNavigationCase findNavigationCase(Object owner, NavigationRules navigationRules,
			NavigationCaseFilter filter) {
		if (navigationRules == null) {
			return null;
		}
		for (NavigationCase navigationCase : navigationRules.value()) {
			if (filter.isSuitable(navigationCase)) {
				return new FoundNavigationCase(navigationCase, owner);
			}
		}
		return null;
	}

	/**
	 * Attempt to find a given annotation on the specified owner.
	 * @param <A>
	 * @param owner The owner of the annotation (either a Method, Class or Package)
	 * @param annotationType The type of annotation
	 * @return The annotation or <tt>null</tt>
	 */
	private <A extends Annotation> A findAnnotation(Object owner, Class<A> annotationType) {
		if (owner == null) {
			return null;
		}
		if (owner instanceof Package) {
			return ((Package) owner).getAnnotation(annotationType);
		}
		if (owner instanceof Method) {
			return AnnotationUtils.findAnnotation((Method) owner, annotationType);
		}
		if (owner instanceof Class) {
			return AnnotationUtils.findAnnotation((Class) owner, annotationType);
		}
		throw new IllegalArgumentException("findAnnotation can only be called with a Method, Class or Package argument");
	}

	/**
	 * Internal filter class used to determine if a navigation case matches an outcome.
	 */
	private static class NavigationCaseFilter {

		private NavigationRequestEvent event;

		public NavigationCaseFilter(NavigationRequestEvent event) {
			this.event = event;
		}

		public boolean isSuitable(NavigationCase navigationCase) {
			if (navigationCase == null) {
				return false;
			}
			if (!isSuitableOn(navigationCase)) {
				return false;
			}
			if (!isSuitableAction(navigationCase)) {
				return false;
			}
			if (!isSuitableException(navigationCase)) {
				return false;
			}
			return true;
		}

		private boolean isSuitableOn(NavigationCase navigationCase) {
			if (navigationCase.on().length == 0) {
				return true;
			}
			for (String on : navigationCase.on()) {
				if (on.equals(event.getOutcome())) {
					return true;
				}
			}
			return false;
		}

		private boolean isSuitableAction(NavigationCase navigationCase) {
			if ("".equals(navigationCase.fromAction())) {
				return true;
			}
			if (StringUtils.hasText(event.getFromAction()) && event.getFromAction().equals(navigationCase.fromAction())) {
				return true;
			}
			return false;
		}

		private boolean isSuitableException(NavigationCase navigationCase) {
			if (navigationCase.onException() == null || void.class.equals(navigationCase.onException())) {
				// onException annotation has not been specified, this case is only suitable when we are not handling an
				// exception
				return (event.getException() == null);
			}
			if (event.getException() == null) {
				// We have an onException annotation but no exception
				return false;
			}

			// Test if the exception matches
			Throwable throwable = event.getException();
			while (throwable != null) {
				if (navigationCase.onException().isInstance(throwable)) {
					return true;
				}
				throwable = throwable.getCause();
			}
			return false;
		}
	}
}
