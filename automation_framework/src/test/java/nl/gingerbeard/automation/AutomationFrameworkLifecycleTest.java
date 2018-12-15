package nl.gingerbeard.automation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class AutomationFrameworkLifecycleTest {

	@Test
	public void state_initial() throws IOException {
		final AutomationFramework framework = AutomationFramework.create();

		assertEquals(AutomationFrameworkState.INITIALIZING, framework.getFrameworkState());
	}

	@Test
	public void state_start_running() throws IOException {
		final AutomationFramework framework = AutomationFramework.create();

		framework.start();

		assertEquals(AutomationFrameworkState.RUNNING, framework.getFrameworkState());
	}

	@Test
	public void state_startTwice_throwsException() throws IOException {
		final AutomationFramework framework = AutomationFramework.create();

		framework.start();
		try {
			framework.start();
			fail("Expected exception");
		} catch (final IllegalStateException e) {
			// expected
		}
	}

	@Test
	public void state_stop_stopped() throws IOException {
		final AutomationFramework framework = AutomationFramework.create();
		framework.start();

		framework.stop();

		assertEquals(AutomationFrameworkState.STOPPED, framework.getFrameworkState());
	}

	@Test
	public void state_stopTwice_throwsException() throws IOException {
		final AutomationFramework framework = AutomationFramework.create();
		framework.start();

		framework.stop();
		try {
			framework.stop();
			fail("Expected exception");
		} catch (final IllegalStateException e) {
			// expected
		}
	}

	@Test
	public void intializing_addroom_noException() throws IOException {
		final AutomationFramework framework = AutomationFramework.create();
		framework.addRoom(new Room());
	}

	@Test
	public void running_addroom_exception() throws IOException {
		final AutomationFramework framework = AutomationFramework.create();
		framework.start();

		try {
			framework.addRoom(new Room());
			fail("Expected exception");
		} catch (final IllegalStateException e) {
			// expected
		}
	}

	@Test
	public void stopped_addroom_exception() throws IOException {
		final AutomationFramework framework = AutomationFramework.create();
		framework.start();
		framework.stop();

		try {
			framework.addRoom(new Room());
			fail("Expected exception");
		} catch (final IllegalStateException e) {
			// expected
		}
	}

}
