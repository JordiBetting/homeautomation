package nl.gingerbeard.automation.devices;

import nl.gingerbeard.automation.devices.TemperatureSensor.TemperatureValue;

public class TemperatureSensor extends Device<TemperatureValue> {

	public static class TemperatureValue {
		public double temperatureCelsius;
	}

	public TemperatureSensor(final int idx) {
		super(idx);
	}

	@Override
	public boolean updateState(final String newState) {
		return false;
	}
}
