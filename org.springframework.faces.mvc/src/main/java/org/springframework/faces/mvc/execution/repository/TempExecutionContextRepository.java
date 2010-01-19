package org.springframework.faces.mvc.execution.repository;

import java.util.HashMap;
import java.util.Map;

import org.springframework.faces.mvc.execution.ExecutionContextKey;
import org.springframework.faces.mvc.execution.MvcFacesRequestContext;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.core.collection.MutableAttributeMap;

//FIXME create proper
public class TempExecutionContextRepository implements ExecutionContextRepository {

	private static final Map state = new HashMap();

	public ExecutionContextKey parseKey(String key) {
		return new IntegerExecutionContextKey(Integer.parseInt(key));
	}

	public synchronized void restore(ExecutionContextKey key, MvcFacesRequestContext requestContext) {
		MutableAttributeMap flash = (MutableAttributeMap) state.get(key);
		requestContext.getFlashScope().replaceWith(flash);
	}

	public synchronized ExecutionContextKey save(MvcFacesRequestContext requestContext) {
		if (requestContext.getFlashScope().isEmpty()) {
			return null;
		}
		MutableAttributeMap flash = new LocalAttributeMap();
		flash.putAll(requestContext.getFlashScope());
		IntegerExecutionContextKey key = new IntegerExecutionContextKey(state.size());
		state.put(key, flash);
		return key;
	}
}
