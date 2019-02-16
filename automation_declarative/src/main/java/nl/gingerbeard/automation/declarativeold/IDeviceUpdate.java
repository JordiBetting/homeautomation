package nl.gingerbeard.automation.declarativeold;

import nl.gingerbeard.automation.state.NextState;

public interface IDeviceUpdate {

	void updateDevice(NextState<?> nextState);

}
