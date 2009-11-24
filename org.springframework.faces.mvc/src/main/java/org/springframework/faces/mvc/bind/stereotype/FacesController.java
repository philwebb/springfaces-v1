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
package org.springframework.faces.mvc.bind.stereotype;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.faces.mvc.annotation.FacesAnnotationMethodHandlerAdapter;
import org.springframework.stereotype.Controller;

@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.TYPE })
@Controller
public @interface FacesController {
	/**
	 * The value may indicate a suggestion for a logical component name, to be turned into a Spring bean in case of an
	 * autodetected component.
	 * @return the suggested component name, if any
	 */
	String value() default "";

	/**
	 * Determine if the controller should be exposed to JSF as a variable. By default controllers will be exposed so
	 * that they can be referenced in page mark-up, for example: <code>#{controller.doSomething()}</code>.
	 * 
	 * @return <tt>true</tt> if the controller should be exposed to JSF <tt>false</tt> if it should not.
	 * @see #controllerName()
	 */
	boolean exposeController() default true;

	/**
	 * Get the name of the controller that is exposed to JSF when {@link #exposeController()} is set to <tt>true</tt>.
	 * The name of the controller can be specified or this value can be omitted to use the default name (as defined by
	 * the {@link FacesAnnotationMethodHandlerAdapter}, usually <tt>controller</tt>).
	 * 
	 * @return The controller name
	 * @see #exposeController()
	 */
	String controllerName() default "";
}
