package org.springframework.faces.mvc;

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
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.faces.mvc.support.MvcFacesContext;
import org.springframework.faces.mvc.support.MvcFacesRequestContext;
import org.springframework.faces.mvc.support.PageScopeHolderComponent;
import org.springframework.web.context.support.StaticWebApplicationContext;

public class AbstractFacesHandlerAdapterTests extends AbstractJsfTestCase {

	private class MockFacesHandlerAdapter extends AbstractFacesHandlerAdapter {

		protected void doHandle(MvcFacesRequestContext mvcFacesRequestContext, HttpServletRequest request,
				HttpServletResponse response) throws Exception {
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
		this.redirectHandler = (RedirectHandler) EasyMock.createMock(RedirectHandler.class);
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
			protected void doHandle(MvcFacesRequestContext mvcFacesRequestContext, HttpServletRequest request,
					HttpServletResponse response) throws Exception {
				MvcFacesRequestContext.getCurrentInstance().getMvcFacesContext().resolveViewId("viewname");
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
			protected void doHandle(MvcFacesRequestContext mvcFacesRequestContext, HttpServletRequest request,
					HttpServletResponse response) throws Exception {
				MvcFacesRequestContext.getCurrentInstance().getMvcFacesContext().getActionUlr(facesContext, "viewid");
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
			protected void doHandle(MvcFacesRequestContext mvcFacesRequestContext, HttpServletRequest request,
					HttpServletResponse response) throws Exception {
				MvcFacesRequestContext.getCurrentInstance().getMvcFacesContext().getViewIdForRestore(facesContext,
						"viewid");
			}
		};
		facesHandlerAdapter.handle(request, response, facesHandler);
		EasyMock.verify(new Object[] { facesViewIdResolver, actionUrlMapper });
	}

	private void doTestViewCreated(final boolean pageScopeAttached) throws Exception {
		final UIViewRoot viewRoot = new UIViewRoot();
		facesContext.setViewRoot(viewRoot);
		final Map model = new HashMap();
		modelBindingExecutor.storeModelToBind(facesContext, model);
		EasyMock.expectLastCall();
		EasyMock.replay(new Object[] { modelBindingExecutor });
		facesHandlerAdapter = new MockFacesHandlerAdapter() {
			protected void doHandle(MvcFacesRequestContext mvcFacesRequestContext, HttpServletRequest request,
					HttpServletResponse response) throws Exception {
				mvcFacesRequestContext.getMvcFacesContext().viewCreated(facesContext, mvcFacesRequestContext, viewRoot,
						model);
			}

			protected boolean isPageScopeSupported() {
				return pageScopeAttached;
			}
		};
		facesHandlerAdapter.handle(request, response, facesHandler);
		EasyMock.verify(new Object[] { modelBindingExecutor });
	}

	public void testPageScopeGetsRegistered() throws Exception {
		facesHandlerAdapter = new MockFacesHandlerAdapter();
		ConfigurableListableBeanFactory beanFactory = (ConfigurableListableBeanFactory) EasyMock
				.createMock(ConfigurableListableBeanFactory.class);
		beanFactory.registerScope((String) EasyMock.eq("page"), (PageScope) EasyMock.isA(PageScope.class));
		EasyMock.expectLastCall();
		EasyMock.replay(new Object[] { beanFactory });
		facesHandlerAdapter.postProcessBeanFactory(beanFactory);
		EasyMock.verify(new Object[] { beanFactory });
	}

	public void testViewCreatedWithPageScope() throws Exception {
		doTestViewCreated(true);
		assertNotNull(PageScopeHolderComponent.locate(facesContext, false));
	}

	public void testViewCreatedWithoutPageScope() throws Exception {
		doTestViewCreated(false);
		try {
			PageScopeHolderComponent.locate(facesContext, true);
			fail();
		} catch (IllegalArgumentException e) {
		}
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
			protected void doHandle(MvcFacesRequestContext mvcFacesRequestContext, HttpServletRequest request,
					HttpServletResponse response) throws Exception {
				MvcFacesRequestContext.getCurrentInstance().getMvcFacesContext().writeState(facesContext);
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
			protected void doHandle(MvcFacesRequestContext mvcFacesRequestContext, HttpServletRequest request,
					HttpServletResponse response) throws Exception {
				PhaseEvent event = new PhaseEvent(facesContext, phaseId, lifecycle);
				mvcFacesRequestContext.getMvcFacesContext().beforePhase(mvcFacesRequestContext, event);
			}
		};
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
			protected void doHandle(MvcFacesRequestContext mvcFacesRequestContext, HttpServletRequest request,
					HttpServletResponse response) throws Exception {
				mvcFacesRequestContext.setException(exception);
				PhaseEvent event = new PhaseEvent(facesContext, phaseId, lifecycle);
				mvcFacesRequestContext.getMvcFacesContext().beforePhase(mvcFacesRequestContext, event);
			}
		};
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
			protected MvcFacesContext newFacesHandlerAdapterContext() {
				return new AbstractFacesHandlerAdapter.FacesHandlerAdapterContext() {
					public String toString() {
						return "customhandler";
					}
				};
			}

			protected void doHandle(MvcFacesRequestContext mvcFacesRequestContext, HttpServletRequest request,
					HttpServletResponse response) throws Exception {
				assertEquals("customhandler", MvcFacesRequestContext.getCurrentInstance().getFacesHandler().toString());
			}
		};
	}

