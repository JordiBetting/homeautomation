package nl.gingerbeard.automation.onkyo;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;

public class Onkyo {

	private String onkyoIp;
	private IExecutor executor;

	public Onkyo(String onkyoIp) {
		this(onkyoIp, new CommandExec());
	}
	
	Onkyo(String onkyoIp, IExecutor executor) {
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
	
	private void execute(String ...command) throws IOException, InterruptedException {
		String[] cli = concatArrays(new String[] { "/usr/local/bin/onkyo", "--host", onkyoIp }, command);
		executor.execute(cli);
	}

	private String[] concatArrays(String[] a, String[] b) {
		return Stream.concat(Arrays.stream(a), Arrays.stream(b)).toArray(String[]::new);
	}
	
	
}
