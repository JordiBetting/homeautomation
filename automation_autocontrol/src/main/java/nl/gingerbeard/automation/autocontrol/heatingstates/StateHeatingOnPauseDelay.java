package nl.gingerbeard.automation.autocontrol.heatingstates;

import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

import nl.gingerbeard.automation.autocontrol.HeatingAutoControlContext;

public class StateHeatingOnPauseDelay extends StateHeatingOn {
	private final Timer timer = new Timer();
	
	StateHeatingOnPauseDelay(HeatingAutoControlContext context){
		super(context);
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
		return super.alarmChanged();
	}

	@Override
	public
	synchronized Optional<HeatingState> allPauseDevicesOff() {
		timer.cancel();
		return Util.createNextOnStateBasedOnDaytime(context);
	}
}