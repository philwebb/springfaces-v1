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

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;

import org.apache.shale.test.base.AbstractJsfTestCase;
import org.easymock.EasyMock;
import org.springframework.faces.mvc.execution.RequestContext;
import org.springframework.faces.mvc.execution.RequestControlContext;
import org.springframework.faces.mvc.test.MvcFacesTestUtils;
import org.springframework.faces.mvc.test.MvcFacesTestUtils.MockRequestContextCallback;

public class MvcPhaseListenerTests extends AbstractJsfTestCase {

	public MvcPhaseListenerTests(String name) {
		super(name);
	}

	public void testPhaseListener() throws Exception {
		final MvcPhaseListener listener = new MvcPhaseListener();
		final PhaseEvent event = new PhaseEvent(facesContext, PhaseId.RESTORE_VIEW, lifecycle);

		MvcFacesTestUtils.doWithMockRequestContext(new MockRequestContextCallback() {
			public void prepare(RequestContext mvcFacesRequestContext) throws Exception {
				assertEquals(PhaseId.ANY_PHASE, listener.getPhaseId());
				((RequestControlContext) mvcFacesRequestContext).getExecution().beforePhase(
						mvcFacesRequestContext, event);
				EasyMock.expectLastCall();
				((RequestControlContext) mvcFacesRequestContext).getExecution().afterPhase(
						mvcFacesRequestContext, event);
				EasyMock.expectLastCall();
			}

			public void execute(RequestContext mvcFacesRequestContext) throws Exception {
				listener.beforePhase(event);
				listener.afterPhase(event);
			}
		});
	}
}
