package nl.gingerbeard.automation.domoticz;

import java.io.IOException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;

public class DomoticzEventReceiver extends NanoHTTPD implements IDomoticzEventReceiver {

	public static interface EventReceived {
		/**
		 * Indicate that a device state has changed in domoticz. Exceptions result in internal server error (500).
		 *
		 * @param idx
		 *            Identification in domoticz
		 * @param newState
		 *            State string
		 * @return True in case the device update was processed succesfully. False otherwise.
		 */
		public boolean deviceChanged(int idx, String newState);
	}

	// matches /id/action/ e.g. /123/close or /123/close/
	private static final Pattern URIPATTERN = Pattern.compile("/([0-9]+)/([a-zA-Z]+)/?");
	private Optional<EventReceived> listener = Optional.empty();

	public DomoticzEventReceiver(final int port) throws IOException {
		super(port);
		start(NanoHTTPD.SOCKET_READ_TIMEOUT, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.gingerbeard.automation.domoticz.IDomoticzEventReceiver#setEventListener(nl.gingerbeard.automation.domoticz.DomoticzEventReceiver.EventReceived)
	 */
	@Override
	public void setEventListener(final EventReceived listener) {
		this.listener = Optional.ofNullable(listener);
	}

	@Override
	public Response serve(final IHTTPSession session) {
		Optional<Response> response = Optional.empty();
		if (session.getMethod() == Method.GET) {
			response = processGetRequest(session);
		} else {
			response = Optional.of(newFixedLengthResponse(Status.METHOD_NOT_ALLOWED, NanoHTTPD.MIME_PLAINTEXT, "Only GET is supported"));
		}
		return response.orElse(super.serve(session));
	}

	private Optional<Response> processGetRequest(final IHTTPSession session) {
		Optional<Response> response;
		try {
			response = processGetRequest(session.getUri());
		} catch (final Throwable t) {
			response = Optional.of(newFixedLengthResponse(Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, "Internal server error: " + t.getMessage()));
		}
		return response;
	}

	private static class ResponseParameters {
		public final int idx;
		public final String state;

		public ResponseParameters(final int idx, final String state) {
			this.idx = idx;
			this.state = state;
		}

	}

	private Optional<Response> processGetRequest(final String uri) {
		Optional<Response> response = Optional.empty();

		final Optional<ResponseParameters> responseParams = parseParameters(uri);
		if (responseParams.isPresent()) {
			final Optional<Response> defaultResponse = Optional.of(newFixedLengthResponse(Status.OK, NanoHTTPD.MIME_PLAINTEXT, "OKIDOKI"));
			final Optional<Response> listenerResponse = triggerListener(responseParams.get());
			response = listenerResponse.isPresent() ? listenerResponse : defaultResponse;
		}
		return response;
	}

	private Optional<Response> triggerListener(final ResponseParameters responseParams) {
		Optional<Response> response = Optional.empty();

		if (listener.isPresent()) {
			final boolean result = listener.get().deviceChanged(responseParams.idx, responseParams.state);
			if (result == false) {
				response = Optional.of(newFixedLengthResponse(Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "Could not process request."));
			}
		}
		return response;
	}

	private Optional<ResponseParameters> parseParameters(final String uri) {
		final Matcher m = URIPATTERN.matcher(uri);
		final boolean matches = m.matches();
		Optional<ResponseParameters> responseParams = Optional.empty();
		if (matches) {
			responseParams = Optional.of(new ResponseParameters(Integer.parseInt(m.group(1)), m.group(2)));
		}
		return responseParams;
	}

}
