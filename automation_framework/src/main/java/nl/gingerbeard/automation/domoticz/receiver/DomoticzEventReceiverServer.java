package nl.gingerbeard.automation.domoticz.receiver;

import java.io.IOException;
import java.util.Optional;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import nl.gingerbeard.automation.domoticz.configuration.DomoticzConfiguration;
import nl.gingerbeard.automation.domoticz.receiver.ResponseParameters.ResponseAlarmParameters;
import nl.gingerbeard.automation.domoticz.receiver.ResponseParameters.ResponseDeviceParameters;
import nl.gingerbeard.automation.domoticz.receiver.ResponseParameters.ResponseParametersType;
import nl.gingerbeard.automation.domoticz.receiver.ResponseParameters.ResponseTimeParameters;
import nl.gingerbeard.automation.logging.ILogger;

public final class DomoticzEventReceiverServer extends NanoHTTPD implements IDomoticzEventReceiver {

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

		/**
		 * Update to the current time
		 *
		 * @param curtime
		 *            The current time in minutes since start of day
		 * @param sunrise
		 *            The sunrise time in minutes since start of day
		 * @param sunset
		 *            The sunset time in minutes since start of day
		 * @return True if processing was successful. False otherwise.
		 */
		public boolean timeChanged(int curtime, int sunrise, int sunset);

		/**
		 * Update the alarm state
		 *
		 * @param alarmState
		 *            the new state
		 * @return True if processing was successful. False otherwise.
		 */
		public boolean alarmChanged(String alarmState);
	}

	private Optional<EventReceived> listener = Optional.empty();
	private final ILogger log;

	public DomoticzEventReceiverServer(final DomoticzConfiguration config, final ILogger log) throws IOException {
		super(config.getListenPort());
		this.log = log;
		start(NanoHTTPD.SOCKET_READ_TIMEOUT, true);
		config.updateListenPort(getListeningPort());
	}

	@Override
	public void setEventListener(final EventReceived listener) {
		this.listener = Optional.ofNullable(listener);
	}

	@Override
	public Response serve(final IHTTPSession session) {
		Response response;
		// log.debug(session.getMethod() + " " + session.getUri() + " from " + session.getRemoteIpAddress());
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
			final Optional<Response> listenerResponse = triggerListener(responseParams.get());
			response = listenerResponse.orElse(newFixedLengthResponse(Status.OK, NanoHTTPD.MIME_PLAINTEXT, "OKIDOKI"));
		} else {
			// log.warning("Returning 404 after unrecognized URL: " + uri);
			response = newFixedLengthResponse(Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "URL not in supported format");
		}
		return response;
	}

	private Optional<Response> triggerListener(final ResponseParameters responseParams) {
		if (responseParams.getType() == ResponseParametersType.DEVICE) {
			return triggerDeviceListener(responseParams.getDeviceParameters().get());
		} else if (responseParams.getType() == ResponseParametersType.TIME) {
			return triggerTimeListener(responseParams.getTimeParameters().get());
		}
		return triggerAlarmListener(responseParams.getAlarmParametres().get());
	}

	private Optional<Response> triggerAlarmListener(final ResponseAlarmParameters alarm) {
		Optional<Response> response = Optional.empty();

		if (listener.isPresent()) {
			final boolean result = listener.get().alarmChanged(alarm.getAlarmState());

			if (result) {
				log.debug("Alarm state updated to " + alarm.getAlarmState());
			} else {
				log.error("Could not process alarm request: " + alarm);
				response = Optional.of(newFixedLengthResponse(Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "Could not process request."));
			}
		}

		return response;
	}

	private Optional<Response> triggerTimeListener(final ResponseTimeParameters time) {
		Optional<Response> response = Optional.empty();

		if (listener.isPresent()) {
			final int curtime = time.getCurrentTime();
			final int sunrise = time.getSunriseTime();
			final int sunset = time.getSunsetTime();
			final boolean result = listener.get().timeChanged(curtime, sunrise, sunset);
			if (result) {
				log.debug("Updated time to " + time.toString());
			} else {
				log.error("Could not process time request: " + time);
				response = Optional.of(newFixedLengthResponse(Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "Could not process request."));
			}
		}

		return response;
	}

	private Optional<Response> triggerDeviceListener(final ResponseDeviceParameters device) {
		Optional<Response> response = Optional.empty();
		if (listener.isPresent()) {
			try {
				response = triggerDeviceListenerWithLogging(device);
			} catch (final Throwable t) {
				log.exception(t, "Failure in processing request");
				response = Optional.of(newFixedLengthResponse(Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, t.getMessage()));
			}
		}
		return response;
	}

	private Optional<Response> triggerDeviceListenerWithLogging(final ResponseDeviceParameters device) {
		Optional<Response> response = Optional.empty();
		final boolean result = listener.get().deviceChanged(device.getIdx(), device.getState());
		if (!result) {
			response = Optional.of(newFixedLengthResponse(Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "Could not process request."));
		}
		return response;
	}

}
