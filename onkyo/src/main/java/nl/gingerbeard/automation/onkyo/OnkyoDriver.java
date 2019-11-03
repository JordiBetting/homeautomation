package nl.gingerbeard.automation.onkyo;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;

public class OnkyoDriver {

	private String onkyoIp;
	private IExecutor executor;

	public OnkyoDriver(String onkyoIp) {
		this(onkyoIp, new CommandExec());
	}
	
	OnkyoDriver(String onkyoIp, IExecutor executor) {
		this.onkyoIp = onkyoIp;
		this.executor = executor;
	}
	
	IExecutor getExecutor() {
		return executor;
	}
	
	public void setAllOff() throws IOException, InterruptedException {
		setMainOff();
		setZone2Off();
	}

	public void setZone2Off() throws IOException, InterruptedException {
		execute("zone2.power=off");
	}

	public void setMainOff() throws IOException, InterruptedException {
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
		return (split.length > 1 && split[split.length-1].toLowerCase().contains(expectedValue.toLowerCase()));
	}
	
	private String execute(String ...command) throws IOException, InterruptedException {
		String[] cli = concatArrays(new String[] { "/usr/local/bin/onkyo", "--host", onkyoIp }, command);
		return executor.execute(cli);
	}

	private String[] concatArrays(String[] a, String[] b) {
		return Stream.concat(Arrays.stream(a), Arrays.stream(b)).toArray(String[]::new);
	}

	public void setZone2On() throws IOException, InterruptedException {
		execute("zone2.power=on");
	}

	public void setMainOn() throws IOException, InterruptedException {
		execute("zone2.power=on");
	}
	
	
}
