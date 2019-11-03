package nl.gingerbeard.automation.onkyo;

import java.io.IOException;

public interface IExecutor {
void execute(String ...command) throws IOException, InterruptedException;
}
