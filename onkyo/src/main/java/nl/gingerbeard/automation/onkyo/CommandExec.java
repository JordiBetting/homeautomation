package nl.gingerbeard.automation.onkyo;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import com.google.common.io.CharStreams;

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

	String readOutput(InputStream stdOut) throws IOException {
		try (InputStreamReader reader = new InputStreamReader(stdOut, Charset.defaultCharset())) {
			return CharStreams.toString(reader);
		}
	}

}
