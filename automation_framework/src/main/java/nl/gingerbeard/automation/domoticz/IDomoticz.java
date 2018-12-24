package nl.gingerbeard.automation.domoticz;

import nl.gingerbeard.automation.devices.Device;

public interface IDomoticz {
	public boolean addDevice(final Device<?> device);
}
