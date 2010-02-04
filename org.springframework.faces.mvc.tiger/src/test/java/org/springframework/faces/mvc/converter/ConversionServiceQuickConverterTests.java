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

import java.io.Reader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.faces.model.DataModel;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.binding.convert.ConversionService;

public class ConversionServiceQuickConverterTests extends TestCase {

	private Object source = new Object();
	private ConversionService conversionService;
	private ConversionServiceQuickConverter converter;

	protected void setUp() throws Exception {
		conversionService = EasyMock.createMock(ConversionService.class);
		converter = new ConversionServiceQuickConverter();
		converter.setConversionService(conversionService);
	}

	private void doTest(Class<?> expectedType, Runnable runnable) {
		EasyMock.expect(conversionService.executeConversion(source, expectedType)).andReturn(null);
		EasyMock.replay(conversionService);
		runnable.run();
		EasyMock.verify(conversionService);
	}

	public void testExecute() throws Exception {
		doTest(Reader.class, new Runnable() {
			public void run() {
				converter.execute(source, Reader.class);
			}
		});
	}

	public void testToString() throws Exception {
		doTest(String.class, new Runnable() {
			public void run() {
				converter.toString(source);
			}
		});
	}

	public void testToByte() throws Exception {
		doTest(Byte.class, new Runnable() {
			public void run() {
				converter.toByte(source);
			}
		});
	}

	public void testToShort() throws Exception {
		doTest(Short.class, new Runnable() {
			public void run() {
				converter.toShort(source);
			}
		});
	}

	public void testToInteger() throws Exception {
		doTest(Integer.class, new Runnable() {
			public void run() {
				converter.toInteger(source);
			}
		});
	}

	public void testToLong() throws Exception {
		doTest(Long.class, new Runnable() {
			public void run() {
				converter.toLong(source);
			}
		});
	}

	public void testToNumber() throws Exception {
		doTest(Number.class, new Runnable() {
			public void run() {
				converter.toNumber(source);
			}
		});
	}

	public void testToDate() throws Exception {
		doTest(Date.class, new Runnable() {
			public void run() {
				converter.toDate(source);
			}
		});
	}

	public void testToDataModel() throws Exception {
		doTest(DataModel.class, new Runnable() {
			public void run() {
				converter.toDataModel(source);
			}
		});
	}

	private void setupFromContext(Map<String, ConversionService> beans) throws Exception {
		ConversionServiceQuickConverter converter = new ConversionServiceQuickConverter();
		assertNull(converter.getConversionService());
		ListableBeanFactory beanFactory = EasyMock.createMock(ListableBeanFactory.class);
		EasyMock.expect(beanFactory.getBeansOfType(ConversionService.class)).andReturn(beans);
		EasyMock.replay(beanFactory);
		converter.setBeanFactory(beanFactory);
		converter.afterPropertiesSet();
	}

	public void testMissingConversionServiceFromContext() throws Exception {
		try {
			setupFromContext(new HashMap<String, ConversionService>());
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals("The conversion service is required", e.getMessage());
		}
	}

	public void testMultipleConversionServiceFromContext() throws Exception {
		try {
			Map<String, ConversionService> beans = new HashMap<String, ConversionService>();
			beans.put("conversionService1", conversionService);
			beans.put("conversionService2", conversionService);
			setupFromContext(beans);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals("The conversion service is required", e.getMessage());
		}
	}

	public void testConversionServiceFromContext() throws Exception {
		Map<String, ConversionService> beans = new HashMap<String, ConversionService>();
		beans.put("conversionService", conversionService);
		setupFromContext(beans);
		assertSame(conversionService, converter.getConversionService());
	}
}
