package nl.gingerbeard.automation.deviceregistry;

import java.util.Optional;
import java.util.Set;

import nl.gingerbeard.automation.devices.Device;

/**
 * This class holds an instance of all client Device types. Clients may use
 * multiple instances of a Device instance, that represent the same physical
 * device (e.g. one device shared in multiple Rooms). This class groups those
 * and ensures that all instances get updated state when a state update has been
 * received.
 * 
 * @author Jordi
 *
 */
public interface IDeviceRegistry {

	/** 
	 * Adds a device to the registry.
	 * @param device The device to add.
	 * @return True when it is the first instance of the device. False otherwise.
	 */
	boolean addDevice(Device<?> device);

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

	/**
	 * Retrieve a unique set of idx of all known devices.
	 * @return Set of unique idx
	 */
	Set<Integer> getAllIdx();
}