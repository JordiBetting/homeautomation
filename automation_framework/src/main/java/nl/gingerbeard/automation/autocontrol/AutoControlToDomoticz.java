package nl.gingerbeard.automation.autocontrol;

import java.util.List;

import nl.gingerbeard.automation.domoticz.api.DomoticzApi;
import nl.gingerbeard.automation.domoticz.api.DomoticzException;
import nl.gingerbeard.automation.logging.ILogger;
import nl.gingerbeard.automation.state.NextState;

public class AutoControlToDomoticz implements AutoControlListener {

	private final DomoticzApi domoticz;
	private final ILogger log;
	private final ILogger tracelog;

	public AutoControlToDomoticz(final ILogger logger, final DomoticzApi domoticz) {
		log = logger;
		tracelog = logger.createContext("trace");
		this.domoticz = domoticz;
	}

	@Override
	public void outputChanged(final String owner, final List<NextState<?>> output) {
		output.forEach((update) -> {
			try {
				tracelog.info(owner + ": " + update);
				domoticz.transmitDeviceUpdate(update);
			} catch (DomoticzException e) {
				log.warning(e, "Could not transmit update");
			}
		});
	}

}
