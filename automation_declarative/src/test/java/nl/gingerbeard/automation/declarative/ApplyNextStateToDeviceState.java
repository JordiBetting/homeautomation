package nl.gingerbeard.automation.declarative;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import nl.gingerbeard.automation.devices.Device;
import nl.gingerbeard.automation.devices.StateDevice;
import nl.gingerbeard.automation.state.NextState;

//glue-ing the output of NextState to update device to improve test readability
final class ApplyNextStateToDeviceState implements IDeviceUpdate {

	private final Map<Integer, Device<?>> devices = new HashMap<>();
	private Optional<CountDownLatch> latch = Optional.empty();

	public ApplyNextStateToDeviceState(final Device<?>... devices) {
		Arrays.stream(devices).forEach((device) -> this.devices.put(device.getIdx(), device));
	}

	void setLatch(final CountDownLatch latch) {
		this.latch = Optional.ofNullable(latch);
	}

	@Override
	public void updateDevice(final NextState<?> nextState) {
		final StateDevice<?> changedDevice = nextState.getDevice();
		if (devices.containsKey(changedDevice.getIdx())) {
			changedDevice.updateState(nextState.get().toString());
			latch.ifPresent((latch) -> latch.countDown());
		} else {
			fail("Got update of non existent device with idx=" + changedDevice.getIdx() + " : " + changedDevice.toString());
		}
	}
}