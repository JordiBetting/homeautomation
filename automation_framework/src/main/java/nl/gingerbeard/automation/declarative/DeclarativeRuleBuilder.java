package nl.gingerbeard.automation.declarative;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import nl.gingerbeard.automation.devices.Device;

public final class DeclarativeRuleBuilder {
	private final List<Action<?>> actions = new ArrayList<>();
	private final List<Action<?>> elseActions = new ArrayList<>();
	private Optional<List<Action<?>>> lastActionList = Optional.empty();
	private final Object expectedState;
	private final IDeviceUpdate output;

	DeclarativeRuleBuilder(final IDeviceUpdate output, final Object expectedState) {
		this.output = output;
		this.expectedState = expectedState;
	}

	public <StateType> DeclarativeRuleBuilder then(final Device<StateType> device, final StateType newState) {
		final Action<StateType> action = new Action<>(device, newState, output);
		actions.add(action);
		lastActionList = Optional.of(actions);
		return this;
	}

	public <StateType> DeclarativeRuleBuilder and(final Device<StateType> device, final StateType newState) {
		if (lastActionList.isPresent()) {
			final Action<StateType> action = new Action<>(device, newState, output);
			lastActionList.get().add(action);
		} else {
			throw new IllegalStateException("and() called without then() or orElse()");
		}
		return then(device, newState);
	}

	public List<Action<?>> getActions() {
		return actions;
	}

	public void execute(final Object newState) {
		if (isExpectedState(newState)) {
			actions.stream().forEach((action) -> action.execute());
		} else {
			elseActions.stream().forEach((action) -> action.execute());
		}
	}

	private boolean isExpectedState(final Object newState) {
		return expectedState.equals(newState);
	}

	boolean hasActions() {
		return !actions.isEmpty() || !elseActions.isEmpty();
	}

	public <StateType> DeclarativeRuleBuilder orElse(final Device<StateType> device, final StateType newState) {
		final Action<StateType> action = new Action<>(device, newState, output);
		elseActions.add(action);
		lastActionList = Optional.of(elseActions);
		return this;
	}

}