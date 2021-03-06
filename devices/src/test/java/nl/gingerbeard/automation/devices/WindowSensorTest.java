package nl.gingerbeard.automation.devices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class WindowSensorTest {

	@Test
	public void idx_works() {
		final WindowSensor device = new WindowSensor(42);

		assertEquals(42, device.getIdx());
	}

	@Test
	public void updateState_coveredBy_OpenCloseDevice() {
		assertTrue(OpenCloseDevice.class.isAssignableFrom(WindowSensor.class));
	}

}
