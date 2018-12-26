package nl.gingerbeard.automation.domoticz.helpers;

import java.util.ArrayList;
import java.util.List;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;

public class TestWebServer extends NanoHTTPD {

	public static final String JSON_OK = "{ \"status\" : \"OK\" }";
	public static final String JSON_ERROR = "{ \"status\" : \"error\" }";
	public static final String JSON_MALFORMED = "{ \"status\" ";
	private Status status = Status.OK;
	private final List<String> requests = new ArrayList<>();
	private String text = JSON_OK;

	public TestWebServer() {
		super(0);
	}

	public void setResponse(final Status status, final String text) {
		this.status = status;
		this.text = text;
	}

	@Override
	public Response serve(final IHTTPSession session) {
		getRequests().add(session.getMethod() + " " + session.getUri() + "?" + session.getQueryParameterString());
		return super.newFixedLengthResponse(status, MIME_PLAINTEXT, text);
	}

	public List<String> getRequests() {
		return requests;
	}

}
