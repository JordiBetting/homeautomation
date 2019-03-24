package nl.gingerbeard.automation.state;

import java.util.Optional;

public enum AlarmState {
	ALWAYS, //
	ARMED, //
	DISARMED, //
	ARM_HOME(AlarmState.ARMED), //
	ARM_AWAY(AlarmState.ARMED), //
	;

	private Optional<AlarmState> matches;

	AlarmState() {
		matches = Optional.empty();
	}

	AlarmState(final AlarmState matches) {
		this.matches = Optional.of(matches);

	}

	public boolean meets(final AlarmState other) {
		return this == AlarmState.ALWAYS || equals(other) || other.equals(ALWAYS) || matches.isPresent() && matches.get().equals(other);
	}

}
