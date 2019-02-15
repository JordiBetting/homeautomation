package nl.gingerbeard.automation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.base.Preconditions;

import nl.gingerbeard.automation.devices.Device;

public class DeviceRegistry {

	private static class DeviceGroup extends Device<Object> {
		private final List<Device<?>> devices = new ArrayList<>();

		public DeviceGroup(final int idx) {
			super(idx);
		}

		public void addDevice(final Device<?> device) {
			Preconditions.checkArgument(getIdx() == device.getIdx(), "Idx must match");
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

		public Optional<Device<?>> getAny() {
			if (devices.isEmpty()) {
				return Optional.empty();
			}
			return Optional.of(devices.get(0));
		}

		public List<? extends Device<?>> getAll() {
			return devices;
		}

	}

	private final Map<Integer, DeviceGroup> deviceGroups = new HashMap<>();

	public void addDevice(final Device<?> device) {
		final DeviceGroup deviceGroup = getOrCreateGroup(device.getIdx());
		deviceGroup.addDevice(device);
	}

	private DeviceGroup getOrCreateGroup(final int idx) {
		DeviceGroup deviceGroup = deviceGroups.get(idx);
		if (deviceGroup == null) {
			deviceGroup = new DeviceGroup(idx);
			deviceGroups.put(idx, deviceGroup);
		}
		return deviceGroup;
	}

	public int getUniqueDeviceCount() {
		return deviceGroups.size();
	}

	/**
	 * Returns all instances of the devices, may include duplicates
	 *
	 * @return
	 */
	public List<Device<?>> getAllDevices() {
		final List<Device<?>> totalList = new ArrayList<>();
		deviceGroups.forEach((idx, deviceGroup) -> totalList.addAll(deviceGroup.getAll()));
		return totalList;
	}

	/**
	 * Updates the devices with the specifix idx, returns one of those registered
	 *
	 * @param idx
	 * @param newState
	 * @return
	 */
	public Optional<Device<?>> updateDevice(final int idx, final String newState) {
		final DeviceGroup group = deviceGroups.get(idx);
		if (group != null) {
			final boolean result = group.updateState(newState);
			if (result) {
				return group.getAny();
			}
		}
		return Optional.empty();
	}

}
