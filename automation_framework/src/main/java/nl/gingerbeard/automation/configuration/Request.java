package nl.gingerbeard.automation.configuration;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;

public final class Request {
	private final String[] uriParameters;
	private final IHTTPSession session;

	public Request(final String[] uriParameters, final IHTTPSession session) {
		this.uriParameters = uriParameters;
		this.session = session;
	}

	public String[] getUriParameters() {
		return uriParameters;
	}

	public IHTTPSession getSession() {
		return session;
	}

}