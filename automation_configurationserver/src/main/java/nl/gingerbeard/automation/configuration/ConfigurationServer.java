package nl.gingerbeard.automation.configuration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
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
		addRequest("^/static/([0-9a-zA-Z_/.]+)$", Method.GET, this::processStatic);
	}

	private void addRequest(final String pattern, final Method method, final Function<Request, Response> executor) {
		requests.add(new SupportedRequest(pattern, method, executor));
	}

	private Response processGetAllRooms(final Request request) {
		final String response = provider.getRooms().stream().collect(Collectors.joining(","));
		return newFixedLengthResponse(response);
	}

	private Response processEnableRoom(final Request request) {
		provider.enable(request.getUriParameters().get(0));
		return success();
	}

	private Response processDisableRoom(final Request request) {
		provider.disable(request.getUriParameters().get(0));
		return success();
	}

	private Response processStatic(final Request request) {
		try {
			final URL resource = locateFile(request);
			if (resource != null) {
				return newFixedLengthResponse(readFile(resource));
			} else {
				return newFixedLengthResponse(Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "File not found");
			}
		} catch (final IOException e) {
			return newFixedLengthResponse(Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, "Failed retrieving file: " + e.getMessage());
		}
	}

	private String readFile(final URL resource) throws IOException {
		final File file = new File(resource.getFile());
		final String content = new String(Files.readAllBytes(file.toPath()), Charset.defaultCharset());
		return content;
	}

	private URL locateFile(final Request request) {
		final String path = "web/" + request.getUriParameters().get(0);
		final URL resource = ClassLoader.getSystemClassLoader().getResource(path);
		return resource;
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
