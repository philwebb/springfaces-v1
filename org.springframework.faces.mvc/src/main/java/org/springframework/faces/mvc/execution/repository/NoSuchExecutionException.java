package org.springframework.faces.mvc.execution.repository;

import org.springframework.faces.mvc.execution.ExecutionContextKey;

/**
 * Exception thrown when no execution with the specified key exists in the repository.
 * 
 * @author Phillip Webb
 */
public class NoSuchExecutionException extends ExecutionContextRepositoryException {

	/**
	 * The key that could not be found.
	 */
	private ExecutionContextKey key;

	/**
	 * Constructor.
	 * @param key The key that could not be found
	 * @param cause The cause of the exception
	 */
	public NoSuchExecutionException(ExecutionContextKey key, Throwable cause) {
		super("Unable to locate a Faces MVC execution with the key '" + key + "'", cause);
		this.key = key;
	}

	/**
	 * Constructor.
	 * @param key The key that could not be found
	 */
	public NoSuchExecutionException(ExecutionContextKey key) {
		this(key, null);
	}

	/**
	 * Returns the execution key that could not be found.
	 * @return The key that could not be found
	 */
	public ExecutionContextKey getKey() {
		return key;
	}
}
