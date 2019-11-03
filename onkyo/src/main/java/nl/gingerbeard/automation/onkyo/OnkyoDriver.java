package nl.gingerbeard.automation.onkyo;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;

import nl.gingerbeard.automation.logging.ILogger;

public class OnkyoDriver {

	private final String onkyoIp;
	private final IExecutor executor;
	private final ILogger log;

	public OnkyoDriver(ILogger log, String onkyoIp) {
		this(log, onkyoIp, new CommandExec());
	}

	OnkyoDriver(ILogger log, String onkyoIp, IExecutor executor) {
		this.onkyoIp = onkyoIp;
		this.executor = executor;
		this.log = log;
	}

	IExecutor getExecutor() {
		return executor;
	}

	public void setAllOff() throws IOException, InterruptedException {
		setMainOff();
		setZone2Off();
	}

	public void setZone2Off() throws IOException, InterruptedException {
		log("zone2", "standby");
		execute("zone2.power=standby");
	}

	private void log(String zone, String state) {
		log.info("Onkyo[" + onkyoIp + "] Switching " + zone + " " + state + ".");
	}

	public void setMainOff() throws IOException, InterruptedException {
		log("main", "off");
		execute("system-power=off");
	}

	public boolean isZone2On() throws IOException, InterruptedException {
		String result = execute("zone2.power=query");
		return getValue(result, "on");
	}

	public boolean isMainOn() throws IOException, InterruptedException {
		String result = execute("system.power=query");
		return getValue(result, "on");
	}

	boolean getValue(String result, String expectedValue) {
		String[] split = result.split("=");
		return (split.length > 1 && split[split.length - 1].toLowerCase().contains(expectedValue.toLowerCase()));
	}

	private String execute(String... command) throws IOException, InterruptedException {
		String[] cli = concatArrays(new String[] { "/usr/local/bin/onkyo", "--host", onkyoIp }, command);
		return executor.execute(cli);
	}

	private String[] concatArrays(String[] a, String[] b) {
		return Stream.concat(Arrays.stream(a), Arrays.stream(b)).toArray(String[]::new);
	}

	public void setZone2On() throws IOException, InterruptedException {
		log("zone2", "on");
		execute("zone2.power=on");
	}

	public void setMainOn() throws IOException, InterruptedException {
		log("main", "on");
		execute("system-power=on");
	}

}
