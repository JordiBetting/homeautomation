package nl.gingerbeard.automation.declarative;

import java.util.Optional;

import nl.gingerbeard.automation.devices.Device;

public final class DeclarativeRuleBuilder {
	private Optional<Action<?>> action = Optional.empty();
	private final Object expectedState;
	private final IDeviceUpdate output;

	DeclarativeRuleBuilder(final IDeviceUpdate output, final Object expectedState) {
		this.output = output;
		this.expectedState = expectedState;
	}

	public <StateType> DeclarativeRuleBuilder then(final Device<StateType> device, final StateType newState) {
		final Action<StateType> action = new Action<>(device, newState, output);
		this.action = Optional.of(action);
		return this;
	}

	public Optional<Action<?>> getAction() {
		return action;
	}

	public void execute(final Object newState) {
		if (action.isPresent() && isExpectedState(newState)) {
			action.get().execute();
		}
	}

	boolean isExpectedState(final Object newState) {
		return expectedState.equals(newState);
	}

}