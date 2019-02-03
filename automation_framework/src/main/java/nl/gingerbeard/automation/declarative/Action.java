package nl.gingerbeard.automation.declarative;

import nl.gingerbeard.automation.devices.Device;
import nl.gingerbeard.automation.state.NextState;

final class Action<StateType> {

	private final NextState<StateType> nextState;
	private IDeviceUpdate output;

	Action(final Device<StateType> device, final StateType newState) {
		nextState = new NextState<>(device, newState);
	}

	void setOutput(final IDeviceUpdate output) {
		this.output = output;
	}

	public void execute() {
		output.updateDevice(nextState);
	}

}