	public void testRedirectHandler() throws Exception {
		HttpServletRequest frequest = (HttpServletRequest) facesContext.getExternalContext().getRequest();
		HttpServletResponse fresponse = (HttpServletResponse) facesContext.getExternalContext().getResponse();
		redirectHandler.handleRedirect(frequest, fresponse, "location");
		EasyMock.expectLastCall();
		EasyMock.replay(new Object[] { redirectHandler });
		facesHandlerAdapter = new MockFacesHandlerAdapter() {
			protected void doHandle(MvcFacesRequestContext mvcFacesRequestContext, HttpServletRequest request,
					HttpServletResponse response) throws Exception {
				MvcFacesRequestContext.getCurrentInstance().getMvcFacesContext().redirect(facesContext, "location");
			}
		};
		facesHandlerAdapter.handle(request, response, facesHandler);
		EasyMock.verify(new Object[] { redirectHandler });
	}

	private void newExceptionThrowingFacesHandlerAdapter() {
		facesHandlerAdapter = new MockFacesHandlerAdapter() {
			protected void doHandle(MvcFacesRequestContext mvcFacesRequestContext, HttpServletRequest request,
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
			protected void doHandle(MvcFacesRequestContext mvcFacesRequestContext, HttpServletRequest request,
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
		redirectHandler.handleRedirect(request, response, "location");
		EasyMock.expectLastCall();
		EasyMock.replay(new Object[] { redirectHandler });
		facesHandlerAdapter.setExceptionHandlers(Collections.singletonList(exceptionHandler));
		facesHandlerAdapter.onRefresh(new StaticWebApplicationContext());
		facesHandlerAdapter.handle(request, response, facesHandler);
		exceptionHandler.assertCalled();
		EasyMock.verify(new Object[] { redirectHandler });
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

		public boolean handleException(Exception exception, MvcFacesRequestContext requestContext,
				HttpServletRequest request, HttpServletResponse response, MvcFacesExceptionOutcome outcome)
				throws Exception {
			Assert.assertEquals(IllegalStateException.class, exception.getClass());
			Assert.assertEquals("errortest", exception.getMessage());
			Assert.assertSame(exception, requestContext.getException());
			this.called = new Long(System.currentTimeMillis());
			Thread.sleep(2);
			if (redisplay) {
				outcome.redisplay();
			}
			if (redirect != null) {
				outcome.redirect(redirect);
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
