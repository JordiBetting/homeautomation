package nl.gingerbeard.automation.state;

import com.google.common.base.Preconditions;

public final class PowerUsage {

	private final int usageWatt;

	public PowerUsage(final int usageWatt) {
		Preconditions.checkArgument(usageWatt >= 0);
		this.usageWatt = usageWatt;
	}

	public int getUsageWatt() {
		return usageWatt;
	}

	@Override
	public String toString() {
		return "PowerUsage [usageWatt=" + usageWatt + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + usageWatt;
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PowerUsage)) {
			return false;
		}
		final PowerUsage other = (PowerUsage) obj;
		if (usageWatt != other.usageWatt) {
			return false;
		}
		return true;
	}

}
