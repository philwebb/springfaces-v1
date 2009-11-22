package org.springframework.faces.mvc.annotation;

import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.generic.GenericBeanFactoryAccessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.faces.mvc.bind.stereotype.FacesController;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.annotation.DefaultAnnotationHandlerMapping;

public class FacesAnnotationHandlerMapping extends DefaultAnnotationHandlerMapping {

	// FIXME test
	// FIXME documment the handler adapter

	public FacesAnnotationHandlerMapping() {
		super();
		setOrder(Ordered.HIGHEST_PRECEDENCE);
	}

	protected String[] determineUrlsForHandler(String beanName) {

		ApplicationContext context = getApplicationContext();
		Class<?> handlerType = context.getType(beanName);
		ListableBeanFactory bf = (context instanceof ConfigurableApplicationContext ? ((ConfigurableApplicationContext) context)
				.getBeanFactory()
				: context);
		GenericBeanFactoryAccessor bfa = new GenericBeanFactoryAccessor(bf);

		if (AnnotationUtils.findAnnotation(handlerType, FacesController.class) == null) {
			return null;
		}

		RequestMapping mapping = bfa.findAnnotationOnBean(beanName, RequestMapping.class);
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
