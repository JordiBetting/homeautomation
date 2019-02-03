package nl.gingerbeard.automation.declarative;

import nl.gingerbeard.automation.state.NextState;

public interface IDeviceUpdate {

	void updateDevice(NextState<?> nextState);

}
