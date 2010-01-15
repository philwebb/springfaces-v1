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
package org.springframework.faces.mvc.navigation;

import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * Interface used to provide context for {@link NavigationOutcomeExpressionResolver} implementations.
 * 
 * @author Phillip Webb
 */
public interface NavigationOutcomeExpressionContext {

	/**
	 * Provides access to the current native web request that caused the navigation outcome.
	 * 
	 * @return The current native web request
	 */
	public NativeWebRequest getWebRequest();

	/**
	 * Factory method that can be used to create a fully initialized {@link WebDataBinder}. The {@link WebDataBinder}
	 * will be initialized using {@link InitBinder} annotated methods from the controller.
	 * 
	 * @param attrName The attribute name or <tt>null</tt> if the binder does not relate to a specific attribute
	 * @param target The target object or <tt>null</tt> if the binder does not relate to a specific target
	 * @param objectName The name of the object being bound or <tt>null</tt> to use the default name
	 * @return A fully initialized {@link WebDataBinder} instance
	 * 
	 * @throws Exception on error
	 */
	public WebDataBinder createDataBinder(String attrName, Object target, String objectName) throws Exception;
}
