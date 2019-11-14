package nl.gingerbeard.automation.autocontrol.heating;

import java.util.Optional;

import nl.gingerbeard.automation.autocontrol.heating.states.HeatingState;

public interface IHeatingAutoControlStateControl {
	void changeStateAsync(Optional<HeatingState> newState);
}