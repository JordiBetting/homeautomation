package nl.gingerbeard.automation.domoticz;

import java.io.IOException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;

public class DomoticzEventReceiver extends NanoHTTPD {

	public static interface EventReceived {
		public void deviceChanged(int id, String newState);
	}

	// matches /id/action/ e.g. /123/close or /123/close/
	private static final Pattern URIPATTERN = Pattern.compile("/([0-9]+)/([a-zA-Z]+)/?");
	private Optional<EventReceived> listener = Optional.empty();

	public DomoticzEventReceiver() throws IOException {
		this(8080);
	}

	DomoticzEventReceiver(final int port) throws IOException {
		super(port);
		start(NanoHTTPD.SOCKET_READ_TIMEOUT, true);
	}

	public void setEventListener(final EventReceived listener) {
		this.listener = Optional.ofNullable(listener);
	}

	@Override
	public Response serve(final IHTTPSession session) {
		if (session.getMethod() == Method.GET) {
			final String uri = session.getUri();

			final Matcher m = URIPATTERN.matcher(uri);
			if (m.matches()) {
				// System.out.println("ID=" + m.group(1));
				// System.out.println("think=" + m.group(2));
				if (listener.isPresent()) {
					listener.get().deviceChanged(Integer.valueOf(m.group(1)), m.group(2));
				}
				return newFixedLengthResponse(Status.OK, NanoHTTPD.MIME_PLAINTEXT, "OKIDOKI");
			}
		} else {
			return newFixedLengthResponse(Status.METHOD_NOT_ALLOWED, NanoHTTPD.MIME_PLAINTEXT, "OKIDOKI");
		}
		return super.serve(session);
	}

}
