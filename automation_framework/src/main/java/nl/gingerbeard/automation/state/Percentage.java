package nl.gingerbeard.automation.state;

import com.google.common.base.Preconditions;

public class Percentage {

	private final int percentage;

	public Percentage(final int percentage) {
		Preconditions.checkArgument(percentage >= 0 && percentage <= 100);
		this.percentage = percentage;
	}

	public int getPercentage() {
		return percentage;
	}

}
