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

import java.util.Collection;
import java.util.Collections;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import org.springframework.web.jsf.DelegatingPhaseListenerMulticaster;

/**
 * {@link PhaseListener} implementation that provides integration with Spring MVC.
 * 
 * @author Phillip Webb
 */
public class MvcPhaseListener extends DelegatingPhaseListenerMulticaster {

	protected Collection getDelegates(FacesContext facesContext) {
		if (MvcFacesRequestContextHolder.getRequestContext() != null) {
			final MvcFacesRequestContext requestContext = MvcFacesRequestContextHolder.getRequestContext();
			return Collections.singleton(new PhaseListener() {

				public PhaseId getPhaseId() {
					return PhaseId.ANY_PHASE;
				}

				public void beforePhase(PhaseEvent event) {
					requestContext.getMvcFacesContext().beforePhase(requestContext, event);
				}

				public void afterPhase(PhaseEvent event) {
					requestContext.getMvcFacesContext().afterPhase(requestContext, event);
				}
			});
		}
		return Collections.EMPTY_SET;
	}
}
