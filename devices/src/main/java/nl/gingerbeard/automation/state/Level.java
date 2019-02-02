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

}
