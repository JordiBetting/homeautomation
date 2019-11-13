package nl.gingerbeard.automation.autocontrol.heatingstates;

import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

import nl.gingerbeard.automation.autocontrol.HeatingAutoControlContext;
import nl.gingerbeard.automation.state.AlarmState;
import nl.gingerbeard.automation.state.Temperature;

public final class StateHeatingOnDelay extends HeatingState {
	private final Timer timer = new Timer();
	private HeatingAutoControlContext context;

	StateHeatingOnDelay(HeatingAutoControlContext context) {
		this.context = context;
		if (context.delayOnMillis > 0) {
			context.getLogger().info("Scheduling heating on for " + context.getOwner() + " in " + context.delayOnMillis + "ms.");
			timer.schedule(new TimerTick(), context.delayOnMillis);
		}
	}

	private class TimerTick extends TimerTask {
		@Override
		public void run() {
			synchronized (StateHeatingOnDelay.this) {
				context.changeStateAsync(Util.createNextOnStateBasedOnDaytime(context));
			}
		}
	}

	@Override
	public Optional<Temperature> stateEntryResult() {
		if (context.delayOnMillis > 0) {
			return Optional.of(context.offTemperature);
		}
		return super.stateEntryResult();
	}

	@Override
	public synchronized Optional<HeatingState> alarmChanged() {
		if (!context.frameworkState.getAlarmState().meets(AlarmState.DISARMED)) {
			timer.cancel();
			return Optional.of(new StateHeatingOff(context));
		}
		return super.alarmChanged();
	}

	@Override
	public Optional<HeatingState> stateEntryNextState() {
		if (context.delayOnMillis == 0) {
			return Util.createNextOnStateBasedOnDaytime(context);
		}
		return super.stateEntryNextState();
	}

}
