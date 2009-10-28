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
package org.springframework.faces.mvc;

import java.util.Enumeration;
import java.util.Properties;

import javax.faces.webapp.FacesServlet;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.faces.mvc.annotation.FacesAnnotationMethodHandlerAdapter;
import org.springframework.util.Assert;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;

/**
 * MVC {@link HandlerAdapter} that can be used to process {@link FacesHandler}s. This class handles most of the low
 * level integration between JSF and Spring MVC. Also consider using the {@link FacesAnnotationMethodHandlerAdapter}
 * class in order to support annotated controllers.
 * 
 * @see FacesAnnotationMethodHandlerAdapter
 * 
 * @author Phillip Webb
 */
public class FacesHandlerAdapter extends AbstractFacesHandlerAdapter implements InitializingBean, BeanNameAware {

	private RequestMappedModelBindingExecutor modelBindingExecutor = new RequestMappedModelBindingExecutor();
	private Servlet facesServlet;
	private String beanName;
	private Class facesServletClass = FacesServlet.class;
	private Properties initParameters = new Properties();
	private boolean pageScopeSupported = true;
	private FacesViewIdResolver facesViewIdResolver;
	private ActionUrlMapper actionUrlMapper;
	private RedirectHandler redirectHandler;

	public boolean supports(Object handler) {
		return handler instanceof FacesHandler;
	}

	protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response, FacesHandler handler)
			throws Exception {
		try {
			facesServlet.service(request, response);
			return null;
		} catch (Exception e) {
			// FIXME support exception handling
			throw e;
		}
	}

	public void setBeanName(String name) {
		this.beanName = name;
	}

	/**
	 * Trigger all post-processors and spring callbacks for internally managed beans.
	 * @param bean The internal bean
	 * @throws Exception
	 */
	private void initializeInternalBean(Object bean) throws Exception {
		getApplicationContext().getAutowireCapableBeanFactory().initializeBean(bean,
				"_" + beanName + "_" + bean.getClass().getSimpleName());
	}

	public void afterPropertiesSet() throws Exception {
		if (facesViewIdResolver == null) {
			facesViewIdResolver = new SimpleFacesViewIdResolver();
			initializeInternalBean(facesViewIdResolver);
		}
		if (actionUrlMapper == null) {
			actionUrlMapper = new PageEncodedActionUrlMapper();
			initializeInternalBean(actionUrlMapper);
		}
		if (redirectHandler == null) {
			redirectHandler = new DefaultRedirectHandler();
			initializeInternalBean(redirectHandler);
		}
		if (modelBindingExecutor.getModelBinder() == null) {
			BeanScopeModelBinder modelBinder = new BeanScopeModelBinder();
			initializeInternalBean(modelBinder);
			modelBindingExecutor.setModelBinder(modelBinder);
		}
		initializeInternalBean(modelBindingExecutor);

		facesServlet = newFacesServlet();
		facesServlet.init(new DelegatingServletConfig());
	}

	protected boolean isPageScopeSupported() {
		return pageScopeSupported;
	}

	protected FacesViewIdResolver getFacesViewIdResolver() {
		return facesViewIdResolver;
	}

	protected RequestMappedModelBindingExecutor getModelBindingExecutor() {
		return modelBindingExecutor;
	}

	protected ActionUrlMapper getActionUrlMapper() {
		return actionUrlMapper;
	}

	protected RedirectHandler getRedirectHandler() {
		return redirectHandler;
	}

	protected Servlet getFacesServlet() {
		return facesServlet;
	}

	/**
	 * Factory method used to construct the servlet class.
	 * 
	 * @return The faces servlet instance.
	 */
	protected Servlet newFacesServlet() {
		try {
			return (Servlet) facesServletClass.newInstance();
		} catch (Exception e) {
			throw new IllegalArgumentException("Unable to instanciate face servlet from the specified class "
					+ facesServletClass.getName(), e);
		}
	}

	/**
	 * Sets the class that will be used to construct the {@link FacesServlet} that will be used to handle requests. If
	 * not specified the default {@link FacesServlet} class will be used.
	 * @param facesServletClass
	 */
	public void setFacesServletClass(Class facesServletClass) {
		Assert.notNull(facesServletClass, "The facesServletClass is required");
		this.facesServletClass = facesServletClass;
	}

	/**
	 * Set the init parameters that will be used to initialise the faces servlet.
	 */
	public void setInitParameters(Properties initParameters) {
		Assert.notNull(initParameters, "The initParameters are required");
		this.initParameters = initParameters;
	}

	/**
	 * Set if the adapter should support "pageScope".
	 * @param pageScopeSupported
	 * @see PageScope
	 */
	public void setPageScopeSupported(boolean pageScopeSupported) {
		this.pageScopeSupported = pageScopeSupported;
	}

	/**
	 * Set the {@link FacesViewIdResolver} that will be used to resolve view IDs. If the resolver is not injected a
	 * {@link SimpleFacesViewIdResolver} will be used.
	 * @param facesViewIdResolver
	 * @see SimpleFacesViewIdResolver
	 */
	public void setFacesViewIdResolver(FacesViewIdResolver facesViewIdResolver) {
		Assert.notNull(facesViewIdResolver, "The facesViewIdResolver is required");
		this.facesViewIdResolver = facesViewIdResolver;
	}

	/**
	 * Set the {@link ActionUrlMapper} that will be used to map action URLS. If the mapper is not injected a
	 * {@link PageEncodedActionUrlMapper} will be used.
	 * @param actionUrlMapper
	 * @see PageEncodedActionUrlMapper
	 */
	public void setActionUrlMapper(ActionUrlMapper actionUrlMapper) {
		Assert.notNull(actionUrlMapper, "The actionUrlMapper is required");
		this.actionUrlMapper = actionUrlMapper;
	}

	/**
	 * Set the model binder that will be used to expose model elements to JSF. If the binder is not injected the
	 * {@link BeanScopeModelBinder} will be used.
	 * @param modelBinder
	 * @see BeanScopeModelBinder
	 */
	public void setModelBinder(ModelBinder modelBinder) {
		Assert.notNull(modelBinder, "The modelBinder is required");
		modelBindingExecutor.setModelBinder(modelBinder);
	}

	/**
	 * Internal implementation of the ServletConfig interface, to be passed to the wrapped servlet. Delegates to
	 * ServletWrappingController fields and methods to provide init parameters and other environment info.
	 */
	private class DelegatingServletConfig implements ServletConfig {

		public String getServletName() {
			return FacesHandlerAdapter.this.beanName;
		}

		public ServletContext getServletContext() {
			return FacesHandlerAdapter.this.getServletContext();
		}

		public String getInitParameter(String paramName) {
			return FacesHandlerAdapter.this.initParameters.getProperty(paramName);
		}

		public Enumeration getInitParameterNames() {
			return FacesHandlerAdapter.this.initParameters.keys();
		}
	}
}
