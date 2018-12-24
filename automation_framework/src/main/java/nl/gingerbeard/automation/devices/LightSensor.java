package nl.gingerbeard.automation.devices;

import nl.gingerbeard.automation.devices.LightSensor.LightMeasurement;

public class LightSensor extends Device<LightMeasurement> {

	public static class LightMeasurement {
		public double lux;
	}

	public LightSensor(final int idx, final int batteryDomoticzId) {
		super(idx, batteryDomoticzId);
	}

	@Override
	public boolean updateState(final String newState) {
		return false;
	}

}
