package gov.nysenate.seta.dao.attendance;

import gov.nysenate.seta.dao.attendance.mapper.RemoteEntryRowMapper;
import gov.nysenate.seta.dao.base.SqlBaseDao;
import gov.nysenate.seta.model.attendance.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository("remoteTimeEntry")
public class SqlRemoteTimeEntryDao extends SqlBaseDao implements TimeEntryDao
{
    private static final Logger logger = LoggerFactory.getLogger(SqlRemoteTimeEntryDao.class);

    protected static final String GET_TIME_ENTRY_SQL_TMPL =
        "SELECT * FROM PD23TIMESHEET WHERE CDSTATUS = 'A' AND %s ";

    protected static final String GET_ENTRY_BY_TIME_RECORD_ID =
        String.format(GET_TIME_ENTRY_SQL_TMPL, "NUXRTIMESHEET = :timesheetId \n" +
        "ORDER BY DTDAY ASC");

    protected static final String GET_ENTRY_BY_EMPID =
        String.format(GET_TIME_ENTRY_SQL_TMPL,"NUXREFEM = :empId AND NUXRTIMESHEET = :timesheetId");

    protected static final String SET_ENTRY_SQL =
        "INSERT INTO PD23TIMESHEET (NUXRDAY, NUXRTIMESHEET, NUXREFEM, DTDAY, NUWORK, NUTRAVEL, NUHOLIDAY, NUSICKEMP, " +
        "                           NUSICKFAM, NUMISC, NUXRMISC, NATXNORGUSER, NATXNUPDUSER, DTTXNORIGIN, DTTXNUPDATE, " +
        "                           CDSTATUS, DECOMMENTS, CDPAYTYPE, NUVACATION, NUPERSONAL)\n" +
        "VALUES (:tSDayId, :timesheetId, :empId, :dayDate, :workHR, :travelHR, :holidayHR, :sickEmpHR, :sickFamilyHR, " +
        "        :miscHR, :miscTypeId, :tOriginalUserId, :tUpdateUserId, :tOriginalDate, :tUpdateDate, :status, " +
        "        :empComment, :payType, :vacationHR, :personalHR)";

    protected static final String UPDATE_ENTRY_SQL =
        "UPDATE PD23TIMESHEET \n" +
        "SET (NUXRTIMESHEET = :timesheetId, NUXREFEM = :empId, DTDAY = :dayDate, NUWORK = :workHR, NUTRAVEL = :travelHR, " +
        "     NUHOLIDAY = :holidayHR, NUSICKEMP = :sickEmpHR, NUSICKFAM = :sickFamilyHR, NUMISC = :miscHR, " +
        "     NUXRMISC = :miscTypeId, NATXNORGUSER = :tOriginalUserId, NATXNUPDUSER = :tUpdateUserId, " +
        "     DTTXNORIGIN = :tOriginalDate, DTTXNUPDATE = :tUpdateDate, CDSTATUS = :status, DECOMMENTS = :empComment, " +
        "     CDPAYTYPE = :payType, NUVACATION = :vacationHR, NUPERSONAL = :personalHR) \n" +
        "WHERE NUXRDAY = :tSDayId";

    /** {@inheritDoc} */
    @Override
    public List<TimeEntry> getTimeEntriesByRecordId(int timeRecordId) throws TimeEntryException {
        List<TimeEntry> timeEntryList;
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("timesheetId", timeRecordId);
        try {
            timeEntryList = remoteNamedJdbc.query(GET_ENTRY_BY_TIME_RECORD_ID, params, new RemoteEntryRowMapper());
        }
        catch (DataRetrievalFailureException ex){
            logger.warn("Retrieve time entries for record {} error: {}", timeRecordId, ex.getMessage());
            throw new TimeEntryNotFoundEx("No matching TimeEntries for TimeRecord id: " + timeRecordId);
        }
        return timeEntryList;
    }

