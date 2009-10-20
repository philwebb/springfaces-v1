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
package org.springframework.faces;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation that can be used to map JSF navigation cases. This annotation can be used as part of the
 * {@link NavigationRules} annotation or an individual method.
 * 
 * @author Phillip Webb
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface NavigationCase {

	/**
	 * The 'on' case that the navigation case applies to. If this value is omitted the navigation case applies for all
	 * views that the handler controls.
	 * 
	 * @return
	 */
	public String[] on() default {};

	/**
	 * Exception classes that the navigation case applies to.
	 */
	public Class<? extends Exception>[] onException() default {};

	/**
	 * The to case that the navigation case applies to. This value is omitted the result of the method will be used or,
	 * if the result is unavailable, the current view will be re-rendered.
	 * 
	 * @return
	 */
	public String to() default "";
}
