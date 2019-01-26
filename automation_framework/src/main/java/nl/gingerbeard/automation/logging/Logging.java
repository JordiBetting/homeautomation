package nl.gingerbeard.automation.logging;

import java.util.Optional;

public class Logging implements ILogger {

	private final Optional<ILogOutput> logOutput;

	public Logging(final Optional<ILogOutput> logOutput) {
		this.logOutput = logOutput;
	}

	@Override
	public void log(final LogLevel level, final String message) {
		if (logOutput.isPresent()) {
			logOutput.get().log(level, message);
		} else {
			System.out.println("[" + level.name() + "] " + message);
		}
	}

}
