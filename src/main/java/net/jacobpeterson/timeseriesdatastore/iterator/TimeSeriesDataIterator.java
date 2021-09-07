package net.jacobpeterson.timeseriesdatastore.iterator;

import net.jacobpeterson.timeseriesdatastore.database.TimeSeriesDatabaseInterface;
import net.jacobpeterson.timeseriesdatastore.datafeed.ReceivedDataHandler;
import net.jacobpeterson.timeseriesdatastore.datafeed.TimeSeriesDataFeedInterface;

import java.time.LocalDateTime;
import java.util.Iterator;

public abstract class TimeSeriesDataIterator<K, P> implements Iterator<P>, ReceivedDataHandler<P> {

    public TimeSeriesDataIterator(TimeSeriesDatabaseInterface<K, ?, P, ?> databaseInterface,
            TimeSeriesDataFeedInterface<P> dataFeedInterface) {

    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public P next() {
        return null;
    }

    @Override
    public void handleReceivedData(P data, LocalDateTime dataTimestamp) {

    }
}
