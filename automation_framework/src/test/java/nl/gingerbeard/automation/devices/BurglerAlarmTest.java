package nl.gingerbeard.automation.devices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.state.AlarmState;

public class BurglerAlarmTest {

	@Test
	public void update_armaway_succeeds() {
		final BurglarAlarm alarm = new BurglarAlarm(1);
		alarm.setState(AlarmState.DISARMED);

		final boolean result = alarm.updateState("arm_away");

		assertTrue(result);
		assertEquals(AlarmState.ARM_AWAY, alarm.getState());
	}

	@Test
	public void update_armhome_succeeds() {
		final BurglarAlarm alarm = new BurglarAlarm(1);
		alarm.setState(AlarmState.DISARMED);

		final boolean result = alarm.updateState("arm_home");

		assertTrue(result);
		assertEquals(AlarmState.ARM_HOME, alarm.getState());
	}

	@Test
	public void update_disarmed_succeeds() {
		final BurglarAlarm alarm = new BurglarAlarm(1);
		alarm.setState(AlarmState.ARM_AWAY);

		final boolean result = alarm.updateState("disarmed");

		assertTrue(result);
		assertEquals(AlarmState.DISARMED, alarm.getState());
	}

	@Test
	public void update_invalid_returnsFalse() {
		final BurglarAlarm alarm = new BurglarAlarm(1);
		alarm.setState(AlarmState.ARM_AWAY);

		final boolean result = alarm.updateState("invalid");

		assertFalse(result);
	}
}
