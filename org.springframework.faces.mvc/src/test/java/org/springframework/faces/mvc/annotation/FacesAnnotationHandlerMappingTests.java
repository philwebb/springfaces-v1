package org.springframework.faces.mvc.annotation;

import junit.framework.TestCase;

import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.faces.mvc.stereotype.FacesController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

public class FacesAnnotationHandlerMappingTests extends TestCase {

	private FacesAnnotationHandlerMapping mapping;
	private StaticApplicationContext context;

	protected void setUp() throws Exception {
		super.setUp();
		this.mapping = new FacesAnnotationHandlerMapping();
		this.context = new StaticApplicationContext();
		mapping.setApplicationContext(context);
	}

	public void testHighestOrder() throws Exception {
		assertEquals(Ordered.HIGHEST_PRECEDENCE, mapping.getOrder());
	}

	public void testDetermineUrlsForHandlerWithoutFacesControllerAnnotation() throws Exception {
		GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
		beanDefinition.setBeanClass(NormalMvcController.class);
		context.registerBeanDefinition("bean", beanDefinition);
		String[] urls = mapping.determineUrlsForHandler("bean");
		assertNull(urls);
	}

	public void testDetermineUrlsForHandler() throws Exception {
		GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
		beanDefinition.setBeanClass(FacesMvcController.class);
		context.registerBeanDefinition("bean", beanDefinition);
		String[] urls = mapping.determineUrlsForHandler("bean");
		assertNotNull(urls);
		assertEquals(2, urls.length);
		assertEquals("/test/*", urls[0]);
		assertEquals("/test/*.*", urls[1]);
	}

	@Controller
	@RequestMapping("/test/*")
	public static class NormalMvcController {
	}

	@FacesController
	@RequestMapping("/test/*")
	public static class FacesMvcController {
	}

}