    /** {@inheritDoc} */
    @Override
    public Map<String, List<TimeEntry>> getTimeEntryByEmpId(int empId) throws TimeEntryNotFoundEx, TimeRecordNotFoundException {
        List<TimeRecord> timeRecords;
        Map<String, List<TimeEntry>> timeEntries = new HashMap<>();
        MapSqlParameterSource params = new MapSqlParameterSource();
//        timeRecords = timeRecordDao.getRecordByEmployeeId(empId);
        timeRecords = new ArrayList<>(); /** FIXME */
        for(TimeRecord timeRecord : timeRecords) {
            params.addValue("empId",empId);
            params.addValue("timesheetId", timeRecord.getTimeRecordId());
            try {
                timeEntries.put(timeRecord.getTimeRecordId(), remoteNamedJdbc.query(GET_ENTRY_BY_EMPID, params,
                        new RemoteEntryRowMapper()));
            }
            catch (DataRetrievalFailureException ex){
                logger.warn("Retrieve Time Entries of {} error: {}", empId, ex.getMessage());
                throw new TimeEntryNotFoundEx("No matching Time Entries for Employee id: " + empId);
            }
        }
        return timeEntries;
    }

    @Override
    public boolean setTimeEntry(TimeEntry tsd) {
        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("tSDayId", tsd.getEntryId());
        param.addValue("timesheetId", tsd.getTimeRecordId());
        param.addValue("empId", tsd.getEmpId());
        param.addValue("dayDate", tsd.getDate());
        param.addValue("workHR", tsd.getWorkHours());
        param.addValue("travelHR", tsd.getTravelHours());
        param.addValue("holidayHR", tsd.getHolidayHours());
        param.addValue("sickEmpHR", tsd.getSickEmpHours());
        param.addValue("sickFamilyHR", tsd.getSickFamHours());
        param.addValue("miscHR", tsd.getMiscHours());
        param.addValue("miscTypeId", (tsd.getMiscType() != null) ? tsd.getMiscType().getCode() : null);
        param.addValue("tOriginalUserId", tsd.getTxOriginalUserId());
        param.addValue("tUpdateUserId", tsd.getTxUpdateUserId());
        param.addValue("tOriginalDate", tsd.getTxOriginalDate());
        param.addValue("tUpdateDate", tsd.getTxUpdateDate());
        param.addValue("status", getStatusCode(tsd.isActive()));
        param.addValue("empComment", tsd.getEmpComment());
        param.addValue("payType", tsd.getPayType().name());
        param.addValue("vacationHR", tsd.getVacationHours());
        param.addValue("personalHR", tsd.getPersonalHours());
        return (remoteNamedJdbc.update(SET_ENTRY_SQL, param) == 1);
    }

    @Override
    public boolean updateTimeEntry(TimeEntry timeEntry) {
        MapSqlParameterSource params = getTimeEntryParams(timeEntry);
        return (remoteNamedJdbc.update(UPDATE_ENTRY_SQL, params) == 1);
    }

    private static MapSqlParameterSource getTimeEntryParams(TimeEntry timeEntry) {
        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("tSDayId", timeEntry.getEntryId());
        param.addValue("timesheetId", timeEntry.getTimeRecordId());
        param.addValue("empId", timeEntry.getEmpId());
        param.addValue("dayDate", timeEntry.getDate());
        param.addValue("workHR", timeEntry.getWorkHours());
        param.addValue("travelHR", timeEntry.getTravelHours());
        param.addValue("holidayHR", timeEntry.getHolidayHours());
        param.addValue("sickEmpHR", timeEntry.getSickEmpHours());
        param.addValue("sickFamilyHR", timeEntry.getSickFamHours());
        param.addValue("miscHR", timeEntry.getMiscHours());
        param.addValue("miscTypeId", timeEntry.getMiscType().getCode());
        param.addValue("tOriginalUserId", timeEntry.getTxOriginalUserId());
        param.addValue("tUpdateUserId", timeEntry.getTxUpdateUserId());
        param.addValue("tOriginalDate", timeEntry.getTxOriginalDate());
        param.addValue("tUpdateDate", timeEntry.getTxUpdateDate());
        param.addValue("status", getStatusCode(timeEntry.isActive()));
        param.addValue("empComment", timeEntry.getEmpComment());
        param.addValue("payType", timeEntry.getPayType().name());
        param.addValue("vacationHR", timeEntry.getVacationHours());
        param.addValue("personalHR", timeEntry.getPersonalHours());
        return param;
    }
}