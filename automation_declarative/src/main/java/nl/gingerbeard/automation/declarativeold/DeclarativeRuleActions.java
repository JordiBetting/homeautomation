package nl.gingerbeard.automation.declarativeold;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import nl.gingerbeard.automation.devices.Device;

final class DeclarativeRuleActions {
	private final List<Action<?>> actions = new ArrayList<>();
	private final List<Action<?>> elseActions = new ArrayList<>();
	private List<Action<?>> lastActionList;
	private final IDeviceUpdate output;

	DeclarativeRuleActions(final IDeviceUpdate output) {
		this.output = output;
		lastActionList = actions;
	}

	void when(final Action<?> action) {
		actions.add(action);
		action.setOutput(output);
	}

	<StateType> DeclarativeRuleActions orElse(final Device<StateType> device, final StateType newState) {
		final Action<StateType> action = createAction(device, newState);
		elseActions.add(action);
		lastActionList = elseActions;
		return this;
	}

	<StateType> DeclarativeRuleActions and(final Device<StateType> device, final StateType newState) {
		final Action<StateType> action = createAction(device, newState);
		lastActionList.add(action);
		return this;
	}

	private <StateType> Action<StateType> createAction(final Device<StateType> device, final StateType newState) {
		final Action<StateType> action = new Action<>(device, newState);
		action.setOutput(output);
		return action;
	}

	void forEachAction(final Consumer<? super Action<?>> consumer) {
		actions.stream().forEach(consumer);
	}

	void forEachElseAction(final Consumer<? super Action<?>> consumer) {
		elseActions.stream().forEach(consumer);
	}

	List<Action<?>> getActions() {
		return actions;
	}

	List<Action<?>> getElseActions() {
		return elseActions;
	}

	public boolean hasActions() {
		return !actions.isEmpty() || !elseActions.isEmpty();
	}
}