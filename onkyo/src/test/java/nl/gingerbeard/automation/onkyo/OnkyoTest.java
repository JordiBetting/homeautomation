package nl.gingerbeard.automation.onkyo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;


public class OnkyoTest {

	public static final class RecordingCommandExec implements IExecutor {

		private List<String> commands = new ArrayList<>();
		
		@Override
		public void execute(String... command) throws IOException, InterruptedException {
			this.commands.add(toOneline(command));
		}

		private String toOneline(String... command) {
			String concat = Arrays.toString(command);
			return concat.substring(1, concat.length()-1).replace(",", "");
		}

		public List<String> getCommands() {
			return commands;
		}
		
	}
	
	@Test
	public void publicConstructorUsesCommandExec() {
		Onkyo onkyo = new Onkyo("");
		assertEquals(CommandExec.class, onkyo.getExecutor().getClass());
	}
	
	@Test
	public void mainOff() throws IOException, InterruptedException {
		RecordingCommandExec exec = new RecordingCommandExec();
		Onkyo onkyo = new Onkyo("1.2.3.4", exec);
		
		onkyo.setMainOff();
		
		assertCommandsExecuted(exec, "/usr/local/bin/onkyo --host 1.2.3.4 system-power=off");
	}
	
	@Test
	public void ipSettingUsed() throws IOException, InterruptedException {
		RecordingCommandExec exec = new RecordingCommandExec();
		Onkyo onkyo = new Onkyo("42.88.66.1", exec);
		
		onkyo.setMainOff();
		
		assertCommandsExecuted(exec, "/usr/local/bin/onkyo --host 42.88.66.1 system-power=off");
	}
	
	@Test
	public void zone2Off() throws IOException, InterruptedException {
		RecordingCommandExec exec = new RecordingCommandExec();
		Onkyo onkyo = new Onkyo("1.2.3.4", exec);
		
		onkyo.setZone2Off();
		
		assertCommandsExecuted(exec, "/usr/local/bin/onkyo --host 1.2.3.4 zone2.power=off");
	}
	
	@Test
	public void allOff() throws IOException, InterruptedException {
		RecordingCommandExec exec = new RecordingCommandExec();
		Onkyo onkyo = new Onkyo("1.2.3.4", exec);
		
		onkyo.setAllOff();
		
		assertCommandsExecuted(exec, "/usr/local/bin/onkyo --host 1.2.3.4 system-power=off", "/usr/local/bin/onkyo --host 1.2.3.4 zone2.power=off");
	}
	

	private void assertCommandsExecuted(RecordingCommandExec exec, String ... expectedCommands) {
		List<String> executedCommands = exec.getCommands();
		assertEquals(expectedCommands.length, executedCommands.size());
		for (int i = 0; i < expectedCommands.length; ++i) {
			assertEquals(expectedCommands[i], executedCommands.get(i));
		}
	}

}
