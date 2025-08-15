package io.github.thecsdev.betterstats.api.util.io;

import java.io.IOException;
import java.util.Objects;
import net.minecraft.network.FriendlyByteBuf;

/**
 * An {@link IOException} that takes place when reading a {@link RAMStatsProvider}'s
 * data form a {@link FriendlyByteBuf} that contains invalid header data.
 * @see StatsProviderIO#read(FriendlyByteBuf, IEditableStatsProvider)
 */
public final class IllegalHeaderException extends IOException
{
	// ==================================================
	private static final long serialVersionUID = -1266798578155159251L;
	// --------------------------------------------------
	private final String expected, got;
	// ==================================================
	public IllegalHeaderException(String expected, String got)
	{
		super(String.format("Failed to read a buffer; Illegal header. Expected \"%s\", but \"%s\" was present instead.",
				Objects.requireNonNull(expected), Objects.requireNonNull(got)));
		this.expected = expected;
		this.got = got;
	}
	// ==================================================
	/**
	 * Returns the header {@link String} that was expected to be in a {@link FriendlyByteBuf} at a given point.
	 */
	public final String getExpected() { return this.expected; }
	
	/**
	 * Returns the {@link String} that was obtained instead of the {@link #getExpected()} one.
	 */
	public final String getGot() { return this.got; }
	// ==================================================
	public final @Override int hashCode() { return Objects.hash(expected, got); }
	public final @Override boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		IllegalHeaderException other = (IllegalHeaderException) obj;
		return Objects.equals(this.expected, other.expected) && Objects.equals(this.got, other.got);
	}
	// ==================================================
}