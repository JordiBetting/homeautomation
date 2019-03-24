package nl.gingerbeard.automation.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

public final class Logging implements ILogger {

	private final Optional<ILogOutput> logOutput;
	private final String context;

	public Logging(final Optional<ILogOutput> logOutput) {
		this("root", logOutput);
	}

	private Logging(final String context, final Optional<ILogOutput> logOutput) {
		this.context = context;
		this.logOutput = logOutput;
	}

	@Override
	public void log(final Optional<Throwable> throwable, final LogLevel level, final String message) {
		if (logOutput.isPresent()) {
			logOutput.get().log(level, context, message);
		} else {
			System.out.println("[" + level.name() + "] " + "[" + context + "] " + message);
		}

		if (throwable.isPresent()) {
			log(Optional.empty(), level, throwable.get().getMessage());
			log(Optional.empty(), level, getStackTrace(throwable.get()));
		}
	}

	private String getStackTrace(final Throwable throwable) {
		final StringWriter sw = new StringWriter();
		try (PrintWriter pw = new PrintWriter(sw)) {
			throwable.printStackTrace(pw);
			return sw.toString();
		}
	}

	@Override
	public ILogger createContext(final String context) {
		return new Logging(context, logOutput);
	}

}
