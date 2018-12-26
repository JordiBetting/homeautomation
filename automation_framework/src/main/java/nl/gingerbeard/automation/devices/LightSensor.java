package nl.gingerbeard.automation.devices;

import nl.gingerbeard.automation.devices.LightSensor.LightMeasurement;
import nl.gingerbeard.automation.state.NextState;

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

	@Override
	public String getDomoticzParam() {
		throw new UnsupportedOperationException("Can't set light sensor value");
	}

	@Override
	public String getDomoticzSwitchCmd(final NextState<LightMeasurement> nextState) {
		throw new UnsupportedOperationException("Can't set light sensor value");
	}

}
