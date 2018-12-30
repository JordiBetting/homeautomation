package nl.gingerbeard.automation.devices;

import com.google.common.collect.Sets;

import nl.gingerbeard.automation.state.Temperature;
import nl.gingerbeard.automation.state.ThermostatState;
import nl.gingerbeard.automation.state.ThermostatState.ThermostatMode;

public class Thermostat extends CompositeDevice<ThermostatState> {

	public abstract static class ThermostatSubdevice<T> extends Device<T> {
		protected Thermostat parent;

		public ThermostatSubdevice(final int idx) {
			super(idx);
		}

		public void setParent(final Thermostat parent) {
			this.parent = parent;
		}
	}

	public static class ThermostatSetpointDevice extends ThermostatSubdevice<Temperature> {

		public ThermostatSetpointDevice(final int idx) {
			super(idx);
		}

		@Override
		public boolean updateState(final String newState) {
			parent.setpointUpdated();
			// TODO
			return false;
		}

	}

	public static class ThermostatModeDevice extends ThermostatSubdevice<ThermostatMode> {

		public ThermostatModeDevice(final int idx) {
			super(idx);
		}

		@Override
		public boolean updateState(final String newState) {
			parent.modeUpdated();
			return false;
		}

	}

	public Thermostat(final int idxSetpoint, final int idxMode) {
		super(Sets.newHashSet(new ThermostatSetpointDevice(idxSetpoint), new ThermostatModeDevice(idxMode)));

		for (final Device<?> device : getDevices()) {
			final ThermostatSubdevice<?> sub = (ThermostatSubdevice<?>) device;
			sub.setParent(this);
		}
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
