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
package org.springframework.faces.mvc;

import java.io.Serializable;
import java.util.Map;

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.faces.mvc.support.PageScopeHolderComponent;
import org.springframework.util.Assert;

/**
 * {@link Scope} implementation that allows beans to be tied to a JSF Page. Page scope store bean definitions inside a
 * JSF view so that they remain available throughout the JSF lifecyle. When a page scoped bean is accessed from a JSF
 * page it will be stored within the view root of the page, subsequent post-back operations will return the same bean
 * instance. Note: Only serializable beans can be stored in page scope.
 * 
 * @author Phillip Webb
 */
public class PageScope implements Scope {

	// FIXME refactor this to be like SWF scope implementations
	// FIXME should be named viewscope

	protected final Log logger = LogFactory.getLog(getClass());

	public Object get(String name, ObjectFactory objectFactory) {
		Map scope = getScope();
		Object scopedObject = scope.get(name);
		if (scopedObject == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("No scoped instance '" + name + "' found; creating new instance");
			}
			scopedObject = objectFactory.getObject();
			Assert.isInstanceOf(Serializable.class, scopedObject,
					"The bean \"name\" cannot be stored in page scope as it is not Serializable");
			scope.put(name, scopedObject);
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("Returning scoped instance '" + name + "'");
			}
		}
		return scopedObject;
	}

	public Object remove(String name) {
		return getScope().remove(name);
	}

	public Object resolveContextualObject(String key) {
		return null;
	}

	private Map getScope() {
		FacesContext context = FacesContext.getCurrentInstance();
		return PageScopeHolderComponent.locate(context, context.getViewRoot(), true).getPageScope();
	}

	public String getConversationId() {
		FacesContext context = FacesContext.getCurrentInstance();
		UIViewRoot viewRoot = (context == null ? null : context.getViewRoot());
		return (viewRoot == null ? null : viewRoot.getViewId());
	}

	public void registerDestructionCallback(String name, Runnable callback) {
		logger.warn("Destruction callback for '" + name + "' was not registered. Spring MVC Face does not "
				+ "support destruction of scoped beans.");
	}
}
