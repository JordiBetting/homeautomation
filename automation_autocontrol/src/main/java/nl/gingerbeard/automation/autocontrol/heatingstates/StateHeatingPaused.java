package nl.gingerbeard.automation.autocontrol.heatingstates;

import java.util.Optional;

import nl.gingerbeard.automation.autocontrol.HeatingAutoControlContext;
import nl.gingerbeard.automation.state.Temperature;
import nl.gingerbeard.automation.state.TimeOfDay;

public final class StateHeatingPaused extends StateHeatingOn {
	
	public StateHeatingPaused(HeatingAutoControlContext context) {
		super(context);
	}
	
	@Override
	public Optional<Temperature> stateEntryResult() {
		return Optional.of(context.offTemperature);
	}

	@Override
	public Optional<HeatingState> allPauseDevicesOff() {
		if (context.frameworkState.getTimeOfDay() == TimeOfDay.DAYTIME) {
			return Optional.of(new StateHeatingOnDaytime(context));
		} else {
			return Optional.of(new StateHeatingOnNighttime(context));
		}
	}

}
