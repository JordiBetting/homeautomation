package nl.gingerbeard.automation.domoticz;

import nl.gingerbeard.automation.state.TimeOfDayValues;

public interface IDomoticzTimeOfDayChanged {
	/**
	 * All in units since start of day.
	 *
	 * @param currentTime
	 * @param sunrise
	 * @param sunset
	 * @return if update was successful
	 */
	void timeChanged(TimeOfDayValues time);
}
