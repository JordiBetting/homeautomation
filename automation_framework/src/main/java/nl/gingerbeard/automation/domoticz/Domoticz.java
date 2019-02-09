package nl.gingerbeard.automation.domoticz;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import nl.gingerbeard.automation.devices.Device;
import nl.gingerbeard.automation.domoticz.receiver.DomoticzEventReceiverServer.EventReceived;
import nl.gingerbeard.automation.logging.ILogger;

// high - level access
final class Domoticz implements EventReceived, IDomoticz {

	private final Optional<IDomoticzDeviceStatusChanged> listener;
	private final ILogger logger;

	public Domoticz() {
		super();
		logger = (t, level, message) -> {
		};
		listener = Optional.empty();
	}

	// for testing
	Domoticz(final Optional<IDomoticzDeviceStatusChanged> listener, final ILogger logger) {
		this.listener = listener;
		this.logger = logger;

	}

	// move to subclass for separtaion
	// public Object domoticzUrl;
	// no auth as it is localhost, allowed from domoticz authmanager

	private final Map<Integer, Device<?>> devices = new HashMap<>();

	@Override
	public boolean addDevice(final Device<?> device) {
		final int idx = device.getIdx();
		if (!devices.containsKey(idx)) {
			devices.put(idx, device);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean deviceChanged(final int idx, final String newState) {
		final Optional<Device<?>> device = Optional.ofNullable(devices.get(idx));
		if (device.isPresent()) {
			logger.debug("Device with idx " + idx + " changed state into: " + newState);
			final Device<?> changedDevice = device.get();
			final boolean result = changedDevice.updateState(newState);
			if (result && listener.isPresent()) {
				listener.get().statusChanged(changedDevice);
			}
			return result;
		}
		logger.debug("Received update for unknown device with idx: " + idx);
		return false;
	}

	@Override
	public boolean timeChanged(final int curTime, final int sunRise, final int sunSet) {
		// TODO Auto-generated method stub
		return false;
	}
}
