package nl.gingerbeard.automation.declarative;

import java.util.Optional;

import nl.gingerbeard.automation.devices.Device;

public final class DeclarativeRuleBuilder {
	private Optional<Action<?>> action = Optional.empty();
	private final Object expectedState;

	DeclarativeRuleBuilder(final Object expectedState) {
		this.expectedState = expectedState;
	}

	public <T> DeclarativeRuleBuilder then(final Device<T> device, final T newState) {
		final Action<T> action = new Action<>(device, newState);
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