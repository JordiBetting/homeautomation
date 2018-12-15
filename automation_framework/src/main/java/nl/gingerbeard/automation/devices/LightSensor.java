package nl.gingerbeard.automation.devices;

import java.util.Optional;

import nl.gingerbeard.automation.devices.LightSensor.LightMeasurement;

public class LightSensor extends Device<LightMeasurement> {

	public static class LightMeasurement {
		public double lux;
	}

	public LightSensor(final int idx, final Optional<Integer> batteryDomoticzId) {
		super(idx, batteryDomoticzId);
	}

	@Override
	public boolean updateState(final String newState) {
		return false;
	}

}
