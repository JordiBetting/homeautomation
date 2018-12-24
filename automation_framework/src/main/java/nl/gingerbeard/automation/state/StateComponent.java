package nl.gingerbeard.automation.state;

import nl.gingerbeard.automation.service.annotation.Activate;
import nl.gingerbeard.automation.service.annotation.Provides;

public class StateComponent {
	@Provides
	public State state;

	@Activate
	public void initState() {
		state = new State();
	}
}
