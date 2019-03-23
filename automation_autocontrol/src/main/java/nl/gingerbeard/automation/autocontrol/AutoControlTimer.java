package nl.gingerbeard.automation.autocontrol;

import java.util.Timer;
import java.util.TimerTask;

import com.google.common.base.Preconditions;

public class AutoControlTimer {

	private final Object taskLock = new Object();
	private volatile Timer timer = new Timer();
	private volatile TimerTask activeTask;
	private volatile boolean running = true;;

	private class RunnableTimerTask extends TimerTask {

		private final Runnable task;

		public RunnableTimerTask(final Runnable task) {
			this.task = task;
		}

		@Override
		public void run() {
			task.run();
			cancelTask();
		}

	}

	/**
	 * Timer that schedules at most a single task to be executed. When task execution is requested and another execution has not started yet, previous requested task execution will not start, Task is
	 * executed on a separate thread when delayMs > 0.
	 *
	 *
	 * @param task
	 *            The task to execute
	 * @param delayMs
	 *            The delay before execution
	 * @throws IllegalStateException
	 *             when called after {@link #stop()} has been called
	 */
	public void executeDelayed(final Runnable task, final long delayMs) {
		synchronized (taskLock) {
			Preconditions.checkState(running);

			cancelTask();
			if (delayMs == 0) {
				executeNow(task);
			} else {
				scheduleTaskDelayed(task, delayMs);
			}
		}
	}

	private void executeNow(final Runnable task) {
		task.run();
	}

	private void scheduleTaskDelayed(final Runnable task, final long delayMs) {
		activeTask = new RunnableTimerTask(task);
		timer.schedule(activeTask, delayMs);
	}

	/**
	 * Do not start new scheduled task. Makes instance of {@link AutoControlTimer} unusable as newly scheduled tasks will be rejcted.
	 */
	public void stop() {
		synchronized (taskLock) {
			timer.cancel();
			activeTask = null;
			running = false;
		}
	}

	/**
	 * If a task is not executed yet, it will not start after calling this method.
	 */
	public void cancelTask() {
		synchronized (taskLock) {
			if (activeTask != null) {
				activeTask.cancel();
				activeTask = null;
			}
		}
	}

}
