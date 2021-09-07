package net.jacobpeterson.timeseriesdatastore.database;

import net.jacobpeterson.timeseriesdatastore.datafeed.TimeSeriesDataFeedInterface;
import net.jacobpeterson.timeseriesdatastore.util.sort.SortDirection;
import net.jacobpeterson.timeseriesdatastore.util.temporalrange.TemporalRange;
import org.jooq.Condition;
import org.jooq.Cursor;
import org.jooq.DSLContext;
import org.jooq.OrderField;
import org.jooq.Record;
import org.jooq.Record3;
import org.jooq.RecordMapper;
import org.jooq.RecordUnmapper;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.SQLDataType;

import java.sql.Time;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static org.jooq.impl.DSL.trueCondition;
import static org.jooq.impl.DSL.val;

/**
 * {@link TimeSeriesDatabaseInterface} is used to access database time series tables. Specifically, a time series data
 * table and a timestamp ranges table. The data table contains records with time series data (data with a timestamp
 * field). The timestamp ranges table contains records that define the valid time ranges that exist in the time series
 * data table. Not only does this provide fast lookup for what date ranges do not exist in the data table, but it also
 * defines what timestamp ranges have already been fetched by the {@link TimeSeriesDataFeedInterface}.
 *
 * @param <K> the type parameter of the key used for the time series data table and the timestamp ranges table
 * @param <R> the time series data {@link Record} type parameter
 * @param <P> the time series data POJO type parameter
 * @param <T> the timestamp {@link Record3} type parameter (value1 being the table key, value2 being the 'from' and
 *            value3 being the 'to')
 */
