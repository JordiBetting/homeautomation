package nl.gingerbeard.automation;

import java.io.IOException;
import java.util.List;

import nl.gingerbeard.automation.domoticz.transmitter.IDomoticzUpdateTransmitter;
import nl.gingerbeard.automation.logging.ILogger;
import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.OnOffState;

public class AutoControlToDomoticz implements AutoControlListener {

	private final IDomoticzUpdateTransmitter transmitter;
	private final ILogger logger;

	public AutoControlToDomoticz(final ILogger logger, final IDomoticzUpdateTransmitter transmitter) {
		this.logger = logger;
		this.transmitter = transmitter;
	}

	@Override
	public void outputChanged(final List<NextState<OnOffState>> output) {
		output.forEach((nextState) -> {
			try {
				transmitter.transmitDeviceUpdate(nextState);
			} catch (final IOException e) {
				logger.warning(e, "Could not transmit update");
			}
		});
	}

}
