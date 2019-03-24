package nl.gingerbeard.automation.state;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class TimeTest {

	@Test
	public void equalsContract() {
		EqualsVerifier.forClass(Time.class).verify();
	}

}
