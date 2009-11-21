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
package org.springframework.faces.mvc.support;

import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import junit.framework.TestCase;

import org.springframework.faces.mvc.MvcFacesTestUtils;
import org.springframework.faces.mvc.MvcFacesTestUtils.MethodCallAssertor;

public class AbstractApplicationDecoratorTest extends TestCase {

	private Application application;
	private MethodCallAssertor methodCallAssertor;
	private MockApplicationDecorator decorator;

	protected void setUp() throws Exception {
		super.setUp();
		application = (Application) MvcFacesTestUtils.methodTrackingObject(Application.class);
		methodCallAssertor = (MethodCallAssertor) application;
		decorator = new MockApplicationDecorator(application);
	}

	public void testAddComponent() throws Exception {
		decorator.addComponent("componentType", "componentClass");
		methodCallAssertor.assertCalled("addComponent");
	}

	public void testAddConverter() throws Exception {
		decorator.addConverter(String.class, "StringConverter");
		methodCallAssertor.assertCalled("addConverter");
	}

	public void testAddConverter2() throws Exception {
		decorator.addConverter("converterId", "converterClass");
		methodCallAssertor.assertCalled("addConverter");
	}

	public void testAddELContextListener() throws Exception {
		decorator.addELContextListener(null);
		methodCallAssertor.assertCalled("addELContextListener");
	}

	public void testAddELResolver() throws Exception {
		decorator.addELResolver(null);
		methodCallAssertor.assertCalled("addELResolver");
	}

	public void testAddValidator() throws Exception {
		decorator.addValidator("validatorId", "validatorClass");
		methodCallAssertor.assertCalled("addValidator");
	}

	public void testCreateComponent() throws Exception {
		decorator.createComponent("componentType");
		methodCallAssertor.assertCalled("createComponent");
	}

	public void testCreateComponent2() throws Exception {
		decorator.createComponent((ValueBinding) null, (FacesContext) null, "componentType");
		methodCallAssertor.assertCalled("createComponent");
	}

	public void testCreateComponent3() throws Exception {
		decorator.createComponent((ValueExpression) null, (FacesContext) null, "componentType");
		methodCallAssertor.assertCalled("createComponent");
	}

	public void testCreateConverter() throws Exception {
		decorator.createConverter((Class) null);
		methodCallAssertor.assertCalled("createConverter");
	}

	public void testCreateConverter1() throws Exception {
		decorator.createConverter("converterId");
		methodCallAssertor.assertCalled("createConverter");
	}

	public void testCreateMethodBinding() throws Exception {
		decorator.createMethodBinding("ref", new Class[] {});
		methodCallAssertor.assertCalled("createMethodBinding");
	}

	public void testCreateValidator() throws Exception {
		decorator.createValidator("validatorId");
		methodCallAssertor.assertCalled("createValidator");
	}

	public void testCreateValueBinding() throws Exception {
		decorator.createValueBinding("ref");
		methodCallAssertor.assertCalled("createValueBinding");
	}

	public void testEvaluateExpressionGet() throws Exception {
		decorator.evaluateExpressionGet(null, "expression", null);
		methodCallAssertor.assertCalled("evaluateExpressionGet");
	}

	public void testGetActionListener() throws Exception {
		decorator.getActionListener();
		methodCallAssertor.assertCalled("getActionListener");
	}

	public void testGetComponentTypes() throws Exception {
		decorator.getComponentTypes();
		methodCallAssertor.assertCalled("getComponentTypes");
	}

	public void testGetConverterIds() throws Exception {
		decorator.getConverterIds();
		methodCallAssertor.assertCalled("getConverterIds");
	}

	public void testGetConverterTypes() throws Exception {
		decorator.getConverterTypes();
		methodCallAssertor.assertCalled("getConverterTypes");
	}

	public void testGetDefaultLocale() throws Exception {
		decorator.getDefaultLocale();
		methodCallAssertor.assertCalled("getDefaultLocale");
	}

