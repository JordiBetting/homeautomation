package nl.gingerbeard.automation.logging;

public interface ILogOutput {

	void log(LogLevel level, String context, String message);

}
