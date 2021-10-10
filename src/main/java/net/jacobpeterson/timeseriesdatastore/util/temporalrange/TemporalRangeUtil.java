package net.jacobpeterson.timeseriesdatastore.util.temporalrange;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * {@link TemporalRangeUtil} contains several utility methods for date/time objects and for {@link TemporalRange}s.
 */
public final class TemporalRangeUtil {

    /**
     * Gets missing {@link TemporalRange}s from found/valid {@link TemporalRange}s. That is, invert {@link
     * TemporalRange}s given in the <code>validTemporalRanges</code> on a timeline.
     *
     * @param from                  the 'from' {@link LocalDateTime} (inclusive)
     * @param to                    the 'to' {@link LocalDateTime} (inclusive)
     * @param validTemporalRanges   the valid {@link TemporalRange}s (the {@link TemporalRange#getFrom()}s MUST be
     *                              sorted from oldest to newest) (this method infers that the {@link
     *                              TemporalRange#getFrom()} and {@link TemporalRange#getTo()} in the {@link
     *                              TemporalRange} are inclusive of their associated {@link LocalDateTime}s)
     * @param fromExclusivityOffset added to a {@link TemporalRange#getFrom()} {@link LocalDateTime} in a returned
     *                              {@link TemporalRange} to enable exclusivity for {@link TemporalRange}s
     *                              (<code>null</code> for no offset)
     * @param toExclusivityOffset   subtracted from a {@link TemporalRange#getTo()} {@link LocalDateTime} in a returned
     *                              {@link TemporalRange} to enable exclusivity for {@link TemporalRange}s
     *                              (<code>null</code> for no offset)
     *
     * @return a {@link List} of {@link TemporalRange}s
     */
    public static ArrayList<TemporalRange<LocalDateTime>> getMissingTemporalRanges(
            LocalDateTime from, LocalDateTime to, List<TemporalRange<LocalDateTime>> validTemporalRanges,
            Duration fromExclusivityOffset, Duration toExclusivityOffset) {
        checkNotNull(from);
        checkNotNull(to);
        checkArgument(!to.isBefore(from), "From must be before to!"); // Checks that to >= from

        // Create an empty list if null was passed in
        validTemporalRanges = validTemporalRanges == null ? new ArrayList<>() : validTemporalRanges;

        // Set defaults for exclusivity offsets if necessary
        fromExclusivityOffset = fromExclusivityOffset == null ? Duration.ZERO : fromExclusivityOffset;
        toExclusivityOffset = toExclusivityOffset == null ? Duration.ZERO : toExclusivityOffset;

        // Note that we do not check if the passed in 'from + fromExclusivityOffset' and 'to - toExclusivityOffset'
        // overlap because the below logic accounts for overlapping of these 'from' and 'to' offsets.
        // This would occur if the 'from' and 'to' are the same and the offsets are not zero.

        // If validTemporalRanges is empty, return the input from and to
        if (validTemporalRanges.isEmpty()) {
            ArrayList<TemporalRange<LocalDateTime>> missingTimestamp = new ArrayList<>();
            missingTimestamp.add(new TemporalRange<>(from, to));
            return missingTimestamp;
        }

        final LocalDateTime fromPlusFromOffset = from.plus(fromExclusivityOffset);
        final ArrayList<TemporalRange<LocalDateTime>> missingTimestampRanges = new ArrayList<>();
        LocalDateTime missingFrom = from;

        // Loop through the validTemporalRanges from oldest to newest
        for (TemporalRange<LocalDateTime> validFromTo : validTemporalRanges) {
            LocalDateTime validFrom = validFromTo.getFrom();
            LocalDateTime validTo = validFromTo.getTo();

            if (validFrom.isAfter(validTo)) {
                throw new IllegalArgumentException("From must be before to in the validTemporalRanges!");
            }

            if (validFrom.isBefore(missingFrom) || validFrom.equals(missingFrom)) {
                if (validTo.isAfter(from)) {
                    missingFrom = validTo;
                } else if (validTo.equals(from)) {
                    // If validTo equals the passed in 'from' then add fromOffset to missingFrom
                    missingFrom = fromPlusFromOffset;
                }
                // else do nothing because validTo is before passed in 'from' so
                // the validFromTo range is before the passed in range

            } else { // validFrom is after missingFrom

                // Break if missingFrom/previous validTo is beyond the passed in 'to'
                // or if the validFrom is after the passed in 'to'
                if (missingFrom.isAfter(to) || validFrom.isAfter(to)) {
                    break;
                }

                // Create missingFrom and missingTo 'LocalDateTime's with offsets
                LocalDateTime missingFromPlusFromOffset = missingFrom.plus(fromExclusivityOffset);
                LocalDateTime missingToMinusToOffset = validFrom.minus(toExclusivityOffset);
                // If offsets cause missingFromTo TemporalRange to overlap, do not add the missingFromTo TemporalRange
                // (but it is okay if they are equal)
                if (!missingFromPlusFromOffset.isAfter(missingToMinusToOffset)) {
                    // Clamp offsets to passed in 'from' and passed in 'to' just in case
                    missingFromPlusFromOffset = min(missingFromPlusFromOffset, to);
                    missingToMinusToOffset = max(missingToMinusToOffset, from);

                    missingTimestampRanges.add(new TemporalRange<>(
                            // Add the missingFromTo TemporalRange with offset, but if the missingFrom is equal to the
                            // passed in 'from' then don't add the fromOffset to the missingFrom because
                            // it should be inclusive if there is no valid range that includes the passed in 'from'.
                            missingFrom.equals(from) ? missingFrom : missingFromPlusFromOffset,
                            missingToMinusToOffset));
                }

                missingFrom = validTo;

                // Break if validTo is beyond the passed in 'to'
                if (validTo.isAfter(to) || validTo.equals(to)) {
                    break;
                }
            }
        }

        // Clamp missingFrom to 'from' (without fromOffset)
        if (missingFrom.isBefore(from)) {
            missingFrom = from;
        }

        // Add the last missingFrom and passed in 'to' (without toOffset) TemporalRange if
        // no validTimestamps extend beyond the passed in 'to'
        if (missingFrom.isBefore(to)) {
            LocalDateTime missingFromPlusFromOffset = missingFrom.plus(fromExclusivityOffset);
            missingTimestampRanges.add(new TemporalRange<>(
                    missingFrom.equals(from) ? missingFrom : missingFromPlusFromOffset,
                    to));
        }

        return missingTimestampRanges;
    }

