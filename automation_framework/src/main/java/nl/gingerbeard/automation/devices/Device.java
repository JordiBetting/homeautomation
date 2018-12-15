package nl.gingerbeard.automation.devices;

import java.util.Optional;

public abstract class Device<T> {
	private final int idx;
	private final Optional<Integer> batteryDomoticzId;
	private T state;

	public Device(final int idx, final Optional<Integer> batteryDomoticzId) {
		this.idx = idx;
		this.batteryDomoticzId = batteryDomoticzId;
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
}
