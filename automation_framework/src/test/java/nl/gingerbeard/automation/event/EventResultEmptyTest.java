package nl.gingerbeard.automation.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Optional;

import org.junit.jupiter.api.Test;

public class EventResultEmptyTest {

	@Test
	public void emptyAlwaysSame() {
		assertEquals(EventResultEmpty.create(), EventResultEmpty.create());
	}

	@Test
	public void empty_add_throwsException() {
		final EventResult empty = EventResultEmpty.create();

		try {
			empty.add(EventResultList.of("test"));
			fail("expected exception");
		} catch (final UnsupportedOperationException e) {
			assertEquals("Cannot add values to empty event result", e.getMessage());
		}
	}

	@Test
	public void empty_size_zero() {
		assertEquals(0, EventResultEmpty.create().size());
	}

	@Test
	public void empty_getAll_returnsEmptyCollection() {
		assertEquals(0, EventResultEmpty.create().getAll().size());
	}

	@Test
	public void empty_get_returnsEmpty() {
		final EventResult empty = EventResultEmpty.create();
		assertEquals(Optional.empty(), empty.get(-1));
		assertEquals(Optional.empty(), empty.get(0));
		assertEquals(Optional.empty(), empty.get(1));
	}
}
