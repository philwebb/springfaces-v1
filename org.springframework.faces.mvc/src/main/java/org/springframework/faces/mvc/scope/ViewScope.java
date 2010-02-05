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
package org.springframework.faces.mvc.scope;

import org.springframework.beans.factory.config.Scope;
import org.springframework.faces.mvc.execution.RequestContext;
import org.springframework.webflow.core.collection.MutableAttributeMap;

/**
 * View {@link Scope scope} implementation from Spring Faces MVC.
 * 
 * @see RequestContext#getViewScope()
 * @author Phillip Webb
 */
public class ViewScope extends AbstractFacesScope {
	protected MutableAttributeMap getScope() throws IllegalStateException {
		return getRequiredRequestContext().getViewScope();
	}
}