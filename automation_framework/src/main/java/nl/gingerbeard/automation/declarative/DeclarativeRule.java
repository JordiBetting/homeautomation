package nl.gingerbeard.automation.declarative;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import nl.gingerbeard.automation.devices.Device;

final class DeclarativeRule {
	private final DeclarativeRulesRegistry registry;
	private final Scheduler scheduler = new Scheduler();
	private final Map<Device<?>, Object> expectedStates = new HashMap<>();
	private final DeclarativeRuleActions actions;
	private DeclarativeRuleType type = DeclarativeRuleType.INSTANT;

	DeclarativeRule(final DeclarativeRulesRegistry registry, final Device<?> device, final IDeviceUpdate output, final Object expectedState) {
		this.registry = registry;
		expectedStates.put(device, expectedState);
		actions = new DeclarativeRuleActions(output);
	}

	public <StateType> DeclarativeRuleActions then(final Device<StateType> device, final StateType newState) {
		final Action<StateType> action = new Action<>(device, newState);
		actions.when(action);
		return actions;
	}

	void updateDevice(final Device<?> device, final Object newState) {
		if (isExpectedState(device, newState)) {
			updateDeviceExpectedState(device);
		} else {
			updateDeviceNotExpectedState(device);
		}
	}

	private void updateDeviceExpectedState(final Device<?> device) {
		if (type == DeclarativeRuleType.INSTANT) {
			actions.forEachAction((action) -> action.execute());
		} else {
			scheduler.schedule(device, actions.getActions());
		}
	}

	private void updateDeviceNotExpectedState(final Device<?> device) {
		scheduler.cancel(device);
		if (type == DeclarativeRuleType.INSTANT) {
			actions.forEachElseAction((elseAction) -> elseAction.execute());
		} else {
			scheduler.schedule(device, actions.getElseActions());
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
		return actions.hasActions();
	}

	public <StateType> DeclarativeRule and(final Device<StateType> device, final StateType expectedState) {
		registry.add(device, this);
		expectedStates.put(device, expectedState);
		return this;
	}

	public DeclarativeRule forDuration(final Duration duration) {
		type = DeclarativeRuleType.DELAYED;
		scheduler.setDuration(duration);
		return this;
	}

}