package nl.gingerbeard.automation.domoticz;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import nl.gingerbeard.automation.controlloop.Controlloop;
import nl.gingerbeard.automation.devices.Device;
import nl.gingerbeard.automation.domoticz.DomoticzEventReceiver.EventReceived;

// high - level access
public class Domoticz implements EventReceived {

	// move to subclass for separtaion
	// public Object domoticzUrl;
	// no auth as it is localhost, allowed from domticz authmanager

	private final Map<Integer, Device<?>> devices = new HashMap<>();
	private final Controlloop controlloop;

	public Domoticz(final IDomoticzEventReceiver receiver, final Controlloop controlloop) {
		this.controlloop = controlloop;
		receiver.setEventListener(this);
	}

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
			final boolean result = device.get().updateState(newState);
			if (result) {
				controlloop.triggerDeviceChanged(device.get());
			}
			return result;
		}
		return false;
	}
}
