package nl.gingerbeard.automation.autocontrol;

import java.util.List;
import java.util.Optional;

import nl.gingerbeard.automation.autocontrol.heatingstates.HeatingState;
import nl.gingerbeard.automation.state.IState;
import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.Temperature;

public final class HeatingAutoControlContext {
	public IState frameworkState;
	public Temperature offTemperature = Temperature.celcius(HeatingAutoControl.DEFAULT_TEMP_C_OFF);
	public Temperature daytimeTemperature = Temperature.celcius(HeatingAutoControl.DEFAULT_TEMP_C_DAY);
	public Temperature nighttimeTemperature = Temperature.celcius(HeatingAutoControl.DEFAULT_TEMP_C_NIGHT);
	public long delayOnMillis = 0;
	public long delayPauseMillis;
	
	private HeatingAutoControl autoControl;

	public HeatingAutoControlContext(HeatingAutoControl autoControl) {
		this.autoControl = autoControl;
	}

	public void changeStateAsync(Optional<HeatingState> newState) {
		List<NextState<?>> result = autoControl.changeState(newState);
		autoControl.asyncOutput(result);
	}
	
}
