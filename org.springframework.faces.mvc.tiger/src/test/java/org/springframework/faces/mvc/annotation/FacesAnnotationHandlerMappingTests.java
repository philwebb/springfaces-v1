package org.springframework.faces.mvc.annotation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
		Set<String> mappedUrls = new HashSet<String>(Arrays.asList(urls));
		Set<String> expectedUrls = new HashSet<String>(Arrays.asList(new String[] { "/test/*", "/test/*.*" }));
		assertEquals(expectedUrls, mappedUrls);
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
