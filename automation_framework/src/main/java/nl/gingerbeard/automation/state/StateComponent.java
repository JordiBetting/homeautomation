package nl.gingerbeard.automation.state;

import nl.gingerbeard.automation.service.annotation.Activate;
import nl.gingerbeard.automation.service.annotation.Deactivate;
import nl.gingerbeard.automation.service.annotation.Provides;

public final class StateComponent {
	@Provides
	public State state;

	@Activate
	public void initState() {
		state = new State();
	}

	@Deactivate
	public void removeState() {
		state = null;
	}
}
