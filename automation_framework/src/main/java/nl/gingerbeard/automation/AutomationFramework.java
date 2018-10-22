package nl.gingerbeard.automation;

import com.google.common.base.Preconditions;

import nl.gingerbeard.automation.controlloop.Controlloop;
import nl.gingerbeard.automation.devices.Device;
import nl.gingerbeard.automation.domoticz.Domoticz;
import nl.gingerbeard.automation.event.Events;
import nl.gingerbeard.automation.event.SynchronousEvents;
import nl.gingerbeard.automation.state.State;

public class AutomationFramework {

	private AutomationFrameworkState frameworkState = AutomationFrameworkState.INITIALIZING;

	private final State state;
	private final Domoticz domoticz;
	private final Events events;
	private final Controlloop controlLoop;

	private AutomationFramework(final State state, final Domoticz domoticz, final Events events, final Controlloop controlloop) {
		this.state = state;
		this.domoticz = domoticz;
		this.events = events;
		controlLoop = controlloop;
	}

	public static AutomationFramework create() {
		final State state = new State();
		final Domoticz domoticz = new Domoticz();
		final Events events = new SynchronousEvents(state);
		final Controlloop controlloop = new Controlloop(events);

		return new AutomationFramework(state, domoticz, events, controlloop);
	}

	public void addRoom(final Room room) {
		ensureState(AutomationFrameworkState.INITIALIZING);

		events.subscribe(room);
	}

	public void start() {
		ensureState(AutomationFrameworkState.INITIALIZING);
		frameworkState = AutomationFrameworkState.RUNNING;
	}

	public void stop() {
		ensureState(AutomationFrameworkState.RUNNING);
		frameworkState = AutomationFrameworkState.STOPPED;
	}

	private void ensureState(final AutomationFrameworkState expectedState) {
		Preconditions.checkState(frameworkState == expectedState);
	}

	public AutomationFrameworkState getFrameworkState() {
		return frameworkState;
	}

	public void deviceChanged(final Device changedDevice) {
		Preconditions.checkState(frameworkState == AutomationFrameworkState.RUNNING);
		events.trigger(changedDevice);
	}

	public State getState() {
		return state;
	}

}
