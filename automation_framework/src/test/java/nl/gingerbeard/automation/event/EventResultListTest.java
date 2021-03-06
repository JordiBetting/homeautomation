package nl.gingerbeard.automation.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

public class EventResultListTest {

	@Test
	public void get_outofbounds_returnsEmpty() {
		final EventResultList list = new EventResultList();

		assertEquals(Optional.empty(), list.get(0));
		assertEquals(Optional.empty(), list.get(-1));

		list.add(EventResult.of("test"));

		assertEquals(Optional.empty(), list.get(1));
		assertTrue(list.get(0).isPresent());
		assertEquals("test", list.get(0).get());
		assertEquals(Optional.empty(), list.get(-1));
	}

}
