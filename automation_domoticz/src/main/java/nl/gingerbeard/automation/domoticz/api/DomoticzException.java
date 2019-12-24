package nl.gingerbeard.automation.domoticz.api;

public class DomoticzException extends Exception {

	private static final long serialVersionUID = 1L;

	public DomoticzException(String message, Throwable cause) {
		super(message, cause);
	}

	public DomoticzException(String message) {
		super(message);
	}
	
	public DomoticzException(Throwable cause) {
		super(cause);
	}

}
