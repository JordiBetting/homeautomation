package nl.gingerbeard.automation.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Optional;

import org.junit.jupiter.api.Test;

public class ChainOfResponsibilityTest {

	static class MyChain1 extends ChainOfResponsibility<String, String> {

		@Override
		protected boolean matches(final String item) {
			return item.startsWith("a");
		}

		@Override
		protected String doWork(final String item) {
			return this.getClass().getSimpleName();
		}
	}

	static class MyChain2 extends ChainOfResponsibility<String, String> {

		@Override
		protected boolean matches(final String item) {
			return item.startsWith("b");
		}

		@Override
		protected String doWork(final String item) {
			return this.getClass().getSimpleName();
		}
	}

	@Test
	public void execute() {
		Optional<String> result;
		final MyChain1 first = new MyChain1();
		final MyChain2 next = new MyChain2();
		first.setNextLink(next);

		result = first.execute("alphabet");
		assertTrue(result.isPresent());
		assertEquals("MyChain1", result.get());

		result = first.execute("bird");
		assertTrue(result.isPresent());
		assertEquals("MyChain2", result.get());
	}

	@Test
	public void execute_notProcesssed() {
		final MyChain1 first = new MyChain1();
		final MyChain2 next = new MyChain2();
		first.setNextLink(next);

		final Optional<String> result = first.execute("notImplemented");

		assertEquals(Optional.empty(), result);
	}

	@Test
	public void chainbuilder() {
		Optional<String> result;
		final ChainOfResponsibility.Builder<String, String> builder = ChainOfResponsibility.builder();
		final ChainOfResponsibility<String, String> chain = builder//
				.add(new MyChain1())//
				.add(new MyChain2())//
				.build();

		result = chain.execute("alphabet");
		assertTrue(result.isPresent());
		assertEquals("MyChain1", result.get());

		result = chain.execute("bird");
		assertTrue(result.isPresent());
		assertEquals("MyChain2", result.get());
	}

	@Test
	public void chainBuilder_empty_throwsException() {

		final ChainOfResponsibility.Builder<String, String> builder = ChainOfResponsibility.builder();
		try {
			builder.build();
			fail("Expected exception");
		} catch (final IllegalStateException e) {
			assertEquals("No links present in chain.", e.getMessage());
		}
	}

}
