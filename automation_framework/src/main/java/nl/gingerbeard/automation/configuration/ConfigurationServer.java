package nl.gingerbeard.automation.configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;

public final class ConfigurationServer extends NanoHTTPD {

	private final IConfigurationProvider provider;
	private final List<SupportedRequest> requests = new ArrayList<>();

	public ConfigurationServer(final ConfigurationServerSettings settings, final IConfigurationProvider provider) throws IOException {
		super(settings.getListenPort());
		this.provider = provider;
		start(NanoHTTPD.SOCKET_READ_TIMEOUT, true);
		settings.setListenPort(getListeningPort());

		addRequest("^/room/?$", Method.GET, this::processGetAllRooms);
		addRequest("^/room/([0-9a-zA-Z_]+)/enable/?$", Method.POST, this::processEnableRoom);
		addRequest("^/room/([0-9a-zA-Z_]+)/disable/?$", Method.POST, this::processDisableRoom);
	}

	private void addRequest(final String pattern, final Method method, final Function<Request, Response> executor) {
		requests.add(new SupportedRequest(pattern, method, executor));
	}

	private Response processGetAllRooms(final Request request) {
		final String response = provider.getRooms().stream().collect(Collectors.joining(","));
		return newFixedLengthResponse(response);
	}

	private Response processEnableRoom(final Request request) {
		provider.enable(request.getUriParameters()[0]);
		return success();
	}

	private Response processDisableRoom(final Request request) {
		provider.disable(request.getUriParameters()[0]);
		return success();
	}

	@Override
	public Response serve(final IHTTPSession session) {
		boolean uriMatch = false;
		for (final SupportedRequest request : requests) {
			if (request.matchesUri(session)) {
				uriMatch = true;
				if (request.matchesMethod(session)) { //
					return request.execute(session);
				}
			}
		}

		// when reaching here, no request was executed
		return uriMatch ? unsupportedMethod(session) : super.serve(session);
	}

	private Response success() {
		return newFixedLengthResponse("Success");
	}

	private Response unsupportedMethod(final IHTTPSession session) {
		return newFixedLengthResponse(Status.METHOD_NOT_ALLOWED, NanoHTTPD.MIME_PLAINTEXT, "Unsupported method " + session.getMethod() + " for " + session.getUri());
	}

}
