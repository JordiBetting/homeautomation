package nl.gingerbeard.automation.state;

import java.util.Optional;

public final class ThermostatState {
	public static enum ThermostatMode {
		FULL_HEAT, //
		SETPOINT, //
		OFF, //
		;
	}

	private ThermostatMode mode;
	private Temperature setPoint;

	public void setFullHeat() {
		mode = ThermostatMode.FULL_HEAT;
	}

	public void setTemperature(final Temperature temperature) {
		mode = ThermostatMode.SETPOINT;
		setPoint = temperature;
	}

	public void setOff() {
		mode = ThermostatMode.OFF;
	}

	public ThermostatMode getMode() {
		return mode;
	}

	public Optional<Temperature> getSetPoint() {
		if (mode == ThermostatMode.SETPOINT) {
			return Optional.of(setPoint);
		}
		return Optional.empty();
	}

}
