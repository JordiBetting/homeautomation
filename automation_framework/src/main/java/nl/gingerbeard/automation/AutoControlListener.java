package nl.gingerbeard.automation;

import java.util.List;

import nl.gingerbeard.automation.state.NextState;

public interface AutoControlListener {
	void outputChanged(List<NextState<?>> output);
}
