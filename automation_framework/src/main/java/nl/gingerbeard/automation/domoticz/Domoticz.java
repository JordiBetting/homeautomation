package nl.gingerbeard.automation.domoticz;

import java.io.IOException;
import java.util.Optional;

import nl.gingerbeard.automation.deviceregistry.IDeviceRegistry;
import nl.gingerbeard.automation.domoticz.clients.TimeOfDayClient;
import nl.gingerbeard.automation.domoticz.receiver.DomoticzEventReceiverServer.EventReceived;
import nl.gingerbeard.automation.logging.ILogger;
import nl.gingerbeard.automation.state.TimeOfDayValues;

// high - level access
final class Domoticz implements EventReceived {

	private final ILogger logger;
	private final Optional<DomoticzThreadHandler> threadHandler;
	private final TimeOfDayClient timeOfDayClient;

	// for testing
	Domoticz(final IDeviceRegistry registry) {
		super();
		logger = (t, level, message) -> {
		};
		threadHandler = Optional.empty();
		timeOfDayClient = null;
	}

	public Domoticz(final ILogger logger, final DomoticzThreadHandler threadHandler, final TimeOfDayClient timeOfDayClient) {
		this.logger = logger;
		this.threadHandler = Optional.of(threadHandler);
		this.timeOfDayClient = timeOfDayClient;
	}

	@Override
	public boolean deviceChanged(final int idx, final String newState) {
		boolean success = false;
		if (threadHandler.isPresent() && threadHandler.get().handlesDevice(idx)) {
			try {
				threadHandler.get().deviceChanged(idx, newState);
				success = true;
			} catch (final InterruptedException e) {
				logger.warning(e, "Interrupted while updating device");
			}
		} else {
			logger.debug("Received update for unknown device with idx: " + idx);
		}
		return success;
	}

	@Override
	public boolean timeChanged(final int curtime, final int sunrise, final int sunset) {
		boolean success = false;
		if (threadHandler.isPresent() && threadHandler.get().handlesTime()) {
			try {
				final TimeOfDayValues timeOfDayValues = timeOfDayClient.createTimeOfDayValues(curtime, sunrise, sunset);
				threadHandler.get().timeChanged(timeOfDayValues);
				success = true;
			} catch (final IOException | InterruptedException e) {
				logger.warning(e, "Failed retrieving time of day values");
			}
		}
		return success;
	}

	@Override
	public boolean alarmChanged(final String alarmState) {
		boolean success = false;
		if (threadHandler.isPresent() && threadHandler.get().handlesAlarm()) {
			try {
				threadHandler.get().alarmChanged(alarmState);
				success = true;
			} catch (final InterruptedException e) {
				logger.warning(e, "Interrupted while changing alarm state");
			}
		}
		return success;
	}

}
