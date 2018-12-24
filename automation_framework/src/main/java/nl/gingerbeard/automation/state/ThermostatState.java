package nl.gingerbeard.automation.state;

public final class ThermostatState {
	public static enum ThermostatMode {
		FULL_HEAT, //
		SETPOINT, //
		OFF, //
		;
	}

	private ThermostatMode mode;
	private Temperature setPoint;
}
