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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator;
import org.springframework.aop.interceptor.DebugInterceptor;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
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

	public void testFindAnnotationOnBeanByClassAnnotation() throws Exception {
		GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
		beanDefinition.setBeanClass(FacesMvcController.class);
		context.registerBeanDefinition("bean", beanDefinition);
		FacesController annotation = mapping.findAnnotationOnBean(context.getBeanFactory(), "bean",
				FacesController.class);
		assertNotNull(annotation);
	}

	public void testFindAnnotationOnBeanWithProxy() throws Exception {
		GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
		beanDefinition.setBeanClass(FacesMvcController.class);
		context.registerBeanDefinition("bean", beanDefinition);
		GenericBeanDefinition interceptorBeanDefinition = new GenericBeanDefinition();
		interceptorBeanDefinition.setBeanClass(DebugInterceptor.class);
		context.registerBeanDefinition("interceptor", interceptorBeanDefinition);
		GenericBeanDefinition aopBeanDefinition = new GenericBeanDefinition();
		aopBeanDefinition.setBeanClass(BeanNameAutoProxyCreator.class);
		MutablePropertyValues propertyValues = new MutablePropertyValues();
		propertyValues.addPropertyValue("beanNames", new String[] { "bean" });
		propertyValues.addPropertyValue("interceptorNames", new String[] { "interceptor" });
		aopBeanDefinition.setPropertyValues(propertyValues);
		context.registerBeanDefinition("aop", aopBeanDefinition);
		context.refresh();

		// First test that the class is a proxy and the annotation cannot be found directly
		Class<?> handlerType = context.getBeanFactory().getType("bean");
		assertNull(AnnotationUtils.findAnnotation(handlerType, FacesController.class));

		// Test that the annotation can be found indirectly
		FacesController annotation = mapping.findAnnotationOnBean(context.getBeanFactory(), "bean",
				FacesController.class);
		assertNotNull(annotation);
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

	private void doTestDetermineUrlsForHandler(Class<?> controllerClass) throws Exception {
		GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
		beanDefinition.setBeanClass(controllerClass);
		context.registerBeanDefinition("bean", beanDefinition);
		String[] urls = mapping.determineUrlsForHandler("bean");
		assertNotNull(urls);
		Set<String> mappedUrls = new HashSet<String>(Arrays.asList(urls));
		Set<String> expectedUrls = new HashSet<String>(Arrays.asList(new String[] { "/test/*", "/test/*.*" }));
		assertEquals(expectedUrls, mappedUrls);
	}

	public void testDetermineUrlsForHandler() throws Exception {
		doTestDetermineUrlsForHandler(FacesMvcController.class);
	}

	public void testDetermineUrlsForHandlerOnMethod() throws Exception {
		doTestDetermineUrlsForHandler(FacesMvcControllerWithMappingsOnMethod.class);
	}

	@Controller
	@RequestMapping("/test/*")
	public static class NormalMvcController {
	}

	public static interface InterfaceWithoutAnnotation {
	}

	@FacesController
	@RequestMapping("/test/*")
	public static class FacesMvcController implements InterfaceWithoutAnnotation {
	}

	@FacesController
	public static class FacesMvcControllerWithMappingsOnMethod {
		@RequestMapping("/test/*")
		public void method() {
		}

	}

}
