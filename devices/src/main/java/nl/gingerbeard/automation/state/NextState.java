package nl.gingerbeard.automation.state;

import com.google.common.base.Preconditions;

import nl.gingerbeard.automation.devices.StateDevice;

public final class NextState<StateType> {

	private final StateDevice<StateType> device;
	private final StateType nextState;

	public NextState(final StateDevice<StateType> device, final StateType nextState) {
		Preconditions.checkArgument(device != null);
		Preconditions.checkArgument(nextState != null);
		this.device = device;
		this.nextState = nextState;
	}

	public StateType get() {
		return nextState;
	}

	public StateDevice<StateType> getDevice() {
		return device;
	}

	@Override
	public String toString() {
		return "NextState [device=" + device + ", nextState=" + nextState + "]";
	}
}
