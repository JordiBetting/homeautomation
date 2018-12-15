package nl.gingerbeard.automation.devices;

import java.util.Optional;

import nl.gingerbeard.automation.devices.TemperatureSensor.TemperatureValue;

public class TemperatureSensor extends Device<TemperatureValue> {

	public static class TemperatureValue {
		public double temperatureCelsius;
	}

	public TemperatureSensor(final int idx, final Optional<Integer> batteryDomoticzId) {
		super(idx, batteryDomoticzId);
	}

	@Override
	public boolean updateState(final String newState) {
		return false;
	}

}
