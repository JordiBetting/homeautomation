package nl.gingerbeard.automation;

import java.io.IOException;
import java.util.List;

import nl.gingerbeard.automation.domoticz.transmitter.IDomoticzUpdateTransmitter;
import nl.gingerbeard.automation.logging.ILogger;
import nl.gingerbeard.automation.state.NextState;

public class AutoControlToDomoticz implements AutoControlListener {

	private final IDomoticzUpdateTransmitter transmitter;
	private final ILogger log;
	private final ILogger tracelog;

	public AutoControlToDomoticz(final ILogger logger, final IDomoticzUpdateTransmitter transmitter) {
		log = logger;
		tracelog = logger.createContext("trace");
		this.transmitter = transmitter;
	}

	@Override
	public void outputChanged(final String owner, final List<NextState<?>> output) {
		output.forEach((update) -> {
			try {
				tracelog.info(owner + ": " + update);
				transmitter.transmitDeviceUpdate(update);
			} catch (final IOException e) {
				log.warning(e, "Could not transmit update");
			}
		});
	}

}
