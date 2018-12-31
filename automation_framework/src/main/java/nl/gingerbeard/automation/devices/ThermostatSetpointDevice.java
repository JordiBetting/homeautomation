package nl.gingerbeard.automation.devices;

import nl.gingerbeard.automation.state.Temperature;
import nl.gingerbeard.automation.state.Temperature.Unit;

public final class ThermostatSetpointDevice extends Subdevice<Thermostat, Temperature> {

	public ThermostatSetpointDevice(final int idx) {
		super(idx);
		setState(Temperature.celcius(10));
	}

	@Override
	public boolean updateState(final String newState) {
		final double newValue = Double.parseDouble(newState);
		// TODO: Use domoticz settings to understand what temperature unit is used.
		setState(new Temperature(newValue, Unit.CELSIUS));
		parent.ifPresent((parent) -> parent.setpointUpdated());
		return true;
	}

}
