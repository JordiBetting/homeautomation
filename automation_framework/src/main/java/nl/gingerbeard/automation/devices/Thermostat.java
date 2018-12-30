package nl.gingerbeard.automation.devices;

import com.google.common.collect.Sets;

import nl.gingerbeard.automation.state.Temperature;
import nl.gingerbeard.automation.state.ThermostatState;
import nl.gingerbeard.automation.state.ThermostatState.ThermostatMode;

public class Thermostat extends CompositeDevice<ThermostatState> {

	public static class ThermostatSetpointDevice extends Device<Temperature> {

		private final Thermostat parent;

		public ThermostatSetpointDevice(final int idx, final Thermostat parent) {
			super(idx);
			this.parent = parent;
		}

		@Override
		public boolean updateState(final String newState) {
			parent.setpointUpdated();
			// TODO
			return false;
		}
	}

	public static class ThermostatModeDevice extends Device<ThermostatMode> {

		private final Thermostat parent;

		public ThermostatModeDevice(final int idx, final Thermostat parent) {
			super(idx);
			this.parent = parent;
		}

		@Override
		public boolean updateState(final String newState) {
			parent.modeUpdated();
			return false;
		}

	}

	public Thermostat(final int idxSetpoint, final int idxMode) {
		super(Sets.newHashSet());
	}

	public void modeUpdated() {
		// TODO Auto-generated method stub

	}

	public void setpointUpdated() {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean updateState(final String newState) {
		// TODO Auto-generated method stub
		return false;
	}

}
