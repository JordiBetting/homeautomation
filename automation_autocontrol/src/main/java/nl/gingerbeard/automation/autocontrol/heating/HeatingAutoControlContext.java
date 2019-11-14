package nl.gingerbeard.automation.autocontrol.heating;

import java.util.Optional;

import nl.gingerbeard.automation.autocontrol.HeatingAutoControl;
import nl.gingerbeard.automation.autocontrol.heating.states.HeatingState;
import nl.gingerbeard.automation.logging.ILogger;
import nl.gingerbeard.automation.state.IState;
import nl.gingerbeard.automation.state.Temperature;

public final class HeatingAutoControlContext {
	
	// configuration
	public Temperature offTemperature = Temperature.celcius(HeatingAutoControl.DEFAULT_TEMP_C_OFF);
	public Temperature daytimeTemperature = Temperature.celcius(HeatingAutoControl.DEFAULT_TEMP_C_DAY);
	public Temperature nighttimeTemperature = Temperature.celcius(HeatingAutoControl.DEFAULT_TEMP_C_NIGHT);
	public long delayOnMillis = 0;
	public long delayPauseMillis;

	// internals
	private final IHeatingAutoControlStateControl autoControl;
	public IState frameworkState;
	private ILogger log;
	private String owner;

	public HeatingAutoControlContext(IHeatingAutoControlStateControl autoControl, String owner) {
		this.autoControl = autoControl;
		this.owner = owner;
	}

	public void changeStateAsync(Optional<HeatingState> newState) {
		autoControl.changeStateAsync(newState);
	}

	public ILogger getLogger() {
		return log;
	}

	public String getOwner() {
		return owner;
	}

	public void configure(IState state, ILogger logger) {
		this.log = logger;
		this.frameworkState = state;
	}

}
