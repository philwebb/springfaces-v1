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

import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.faces.webapp.FacesServlet;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.AopProxy;
import org.springframework.aop.framework.DefaultAopProxyFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.faces.mvc.bind.DefaultModelBinder;
import org.springframework.faces.mvc.bind.ModelBinder;
import org.springframework.faces.mvc.bind.ModelBindingExecutor;
import org.springframework.faces.mvc.bind.RequestMappedModelBindingExecutor;
import org.springframework.faces.mvc.execution.ActionUrlMapper;
import org.springframework.faces.mvc.execution.RequestContext;
import org.springframework.faces.mvc.execution.PageEncodedActionUrlMapper;
import org.springframework.faces.mvc.view.FacesViewIdResolver;
import org.springframework.faces.mvc.view.SimpleFacesViewIdResolver;
import org.springframework.util.Assert;
import org.springframework.web.servlet.HandlerAdapter;

/**
 * MVC {@link HandlerAdapter} that can be used to process {@link FacesHandler}s. This class handles most of the low
 * level integration between JSF and Spring MVC. Also consider using the <tt>FacesAnnotationMethodHandlerAdapter</tt>
 * class in order to support annotated controllers.
 * 
 * @author Phillip Webb
 */
public class FacesHandlerAdapter extends AbstractFacesHandlerAdapter implements InitializingBean, BeanNameAware {

	private RequestMappedModelBindingExecutor modelBindingExecutor = new RequestMappedModelBindingExecutor();
	private Servlet facesServlet;
	private String beanName;
	private Class facesServletClass = FacesServlet.class;
	private Properties initParameters = new Properties();
	private FacesViewIdResolver facesViewIdResolver;
	private ActionUrlMapper actionUrlMapper;
	private RedirectHandler redirectHandler;
	private boolean overrideInitParameters = true;
	private ServletContext facesServletContext;

	public boolean supports(Object handler) {
		return handler instanceof FacesHandler;
	}

	protected void doHandle(RequestContext requestContext, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		facesServlet.service(request, response);
	}

	public void setBeanName(String name) {
		this.beanName = name;
	}

