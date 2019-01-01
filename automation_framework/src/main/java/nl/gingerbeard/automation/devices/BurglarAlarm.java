package nl.gingerbeard.automation.devices;

import java.util.Locale;

import nl.gingerbeard.automation.state.AlarmState;

public class BurglarAlarm extends Device<AlarmState> {

	public BurglarAlarm(final int idx) {
		super(idx);
	}

	@Override
	public boolean updateState(final String newState) {
		try {
			final AlarmState alarmState = AlarmState.valueOf(newState.toUpperCase(Locale.US));
			setState(alarmState);
			return true;
		} catch (final IllegalArgumentException e) {
			return false;
		}
	}

}
