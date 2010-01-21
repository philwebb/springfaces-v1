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
package org.springframework.faces.mvc.execution;

import org.springframework.faces.mvc.execution.repository.ExecutionContextRepository;

/**
 * A key that uniquely identifies a MVC faces execution in a managed {@link ExecutionContextRepository}. This class is
 * abstract. The repository subsystem encapsulates the structure of concrete key implementations.
 */
public abstract class ExecutionContextKey {

	public abstract boolean equals(Object o);

	public abstract int hashCode();

	public abstract String toString();
}
