package nl.gingerbeard.automation.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Optional;

import org.junit.jupiter.api.Test;

public class EventResultEmptyTest {

	@Test
	public void emptyAlwaysSame() {
		assertEquals(EventResult.empty(), EventResult.empty());
	}

	@Test
	public void empty_add_throwsException() {
		final EventResult empty = EventResult.empty();

		try {
			empty.add(EventResult.of("test"));
			fail("expected exception");
		} catch (final UnsupportedOperationException e) {
			assertEquals("Cannot add values to empty event result", e.getMessage());
		}
	}

	@Test
	public void empty_size_zero() {
		assertEquals(0, EventResult.empty().size());
	}

	@Test
	public void empty_getAll_returnsEmptyCollection() {
		assertEquals(0, EventResult.empty().getAll().size());
	}

	@Test
	public void empty_get_returnsEmpty() {
		final EventResult empty = EventResult.empty();
		assertEquals(Optional.empty(), empty.get(-1));
		assertEquals(Optional.empty(), empty.get(0));
		assertEquals(Optional.empty(), empty.get(1));
	}

}
