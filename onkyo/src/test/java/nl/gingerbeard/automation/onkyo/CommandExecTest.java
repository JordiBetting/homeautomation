package nl.gingerbeard.automation.onkyo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
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
	
	@Test
	public void exec_readOutput_works() throws IOException {
		CommandExec exec = new CommandExec();
		ByteArrayInputStream stream = new ByteArrayInputStream("hello".getBytes());

		String result = exec.readOutput(stream);

		assertEquals("hello", result);
	}

	@Test
	public void exec_readOutput_noOutput() throws IOException {
		CommandExec exec = new CommandExec();
		ByteArrayInputStream stream = new ByteArrayInputStream(new byte[] {});
		
		String result = exec.readOutput(stream);
		
		assertEquals("", result);
	}
	
}
