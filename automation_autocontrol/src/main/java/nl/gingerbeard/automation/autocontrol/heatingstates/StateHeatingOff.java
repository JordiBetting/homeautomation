package nl.gingerbeard.automation.autocontrol.heatingstates;

import java.util.Optional;

import nl.gingerbeard.automation.autocontrol.HeatingAutoControlContext;
import nl.gingerbeard.automation.state.AlarmState;
import nl.gingerbeard.automation.state.Temperature;

public class StateHeatingOff extends HeatingState {

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