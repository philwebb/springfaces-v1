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
package org.springframework.faces.mvc.servlet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIViewRoot;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.Assert;

import org.apache.shale.test.base.AbstractJsfTestCase;
import org.apache.shale.test.mock.MockFacesContext;
import org.easymock.EasyMock;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.faces.mvc.bind.ModelBindingExecutor;
import org.springframework.faces.mvc.context.ExternalContext;
import org.springframework.faces.mvc.context.MvcFacesExecution;
import org.springframework.faces.mvc.context.WebFlowExternalContextAdapter;
import org.springframework.faces.mvc.execution.ActionUrlMapper;
import org.springframework.faces.mvc.execution.MvcFacesExceptionHandler;
import org.springframework.faces.mvc.execution.MvcFacesExceptionOutcome;
import org.springframework.faces.mvc.execution.RequestContext;
import org.springframework.faces.mvc.execution.RequestContextHolder;
import org.springframework.faces.mvc.execution.RequestControlContext;
import org.springframework.faces.mvc.navigation.NavigationLocation;
import org.springframework.faces.mvc.navigation.RedirectHandler;
import org.springframework.faces.mvc.support.MvcFacesStateHolderComponent;
import org.springframework.faces.mvc.view.FacesViewIdResolver;
import org.springframework.js.ajax.AjaxHandler;
import org.springframework.js.ajax.SpringJavascriptAjaxHandler;
import org.springframework.web.context.support.StaticWebApplicationContext;
import org.springframework.webflow.test.MockExternalContext;

public class AbstractFacesHandlerAdapterTests extends AbstractJsfTestCase {

	private class MockFacesHandlerAdapter extends AbstractFacesHandlerAdapter {

		protected void doHandle(RequestContext requestContext, HttpServletRequest request,
				HttpServletResponse response) throws Exception {
		}

		protected ExternalContext createExternalContext(HttpServletRequest request, HttpServletResponse response) {
			return new WebFlowExternalContextAdapter(new MockExternalContext());
		}

		protected ActionUrlMapper getActionUrlMapper() {
			return actionUrlMapper;
		}

		protected FacesViewIdResolver getFacesViewIdResolver() {
			return facesViewIdResolver;
		}

		protected ModelBindingExecutor getModelBindingExecutor() {
			return modelBindingExecutor;
		}

		protected RedirectHandler getRedirectHandler() {
			return redirectHandler;
		}
	}

	private ActionUrlMapper actionUrlMapper;
	private FacesViewIdResolver facesViewIdResolver;
	private ModelBindingExecutor modelBindingExecutor;
	private RedirectHandler redirectHandler;
	private MockFacesHandlerAdapter facesHandlerAdapter;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private FacesHandler facesHandler;

	public AbstractFacesHandlerAdapterTests(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		this.actionUrlMapper = (ActionUrlMapper) EasyMock.createMock(ActionUrlMapper.class);
		this.facesViewIdResolver = (FacesViewIdResolver) EasyMock.createMock(FacesViewIdResolver.class);
		this.modelBindingExecutor = (ModelBindingExecutor) EasyMock.createMock(ModelBindingExecutor.class);
		this.redirectHandler = (RedirectHandler) EasyMock.createNiceMock(RedirectHandler.class);
		this.request = (HttpServletRequest) EasyMock.createMock(HttpServletRequest.class);
		this.response = (HttpServletResponse) EasyMock.createMock(HttpServletResponse.class);
		this.facesHandler = (FacesHandler) EasyMock.createMock(FacesHandler.class);
	}

	public void testOnlyForFacesHandler() throws Exception {
		facesHandlerAdapter = new MockFacesHandlerAdapter();
		assertTrue(facesHandlerAdapter.supports(facesHandler));
		assertFalse(facesHandlerAdapter.supports("Test"));
	}

	public void testResolveViewId() throws Exception {
		EasyMock.expect(facesViewIdResolver.resolveViewId("viewname")).andReturn("viewid");
		EasyMock.replay(new Object[] { facesViewIdResolver });
		facesHandlerAdapter = new MockFacesHandlerAdapter() {
			protected void doHandle(RequestContext requestContext, HttpServletRequest request,
					HttpServletResponse response) throws Exception {
				((RequestControlContext) RequestContextHolder.getRequestContext()).getExecution()
						.resolveViewId("viewname");
			}
		};
		facesHandlerAdapter.handle(request, response, facesHandler);
		EasyMock.verify(new Object[] { facesViewIdResolver });
	}

