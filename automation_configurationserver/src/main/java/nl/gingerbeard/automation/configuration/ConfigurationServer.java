package nl.gingerbeard.automation.configuration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

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
		addRequest("^/room/([0-9a-zA-Z_]+)/isenabled/?$", Method.GET, this::processRoomIsEnabled);
		addRequest("^/static/([0-9a-zA-Z_/.]+)$", Method.GET, this::processStatic);
	}

	private void addRequest(final String pattern, final Method method, final Function<Request, Response> executor) {
		requests.add(new SupportedRequest(pattern, method, executor));
	}

	private Response processGetAllRooms(final Request request) {
		final String csv = createRoomsCsv();
		return newFixedLengthResponse(csv);
	}

	private String createRoomsCsv() {
		final StringBuilder response = new StringBuilder();
		provider.getRooms().stream().forEach((room) -> //
		response.append(room)//
				.append(",")//
				.append(provider.isEnabled(room))//
				.append("\n")//
		);
		final String csv = response.toString();
		return csv;
	}

	private Response processEnableRoom(final Request request) {
		provider.enable(request.getUriParameters().get(0));
		return success();
	}

	private Response processDisableRoom(final Request request) {
		provider.disable(request.getUriParameters().get(0));
		return success();
	}

	private Response processRoomIsEnabled(final Request request) {
		final boolean enabled = provider.isEnabled(request.getUriParameters().get(0));
		return newFixedLengthResponse("" + enabled);
	}

	private Response processStatic(final Request request) {
		try {
			final String fileContent = readFile(request);
			return newFixedLengthResponse(fileContent);
		} catch (final FileNotFoundException e) {
			return newFixedLengthResponse(Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "File not found");
		} catch (final IOException e) {
			return newFixedLengthResponse(Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, "Failed retrieving file: " + e.getMessage());
		}
	}

	String readFile(final Request request) throws IOException {
		final Optional<URL> fileLocation = locateFile(request);
		if (!fileLocation.isPresent()) {
			throw new FileNotFoundException(request.getUriParameters().get(0));
		}
		return readFile(fileLocation.get());
	}

	// test interface
	private static boolean throwIOException = false;

	static void setThrowIOExceptionOnReadFile(final boolean enabled) {
		throwIOException = enabled;
	}

	private String readFile(final URL resource) throws IOException {
		if (throwIOException) {
			throw new IOException("Test exception");
		}
		final File file = new File(resource.getFile());
		final String content = new String(Files.readAllBytes(file.toPath()), Charset.defaultCharset());
		return content;
	}

	private Optional<URL> locateFile(final Request request) {
		final String path = "/web/" + request.getUriParameters().get(0);
		final URL resource = ClassLoader.getSystemClassLoader().getResource(path);
		return Optional.ofNullable(resource);
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
