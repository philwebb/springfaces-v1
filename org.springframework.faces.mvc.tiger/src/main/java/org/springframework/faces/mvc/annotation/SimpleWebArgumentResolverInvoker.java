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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.springframework.core.GenericTypeResolver;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.support.HandlerMethodInvoker;
import org.springframework.web.bind.support.WebArgumentResolver;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * Support class that can be used to resolve parameters using an array of {@link WebArgumentResolver}s when invoking a
 * Method. This class is based heavily on {@link HandlerMethodInvoker}.
 * 
 * @author Juergen Hoeller
 * @author Arjen Poutsma
 * @author Phillip Webb
 */
public class SimpleWebArgumentResolverInvoker {

	// FIXME Delete this it is a duplicate of FacesContollerAnnotatedMethodInvoker

	private ParameterNameDiscoverer parameterNameDiscoverer;
	private WebArgumentResolver[] webArgumentResolvers;

	/**
	 * Constructor.
	 * 
	 * @param parameterNameDiscoverer The {@link ParameterNameDiscoverer} that should be used to discover parameter
	 * names or <tt>null</tt> to use the default {@link LocalVariableTableParameterNameDiscoverer}.
	 * @param webArgumentResolvers An array of resolvers that should be applied in order to obtain paramter values.
	 */
	public SimpleWebArgumentResolverInvoker(ParameterNameDiscoverer parameterNameDiscoverer,
			WebArgumentResolver[] webArgumentResolvers) {
		Assert.notNull(webArgumentResolvers, "webArgumentResolvers are required");
		Assert.noNullElements(webArgumentResolvers, "webArgumentResolvers cannot contain null elements");
		this.parameterNameDiscoverer = (parameterNameDiscoverer == null ? new LocalVariableTableParameterNameDiscoverer()
				: parameterNameDiscoverer);
		this.webArgumentResolvers = webArgumentResolvers;
	}

	/**
	 * Invoke the specified method on the given target using {@link WebArgumentResolver}s to obtain parameter values.
	 * 
	 * @param method The method to execute.
	 * @param target The target object.
	 * @param webRequest The webRequest being executed.
	 * @return The result of the invocation.
	 * 
	 * @throws Exception
	 */
	public Object invoke(Method method, Object target, NativeWebRequest webRequest) throws Exception {
		Object[] args = resolveArguments(method, target, webRequest);
		return doInvokeMethod(method, target, args);
	}

	private Object[] resolveArguments(Method method, Object target, NativeWebRequest webRequest) throws Exception {
		Class<?>[] paramTypes = method.getParameterTypes();
		Object[] args = new Object[paramTypes.length];
		for (int i = 0; i < args.length; i++) {
			MethodParameter methodParameter = new MethodParameter(method, i);
			methodParameter.initParameterNameDiscovery(this.parameterNameDiscoverer);
			GenericTypeResolver.resolveParameterType(methodParameter, target.getClass());
			Object resolved = resolveArgument(methodParameter, webRequest);
			if (resolved != WebArgumentResolver.UNRESOLVED) {
				args[i] = resolved;
			}
		}
		return args;
	}

	private Object resolveArgument(MethodParameter methodParameter, NativeWebRequest webRequest) throws Exception {
		for (WebArgumentResolver argumentResolver : this.webArgumentResolvers) {
			Object value = argumentResolver.resolveArgument(methodParameter, webRequest);
			if (value != WebArgumentResolver.UNRESOLVED) {
				return value;
			}
		}
		return WebArgumentResolver.UNRESOLVED;
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

}
