package nl.gingerbeard.automation.deviceregistry;

import java.util.Optional;

import nl.gingerbeard.automation.devices.Device;

public interface IDeviceRegistry {

	void addDevice(Device<?> device);

	/**
	 * Updates the devices with the specifix idx, returns one of those registered
	 *
	 * @param idx
	 * @param newState
	 * @return
	 */
	Optional<Device<?>> updateDevice(int idx, String newState);

	/**
	 * Returns true if a device with provided idx is present in the registry
	 * 
	 * @param idx
	 * @return
	 */
	boolean devicePresent(int idx);

	Optional<?> getDeviceState(int idx);
}