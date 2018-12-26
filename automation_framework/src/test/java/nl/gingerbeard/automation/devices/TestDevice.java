package nl.gingerbeard.automation.devices;

import nl.gingerbeard.automation.state.NextState;

public class TestDevice extends Device<Void> {

	public TestDevice() {
		super(0);
	}

	@Override
	public boolean updateState(final String newState) {
		return false;
	}

	@Override
	public String getDomoticzParam() {
		return null;
	}

	@Override
	public String getDomoticzSwitchCmd(final NextState<Void> nextState) {
		return null;
	}

}