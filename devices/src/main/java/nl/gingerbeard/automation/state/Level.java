package nl.gingerbeard.automation.state;

import com.google.common.base.Preconditions;

public class Level {

	private final int level;

	public Level(final int level) {
		Preconditions.checkArgument(level >= 0 && level <= 100);
		this.level = level;
	}

	public int getLevel() {
		return level;
	}

	@Override
	public String toString() {
		return "Level [level=" + level + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + level;
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
		final Level other = (Level) obj;
		if (level != other.level) {
			return false;
		}
		return true;
	}

}
