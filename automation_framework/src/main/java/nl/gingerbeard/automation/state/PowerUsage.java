package nl.gingerbeard.automation.state;

import com.google.common.base.Preconditions;

public class PowerUsage {

	private final int usageWatt;

	public PowerUsage(final int usageWatt) {
		Preconditions.checkArgument(usageWatt >= 0);
		this.usageWatt = usageWatt;
	}

	public int getUsageWatt() {
		return usageWatt;
	}

}
