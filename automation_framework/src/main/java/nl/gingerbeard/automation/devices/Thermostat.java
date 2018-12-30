package nl.gingerbeard.automation.devices;

import nl.gingerbeard.automation.state.ThermostatState;

public class Thermostat extends Device<ThermostatState> {

	public Thermostat(final int idx) {
		super(idx);
	}

	@Override
	public boolean updateState(final String newState) {
		// TODO Auto-generated method stub
		return false;
	}

}
