package nl.gingerbeard.automation.devices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class DimmeableLightTest {
	@Test
	public void idx_works() {
		final DimmeableLight light = new DimmeableLight(42);

		assertEquals(42, light.getIdx());
	}

	@Test
	public void updateState_coveredBy_LevelDevice() {
		assertTrue(LevelDevice.class.isAssignableFrom(DimmeableLight.class));
	}

}
