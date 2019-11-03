package nl.gingerbeard.automation.onkyo;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CommandExecTest {

	@Test
	public void execute_noException() throws IOException, InterruptedException {
		IExecutor exec = new CommandExec();
		String[] command = isWindows() ? new String[] {"cmd.exe", "/C", "exit", "0"} : new String[] {"/bin/sh", "-c", "exit", "0" };
		exec.execute(command);
	}
	
	private boolean isWindows() {
		return System.getProperty("os.name").toLowerCase().contains("win");
	}

	@Test
	public void execute_throwsException() throws IOException, InterruptedException {
		IExecutor exec = new CommandExec();
		String[] command = isWindows() ? new String[] {"cmd.exe", "/C", "thisCommandWillProbablyNotExistOnAnySystem"} : new String[] {"/usr/bin/sh", "-c", "thisCommandWillProbablyNotExistOnAnySystem"};
		Assertions.assertThrows(IOException.class, () -> exec.execute(command));
	}
	
	
}
