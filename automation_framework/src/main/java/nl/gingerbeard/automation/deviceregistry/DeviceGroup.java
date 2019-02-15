package nl.gingerbeard.automation.deviceregistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import nl.gingerbeard.automation.devices.Device;

final class DeviceGroup extends Device<Object> {
	private final List<Device<?>> devices = new ArrayList<>();

	DeviceGroup(final int idx) {
		super(idx);
	}

	void addDevice(final Device<?> device) {
		devices.add(device);
	}

	@Override
	public boolean updateState(final String newState) {
		boolean groupResult = true;
		for (final Device<?> device : devices) {
			final boolean deviceResult = device.updateState(newState);
			if (deviceResult == false) {
				groupResult = false;
			}
		}
		return groupResult;
	}

	Optional<Device<?>> getAny() {
		return Optional.of(devices.get(0));
	}

	List<? extends Device<?>> getAll() {
		return devices;
	}

}
