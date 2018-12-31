package nl.gingerbeard.automation.devices;

import nl.gingerbeard.automation.state.Temperature;

public class Heating extends Device<Temperature> {

	public Heating(final int idx) {
		super(idx);
	}

	@Override
	public boolean updateState(final String newState) {
		// TODO Will it receive any events from domoticz since it is not seen as a default device?
		return false;
	}

}
