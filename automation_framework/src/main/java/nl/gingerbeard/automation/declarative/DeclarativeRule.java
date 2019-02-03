package nl.gingerbeard.automation.declarative;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import nl.gingerbeard.automation.devices.Device;

final class DeclarativeRule {
	// TODO: yeah this is becoming a mess. Split up class, use classes
	// for state or think of something better. This needs cleaning!
	private final List<Action<?>> actions = new ArrayList<>();
	private final List<Action<?>> elseActions = new ArrayList<>();
	private final Map<Device<?>, Object> expectedStates = new HashMap<>();
	private final IDeviceUpdate output;
	private final DeclarativeRulesRegistry registry;
	private Duration duration;
	private RuleType type = RuleType.INSTANT;
	private final Scheduler scheduler = new Scheduler();

	private static enum RuleType {
		INSTANT, //
		DELAYED, //
		;
	}

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

	DeclarativeRule(final DeclarativeRulesRegistry registry, final Device<?> device, final IDeviceUpdate output, final Object expectedState) {
		this.registry = registry;
		this.output = output;
		expectedStates.put(device, expectedState);
	}

	public <StateType> DeclarativeThenBuilder then(final Device<StateType> device, final StateType newState) {
		final Action<StateType> action = new Action<>(device, newState, output);
		actions.add(action);
		return new DeclarativeThenBuilder();
	}

	void updateDevice(final Device<?> device, final Object newState) {
		// TODO: I don't like this if/this/then/then structure
		if (isExpectedState(device, newState)) {
			if (type == RuleType.INSTANT) {
				actions.stream().forEach((action) -> action.execute());
			} else {
				scheduler.schedule(device, actions, duration);
			}
		} else {
			scheduler.cancel(device);
			if (type == RuleType.INSTANT) {
				elseActions.stream().forEach((elseAction) -> elseAction.execute());
			} else {
				scheduler.schedule(device, elseActions, duration);
			}
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

	public <StateType> DeclarativeRule and(final Device<StateType> device, final StateType expectedState) {
		registry.add(device, this);
		expectedStates.put(device, expectedState);
		return this;
	}

	public DeclarativeRule forDuration(final Duration duration) {
		type = RuleType.DELAYED;
		this.duration = duration;
		return this;
	}

}