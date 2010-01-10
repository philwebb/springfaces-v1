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
package org.springframework.faces.mvc.annotation;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.faces.context.FacesContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.faces.mvc.stereotype.FacesController;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.Errors;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.support.HandlerMethodInvocationException;
import org.springframework.web.bind.annotation.support.HandlerMethodInvoker;
import org.springframework.web.bind.support.WebArgumentResolver;
import org.springframework.web.bind.support.WebBindingInitializer;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartRequest;
import org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * Helper class used to invoke annotated methods on {@link FacesController} annotated classes. The class is based
 * heavily on the <tt>ServletHandlerMethodInvoker</tt> internal class used by {@link AnnotationMethodHandlerAdapter}s.
 * 
 * @author Juergen Hoeller
 * @author Phillip Webb
 */
abstract class FacesControllerAnnotatedMethodInvoker {

	// FIXME tidy up, dc, tests

	private static final Log logger = LogFactory.getLog(HandlerMethodInvoker.class);

	private static final ModelArgumentResolver INIT_BINDER_NO_MODEL_ARGUMENT_RESOLVER = new ModelArgumentResolver() {
		public ResolvedModelArgument resolve(String modelAttributeName, MethodParameter methodParameter,
				WebRequest webRequest, boolean failOnErrors) {
			if (modelAttributeName == null) {
				throw new IllegalStateException("Unsupported argument [" + methodParameter.getParameterType().getName()
						+ "] for @InitBinder method: " + methodParameter.getMethod());
			}
			throw new IllegalStateException("@ModelAttribute is not supported on @InitBinder methods: "
					+ methodParameter.getMethod());
		}
	};

	private WebBindingInitializer bindingInitializer;

	private RequestMappingMethodResolver methodResolver;

	private ParameterNameDiscoverer parameterNameDiscoverer;

	private WebArgumentResolver[] customArgumentResolvers;

	public FacesControllerAnnotatedMethodInvoker(RequestMappingMethodResolver resolver,
			WebBindingInitializer bindingInitializer, ParameterNameDiscoverer parameterNameDiscoverer,
			WebArgumentResolver... customArgumentResolvers) {

		this.methodResolver = resolver;
		this.bindingInitializer = bindingInitializer;
		this.parameterNameDiscoverer = parameterNameDiscoverer;
		this.customArgumentResolvers = customArgumentResolvers;
	}

	protected abstract WebDataBinder createBinder(NativeWebRequest webRequest, Object target, String objectName)
			throws Exception;

	public final Object invokeOnActiveHandler(Method handlerMethod, Object handler, NativeWebRequest webRequest)
			throws Exception {

		Method handlerMethodToInvoke = BridgeMethodResolver.findBridgedMethod(handlerMethod);
		try {
			ModelArgumentResolver modelResolver = new FacesModelArgumentResolver(FacesContext.getCurrentInstance());
			Object[] args = resolveArguments(handler, handlerMethod, webRequest, null, modelResolver);
			if (logger.isDebugEnabled()) {
				logger.debug("Invoking method and active handler: " + handlerMethodToInvoke);
			}
			return doInvokeMethod(handlerMethodToInvoke, handler, args);
		} catch (IllegalStateException ex) {
			// Throw exception with full handler method context...
			throw new HandlerMethodInvocationException(handlerMethodToInvoke, ex);
		}
	}

