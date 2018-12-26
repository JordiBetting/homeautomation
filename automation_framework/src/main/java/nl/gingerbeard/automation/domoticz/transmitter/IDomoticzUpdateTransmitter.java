package nl.gingerbeard.automation.domoticz.transmitter;

import java.io.IOException;

import nl.gingerbeard.automation.state.NextState;

public interface IDomoticzUpdateTransmitter {

	<T> void transmitDeviceUpdate(NextState<T> newState) throws IOException;

}
