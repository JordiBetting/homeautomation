package nl.gingerbeard.automation.autocontrol.heating.states;

import java.util.Optional;

import nl.gingerbeard.automation.state.Temperature;

public abstract class HeatingState {
	public Optional<HeatingState> alarmChanged() {
		return Optional.empty();
	}

	public Optional<HeatingState> timeOfDayChanged() {
		return Optional.empty();
	}

	public Optional<Temperature> stateEntryResult() {
		return Optional.empty();
	}

	public Optional<HeatingState> stateEntryNextState() {
		return Optional.empty();
	}

	public Optional<HeatingState> allPauseDevicesOff() {
		return Optional.empty();
	}

	public Optional<HeatingState> pauseDeviceOn() {
		return Optional.empty();
	}
}
