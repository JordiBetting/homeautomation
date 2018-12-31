package nl.gingerbeard.automation.devices;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.Sets;

import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.Temperature;
import nl.gingerbeard.automation.state.ThermostatState;
import nl.gingerbeard.automation.state.ThermostatState.ThermostatMode;

public class Thermostat extends CompositeDevice<ThermostatState> {

	public abstract static class ThermostatSubdevice<T> extends Device<T> {
		protected Optional<Thermostat> parent = Optional.empty();

		public ThermostatSubdevice(final int idx) {
			super(idx);
		}

		public void setParent(final Thermostat parent) {
			this.parent = Optional.of(parent);
		}
	}

	public static class ThermostatSetpointDevice extends ThermostatSubdevice<Temperature> {

		public ThermostatSetpointDevice(final int idx) {
			super(idx);
		}

		@Override
		public boolean updateState(final String newState) {
			parent.ifPresent((parent) -> parent.setpointUpdated());
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
			parent.ifPresent((parent) -> parent.modeUpdated());
			// TODO
			return false;
		}

	}

	private ThermostatModeDevice modeDevice;
	private ThermostatSetpointDevice setpointDevice;

	public Thermostat(final int idxSetpoint, final int idxMode) {
		super(Sets.newHashSet(new ThermostatSetpointDevice(idxSetpoint), new ThermostatModeDevice(idxMode)));

		for (final Device<?> device : getDevices()) {
			final ThermostatSubdevice<?> sub = (ThermostatSubdevice<?>) device;
			sub.setParent(this);
			if (device instanceof ThermostatModeDevice) {
				modeDevice = (ThermostatModeDevice) device;
			} else {
				setpointDevice = (ThermostatSetpointDevice) device;
			}
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

	public List<NextState<?>> createNextState(final ThermostatState thermostatState) {
		final List<NextState<?>> nextStates = new ArrayList<>();

		nextStates.add(new NextState<>(modeDevice, thermostatState.getMode()));
		thermostatState.getSetPoint().ifPresent(//
				(setpoint) -> nextStates.add(new NextState<>(setpointDevice, setpoint)));

		return nextStates;
	}

}
