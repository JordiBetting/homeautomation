package nl.gingerbeard.automation.devices;

public abstract class Device<T> extends StateDevice<T> {
	private final int idx;

	public Device(final int idx) {
		this.idx = idx;
	}

	public int getIdx() {
		return idx;
	}

	public abstract boolean updateState(final String newState);

	@Override
	public String toString() {
		return "Device [idx=" + idx + "]";
	}
}
