package nl.gingerbeard.automation.domoticz.receiver;

import java.io.IOException;
import java.util.Optional;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.logging.ILogger;

public final class DomoticzEventReceiver extends NanoHTTPD implements IDomoticzEventReceiver {

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

	private Optional<EventReceived> listener = Optional.empty();
	private final ILogger log;

	public DomoticzEventReceiver(final DomoticzConfiguration config, final ILogger log) throws IOException {
		super(config.getListenPort());
		this.log = log;
		start(NanoHTTPD.SOCKET_READ_TIMEOUT, true);
		config.updateListenPort(getListeningPort());
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
		Response response;
		log.debug(session.getMethod() + " " + session.getUri() + " from " + session.getRemoteIpAddress());
		if (session.getMethod() == Method.GET) {
			response = processGetRequest(session.getUri());
		} else {
			log.warning("Received unsupported method " + session.getMethod().name() + " on " + session.getUri());
			response = newFixedLengthResponse(Status.METHOD_NOT_ALLOWED, NanoHTTPD.MIME_PLAINTEXT, "Only GET is supported");
		}
		return response;
	}

	private Response processGetRequest(final String uri) {
		Response response;

		final Optional<ResponseParameters> responseParams = UrlPatternParser.parseParameters(uri);
		if (responseParams.isPresent()) {
			final Response defaultResponse = newFixedLengthResponse(Status.OK, NanoHTTPD.MIME_PLAINTEXT, "OKIDOKI");
			final Optional<Response> listenerResponse = triggerListener(responseParams.get());
			response = listenerResponse.orElse(defaultResponse);
		} else {
			log.warning("Returning 404 after unrecognized URL: " + uri);
			response = newFixedLengthResponse(Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "URL not in supported format");
		}
		return response;
	}

	private Optional<Response> triggerListener(final ResponseParameters responseParams) {
		Optional<Response> response = Optional.empty();

		if (listener.isPresent()) {
			try {
				final boolean result = listener.get().deviceChanged(responseParams.getIdx(), responseParams.getState());
				if (result) {
					log.debug("Success");
				} else {
					log.error("Could not process request idx=" + responseParams.getIdx() + ", state=" + responseParams.getState());
					response = Optional.of(newFixedLengthResponse(Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "Could not process request."));
				}
			} catch (final Throwable t) {
				log.exception(t, "Failure in processing request");
				response = Optional.of(newFixedLengthResponse(Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, t.getMessage()));
			}
		}
		return response;
	}

}