	protected final void initBinder(Object handler, String attrName, WebDataBinder binder, NativeWebRequest webRequest)
			throws Exception {
		if (this.bindingInitializer != null) {
			this.bindingInitializer.initBinder(binder, webRequest);
		}
		if (handler != null) {
			Set<Method> initBinderMethods = this.methodResolver.getInitBinderMethods();
			if (!initBinderMethods.isEmpty()) {
				boolean debug = logger.isDebugEnabled();
				for (Method initBinderMethod : initBinderMethods) {
					Method methodToInvoke = BridgeMethodResolver.findBridgedMethod(initBinderMethod);
					String[] targetNames = AnnotationUtils.findAnnotation(methodToInvoke, InitBinder.class).value();
					if (targetNames.length == 0 || Arrays.asList(targetNames).contains(attrName)) {
						Object[] initBinderArgs = resolveInitBinderArguments(handler, methodToInvoke, binder,
								webRequest);
						if (debug) {
							logger.debug("Invoking init-binder method: " + methodToInvoke);
						}
						Object returnValue = doInvokeMethod(methodToInvoke, handler, initBinderArgs);
						if (returnValue != null) {
							throw new IllegalStateException("InitBinder methods must not have a return value: "
									+ methodToInvoke);
						}
					}
				}
			}
		}
	}

	private Object[] resolveInitBinderArguments(Object handler, Method initBinderMethod, final WebDataBinder binder,
			NativeWebRequest webRequest) throws Exception {
		WebArgumentResolver initBinderArgumentResolver = new WebArgumentResolver() {
			public Object resolveArgument(MethodParameter methodParameter, NativeWebRequest webRequest)
					throws Exception {
				if (methodParameter.getParameterType().isInstance(binder)) {
					return binder;
				}
				return WebArgumentResolver.UNRESOLVED;
			}
		};
		WebArgumentResolver[] argumentResolvers = { initBinderArgumentResolver };
		return resolveArguments(handler, initBinderMethod, webRequest, argumentResolvers,
				INIT_BINDER_NO_MODEL_ARGUMENT_RESOLVER);
	}

	private Object[] resolveArguments(Object handler, Method handlerMethod, NativeWebRequest webRequest,
			WebArgumentResolver[] argumentResolvers, ModelArgumentResolver modelArgumentResolver) throws Exception {

		Class[] paramTypes = handlerMethod.getParameterTypes();
		Object[] args = new Object[paramTypes.length];

		for (int i = 0; i < args.length; i++) {
			MethodParameter methodParameter = new MethodParameter(handlerMethod, i);
			methodParameter.initParameterNameDiscovery(this.parameterNameDiscoverer);
			GenericTypeResolver.resolveParameterType(methodParameter, handler.getClass());
			String requestParamName = null;
			boolean requestParamRequired = false;
			String modelAttributeName = null;
			Object[] methodParamAnnotations = methodParameter.getParameterAnnotations();

			for (int j = 0; j < methodParamAnnotations.length; j++) {
				Object methodParamAnnotation = methodParamAnnotations[j];
				if (RequestParam.class.isInstance(methodParamAnnotation)) {
					RequestParam requestParam = (RequestParam) methodParamAnnotation;
					requestParamName = requestParam.value();
					requestParamRequired = requestParam.required();
					break;
				} else if (ModelAttribute.class.isInstance(methodParamAnnotation)) {
					ModelAttribute modelAttribute = (ModelAttribute) methodParamAnnotation;
					modelAttributeName = modelAttribute.value();
				}
			}
			if (requestParamName != null && modelAttributeName != null) {
				throw new IllegalStateException("@RequestParam and @ModelAttribute are an exclusive choice -"
						+ "do not specify both on the same parameter: " + handlerMethod);
			}

			if (requestParamName == null && modelAttributeName == null) {
				Object argValue = resolveCommonArgument(methodParameter, webRequest);
				if (argValue != WebArgumentResolver.UNRESOLVED) {
					args[i] = argValue;
				} else {
					Class paramType = paramTypes[i];
					if (Errors.class.isAssignableFrom(paramType)) {
						throw new IllegalStateException("Errors/BindingResult argument declared "
								+ "without preceding model attribute. Check your handler method signature!");
					}
					args[i] = invokeArgumentResolvers(methodParameter, webRequest, argumentResolvers);
					if (args[i] == WebArgumentResolver.UNRESOLVED) {
						if (BeanUtils.isSimpleProperty(paramType)) {
							// Set the request param to a non null value to trigger a resolve
							requestParamName = "";
						} else {
							// Set the model attribute name to a non null value to trigger a model resolve
							modelAttributeName = "";
						}
					}
				}
			}

			if (requestParamName != null) {
				args[i] = resolveRequestParam(requestParamName, requestParamRequired, methodParameter, webRequest, null);
			} else if (modelAttributeName != null) {
				boolean assignBindingResult = (args.length > i + 1 && Errors.class.isAssignableFrom(paramTypes[i + 1]));
				ResolvedModelArgument resolved = null;
				if (modelArgumentResolver != null) {
					resolved = modelArgumentResolver.resolve((modelAttributeName.length() == 0 ? null
							: modelAttributeName), methodParameter, webRequest, !assignBindingResult);
				}
				if (resolved != null) {
					args[i] = resolved.getResult();
					if (assignBindingResult) {
						args[i + 1] = resolved.getErrors();
						i++;
					}
				}
			}
		}

		return args;
	}

