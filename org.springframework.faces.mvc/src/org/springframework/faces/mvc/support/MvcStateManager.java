package org.springframework.faces.mvc.support;

import java.io.IOException;

import javax.faces.application.StateManager;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

public class MvcStateManager extends StateManager {

	private StateManager delegate;

	public MvcStateManager(StateManager delegate) {
		this.delegate = delegate;
	}

	public void writeState(FacesContext context, javax.faces.application.StateManager.SerializedView state)
			throws IOException {
		if (MvcFacesRequestContext.getCurrentInstance() != null) {
			MvcFacesRequestContext.getCurrentInstance().getMvcFacesContext().writeState(context);
		}
		delegate.writeState(context, state);
	}

	public boolean isSavingStateInClient(FacesContext context) {
		return delegate.isSavingStateInClient(context);
	}

	public javax.faces.application.StateManager.SerializedView saveSerializedView(FacesContext context) {
		return delegate.saveSerializedView(context);
	}

	public Object saveView(FacesContext context) {
		return delegate.saveView(context);
	}

	public UIViewRoot restoreView(FacesContext context, String viewId, String renderKitId) {
		return delegate.restoreView(context, viewId, renderKitId);
	}
}
