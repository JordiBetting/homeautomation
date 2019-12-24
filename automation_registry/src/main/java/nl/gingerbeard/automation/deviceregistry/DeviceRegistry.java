package nl.gingerbeard.automation.deviceregistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.common.base.Preconditions;

import nl.gingerbeard.automation.devices.Device;

public final class DeviceRegistry implements IDeviceRegistry {

	private final Map<Integer, DeviceGroup> deviceGroups = new HashMap<>();

	@Override
	public boolean addDevice(final Device<?> device) {
		Preconditions.checkArgument(device != null, "Please provide a non-null device to registry");
		boolean isNew = !devicePresent(device.getIdx());
		final DeviceGroup deviceGroup = getOrCreateGroup(device.getIdx());
		deviceGroup.addDevice(device);
		return isNew;
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

	@Override
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

	@Override
	public boolean devicePresent(final int idx) {
		return deviceGroups.get(idx) != null;
	}

	@Override
	public Optional<?> getDeviceState(int idx) {
		DeviceGroup group = deviceGroups.get(idx);
		if (group != null) {
			return Optional.ofNullable(group.getAny().get().getState());
		}
		return Optional.empty();
	}

	@Override
	public Set<Integer> getAllIdx() {
		return deviceGroups.keySet();
	}

}
