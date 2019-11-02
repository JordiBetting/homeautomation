package nl.gingerbeard.automation.autocontrol.heatingstates;

import java.util.Optional;

import nl.gingerbeard.automation.autocontrol.HeatingAutoControlContext;
import nl.gingerbeard.automation.state.TimeOfDay;

final class Util {
	private Util() {
		
	}
	
	static Optional<HeatingState> createNextOnStateBasedOnDaytime(HeatingAutoControlContext context) {
		if (context.frameworkState.getTimeOfDay() == TimeOfDay.DAYTIME) {
			return Optional.of(new StateHeatingOnDaytime(context));
		} else {
			return Optional.of(new StateHeatingOnNighttime(context));
		}
	}
}
