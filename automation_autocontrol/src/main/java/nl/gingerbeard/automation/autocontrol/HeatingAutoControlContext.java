package nl.gingerbeard.automation.autocontrol;

import java.util.List;
import java.util.Optional;

import nl.gingerbeard.automation.autocontrol.heatingstates.HeatingState;
import nl.gingerbeard.automation.logging.ILogger;
import nl.gingerbeard.automation.state.IState;
import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.Temperature;

public final class HeatingAutoControlContext {
	public final IState frameworkState;
	public Temperature offTemperature = Temperature.celcius(HeatingAutoControl.DEFAULT_TEMP_C_OFF);
	public Temperature daytimeTemperature = Temperature.celcius(HeatingAutoControl.DEFAULT_TEMP_C_DAY);
	public Temperature nighttimeTemperature = Temperature.celcius(HeatingAutoControl.DEFAULT_TEMP_C_NIGHT);
	public long delayOnMillis = 0;
	public long delayPauseMillis;
	
	private final HeatingAutoControl autoControl;
	private final ILogger log;

	public HeatingAutoControlContext(HeatingAutoControl autoControl, IState iState, ILogger iLogger) {
		this.autoControl = autoControl;
		this.log = iLogger;
		this.frameworkState = iState;
	}

	public void changeStateAsync(Optional<HeatingState> newState) {
		List<NextState<?>> result = autoControl.changeState(newState);
		autoControl.asyncOutput(result);
	}

	public ILogger getLogger() {
		return log;
	}

	public String getOwner() {
		return autoControl.getOwner();
	}
	
}
