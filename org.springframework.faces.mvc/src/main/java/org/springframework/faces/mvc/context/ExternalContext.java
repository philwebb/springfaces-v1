package org.springframework.faces.mvc.context;

import org.springframework.webflow.core.collection.SharedAttributeMap;

public interface ExternalContext {
	/**
	 * Provides access to the external session map, providing a storage for data local to the current user session and
	 * accessible to both internal and external MVC artifacts.
	 * @return the mutable session attribute map
	 */
	public SharedAttributeMap getSessionMap();

}
