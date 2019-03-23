package nl.gingerbeard.automation.deviceregistry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.devices.Device;
import nl.gingerbeard.automation.devices.Switch;
import nl.gingerbeard.automation.state.OnOffState;

public class DeviceRegistryTest {

	private DeviceRegistry registry;

	@BeforeEach
	public void createRegistry() {
		registry = new DeviceRegistry();
	}

	@Test
	public void addSingle_size1() {
		final Switch testDevice = new Switch(1);

		registry.addDevice(testDevice);

		assertEquals(1, registry.getUniqueDeviceCount());
	}

	@Test
	public void twoDevicesSameIdx_size1() {
		final Switch testDevice1 = new Switch(1);
		final Switch testDevice2 = new Switch(1);

		registry.addDevice(testDevice1);
		registry.addDevice(testDevice2);

		assertEquals(1, registry.getUniqueDeviceCount());
	}

	@Test
	public void twoDeviceDiffernetIdx_size2() {
		final Switch testDevice1 = new Switch(1);
		final Switch testDevice2 = new Switch(2);

		registry.addDevice(testDevice1);
		registry.addDevice(testDevice2);

		assertEquals(2, registry.getUniqueDeviceCount());
	}

	@Test
	public void twoDevicesSameIdx_bothUpdated() {
		final Switch testDevice1 = new Switch(1);
		final Switch testDevice2 = new Switch(1);

		registry.addDevice(testDevice1);
		registry.addDevice(testDevice2);

		registry.updateDevice(1, "On");
		assertEquals(OnOffState.ON, testDevice1.getState());
		assertEquals(OnOffState.ON, testDevice2.getState());

		registry.updateDevice(1, "Off");
		assertEquals(OnOffState.OFF, testDevice1.getState());
		assertEquals(OnOffState.OFF, testDevice2.getState());
	}

	@Test
	public void twoDeviceDifferentIdx_oneUpdated() {
		final Switch testDevice = new Switch(1);
		final Switch testDeviceOff = new Switch(2);
		testDeviceOff.setState(OnOffState.OFF);

		registry.addDevice(testDevice);
		registry.addDevice(testDeviceOff);

		registry.updateDevice(1, "On");
		assertEquals(OnOffState.ON, testDevice.getState());
		assertEquals(OnOffState.OFF, testDeviceOff.getState());

		registry.updateDevice(1, "Off");
		assertEquals(OnOffState.OFF, testDevice.getState());
		assertEquals(OnOffState.OFF, testDeviceOff.getState());
	}

	@Test
	public void addNullDevice_throwsException() {
		final IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> registry.addDevice(null));
		assertEquals("Please provide a non-null device to registry", e.getMessage());
	}

	@Test
	public void updateDevice_unknownIdx_returnsEmpty() {
		final Optional<Device<?>> returned = registry.updateDevice(1, "");

		assertEquals(Optional.empty(), returned);
	}

	@Test
	public void updateIllegal_returnsFalse() {
		final Switch testDevice = new Switch(1);
		registry.addDevice(testDevice);

		final Optional<Device<?>> result = registry.updateDevice(1, "blaat");

		assertEquals(Optional.empty(), result);
	}

	@Test
	public void getAllDevices() {
		final Switch testDevice1 = new Switch(1);
		final Switch testDevice2 = new Switch(1);
		final Switch testDevice3 = new Switch(2);

		registry.addDevice(testDevice1);
		registry.addDevice(testDevice2);
		registry.addDevice(testDevice3);

		assertEquals(3, registry.getAllDevices().size());
		assertEquals(2, registry.getUniqueDeviceCount());
	}

	@Test
	public void addDevice_devicePresent_true() {
		final Switch testDevice = new Switch(42);

		registry.addDevice(testDevice);

		assertTrue(registry.devicePresent(42));
	}

	@Test
	public void noDevice_devicePresent_false() {
		assertFalse(registry.devicePresent(42));
	}

	@Test
	public void addDeviceCheckOther_devicePresent_false() {
		final Switch testDevice = new Switch(42);

		registry.addDevice(testDevice);

		assertFalse(registry.devicePresent(66));
	}
}
