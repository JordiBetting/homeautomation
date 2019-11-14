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
	public Optional<Temperature> stateEntryResult() {
		return Optional.of(context.offTemperature);
	}

	@Override
	public Optional<HeatingState> alarmChanged() {
		if (context.frameworkState.getAlarmState().meets(AlarmState.DISARMED)) {
			return Optional.of(new StateHeatingOnDelay(context));
		} else {
			return Optional.empty();
		}
	}

}