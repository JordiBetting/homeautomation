package nl.gingerbeard.automation.devices;

import nl.gingerbeard.automation.state.Temperature;

public final class ThermostatSetpointDevice extends Subdevice<Thermostat, Temperature> {

	public ThermostatSetpointDevice(final int idx) {
		super(idx);
	}

	@Override
	public boolean updateState(final String newState) {
		parent.ifPresent((parent) -> parent.setpointUpdated());
		// TODO
		return false;
	}

}
