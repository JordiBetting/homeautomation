package nl.gingerbeard.automation.devices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class BatteryTest {

	@Test
	public void idx_works() {
		final Battery battery = new Battery(42);

		assertEquals(42, battery.getIdx());
	}

	@Test
	public void updateState_coveredBy_LevelDevice() {
		assertTrue(LevelDevice.class.isAssignableFrom(Battery.class));
	}

}
