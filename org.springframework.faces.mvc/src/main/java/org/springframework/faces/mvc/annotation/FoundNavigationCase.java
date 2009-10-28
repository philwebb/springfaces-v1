package org.springframework.faces.mvc.annotation;

import java.lang.reflect.Method;

import org.springframework.core.style.ToStringCreator;
import org.springframework.faces.bind.annotation.NavigationCase;
import org.springframework.util.Assert;

public final class FoundNavigationCase {

	enum FoundNavigationCaseType {
		METHOD, CLASS, PACKAGE
	}

	private NavigationCase navigationCase;
	private Object owner;
	private FoundNavigationCaseType type;

	public FoundNavigationCase(NavigationCase navigationCase, Object owner) {
		Assert.notNull(navigationCase, "navigationCase is required");
		Assert.notNull(owner, "owner is required");
		this.navigationCase = navigationCase;
		this.owner = owner;
		if (owner instanceof Package) {
			this.type = FoundNavigationCaseType.PACKAGE;
		}
		if (owner instanceof Method) {
			this.type = FoundNavigationCaseType.METHOD;
		}
		if (owner instanceof Class) {
			this.type = FoundNavigationCaseType.CLASS;
		}
		if (this.type == null) {
			throw new IllegalArgumentException("owner must be a Method, Class or Package");
		}
	}

	public NavigationCase getNavigationCase() {
		return navigationCase;
	}

	public Object getOwner() {
		return owner;
	}

	public FoundNavigationCaseType getType() {
		return type;
	}

	public String toString() {
		return new ToStringCreator(this).append("navigationCase", navigationCase).append("type", type).append("owner",
				owner).toString();
	}
}
