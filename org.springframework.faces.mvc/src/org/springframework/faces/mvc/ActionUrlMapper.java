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

import java.io.IOException;

import javax.faces.context.FacesContext;

/**
 * Interface that is used to map between MVC JSF views and the action URL that should be used for faces post-back.
 * 
 * @see PageEncodedActionUrlMapper
 * 
 * @author Phillip Webb
 */
public interface ActionUrlMapper {

	/**
	 * Get the action URL that the view will use for post-back faces operations. Generally this method will either
	 * return a URL based on the current request or a global post-back URL. When post-back occurs the
	 * {@link #getViewNameForRestore(FacesContext)} method will be called to obtain the actual view to restore.
	 * 
	 * @param facesContext The faces context.
	 * @param viewName The MVC view name that is being rendered.
	 * 
	 * @return The action URL
	 */
	String getActionUlr(FacesContext facesContext, String viewName);

	/**
	 * Method that can be used to write additional state information inside the rendered view. This method will be
	 * called with the ResponseWriter at the correct position for the saved state to be written. Often this method is
	 * used to write hidden form inputs that can be retrieved during {@link #getViewNameForRestore(FacesContext)}.
	 * 
	 * @param facesContext
	 * @param viewName
	 * @throws IOException
	 */
	void writeState(FacesContext facesContext, String viewName) throws IOException;

	/**
	 * Called when a MVC JSF view is resumed to obtain the actual view to restore. Implementations of this interface
	 * must provide a mapping between a faces post-back and the actual view to restore. Unlike standard JSF, the view
	 * that is being rendered is determined by the controller and so there may not be a direct correlation to the
	 * request URL. The {@link #writeState(FacesContext, String)} method is often used in combination with this method.
	 * 
	 * @param facesContext The faces context. This can be used to obtain the request URL if required.
	 * @return The actual view name to restore.
	 */
	String getViewNameForRestore(FacesContext facesContext);

}
