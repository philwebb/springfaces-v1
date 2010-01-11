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

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.springframework.faces.mvc.FacesHandler;
import org.springframework.faces.mvc.NavigationRequestEvent;

public class MvcFacesRequestContextTests extends TestCase {

	private static final class LifeCycleRun implements Runnable {
		private boolean wait;

		public LifeCycleRun(boolean wait) {
			this.wait = wait;
		}

		public void run() {
			MvcFacesContext mvcFacesContext = (MvcFacesContext) EasyMock.createMock(MvcFacesContext.class);
			FacesHandler facesHandler = (FacesHandler) EasyMock.createMock(FacesHandler.class);
			final MvcFacesRequestControlContextImpl requestContext = new MvcFacesRequestControlContextImpl(mvcFacesContext,
					facesHandler);
			try {
				while (wait) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
				assertSame(facesHandler, MvcFacesRequestContextHolder.getRequestContext().getFacesHandler());
				assertSame(mvcFacesContext, MvcFacesRequestContextHolder.getRequestContext().getMvcFacesContext());
			} finally {
				requestContext.release();
			}
		}

		public void setWait(boolean wait) {
			this.wait = wait;
		}
	}

	public void testNormalLifecycle() throws Exception {
		LifeCycleRun thisRun = new LifeCycleRun(false);
		LifeCycleRun threadRun = new LifeCycleRun(true);
		Thread thread = new Thread(threadRun);
		thread.start();
		Thread.sleep(100);
		thisRun.run();
		threadRun.setWait(false);
		thread.join();
	}

	public void testDoubleRelease() throws Exception {
		MvcFacesContext mvcFacesContext = (MvcFacesContext) EasyMock.createMock(MvcFacesContext.class);
		FacesHandler facesHandler = (FacesHandler) EasyMock.createMock(FacesHandler.class);
		MvcFacesRequestControlContextImpl requestContext = new MvcFacesRequestControlContextImpl(mvcFacesContext, facesHandler);
		requestContext.release();
		try {
			requestContext.release();
			fail("Double release");
		} catch (IllegalStateException e) {
			assertEquals("The MvcFacesRequest has already been released", e.getMessage());
		}
	}

	public void testSetGetException() throws Exception {
		MvcFacesContext mvcFacesContext = (MvcFacesContext) EasyMock.createMock(MvcFacesContext.class);
		FacesHandler facesHandler = (FacesHandler) EasyMock.createMock(FacesHandler.class);
		MvcFacesRequestControlContextImpl requestContext = new MvcFacesRequestControlContextImpl(mvcFacesContext, facesHandler);
		Exception exception = new Exception();
		requestContext.setException(exception);
		assertSame(exception, requestContext.getException());
	}

	public void testSetGetLastNavigationRequestEvent() throws Exception {
		MvcFacesContext mvcFacesContext = (MvcFacesContext) EasyMock.createMock(MvcFacesContext.class);
		FacesHandler facesHandler = (FacesHandler) EasyMock.createMock(FacesHandler.class);
		MvcFacesRequestControlContextImpl requestContext = new MvcFacesRequestControlContextImpl(mvcFacesContext, facesHandler);
		NavigationRequestEvent event = new NavigationRequestEvent(this, null, "outcome");
		requestContext.setLastNavigationRequestEvent(event);
		assertSame(event, requestContext.getLastNavigationRequestEvent());
	}
}
