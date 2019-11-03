package nl.gingerbeard.automation.onkyo;

import java.io.IOException;

public class CommandExec implements IExecutor {
 
	@Override
	public void execute(String ... commandWithArgs) throws IOException, InterruptedException {
		ProcessBuilder builder = new ProcessBuilder(commandWithArgs);
		Process process = builder.start();
		int exitcode = process.waitFor();
		if (exitcode != 0) {
			throw new IOException("Failure, command '"+String.join(" ", commandWithArgs)+"' returned exitcode: " + exitcode);  
		}
	}
	
}
