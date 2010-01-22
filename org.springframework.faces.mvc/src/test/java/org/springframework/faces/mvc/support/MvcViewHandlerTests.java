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

import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import junit.framework.Assert;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.shale.test.base.AbstractJsfTestCase;
import org.easymock.EasyMock;
import org.springframework.faces.mvc.execution.MvcFacesRequestContext;
import org.springframework.faces.mvc.execution.MvcFacesRequestControlContext;
import org.springframework.faces.mvc.test.MvcFacesTestUtils;
import org.springframework.faces.mvc.test.MvcFacesTestUtils.MethodCallAssertor;
import org.springframework.faces.mvc.test.MvcFacesTestUtils.MockMvcFacesRequestContextCallback;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.xslt.XsltView;

public class MvcViewHandlerTests extends AbstractJsfTestCase {

	private static final String VIEW_ID = "someview";
	private static final String XHTML_VIEW_NAME = "someview.xhtml";
	private static final String ACTION_URL = "/web/someaction/url";

	public MvcViewHandlerTests(String name) {
		super(name);
	}

	public void testCallsDelegate() throws Exception {
		ViewHandler delegate = (ViewHandler) MvcFacesTestUtils.methodTrackingObject(ViewHandler.class);
		MvcViewHandler handler = new MvcViewHandler(delegate);
		String[] methods = new String[] { "createView", "restoreView", "renderView", "getActionURL", "calculateLocale",
				"calculateRenderKitId", "getResourceURL", "writeState" };
		MvcFacesTestUtils.callMethods(handler, methods);
		((MethodCallAssertor) delegate).assertCalled(methods);
	}

	public void testCreateView() throws Exception {
		final UIViewRoot uiViewRoot = new UIViewRoot();
		ViewHandler delegate = (ViewHandler) MvcFacesTestUtils.nullImplementation(ViewHandler.class,
				new MethodInterceptor() {
					public Object invoke(MethodInvocation invocation) throws Throwable {
						if ("createView".equals(invocation.getMethod().getName())) {
							return uiViewRoot;
						}
						return null;
					}
				});
		final MvcViewHandler handler = new MvcViewHandler(delegate);
		final ModelAndView modelAndView = new ModelAndView(VIEW_ID);
		modelAndView.getModel();
		MvcFacesTestUtils.doWithMockMvcFacesRequestContext(new MockMvcFacesRequestContextCallback() {
			public void prepare(MvcFacesRequestContext mvcFacesRequestContext) throws Exception {
				EasyMock.expect(mvcFacesRequestContext.getFacesHandler().createView(facesContext)).andReturn(
						modelAndView);
				EasyMock.expect(
						((MvcFacesRequestControlContext) mvcFacesRequestContext).getExecution().resolveViewId(VIEW_ID))
						.andReturn(XHTML_VIEW_NAME);
				((MvcFacesRequestControlContext) mvcFacesRequestContext).getExecution().viewCreated(
						(FacesContext) EasyMock.eq(facesContext),
						(MvcFacesRequestContext) EasyMock.eq(mvcFacesRequestContext),
						(UIViewRoot) EasyMock.eq(uiViewRoot), (ModelMap) EasyMock.eq(modelAndView.getModelMap()));
				EasyMock.expectLastCall();
			}

			public void execute(MvcFacesRequestContext mvcFacesRequestContext) throws Exception {
				handler.createView(facesContext, VIEW_ID);
			}
		});
	}

	public void testCreateViewFailsWhenModelAndViewUsesViewClass() throws Exception {
		final UIViewRoot uiViewRoot = new UIViewRoot();
		ViewHandler delegate = (ViewHandler) MvcFacesTestUtils.nullImplementation(ViewHandler.class,
				new MethodInterceptor() {
					public Object invoke(MethodInvocation invocation) throws Throwable {
						if ("createView".equals(invocation.getMethod().getName())) {
							return uiViewRoot;
						}
						return null;
					}
				});
		final MvcViewHandler handler = new MvcViewHandler(delegate);
		final ModelAndView modelAndView = new ModelAndView(new XsltView());
		MvcFacesTestUtils.doWithMockMvcFacesRequestContext(new MockMvcFacesRequestContextCallback() {
			public void prepare(MvcFacesRequestContext mvcFacesRequestContext) throws Exception {
				EasyMock.expect(mvcFacesRequestContext.getFacesHandler().createView(facesContext)).andReturn(
						modelAndView);
			}

			public void execute(MvcFacesRequestContext mvcFacesRequestContext) throws Exception {
				try {
					handler.createView(facesContext, VIEW_ID);
					fail();
				} catch (IllegalArgumentException e) {
					assertTrue(e.getMessage().equals("MVC Faces can only support viewName references"));
				}
			}
		});
	}

