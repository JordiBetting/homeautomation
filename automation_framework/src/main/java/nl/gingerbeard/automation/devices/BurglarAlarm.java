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

}
