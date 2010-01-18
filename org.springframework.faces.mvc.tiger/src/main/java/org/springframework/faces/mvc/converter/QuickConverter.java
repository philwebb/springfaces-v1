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

import javax.faces.model.DataModel;

import org.springframework.binding.convert.ConversionException;

/**
 * Helper interface that makes it easier to perform conversion of a source input to a target output programmatically.
 * The {@link #execute(Object, Class)} method can be used to convert a source object to a specific class. In addition
 * several helper methods are provided for conversion to common types, for example, to convert the source object to a
 * JSF DataModel use {@link #toDataModel(Object) toDataModel}.
 * 
 * @see ConversionServiceQuickConverter
 * 
 * @author Phillip Webb
 */
public interface QuickConverter {

	/**
	 * Execute conversion from the specified source object to the target class.
	 * @param <T> The target class type
	 * @param source The source object to convert (may be null)
	 * @param targetClass the target type to convert to (required)
	 * @return the converted object, an instance of targetType
	 * @throws ConversionException if an exception occurred
	 */
	<T> T execute(Object source, Class<T> targetClass);

	/**
	 * Convert the specified source to a <tt>String</tt>.
	 * @param source The source object to convert (may be null)
	 * @return the converted object as a <tt>String</tt>
	 * @throws ConversionException if an exception occurred
	 */
	String toString(Object source);

	/**
	 * Convert the specified source to a <tt>Byte</tt>.
	 * @param source The source object to convert (may be null)
	 * @return the converted object as a <tt>Byte</tt>
	 * @throws ConversionException if an exception occurred
	 */
	Byte toByte(Object source);

	/**
	 * Convert the specified source to a <tt>Short</tt>.
	 * @param source The source object to convert (may be null)
	 * @return the converted object as a <tt>Short</tt>
	 * @throws ConversionException if an exception occurred
	 */
	Short toShort(Object source);

	/**
	 * Convert the specified source to an <tt>Integer</tt>.
	 * @param source The source object to convert (may be null)
	 * @return the converted object as an <tt>Integer</tt>
	 * @throws ConversionException if an exception occurred
	 */
	Integer toInteger(Object source);

	/**
	 * Convert the specified source to a <tt>Long</tt>.
	 * @param source The source object to convert (may be null)
	 * @return the converted object as a <tt>Long</tt>
	 * @throws ConversionException if an exception occurred
	 */
	Long toLong(Object source);

	/**
	 * Convert the specified source to a <tt>Number</tt>.
	 * @param source The source object to convert (may be null)
	 * @return the converted object as a <tt>Number</tt>
	 * @throws ConversionException if an exception occurred
	 */
	Number toNumber(Object source);

	/**
	 * Convert the specified source to a <tt>Date</tt>.
	 * @param source The source object to convert (may be null)
	 * @return the converted object as a <tt>Date</tt>
	 * @throws ConversionException if an exception occurred
	 */
	Date toDate(Object source);

	/**
	 * Convert the specified source to a <tt>DataModel</tt>.
	 * @param source The source object to convert (may be null)
	 * @return the converted object as a <tt>DataModel</tt>
	 * @throws ConversionException if an exception occurred
	 */
	DataModel toDataModel(Object source);
}
