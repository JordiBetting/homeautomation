package nl.gingerbeard.automation.declarative;

import java.util.ArrayList;
import java.util.List;

import nl.gingerbeard.automation.devices.Device;

public final class DeclarativeRuleBuilder {
	private final List<Action<?>> actions = new ArrayList<>();
	private final List<Action<?>> elseActions = new ArrayList<>();
	private final Object expectedState;
	private final IDeviceUpdate output;

	public class DeclarativeThenBuilder {
		private List<Action<?>> lastActionList;

		public DeclarativeThenBuilder() {
			lastActionList = actions;
		}

		public <StateType> DeclarativeThenBuilder orElse(final Device<StateType> device, final StateType newState) {
			final Action<StateType> action = new Action<>(device, newState, output);
			elseActions.add(action);
			lastActionList = elseActions;
			return this;
		}

		public <StateType> DeclarativeThenBuilder and(final Device<StateType> device, final StateType newState) {
			final Action<StateType> action = new Action<>(device, newState, output);
			lastActionList.add(action);
			return this;
		}
	}

	DeclarativeRuleBuilder(final IDeviceUpdate output, final Object expectedState) {
		this.output = output;
		this.expectedState = expectedState;
	}

	public <StateType> DeclarativeThenBuilder then(final Device<StateType> device, final StateType newState) {
		final Action<StateType> action = new Action<>(device, newState, output);
		actions.add(action);
		return new DeclarativeThenBuilder();
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

}