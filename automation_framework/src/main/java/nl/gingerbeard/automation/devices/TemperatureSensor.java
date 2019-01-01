package nl.gingerbeard.automation.devices;

import nl.gingerbeard.automation.state.Temperature;
import nl.gingerbeard.automation.state.Temperature.Unit;

public class TemperatureSensor extends Device<Temperature> {

	public TemperatureSensor(final int idx) {
		super(idx);
	}

	@Override
	public boolean updateState(final String newState) {
		try {
			final double newValue = Double.parseDouble(newState);
			// TODO: Use domoticz settings to understand what temperature unit is used.
			setState(new Temperature(newValue, Unit.CELSIUS));
			return true;
		} catch (final NumberFormatException e) {
			return false;
		}
	}
}
