package nl.gingerbeard.automation.logging;

import java.util.Optional;

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
