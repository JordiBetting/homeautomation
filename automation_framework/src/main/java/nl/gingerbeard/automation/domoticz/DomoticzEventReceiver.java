package nl.gingerbeard.automation.domoticz;

import java.io.IOException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;

public class DomoticzEventReceiver extends NanoHTTPD {

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

	DomoticzEventReceiver(final int port) throws IOException {
		super(port);
		start(NanoHTTPD.SOCKET_READ_TIMEOUT, true);
	}

	public void setEventListener(final EventReceived listener) {
		this.listener = Optional.ofNullable(listener);
	}

	@Override
	public Response serve(final IHTTPSession session) {
		Optional<Response> response = Optional.empty();
		if (session.getMethod() == Method.GET) {
			try {
				final String uri = session.getUri();

				final Matcher m = URIPATTERN.matcher(uri);
				if (m.matches()) {
					response = Optional.of(newFixedLengthResponse(Status.OK, NanoHTTPD.MIME_PLAINTEXT, "OKIDOKI"));
					if (listener.isPresent()) {
						final boolean result = listener.get().deviceChanged(Integer.parseInt(m.group(1)), m.group(2));
						if (result == false) {
							response = Optional.of(newFixedLengthResponse(Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "Could not process request."));
						}
					}
				}
			} catch (final Throwable t) {
				response = Optional.of(newFixedLengthResponse(Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, "Internal server error: " + t.getMessage()));
			}
		} else {
			response = Optional.of(newFixedLengthResponse(Status.METHOD_NOT_ALLOWED, NanoHTTPD.MIME_PLAINTEXT, "Only GET is supported"));
		}
		return response.orElse(super.serve(session));
	}

}