	private Object resolveRequestParam(String paramName, boolean paramRequired, MethodParameter methodParam,
			NativeWebRequest webRequest, Object handlerForInitBinderCall) throws Exception {
		Class paramType = methodParam.getParameterType();
		if ("".equals(paramName)) {
			paramName = methodParam.getParameterName();
			if (paramName == null) {
				throw new IllegalStateException("No parameter specified for @RequestParam argument of type ["
						+ paramType.getName() + "], and no parameter name information found in class file either.");
			}
		}
		Object paramValue = null;
		if (webRequest.getNativeRequest() instanceof MultipartRequest) {
			paramValue = ((MultipartRequest) webRequest.getNativeRequest()).getFile(paramName);
		}
		if (paramValue == null) {
			String[] paramValues = webRequest.getParameterValues(paramName);
			if (paramValues != null) {
				paramValue = (paramValues.length == 1 ? paramValues[0] : paramValues);
			}
		}
		if (paramValue == null) {
			if (paramRequired) {
				raiseMissingParameterException(paramName, paramType);
			}
			if (paramType.isPrimitive()) {
				throw new IllegalStateException(
						"Optional "
								+ paramType
								+ " parameter '"
								+ paramName
								+ "' is not present but cannot be translated into a null value due to being declared as a "
								+ "primitive type. Consider declaring it as object wrapper for the corresponding primitive type.");
			}
		}
		WebDataBinder binder = createBinder(webRequest, null, paramName);
		initBinder(handlerForInitBinderCall, paramName, binder, webRequest);
		return binder.convertIfNecessary(paramValue, paramType, methodParam);
	}

	private Object doInvokeMethod(Method method, Object target, Object[] args) throws Exception {
		ReflectionUtils.makeAccessible(method);
		try {
			return method.invoke(target, args);
		} catch (InvocationTargetException ex) {
			ReflectionUtils.rethrowException(ex.getTargetException());
		}
		throw new IllegalStateException("Should never get here");
	}

	protected void raiseMissingParameterException(String paramName, Class paramType) throws Exception {
		throw new MissingServletRequestParameterException(paramName, paramType.getName());
	}

	protected Object resolveCommonArgument(MethodParameter methodParameter, NativeWebRequest webRequest)
			throws Exception {
		// Invoke custom argument resolvers if present...
		Object custom = invokeArgumentResolvers(methodParameter, webRequest, customArgumentResolvers);
		if (custom != WebArgumentResolver.UNRESOLVED) {
			return custom;
		}

		// Resolution of standard parameter types...
		Class paramType = methodParameter.getParameterType();
		Object value = resolveStandardArgument(paramType, webRequest);
		if (value != WebArgumentResolver.UNRESOLVED && !ClassUtils.isAssignableValue(paramType, value)) {
			throw new IllegalStateException("Standard argument type [" + paramType.getName()
					+ "] resolved to incompatible value of type [" + (value != null ? value.getClass() : null)
					+ "]. Consider declaring the argument type in a less specific fashion.");
		}
		return value;
	}

