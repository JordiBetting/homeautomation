package nl.gingerbeard.automation.domoticz.transmitter;

import java.io.IOException;

import nl.gingerbeard.automation.devices.Device;

public interface IDomoticzUpdateTransmitter {

	void transmitDeviceUpdate(Device<?> device) throws IOException;

}
