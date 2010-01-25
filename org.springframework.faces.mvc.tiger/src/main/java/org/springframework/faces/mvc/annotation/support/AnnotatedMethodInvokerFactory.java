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
package org.springframework.faces.mvc.annotation.support;

import org.springframework.web.bind.support.WebArgumentResolver;

/**
 * Factory that can be used to create {@link AnnotatedMethodInvoker}s.
 * 
 * @author Phillip Webb
 */
public interface AnnotatedMethodInvokerFactory {
	/**
	 * Create a new {@link AnnotatedMethodInvoker} instance.
	 * @param additionalArgumentResolvers Any additional argument resolvers that should be used with the invoker (can be
	 * <tt>null</tt>.
	 * @return An {@link AnnotatedMethodInvoker} instance.
	 */
	public AnnotatedMethodInvoker newInvoker(WebArgumentResolver... additionalArgumentResolvers);
}
