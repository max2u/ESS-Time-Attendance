package gov.nysenate.seta.dao.period;

import com.google.common.collect.Range;
import gov.nysenate.common.OrderBy;
import gov.nysenate.common.SortOrder;
import gov.nysenate.seta.dao.base.SqlBaseDao;
import gov.nysenate.seta.dao.period.mapper.PayPeriodRowMapper;
import gov.nysenate.seta.model.exception.PayPeriodException;
import gov.nysenate.seta.model.exception.PayPeriodNotFoundEx;
import gov.nysenate.seta.model.period.PayPeriod;
import gov.nysenate.seta.model.period.PayPeriodType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

import static gov.nysenate.common.DateUtils.endOfDateRange;
import static gov.nysenate.common.DateUtils.startOfDateRange;
import static gov.nysenate.seta.dao.period.SqlPayPeriodQuery.*;

/** {@inheritDoc} */
@Repository
public class SqlPayPeriodDao extends SqlBaseDao implements PayPeriodDao
{
    private static final Logger logger = LoggerFactory.getLogger(SqlPayPeriodDao.class);

    /** {@inheritDoc} */
    @Override
    public PayPeriod getPayPeriod(PayPeriodType type, LocalDate date) throws PayPeriodException {
        PayPeriod payPeriod;
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("periodType", type.getCode());
        params.addValue("date", toDate(date));
        try {
            payPeriod = remoteNamedJdbc.queryForObject(GET_PAY_PERIOD_SQL.getSql(schemaMap()), params, new PayPeriodRowMapper(""));
        }
        catch (DataRetrievalFailureException ex) {
            logger.warn("Retrieve pay period of type: {} during: {} error: {}", type, date, ex.getMessage());
            throw new PayPeriodNotFoundEx("No matching pay period(s) of type " + type + " during " + date);
        }
        return payPeriod;
    }

    /** {@inheritDoc} */
    @Override
    public List<PayPeriod> getPayPeriods(PayPeriodType type, Range<LocalDate> dateRange, SortOrder dateOrder) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("periodType", type.getCode())
            .addValue("startDate", toDate(startOfDateRange(dateRange)))
            .addValue("endDate", toDate(endOfDateRange(dateRange)));
        OrderBy orderBy = new OrderBy("DTBEGIN", dateOrder);
        String sql = GET_PAY_PERIODS_IN_RANGE_SQL.getSql(schemaMap(), orderBy);
        return remoteNamedJdbc.query(sql, params, new PayPeriodRowMapper(""));
    }
}