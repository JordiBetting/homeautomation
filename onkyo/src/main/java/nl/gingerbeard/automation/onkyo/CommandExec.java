package nl.gingerbeard.automation.onkyo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class CommandExec implements IExecutor {

	@Override
	public String execute(String... commandWithArgs) throws IOException, InterruptedException {
		ProcessBuilder builder = new ProcessBuilder(commandWithArgs);
		Process process = builder.start();
		int exitcode = process.waitFor();
		if (exitcode != 0) {
			throw new IOException(
					"Failure, command '" + String.join(" ", commandWithArgs) + "' returned exitcode: " + exitcode);
		}

		return readOutput(process.getInputStream());
	}

	String readOutput(InputStream stdOut) {
		try (@SuppressWarnings("resource") // useDelimiter() returns same object, thus scanner is closed
		Scanner scanner = new Scanner(stdOut).useDelimiter("\\A")) {
			return scanner.hasNext() ? scanner.next() : "";
		}
	}

}
