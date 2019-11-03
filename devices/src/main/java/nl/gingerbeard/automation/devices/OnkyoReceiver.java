package nl.gingerbeard.automation.devices;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import nl.gingerbeard.automation.state.NextState;
import nl.gingerbeard.automation.state.OnOffState;

public class OnkyoReceiver extends CompositeDevice<OnkyoReceiverState> {

	public static abstract class OnkyoSubdevice extends Subdevice<OnkyoReceiver, OnOffState> {
		
	}
	
	private final String host;
	private OnkyoZoneMain main;
	private OnkyoZone2 zone2;

	public OnkyoReceiver(String host) {
		super(Sets.newHashSet(new OnkyoZoneMain(), new OnkyoZone2()));
		this.host = host;
		setState(new OnkyoReceiverState(OnOffState.OFF, OnOffState.OFF));

		for (final Device<?> device : getDevices()) {
			@SuppressWarnings("unchecked")
			final Subdevice<OnkyoReceiver, ?> sub = (Subdevice<OnkyoReceiver, ?>) device;
			sub.setParent(this);
			if (sub instanceof OnkyoZoneMain) {
				main = (OnkyoZoneMain) sub;
			} else {
				zone2 = (OnkyoZone2) sub;
			}
		}
	}

	public String getHost() {
		return host;
	}

	public NextState<OnOffState> createNextStateMain(OnOffState requestedState) {
		return new NextState<OnOffState>(main, requestedState);
	}

	public NextState<OnOffState> createNextStateZone2(OnOffState requestedState) {
		return new NextState<OnOffState>(zone2, requestedState);
	}

	public List<NextState<OnOffState>> createNextStateMainAndZone2(OnOffState requestedState) {
		return Lists.newArrayList( //
				createNextStateMain(requestedState), //
				createNextStateZone2(requestedState) //
		);
	}
}
