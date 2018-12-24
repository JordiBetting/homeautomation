package nl.gingerbeard.automation.event;

import nl.gingerbeard.automation.service.annotation.Activate;
import nl.gingerbeard.automation.service.annotation.Provides;
import nl.gingerbeard.automation.service.annotation.Requires;
import nl.gingerbeard.automation.state.State;

public class EventsCompoent {

	@Provides
	public Events events;

	@Requires
	public State state;

	@Activate
	public void createEvents() {
		events = new SynchronousEvents(state);
	}
}
