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

import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.el.ELContextListener;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.NavigationHandler;
import javax.faces.application.StateManager;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.el.MethodBinding;
import javax.faces.el.PropertyResolver;
import javax.faces.el.ReferenceSyntaxException;
import javax.faces.el.ValueBinding;
import javax.faces.el.VariableResolver;
import javax.faces.event.ActionListener;
import javax.faces.validator.Validator;

/**
 * Abstract base class for {@link Application} decorators.
 * 
 * @author Phillip Webb
 */
public abstract class AbstractApplicationDecorator extends Application {
	private final Application parent;

	/**
	 * Constructor.
	 * 
	 * @param parent
	 */
	public AbstractApplicationDecorator(Application parent) {
		super();
		this.parent = parent;
	}

	public void addComponent(String componentType, String componentClass) {
		parent.addComponent(componentType, componentClass);
	}

	public void addConverter(Class targetClass, String converterClass) {
		parent.addConverter(targetClass, converterClass);
	}

	public void addConverter(String converterId, String converterClass) {
		parent.addConverter(converterId, converterClass);
	}

	public void addELContextListener(ELContextListener listener) {
		parent.addELContextListener(listener);
	}

	public void addELResolver(ELResolver resolver) {
		parent.addELResolver(resolver);
	}

	public void addValidator(String validatorId, String validatorClass) {
		parent.addValidator(validatorId, validatorClass);
	}

	public UIComponent createComponent(String componentType) throws FacesException {
		return parent.createComponent(componentType);
	}

	public UIComponent createComponent(ValueBinding componentBinding, FacesContext context, String componentType)
			throws FacesException {
		return parent.createComponent(componentBinding, context, componentType);
	}

	public UIComponent createComponent(ValueExpression componentExpression, FacesContext context, String componentType)
			throws FacesException {
		return parent.createComponent(componentExpression, context, componentType);
	}

	public Converter createConverter(Class targetClass) {
		return parent.createConverter(targetClass);
	}

	public Converter createConverter(String converterId) {
		return parent.createConverter(converterId);
	}

	public MethodBinding createMethodBinding(String ref, Class[] params) throws ReferenceSyntaxException {
		return parent.createMethodBinding(ref, params);
	}

	public Validator createValidator(String validatorId) throws FacesException {
		return parent.createValidator(validatorId);
	}

	public ValueBinding createValueBinding(String ref) throws ReferenceSyntaxException {
		return parent.createValueBinding(ref);
	}

	public Object evaluateExpressionGet(FacesContext context, String expression, Class expectedType) throws ELException {
		return parent.evaluateExpressionGet(context, expression, expectedType);
	}

	public ActionListener getActionListener() {
		return parent.getActionListener();
	}

	public Iterator getComponentTypes() {
		return parent.getComponentTypes();
	}

	public Iterator getConverterIds() {
		return parent.getConverterIds();
	}

	public Iterator getConverterTypes() {
		return parent.getConverterTypes();
	}

	public Locale getDefaultLocale() {
		return parent.getDefaultLocale();
	}

	public String getDefaultRenderKitId() {
		return parent.getDefaultRenderKitId();
	}

	public ELContextListener[] getELContextListeners() {
		return parent.getELContextListeners();
	}

	public ELResolver getELResolver() {
		return parent.getELResolver();
	}

	public ExpressionFactory getExpressionFactory() {
		return parent.getExpressionFactory();
	}

	public String getMessageBundle() {
		return parent.getMessageBundle();
	}

	public NavigationHandler getNavigationHandler() {
		return parent.getNavigationHandler();
	}

	public PropertyResolver getPropertyResolver() {
		return parent.getPropertyResolver();
	}

	public ResourceBundle getResourceBundle(FacesContext ctx, String name) {
		return parent.getResourceBundle(ctx, name);
	}

	public StateManager getStateManager() {
		return parent.getStateManager();
	}

	public Iterator getSupportedLocales() {
		return parent.getSupportedLocales();
	}

	public Iterator getValidatorIds() {
		return parent.getValidatorIds();
	}

	public VariableResolver getVariableResolver() {
		return parent.getVariableResolver();
	}

	public ViewHandler getViewHandler() {
		return parent.getViewHandler();
	}

	public void removeELContextListener(ELContextListener listener) {
		parent.removeELContextListener(listener);
	}

	public void setActionListener(ActionListener listener) {
		parent.setActionListener(listener);
	}

	public void setDefaultLocale(Locale locale) {
		parent.setDefaultLocale(locale);
	}

	public void setDefaultRenderKitId(String renderKitId) {
		parent.setDefaultRenderKitId(renderKitId);
	}

	public void setMessageBundle(String bundle) {
		parent.setMessageBundle(bundle);
	}

	public void setNavigationHandler(NavigationHandler handler) {
		parent.setNavigationHandler(handler);
	}

	public void setPropertyResolver(PropertyResolver resolver) {
		parent.setPropertyResolver(resolver);
	}

	public void setStateManager(StateManager manager) {
		parent.setStateManager(manager);
	}

	public void setSupportedLocales(Collection locales) {
		parent.setSupportedLocales(locales);
	}

	public void setVariableResolver(VariableResolver resolver) {
		parent.setVariableResolver(resolver);
	}

	public void setViewHandler(ViewHandler handler) {
		parent.setViewHandler(handler);
	}
}
