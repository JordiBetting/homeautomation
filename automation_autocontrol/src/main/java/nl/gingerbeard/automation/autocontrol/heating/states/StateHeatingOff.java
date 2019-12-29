package nl.gingerbeard.automation.autocontrol.heating.states;

import java.util.Optional;

import nl.gingerbeard.automation.autocontrol.heating.HeatingAutoControlContext;
import nl.gingerbeard.automation.state.AlarmState;
import nl.gingerbeard.automation.state.Temperature;

public final class StateHeatingOff extends HeatingState {

	private HeatingAutoControlContext context;

	public StateHeatingOff(HeatingAutoControlContext context) {
		this.context = context;
	}
	
	@Override
	public Optional<HeatingState> stateEntryNextState() {
		if (isDisarmed()) {
			return Util.createNextOnStateBasedOnDaytime(context);
		} else {
			return Optional.empty();
		}
	}
	
	@Override
	public Optional<Temperature> stateEntryResult() {
		return Optional.of(context.offTemperature);
	}

	@Override
	public Optional<HeatingState> alarmChanged() {
		if (isDisarmed()) {
			return Optional.of(new StateHeatingOnDelay(context));
		} else {
			return Optional.empty();
		}
	}

	private boolean isDisarmed() {
		return context.frameworkState.getAlarmState().meets(AlarmState.DISARMED);
	}

}