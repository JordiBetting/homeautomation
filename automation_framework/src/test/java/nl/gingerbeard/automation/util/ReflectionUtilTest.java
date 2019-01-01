package nl.gingerbeard.automation.util;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.google.common.collect.Iterables;

import nl.gingerbeard.automation.util.ReflectionUtil.ReflectionException;

public class ReflectionUtilTest {

	public static interface MyTestInterface {

	}

	public static class Impl1 implements MyTestInterface {

	}

	public static class Impl2 implements MyTestInterface {

	}

	@Test
	public void createInstanceBySubtype() {
		final Set<MyTestInterface> instances = ReflectionUtil.createInstancesBySubtype("nl.gingerbeard.automation.util", MyTestInterface.class);
		assertEquals(2, instances.size());

		final MyTestInterface instance1 = Iterables.get(instances, 0);
		final MyTestInterface instance2 = Iterables.get(instances, 1);

		assertTrue(instance1 instanceof Impl1 ^ instance2 instanceof Impl1);
		assertTrue(instance1 instanceof Impl2 ^ instance2 instanceof Impl2);
	}

	public static interface MyTestInterface2 {

	}

	public static class FailImpl implements MyTestInterface2 {
		public FailImpl() {
			throw new RuntimeException("test exception");
		}
	}

	@Test
	public void createInstance_constructorThrowsException() {
		final ReflectionException e = assertThrows(ReflectionException.class, () -> ReflectionUtil.createInstancesBySubtype("nl.gingerbeard.automation.util", MyTestInterface2.class));
		assertEquals("Cannot instantiate nl.gingerbeard.automation.util.ReflectionUtilTest$MyTestInterface2", e.getMessage());
	}

}
