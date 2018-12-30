package nl.gingerbeard.automation.devices;

import java.util.Optional;

public abstract class Device<T> extends StateDevice<T> {
	private final int idx;
	private final Optional<Integer> batteryDomoticzId;

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

}
