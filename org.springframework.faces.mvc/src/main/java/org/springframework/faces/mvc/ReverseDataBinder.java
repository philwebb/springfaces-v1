package org.springframework.faces.mvc;

import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.util.Collections;
import java.util.Map;
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
import org.springframework.core.JdkVersion;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;

/**
 * Utility class that can be used to perform a reverse bind for a given {@link DataBinder}. This class can be used to
 * obtain {@link PropertyValues} for a given a {@link DataBinder} based on the current values of its <tt>target</tt>.
 * Use the {@link #reverseBind()} method to obtain {@link PropertyValues} containing a name/value pairs for each
 * property that can be bound. Property values are encoded as Strings using the property editors bound to the original
 * dataBinder. Note: Calling reverse bind will also trigger a <tt>bind</tt> operation on the dataBinder to test for
 * errors.
 * 
 * @author Phillip Webb
 */
public class ReverseDataBinder {

	Log logger = LogFactory.getLog(getClass());

	private static final Map unknownEditorTypes = Collections.synchronizedMap(new WeakHashMap());

	private DataBinder dataBinder;

	/**
	 * @param dataBinder A non null dataBinder with a valid <tt>target</tt> object set.
	 */
	public ReverseDataBinder(DataBinder dataBinder) {
		Assert.notNull(dataBinder, "Missing dataBinder");
		Assert.notNull(dataBinder.getTarget(),
				"ReverseDataBinder can only be used with a DataBinder that has a target object");
		this.dataBinder = dataBinder;
	}

	/**
	 * Find a default editor for the given type. This code is based on <tt>TypeConverterDelegate.findDefaultEditor</tt>
	 * from Spring 2.5.6.
	 * @param requiredType the type to find an editor for
	 * @param descriptor the JavaBeans descriptor for the property
	 * @return the corresponding editor, or <code>null</code> if none
	 * 
	 * @author Juergen Hoeller
	 * @author Rob Harrop
	 */
	protected PropertyEditor findDefaultEditor(PropertyEditorRegistrySupport propertyEditorRegistry,
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
			editor = propertyEditorRegistry.getDefaultEditor(requiredType);
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
	 * Perform the reverse bind on the <tt>dataBinder</tt> provided in the constructor. See class level comments for
	 * more information.
	 * 
	 * @return {@link PropertyValues} containing a name/value pairs for each property that can be bound.
	 */
	public PropertyValues reverseBind() {
		MutablePropertyValues pvs = new MutablePropertyValues();
		BeanWrapper target = PropertyAccessorFactory.forBeanPropertyAccess(dataBinder.getTarget());
		PropertyDescriptor[] propertyDescriptors = target.getPropertyDescriptors();
		for (int i = 0; i < propertyDescriptors.length; i++) {
			PropertyDescriptor property = propertyDescriptors[i];
			String propertyName = PropertyAccessorUtils.canonicalPropertyName(property.getName());
			if (!isReadWriteProperty(property)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Ignoring '" + propertyName + "' property due to missing read/write methods");
				}
				continue;
			}

			String propertyValue = reverseBindProperty(target, property, propertyName);
			if (propertyValue != null) {
				pvs.addPropertyValue(propertyName, propertyValue);
			}
		}

		dataBinder.bind(pvs);
		BindingResult bindingResult = dataBinder.getBindingResult();
		if (bindingResult.hasErrors()) {
			throw new IllegalStateException("Unable to reverse bind from target '" + dataBinder.getObjectName()
					+ "', the properties '" + pvs + "' will result in binding errors when re-bound "
					+ bindingResult.getAllErrors());
		}
		return pvs;
	}

	/**
	 * Reverse bind a single property.
	 * 
	 * @param target The target {@link BeanWrapper}.
	 * @param property The {@link PropertyDescriptor} for the property to convert.
	 * @param propertyName The property name in canonical form.
	 * @return A value for the property or <tt>null</tt>
	 */
	protected String reverseBindProperty(BeanWrapper target, PropertyDescriptor property, String propertyName) {

		Class type = target.getPropertyType(propertyName);

		// Find any custom editor
		PropertyEditor propertyEditor = dataBinder.findCustomEditor(type, propertyName);

		// Fall back to default editors
		if (propertyEditor == null && target instanceof PropertyEditorRegistrySupport) {
			propertyEditor = findDefaultEditor((PropertyEditorRegistrySupport) target, target.getWrappedInstance(),
					type, property);
		}

		Object value = target.getPropertyValue(propertyName);
		if (propertyEditor != null) {
			propertyEditor.setValue(value);
			return propertyEditor.getAsText();
		}
		if (value instanceof String) {
			return value == null ? null : value.toString();
		}
		return null;
	}

	private boolean isReadWriteProperty(PropertyDescriptor propertyDescriptor) {
		return propertyDescriptor.getReadMethod() != null && propertyDescriptor.getWriteMethod() != null;
	}
}