    /**
     * Squashes the given {@link TemporalRange}s on the timeline. That is, any overlapping {@link TemporalRange}s become
     * one {@link TemporalRange}.
     *
     * @param temporalRanges the {@link List} of {@link TemporalRange}s <strong>(the {@link TemporalRange#getFrom()}s
     *                       MUST be sorted from oldest to newest)</strong>
     *
     * @return a squashed {@link TemporalRange}s {@link List}
     */
    public static List<TemporalRange<LocalDateTime>> squash(List<TemporalRange<LocalDateTime>> temporalRanges) {
        if (temporalRanges == null || temporalRanges.isEmpty()) {
            return temporalRanges;
        }

        ArrayList<TemporalRange<LocalDateTime>> squashedTemporalRanges = new ArrayList<>();

        LocalDateTime currentOldestFrom = temporalRanges.get(0).getFrom();
        LocalDateTime currentNewestTo = temporalRanges.get(0).getTo();
        for (int index = 1; index < temporalRanges.size(); index++) { // Start the loop at the 2nd TemporalRange
            TemporalRange<LocalDateTime> currentTemporalRange = temporalRanges.get(index);

            // Check if the currentTemporalRange 'from' > the 'newestTo'
            if (currentTemporalRange.getFrom().isAfter(currentNewestTo)) {
                squashedTemporalRanges.add(new TemporalRange<>(currentOldestFrom, currentNewestTo));
                currentOldestFrom = currentTemporalRange.getFrom();
                currentNewestTo = currentTemporalRange.getTo();

                // Check if currentTemporalRange 'to' > the 'newestTo'
            } else if (currentTemporalRange.getTo().isAfter(currentNewestTo)) {
                currentNewestTo = currentTemporalRange.getTo();
            }
        }

        // Add the last squashed TemporalRange (to also account for there only being one TemporalRange in the
        // passed-in list OR that there was only be one squashed TemporalRange)
        squashedTemporalRanges.add(new TemporalRange<>(currentOldestFrom, currentNewestTo));

        return squashedTemporalRanges;
    }

