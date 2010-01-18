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
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.faces.mvc.stereotype.FacesController;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.annotation.DefaultAnnotationHandlerMapping;

/**
 * Implementation of the {@link org.springframework.web.servlet.HandlerMapping} interface that maps handlers based on
 * HTTP paths expressed through the {@link RequestMapping} annotation at the type or method level when the type also
 * includes the {@link FacesController} annotation.
 * <p>
 * Note: When using this mapper in combination with the {@link DefaultAnnotationHandlerMapping} ensure that this mapper
 * is ordered above the {@link DefaultAnnotationHandlerMapping}.
 * 
 * @see DefaultAnnotationHandlerMapping
 * 
 * @author Juergen Hoeller
 * @author Arjen Poutsma
 * @author Phillip Webb
 */
public class FacesAnnotationHandlerMapping extends DefaultAnnotationHandlerMapping {

	public FacesAnnotationHandlerMapping() {
		super();
		setOrder(Ordered.HIGHEST_PRECEDENCE);
	}

	/**
	 * Find a {@link Annotation} of <code>annotationType</code> on the specified bean, traversing its interfaces and
	 * super classes if no annotation can be found on the given class itself, as well as checking its raw bean class if
	 * not found on the exposed bean reference (e.g. in case of a proxy).
	 * @param beanName the name of the bean to look for annotations on
	 * @param annotationType the annotation class to look for
	 * @return the annotation of the given type found, or <code>null</code>
	 * @see org.springframework.core.annotation.AnnotationUtils#findAnnotation(Class, Class)
	 * 
	 * @author Rob Harrop
	 * @author Juergen Hoeller
	 */
	protected <A extends Annotation> A findAnnotationOnBean(ListableBeanFactory beanFactory, String beanName,
			Class<A> annotationType) {
		Class<?> handlerType = beanFactory.getType(beanName);
		A ann = AnnotationUtils.findAnnotation(handlerType, annotationType);
		if (ann == null && beanFactory instanceof ConfigurableBeanFactory
				&& beanFactory.containsBeanDefinition(beanName)) {
			ConfigurableBeanFactory cbf = (ConfigurableBeanFactory) beanFactory;
			BeanDefinition bd = cbf.getMergedBeanDefinition(beanName);
			if (bd instanceof AbstractBeanDefinition) {
				AbstractBeanDefinition abd = (AbstractBeanDefinition) bd;
				if (abd.hasBeanClass()) {
					Class<?> beanClass = abd.getBeanClass();
					ann = AnnotationUtils.findAnnotation(beanClass, annotationType);
				}
			}
		}
		return ann;
	}

	protected String[] determineUrlsForHandler(String beanName) {
		ApplicationContext context = getApplicationContext();
		Class<?> handlerType = context.getType(beanName);
		ListableBeanFactory bf = (context instanceof ConfigurableApplicationContext ? ((ConfigurableApplicationContext) context)
				.getBeanFactory()
				: context);

		if (AnnotationUtils.findAnnotation(handlerType, FacesController.class) == null) {
			return null;
		}

		RequestMapping mapping = findAnnotationOnBean(bf, beanName, RequestMapping.class);
		if (mapping != null) {
			// @RequestMapping found at type level
			Set<String> urls = new LinkedHashSet<String>();
			String[] paths = mapping.value();
			if (paths.length > 0) {
				// @RequestMapping specifies paths at type level
				for (String path : paths) {
					addUrlsForPath(urls, path);
				}
				return StringUtils.toStringArray(urls);
			}
		}

		// actual paths specified by @RequestMapping at method level
		return determineUrlsForHandlerMethods(handlerType);
	}
}
