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
import org.springframework.faces.mvc.context.ExternalContext;
import org.springframework.faces.mvc.context.MvcFacesExecution;
import org.springframework.faces.mvc.execution.RequestContextHolder;
import org.springframework.faces.mvc.execution.RequestControlContext;
import org.springframework.faces.mvc.execution.RequestControlContextImpl;
import org.springframework.faces.mvc.navigation.NavigationRequestEvent;
import org.springframework.faces.mvc.servlet.FacesHandler;

public class RequestContextTests extends TestCase {

	private static final class LifeCycleRun implements Runnable {
		private boolean wait;

		public LifeCycleRun(boolean wait) {
			this.wait = wait;
		}

		public void run() {
			ExternalContext externalContext = (ExternalContext) EasyMock.createMock(ExternalContext.class);
			MvcFacesExecution execution = (MvcFacesExecution) EasyMock.createMock(MvcFacesExecution.class);
			FacesHandler facesHandler = (FacesHandler) EasyMock.createMock(FacesHandler.class);
			final RequestControlContextImpl requestContext = new RequestControlContextImpl(externalContext, execution,
					facesHandler);
			try {
				while (wait) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
				assertSame(facesHandler, RequestContextHolder.getRequestContext().getFacesHandler());
				assertSame(execution, ((RequestControlContext) RequestContextHolder.getRequestContext()).getExecution());
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
		ExternalContext externalContext = (ExternalContext) EasyMock.createMock(ExternalContext.class);
		MvcFacesExecution execution = (MvcFacesExecution) EasyMock.createMock(MvcFacesExecution.class);
		FacesHandler facesHandler = (FacesHandler) EasyMock.createMock(FacesHandler.class);
		RequestControlContextImpl requestContext = new RequestControlContextImpl(externalContext, execution,
				facesHandler);
		requestContext.release();
		try {
			requestContext.release();
			fail("Double release");
		} catch (IllegalStateException e) {
			assertEquals("The RequestContext has already been released", e.getMessage());
		}
	}

	public void testSetGetException() throws Exception {
		ExternalContext externalContext = (ExternalContext) EasyMock.createMock(ExternalContext.class);
		MvcFacesExecution execution = (MvcFacesExecution) EasyMock.createMock(MvcFacesExecution.class);
		FacesHandler facesHandler = (FacesHandler) EasyMock.createMock(FacesHandler.class);
		RequestControlContextImpl requestContext = new RequestControlContextImpl(externalContext, execution,
				facesHandler);
		Exception exception = new Exception();
		requestContext.setException(exception);
		assertSame(exception, requestContext.getException());
	}

	public void testSetGetLastNavigationRequestEvent() throws Exception {
		ExternalContext externalContext = (ExternalContext) EasyMock.createMock(ExternalContext.class);
		MvcFacesExecution execution = (MvcFacesExecution) EasyMock.createMock(MvcFacesExecution.class);
		FacesHandler facesHandler = (FacesHandler) EasyMock.createMock(FacesHandler.class);
		RequestControlContextImpl requestContext = new RequestControlContextImpl(externalContext, execution,
				facesHandler);
		NavigationRequestEvent event = new NavigationRequestEvent(this, null, "outcome");
		requestContext.setLastNavigationRequestEvent(event);
		assertSame(event, requestContext.getLastNavigationRequestEvent());
	}
}
