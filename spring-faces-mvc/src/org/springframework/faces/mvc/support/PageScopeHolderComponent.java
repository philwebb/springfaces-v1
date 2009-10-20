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

import java.util.HashMap;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.render.Renderer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

/**
 * A JSF component that can be used to hold page scope data. This allows for data to be stored within the JSF view and
 * restored during the faces lifecyle.
 * 
 * @author Keith Donald
 * @author Phillip Webb
 */
public class PageScopeHolderComponent extends UIComponentBase {

	protected final static Log logger = LogFactory.getLog(PageScopeHolderComponent.class);

	private static final String COMPONENT_FAMILY = "javax.faces.Parameter";

	/**
	 * Immutable id of the flow execution key component for easier lookup later.
	 */
	public static final String COMPONENT_ID = "MvcStateHolder";

	private boolean transientValue;

	private Map pageScope;

	public String getId() {
		return COMPONENT_ID;
	}

	public void setId(String id) {
		// Do nothing so as to ensure the id never gets overwritten.
		return;
	}

	public String getClientId(FacesContext context) {
		return COMPONENT_ID;
	}

	public String getFamily() {
		return COMPONENT_FAMILY;
	}

	public Renderer getRenderer() {
		// this component is not rendered
		return null;
	}

	public boolean isTransient() {
		return transientValue;
	}

	public void setTransient(boolean transientValue) {
		this.transientValue = transientValue;
	}

	public void restoreState(FacesContext context, Object state) {
		Object values[] = (Object[]) state;
		pageScope = (Map) values[0];
	}

	public Object saveState(FacesContext context) {
		Object values[] = new Object[1];
		values[0] = pageScope;
		return values;
	}

	public Map getPageScope() {
		if (pageScope == null) {
			pageScope = new HashMap();
		}
		return pageScope;
	}

	public static void attach(FacesContext facesContext, UIViewRoot viewRoot) {
		viewRoot.getChildren().add(new PageScopeHolderComponent());
	}

	public static PageScopeHolderComponent locate(FacesContext facesContext, UIViewRoot viewRoot) {
		viewRoot = viewRoot == null ? facesContext.getViewRoot() : viewRoot;
		UIComponent component = viewRoot.findComponent(COMPONENT_ID);
		Assert.notNull(component, "The MVC State Holder component cannot be found in the specified viewRoot, "
				+ "perhaps you are not rendering this view using Spring MVC");
		return (PageScopeHolderComponent) component;
	}

	public static PageScopeHolderComponent locate(FacesContext context) {
		return locate(context, null);
	}
}
