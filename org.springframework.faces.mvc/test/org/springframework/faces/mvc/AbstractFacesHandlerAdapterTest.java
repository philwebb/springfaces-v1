package org.springframework.faces.mvc;

import java.util.HashMap;
import java.util.Map;

import javax.faces.component.UIViewRoot;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shale.test.base.AbstractJsfTestCase;
import org.easymock.EasyMock;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.faces.mvc.support.MvcFacesContext;
import org.springframework.faces.mvc.support.MvcFacesRequestContext;
import org.springframework.faces.mvc.support.PageScopeHolderComponent;
import org.springframework.web.servlet.ModelAndView;

public class AbstractFacesHandlerAdapterTest extends AbstractJsfTestCase {

	private class MockFacesHandlerAdapter extends AbstractFacesHandlerAdapter {

		protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response, FacesHandler handler)
				throws Exception {
			return null;
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

	public AbstractFacesHandlerAdapterTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		this.actionUrlMapper = EasyMock.createMock(ActionUrlMapper.class);
		this.facesViewIdResolver = EasyMock.createMock(FacesViewIdResolver.class);
		this.modelBindingExecutor = EasyMock.createMock(ModelBindingExecutor.class);
		this.redirectHandler = EasyMock.createMock(RedirectHandler.class);
		this.request = EasyMock.createMock(HttpServletRequest.class);
		this.response = EasyMock.createMock(HttpServletResponse.class);
		this.facesHandler = EasyMock.createMock(FacesHandler.class);
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
			protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
					FacesHandler handler) throws Exception {
				MvcFacesRequestContext.getCurrentInstance().getMvcFacesContext().resolveViewId("viewname");
				return null;
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
			protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
					FacesHandler handler) throws Exception {
				MvcFacesRequestContext.getCurrentInstance().getMvcFacesContext().getActionUlr(facesContext, "viewid");
				return null;
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
			protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
					FacesHandler handler) throws Exception {
				MvcFacesRequestContext.getCurrentInstance().getMvcFacesContext().getViewIdForRestore(facesContext,
						"viewid");
				return null;
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
			protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
					FacesHandler handler) throws Exception {
				MvcFacesRequestContext mvcFacesRequestContext = MvcFacesRequestContext.getCurrentInstance();
				mvcFacesRequestContext.getMvcFacesContext().viewCreated(facesContext, mvcFacesRequestContext, viewRoot,
						model);
				return null;
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
		ConfigurableListableBeanFactory beanFactory = EasyMock.createMock(ConfigurableListableBeanFactory.class);
		beanFactory.registerScope(EasyMock.eq("page"), EasyMock.isA(PageScope.class));
		EasyMock.expectLastCall();
		EasyMock.replay(new Object[] { beanFactory });
		facesHandlerAdapter.postProcessBeanFactory(beanFactory);
		EasyMock.verify(new Object[] { beanFactory });
	}

	public void testViewCreatedWithPageScope() throws Exception {
		doTestViewCreated(true);
		assertNotNull(PageScopeHolderComponent.locate(facesContext));
	}

	public void testViewCreatedWithoutPageScope() throws Exception {
		doTestViewCreated(false);
		try {
			PageScopeHolderComponent.locate(facesContext);
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
			protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
					FacesHandler handler) throws Exception {
				MvcFacesRequestContext.getCurrentInstance().getMvcFacesContext().writeState(facesContext);
				return null;
			}
		};
		facesHandlerAdapter.handle(request, response, facesHandler);
		EasyMock.verify(new Object[] { facesViewIdResolver, actionUrlMapper });
	}

	private void doTestAfterPhaseBindsModel(final PhaseId phaseId, boolean binds) throws Exception {
		EasyMock.reset(new Object[] { modelBindingExecutor });
		modelBindingExecutor.bindStoredModel(facesContext);
		EasyMock.expectLastCall();
		EasyMock.replay(new Object[] { modelBindingExecutor });
		facesHandlerAdapter = new MockFacesHandlerAdapter() {
			protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
					FacesHandler handler) throws Exception {
				MvcFacesRequestContext mvcFacesRequestContext = MvcFacesRequestContext.getCurrentInstance();
				PhaseEvent event = new PhaseEvent(facesContext, phaseId, lifecycle);
				mvcFacesRequestContext.getMvcFacesContext().afterPhase(mvcFacesRequestContext, event);
				return null;
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
		doTestAfterPhaseBindsModel(PhaseId.APPLY_REQUEST_VALUES, false);
		doTestAfterPhaseBindsModel(PhaseId.INVOKE_APPLICATION, false);
		doTestAfterPhaseBindsModel(PhaseId.PROCESS_VALIDATIONS, false);
		doTestAfterPhaseBindsModel(PhaseId.RENDER_RESPONSE, true);
		doTestAfterPhaseBindsModel(PhaseId.RESTORE_VIEW, false);
		doTestAfterPhaseBindsModel(PhaseId.UPDATE_MODEL_VALUES, false);
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

			protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
					FacesHandler handler) throws Exception {
				assertEquals("customhandler", MvcFacesRequestContext.getCurrentInstance().getFacesHandler().toString());
				return null;
			}
		};
	}

	public void testRedirectHandler() throws Exception {
		redirectHandler.handleRedirect(facesContext, "location");
		EasyMock.expectLastCall();
		EasyMock.replay(new Object[] { redirectHandler });
		facesHandlerAdapter = new MockFacesHandlerAdapter() {
			protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
					FacesHandler handler) throws Exception {
				MvcFacesRequestContext.getCurrentInstance().getMvcFacesContext().redirect(facesContext, "location");
				return null;
			}
		};
		facesHandlerAdapter.handle(request, response, facesHandler);
		EasyMock.verify(new Object[] { redirectHandler });

	}
}
