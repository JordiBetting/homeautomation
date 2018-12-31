package nl.gingerbeard.automation.devices;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import nl.gingerbeard.automation.state.AlarmState;

public class BurglerAlarmTest {

	@Test
	public void update_armaway_succeeds() {
		final BurglarAlarm alarm = new BurglarAlarm(1);
		alarm.setState(AlarmState.DISARMED);

		alarm.updateState("arm_away");

		assertEquals(AlarmState.ARM_AWAY, alarm.getState());
	}

	@Test
	public void update_armhome_succeeds() {
		final BurglarAlarm alarm = new BurglarAlarm(1);
		alarm.setState(AlarmState.DISARMED);

		alarm.updateState("arm_home");

		assertEquals(AlarmState.ARM_HOME, alarm.getState());
	}

	@Test
	public void update_disarmed_succeeds() {
		final BurglarAlarm alarm = new BurglarAlarm(1);
		alarm.setState(AlarmState.ARM_AWAY);

		alarm.updateState("disarmed");

		assertEquals(AlarmState.DISARMED, alarm.getState());
	}
}