    /**
     * Clamps the given {@link TemporalRange}s on the timeline. That is, any {@link TemporalRange}s that lie outside the
     * passed in <code>from</code> and <code>to</code> are clamped to the passed in <code>from</code> and
     * <code>to</code>.
     *
     * @param temporalRanges the {@link List} of {@link TemporalRange}s <strong>(the {@link TemporalRange#getFrom()}s
     *                       MUST be sorted from oldest to newest)</strong>
     * @param from           the 'from' {@link LocalDateTime} to clamp to
     * @param to             the 'to' {@link LocalDateTime} to clamp to
     *
     * @return a clamped {@link TemporalRange} {@link List}
     */
    public static List<TemporalRange<LocalDateTime>> clamp(List<TemporalRange<LocalDateTime>> temporalRanges,
            LocalDateTime from, LocalDateTime to) {
        if (temporalRanges == null || temporalRanges.isEmpty()) {
            return temporalRanges;
        }

        checkArgument(!to.isBefore(from), "From must be before to!"); // Checks that to >= from

        ArrayList<TemporalRange<LocalDateTime>> clampedTemporalRanges = new ArrayList<>();

        for (TemporalRange<LocalDateTime> currentTemporalRange : temporalRanges) {
            // Only add it if the TemporalRange is even within the 'from' and 'to'
            if (currentTemporalRange.getFrom().isAfter(to) || currentTemporalRange.getTo().isBefore(from)) {
                continue;
            }

            // Add the clamped TemporalRange
            clampedTemporalRanges.add(new TemporalRange<>(max(from, currentTemporalRange.getFrom()),
                    min(to, currentTemporalRange.getTo())));
        }

        return clampedTemporalRanges;
    }

    /**
     * Clamps the {@link TemporalRange}s on the timeline to {@link TemporalRange}s that are only within the given
     * <code>from</code> and <code>to</code> {@link LocalTime}s. <strong>This method assumes that NO {@link
     * TemporalRange}s passed in intersect with another. This can be done via {@link #squash(List)}.</strong>
     *
     * @param temporalRanges the {@link List} of {@link TemporalRange}s <strong>(the {@link TemporalRange#getFrom()}s
     *                       MUST be sorted from oldest to newest)</strong>
     * @param from           the 'from' {@link LocalTime} to clamp to
     * @param to             the 'to' {@link LocalTime} to clamp to
     *
     * @return a clamped {@link TemporalRange} {@link List}
     */
    public static List<TemporalRange<LocalDateTime>> clamp(List<TemporalRange<LocalDateTime>> temporalRanges,
            LocalTime from, LocalTime to) {
        if (temporalRanges == null || temporalRanges.isEmpty()) {
            return temporalRanges;
        }

        from = from == null ? LocalTime.MIN : from;
        to = to == null ? LocalTime.MAX : to;

        checkArgument(!to.isBefore(from), "From must be before to!"); // Checks that to >= from

        ArrayList<TemporalRange<LocalDateTime>> clampedTemporalRanges = new ArrayList<>();

        for (TemporalRange<LocalDateTime> currentTemporalRange : temporalRanges) {
            LocalDateTime loopDateTime = currentTemporalRange.getFrom().with(LocalTime.MIN);

            // Loop while the 'loopDateTime' <= 'to'
            while (!loopDateTime.isAfter(currentTemporalRange.getTo())) {
                LocalDateTime loopDateTimeFrom = loopDateTime.with(from);
                LocalDateTime loopDateTimeTo = loopDateTime.with(to);

                LocalDateTime clampedFrom = max(currentTemporalRange.getFrom(), max(loopDateTimeFrom, loopDateTime));
                loopDateTime = loopDateTime.plusDays(1).with(LocalTime.MIN); // Advance to start of the next day
                LocalDateTime clampedTo = min(currentTemporalRange.getTo(), min(loopDateTimeTo, loopDateTime));

                // Add the time-clamped TemporalRange if it exists within the 'from' and 'to' bounds
                // AND the 'clampedFrom' != 'clampedTo'.
                if (!clampedTo.isBefore(loopDateTimeFrom) && !clampedFrom.isAfter(loopDateTimeTo)
                        && !clampedFrom.equals(clampedTo)) {
                    clampedTemporalRanges.add(new TemporalRange<>(clampedFrom, clampedTo));
                }
            }
        }

        return clampedTemporalRanges;
    }

