package nl.gingerbeard.automation.onkyo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OnkyoDriverTest {

	private RecordingCommandExec exec;
	private OnkyoDriver onkyo;

	public static final class RecordingCommandExec implements IExecutor {

		private List<String> commands = new ArrayList<>();
		private Optional<String> answer = Optional.empty();

		@Override
		public String execute(String... command) throws IOException, InterruptedException {
			this.commands.add(toOneline(command));
			return answer.orElse("");
		}

		private String toOneline(String... command) {
			String concat = Arrays.toString(command);
			return concat.substring(1, concat.length() - 1).replace(",", "");
		}

		public List<String> getCommands() {
			return commands;
		}

		public void setAnswer(String answer) {
			this.answer = Optional.of(answer);
		}

	}

	@Test
	public void publicConstructorUsesCommandExec() {
		OnkyoDriver onkyo = new OnkyoDriver("");
		assertEquals(CommandExec.class, onkyo.getExecutor().getClass());
	}
	
	@BeforeEach
	public void createOnkyoWithRecordingExec() {
		exec = new RecordingCommandExec();
		onkyo = new OnkyoDriver("1.2.3.4", exec);
	}

	@Test
	public void mainOff() throws IOException, InterruptedException {
		onkyo.setMainOff();

		assertCommandsExecuted(exec, "/usr/local/bin/onkyo --host 1.2.3.4 system-power=off");
	}

	@Test
	public void ipSettingUsed() throws IOException, InterruptedException {
		OnkyoDriver onkyo = new OnkyoDriver("42.88.66.1", exec);

		onkyo.setMainOff();

		assertCommandsExecuted(exec, "/usr/local/bin/onkyo --host 42.88.66.1 system-power=off");
	}

	@Test
	public void zone2Off() throws IOException, InterruptedException {
		onkyo.setZone2Off();

		assertCommandsExecuted(exec, "/usr/local/bin/onkyo --host 1.2.3.4 zone2.power=off");
	}

	@Test
	public void allOff() throws IOException, InterruptedException {
		onkyo.setAllOff();

		assertCommandsExecuted(exec, "/usr/local/bin/onkyo --host 1.2.3.4 system-power=off",
				"/usr/local/bin/onkyo --host 1.2.3.4 zone2.power=off");
	}

	@Test
	public void mainOn() throws IOException, InterruptedException {
		onkyo.setMainOn();
		
		assertCommandsExecuted(exec, "/usr/local/bin/onkyo --host 1.2.3.4 system-power=on");
	}
	
	@Test
	public void zone2On() throws IOException, InterruptedException {
		onkyo.setZone2On();
		
		assertCommandsExecuted(exec, "/usr/local/bin/onkyo --host 1.2.3.4 zone2.power=on");
	}

	@Test
	public void isZone2On() throws IOException, InterruptedException {
		exec.setAnswer("HT-R993: power = on");
		
		assertTrue(onkyo.isZone2On());
	}
	
	@Test
	public void isZone2Off() throws IOException, InterruptedException {
		exec.setAnswer("HT-R993: power = standby");
		
		assertFalse(onkyo.isZone2On());
	}
	
	@Test
	public void isMainOn() throws IOException, InterruptedException {
		exec.setAnswer("HT-R993: power = on");
		
		assertTrue(onkyo.isMainOn());
	}
	
	@Test
	public void isMainOff() throws IOException, InterruptedException {
		exec.setAnswer("HT-R993: power = standby");
		
		assertFalse(onkyo.isMainOn());
	}

	private void assertCommandsExecuted(RecordingCommandExec exec, String... expectedCommands) {
		List<String> executedCommands = exec.getCommands();
		assertEquals(expectedCommands.length, executedCommands.size());
		for (int i = 0; i < expectedCommands.length; ++i) {
			assertEquals(expectedCommands[i], executedCommands.get(i));
		}
	}
	
	@Test
	public void getValue_expectedValue_returnsTrue() {
		boolean result = onkyo.getValue("ABCD key=value", "value");
		
		assertTrue(result);
	}
	
	@Test
	public void getValue_otherValue_returnsFalse() {
		boolean result = onkyo.getValue("ABCD key=blaat", "value");
		
		assertFalse(result);
	}
	
	@Test
	public void getValue_caseInsensitive_returnsFalse() {
		boolean result = onkyo.getValue("ABCD key=blaAt", "BlaaT");
		
		assertTrue(result);
	}
	
	@Test
	public void getValue_unexpectedFormat_returnsFalse() {
		boolean result = onkyo.getValue("noEqualsSign", "yes");
		
		assertFalse(result);
	}
	
	
	

}
