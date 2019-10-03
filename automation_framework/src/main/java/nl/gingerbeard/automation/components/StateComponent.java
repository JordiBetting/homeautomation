package nl.gingerbeard.automation.components;

import nl.gingerbeard.automation.logging.ILogger;
import nl.gingerbeard.automation.service.annotation.Activate;
import nl.gingerbeard.automation.service.annotation.Deactivate;
import nl.gingerbeard.automation.service.annotation.Provides;
import nl.gingerbeard.automation.service.annotation.Requires;
import nl.gingerbeard.automation.state.IState;
import nl.gingerbeard.automation.state.State;

public final class StateComponent {
	@Provides
	public volatile IState state;
	
	@Requires
	public ILogger log;

	@Activate
	public void initState() {
		state = new State();
	}

	@Deactivate
	public void removeState() {
		state = null;
	}
}
