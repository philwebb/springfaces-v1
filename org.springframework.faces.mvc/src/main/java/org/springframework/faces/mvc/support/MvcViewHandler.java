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
package org.springframework.faces.mvc.support;

import java.io.IOException;
import java.util.Locale;

import javax.faces.FacesException;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.faces.ui.AjaxViewRoot;
import org.springframework.js.ajax.SpringJavascriptAjaxHandler;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;

/**
 * A JSF {@link ViewHandler} that provides integration with Spring MVC.
 * 
 * @author Phillip Webb
 */
public class MvcViewHandler extends ViewHandler {

	private static SpringJavascriptAjaxHandler springJsAjaxHandler = new SpringJavascriptAjaxHandler();

	private ViewHandler delegate;

	public MvcViewHandler(ViewHandler delegate) {
		this.delegate = delegate;
	}

	public UIViewRoot createView(FacesContext context, String viewId) {
		if (MvcFacesRequestContextHolder.getRequestContext() != null) {
			MvcFacesRequestContext requestContext = MvcFacesRequestContextHolder.getRequestContext();
			ModelAndView modelAndView;
			try {
				modelAndView = requestContext.getFacesHandler().createView(context);
			} catch (Exception e) {
				throw new FacesException(e);
			}
			Assert.isNull(modelAndView.getView(), "MVC Faces can only support viewName references");

			// Deal with handler that renders response directly
			if (modelAndView.getViewName() == null) {
				context.responseComplete();
				return new EmptyUIViewRoot();
			}

			viewId = requestContext.getMvcFacesContext().resolveViewId(modelAndView.getViewName());
			UIViewRoot view = delegate.createView(context, viewId);
			requestContext.getMvcFacesContext().viewCreated(context, requestContext, view, modelAndView.getModel());

			// FIXME Need to detect this and create, possibly move elsewhere
			if (isSpringJavascriptAjaxRequest(context.getExternalContext())) {
				view = new AjaxViewRoot(view);
			}
			return view;
		}
		return delegate.createView(context, viewId);
	}

	private boolean isSpringJavascriptAjaxRequest(ExternalContext context) {
		// consider factoring out into external context
		if (context.getRequest() instanceof HttpServletRequest) {
			return springJsAjaxHandler.isAjaxRequest((HttpServletRequest) context.getRequest(),
					(HttpServletResponse) context.getResponse());
		} else {
			return false;
		}
	}

	public UIViewRoot restoreView(FacesContext context, String viewId) {
		boolean mvcRequest = false;
		if (MvcFacesRequestContextHolder.getRequestContext() != null) {
			MvcFacesRequestContext requestContext = MvcFacesRequestContextHolder.getRequestContext();
			String originalViewId = viewId;
			viewId = requestContext.getMvcFacesContext().getViewIdForRestore(context, viewId);
			Assert.notNull(viewId, "The MVC Faces Context could not map the view \"" + originalViewId
					+ "\" to a valid viewId");
			mvcRequest = true;
		}
		UIViewRoot view = delegate.restoreView(context, viewId);
		if (mvcRequest && isSpringJavascriptAjaxRequest(context.getExternalContext())) {
			view = new AjaxViewRoot(view);
		}
		return view;
	}

	public void renderView(FacesContext context, UIViewRoot viewToRender) throws IOException, FacesException {
		if (MvcFacesRequestContextHolder.getRequestContext() != null) {
			// Check to see if the response has already been rendered
			if (viewToRender instanceof EmptyUIViewRoot) {
				return;
			}
		}
		delegate.renderView(context, viewToRender);
	}

	public String getActionURL(FacesContext context, String viewId) {
		if (MvcFacesRequestContextHolder.getRequestContext() != null) {
			MvcFacesRequestContext requestContext = MvcFacesRequestContextHolder.getRequestContext();
			String actionUrl = requestContext.getMvcFacesContext().getActionUlr(context, viewId);
			Assert.notNull(actionUrl, "The action URL for the view \"" + viewId + "\" is not mapped");
			return actionUrl;
		}
		return delegate.getActionURL(context, viewId);
	}

	public Locale calculateLocale(FacesContext context) {
		return delegate.calculateLocale(context);
	}

	public String calculateRenderKitId(FacesContext context) {
		return delegate.calculateRenderKitId(context);
	}

	public String getResourceURL(FacesContext context, String path) {
		return delegate.getResourceURL(context, path);
	}

	public void writeState(FacesContext context) throws IOException {
		delegate.writeState(context);
	}

	/**
	 * {@link UIViewRoot} instance that is used when a view does not need to be rendered.
	 */
	private static class EmptyUIViewRoot extends UIViewRoot {
	}
}
