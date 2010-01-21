package org.springframework.faces.mvc.execution.repository;

/**
 * Root of all execution context repository exceptions.
 * 
 * @author Phillip Webb
 * @see ExecutionContextRepository
 */
public class ExecutionContextRepositoryException extends RuntimeException {

	public ExecutionContextRepositoryException() {
		super();
	}

	public ExecutionContextRepositoryException(String message, Throwable cause) {
		super(message, cause);
	}

	public ExecutionContextRepositoryException(String message) {
		super(message);
	}

	public ExecutionContextRepositoryException(Throwable cause) {
		super(cause);
	}
}
