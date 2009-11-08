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

import javax.faces.application.Application;
import javax.faces.application.StateManager;

import org.springframework.faces.webflow.FlowViewStateManager;

/**
 * JSF Application decorator for MVC.
 * 
 * @author Phillip Webb
 */
public class MvcApplication extends AbstractApplicationDecorator {

	public MvcApplication(Application parent) {
		super(parent);
	}

	public void setStateManager(StateManager manager) {
		// We need to ensure that MVC state manager is above RichFaces, unfortunately this is not as straight forward as
		// it could be because we have the following issues to deal with:
		// - We need to be before the RichFaces AjaxStateManager class as this does not delegate
		// - JSF gives us no way of specifying StateManager order
		// - We cannot just insert from the ApplicationFactory as some chained factories cache the application
		// instance (RichFaces StateApplicationFactory for instance), this means if we end up behind that factory
		// and setStateManager is called after the initial construction we end up loosing our custom state manager
		// - The web flow ApplicationFactory will always replace the top element if it is not a FlowViewStateManager
		manager = new MvcStateManager(manager);
		manager = new FlowViewStateManager(manager);
		super.setStateManager(manager);
	}
}
