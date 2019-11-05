package nl.gingerbeard.automation.logging;

import java.time.LocalDateTime;

public interface ILogOutput {

	void log(LocalDateTime time, LogLevel level, String context, String message);

}
