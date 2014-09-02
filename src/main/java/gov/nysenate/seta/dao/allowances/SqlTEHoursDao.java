package gov.nysenate.seta.dao.allowances;

import gov.nysenate.seta.dao.allowances.mapper.AllowanceRowMapper;
import gov.nysenate.seta.dao.allowances.mapper.AmountExceedRowMapper;
import gov.nysenate.seta.dao.allowances.mapper.TEHoursRowMapper;
import gov.nysenate.seta.dao.base.SqlBaseDao;
import gov.nysenate.seta.model.allowances.AllowanceUsage;
import gov.nysenate.seta.model.allowances.TEHours;
import gov.nysenate.seta.model.payroll.SalaryRec;
import gov.nysenate.seta.model.transaction.AuditHistory;
import gov.nysenate.seta.util.OutputUtils;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;


import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by heitner on 7/28/2014.
 */

@Repository
public class SqlTEHoursDao extends SqlBaseDao implements TEHoursDao {

    protected int empId = -1;
    protected static ArrayList<TEHours> teHourses;
    protected Date beginDate;
    protected Date endDate;
    protected boolean parametersChanged = false;
    protected static final Logger logger = LoggerFactory.getLogger(AllowanceUsage.class);

    /** --- SQL Queries --- */

    protected static final String GET_TE_HOURS_PAID_SQL =
            "SELECT  nuxrefem, dteffect, dtendte, nuhrhrspd  " +
                    "FROM pm21peraudit " +
                    "WHERE nudocument like 'T%' " +
                    " AND nuxrefem = :empId " +
                    " AND dtendte >= :beginDate " +
                    " AND dtendte <= :endDate " +
                    " AND cdstatus = 'A'";

    /** --- Constructors --- */

    public SqlTEHoursDao () {

    }

    /** --- Functional Setters and Getters --- */

    public ArrayList<TEHours> getTEHours(int empId, int year) {
        beginDate = new LocalDate(year, 1, 1).toDate();
        endDate = new LocalDate(year, 12, 31).toDate();

        return getTEHours(empId, beginDate, endDate);
    }

   // @Override
    public  ArrayList<TEHours> getTEHours(int empId, Date beginDate, Date endDate) {
            MapSqlParameterSource params = new MapSqlParameterSource();
            logger.debug("empId:"+empId);
            params.addValue("empId", empId);
            params.addValue("beginDate", beginDate);
            params.addValue("endDate", endDate);

            teHourses = new ArrayList<>(remoteNamedJdbc.query(GET_TE_HOURS_PAID_SQL, params,
                    new TEHoursRowMapper("")));

        return teHourses;
    }

    @Override
    public  TEHours sumTEHours(ArrayList<TEHours> teHourses) {

        TEHours tEHours = new TEHours();

        tEHours.setEmpId(empId);
        BigDecimal totalHours = new BigDecimal(0.0);

        for (TEHours curTEHours : teHourses) {
            totalHours = totalHours.add(curTEHours.getTEHours());

            if (tEHours.getBeginDate()==null||curTEHours.getBeginDate().before(tEHours.getBeginDate())) {
                tEHours.setBeginDate(curTEHours.getBeginDate());
            }

            if (tEHours.getEndDate()==null||curTEHours.getEndDate().after(tEHours.getEndDate())) {
                tEHours.setEndDate(curTEHours.getEndDate());
            }
        }
        tEHours.setTEHours(totalHours);

        return tEHours;
    }



}