public abstract class TimeSeriesDatabaseInterface<K, R extends Record, P,
        T extends Record3<K, LocalDateTime, LocalDateTime>> {

    protected DSLContext dslCreate;

    /**
     * Instantiates a new {@link TimeSeriesDatabaseInterface}.
     *
     * @param dslCreate the {@link DSLContext}
     */
    public TimeSeriesDatabaseInterface(DSLContext dslCreate) {
        this.dslCreate = dslCreate;
    }

    /**
     * Gets the time series data {@link Table}.
     *
     * @return the time series data {@link Table}
     */
    protected abstract Table<R> getDataTable();

    /**
     * Gets the time series data 'key' {@link TableField}.
     *
     * @return the time series data 'key' {@link TableField}
     */
    protected abstract TableField<R, K> getDataKeyTableField();

    /**
     * Gets the time series data 'timestamp' {@link TableField}.
     *
     * @return the time series data 'timestamp' {@link TableField}
     */
    protected abstract TableField<R, LocalDateTime> getDataTimestampTableField();

    /**
     * Gets the time series data {@link Record} to POJO {@link RecordMapper}.
     *
     * @return the {@link Record} to POJO {@link RecordMapper}
     */
    protected abstract RecordMapper<R, P> getDataRecordMapper();

    /**
     * Gets the POJO to {@link Record} {@link RecordUnmapper}.
     *
     * @return the the POJO to {@link Record} {@link RecordUnmapper}
     */
    protected abstract RecordUnmapper<P, R> getDataRecordUnmapper();

    /**
     * Gets the time series data timestamp ranges {@link Table}.
     *
     * @return the time series data timestamp ranges {@link Table}
     */
    protected abstract Table<T> getTimestampRangesTable();

    /**
     * Gets the time series data timestamp ranges 'key' {@link TableField}.
     *
     * @return the time series data timestamp ranges 'key' {@link TableField}
     */
    protected abstract TableField<T, K> getTimestampRangesKeyTableField();

    /**
     * Gets the timestamp ranges 'from' timestamp {@link TableField}.
     *
     * @return the timestamp ranges 'from' timestamp {@link TableField}
     */
    protected abstract TableField<T, LocalDateTime> getTimestampRangesFromTableField();

    /**
     * Gets the timestamp ranges 'to' timestamp {@link TableField}.
     *
     * @return the timestamp ranges 'to' timestamp {@link TableField}
     */
    protected abstract TableField<T, LocalDateTime> getTimestampRangesToTableField();

    /**
     * Returns the amount of rows that a data query should fetch at one time. This is used for the {@link #get(Object,
     * LocalDateTime, LocalDateTime, LocalTime, LocalTime, SortDirection)} method.
     *
     * @return the fetch row size
     */
    protected abstract int getDataFetchSize();

    /**
     * Inserts a time series data POJO into the {@link #getDataTable()}.
     *
     * @param dataPOJO the data POJO
     *
     * @throws DataAccessException thrown for {@link DataAccessException}s
     */
    public void insert(P dataPOJO) throws DataAccessException {
        checkArgument(dataPOJO != null, "The data POJO cannot be null!");

        // Insert the converted POJO to table record into the database data table
        dslCreate.insertInto(getDataTable())
                .set(getDataRecordUnmapper().unmap(dataPOJO))
                .onDuplicateKeyIgnore()
                .execute();
    }

    /**
     * Gets the the data POJOs from this database.
     *
     * @param key             the key
     * @param from            the 'from' (inclusive)
     * @param to              the 'to' (exclusive)
     * @param beginFilterTime allows you to specify the earliest time of day for which to receive data (null for no
     *                        filter) (inclusive)
     * @param endFilterTime   allows you to specify the latest time of day for which to receive data (null for no
     *                        filter) (exclusive)
     * @param sortDirection   the {@link SortDirection} (defaults to {@link SortDirection#ASCENDING})
     *
     * @return a lazy {@link Iterator} (that is, an {@link Iterator} that fetches {@link #getDataFetchSize()} rows at a
     * time)
     *
     * @throws DataAccessException thrown for {@link DataAccessException}s
     */
    public Iterator<P> get(K key, LocalDateTime from, LocalDateTime to, LocalTime beginFilterTime,
            LocalTime endFilterTime, SortDirection sortDirection)
            throws DataAccessException {
        // Check arguments
        checkArgument(key != null, "Key cannot be null!");
        checkArgument(from != null, "From cannot be null!");
        checkArgument(to != null, "To cannot be null!");
        sortDirection = sortDirection == null ? SortDirection.ASCENDING : sortDirection;

        // Create WHERE clause conditions
        Condition keyEqualCondition = getDataKeyTableField().equal(key);
        Condition timestampRangeCondition = getDataTimestampTableField().greaterOrEqual(from)
                .and(getDataTimestampTableField().lessThan(to));
        Condition timestampFilterCondition = trueCondition();
        if (beginFilterTime != null) {
            timestampFilterCondition = timestampFilterCondition.and(getDataTimestampTableField().cast(SQLDataType.TIME)
                    .greaterOrEqual(Time.valueOf(beginFilterTime)));
        }
        if (endFilterTime != null) {
            timestampFilterCondition = timestampFilterCondition.and(getDataTimestampTableField().cast(SQLDataType.TIME)
                    .lessThan(Time.valueOf(endFilterTime)));
        }

        // Create ORDER BY clause
        OrderField<LocalDateTime> orderByField = sortDirection == SortDirection.ASCENDING ?
                getDataTimestampTableField().asc() :
                getDataTimestampTableField().desc();

        final RecordMapper<R, P> dataRecordMapper = getDataRecordMapper();
        final Cursor<R> recordCursor = dslCreate.selectFrom(getDataTable())
                .where(keyEqualCondition.and(timestampRangeCondition).and(timestampFilterCondition))
                .orderBy(orderByField)
                .fetchSize(getDataFetchSize())
                .fetchLazy();

        return new Iterator<P>() {
            @Override
            public boolean hasNext() {
                return recordCursor.hasNext(); // Closes the database query cursor automatically
            }

            @Override
            public P next() {
                return recordCursor.fetchNext(dataRecordMapper);
            }
        };
    }

    /**
     * Inserts a timestamp range {@link Record3} into the {@link #getTimestampRangesTable()} table (does nothing if it
     * already exists). Note that only {@link TemporalRange}s that are completely filled in the underlying data table
     * should be inserted into the {@link #getTimestampRangesTable()} table.
     *
     * @param key  the key
     * @param from the 'from' whose value should be treated inclusively
     * @param to   the 'to' whose value should be treated exclusively
     *
     * @throws DataAccessException thrown for {@link DataAccessException}s
     */
    public void insertTimestampRangeRecord(K key, LocalDateTime from, LocalDateTime to) throws DataAccessException {
        // Check arguments
        checkArgument(key != null, "Key cannot be null!");
        checkArgument(from != null, "From cannot be null!");
        checkArgument(to != null, "To cannot be null!");

        // Create the record
        Record3<K, LocalDateTime, LocalDateTime> timestampRangesRecord = dslCreate.newRecord(getTimestampRangesTable());
        timestampRangesRecord.set(getTimestampRangesKeyTableField(), key);
        timestampRangesRecord.set(getTimestampRangesFromTableField(), from);
        timestampRangesRecord.set(getTimestampRangesToTableField(), to);

        // Insert into the table (do nothing if it already exists)
        dslCreate.insertInto(getTimestampRangesTable())
                .set(timestampRangesRecord)
                .onDuplicateKeyIgnore()
                .execute();
    }

    /**
     * Gets {@link TemporalRange}s that were inserted via {@link #insertTimestampRangeRecord(Object, LocalDateTime,
     * LocalDateTime)} given a {@link LocalDateTime} range.
     *
     * @param key           the key
     * @param from          the 'from' (inclusive)
     * @param to            the 'to' (inclusive)
     * @param sortDirection the {@link SortDirection} (defaults to {@link SortDirection#ASCENDING})
     *
     * @return a {@link List} of {@link TemporalRange}s of type {@link LocalDateTime}. Note that the {@link
     * LocalDateTime}s in the {@link TemporalRange}s could go beyond the range of the 'from' and the 'to' and is added
     * to the {@link List} as long as one of the {@link LocalDateTime}s is in the given range. This {@link List} is
     * sorted by the 'from' and then the 'to' if sortDirection is {@link SortDirection#ASCENDING}, otherwise it is
     * sorted by the 'to' and then the 'from'.
     *
     * @throws DataAccessException thrown for {@link DataAccessException}s
     */
    public List<TemporalRange<LocalDateTime>> getTimestampRanges(K key, LocalDateTime from, LocalDateTime to,
            SortDirection sortDirection) throws DataAccessException {
        // Check arguments
        checkArgument(key != null, "Key cannot be null!");
        checkArgument(from != null, "From cannot be null!");
        checkArgument(to != null, "To cannot be null!");
        sortDirection = sortDirection == null ? SortDirection.ASCENDING : sortDirection;

        // Create WHERE clause conditions
        // We basically want to get any timestamp range that intersects, is contained within, or contains the
        // passed in timestamp range.
        Condition keyEqualCondition = getTimestampRangesKeyTableField().equal(key);
        Condition fromArgumentCondition = val(from).greaterOrEqual(getTimestampRangesFromTableField()).and(
                val(from).lessOrEqual(getTimestampRangesToTableField()));
        Condition fromDatabaseCondition = getTimestampRangesFromTableField().greaterOrEqual(from).and(
                getTimestampRangesToTableField().lessOrEqual(from));
        Condition toArgumentCondition = val(to).lessOrEqual(getTimestampRangesToTableField()).and(
                val(from).greaterOrEqual(getTimestampRangesToTableField()));
        Condition toDatabaseCondition = getTimestampRangesToTableField().lessOrEqual(to).and(
                getTimestampRangesToTableField().greaterOrEqual(from));

        // Create ORDER BY clause
        // We first want to order by the 'from' ASC and then by the 'to' ASC if the sortDirection is ASCENDING and
        // order by the 'to' DESC and then by the 'from' DESC if the sortDirection is DESCENDING.
        OrderField<LocalDateTime> firstOrder = sortDirection == SortDirection.ASCENDING ?
                getTimestampRangesFromTableField().asc() :
                getTimestampRangesToTableField().desc();
        OrderField<LocalDateTime> secondOrder = sortDirection == SortDirection.ASCENDING ?
                getTimestampRangesToTableField().asc() :
                getTimestampRangesFromTableField().desc();

        return dslCreate
                .select(getTimestampRangesFromTableField(), getTimestampRangesToTableField())
                .from(getTimestampRangesTable())
                .where(keyEqualCondition.and(
                        fromArgumentCondition
                                .or(fromDatabaseCondition)
                                .or(toArgumentCondition)
                                .or(toDatabaseCondition)))
                .orderBy(firstOrder, secondOrder)
                .fetch(record -> new TemporalRange<>(record.value1(), record.value2()));
    }
}