	private Object invokeArgumentResolvers(MethodParameter methodParameter, NativeWebRequest webRequest,
			WebArgumentResolver[] resolvers) throws Exception {
		if (resolvers != null) {
			for (WebArgumentResolver resolver : resolvers) {
				Object value = resolver.resolveArgument(methodParameter, webRequest);
				if (value != WebArgumentResolver.UNRESOLVED) {
					return value;
				}
			}
		}
		return WebArgumentResolver.UNRESOLVED;
	}

	protected Object resolveStandardArgument(Class parameterType, NativeWebRequest webRequest) throws Exception {
		if ((webRequest.getNativeRequest() instanceof HttpServletRequest)
				&& (webRequest.getNativeResponse() instanceof HttpServletResponse)) {

			HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
			HttpServletResponse response = (HttpServletResponse) webRequest.getNativeResponse();

			if (ServletRequest.class.isAssignableFrom(parameterType)) {
				return request;
			} else if (ServletResponse.class.isAssignableFrom(parameterType)) {
				return response;
			} else if (HttpSession.class.isAssignableFrom(parameterType)) {
				return request.getSession();
			} else if (Principal.class.isAssignableFrom(parameterType)) {
				return request.getUserPrincipal();
			} else if (Locale.class.equals(parameterType)) {
				return RequestContextUtils.getLocale(request);
			} else if (InputStream.class.isAssignableFrom(parameterType)) {
				return request.getInputStream();
			} else if (Reader.class.isAssignableFrom(parameterType)) {
				return request.getReader();
			} else if (OutputStream.class.isAssignableFrom(parameterType)) {
				return response.getOutputStream();
			} else if (Writer.class.isAssignableFrom(parameterType)) {
				return response.getWriter();
			} else if (WebRequest.class.isAssignableFrom(parameterType)) {
				return webRequest;
			}
		}
		return WebArgumentResolver.UNRESOLVED;
	}

	protected static final class ResolvedModelArgument {

		private Object result;
		private Errors errors;

		public ResolvedModelArgument(Object result) {
			this(result, null);
		}

		public ResolvedModelArgument(Object result, Errors errors) {
			this.result = result;
			this.errors = errors;
		}

		public Object getResult() {
			return result;
		}

		/**
		 * @return Errors or <tt>null</tt>
		 */
		public Errors getErrors() {
			return errors;
		}
	}

	/**
	 * Internal resolver called when a method argument contains a {@link ModelAttribute} annotation or when all other
	 * attribute resolution has failed.
	 */
	protected static interface ModelArgumentResolver {
		/**
		 * @param modelAttributeName The model attribute name or <tt>null</tt> if the model attribute is being deduced
		 * by type alone.
		 * @param methodParameter
		 * @param webRequest
		 * @param failOnErrors
		 * @return A resolved model argument or <tt>null</tt>
		 */
		public ResolvedModelArgument resolve(String modelAttributeName, MethodParameter methodParameter,
				WebRequest webRequest, boolean failOnErrors);
	}

	protected static class FacesModelArgumentResolver implements ModelArgumentResolver {

		private FacesContext facesContext;

		public FacesModelArgumentResolver(FacesContext facesContext) {
			this.facesContext = facesContext;
		}

		public ResolvedModelArgument resolve(String modelAttributeName, MethodParameter methodParameter,
				WebRequest webRequest, boolean failOnErrors) {

			ELContext elContext = facesContext.getELContext();
			ELResolver resolver = elContext.getELResolver();
			Object resolved = resolver.getValue(elContext, null, modelAttributeName);
			if (!elContext.isPropertyResolved()) {
				return null;
			}
			return new ResolvedModelArgument(resolved);
		}

	}
}
