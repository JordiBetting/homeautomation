package nl.gingerbeard.automation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

}
