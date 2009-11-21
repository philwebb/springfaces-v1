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
import javax.faces.application.ApplicationFactory;

/**
 * JSF Application Factory to insert the {@link MvcApplication} decorator.
 * 
 * @author Phillip Webb
 */
public class MvcApplicationFactory extends ApplicationFactory {

	private ApplicationFactory delegate;

	private Application application;

	public MvcApplicationFactory(ApplicationFactory delegate) {
		this.delegate = delegate;
	}

	public Application getApplication() {
		if (application == null) {
			Application delegateApplication = delegate.getApplication();
			application = delegateApplication == null ? null : new MvcApplication(delegateApplication);
		}
		return application;
	}

	public void setApplication(Application application) {
		delegate.setApplication(application);
		this.application = new MvcApplication(application);
	}
}
