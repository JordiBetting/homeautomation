package nl.gingerbeard.automation.state;

import com.google.common.base.Preconditions;

import nl.gingerbeard.automation.devices.Device;

public class NextState<StateType> {

	private final Device<StateType> device;
	private final StateType nextState;

	public NextState(final Device<StateType> device, final StateType nextState) {
		Preconditions.checkArgument(device != null);
		Preconditions.checkArgument(nextState != null);
		this.device = device;
		this.nextState = nextState;
	}

	public StateType get() {
		return nextState;
	}

	public Device<StateType> getDevice() {
		return device;
	}

}
