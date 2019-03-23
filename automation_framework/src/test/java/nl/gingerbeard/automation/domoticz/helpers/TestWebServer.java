package nl.gingerbeard.automation.domoticz.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;

public class TestWebServer extends NanoHTTPD {

	private static final String MIME_JSON = "application/json";
	public static final String JSON_OK = "{ \"status\" : \"OK\" }";
	public static final String JSON_ERROR = "{ \"status\" : \"error\" }";
	public static final String JSON_MALFORMED = "{ \"status\" ";

	private final List<String> requests = new ArrayList<>();
	private FixedResponse defaultResponse;

	private static class FixedResponse {
		private final Status status;
		private final String body;

		public FixedResponse(final Status status, final String body) {
			super();
			this.status = status;
			this.body = body;
		}

		public Status getStatus() {
			return status;
		}

		public String getBody() {
			return body;
		}

	}

	private final Map<String, FixedResponse> responses = new HashMap<>();
	private Optional<CountDownLatch> requestLatch = Optional.empty();

	public TestWebServer() {
		super(0);
		setDefaultResponse(Status.OK, JSON_OK);
	}

	public void setResponse(final String uri, final Status status, final String body) {
		responses.put(uri, new FixedResponse(status, body));
	}

	@Override
	public Response serve(final IHTTPSession session) {
		recordRequest(session);
		final FixedResponse response = determineResponse(session);
		final Response out = createHttpResponse(response);
		triggerLatch();
		return out;
	}

	private void triggerLatch() {
		if (requestLatch.isPresent()) {
			requestLatch.get().countDown();
			requestLatch = Optional.empty();
		}
	}

	private Response createHttpResponse(final FixedResponse response) {
		return super.newFixedLengthResponse(response.getStatus(), MIME_JSON, response.getBody());
	}

	private void recordRequest(final IHTTPSession session) {
		final String logline = String.format("%s %s?%s", //
				session.getMethod(), //
				session.getUri(), //
				session.getQueryParameterString());
		getRequests().add(logline);
	}

	private FixedResponse determineResponse(final IHTTPSession session) {
		final FixedResponse response = responses.get(session.getUri() + "?" + session.getQueryParameterString());
		return response != null ? response : defaultResponse;
	}

	public List<String> getRequests() {
		return requests;
	}

	public void setDefaultResponse(final Status status, final String body) {
		defaultResponse = new FixedResponse(status, body);
	}

	public void forgetRequest(final String request) {
		requests.remove(request);
	}

	public void setRequestLatch(final CountDownLatch requestLatch) {
		this.requestLatch = Optional.of(requestLatch);
	}

}