    /**
     * Gets the {@link LocalDateTime} that's the furthest in the future.
     *
     * @param a a {@link LocalDateTime}
     * @param b a {@link LocalDateTime}
     *
     * @return a {@link LocalDateTime}
     */
    public static LocalDateTime max(LocalDateTime a, LocalDateTime b) {
        return a.isAfter(b) ? a : b;
    }

    /**
     * Gets the {@link LocalDateTime} that's the furthest in the past.
     *
     * @param a a {@link LocalDateTime}
     * @param b a {@link LocalDateTime}
     *
     * @return a {@link LocalDateTime}
     */
    public static LocalDateTime min(LocalDateTime a, LocalDateTime b) {
        return a.isBefore(b) ? a : b;
    }

    /**
     * Checks if the {@link LocalDateTime} is in between the passed in <code>from</code> and <code>to</code>.
     *
     * @param dateTime  the {@link LocalDateTime} to check
     * @param from      the 'from' {@link LocalDateTime}
     * @param to        the 'to' {@link LocalDateTime}
     * @param inclusive if true, then if the <code>dateTime</code> is equal to the <code>from</code> or the
     *                  <code>to</code> then this method will return true (aka inclusive of the range limits),
     *                  otherwise it's exclusive of the range limits.
     *
     * @return a boolean
     */
    public static boolean isBetween(LocalDateTime dateTime, LocalDateTime from, LocalDateTime to,
            boolean inclusive) {
        if (inclusive && (dateTime.equals(from) || dateTime.equals(to))) {
            return true;
        } else {
            return dateTime.isAfter(from) && dateTime.isBefore(to);
        }
    }

    /**
     * Gets a comparator which will sort {@link TemporalRange}s by their {@link TemporalRange#getFrom()} and then by
     * their {@link TemporalRange#getTo()}.
     *
     * @return the {@link Comparator}
     */
    public static Comparator<TemporalRange<LocalDateTime>> getFromToLocalDateTimeComparator() {
        return (range1, range2) -> {
            if (range1.getFrom().equals(range2.getFrom())) {
                return range1.getTo().compareTo(range2.getTo());
            } else {
                return range1.getFrom().compareTo(range2.getFrom());
            }
        };
    }

    /**
     * Gets a comparator which will sort {@link TemporalRange}s by their {@link TemporalRange#getTo()} and then by their
     * {@link TemporalRange#getFrom()}.
     *
     * @return the {@link Comparator}
     */
    public static Comparator<TemporalRange<LocalDateTime>> getToFromLocalDateTimeComparator() {
        return (range1, range2) -> {
            if (range1.getTo().equals(range2.getTo())) {
                return range1.getFrom().compareTo(range2.getFrom());
            } else {
                return range1.getTo().compareTo(range2.getTo());
            }
        };
    }
}
