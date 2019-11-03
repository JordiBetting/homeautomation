package nl.gingerbeard.automation.devices;

import java.util.Optional;

public abstract class Device<T> extends StateDevice<T> {
	private final int idx;
	private Optional<String> name = Optional.empty();

	public Device(final int idx) {
		this.idx = idx;
	}
	
	public Device() {
		this.idx = -1;
	}

	@Override
	public int getIdx() {
		return idx;
	}

	public final void setName(final String newName) {
		this.name = Optional.ofNullable(newName);
	}

	public Optional<String> getName() {
		return name;
	}

	@Override
	public String toString() {
		return "Device [idx=" + idx + ", name=" + name + ", state=" + getState() + "]";
	}
}
