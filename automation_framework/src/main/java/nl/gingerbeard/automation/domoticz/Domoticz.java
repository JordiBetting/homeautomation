package nl.gingerbeard.automation.domoticz;

import java.io.IOException;
import java.util.Locale;
import java.util.Optional;

import nl.gingerbeard.automation.deviceregistry.IDeviceRegistry;
import nl.gingerbeard.automation.devices.Device;
import nl.gingerbeard.automation.domoticz.receiver.DomoticzEventReceiverServer.EventReceived;
import nl.gingerbeard.automation.logging.ILogger;
import nl.gingerbeard.automation.state.AlarmState;
import nl.gingerbeard.automation.state.TimeOfDayValues;

// high - level access
final class Domoticz implements EventReceived {

	private final Optional<IDomoticzDeviceStatusChanged> deviceListener;
	private final Optional<IDomoticzTimeOfDayChanged> timeListener;
	private final Optional<IDomoticzAlarmChanged> alarmListener;
	private final ILogger logger;
	private final IDeviceRegistry deviceRegistry;
	private TimeOfDayClient timeOfDayClient;

	// for testing
	Domoticz(final IDeviceRegistry registry) {
		super();
		logger = (t, level, message) -> {
		};
		deviceListener = Optional.empty();
		timeListener = Optional.empty();
		alarmListener = Optional.empty();
		deviceRegistry = registry;
	}

	public Domoticz(final Optional<IDomoticzDeviceStatusChanged> deviceListener, final Optional<IDomoticzTimeOfDayChanged> timeListener, final Optional<IDomoticzAlarmChanged> alarmListener,
			final ILogger logger, final IDeviceRegistry deviceRegistry, final TimeOfDayClient timeOfDayClient) {
		this.deviceListener = deviceListener;
		this.timeListener = timeListener;
		this.alarmListener = alarmListener;
		this.logger = logger;
		this.deviceRegistry = deviceRegistry;
		this.timeOfDayClient = timeOfDayClient;
	}

	@Override
	public boolean deviceChanged(final int idx, final String newState) {
		final Optional<Device<?>> device = deviceRegistry.updateDevice(idx, newState);
		if (device.isPresent()) {
			logger.debug("Device with idx " + idx + " changed state into: " + newState);
			final Device<?> changedDevice = device.get();
			deviceListener.ifPresent((listener) -> listener.statusChanged(changedDevice));
			return true;
		}
		logger.debug("Received update for unknown device with idx: " + idx);
		return false;
	}

	@Override
	public boolean timeChanged(final int curtime, final int sunrise, final int sunset) {
		try {
			if (timeListener.isPresent()) {
				final TimeOfDayValues timeOfDayValues = timeOfDayClient.createTimeOfDayValues(curtime, sunrise, sunset);
				return timeListener.get().timeChanged(timeOfDayValues);
			}
		} catch (final IOException e) {
			logger.exception(e, "Could not process time");
		}

		return false;
	}

	@Override
	public boolean alarmChanged(final String alarmState) {
		if (alarmListener.isPresent()) {
			final AlarmState alarm = getAlarmStateByString(alarmState);
			return alarmListener.get().alarmChanged(alarm);
		}
		return false;
	}

	private AlarmState getAlarmStateByString(final String alarmStateString) {
		final String ucState = alarmStateString.toUpperCase(Locale.US);
		return AlarmState.valueOf(ucState);
	}
}
