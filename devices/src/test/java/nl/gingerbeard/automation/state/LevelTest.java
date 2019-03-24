package nl.gingerbeard.automation.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class LevelTest {

	@Test
	public void validLevel_ok() {
		final Level level0 = new Level(0);
		final Level level1 = new Level(42);
		final Level level2 = new Level(100);

		assertEquals(0, level0.getLevel());
		assertEquals(42, level1.getLevel());
		assertEquals(100, level2.getLevel());
	}

	@Test
	public void negativeLevel_throwsException() {
		try {
			new Level(-1);
			fail("expected exception");
		} catch (final IllegalArgumentException e) {
			// expected
		}
	}

	@Test
	public void moreThan100Level_throwsException() {
		try {
			new Level(101);
			fail("expected exception");
		} catch (final IllegalArgumentException e) {
			// expected
		}
	}

	@Test
	public void test_toString() {
		final Level level = new Level(42);

		assertEquals("Level [level=42]", level.toString());
	}

	@Test
	public void equalsContract() {
		EqualsVerifier.forClass(Level.class).verify();
	}
}
