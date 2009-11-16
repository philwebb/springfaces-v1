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
package org.springframework.faces.bind.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;

/**
 * Annotation that can be used to encapsulate JSF navigation rules. This annotation can be placed on a {@link Method}
 * {@link Class} or {@link Package}.
 * 
 * @see NavigationCase
 * 
 * @author Phillip Webb
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface NavigationRules {
	public NavigationCase[] value();
}
