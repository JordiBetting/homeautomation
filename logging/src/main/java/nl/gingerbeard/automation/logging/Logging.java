package nl.gingerbeard.automation.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.Optional;

public final class Logging implements ILogger {

	private static final String ROOT_CONTEXT = "root";
	private final Optional<ILogOutput> logOutput;
	private final Optional<LocalDateTime> fixedTime;
	private final String context;

	public Logging(final Optional<ILogOutput> logOutput) {
		this(ROOT_CONTEXT, logOutput);
	}

	private Logging(final String context, final Optional<ILogOutput> logOutput) {
		this.context = context;
		this.logOutput = logOutput;
		fixedTime = Optional.empty();
	}

	Logging(Optional<ILogOutput> logOutput, LocalDateTime fixedTime) {
		context = ROOT_CONTEXT;
		this.logOutput = logOutput;
		this.fixedTime = Optional.of(fixedTime);
	}

	@Override
	public void log(final Optional<Throwable> throwable, final LogLevel level, final String message) {
		LocalDateTime time = fixedTime.orElse(LocalDateTime.now());
		if (logOutput.isPresent()) {
			logOutput.get().log(time, level, context, message);
		} else {
			System.out.println( time.toString() + " [" + level.name() + "] " + "[" + context + "] " + message);
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
