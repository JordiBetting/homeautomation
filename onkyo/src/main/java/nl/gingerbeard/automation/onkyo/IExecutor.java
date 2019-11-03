package nl.gingerbeard.automation.onkyo;

import java.io.IOException;

public interface IExecutor {
	String execute(String ...command) throws IOException, InterruptedException;
}
