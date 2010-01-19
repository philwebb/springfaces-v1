package org.springframework.faces.mvc.execution.repository;

import org.springframework.faces.mvc.execution.ExecutionContextKey;

public class IntegerExecutionContextKey extends ExecutionContextKey {

	private int value;

	public IntegerExecutionContextKey(int value) {
		this.value = value;
	}

	public boolean equals(Object o) {
		if (!(o instanceof IntegerExecutionContextKey)) {
			return false;
		}
		IntegerExecutionContextKey key = (IntegerExecutionContextKey) o;
		return value == key.value;
	}

	public int hashCode() {
		return value * 29;
	}

	public String toString() {
		return String.valueOf(value);
	}

}