	public void testRenderedByHandler() throws Exception {
		final ViewHandler delegate = (ViewHandler) MvcFacesTestUtils.methodTrackingObject(ViewHandler.class);
		final MvcViewHandler handler = new MvcViewHandler(delegate);
		final ModelAndView modelAndView = new ModelAndView();
		MvcFacesTestUtils.doWithMockMvcFacesRequestContext(new MockMvcFacesRequestContextCallback() {
			public void prepare(MvcFacesRequestContext mvcFacesRequestContext) throws Exception {
				EasyMock.expect(mvcFacesRequestContext.getFacesHandler().createView(facesContext)).andReturn(
						modelAndView);
			}

			public void execute(MvcFacesRequestContext mvcFacesRequestContext) throws Exception {
				UIViewRoot viewRoot = handler.createView(facesContext, VIEW_ID);
				assertTrue(facesContext.getResponseComplete());
				assertTrue(viewRoot.getClass().getName().endsWith("EmptyUIViewRoot"));
				handler.renderView(facesContext, viewRoot);
				((MethodCallAssertor) delegate).assertNotCalled("renderView");
			}
		});
	}

	public void testRestoreView() throws Exception {
		final ViewHandler delegate = (ViewHandler) MvcFacesTestUtils.nullImplementation(ViewHandler.class,
				new MethodInterceptor() {
					public Object invoke(MethodInvocation invocation) throws Throwable {
						if ("restoreView".equals(invocation.getMethod().getName())) {
							Assert.assertEquals(XHTML_VIEW_NAME, invocation.getArguments()[1]);
						}
						return null;
					}
				});
		final MvcViewHandler handler = new MvcViewHandler(delegate);
		MvcFacesTestUtils.doWithMockMvcFacesRequestContext(new MockMvcFacesRequestContextCallback() {
			public void prepare(MvcFacesRequestContext mvcFacesRequestContext) throws Exception {
				EasyMock.expect(
						((MvcFacesRequestControlContext) mvcFacesRequestContext).getExecution().getViewIdForRestore(
								facesContext, VIEW_ID)).andReturn(XHTML_VIEW_NAME);
			}

			public void execute(MvcFacesRequestContext mvcFacesRequestContext) throws Exception {
				handler.restoreView(facesContext, VIEW_ID);
			}
		});
	}

	public void testRestoreViewWhenNotMapped() throws Exception {
		final ViewHandler delegate = (ViewHandler) MvcFacesTestUtils.nullImplementation(ViewHandler.class);
		final MvcViewHandler handler = new MvcViewHandler(delegate);
		MvcFacesTestUtils.doWithMockMvcFacesRequestContext(new MockMvcFacesRequestContextCallback() {
			public void prepare(MvcFacesRequestContext mvcFacesRequestContext) throws Exception {
				EasyMock.expect(
						((MvcFacesRequestControlContext) mvcFacesRequestContext).getExecution().getViewIdForRestore(
								facesContext, VIEW_ID)).andReturn(null);
			}

			public void execute(MvcFacesRequestContext mvcFacesRequestContext) throws Exception {
				try {
					handler.restoreView(facesContext, VIEW_ID);
					fail();
				} catch (IllegalArgumentException e) {
					assertEquals("The MVC Faces Context could not map the view \"someview\" to a valid viewId", e
							.getMessage());
				}
			}
		});
	}

	public void testGetActionUrl() throws Exception {
		final ViewHandler delegate = (ViewHandler) MvcFacesTestUtils.nullImplementation(ViewHandler.class);
		final MvcViewHandler handler = new MvcViewHandler(delegate);
		MvcFacesTestUtils.doWithMockMvcFacesRequestContext(new MockMvcFacesRequestContextCallback() {
			public void prepare(MvcFacesRequestContext mvcFacesRequestContext) throws Exception {
				EasyMock.expect(
						((MvcFacesRequestControlContext) mvcFacesRequestContext).getExecution().getActionUlr(
								facesContext, VIEW_ID)).andReturn(ACTION_URL);
			}

			public void execute(MvcFacesRequestContext mvcFacesRequestContext) throws Exception {
				handler.getActionURL(facesContext, VIEW_ID);
			}
		});
	}

	public void testGetActionUrlWhenNotMapped() throws Exception {
		final ViewHandler delegate = (ViewHandler) MvcFacesTestUtils.nullImplementation(ViewHandler.class);
		final MvcViewHandler handler = new MvcViewHandler(delegate);
		MvcFacesTestUtils.doWithMockMvcFacesRequestContext(new MockMvcFacesRequestContextCallback() {
			public void prepare(MvcFacesRequestContext mvcFacesRequestContext) throws Exception {
				EasyMock.expect(
						((MvcFacesRequestControlContext) mvcFacesRequestContext).getExecution().getActionUlr(
								facesContext, VIEW_ID)).andReturn(null);
			}

			public void execute(MvcFacesRequestContext mvcFacesRequestContext) throws Exception {
				try {
					handler.getActionURL(facesContext, VIEW_ID);
					fail();
				} catch (IllegalArgumentException e) {
					assertEquals("The action URL for the view \"someview\" is not mapped", e.getMessage());
				}
			}
		});
	}
}
