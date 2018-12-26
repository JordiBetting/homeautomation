package nl.gingerbeard.automation.devices;

import java.util.Optional;

import nl.gingerbeard.automation.state.NextState;

public abstract class Device<T> {
	private final int idx;
	private final Optional<Integer> batteryDomoticzId;
	private T state;

	public Device(final int idx, final int batteryDomoticzId) {
		this.idx = idx;
		this.batteryDomoticzId = Optional.of(batteryDomoticzId);
	}

	public Device(final int idx) {
		this.idx = idx;
		this.batteryDomoticzId = Optional.empty();
	}

	public int getIdx() {
		return idx;
	}

	public Optional<Integer> getBatteryDomoticzId() {
		return batteryDomoticzId;
	}

	public abstract boolean updateState(final String newState);

	public T getState() {
		return state;
	}

	protected void setState(final T newState) {
		state = newState;
	}

	public abstract String getDomoticzParam();

	public abstract String getDomoticzSwitchCmd(NextState<T> nextState);
}
