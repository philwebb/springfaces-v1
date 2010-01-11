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

import junit.framework.TestCase;

import org.springframework.faces.mvc.test.MvcFacesTestUtils;
import org.springframework.faces.mvc.test.MvcFacesTestUtils.MethodCallAssertor;

public class MvcApplicationFactoryTests extends TestCase {

	private Application application;

	public void testGetApplicationWhenNotSet() throws Exception {
		this.application = (Application) MvcFacesTestUtils.methodTrackingObject(Application.class);
		MvcApplicationFactory factory = new MvcApplicationFactory(new MockApplicationFactory());
		Application getApplication = factory.getApplication();
		assertTrue(getApplication instanceof MvcApplication);
		getApplication.setDefaultLocale(null);
		((MethodCallAssertor) application).assertCalled("setDefaultLocale");
	}

	public void testSetApplication() throws Exception {
		Application applicationToSet = (Application) MvcFacesTestUtils.methodTrackingObject(Application.class);
		MvcApplicationFactory factory = new MvcApplicationFactory(new MockApplicationFactory());
		assertNull(factory.getApplication());
		factory.setApplication(applicationToSet);
		Application getApplication = factory.getApplication();
		assertTrue(getApplication instanceof MvcApplication);
		assertSame(applicationToSet, application);
	}

	private class MockApplicationFactory extends ApplicationFactory {

		public Application getApplication() {
			return MvcApplicationFactoryTests.this.application;
		}

		public void setApplication(Application application) {
			MvcApplicationFactoryTests.this.application = application;
		}
	}
}
