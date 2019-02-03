package nl.gingerbeard.automation.declarative;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import nl.gingerbeard.automation.devices.Device;

public final class DeclarativeRuleBuilder {
	private final List<Action<?>> actions = new ArrayList<>();
	private final List<Action<?>> elseActions = new ArrayList<>();
	private final Map<Device<?>, Object> expectedStates = new HashMap<>();
	private final IDeviceUpdate output;
	private final DeclarativeRulesRegistry registry;

	// isolates possible actions after a then() call.
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

	DeclarativeRuleBuilder(final DeclarativeRulesRegistry registry, final Device<?> device, final IDeviceUpdate output, final Object expectedState) {
		this.registry = registry;
		this.output = output;
		expectedStates.put(device, expectedState);
	}

	public <StateType> DeclarativeThenBuilder then(final Device<StateType> device, final StateType newState) {
		final Action<StateType> action = new Action<>(device, newState, output);
		actions.add(action);
		return new DeclarativeThenBuilder();
	}

	public void execute(final Device<?> device, final Object newState) {
		if (isExpectedState(device, newState)) {
			actions.stream().forEach((action) -> action.execute());
		} else {
			elseActions.stream().forEach((elseAction) -> elseAction.execute());
		}
	}

	private boolean isExpectedState(final Device<?> device, final Object newState) {
		for (final Entry<Device<?>, Object> entry : expectedStates.entrySet()) {
			if (!entry.getKey().getState().equals(entry.getValue())) {
				return false;
			}
		}
		return true;
	}

	boolean hasActions() {
		return !actions.isEmpty() || !elseActions.isEmpty();
	}

	public <StateType> DeclarativeRuleBuilder and(final Device<StateType> device, final StateType expectedState) {
		registry.add(device, this);
		expectedStates.put(device, expectedState);
		return this;
	}

}