	public void testGetActionUlr() throws Exception {
		EasyMock.expect(facesViewIdResolver.resolveViewName("viewid")).andReturn("viewname");
		EasyMock.expect(actionUrlMapper.getActionUlr(facesContext, "viewname")).andReturn("action");
		EasyMock.replay(new Object[] { facesViewIdResolver, actionUrlMapper });
		facesHandlerAdapter = new MockFacesHandlerAdapter() {
			protected void doHandle(RequestContext requestContext, HttpServletRequest request,
					HttpServletResponse response) throws Exception {
				((RequestControlContext) RequestContextHolder.getRequestContext()).getExecution()
						.getActionUlr(facesContext, "viewid");
			}
		};
		facesHandlerAdapter.handle(request, response, facesHandler);
		EasyMock.verify(new Object[] { facesViewIdResolver, actionUrlMapper });
	}

	public void testGetViewIdForRestore() throws Exception {
		EasyMock.expect(actionUrlMapper.getViewNameForRestore(facesContext)).andReturn("viewname");
		EasyMock.expect(facesViewIdResolver.resolveViewId("viewname")).andReturn("viewid");
		EasyMock.replay(new Object[] { facesViewIdResolver, actionUrlMapper });
		facesHandlerAdapter = new MockFacesHandlerAdapter() {
			protected void doHandle(RequestContext requestContext, HttpServletRequest request,
					HttpServletResponse response) throws Exception {
				((RequestControlContext) RequestContextHolder.getRequestContext()).getExecution()
						.getViewIdForRestore(facesContext, "viewid");
			}
		};
		facesHandlerAdapter.handle(request, response, facesHandler);
		EasyMock.verify(new Object[] { facesViewIdResolver, actionUrlMapper });
	}

	public void testViewCreatedWithPageScope() throws Exception {
		final UIViewRoot viewRoot = new UIViewRoot();
		facesContext.setViewRoot(viewRoot);
		final Map model = new HashMap();
		modelBindingExecutor.storeModelToBind(facesContext, model);
		EasyMock.expectLastCall();
		EasyMock.replay(new Object[] { modelBindingExecutor });
		facesHandlerAdapter = new MockFacesHandlerAdapter() {
			protected void doHandle(RequestContext requestContext, HttpServletRequest request,
					HttpServletResponse response) throws Exception {
				((RequestControlContext) requestContext).getExecution().viewCreated(facesContext,
						requestContext, viewRoot, model);
			}
		};
		facesHandlerAdapter.handle(request, response, facesHandler);
		EasyMock.verify(new Object[] { modelBindingExecutor });
		assertNotNull(MvcFacesStateHolderComponent.locate(facesContext, false));
	}

	public void testWriteState() throws Exception {
		UIViewRoot viewRoot = new UIViewRoot();
		viewRoot.setViewId("viewid");
		facesContext.setViewRoot(viewRoot);
		EasyMock.expect(facesViewIdResolver.resolveViewName("viewid")).andReturn("viewname");
		actionUrlMapper.writeState(facesContext, "viewname");
		EasyMock.expectLastCall();
		EasyMock.replay(new Object[] { facesViewIdResolver, actionUrlMapper });
		facesHandlerAdapter = new MockFacesHandlerAdapter() {
			protected void doHandle(RequestContext requestContext, HttpServletRequest request,
					HttpServletResponse response) throws Exception {
				((RequestControlContext) RequestContextHolder.getRequestContext()).getExecution()
						.writeState(facesContext);
			}
		};
		facesHandlerAdapter.handle(request, response, facesHandler);
		EasyMock.verify(new Object[] { facesViewIdResolver, actionUrlMapper });
	}

	private void doTestBeforePhaseBindsModel(final PhaseId phaseId, boolean binds) throws Exception {
		EasyMock.reset(new Object[] { modelBindingExecutor });
		modelBindingExecutor.bindStoredModel(facesContext);
		EasyMock.expectLastCall();
		EasyMock.replay(new Object[] { modelBindingExecutor });
		facesHandlerAdapter = new MockFacesHandlerAdapter() {
			protected void doHandle(RequestContext requestContext, HttpServletRequest request,
					HttpServletResponse response) throws Exception {
				PhaseEvent event = new PhaseEvent(facesContext, phaseId, lifecycle);
				((RequestControlContext) requestContext).getExecution().beforePhase(
						requestContext, event);
			}
		};
		this.redirectHandler = (RedirectHandler) EasyMock.createNiceMock(RedirectHandler.class);
		facesHandlerAdapter.handle(request, response, facesHandler);
		try {
			EasyMock.verify(new Object[] { modelBindingExecutor });
			if (!binds) {
				fail();
			}
		} catch (AssertionError e) {
			if (binds) {
				fail();
			}
		}
	}

