package nl.gingerbeard.automation.state;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class OnkyoReceiverStateTest {

	@Test
	public void theTest() {
		OnkyoReceiverState state = new OnkyoReceiverState(OnOffState.OFF, OnOffState.ON);
		assertEquals(OnOffState.OFF, state.getMain());
		assertEquals(OnOffState.ON, state.getZone2());
		
		state = new OnkyoReceiverState(OnOffState.ON, OnOffState.OFF);
		assertEquals(OnOffState.ON, state.getMain());
		assertEquals(OnOffState.OFF, state.getZone2());
	}
}
