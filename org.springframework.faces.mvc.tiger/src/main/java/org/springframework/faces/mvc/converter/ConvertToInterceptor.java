package org.springframework.faces.mvc.converter;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.binding.convert.ConversionService;

public class ConvertToInterceptor implements MethodInterceptor {

	private ConversionService conversionService;

	public Object invoke(MethodInvocation invocation) throws Throwable {
		Object result = invocation.proceed();
		ConvertTo convertTo = invocation.getMethod().getAnnotation(ConvertTo.class);
		if (convertTo != null) {
			result = conversionService.executeConversion(result, convertTo.value());
		}
		return result;
	}

	public void setConversionService(ConversionService conversionService) {
		this.conversionService = conversionService;
	}
}
