package nl.gingerbeard.automation.devices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class MovementSensorTest {

	@Test
	public void idxTest() {
		final MovementSensor device = new MovementSensor(1);

		assertEquals(1, device.getIdx());
	}

	@Test
	public void updateState_coveredBy_OnOffDevice() {
		assertTrue(OnOffDevice.class.isAssignableFrom(MovementSensor.class));
	}
}
