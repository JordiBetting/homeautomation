package nl.gingerbeard.automation.autocontrol.heating.states;

import java.util.Optional;

import nl.gingerbeard.automation.autocontrol.heating.HeatingAutoControlContext;
import nl.gingerbeard.automation.state.Temperature;

public final class StateHeatingOnNighttime extends StateHeatingOn {

	public StateHeatingOnNighttime(HeatingAutoControlContext context) {
		super(context);
	}

	@Override
	public Optional<Temperature> stateEntryResult() {
		return Optional.of(context.nighttimeTemperature);
	}

	@Override
	public Optional<HeatingState> timeOfDayChanged() {
		return Optional.of(new StateHeatingOnDaytime(context));
	}

}
