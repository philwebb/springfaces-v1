package org.springframework.faces.mvc.annotation;

import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.generic.GenericBeanFactoryAccessor;
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
