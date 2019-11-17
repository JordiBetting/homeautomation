package nl.gingerbeard.automation.domoticz.api;

import nl.gingerbeard.automation.devices.StateDevice;

public interface IDomoticzDeviceStatusChanged {
	void statusChanged(StateDevice<?> device);
}
