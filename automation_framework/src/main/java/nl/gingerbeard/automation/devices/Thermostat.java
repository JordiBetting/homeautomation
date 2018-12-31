package nl.gingerbeard.automation.devices;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Sets;

import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.ThermostatState;
import nl.gingerbeard.automation.state.ThermostatState.ThermostatMode;

public final class Thermostat extends CompositeDevice<ThermostatState> {

	private ThermostatModeDevice modeDevice;
	private ThermostatSetpointDevice setpointDevice;

	public Thermostat(final int idxSetpoint, final int idxMode) {
		super(Sets.newHashSet(new ThermostatSetpointDevice(idxSetpoint), new ThermostatModeDevice(idxMode)));

		setState(new ThermostatState());

		for (final Device<?> device : getDevices()) {
			@SuppressWarnings("unchecked")
			final Subdevice<Thermostat, ?> sub = (Subdevice<Thermostat, ?>) device;
			sub.setParent(this);
			if (sub instanceof ThermostatModeDevice) {
				modeDevice = (ThermostatModeDevice) sub;
			} else {
				setpointDevice = (ThermostatSetpointDevice) sub;
			}
		}
	}

	public void modeUpdated() {
		final ThermostatMode mode = modeDevice.getState();
		getState().setMode(mode);
	}

	public void setpointUpdated() {
		getState().setTemperature(setpointDevice.getState());
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
