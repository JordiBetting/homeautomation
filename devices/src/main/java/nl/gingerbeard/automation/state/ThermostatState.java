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

	public ThermostatState() {
		mode = ThermostatMode.OFF;
		setPoint = Temperature.celcius(20);
	}

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

	public void setMode(final ThermostatMode newMode) {
		mode = newMode;
	}

	@Override
	public String toString() {
		return "ThermostatState [mode=" + mode + ", setPoint=" + getSetPoint() + "]";
	}

}
