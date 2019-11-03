package nl.gingerbeard.automation.onkyo;

import nl.gingerbeard.automation.state.NextState;

public interface IOnkyoTransmitter {

	void transmit(NextState<?> newState);

}