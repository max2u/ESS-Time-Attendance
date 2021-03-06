package gov.nysenate.seta.dao.accrual;

import gov.nysenate.seta.dao.base.BaseDao;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by heitner on 7/23/2014.
 */
public interface HoursDao extends BaseDao {

    public BigDecimal getTotalHours(int empId, int year);

    public  BigDecimal getTotalHours(int empId, Date beginDate, Date endDate);

}
