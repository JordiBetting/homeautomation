package nl.gingerbeard.automation.devices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class ColorLightTest {
	@Test
	public void idx_works() {
		final ColorLight device = new ColorLight(66);

		assertEquals(66, device.getIdx());
	}

	@Test
	public void updateState_notImplemented() {
		final ColorLight device = new ColorLight(66);

		assertThrows(UnsupportedOperationException.class, () -> device.updateState(""));
	}
}
