package nl.gingerbeard.automation.devices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class WallPlugTest {

	@Test
	public void idxTest() {
		final WallPlug device = new WallPlug(1);

		assertEquals(1, device.getIdx());
	}

	@Test
	public void updateState_coveredBy_OnOffDevice() {
		assertTrue(OnOffDevice.class.isAssignableFrom(WallPlug.class));
	}
}
