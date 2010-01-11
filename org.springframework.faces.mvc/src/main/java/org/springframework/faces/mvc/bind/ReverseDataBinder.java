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
package org.springframework.faces.mvc.bind;

import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.PropertyAccessorUtils;
import org.springframework.beans.PropertyEditorRegistrySupport;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.core.JdkVersion;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;

/**
 * Utility class that can be used to perform a reverse bind for a given {@link DataBinder}. This class can be used to
 * obtain {@link PropertyValues} for a given a {@link DataBinder} based on the current values of its <tt>target</tt> or
 * perform a simple reverse conversion for plain parameter values when the binders <tt>target</tt> is <tt>null</tt>.
 * 
 * @author Phillip Webb
 */
public class ReverseDataBinder {

	Log logger = LogFactory.getLog(getClass());

	private static Map unknownEditorTypes = Collections.synchronizedMap(new WeakHashMap());

	/**
	 * Set of properties that are always skipped.
	 */
	private static final Set SKIPPED_PROPERTIES;
	static {
		SKIPPED_PROPERTIES = new HashSet();
		SKIPPED_PROPERTIES.add("class");
	}

	private DataBinder dataBinder;

	private SimpleTypeConverter simpleTypeConverter;

	private boolean skipDefaultValues = true;

	/**
	 * Get the canonical property name for a given optional descriptor.
	 * 
	 * @param descriptor The descriptor or <tt>null</tt>
	 * @return The canonical property name or <tt>null</tt>
	 */
	private String getPropertyName(PropertyDescriptor descriptor) {
		return descriptor == null ? null : PropertyAccessorUtils.canonicalPropertyName(descriptor.getName());
	}

	/**
	 * Determine if a property contains both read and write methods.
	 * 
	 * @param descriptor The property descriptor
	 * @return <tt>true</tt> if the property is mutable
	 */
	private boolean isMutableProperty(PropertyDescriptor descriptor) {
		return descriptor.getReadMethod() != null && descriptor.getWriteMethod() != null;
	}

	/**
	 * Determine if a property should be skipped. Used to ignore object properties.
	 * 
	 * @param property the property descriptor
	 * @return <tt>true</tt> if the property is skipped
	 */
	private boolean isSkippedProperty(PropertyDescriptor property) {
		return SKIPPED_PROPERTIES.contains(property.getName());
	}

	/**
	 * @param dataBinder A non null dataBinder
	 */
	public ReverseDataBinder(DataBinder dataBinder) {
		Assert.notNull(dataBinder, "Missing dataBinder");
		this.dataBinder = dataBinder;
	}

	/**
	 * Find a property editor by searching custom editors or falling back to default editors.
	 * 
	 * @param propertyEditorRegistrySupport An optional {@link PropertyEditorRegistrySupport} instance. If <tt>null</tt>
	 * a {@link SimpleTypeConverter} instance will be used
	 * @param targetObject The target object or <tt>null</tt>
	 * @param requiredType The required type.
	 * @param descriptor The descriptor or <tt>null</tt>
	 * @return the corresponding editor, or <code>null</code> if none
	 */
	protected PropertyEditor findEditor(PropertyEditorRegistrySupport propertyEditorRegistrySupport,
			Object targetObject, Class requiredType, PropertyDescriptor descriptor) {
		String propertyName = getPropertyName(descriptor);

		// Find any custom editor
		PropertyEditor propertyEditor = dataBinder.findCustomEditor(requiredType, propertyName);

		// Fall back to default editors
		if (propertyEditor == null) {
			if (propertyEditorRegistrySupport == null) {
				propertyEditorRegistrySupport = getSimpleTypeConverter();
			}
			propertyEditor = findDefaultEditor(propertyEditorRegistrySupport, targetObject, requiredType, descriptor);
		}
		return propertyEditor;
	}

	/**
	 * Find a default editor for the given type. This code is based on <tt>TypeConverterDelegate.findDefaultEditor</tt>
	 * from Spring 2.5.6.
	 * 
	 * @param requiredType the type to find an editor for
	 * @param descriptor the JavaBeans descriptor for the property
	 * @return the corresponding editor, or <code>null</code> if none
	 * 
	 * @author Juergen Hoeller
	 * @author Rob Harrop
	 */
	protected PropertyEditor findDefaultEditor(PropertyEditorRegistrySupport propertyEditorRegistrySupport,
			Object targetObject, Class requiredType, PropertyDescriptor descriptor) {

		PropertyEditor editor = null;
		if (descriptor != null) {
			if (JdkVersion.isAtLeastJava15()) {
				editor = descriptor.createPropertyEditor(targetObject);
			} else {
				Class editorClass = descriptor.getPropertyEditorClass();
				if (editorClass != null) {
					editor = (PropertyEditor) BeanUtils.instantiateClass(editorClass);
				}
			}
		}

		if (editor == null && requiredType != null) {
			// No custom editor -> check default editors.
			editor = propertyEditorRegistrySupport.getDefaultEditor(requiredType);
			if (editor == null && !String.class.equals(requiredType)) {
				// No BeanWrapper default editor -> check standard JavaBean editor.
				editor = BeanUtils.findEditorByConvention(requiredType);
				if (editor == null && !unknownEditorTypes.containsKey(requiredType)) {
					// Global PropertyEditorManager fallback...
					editor = PropertyEditorManager.findEditor(requiredType);
					if (editor == null) {
						// Regular case as of Spring 2.5
						unknownEditorTypes.put(requiredType, Boolean.TRUE);
					}
				}
			}
		}
		return editor;
	}

