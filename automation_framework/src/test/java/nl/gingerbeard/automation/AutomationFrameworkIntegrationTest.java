package nl.gingerbeard.automation;

import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.controlloop.Controlloop;
import nl.gingerbeard.automation.domoticz.Domoticz;
import nl.gingerbeard.automation.event.Events;
import nl.gingerbeard.automation.event.SynchronousEvents;
import nl.gingerbeard.automation.state.State;

public class AutomationFrameworkIntegrationTest {

	@Test
	public void deviceChangeInDomoticzListener_receivedInStateListener() {
		final State state = new State();
		final Events events = new SynchronousEvents(state);
		final Controlloop controlLoop = new Controlloop(events);
		final AutomationFramework framework = new AutomationFramework(state, new Domoticz((e) -> {
		}, controlLoop), events, controlLoop);
	}
}