	/**
	 * Trigger all post-processors and spring call-backs for internally managed beans.
	 * @param bean The internal bean
	 * @throws Exception
	 */
	private void initializeInternalBean(Object bean) throws Exception {
		getApplicationContext().getAutowireCapableBeanFactory().initializeBean(bean,
				"_" + beanName + "_" + bean.getClass().getSimpleName());
	}

	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
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
			DefaultModelBinder modelBinder = new DefaultModelBinder();
			initializeInternalBean(modelBinder);
			modelBindingExecutor.setModelBinder(modelBinder);
		}
		initializeInternalBean(modelBindingExecutor);

		facesServlet = newFacesServlet();
		facesServlet.init(new DelegatingServletConfig());
	}

	protected FacesViewIdResolver getFacesViewIdResolver() {
		return facesViewIdResolver;
	}

	protected ModelBindingExecutor getModelBindingExecutor() {
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
	 * @return The faces servlet instance
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
	 * Obtain a {@link ServletContext} instance that will be used to configure the faces servlet. By default the
	 * {@link ServletContext#getInitParameter(String)} method will be intercepted in order to provide a config that will
	 * work with all {@link FacesServlet}s, regardless of whether the user has set the required parameters in their
	 * web.xml. Note: This behaviour can be disabled using {@link #setOverrideInitParameters(boolean)}.
	 * @return {@link ServletConfig} instance
	 * @see #setOverrideInitParameters(boolean)
	 */
	protected ServletContext getFacesServletContext() {
		if (!overrideInitParameters) {
			return getServletContext();
		}
		if (facesServletContext == null) {
			AdvisedSupport aopConfig = new AdvisedSupport();
			aopConfig.setInterfaces(new Class[] { ServletContext.class });
			aopConfig.setTarget(getServletContext());
			aopConfig.addAdvice(new OverrideInitParameterInterceptor());
			DefaultAopProxyFactory aopProxyFactory = new DefaultAopProxyFactory();
			AopProxy proxy = aopProxyFactory.createAopProxy(aopConfig);
			facesServletContext = (ServletContext) proxy.getProxy();
		}
		return facesServletContext;
	}

	/**
	 * Determine if override <tt>init</tt> parameters should be used with the FacesServlet. In order for the
	 * FacesServlet instance to process MVC requests correctly exceptions must not be handled internally by the servlet.
	 * Some JSF implementations (MyFaces for example) require additional configuration in order to propagate exceptions
	 * correctly and allow MVC to handle them. By default this class will use AOP to ensure that this configuration
	 * occurs automatically. If this behaviour is not desired then this set <tt>overrideInitParameters</tt> to
	 * <tt>false</tt>
	 * @param overrideInitParameters <tt>true</tt> if init parameters are automatically set for correct MVC operation or
	 * <tt>false</tt> if parameters should be set manually in web.xml. Defaults to <tt>true</tt> when not explicitly set
	 */
	public void setOverrideInitParameters(boolean overrideInitParameters) {
		this.overrideInitParameters = overrideInitParameters;
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
	 * Set the init parameters that will be used to initialise the FacesServlet.
	 * @param initParameters init parameters to use with the FacesServlet
	 */
	public void setInitParameters(Properties initParameters) {
		Assert.notNull(initParameters, "The initParameters are required");
		this.initParameters = initParameters;
	}

	/**
	 * Set the {@link FacesViewIdResolver} that will be used to resolve view IDs. If the resolver is not specified a
	 * {@link SimpleFacesViewIdResolver} will be used.
	 * @param facesViewIdResolver
	 * @see SimpleFacesViewIdResolver
	 */
	public void setFacesViewIdResolver(FacesViewIdResolver facesViewIdResolver) {
		Assert.notNull(facesViewIdResolver, "The facesViewIdResolver is required");
		this.facesViewIdResolver = facesViewIdResolver;
	}

	/**
	 * Set the {@link ActionUrlMapper} that will be used to map action URLS. If the mapper is not specified a
	 * {@link PageEncodedActionUrlMapper} will be used.
	 * @param actionUrlMapper
	 * @see PageEncodedActionUrlMapper
	 */
	public void setActionUrlMapper(ActionUrlMapper actionUrlMapper) {
		Assert.notNull(actionUrlMapper, "The actionUrlMapper is required");
		this.actionUrlMapper = actionUrlMapper;
	}

	/**
	 * Set the model binder that will be used to expose model elements to JSF. If the binder is not specified the
	 * {@link DefaultModelBinder} will be used.
	 * @param modelBinder
	 * @see DefaultModelBinder
	 */
	public void setModelBinder(ModelBinder modelBinder) {
		Assert.notNull(modelBinder, "The modelBinder is required");
		modelBindingExecutor.setModelBinder(modelBinder);
	}

	/**
	 * Set the redirect handler that will be used to handle navigation outcome. If the handler is not specified the
	 * {@link DefaultRedirectHandler} will be used.
	 * @param redirectHandler
	 */
	public void setRedirectHandler(RedirectHandler redirectHandler) {
		Assert.notNull(redirectHandler, "The redirectHandler is required");
		this.redirectHandler = redirectHandler;
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
			return FacesHandlerAdapter.this.getFacesServletContext();
		}

		public String getInitParameter(String paramName) {
			return FacesHandlerAdapter.this.initParameters.getProperty(paramName);
		}

		public Enumeration getInitParameterNames() {
			return FacesHandlerAdapter.this.initParameters.keys();
		}
	}

	/**
	 * Internal {@link MethodInterceptor} used to ensure that the {@link ServletContext} has sensible default init
	 * parameters for MVC operation.
	 */
	private static class OverrideInitParameterInterceptor implements MethodInterceptor {

		private static final Method SERVLETCONTEXT_INITPARAMETER_METHOD;
		private static final Map INIT_PARAMETER_OVERRIDES;
		static {
			try {
				SERVLETCONTEXT_INITPARAMETER_METHOD = ServletContext.class.getMethod("getInitParameter",
						new Class[] { String.class });
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
			INIT_PARAMETER_OVERRIDES = new HashMap();
			INIT_PARAMETER_OVERRIDES.put("org.apache.myfaces.ERROR_HANDLING", "false");
		}

		public Object invoke(MethodInvocation invocation) throws Throwable {
			if (SERVLETCONTEXT_INITPARAMETER_METHOD.equals(invocation.getMethod())) {
				String name = (String) invocation.getArguments()[0];
				if (INIT_PARAMETER_OVERRIDES.containsKey(name)) {
					return INIT_PARAMETER_OVERRIDES.get(name);
				}
			}
			return invocation.proceed();
		}
	}
}
