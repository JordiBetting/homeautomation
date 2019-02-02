package nl.gingerbeard.automation.state;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class HomeAwayTest {

	@Test
	public void matchesAlways() {
		assertTrue(HomeAway.ALWAYS.meets(HomeAway.ALWAYS));
		assertFalse(HomeAway.ALWAYS.meets(HomeAway.AWAY));
		assertFalse(HomeAway.ALWAYS.meets(HomeAway.HOME));
	}

	@Test
	public void matchesAway() {
		assertTrue(HomeAway.AWAY.meets(HomeAway.ALWAYS));
		assertTrue(HomeAway.AWAY.meets(HomeAway.AWAY));
		assertFalse(HomeAway.AWAY.meets(HomeAway.HOME));
	}

	@Test
	public void matchesHome() {
		assertTrue(HomeAway.HOME.meets(HomeAway.ALWAYS));
		assertFalse(HomeAway.HOME.meets(HomeAway.AWAY));
		assertTrue(HomeAway.HOME.meets(HomeAway.HOME));
	}

}
