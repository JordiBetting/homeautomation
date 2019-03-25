package nl.gingerbeard.automation.components;

import nl.gingerbeard.automation.event.IEvents;
import nl.gingerbeard.automation.event.SynchronousEvents;
import nl.gingerbeard.automation.logging.ILogger;
import nl.gingerbeard.automation.service.annotation.Activate;
import nl.gingerbeard.automation.service.annotation.Deactivate;
import nl.gingerbeard.automation.service.annotation.Provides;
import nl.gingerbeard.automation.service.annotation.Requires;
import nl.gingerbeard.automation.state.IState;

public final class EventsComponent {

	@Provides
	public IEvents events;

	@Requires
	public IState state;

	@Requires
	public ILogger log;

	private SynchronousEvents instance;

	@Activate
	public void createEvents() {
		events = instance = new SynchronousEvents(state, log);
	}

	@Deactivate
	public void removeEvents() {
		instance.clear();
		events = instance = null;
	}
}
