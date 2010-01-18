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

import org.springframework.core.style.ToStringCreator;
import org.springframework.util.ObjectUtils;

/**
 * Data holder class used to encapsulate a navigation location and meta-data. Navigation location objects are processed
 * by {@link RedirectHandler} implementations in order to redirect the browser. Location objects can be any type
 * supported by the redirect handler, although most often <tt>String</tt> values.
 * <p>
 * Each navigation location can also include the following meta-data:
 * <ul>
 * <li><tt>popup</tt></li> - Renders the redirected view in a modal popup dailog. When using Spring Faces MVC with the
 * Spring Javascript, no client side code is necessary for the popup to display. The redirect handler will send a
 * response to the client requesting a redirect to the view from a popup, and the client will honor the request.
 * <p>
 * The <tt>fragemnts</tt> attribute is often used in conjunction with <tt>popup</tt> in order to render only a limited
 * part of the response inside the dialog.
 * <p>
 * This functionality will degrade gracefully if JavaScript is not enabled on the clients web browser.
 * <li><tt>fragments</tt> - Specifies the fragements that will be rendered on the response. Fragments are used when
 * rendering an ajax response to a navigation request. Usually this means that fragments can only be used when
 * <tt>popup</tt> is <tt>true</tt> or when the <tt>location</tt> is null (meaning that the current page is re-rendered).
 * <p>
 * Note: Framents cannot be used then redirecting the user to a new page.</li>
 * 
 * @see RedirectHandler
 * 
 * @author Phillip Webb
 */
public final class NavigationLocation {

	private Object location;
	private Boolean popup;
	private String[] fragments;

	/**
	 * Constructor.
	 * @param location The navigation location
	 * @param popup If the redirect should be rendered as a popup modal dialog
	 * @param fragments The fragments that will be re-rendered in the response
	 */
	public NavigationLocation(Object location, boolean popup, String[] fragments) {
		this.location = location;
		this.popup = new Boolean(popup);
		this.fragments = fragments == null ? new String[] {} : fragments;
	}

	/**
	 * Convenience constructor that can be used when <tt>popup</tt> and <tt>fragments</tt> are not needed.
	 * @param location The navigation location
	 */
	public NavigationLocation(Object location) {
		this(location, false, null);
	}

	/**
	 * @return The navigation location
	 */
	public Object getLocation() {
		return location;
	}

	/**
	 * @return If the redirect should be rendered as a popup modal dialog
	 */
	public boolean getPopup() {
		return popup.booleanValue();
	}

	/**
	 * @return The fragments that will be re-rendered in the response
	 */
	public String[] getFragments() {
		return fragments;
	}

	public String toString() {
		return new ToStringCreator(this).append("location", location).append("popup", popup).append("fragments",
				fragments).toString();
	}

	public int hashCode() {
		return ObjectUtils.nullSafeHashCode(new Object[] { location, popup, fragments });
	}

	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj instanceof NavigationLocation) {
			NavigationLocation other = (NavigationLocation) obj;
			return ObjectUtils.nullSafeEquals(location, other.location) && popup.equals(other.popup)
					&& ObjectUtils.nullSafeEquals(fragments, other.fragments);
		}
		return false;
	}
}
