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
package org.springframework.faces.mvc;

import javax.faces.event.PhaseId;

/**
 * Call-back interface that can be used by {@link MvcFacesExceptionHandler} implementations to request that a specific
 * action is taken after the exception has been handled.
 * 
 * @author Phillip Webb
 */
public interface MvcFacesExceptionOutcome {

	/**
	 * Issue a redirect to the specific location. The redirect will be handled using the {@link RedirectHandler} from
	 * the {@link AbstractFacesHandlerAdapter} that is processing the request.
	 * 
	 * @param location The redirect location.
	 */
	public void redirect(NavigationLocation location);

	/**
	 * Re-rendered the current view up to the point of {@link PhaseId#PROCESS_VALIDATIONS}.
	 */
	public void redisplay();
}
