package net.jacobpeterson.timeseriesdatastore.test.util.temporalrange;

import net.jacobpeterson.timeseriesdatastore.util.temporalrange.TemporalRange;
import net.jacobpeterson.timeseriesdatastore.util.temporalrange.TemporalRangeUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for {@link TemporalRangeUtil}.
 */
public class TemporalRangeUtilTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TemporalRangeUtilTest.class);

    /**
     * Tests {@link TemporalRangeUtil#getMissingTemporalRanges(LocalDateTime, LocalDateTime, List, Duration, Duration)}
     * with normal arguments.
     */
    @Test
    public void testGetMissingTemporalRangesNormal() {
        LocalDateTime rangeFrom = LocalDateTime.of(2021, 1, 1, 0, 0);
        LocalDateTime rangeTo = LocalDateTime.of(2021, 1, 7, 0, 0);

        List<TemporalRange<LocalDateTime>> validTemporalRanges = new ArrayList<>();
        validTemporalRanges.add(new TemporalRange<>(
                LocalDateTime.of(2021, 1, 1, 1, 0), LocalDateTime.of(2021, 1, 3, 12, 30)));
        validTemporalRanges.add(new TemporalRange<>(
                LocalDateTime.of(2021, 1, 2, 0, 0), LocalDateTime.of(2021, 1, 4, 0, 0)));
        validTemporalRanges.add(new TemporalRange<>(
                LocalDateTime.of(2021, 1, 5, 9, 0), LocalDateTime.of(2021, 1, 6, 1, 0)));
        validTemporalRanges.add(new TemporalRange<>(
                LocalDateTime.of(2021, 1, 6, 4, 0), LocalDateTime.of(2021, 1, 6, 8, 0)));

        List<TemporalRange<LocalDateTime>> expectedMissingTemporalRanges = new ArrayList<>();
        expectedMissingTemporalRanges.add(new TemporalRange<>(
                LocalDateTime.of(2021, 1, 1, 0, 0), LocalDateTime.of(2021, 1, 1, 1, 0)));
        expectedMissingTemporalRanges.add(new TemporalRange<>(
                LocalDateTime.of(2021, 1, 4, 0, 0), LocalDateTime.of(2021, 1, 5, 9, 0)));
        expectedMissingTemporalRanges.add(new TemporalRange<>(
                LocalDateTime.of(2021, 1, 6, 1, 0), LocalDateTime.of(2021, 1, 6, 4, 0)));
        expectedMissingTemporalRanges.add(new TemporalRange<>(
                LocalDateTime.of(2021, 1, 6, 8, 0), LocalDateTime.of(2021, 1, 7, 0, 0)));

        List<TemporalRange<LocalDateTime>> actualMissingTemporalRanges =
                TemporalRangeUtil.getMissingTemporalRanges(rangeFrom, rangeTo, validTemporalRanges, null, null);

        LOGGER.debug("Valid: {}", validTemporalRanges);
        LOGGER.debug("Expected: {}", expectedMissingTemporalRanges);
        LOGGER.debug("Actual: {}", actualMissingTemporalRanges);

        Assertions.assertEquals(expectedMissingTemporalRanges, actualMissingTemporalRanges);
    }
}