	public void testGetDefaultRenderKitId() throws Exception {
		decorator.getDefaultRenderKitId();
		methodCallAssertor.assertCalled("getDefaultRenderKitId");
	}

	public void testGetELContextListeners() throws Exception {
		decorator.getELContextListeners();
		methodCallAssertor.assertCalled("getELContextListeners");
	}

	public void testGetELResolver() throws Exception {
		decorator.getELResolver();
		methodCallAssertor.assertCalled("getELResolver");
	}

	public void testGetExpressionFactory() throws Exception {
		decorator.getExpressionFactory();
		methodCallAssertor.assertCalled("getExpressionFactory");
	}

	public void testGetMessageBundle() throws Exception {
		decorator.getMessageBundle();
		methodCallAssertor.assertCalled("getMessageBundle");
	}

	public void testGetNavigationHandler() throws Exception {
		decorator.getNavigationHandler();
		methodCallAssertor.assertCalled("getNavigationHandler");
	}

	public void testGetPropertyResolver() throws Exception {
		decorator.getPropertyResolver();
		methodCallAssertor.assertCalled("getPropertyResolver");
	}

	public void testGetResourceBundle() throws Exception {
		decorator.getResourceBundle(null, "name");
		methodCallAssertor.assertCalled("getResourceBundle");
	}

	public void testGetStateManager() throws Exception {
		decorator.getStateManager();
		methodCallAssertor.assertCalled("getStateManager");
	}

	public void testGetSupportedLocales() throws Exception {
		decorator.getSupportedLocales();
		methodCallAssertor.assertCalled("getSupportedLocales");
	}

	public void testGetValidatorIds() throws Exception {
		decorator.getValidatorIds();
		methodCallAssertor.assertCalled("getValidatorIds");
	}

	public void testGetVariableResolver() throws Exception {
		decorator.getVariableResolver();
		methodCallAssertor.assertCalled("getVariableResolver");
	}

	public void testGetViewHandler() throws Exception {
		decorator.getViewHandler();
		methodCallAssertor.assertCalled("getViewHandler");
	}

	public void testRemoveELContextListener() throws Exception {
		decorator.removeELContextListener(null);
		methodCallAssertor.assertCalled("removeELContextListener");
	}

	public void testSetActionListener() throws Exception {
		decorator.setActionListener(null);
		methodCallAssertor.assertCalled("setActionListener");
	}

	public void testSetDefaultLocale() throws Exception {
		decorator.setDefaultLocale(null);
		methodCallAssertor.assertCalled("setDefaultLocale");
	}

	public void testSetDefaultRenderKitId() throws Exception {
		decorator.setDefaultRenderKitId("renderKitId");
		methodCallAssertor.assertCalled("setDefaultRenderKitId");
	}

	public void testSetMessageBundle() throws Exception {
		decorator.setMessageBundle("bundle");
		methodCallAssertor.assertCalled("setMessageBundle");
	}

	public void testSetNavigationHandler() throws Exception {
		decorator.setNavigationHandler(null);
		methodCallAssertor.assertCalled("setNavigationHandler");
	}

	public void testSetPropertyResolver() throws Exception {
		decorator.setPropertyResolver(null);
		methodCallAssertor.assertCalled("setPropertyResolver");
	}

	public void testSetStateManager() throws Exception {
		decorator.setStateManager(null);
		methodCallAssertor.assertCalled("setStateManager");
	}

	public void testSetSupportedLocales() throws Exception {
		decorator.setSupportedLocales(null);
		methodCallAssertor.assertCalled("setSupportedLocales");
	}

	public void testSetVariableResolver() throws Exception {
		decorator.setVariableResolver(null);
		methodCallAssertor.assertCalled("setVariableResolver");
	}

	public void testSetViewHandler() throws Exception {
		decorator.setViewHandler(null);
		methodCallAssertor.assertCalled("setViewHandler");
	}

	private static class MockApplicationDecorator extends AbstractApplicationDecorator {
		public MockApplicationDecorator(Application parent) {
			super(parent);
		}
	}
}
