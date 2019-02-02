package nl.gingerbeard.automation.service.exception;

public class ComponentException extends RuntimeException {
	private static final long serialVersionUID = -6897990188839479412L;

	public ComponentException(final String message) {
		super(message);
	}

	public ComponentException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
