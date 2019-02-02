package nl.gingerbeard.automation.devices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.google.common.collect.Iterables;

public class CompositeDeviceTest {

	private static class TestCompositeDevice extends CompositeDevice<String> {

		public TestCompositeDevice(final Set<Device<?>> devices) {
			super(devices);
		}

	}

	@Test
	public void createWithSubdevices() {
		final Set<Device<?>> set = new HashSet();
		set.add(new Switch(42));
		set.add(new DoorSensor(666));

		final CompositeDevice<String> device = new TestCompositeDevice(set);

		final Set<Device<?>> receivedDevices = device.getDevices();

		assertEquals(2, receivedDevices.size());
		final Device<?> device0 = Iterables.get(receivedDevices, 0);
		final Device<?> device1 = Iterables.get(receivedDevices, 1);
		assertTrue(device0 instanceof Switch ^ device1 instanceof Switch);
		assertTrue(device0 instanceof DoorSensor ^ device1 instanceof DoorSensor);

		int switchId, doorSensorId;
		if (device0 instanceof Switch) {
			switchId = device0.getIdx();
			doorSensorId = device1.getIdx();
		} else {
			switchId = device1.getIdx();
			doorSensorId = device0.getIdx();
		}

		assertEquals(42, switchId);
		assertEquals(666, doorSensorId);
	}

}
