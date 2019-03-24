package nl.gingerbeard.automation.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class PowerUsageTest {

	@Test
	public void valid() {
		final PowerUsage pu = new PowerUsage(1);

		assertEquals(1, pu.getUsageWatt());
	}

	@Test
	public void negative() {
		assertThrows(IllegalArgumentException.class, () -> new PowerUsage(-2));
	}

	@Test
	public void powerUsage_toString() {
		final PowerUsage pu = new PowerUsage(42);

		assertEquals("PowerUsage [usageWatt=42]", pu.toString());
	}

	@Test
	public void equalsContract() {
		EqualsVerifier.forClass(PowerUsage.class).verify();
	}
}
