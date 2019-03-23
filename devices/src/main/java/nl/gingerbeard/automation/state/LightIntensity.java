package nl.gingerbeard.automation.state;

import com.google.common.base.Preconditions;

public class LightIntensity {
	private final int lux;

	public LightIntensity(final int lux) {
		Preconditions.checkArgument(lux >= 0);
		this.lux = lux;
	}

	public int getLux() {
		return lux;
	}

	@Override
	public String toString() {
		return "LightIntensity [lux=" + lux + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + lux;
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
		if (getClass() != obj.getClass()) {
			return false;
		}
		final LightIntensity other = (LightIntensity) obj;
		if (lux != other.lux) {
			return false;
		}
		return true;
	}

}
