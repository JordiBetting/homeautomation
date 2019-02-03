package nl.gingerbeard.automation.declarative;

import java.util.Optional;

import nl.gingerbeard.automation.devices.Device;
import nl.gingerbeard.automation.state.NextState;

final class Action<StateType> {

	private final NextState<StateType> nextState;
	private Optional<IDeviceUpdate> output = Optional.empty();

	Action(final Device<StateType> device, final StateType newState) {
		nextState = new NextState<>(device, newState);
	}

	void setOutput(final IDeviceUpdate output) {
		this.output = Optional.of(output);
	}

	public void execute() {
		output.ifPresent((output) -> output.updateDevice(nextState));
	}

}
