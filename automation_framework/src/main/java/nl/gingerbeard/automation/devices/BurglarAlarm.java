package nl.gingerbeard.automation.devices;

import java.util.Optional;

import nl.gingerbeard.automation.state.AlarmState;

public class BurglarAlarm extends Device<AlarmState> {

	public BurglarAlarm(final int idx, final Optional<Integer> batteryDomoticzId) {
		super(idx, batteryDomoticzId);
	}

	@Override
	public boolean updateState(final String newState) {
		return false; // TODO
	}

}
