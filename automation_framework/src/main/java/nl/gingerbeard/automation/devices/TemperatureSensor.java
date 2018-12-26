package nl.gingerbeard.automation.devices;

import nl.gingerbeard.automation.devices.TemperatureSensor.TemperatureValue;
import nl.gingerbeard.automation.state.NextState;

public class TemperatureSensor extends Device<TemperatureValue> {

	public static class TemperatureValue {
		public double temperatureCelsius;
	}

	public TemperatureSensor(final int idx, final int batteryDomoticzId) {
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
	public String getDomoticzSwitchCmd(final NextState<TemperatureValue> nextState) {
		throw new UnsupportedOperationException("Can't set light sensor value");
	}
}
