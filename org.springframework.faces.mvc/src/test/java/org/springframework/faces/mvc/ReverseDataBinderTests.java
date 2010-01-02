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

import java.beans.PropertyEditorSupport;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import junit.framework.TestCase;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.validation.DataBinder;

public class ReverseDataBinderTests extends TestCase {

	private static final Date D01_12_2009;
	static {
		Calendar c = Calendar.getInstance();
		c.set(2009, Calendar.DECEMBER, 1, 0, 0, 0);
		c.set(Calendar.MILLISECOND, 0);
		D01_12_2009 = c.getTime();
	}

	private static final String S01_12_2009 = "2009/01/12";

	private void initBinder(DataBinder dataBinder) {
		DateFormat df = new SimpleDateFormat("yyyy/dd/MM");
		df.setLenient(false);
		dataBinder.registerCustomEditor(Date.class, new CustomDateEditor(df, false));
	}

	private void doTestReverseConvert(String value, Object expected) throws Exception {
		DataBinder dataBinder = new DataBinder(null);
		initBinder(dataBinder);
		Object converted = (value == null ? null : dataBinder.convertIfNecessary(value, expected.getClass()));
		assertEquals(expected, converted);
		ReverseDataBinder reverseDataBinder = new ReverseDataBinder(dataBinder);
		String reversed = reverseDataBinder.reverseConvert(converted);
		assertEquals(value, reversed);
	}

	public void testReverseConvertWithCustomEditor() throws Exception {
		doTestReverseConvert(S01_12_2009, D01_12_2009);
	}

	public void testReverseConvertForDefaultType() throws Exception {
		doTestReverseConvert("1234", new Integer(1234));
	}

	public void testReverseConvertWithNull() throws Exception {
		doTestReverseConvert(null, null);
	}

	public void testReverseBind() throws Exception {
		Sample target = new Sample();
		DataBinder dataBinder = new DataBinder(target);
		initBinder(dataBinder);
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("dateValue", S01_12_2009);
		pvs.addPropertyValue("integerValue", "123");
		pvs.addPropertyValue("stringValue", "string");
		dataBinder.bind(pvs);

		assertEquals(new Integer(123), target.getIntegerValue());
		assertEquals("string", target.getStringValue());
		assertEquals(D01_12_2009, target.getDateValue());

		ReverseDataBinder reverseDataBinder = new ReverseDataBinder(dataBinder);
		PropertyValues result = reverseDataBinder.reverseBind();
		for (int i = 0; i < result.getPropertyValues().length; i++) {
			PropertyValue pv = result.getPropertyValues()[i];
			System.out.println(pv.getName() + "=" + pv.getValue());
		}
		assertEquals(pvs, result);
	}

	public void testReverseBindWithErrors() throws Exception {
		Sample target = new Sample();
		DataBinder dataBinder = new DataBinder(target);
		dataBinder.setRequiredFields(new String[] { "integerValue" });
		ReverseDataBinder reverseDataBinder = new ReverseDataBinder(dataBinder);
		try {
			reverseDataBinder.reverseBind();
			fail();
		} catch (IllegalStateException e) {
			assertEquals(
					"Unable to reverse bind from target 'target', the properties 'PropertyValues: length=0; ' "
							+ "will result in binding errors when re-bound [Field error in object 'target' on field "
							+ "'integerValue': rejected value []; codes [required.target.integerValue,required.integerValue,"
							+ "required.java.lang.Integer,required]; arguments [org.springframework.context.support.DefaultMessageSourceResolvable: "
							+ "codes [target.integerValue,integerValue]; arguments []; default message [integerValue]]; "
							+ "default message [Field 'integerValue' is required]]", e.getMessage());
		}
	}

	private void doTestReverseBindWithDefaultValues(boolean dontSkip, boolean noConstructor) throws Exception {
		Sample target = noConstructor ? new SampleWithoutDefaultConstructor("") : new Sample();
		target.setIntegerValue(new Integer(123));
		DataBinder dataBinder = new DataBinder(target);
		ReverseDataBinder reverseDataBinder = new ReverseDataBinder(dataBinder);
		if (dontSkip) {
			// Only set when skipped to test default is true
			reverseDataBinder.setSkipDefaultValues(false);
		}
		PropertyValues result = reverseDataBinder.reverseBind();
		boolean fullBindExpected = dontSkip || noConstructor;
		assertEquals(fullBindExpected ? 2 : 1, result.getPropertyValues().length);
		assertEquals("123", result.getPropertyValue("integerValue").getValue());
		if (fullBindExpected) {
			assertEquals("default", result.getPropertyValue("stringValue").getValue());
		}
	}

	public void testReverseBindWithDefaultValues() throws Exception {
		doTestReverseBindWithDefaultValues(false, false);
	}

	public void testReverseBindWithDefaultValuesNotSkipped() throws Exception {
		doTestReverseBindWithDefaultValues(true, false);
	}

	public void testReverseBindWithDefaultValuesNoConstructor() throws Exception {
		doTestReverseBindWithDefaultValues(false, true);
	}

	public static class Sample {
		private Date dateValue;

		private Integer integerValue;

		private String stringValue = "default";

		public Date getDateValue() {
			return dateValue;
		}

		public void setDateValue(Date dateValue) {
			this.dateValue = dateValue;
		}

		public Integer getIntegerValue() {
			return integerValue;
		}

		public void setIntegerValue(Integer integerValue) {
			this.integerValue = integerValue;
		}

		public String getStringValue() {
			return stringValue;
		}

		public void setStringValue(String stringValue) {
			this.stringValue = stringValue;
		}

		public String getNonMutable() {
			return "";
		}
	}

	public static class SampleWithoutDefaultConstructor extends Sample {
		public SampleWithoutDefaultConstructor(String argument) {
			super();
		}
	}

	public static class ThrowingPropertyEditor extends PropertyEditorSupport {
		public String getAsText() {
			throw new RuntimeException("test error");
		}
	}
}