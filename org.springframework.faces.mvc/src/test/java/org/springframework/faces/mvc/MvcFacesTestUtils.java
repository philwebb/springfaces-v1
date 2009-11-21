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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.easymock.EasyMock;
import org.springframework.aop.ProxyMethodInvocation;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.AopProxy;
import org.springframework.aop.framework.DefaultAopProxyFactory;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;
import org.springframework.faces.mvc.support.MvcFacesContext;
import org.springframework.faces.mvc.support.MvcFacesRequestContext;

public class MvcFacesTestUtils {

	public static interface MethodCallAssertor {

		public void recordMethodCall(Method method);

		public void assertCalled(String methodName);

		public void assertCalled(String[] methodNames);

		public void assertNotCalled(String methodName);
	}

	public static class MethodTrackerInterceptor extends DelegatingIntroductionInterceptor implements
			MethodCallAssertor {

		private Set called = new HashSet();

		public void recordMethodCall(Method method) {
			called.add(method.getName());
		}

		public void assertCalled(String methodName) {
			assertCalled(new String[] { methodName });
		}

		public void assertCalled(String[] methods) {
			Assert.assertEquals(new HashSet(Arrays.asList(methods)), called);
		}

		public void assertNotCalled(String methodName) {
			Assert.assertFalse(called.contains(methodName));
		}

	}

	public static abstract class MockMvcFacesRequestContextCallback {
		public void prepare(MvcFacesRequestContext mvcFacesRequestContext) throws Exception {
		};

		public abstract void execute(MvcFacesRequestContext mvcFacesRequestContext) throws Exception;
	}

	public static void doWithMockMvcFacesRequestContext(MockMvcFacesRequestContextCallback callback) throws Exception {
		MvcFacesContext mvcFacesContext = EasyMock.createMock(MvcFacesContext.class);
		FacesHandler facesHandler = EasyMock.createMock(FacesHandler.class);
		MvcFacesRequestContext mvcFacesRequestContext = new MvcFacesRequestContext(mvcFacesContext, facesHandler);
		try {
			callback.prepare(mvcFacesRequestContext);
			EasyMock.replay(new Object[] { mvcFacesContext, facesHandler });
			callback.execute(mvcFacesRequestContext);
			EasyMock.verify(new Object[] { mvcFacesContext, facesHandler });
		} finally {
			mvcFacesRequestContext.release();
		}
	}

	public static Object nullImplementation(Class targetClass) {
		return nullImplementation(targetClass, null);
	}

	public static Object nullImplementation(Class targetClass, final MethodInterceptor methodInterceptor) {
		AdvisedSupport aopConfig = new AdvisedSupport();
		aopConfig.setTargetClass(targetClass);
		aopConfig.setProxyTargetClass(true);
		aopConfig.addAdvice(new MethodInterceptor() {
			public Object invoke(MethodInvocation invocation) throws Throwable {
				Object rtn = null;
				if (methodInterceptor != null) {
					rtn = methodInterceptor.invoke(invocation);
				}
				return rtn;
			}
		});

		DefaultAopProxyFactory aopProxyFactory = new DefaultAopProxyFactory();
		AopProxy proxy = aopProxyFactory.createAopProxy(aopConfig);
		return proxy.getProxy();
	}

	public static Object methodTrackingObject(Object target) {

		AdvisedSupport aopConfig = new AdvisedSupport();
		aopConfig.setTarget(target);
		aopConfig.setProxyTargetClass(true);

		aopConfig.addAdvice(new MethodInterceptor() {
			public Object invoke(MethodInvocation invocation) throws Throwable {
				Object proxy = ((ProxyMethodInvocation) invocation).getProxy();
				if (!MethodCallAssertor.class.equals(invocation.getMethod().getDeclaringClass())) {
					((MethodCallAssertor) proxy).recordMethodCall(invocation.getMethod());
				}
				return invocation.proceed();
			}
		});
		aopConfig.addAdvice(new MethodTrackerInterceptor());

		DefaultAopProxyFactory aopProxyFactory = new DefaultAopProxyFactory();
		AopProxy proxy = aopProxyFactory.createAopProxy(aopConfig);
		return proxy.getProxy();
	}

	public static Object methodTrackingObject(Class targetClass) {
		Object nullImplementation = nullImplementation(targetClass);
		return methodTrackingObject(nullImplementation);
	}

	public static void callMethods(Object object, String[] methods) throws Exception {
		Set methodNames = new HashSet(Arrays.asList(methods));
		Method[] classMethods = object.getClass().getMethods();
		for (int i = 0; i < classMethods.length; i++) {
			if (methodNames.contains(classMethods[i].getName())) {
				classMethods[i].invoke(object, new Object[classMethods[i].getParameterTypes().length]);
			}
		}
	}
}
