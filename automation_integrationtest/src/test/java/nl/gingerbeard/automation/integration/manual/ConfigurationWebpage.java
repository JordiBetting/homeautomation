package nl.gingerbeard.automation.integration.manual;

import java.io.IOException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.Room;
import nl.gingerbeard.automation.event.annotations.Subscribe;
import nl.gingerbeard.automation.integration.IntegrationTest;

@Disabled
public class ConfigurationWebpage extends IntegrationTest {

	public static class Room1 extends Room {

		@Subscribe
		public void feedMe(final String now) {

		}

	}

	public static class Room2 extends Room {

		@Subscribe
		public void feedMe(final String now) {

		}

	}

	@Test
	public void hostWebpage() throws InterruptedException, IOException {
		final int duration = 30;

		start(Room1.class, Room2.class);

		automation.getRoom(Room1.class);
		automation.getRoom(Room2.class);
		System.out.println(
				"configuration page reachable on http://localhost:" + configPort + "/static/configuration.html");
		System.out.println("will automatically stop in " + duration + " seconds.");

		Thread.sleep(duration * 1000);
	}

}
