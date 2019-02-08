package nl.gingerbeard.automation.declarative;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import nl.gingerbeard.automation.devices.Device;

final class Scheduler {

	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private final Map<Device<?>, SchedulerAction> scheduledTasks = new HashMap<>();
	private Duration duration;

	private static class SchedulerAction {
		ScheduledFuture<?> task;
		List<Action<?>> actions;

		SchedulerAction(final ScheduledFuture<?> task, final List<Action<?>> actions) {
			this.task = task;
			this.actions = actions;
		}

		public ScheduledFuture<?> getScheduledTask() {
			return task;
		}

		public void executeActions() {
			actions.stream().forEach((action) -> action.execute());
		}

	}

	void schedule(final Device<?> device, final List<Action<?>> actions) {
		final ScheduledFuture<?> task = scheduler.schedule(() -> {
			execute(device);
		}, duration.toMillis(), TimeUnit.MILLISECONDS);
		scheduledTasks.put(device, new SchedulerAction(task, actions));
	}

	private void execute(final Device<?> device) {
		final SchedulerAction action = scheduledTasks.get(device);
		scheduledTasks.remove(device);
		action.executeActions();
	}

	void cancel(final Device<?> device) {
		final SchedulerAction task = scheduledTasks.get(device);
		if (task != null) {
			task.getScheduledTask().cancel(false);
			scheduledTasks.remove(device);
		}
	}

	public void setDuration(final Duration duration) {
		this.duration = duration;
	}
}
