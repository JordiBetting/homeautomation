package nl.gingerbeard.automation.devices;

import java.util.Collections;
import java.util.Set;

public abstract class CompositeDevice<T> extends StateDevice<T> {

	private final Set<Device<?>> devices;

	protected CompositeDevice(final Set<Device<?>> devices) {
		this.devices = devices;
	}

	public final Set<Device<?>> getDevices() {
		return Collections.unmodifiableSet(devices);
	}

	@Override
	public boolean updateState(final String newState) {
		throw new UnsupportedOperationException("CompositeDevices cannot be updated. Update subdevices instead");
	}

	@Override
	public int getIdx() {
		throw new UnsupportedOperationException("CompositeDevices don't have an idx, use subdevices instead");
	}

}
