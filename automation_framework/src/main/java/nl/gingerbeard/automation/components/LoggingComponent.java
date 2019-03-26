package nl.gingerbeard.automation.components;

import java.util.Optional;

import nl.gingerbeard.automation.logging.ILogOutput;
import nl.gingerbeard.automation.logging.ILogger;
import nl.gingerbeard.automation.logging.Logging;
import nl.gingerbeard.automation.service.annotation.Activate;
import nl.gingerbeard.automation.service.annotation.Provides;
import nl.gingerbeard.automation.service.annotation.Requires;

public class LoggingComponent {

	@Provides
	public ILogger logInput;

	@Requires
	public Optional<ILogOutput> logOutput = Optional.empty();

	private Logging impl;

	@Activate
	public void createComponent() {
		impl = new Logging(logOutput);
		logInput = impl;
	}

}
