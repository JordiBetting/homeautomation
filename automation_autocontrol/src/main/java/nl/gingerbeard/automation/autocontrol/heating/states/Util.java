package nl.gingerbeard.automation.autocontrol.heating.states;

import java.util.Optional;

import nl.gingerbeard.automation.autocontrol.heating.HeatingAutoControlContext;
import nl.gingerbeard.automation.state.TimeOfDay;

final class Util {
	private Util() {
		//avoid instantiation
	}
	
	static Optional<HeatingState> createNextOnStateBasedOnDaytime(HeatingAutoControlContext context) {
		if (context.frameworkState.getTimeOfDay() == TimeOfDay.DAYTIME) {
			return Optional.of(new StateHeatingOnDaytime(context));
		} else {
			return Optional.of(new StateHeatingOnNighttime(context));
		}
	}
}
