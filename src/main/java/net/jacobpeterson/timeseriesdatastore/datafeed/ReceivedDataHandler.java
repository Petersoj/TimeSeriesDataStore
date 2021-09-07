package net.jacobpeterson.timeseriesdatastore.datafeed;

import java.time.LocalDateTime;

/**
 * {@link ReceivedDataHandler} is an interface for handling received time series data.
 *
 * @param <P> the time series data POJO type parameter
 */
public interface ReceivedDataHandler<P> {

    /**
     * Handles received time series data.
     *
     * @param data          the data
     * @param dataTimestamp the data timestamp
     */
    void handleReceivedData(P data, LocalDateTime dataTimestamp);
}
