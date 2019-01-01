package nl.gingerbeard.automation.devices;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class BatteryTest {

	@Test
	public void idx_works() {
		final Battery battery = new Battery(42);

		assertEquals(42, battery.getIdx());
	}

}
