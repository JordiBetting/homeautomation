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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (mode == null ? 0 : mode.hashCode());
		result = prime * result + (setPoint == null ? 0 : setPoint.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ThermostatState other = (ThermostatState) obj;
		if (mode != other.mode) {
			return false;
		}
		if (setPoint == null) {
			if (other.setPoint != null) {
				return false;
			}
		} else if (!setPoint.equals(other.setPoint)) {
			return false;
		}
		return true;
	}

}
