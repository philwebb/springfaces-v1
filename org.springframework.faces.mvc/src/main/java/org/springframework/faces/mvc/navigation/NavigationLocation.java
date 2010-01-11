package org.springframework.faces.mvc.navigation;

import org.springframework.core.style.ToStringCreator;
import org.springframework.util.ObjectUtils;

public final class NavigationLocation {

	private Object location;
	private Boolean popup;
	private String[] fragments;

	public NavigationLocation(Object location, boolean popup, String[] fragments) {
		this.location = location;
		this.popup = new Boolean(popup);
		this.fragments = fragments == null ? new String[] {} : fragments;
	}

	public NavigationLocation(Object location) {
		this(location, false, null);
	}

	public Object getLocation() {
		return location;
	}

	public boolean getPopup() {
		return popup.booleanValue();
	}

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
