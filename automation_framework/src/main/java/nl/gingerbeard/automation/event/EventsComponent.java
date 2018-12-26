package nl.gingerbeard.automation.event;

import nl.gingerbeard.automation.service.annotation.Activate;
import nl.gingerbeard.automation.service.annotation.Deactivate;
import nl.gingerbeard.automation.service.annotation.Provides;
import nl.gingerbeard.automation.service.annotation.Requires;
import nl.gingerbeard.automation.state.State;

public final class EventsComponent {

	@Provides
	public IEvents events;

	@Requires
	public State state;

	private SynchronousEvents instance;

	@Activate
	public void createEvents() {
		events = instance = new SynchronousEvents(state);
	}

	@Deactivate
	public void removeEvents() {
		instance.clear();
		events = instance = null;
	}
}
