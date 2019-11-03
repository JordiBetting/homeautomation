package nl.gingerbeard.automation.devices;

import java.util.Optional;

public abstract class Subdevice<ParentType extends CompositeDevice<?>, StateType> extends Device<StateType> {
	protected Optional<ParentType> parent = Optional.empty();

	protected Subdevice(final int idx) {
		super(idx);
	}
	
	protected Subdevice() {
		super();
	}

	final void setParent(final ParentType parent) {
		this.parent = Optional.ofNullable(parent);
	}

	public Optional<ParentType> getParent() {
		return parent;
	}

}