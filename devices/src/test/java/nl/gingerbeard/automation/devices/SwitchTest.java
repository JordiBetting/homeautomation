package nl.gingerbeard.automation.devices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class SwitchTest {
	@Test
	public void idx_works() {
		final Switch device = new Switch(42);

		assertEquals(42, device.getIdx());
	}

	@Test
	public void updateState_coveredBy_OnOffDevice() {
		assertTrue(OnOffDevice.class.isAssignableFrom(Switch.class));
	}

}
