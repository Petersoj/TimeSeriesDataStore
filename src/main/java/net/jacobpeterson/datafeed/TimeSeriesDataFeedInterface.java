package net.jacobpeterson.datafeed;

import java.time.LocalDateTime;

/**
 * {@link TimeSeriesDataFeedInterface} is used to interface with a data feed that serves time series data.
 *
 * @param <P> the time series data POJO type parameter
 */
public abstract class TimeSeriesDataFeedInterface<P> {

    protected ReceivedDataHandler<P> receivedDataHandler;

    /**
     * Instantiates a new {@link TimeSeriesDataFeedInterface}.
     */
    public TimeSeriesDataFeedInterface() {
    }

    /**
     * Called to request time series data in a given time range.
     *
     * @param from the from {@link LocalDateTime}
     * @param to   the to {@link LocalDateTime}
     */
    protected abstract void requestData(LocalDateTime from, LocalDateTime to);

    /**
     * Called when time series data has been received.
     *
     * @param data          the data
     * @param dataTimestamp the data timestamp
     */
    protected void onReceiveData(P data, LocalDateTime dataTimestamp) {
        receivedDataHandler.handleReceivedData(data, dataTimestamp);
    }

    /**
     * Gets {@link #receivedDataHandler}.
     *
     * @return the {@link #receivedDataHandler}
     */
    public ReceivedDataHandler<P> getReceivedDataHandler() {
        return receivedDataHandler;
    }

    /**
     * Sets {@link #receivedDataHandler}.
     *
     * @param receivedDataHandler the {@link #receivedDataHandler}
     */
    public void setReceivedDataHandler(ReceivedDataHandler<P> receivedDataHandler) {
        this.receivedDataHandler = receivedDataHandler;
    }
}
