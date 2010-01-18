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
package org.springframework.faces.mvc.converter;

import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.model.DataModel;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.binding.convert.ConversionService;
import org.springframework.util.Assert;

/**
 * {@link QuickConverter} implementation that is backed by a {@link ConversionService}.
 * 
 * @author Phillip Webb
 */
public class ConversionServiceQuickConverter implements QuickConverter, InitializingBean, BeanFactoryAware {

	private ConversionService conversionService;
	private BeanFactory beanFactory;

	@SuppressWarnings("unchecked")
	public <T> T execute(Object source, Class<T> targetClass) {
		return (T) conversionService.executeConversion(source, targetClass);
	}

	@SuppressWarnings("unchecked")
	public <T> T execute(String converterId, Object source, Class<T> targetClass) {
		return (T) conversionService.executeConversion(converterId, source, targetClass);
	}

	public Byte toByte(Object source) {
		return execute(source, Byte.class);
	}

	public DataModel toDataModel(Object source) {
		return execute(source, DataModel.class);
	}

	public Date toDate(Object source) {
		return execute(source, Date.class);
	}

	public Integer toInteger(Object source) {
		return execute(source, Integer.class);
	}

	public Long toLong(Object source) {
		return execute(source, Long.class);
	}

	public Number toNumber(Object source) {
		return execute(source, Number.class);
	}

	public Short toShort(Object source) {
		return execute(source, Short.class);
	}

	public String toString(Object source) {
		return execute(source, String.class);
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	public void afterPropertiesSet() throws Exception {
		// Search for a conversion service if one has not been set
		if ((conversionService == null) && (beanFactory != null) && (beanFactory instanceof ListableBeanFactory)) {
			ListableBeanFactory listableBeanFactory = (ListableBeanFactory) beanFactory;
			Map conversionBeans = listableBeanFactory.getBeansOfType(ConversionService.class);
			if (conversionBeans.size() == 1) {
				Map.Entry entry = (Entry) conversionBeans.entrySet().iterator().next();
				this.conversionService = (ConversionService) entry.getValue();
			}
		}
		Assert.notNull(conversionService, "The conversion service is required");
	}

	public void setConversionService(ConversionService conversionService) {
		Assert.notNull(conversionService, "The conversion service is required");
		this.conversionService = conversionService;
	}
}
