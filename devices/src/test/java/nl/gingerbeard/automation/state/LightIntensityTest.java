package nl.gingerbeard.automation.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class LightIntensityTest {

	@Test
	public void valid() {
		final LightIntensity li = new LightIntensity(0);

		assertEquals(0, li.getLux());
	}

	@Test
	public void negative() {
		assertThrows(IllegalArgumentException.class, () -> new LightIntensity(-1));
	}

	@Test
	public void lightIntensity_toString() {
		final LightIntensity li = new LightIntensity(42);
		assertEquals("LightIntensity [lux=42]", li.toString());
	}

}
