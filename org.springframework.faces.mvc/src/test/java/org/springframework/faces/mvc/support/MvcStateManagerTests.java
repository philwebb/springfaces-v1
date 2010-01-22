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

import javax.faces.application.StateManager;

import org.apache.shale.test.base.AbstractJsfTestCase;
import org.easymock.EasyMock;
import org.springframework.faces.mvc.execution.MvcFacesRequestContext;
import org.springframework.faces.mvc.execution.MvcFacesRequestControlContext;
import org.springframework.faces.mvc.test.MvcFacesTestUtils;
import org.springframework.faces.mvc.test.MvcFacesTestUtils.MethodCallAssertor;
import org.springframework.faces.mvc.test.MvcFacesTestUtils.MockMvcFacesRequestContextCallback;

public class MvcStateManagerTests extends AbstractJsfTestCase {

	public MvcStateManagerTests(String name) {
		super(name);
	}

	public void testCallsDelegate() throws Exception {
		StateManager delegate = (StateManager) MvcFacesTestUtils.methodTrackingObject(StateManager.class);
		MvcStateManager stateManager = new MvcStateManager(delegate);
		String[] methods = new String[] { "writeState", "isSavingStateInClient", "saveSerializedView", "saveView",
				"restoreView" };
		MvcFacesTestUtils.callMethods(stateManager, methods);
		((MethodCallAssertor) delegate).assertCalled(methods);
	}

	public void testWriteStatePropagates() throws Exception {
		MvcFacesTestUtils.doWithMockMvcFacesRequestContext(new MockMvcFacesRequestContextCallback() {
			public void prepare(MvcFacesRequestContext mvcFacesRequestContext) throws Exception {
				((MvcFacesRequestControlContext) mvcFacesRequestContext).getExecution().writeState(facesContext);
				EasyMock.expectLastCall();
			}

			public void execute(MvcFacesRequestContext mvcFacesRequestContext) throws Exception {
				StateManager delegate = (StateManager) MvcFacesTestUtils.nullImplementation(StateManager.class);
				MvcStateManager stateManager = new MvcStateManager(delegate);
				stateManager.writeState(facesContext, null);
			}
		});
	}

}
