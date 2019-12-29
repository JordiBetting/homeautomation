package nl.gingerbeard.automation.autocontrol.heating.states;

import java.util.Optional;

import nl.gingerbeard.automation.autocontrol.heating.HeatingAutoControlContext;
import nl.gingerbeard.automation.state.Temperature;

public final class StateHeatingOnDaytime extends StateHeatingOn {

	public StateHeatingOnDaytime(HeatingAutoControlContext context) {
		super(context);
	}

	@Override
	public Optional<Temperature> stateEntryResult() {
		return Optional.of(context.daytimeTemperature);
	}

	@Override
	public Optional<HeatingState> timeOfDayChanged() {
		return Optional.of(new StateHeatingOnNighttime(context));
	}

}
