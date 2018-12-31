package nl.gingerbeard.automation.domoticz;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import nl.gingerbeard.automation.devices.Device;
import nl.gingerbeard.automation.domoticz.receiver.DomoticzEventReceiver.EventReceived;

// high - level access
final class Domoticz implements EventReceived, IDomoticz {

	private final Optional<IDomoticzDeviceStatusChanged> listener;

	public Domoticz() {
		super();
		listener = Optional.empty();
	}

	// for testing
	Domoticz(final Optional<IDomoticzDeviceStatusChanged> listener) {
		this.listener = listener;
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
			final Device<?> changedDevice = device.get();
			final boolean result = changedDevice.updateState(newState);
			if (result && listener.isPresent()) {
				listener.get().statusChanged(changedDevice);
			}
			return result;
		}
		return false;
	}
}
