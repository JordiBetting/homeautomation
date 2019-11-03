package nl.gingerbeard.automation.autocontrol.logging;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;

import nl.gingerbeard.automation.autocontrol.AutoControl;
import nl.gingerbeard.automation.devices.IDevice;
import nl.gingerbeard.automation.event.annotations.Subscribe;
import nl.gingerbeard.automation.logging.LogLevel;
import nl.gingerbeard.automation.logging.TestLogger;
import nl.gingerbeard.automation.state.State;
import nl.gingerbeard.automation.state.TimeOfDay;

public class AutoControlLoggingTest {

	public static class LoggingAutoControl extends AutoControl {

		@Override
		public List<IDevice<?>> getDevices() {
			return Lists.newArrayList();
		}
		
		
		@Subscribe
		public void trigger(TimeOfDay _void) {
			getLogger().debug("Hello World!");
		}
	}
	
	
	@Test
	public void trigger_autoControl_Logs() {
		TestLogger log = new TestLogger();
		LoggingAutoControl control = new LoggingAutoControl();
		control.init(null, new State(), log);
		
		control.trigger(null);
		
		log.assertContains(LogLevel.DEBUG, "Hello World!");
	}
}
