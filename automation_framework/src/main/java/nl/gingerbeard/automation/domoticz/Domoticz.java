package nl.gingerbeard.automation.domoticz;

import java.io.IOException;

import nl.gingerbeard.automation.domoticz.clients.TimeOfDayClient;
import nl.gingerbeard.automation.domoticz.receiver.DomoticzEventReceiverServer.EventReceived;
import nl.gingerbeard.automation.logging.ILogger;
import nl.gingerbeard.automation.state.TimeOfDayValues;

// high - level access
final class Domoticz implements EventReceived {

	private final ILogger logger;
	private final DomoticzThreadHandler threadHandler;
	private final TimeOfDayClient timeOfDayClient;

	public Domoticz(final ILogger logger, final DomoticzThreadHandler threadHandler, final TimeOfDayClient timeOfDayClient) {
		this.logger = logger;
		this.threadHandler = threadHandler;
		this.timeOfDayClient = timeOfDayClient;
	}

	@Override
	public boolean deviceChanged(final int idx, final String newState) {
		boolean success = false;
		if (threadHandler.handlesDevice(idx)) {
			try {
				threadHandler.deviceChanged(idx, newState);
				success = true;
			} catch (final InterruptedException e) {
				logger.warning(e, "Interrupted while updating device");
			}
		} else {
			// logger.debug("Received update for unknown device with idx: " + idx);
		}
		return success;
	}

	@Override
	public boolean timeChanged(final int curtime, final int sunrise, final int sunset) {
		boolean success = false;
		if (threadHandler.handlesTime()) {
			try {
				final TimeOfDayValues timeOfDayValues = timeOfDayClient.createTimeOfDayValues();
				threadHandler.timeChanged(timeOfDayValues);
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
		if (threadHandler.handlesAlarm()) {
			try {
				threadHandler.alarmChanged(alarmState);
				success = true;
			} catch (final InterruptedException e) {
				logger.warning(e, "Interrupted while changing alarm state");
			}
		}
		return success;
	}

}
