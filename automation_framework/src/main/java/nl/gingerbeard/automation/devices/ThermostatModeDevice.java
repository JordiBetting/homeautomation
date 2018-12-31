package nl.gingerbeard.automation.devices;

import java.util.Locale;

import nl.gingerbeard.automation.state.ThermostatState.ThermostatMode;

public final class ThermostatModeDevice extends Subdevice<Thermostat, ThermostatMode> {

	public ThermostatModeDevice(final int idx) {
		super(idx);
	}

	@Override
	public boolean updateState(final String newState) {
		final ThermostatMode thermostatMode = ThermostatMode.valueOf(newState.toUpperCase(Locale.US));
		setState(thermostatMode);
		parent.ifPresent((parent) -> parent.modeUpdated());
		return true;
	}

}