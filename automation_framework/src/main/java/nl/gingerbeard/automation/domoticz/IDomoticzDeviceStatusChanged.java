package nl.gingerbeard.automation.domoticz;

import nl.gingerbeard.automation.devices.Device;

public interface IDomoticzDeviceStatusChanged {
	void statusChanged(Device<?> device);
}
