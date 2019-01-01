package nl.gingerbeard.automation.util;

import java.util.Optional;

public abstract class ChainOfResponsibility<SubjectType, ReturnType> {

	private Optional<ChainOfResponsibility<SubjectType, ReturnType>> nextLink = Optional.empty();

	public final Optional<ReturnType> execute(final SubjectType item) {
		if (matches(item)) {
			return handleByThisLink(item);
		} else if (hasNextLink()) {
			return passToNextLink(item);
		} else {
			return endOfChain();
		}
	}

	private Optional<ReturnType> endOfChain() {
		return Optional.empty();
	}

	private boolean hasNextLink() {
		return nextLink.isPresent();
	}

	private Optional<ReturnType> passToNextLink(final SubjectType item) {
		return nextLink.get().execute(item);
	}

	private Optional<ReturnType> handleByThisLink(final SubjectType item) {
		return Optional.ofNullable(doWork(item));
	}

	public final void setNextLink(final ChainOfResponsibility<SubjectType, ReturnType> nextType) {
		this.nextLink = Optional.ofNullable(nextType);
	}

	protected abstract boolean matches(SubjectType item);

	protected abstract ReturnType doWork(SubjectType item);

	public static <SubjectType, ReturnType> Builder<SubjectType, ReturnType> builder() {
		return new Builder<>();
	}

	public static final class Builder<SubjectType, ReturnType> {

		private Optional<ChainOfResponsibility<SubjectType, ReturnType>> chain = Optional.empty();

		public Builder<SubjectType, ReturnType> add(final ChainOfResponsibility<SubjectType, ReturnType> link) {
			chain.ifPresent((nextLink) -> link.setNextLink(nextLink));
			chain = Optional.of(link);
			return this;
		}

		public ChainOfResponsibility<SubjectType, ReturnType> build() {
			return chain.orElseThrow(() -> new IllegalStateException("No links present in chain."));
		}

	}
}
