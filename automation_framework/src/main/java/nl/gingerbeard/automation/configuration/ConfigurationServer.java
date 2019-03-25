package nl.gingerbeard.automation.configuration;

import java.io.IOException;
import java.util.stream.Collectors;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;

public final class ConfigurationServer extends NanoHTTPD {

	private final IConfigurationProvider provider;

	public ConfigurationServer(final ConfigurationServerSettings settings, final IConfigurationProvider provider) throws IOException {
		super(settings.getListenPort());
		this.provider = provider;
		start(NanoHTTPD.SOCKET_READ_TIMEOUT, true);
		settings.setListenPort(getListeningPort());
	}

	@Override
	public Response serve(final IHTTPSession session) {
		if (session.getUri().equals("/rooms")) {
			if (session.getMethod() != Method.GET) { // This combination of method and urls can be smarter!
				return unsupportedMethod(session);
			}
			final String response = provider.getRooms().stream().collect(Collectors.joining(","));
			return newFixedLengthResponse(response);
		} else if (session.getUri().startsWith("/rooms/")) {// TODO; regex
			if (session.getMethod() != Method.POST) {
				return unsupportedMethod(session);
			} else if (session.getUri().endsWith("/disable")) {
				provider.disable(getRoom(session));
			} else if (session.getUri().endsWith("/enable")) {
				provider.enable(getRoom(session));
			} else {
				return super.serve(session); // 404
			}
			return success();

		} else {
			return super.serve(session); // 404
		}
	}

	private String getRoom(final IHTTPSession session) {
		final String room = session.getUri().replaceAll("/rooms/", "").replaceAll("/disable", "").replaceAll("/enable", "");// TODO: regex
		return room;
	}

	private Response success() {
		return newFixedLengthResponse("Success");
	}

	private Response unsupportedMethod(final IHTTPSession session) {
		return newFixedLengthResponse(Status.METHOD_NOT_ALLOWED, NanoHTTPD.MIME_PLAINTEXT, "Unsupported method " + session.getMethod() + " for " + session.getUri());
	}

}