	/**
	 * Utility method to convert a given value into a string using a property editor.
	 * 
	 * @param value The value to convert (can be <tt>null</tt>)
	 * @param propertyEditor The property editor or <tt>null</tt> if no suitable property editor exists
	 * @return The converted value
	 */
	private String convertToStringUsingPropertyEditor(Object value, PropertyEditor propertyEditor) {
		if (propertyEditor != null) {
			propertyEditor.setValue(value);
			return propertyEditor.getAsText();
		}
		if (value instanceof String) {
			return value == null ? null : value.toString();
		}
		return null;

	}

	protected SimpleTypeConverter getSimpleTypeConverter() {
		if (simpleTypeConverter == null) {
			simpleTypeConverter = new SimpleTypeConverter();
		}
		return simpleTypeConverter;
	}

	/**
	 * Perform the reverse bind on the <tt>dataBinder</tt> provided in the constructor. Note: Calling with method will
	 * also trigger a <tt>bind</tt> operation on the <tt>dataBinder</tt>.
	 * 
	 * @return {@link PropertyValues} containing a name/value pairs for each property that can be bound.Property values
	 * are encoded as Strings using the property editors bound to the original dataBinder
	 * 
	 * @throws IllegalStateException if the target object values cannot be bound
	 */
	public PropertyValues reverseBind() {
		Assert.notNull(dataBinder.getTarget(),
				"ReverseDataBinder.reverseBind can only be used with a DataBinder that has a target object");
		MutablePropertyValues rtn = new MutablePropertyValues();
		BeanWrapper target = PropertyAccessorFactory.forBeanPropertyAccess(dataBinder.getTarget());
		PropertyDescriptor[] propertyDescriptors = target.getPropertyDescriptors();

		BeanWrapper defaultValues = null;
		if (skipDefaultValues) {
			defaultValues = newDefaultTargetValues(dataBinder.getTarget());
		}

		for (int i = 0; i < propertyDescriptors.length; i++) {
			PropertyDescriptor property = propertyDescriptors[i];
			String propertyName = getPropertyName(property);
			Object propertyValue = target.getPropertyValue(propertyName);

			if (isSkippedProperty(property)) {
				continue;
			}

			if (!isMutableProperty(property)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Ignoring '" + propertyName + "' due to missing read/write methods");
				}
				continue;
			}

			if (defaultValues != null
					&& ObjectUtils.nullSafeEquals(defaultValues.getPropertyValue(propertyName), propertyValue)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Skipping '" + propertyName + "' as property contains default value");
				}
				continue;
			}

			// Find a property editor
			PropertyEditorRegistrySupport propertyEditorRegistrySupport = null;
			if (target instanceof PropertyEditorRegistrySupport) {
				propertyEditorRegistrySupport = (PropertyEditorRegistrySupport) target;
			}
			PropertyEditor propertyEditor = findEditor(propertyEditorRegistrySupport, target.getWrappedInstance(),
					target.getPropertyType(propertyName), property);

			// Convert and store the value
			String convertedPropertyValue = convertToStringUsingPropertyEditor(propertyValue, propertyEditor);
			if (convertedPropertyValue != null) {
				rtn.addPropertyValue(propertyName, convertedPropertyValue);
			}
		}

		dataBinder.bind(rtn);
		BindingResult bindingResult = dataBinder.getBindingResult();
		if (bindingResult.hasErrors()) {
			throw new IllegalStateException("Unable to reverse bind from target '" + dataBinder.getObjectName()
					+ "', the properties '" + rtn + "' will result in binding errors when re-bound "
					+ bindingResult.getAllErrors());
		}
		return rtn;
	}

	private BeanWrapper newDefaultTargetValues(Object target) {
		try {
			Object defaultValues = target.getClass().newInstance();
			return PropertyAccessorFactory.forBeanPropertyAccess(defaultValues);
		} catch (Exception e) {
			logger.warn("Unable to construct default values target instance for class " + target.getClass()
					+ ", default values will not be skipped");
			return null;
		}
	}

	/**
	 * Reverse convert a simple object value.
	 * 
	 * @param value The value to convert
	 * @return The converted value
	 */
	public String reverseConvert(Object value) {
		if (value == null) {
			return null;
		}
		PropertyEditor propertyEditor = findEditor(null, null, value.getClass(), null);
		return convertToStringUsingPropertyEditor(value, propertyEditor);
	}

	/**
	 * Skip any bound values when the current value is identical to the value of a newly constructed instance. This
	 * setting can help to reduce the number of superfluous bound properties. Note: If the target object class does not
	 * have a default (no-args) constructor this setting will be ignored. The default setting is <tt>true</tt>.
	 * 
	 * @param skipDefaultValues <tt>true</tt> if default properties should be ignored, otherwise <tt>false</tt>
	 */
	public void setSkipDefaultValues(boolean skipDefaultValues) {
		this.skipDefaultValues = skipDefaultValues;
	}
}
