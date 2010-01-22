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

import javax.faces.FacesException;
import javax.faces.application.NavigationHandler;
import javax.faces.context.FacesContext;

import org.apache.shale.test.base.AbstractJsfTestCase;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.springframework.faces.mvc.FacesHandler;
import org.springframework.faces.mvc.execution.MvcFacesRequestContext;
import org.springframework.faces.mvc.execution.MvcFacesRequestControlContext;
import org.springframework.faces.mvc.navigation.NavigationLocation;
import org.springframework.faces.mvc.navigation.NavigationRequestEvent;
import org.springframework.faces.mvc.test.MvcFacesTestUtils;
import org.springframework.faces.mvc.test.MvcFacesTestUtils.MethodCallAssertor;
import org.springframework.faces.mvc.test.MvcFacesTestUtils.MockMvcFacesRequestContextCallback;

public class MvcNavigationHandlerTests extends AbstractJsfTestCase {

	public MvcNavigationHandlerTests(String name) {
		super(name);
	}

	public void doTestNavigate(final NavigationLocation location) throws Exception {
		NavigationHandler delegate = (NavigationHandler) MvcFacesTestUtils
				.methodTrackingObject(NavigationHandler.class);
		final MvcNavigationHandler handler = new MvcNavigationHandler(delegate);
		MvcFacesTestUtils.doWithMockMvcFacesRequestContext(new MockMvcFacesRequestContextCallback() {
			public void prepare(MvcFacesRequestContext mvcFacesRequestContext) throws Exception {
				FacesHandler facesHandler = mvcFacesRequestContext.getFacesHandler();
				EasyMock.expect(
						facesHandler.getNavigationOutcomeLocation((FacesContext) EasyMock.eq(facesContext),
								(NavigationRequestEvent) EasyMock.anyObject())).andAnswer(new IAnswer() {
					public Object answer() throws Throwable {
						NavigationRequestEvent event = (NavigationRequestEvent) EasyMock.getCurrentArguments()[1];
						assertEquals("action", event.getFromAction());
						assertEquals("outcome", event.getOutcome());
						return location;
					}
				});
				// if navigating ensure the context is called
				if (location != null) {
					((MvcFacesRequestControlContext) mvcFacesRequestContext).getExecution().redirect(facesContext,
							mvcFacesRequestContext, location);
					EasyMock.expectLastCall();
				}
			}

			public void execute(MvcFacesRequestContext mvcFacesRequestContext) throws Exception {
				handler.handleNavigation(facesContext, "action", "outcome");
				assertEquals("action", mvcFacesRequestContext.getLastNavigationRequestEvent().getFromAction());
				assertEquals("outcome", mvcFacesRequestContext.getLastNavigationRequestEvent().getOutcome());
			}
		});
		MethodCallAssertor assertor = (MethodCallAssertor) delegate;
		if (location != null) {
			// if navigating ensure delegate was not called
			assertor.assertNotCalled("handleNavigation");
		} else {
			// if we could not navigate, ensure the delegate was asked
			assertor.assertCalled("handleNavigation");

		}
	}

	public void testNavigate() throws Exception {
		doTestNavigate(new NavigationLocation("navigate"));
	}

	public void testNavigateWithNoneFound() throws Exception {
		doTestNavigate(null);
	}

	public void testThrows() throws Exception {
		NavigationHandler delegate = (NavigationHandler) MvcFacesTestUtils
				.methodTrackingObject(NavigationHandler.class);
		final MvcNavigationHandler handler = new MvcNavigationHandler(delegate);
		try {
			MvcFacesTestUtils.doWithMockMvcFacesRequestContext(new MockMvcFacesRequestContextCallback() {
				public void prepare(MvcFacesRequestContext mvcFacesRequestContext) throws Exception {
					FacesHandler facesHandler = mvcFacesRequestContext.getFacesHandler();
					EasyMock.expect(
							facesHandler.getNavigationOutcomeLocation((FacesContext) EasyMock.eq(facesContext),
									(NavigationRequestEvent) EasyMock.anyObject())).andThrow(
							new IllegalStateException("Error"));
				}

				public void execute(MvcFacesRequestContext mvcFacesRequestContext) throws Exception {
					handler.handleNavigation(facesContext, "action", "outcome");
				}
			});
			fail("did not throw");
		} catch (FacesException e) {
			assertTrue(e.getCause() instanceof IllegalStateException);
			assertEquals("Error", e.getMessage());
		}
	}

	public void testWithoutMvcContext() throws Exception {
		NavigationHandler delegate = (NavigationHandler) MvcFacesTestUtils
				.methodTrackingObject(NavigationHandler.class);
		MvcNavigationHandler handler = new MvcNavigationHandler(delegate);
		handler.handleNavigation(facesContext, "action", "outcome");
		((MethodCallAssertor) delegate).assertCalled("handleNavigation");
	}
}
