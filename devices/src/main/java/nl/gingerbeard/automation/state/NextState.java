package nl.gingerbeard.automation.state;

import com.google.common.base.Preconditions;

import nl.gingerbeard.automation.devices.StateDevice;

public final class NextState<StateType> {

	private final StateDevice<StateType> device;
	private final StateType nextState;
	private String trigger;

	public NextState(final StateDevice<StateType> device, final StateType nextState) {
		Preconditions.checkArgument(device != null);
		Preconditions.checkArgument(nextState != null);
		this.device = device;
		this.nextState = nextState;
		setTrigger(getCaller());
	}

	private String getCaller() {
		// 0 Thread
		// 1 this method
		// 2 constructor
		// 3 caller
		return Thread.currentThread().getStackTrace()[3].getClassName().replaceAll(".*\\.", "");
	}

	public void setTrigger(final String name) {
		trigger = name;
	}

	public String getTrigger() {
		return trigger;
	}

	public StateType get() {
		return nextState;
	}

	public StateDevice<StateType> getDevice() {
		return device;
	}

	@Override
	public String toString() {
		return "NextState [device=" + device + ", nextState=" + nextState + "]";
	}
}
