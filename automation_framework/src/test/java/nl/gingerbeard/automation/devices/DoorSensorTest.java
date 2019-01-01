package nl.gingerbeard.automation.devices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class DoorSensorTest {

	@Test
	public void idx_works() {
		final DoorSensor device = new DoorSensor(42);

		assertEquals(42, device.getIdx());
	}

	@Test
	public void updateState_coveredBy_LevelDevice() {
		assertTrue(OpenCloseDevice.class.isAssignableFrom(DoorSensor.class));
	}

}
