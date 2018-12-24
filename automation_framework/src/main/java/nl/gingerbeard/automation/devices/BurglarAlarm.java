package nl.gingerbeard.automation.devices;

import nl.gingerbeard.automation.state.AlarmState;

public class BurglarAlarm extends Device<AlarmState> {

	public BurglarAlarm(final int idx) {
		super(idx);
	}

	@Override
	public boolean updateState(final String newState) {
		return false; // TODO
	}

	@Override
	public String getDomoticzParam() {
		throw new UnsupportedOperationException("not implemented yet"); // TODO
	}

	@Override
	public String getDomoticzSwitchCmd() {
		throw new UnsupportedOperationException("not implemented yet"); // TODO
	}

}
