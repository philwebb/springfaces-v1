package org.springframework.faces.mvc;

import org.springframework.core.style.ToStringCreator;
import org.springframework.util.ObjectUtils;

public final class NavigationLocation {

	private Object location;
	private Boolean popup;

	public NavigationLocation(Object location, boolean popup) {
		this.location = location;
		this.popup = new Boolean(popup);
	}

	public NavigationLocation(Object location) {
		this(location, false);
	}

	public Object getLocation() {
		return location;
	}

	public boolean getPopup() {
		return popup.booleanValue();
	}

	public String toString() {
		return new ToStringCreator(this).append("location", location).append("popup", popup).toString();
	}

	public int hashCode() {
		return ObjectUtils.nullSafeHashCode(new Object[] { location, popup });
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
			return ObjectUtils.nullSafeEquals(location, other.location) && popup.equals(other.popup);
		}
		return false;
	}

}
