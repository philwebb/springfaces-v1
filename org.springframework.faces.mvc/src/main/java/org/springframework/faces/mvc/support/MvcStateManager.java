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

import java.io.IOException;

import javax.faces.application.StateManager;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import org.springframework.faces.mvc.execution.MvcFacesRequestContextHolder;

/**
 * {@link StateManager} implementation that provides integration with Spring MVC.
 * 
 * @author Phillip Webb
 */
public class MvcStateManager extends StateManager {

	private StateManager delegate;

	public MvcStateManager(StateManager delegate) {
		this.delegate = delegate;
	}

	public void writeState(FacesContext context, StateManager.SerializedView state) throws IOException {
		if (MvcFacesRequestContextHolder.getRequestContext() != null) {
			MvcFacesRequestContextHolder.getRequestContext().getMvcFacesContext().writeState(context);
		}
		delegate.writeState(context, state);
	}

	public boolean isSavingStateInClient(FacesContext context) {
		return delegate.isSavingStateInClient(context);
	}

	public StateManager.SerializedView saveSerializedView(FacesContext context) {
		return delegate.saveSerializedView(context);
	}

	public Object saveView(FacesContext context) {
		return delegate.saveView(context);
	}

	public UIViewRoot restoreView(FacesContext context, String viewId, String renderKitId) {
		return delegate.restoreView(context, viewId, renderKitId);
	}
}
