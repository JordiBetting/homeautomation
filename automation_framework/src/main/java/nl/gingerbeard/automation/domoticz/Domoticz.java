package nl.gingerbeard.automation.domoticz;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import nl.gingerbeard.automation.devices.Device;
import nl.gingerbeard.automation.domoticz.DomoticzEventReceiver.EventReceived;
import nl.gingerbeard.automation.service.annotation.Activate;
import nl.gingerbeard.automation.service.annotation.Provides;
import nl.gingerbeard.automation.service.annotation.Requires;

// high - level access
public class Domoticz implements EventReceived, IDomoticz {

	@Requires
	public Optional<IDomoticzDeviceStatusChanged> listener;

	@Requires
	public IDomoticzEventReceiver domoticzReceiver;

	@Provides
	public IDomoticz domoticzInstance;

	@Activate
	public void registerReceiver() {
		domoticzInstance = this;
		domoticzReceiver.setEventListener(this);
	}

	public Domoticz() {
		super();
		listener = Optional.empty();
	}

	// for testing
	Domoticz(final IDomoticzEventReceiver domoticzReceiver) {
		this.domoticzReceiver = domoticzReceiver;
		listener = Optional.empty();
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
			// TODO: How to deal with battery of devices. How to even link it to the original device?
			// if (device.getBatteryDomoticzId().isPresent()) {
			// devices.put(device.getBatteryDomoticzId().get(), device);
			// }
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
