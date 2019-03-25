package nl.gingerbeard.automation.configuration;

public final class Request {
	private final String[] uriParameters;

	public Request(final String[] uriParameters) {
		this.uriParameters = uriParameters;
	}

	public String[] getUriParameters() {
		return uriParameters;
	}

}