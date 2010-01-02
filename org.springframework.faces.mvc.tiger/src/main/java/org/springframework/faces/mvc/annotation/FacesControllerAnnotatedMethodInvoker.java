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
import org.springframework.web.HttpSessionRequiredException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
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
public abstract class FacesControllerAnnotatedMethodInvoker {
	private static final Log logger = LogFactory.getLog(HandlerMethodInvoker.class);

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

	private Object[] resolveInitBinderArguments(Object handler, Method initBinderMethod, WebDataBinder binder,
			NativeWebRequest webRequest) throws Exception {

		Class[] initBinderParams = initBinderMethod.getParameterTypes();
		Object[] initBinderArgs = new Object[initBinderParams.length];

		for (int i = 0; i < initBinderArgs.length; i++) {
			MethodParameter methodParam = new MethodParameter(initBinderMethod, i);
			methodParam.initParameterNameDiscovery(this.parameterNameDiscoverer);
			GenericTypeResolver.resolveParameterType(methodParam, handler.getClass());
			String paramName = null;
			boolean paramRequired = false;
			Object[] paramAnns = methodParam.getParameterAnnotations();

			for (int j = 0; j < paramAnns.length; j++) {
				Object paramAnn = paramAnns[j];
				if (RequestParam.class.isInstance(paramAnn)) {
					RequestParam requestParam = (RequestParam) paramAnn;
					paramName = requestParam.value();
					paramRequired = requestParam.required();
					break;
				} else if (ModelAttribute.class.isInstance(paramAnn)) {
					throw new IllegalStateException("@ModelAttribute is not supported on @InitBinder methods: "
							+ initBinderMethod);
				}
			}

			if (paramName == null) {
				Object argValue = resolveCommonArgument(methodParam, webRequest);
				if (argValue != WebArgumentResolver.UNRESOLVED) {
					initBinderArgs[i] = argValue;
				} else {
					Class paramType = initBinderParams[i];
					if (paramType.isInstance(binder)) {
						initBinderArgs[i] = binder;
					} else if (BeanUtils.isSimpleProperty(paramType)) {
						paramName = "";
					} else {
						throw new IllegalStateException("Unsupported argument [" + paramType.getName()
								+ "] for @InitBinder method: " + initBinderMethod);
					}
				}
			}

			if (paramName != null) {
				initBinderArgs[i] = resolveRequestParam(paramName, paramRequired, methodParam, webRequest, null);
			}
		}

		return initBinderArgs;
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

	protected void raiseSessionRequiredException(String message) throws Exception {
		throw new HttpSessionRequiredException(message);
	}

	protected Object resolveCommonArgument(MethodParameter methodParameter, NativeWebRequest webRequest)
			throws Exception {

		// Invoke custom argument resolvers if present...
		if (this.customArgumentResolvers != null) {
			for (WebArgumentResolver argumentResolver : this.customArgumentResolvers) {
				Object value = argumentResolver.resolveArgument(methodParameter, webRequest);
				if (value != WebArgumentResolver.UNRESOLVED) {
					return value;
				}
			}
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

	protected Object resolveStandardArgument(Class parameterType, NativeWebRequest webRequest) throws Exception {
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
		return WebArgumentResolver.UNRESOLVED;
	}

}
