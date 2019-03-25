package nl.gingerbeard.automation.devices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

public class DeviceTest {

	private static class MockDevice extends Device<String> {

		public MockDevice() {
			super(123);
		}

		public MockDevice(final int idx) {
			super(idx);
		}

		@Override
		public boolean updateState(final String newState) {
			return false;
		}

	}

	@Test
	public void toStringTest() {
		final MockDevice device = new MockDevice();

		assertEquals("Device [idx=123, name=Optional.empty, state=null]", device.toString());
		device.setState("STATE");
		assertEquals("Device [idx=123, name=Optional.empty, state=STATE]", device.toString());
		device.setName("NAME");
		assertEquals("Device [idx=123, name=Optional[NAME], state=STATE]", device.toString());
	}

	@Test
	public void nameTest() {
		final MockDevice device = new MockDevice();
		assertEquals(Optional.empty(), device.getName());

		device.setName("Fender");
		assertTrue(device.getName().isPresent());
		assertEquals("Fender", device.getName().get());
	}

	@Test
	public void idxTest() {
		final MockDevice device1 = new MockDevice(1);
		assertEquals(1, device1.getIdx());

		final MockDevice device2 = new MockDevice(42);
		assertEquals(42, device2.getIdx());

		assertThrows(IllegalArgumentException.class, () -> new MockDevice(0));

		assertThrows(IllegalArgumentException.class, () -> new MockDevice(-1));
	}

}
