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
package org.springframework.faces.mvc.annotation;

import org.springframework.faces.mvc.NavigationLocation;

/**
 * Strategy interface used to resolve navigation outcome expressions.
 * 
 * @see NavigationOutcomeExpressionElResolver
 * 
 * @author Phillip Webb
 */
public interface NavigationOutcomeExpressionResolver {

	/**
	 * Resolve any expression strings contained in the specified outcome.
	 * 
	 * @param context The navigation outcome expression context
	 * @param outcome The navigation outcome
	 * @return A fully resolved outcome.
	 * 
	 * @throws Exception
	 */
	public NavigationLocation resolveNavigationOutcome(NavigationOutcomeExpressionContext context,
			NavigationLocation outcome) throws Exception;
}
