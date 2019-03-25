package nl.gingerbeard.automation.devices;

import java.util.Optional;

import com.google.common.base.Preconditions;

public abstract class Device<T> extends StateDevice<T> {
	private final int idx;
	private Optional<String> name = Optional.empty();

	public Device(final int idx) {
		Preconditions.checkArgument(idx > 0, "idx shall be bigger then 0");
		this.idx = idx;
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
