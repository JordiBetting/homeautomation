package nl.gingerbeard.automation.devices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class WhiteAmbianceLightTest {

	@Test
	public void idx_works() {
		final WhiteAmbianceLight device = new WhiteAmbianceLight(66);

		assertEquals(66, device.getIdx());
	}

	@Test
	public void updateState_notImplemented() {
		final WhiteAmbianceLight device = new WhiteAmbianceLight(66);

		assertThrows(UnsupportedOperationException.class, () -> device.updateState(""));
	}
}
