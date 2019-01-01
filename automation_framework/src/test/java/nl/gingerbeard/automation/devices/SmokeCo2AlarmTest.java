package nl.gingerbeard.automation.devices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class SmokeCo2AlarmTest {
	@Test
	public void idx_works() {
		final SmokeCo2Alarm device = new SmokeCo2Alarm(42);

		assertEquals(42, device.getIdx());
	}

	@Test
	public void updateState_coveredBy_OnOffDevice() {
		assertTrue(OnOffDevice.class.isAssignableFrom(SmokeCo2Alarm.class));
	}

}
