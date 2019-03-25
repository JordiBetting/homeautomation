package nl.gingerbeard.automation.autocontrol;

import java.util.List;

import nl.gingerbeard.automation.state.NextState;

public interface AutoControlListener {
	void outputChanged(String owner, List<NextState<?>> output);
}
