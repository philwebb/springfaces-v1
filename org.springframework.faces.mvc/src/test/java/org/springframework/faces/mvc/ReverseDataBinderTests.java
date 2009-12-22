package org.springframework.faces.mvc;

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

	private void initBinder(DataBinder dataBinder) {
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		df.setLenient(false);
		dataBinder.registerCustomEditor(Date.class, new CustomDateEditor(df, false));
	}

	public void testReverseBind() throws Exception {
		Sample target = new Sample();
		DataBinder dataBinder = new DataBinder(target);
		initBinder(dataBinder);
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("dateValue", "01/12/2009");
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

	private static class Sample {
		private Date dateValue;

		private Integer integerValue;

		private String stringValue;

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
	}
}