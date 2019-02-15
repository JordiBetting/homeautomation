package nl.gingerbeard.automation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;

import nl.gingerbeard.automation.devices.Device;
import nl.gingerbeard.automation.devices.Switch;

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

	}

	private final Map<Integer, DeviceGroup> deviceGroups = new HashMap<>();

	public void addDevice(final Switch device) {
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

	public boolean updateDevice(final int idx, final String newState) {
		final DeviceGroup group = deviceGroups.get(idx);
		if (group != null) {
			return group.updateState(newState);
		}
		return false;
	}

}