	public void testAfterPhaseBindsModel() throws Exception {
		doTestBeforePhaseBindsModel(PhaseId.APPLY_REQUEST_VALUES, false);
		doTestBeforePhaseBindsModel(PhaseId.INVOKE_APPLICATION, false);
		doTestBeforePhaseBindsModel(PhaseId.PROCESS_VALIDATIONS, false);
		doTestBeforePhaseBindsModel(PhaseId.RENDER_RESPONSE, true);
		doTestBeforePhaseBindsModel(PhaseId.RESTORE_VIEW, false);
		doTestBeforePhaseBindsModel(PhaseId.UPDATE_MODEL_VALUES, false);
	}

	private void doTestStopAtProcessValidationsWhenHasCurrentException(final PhaseId phaseId,
			final Exception exception, boolean expectedResponseComplete) throws Exception {
		facesContext = new MockFacesContext();
		facesHandlerAdapter = new MockFacesHandlerAdapter() {
			protected void doHandle(RequestContext requestContext, HttpServletRequest request,
					HttpServletResponse response) throws Exception {
				((RequestControlContext) requestContext).setException(exception);
				PhaseEvent event = new PhaseEvent(facesContext, phaseId, lifecycle);
				((RequestControlContext) requestContext).getExecution().beforePhase(
						requestContext, event);
			}
		};
		this.redirectHandler = (RedirectHandler) EasyMock.createNiceMock(RedirectHandler.class);
		facesHandlerAdapter.handle(request, response, facesHandler);
		assertEquals(expectedResponseComplete, facesContext.getRenderResponse());

	}

	public void testStopAtProcessValidationsWhenHasCurrentException() throws Exception {
		doTestStopAtProcessValidationsWhenHasCurrentException(PhaseId.APPLY_REQUEST_VALUES, new Exception(), false);
		doTestStopAtProcessValidationsWhenHasCurrentException(PhaseId.INVOKE_APPLICATION, new Exception(), false);
		doTestStopAtProcessValidationsWhenHasCurrentException(PhaseId.PROCESS_VALIDATIONS, new Exception(), true);
		doTestStopAtProcessValidationsWhenHasCurrentException(PhaseId.PROCESS_VALIDATIONS, null, false);
	}

	public void testNewFacesHandlerAdapterContext() throws Exception {
		facesHandlerAdapter = new MockFacesHandlerAdapter() {
			protected MvcFacesExecution newExecution() {
				return new AbstractFacesHandlerAdapter.FacesHandlerAdapterExecution() {
					public String toString() {
						return "customhandler";
					}
				};
			}

			protected void doHandle(RequestContext requestContext, HttpServletRequest request,
					HttpServletResponse response) throws Exception {
				assertEquals("customhandler", RequestContextHolder.getRequestContext().getFacesHandler()
						.toString());
			}
		};
	}

	public void testRedirectHandler() throws Exception {
		AjaxHandler ajaxHandler = new SpringJavascriptAjaxHandler();
		HttpServletRequest frequest = (HttpServletRequest) facesContext.getExternalContext().getRequest();
		HttpServletResponse fresponse = (HttpServletResponse) facesContext.getExternalContext().getResponse();
		redirectHandler.handleRedirect(ajaxHandler, frequest, fresponse, new NavigationLocation("location"), null);
		EasyMock.expectLastCall();
		EasyMock.replay(new Object[] { redirectHandler });
		facesHandlerAdapter = new MockFacesHandlerAdapter() {
			protected void doHandle(RequestContext requestContext, HttpServletRequest request,
					HttpServletResponse response) throws Exception {
				((RequestControlContext) RequestContextHolder.getRequestContext()).getExecution()
						.redirect(facesContext, requestContext, new NavigationLocation("location"));
			}
		};
		facesHandlerAdapter.setAjaxHandler(ajaxHandler);
		facesHandlerAdapter.handle(request, response, facesHandler);
		EasyMock.verify(new Object[] { redirectHandler });
	}

	private void newExceptionThrowingFacesHandlerAdapter() {
		facesHandlerAdapter = new MockFacesHandlerAdapter() {
			protected void doHandle(RequestContext requestContext, HttpServletRequest request,
					HttpServletResponse response) throws Exception {
				throw new IllegalStateException("errortest");
			}
		};
		facesHandlerAdapter.setDetectAllHandlerExceptionHandlers(false);
	}

