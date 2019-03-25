package nl.gingerbeard.automation;

import nl.gingerbeard.automation.service.annotation.Activate;
import nl.gingerbeard.automation.service.annotation.Deactivate;
import nl.gingerbeard.automation.service.annotation.Provides;
import nl.gingerbeard.automation.state.IState;
import nl.gingerbeard.automation.state.State;

public final class StateComponent {
	@Provides
	public IState state;

	@Activate
	public void initState() {
		state = new State();
	}

	@Deactivate
	public void removeState() {
		state = null;
	}
}
