package nl.gingerbeard.automation.devices;

import java.util.Collections;
import java.util.Set;

public abstract class CompositeDevice<T> extends StateDevice<T> {

	private final Set<Device<?>> devices;

	public CompositeDevice(final Set<Device<?>> devices) {
		this.devices = devices;
	}

	public final Set<Device<?>> getDevices() {
		return Collections.unmodifiableSet(devices);
	}

}
