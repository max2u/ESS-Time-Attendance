package gov.nysenate.seta.dao;

import gov.nysenate.seta.model.PayPeriod;
import gov.nysenate.seta.model.PayPeriodType;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PayPeriodRowMapper implements RowMapper<PayPeriod>
{
    protected String pfx;

    public PayPeriodRowMapper(String pfx) {
        this.pfx = pfx;
    }

    @Override
    public PayPeriod mapRow(ResultSet rs, int rowNum) throws SQLException {
        PayPeriod period = new PayPeriod();
        period.setActive(rs.getString("CDSTATUS").equals("A"));
        period.setType(PayPeriodType.valueOf(rs.getString("CDPERIOD")));
        period.setPayPeriodNum(rs.getInt("NUPERIOD"));
        period.setStartDate(rs.getDate("DTBEGIN"));
        period.setEndDate(rs.getDate("DTEND"));
        return period;
    }
}
