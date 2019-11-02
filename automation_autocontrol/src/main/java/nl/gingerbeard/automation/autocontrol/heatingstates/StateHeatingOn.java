package nl.gingerbeard.automation.autocontrol.heatingstates;

import java.util.Optional;

import nl.gingerbeard.automation.autocontrol.HeatingAutoControlContext;
import nl.gingerbeard.automation.state.AlarmState;

public abstract class StateHeatingOn extends HeatingState {
	HeatingAutoControlContext context;

	public StateHeatingOn(HeatingAutoControlContext context) {
		this.context = context;
	}

	@Override
	public Optional<HeatingState> alarmChanged() {
		if (!context.frameworkState.getAlarmState().meets(AlarmState.DISARMED)) {
			return Optional.of(new StateHeatingOff(context));
		}
		return super.alarmChanged();
	}

}