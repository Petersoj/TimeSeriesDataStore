package net.jacobpeterson.timeseriesdatastore.util.temporalrange;

import java.time.temporal.Temporal;
import java.util.Objects;

/**
 * {@link TemporalRange} represents a {@link Temporal} a <code>from</code> and a <code>to</code> time.
 */
public class TemporalRange<T extends Temporal> {

    private T from;
    private T to;

    /**
     * Instantiates a new {@link TemporalRange}.
     *
     * @param from the 'from'
     * @param to   the 'to'
     */
    public TemporalRange(T from, T to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        TemporalRange<?> otherTemporalRange = (TemporalRange<?>) other;
        return Objects.equals(from, otherTemporalRange.from) && Objects.equals(to, otherTemporalRange.to);
    }

    @Override
    public String toString() {
        return "TemporalRange{" +
                "from=" + from +
                ", to=" + to +
                '}';
    }

    /**
     * Gets the {@link #from}.
     *
     * @return the {@link #from}
     */
    public T getFrom() {
        return from;
    }

    /**
     * Sets the {@link #from}.
     *
     * @param from the {@link #from}
     */
    public void setFrom(T from) {
        this.from = from;
    }

    /**
     * Gets the {@link #to}.
     *
     * @return the {@link #to}
     */
    public T getTo() {
        return to;
    }

    /**
     * Sets the {@link #to}.
     *
     * @param to the {@link #to}
     */
    public void setTo(T to) {
        this.to = to;
    }
}
