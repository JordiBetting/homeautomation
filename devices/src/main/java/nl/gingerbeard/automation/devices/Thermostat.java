package nl.gingerbeard.automation.devices;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.ThermostatState;
import nl.gingerbeard.automation.state.ThermostatState.ThermostatMode;

public final class Thermostat extends CompositeDevice<ThermostatState> {

	private ThermostatModeDevice modeDevice;

	private List<ThermostatSetpointDevice> setpointDevices = Lists.newArrayList();

	public Thermostat(final int idxMode, final int ... idxSetpoint) {
		super(createDevices(idxMode, idxSetpoint));

		setState(new ThermostatState());

		for (final Device<?> device : getDevices()) {
			@SuppressWarnings("unchecked")
			final Subdevice<Thermostat, ?> sub = (Subdevice<Thermostat, ?>) device;
			sub.setParent(this);
			if (sub instanceof ThermostatModeDevice) {
				modeDevice = (ThermostatModeDevice) sub;
			} else {
				setpointDevices.add((ThermostatSetpointDevice) sub);
			}
		}
	}

	private static Set<Device<?>> createDevices(int idxMode, int ... idxSetpointDevices) {
		Preconditions.checkArgument(idxSetpointDevices != null, "idxSetpointDevices should not be null");
		Preconditions.checkArgument(idxSetpointDevices.length > 0, "idxSetPointDevices array should not be empty");
		
		Set<Device<?>> devices = new LinkedHashSet<>(); //for maintaining order of insertion
		
		devices.add(new ThermostatModeDevice(idxMode));
		
		for (int idxSetpoint : idxSetpointDevices) {
			devices.add(new ThermostatSetpointDevice(idxSetpoint));
		}
		return devices;
	}

	public void modeUpdated() {
		final ThermostatMode mode = modeDevice.getState();
		getState().setMode(mode);
	}

	public void setpointUpdated() {
		getState().setTemperature(setpointDevices.get(0).getState());
	}

	public List<NextState<?>> createNextState(final ThermostatState thermostatState) {
		final List<NextState<?>> nextStates = new ArrayList<>();

		nextStates.add(new NextState<>(modeDevice, thermostatState.getMode()));
		thermostatState.getSetPoint().ifPresent(//
				(setpoint) -> { 
					setpointDevices.stream().forEach((device) -> nextStates.add(new NextState<>(device, setpoint)));
				});

		return nextStates;
	}

	public ThermostatModeDevice getModeDevice() {
		return modeDevice;
	}

	public List<ThermostatSetpointDevice> getSetpointDevice() {
		return setpointDevices;
	}
}