	public void testNoExceptionHandlersHandleButAllAreCalledInTheCorrectOrder() throws Exception {
		newExceptionThrowingFacesHandlerAdapter();
		facesHandlerAdapter.setDetectAllHandlerExceptionHandlers(false);
		MockMvcFacesExceptionHandler direct = new MockMvcFacesExceptionHandler();
		MockMvcFacesExceptionHandler viaFacesHandler = new MockMvcFacesExceptionHandler();
		EasyMock.expect(facesHandler.getExceptionHandlers()).andReturn(
				new MvcFacesExceptionHandler[] { viaFacesHandler });
		facesHandlerAdapter.setExceptionHandlers(Collections.singletonList(direct));
		EasyMock.replay(new Object[] { facesHandler });
		facesHandlerAdapter.onRefresh(new StaticWebApplicationContext());
		try {
			facesHandlerAdapter.handle(request, response, facesHandler);
			fail("did not throw");
		} catch (IllegalStateException e) {
			assertEquals("errortest", e.getMessage());
		}
		direct.assertCalled();
		viaFacesHandler.assertCalled();
		viaFacesHandler.assertCalledBefore(direct);
		EasyMock.verify(new Object[] { facesHandler });
	}

	public void testExceptionHandlerDirectResponse() throws Exception {
		newExceptionThrowingFacesHandlerAdapter();
		List handlers = new ArrayList();
		handlers.add(new MockMvcFacesExceptionHandler(true));
		handlers.add(new MockMvcFacesExceptionHandler());
		facesHandlerAdapter.setExceptionHandlers(handlers);
		facesHandlerAdapter.onRefresh(new StaticWebApplicationContext());
		facesHandlerAdapter.handle(request, response, facesHandler);
		((MockMvcFacesExceptionHandler) handlers.get(0)).assertCalled();
		((MockMvcFacesExceptionHandler) handlers.get(1)).assertNotCalled();
	}

	public void testExceptionHandlerRedisplay() throws Exception {
		final boolean[] flag = new boolean[] { false };
		facesHandlerAdapter = new MockFacesHandlerAdapter() {
			protected void doHandle(RequestContext requestContext, HttpServletRequest request,
					HttpServletResponse response) throws Exception {
				flag[0] = !flag[0];
				if (flag[0]) {
					throw new IllegalStateException("errortest");
				}
			}
		};
		facesHandlerAdapter.setDetectAllHandlerExceptionHandlers(false);
		MockMvcFacesExceptionHandler exceptionHandler = new MockMvcFacesExceptionHandler(true);
		exceptionHandler.setRedisplay(true);
		facesHandlerAdapter.setExceptionHandlers(Collections.singletonList(exceptionHandler));
		facesHandlerAdapter.onRefresh(new StaticWebApplicationContext());
		facesHandlerAdapter.handle(request, response, facesHandler);
		exceptionHandler.assertCalled();
		assertFalse("Do handle not called twice", flag[0]);
	}

	public void testExceptionHandlerRedirect() throws Exception {
		newExceptionThrowingFacesHandlerAdapter();
		MockMvcFacesExceptionHandler exceptionHandler = new MockMvcFacesExceptionHandler(true);
		exceptionHandler.setRedirect("location");
		AjaxHandler ajaxHandler = new SpringJavascriptAjaxHandler();
		redirectHandler.handleRedirect(ajaxHandler, request, response, new NavigationLocation("location"), null);
		EasyMock.expectLastCall();
		EasyMock.replay(new Object[] { redirectHandler });
		facesHandlerAdapter.setExceptionHandlers(Collections.singletonList(exceptionHandler));
		facesHandlerAdapter.onRefresh(new StaticWebApplicationContext());
		facesHandlerAdapter.setAjaxHandler(ajaxHandler);
		facesHandlerAdapter.handle(request, response, facesHandler);
		exceptionHandler.assertCalled();
		EasyMock.verify(new Object[] { redirectHandler });
	}

