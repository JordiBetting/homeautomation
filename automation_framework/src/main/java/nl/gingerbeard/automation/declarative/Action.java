package nl.gingerbeard.automation.declarative;

import nl.gingerbeard.automation.devices.Device;
import nl.gingerbeard.automation.state.NextState;

final class Action<StateType> {

	private final IDeviceUpdate output;
	private final NextState<StateType> nextState;

	public Action(final Device<StateType> device, final StateType newState, final IDeviceUpdate output) {
		nextState = new NextState<>(device, newState);
		this.output = output;
	}

	public Device<StateType> getDevice() {
		return nextState.getDevice();
	}

	public StateType getNewState() {
		return nextState.get();
	}

	public void execute() {
		output.updateDevice(nextState);
	}

}
