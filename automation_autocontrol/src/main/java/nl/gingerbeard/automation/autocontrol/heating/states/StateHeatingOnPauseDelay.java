package nl.gingerbeard.automation.autocontrol.heating.states;

import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

import nl.gingerbeard.automation.autocontrol.heating.HeatingAutoControlContext;
import nl.gingerbeard.automation.state.AlarmState;

public final class StateHeatingOnPauseDelay extends HeatingState {
	private final Timer timer = new Timer();
	private HeatingAutoControlContext context;
	
	StateHeatingOnPauseDelay(HeatingAutoControlContext context){
		this.context = context;
		if (context.delayPauseMillis > 0) {
			timer.schedule(new TimerTick(), context.delayPauseMillis);
		}
	}

	private class TimerTick extends TimerTask {
		@Override
		public void run() {
			synchronized (StateHeatingOnPauseDelay.this) {
				context.changeStateAsync(Optional.of(new StateHeatingPaused(context)));
			}
		}
	}

	@Override
	public Optional<HeatingState> stateEntryNextState() {
		if (context.delayPauseMillis == 0) {
			return Optional.of(new StateHeatingPaused(context));
		}
		return super.stateEntryNextState();
	}

	@Override
	public synchronized Optional<HeatingState> alarmChanged() {
		timer.cancel();
		if (!context.frameworkState.getAlarmState().meets(AlarmState.DISARMED)) {
			return Optional.of(new StateHeatingOff(context));
		}
		return super.alarmChanged();
	}

	@Override
	public synchronized Optional<HeatingState> allPauseDevicesOff() {
		timer.cancel();
		return Util.createNextOnStateBasedOnDaytime(context);
	}
	
}