	public void testExceptionHandlerRedirectAndRedisplayThrows() throws Exception {
		newExceptionThrowingFacesHandlerAdapter();
		MockMvcFacesExceptionHandler exceptionHandler = new MockMvcFacesExceptionHandler(true);
		exceptionHandler.setRedirect("location");
		exceptionHandler.setRedisplay(true);
		AjaxHandler ajaxHandler = new SpringJavascriptAjaxHandler();
		facesHandlerAdapter.setExceptionHandlers(Collections.singletonList(exceptionHandler));
		facesHandlerAdapter.onRefresh(new StaticWebApplicationContext());
		facesHandlerAdapter.setAjaxHandler(ajaxHandler);
		try {
			facesHandlerAdapter.handle(request, response, facesHandler);
			fail();
		} catch (IllegalStateException e) {
			assertEquals("Illegal outcome specified, redirect or redisplay are mutually exclusive", e.getMessage());
		}
	}

	public void testUserDefinedHandlersIgnoredAndCanFindInContext() throws Exception {
		newExceptionThrowingFacesHandlerAdapter();
		facesHandlerAdapter.setDetectAllHandlerExceptionHandlers(true);
		MockMvcFacesExceptionHandler direct = new MockMvcFacesExceptionHandler(true);
		MockMvcFacesExceptionHandler bean = new MockMvcFacesExceptionHandler(true);
		facesHandlerAdapter.setExceptionHandlers(Collections.singletonList(direct));
		ApplicationContext applicationContext = (ApplicationContext) EasyMock.createMock(ApplicationContext.class);
		Map beans = new HashMap();
		beans.put("bean", bean);
		EasyMock.expect(applicationContext.getBeansOfType(MvcFacesExceptionHandler.class, true, false))
				.andReturn(beans);
		EasyMock.expect(applicationContext.getParentBeanFactory()).andReturn(null);
		EasyMock.replay(new Object[] { applicationContext });
		ContextRefreshedEvent event = new ContextRefreshedEvent(applicationContext);
		facesHandlerAdapter.onApplicationEvent(event);
		facesHandlerAdapter.handle(request, response, facesHandler);
		EasyMock.verify(new Object[] { applicationContext });
		direct.assertNotCalled();
		bean.assertCalled();
	}

	public void testAjaxHandlerSetWhenNull() throws Exception {
		facesHandlerAdapter = new MockFacesHandlerAdapter();
		assertNull(facesHandlerAdapter.getAjaxHandler());
		facesHandlerAdapter.afterPropertiesSet();
		assertNotNull(facesHandlerAdapter.getAjaxHandler());
		assertTrue(facesHandlerAdapter.getAjaxHandler() instanceof SpringJavascriptAjaxHandler);
	}

	public void testAjaxHandlerNotReplacedWhenSet() throws Exception {
		facesHandlerAdapter = new MockFacesHandlerAdapter();
		AjaxHandler ajaxHandler = (AjaxHandler) EasyMock.createMock(AjaxHandler.class);
		facesHandlerAdapter.setAjaxHandler(ajaxHandler);
		facesHandlerAdapter.afterPropertiesSet();
		assertSame(ajaxHandler, facesHandlerAdapter.getAjaxHandler());
	}

	public void testGetLastModified() throws Exception {
		facesHandlerAdapter = new MockFacesHandlerAdapter();
		Object handler = new Object();
		assertEquals(-1, facesHandlerAdapter.getLastModified(request, handler));
	}

	private static class MockMvcFacesExceptionHandler implements MvcFacesExceptionHandler {

		private Long called;

		private boolean handle;

		private boolean redisplay;

		private String redirect;

		public MockMvcFacesExceptionHandler() {
			this(false);
		}

		public MockMvcFacesExceptionHandler(boolean handle) {
			this.handle = handle;
		}

		public boolean handleException(Exception exception, RequestContext requestContext,
				MvcFacesExceptionOutcome outcome) throws Exception {
			Assert.assertEquals(IllegalStateException.class, exception.getClass());
			Assert.assertEquals("errortest", exception.getMessage());
			Assert.assertSame(exception, requestContext.getException());
			this.called = new Long(System.currentTimeMillis());
			Thread.sleep(2);
			if (redisplay) {
				outcome.redisplay();
			}
			if (redirect != null) {
				outcome.redirect(new NavigationLocation(redirect));
			}
			return handle;
		}

		public void setRedisplay(boolean redisplay) {
			this.redisplay = redisplay;
		}

		public void setRedirect(String redirect) {
			this.redirect = redirect;
		}

		public void assertCalledBefore(MockMvcFacesExceptionHandler other) {
			assertTrue(called.longValue() < other.called.longValue());
		}

		public void assertCalled() {
			Assert.assertTrue(called != null);
		}

		public void assertNotCalled() {
			Assert.assertTrue(called == null);
		}
	}
}
