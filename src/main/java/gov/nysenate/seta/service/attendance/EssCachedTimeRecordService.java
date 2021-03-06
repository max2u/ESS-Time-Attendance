package gov.nysenate.seta.service.attendance;

import com.google.common.collect.*;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import gov.nysenate.common.RangeUtils;
import gov.nysenate.common.SortOrder;
import gov.nysenate.common.WorkInProgress;
import gov.nysenate.seta.model.attendance.*;
import gov.nysenate.seta.model.cache.ContentCache;
import gov.nysenate.seta.model.exception.SupervisorException;
import gov.nysenate.seta.model.payroll.Holiday;
import gov.nysenate.seta.model.payroll.PayType;
import gov.nysenate.seta.model.period.PayPeriod;
import gov.nysenate.seta.model.personnel.SupervisorEmpGroup;
import gov.nysenate.seta.model.transaction.TransactionHistory;
import gov.nysenate.seta.service.accrual.AccrualInfoService;
import gov.nysenate.seta.service.base.SqlDaoBackedService;
import gov.nysenate.seta.service.cache.EhCacheManageService;
import gov.nysenate.seta.service.period.HolidayService;
import gov.nysenate.seta.service.personnel.EmployeeInfoService;
import gov.nysenate.seta.service.personnel.SupervisorInfoService;
import gov.nysenate.seta.service.transaction.EmpTransactionService;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
@WorkInProgress(author = "Ash", since = "2015/09/11", desc = "Reworking methods in the class, adding caching")
public class EssCachedTimeRecordService extends SqlDaoBackedService implements TimeRecordService
{
    private static final Logger logger = LoggerFactory.getLogger(EssCachedTimeRecordService.class);

    /** --- Caching / Events --- */
    @Autowired protected EventBus eventBus;
    @Autowired protected EhCacheManageService cacheManageService;
    private Cache activeRecordCache;

    /** --- Services --- */
    @Autowired protected TimeRecordManager timeRecordManager;
    @Autowired protected EmployeeInfoService empInfoService;
    @Autowired protected EmpTransactionService transService;
    @Autowired protected AccrualInfoService accrualInfoService;
    @Autowired protected SupervisorInfoService supervisorInfoService;
    @Autowired protected HolidayService holidayService;

    @PostConstruct
    public void init() {
        this.eventBus.register(this);
        this.activeRecordCache = this.cacheManageService.registerEternalCache(ContentCache.ACTIVE_TIME_RECORDS.name());
    }

    /** Helper class to store a collection of time records in a cache. */
    protected static class TimeRecordCacheCollection
    {
        private int empId;
        private Map<BigInteger, TimeRecord> cachedTimeRecords = new LinkedHashMap<>();

        public TimeRecordCacheCollection(int empId, Collection<TimeRecord> cachedTimeRecords) {
            this.empId = empId;
            cachedTimeRecords.stream().forEach(this::update);
        }

        public int getEmpId() {
            return empId;
        }

        public List<TimeRecord> getTimeRecords() {
            return new ArrayList<>(cachedTimeRecords.values());
        }

        public void update(TimeRecord record) {
            if (record.getTimeRecordId() == null) {
                throw new IllegalArgumentException("Attempt to insert time record with null id into cache");
            }
            cachedTimeRecords.put(record.getTimeRecordId(), record);
        }

        public void remove(BigInteger timeRecId) {
            cachedTimeRecords.remove(timeRecId);
        }
    }

    /** --- TimeRecordService Implementation --- */

    /** {@inheritDoc} */
    @Override
    public List<Integer> getTimeRecordYears(Integer empId, SortOrder yearOrder) {
        return timeRecordDao.getTimeRecordYears(empId, yearOrder);
    }

    /** {@inheritDoc}
     *
     * The active time records for an employee will be cached.
     */
    @Override
    public List<TimeRecord> getActiveTimeRecords(Integer empId) {
        activeRecordCache.acquireReadLockOnKey(empId);
        TimeRecordCacheCollection cachedRecs;
        Element element = activeRecordCache.get(empId);
        activeRecordCache.releaseReadLockOnKey(empId);
        if (element != null) {
            cachedRecs = (TimeRecordCacheCollection) element.getObjectValue();
        }
        else {
            List<TimeRecord> records = timeRecordDao.getActiveRecords(empId);
            records.forEach(this::initializeEntries);
            cachedRecs = new TimeRecordCacheCollection(empId, records);
            activeRecordCache.acquireWriteLockOnKey(empId);
            activeRecordCache.put(new Element(empId, cachedRecs));
            activeRecordCache.releaseWriteLockOnKey(empId);
        }
        return cachedRecs.getTimeRecords();
    }

    /** {@inheritDoc} */
    @Override
    public List<TimeRecord> getTimeRecords(Set<Integer> empIds, Range<LocalDate> dateRange,
                                           Set<TimeRecordStatus> statuses) {
        TreeMultimap<PayPeriod, TimeRecord> records = TreeMultimap.create();
        timeRecordDao.getRecordsDuring(empIds, dateRange, EnumSet.allOf(TimeRecordStatus.class)).values().stream()
                .forEach(rec -> records.put(rec.getPayPeriod(), rec));
        return records.values().stream()
                .filter(record -> statuses.contains(record.getRecordStatus()))
                .peek(this::initializeEntries)
                .collect(toList());
    }

    @Override
    public List<TimeRecord> getTimeRecords(Set<Integer> empIds, Collection<PayPeriod> payPeriods,
                                           Set<TimeRecordStatus> statuses) {
        RangeSet<LocalDate> dateRanges = TreeRangeSet.create();
        payPeriods.forEach(period -> dateRanges.add(period.getDateRange()));
        if (dateRanges.isEmpty()) {
            return Collections.emptyList();
        }
        return getTimeRecords(empIds, dateRanges.span(), statuses).stream()
                .filter(record -> dateRanges.encloses(record.getDateRange()))
                .collect(toList());
    }

    @Override
    public List<TimeRecord> getTimeRecordsWithSupervisor(Integer empId, Integer supId, Range<LocalDate> dateRange) {
        List<TimeRecord> timeRecords = getTimeRecords(Collections.singleton(empId), dateRange, TimeRecordStatus.getAll());
        return timeRecords.stream().filter(t -> t.getSupervisorId().equals(supId)).collect(Collectors.toList());
    }

    /** {@inheritDoc} */
    @Override
    public ListMultimap<Integer, TimeRecord> getSupervisorRecords(int supId, Range<LocalDate> dateRange,
                                                                  Set<TimeRecordStatus> statuses)
            throws SupervisorException {

        SupervisorEmpGroup empGroup = supervisorInfoService.getSupervisorEmpGroup(supId, dateRange);
        ListMultimap<Integer, TimeRecord> records = ArrayListMultimap.create();
        empGroup.getAllEmployees().forEach(emp -> {
            records.putAll(emp.getEmpId(), getActiveTimeRecords(emp.getEmpId()).stream()
                    .filter(tr -> statuses.contains(tr.getRecordStatus()) &&
                            dateRange.contains(tr.getBeginDate()))
                    .collect(toList()));
        });
        return records;
    }

    @Override
    @Transactional(value = "remoteTxManager")
    @WorkInProgress(author = "ash", desc = "Need to test this a bit better...")
    public synchronized boolean saveRecord(TimeRecord record) {
        boolean updated = timeRecordDao.saveRecord(record);
        if (updated) {
            if (record.isActive() && record.getRecordStatus().getScope() != TimeRecordScope.EMPLOYEE) {
                // If the record is not in the employee scope i.e. it has been submitted,
                // set any earlier, unsubmitted records as inactive
                getActiveTimeRecords(record.getEmployeeId()).stream()
                        .filter(otherRec -> otherRec.getBeginDate().isBefore(record.getBeginDate()))
                        .filter(otherRec -> otherRec.getRecordStatus().getScope() == TimeRecordScope.EMPLOYEE)
                        .peek(otherRec -> otherRec.setActive(false))
                        .forEach(this::saveRecord);
            }
            updateCache(record);
        }
        return updated;
    }

    @Override
    public boolean deleteRecord(BigInteger timeRecordId) {
        return timeRecordDao.deleteRecord(timeRecordId);
    }

    @Subscribe
    public void handleActiveTimeRecordCacheEvict(ActiveTimeRecordCacheEvictEvent event) {
        if (event != null) {
            if (event.allRecords) {
                logger.debug("Clearing out all cached active time records...");
                activeRecordCache.removeAll();
            }
            else {
                for (Integer empId : event.affectedEmployees) {
                    logger.debug("Clearing out active time records for {}", empId);
                    activeRecordCache.remove(empId);
                }
            }
        }
    }

    /** --- Internal Methods --- */

    /**
     * Updates the active time record cache with the given record
     * If the record is active and in progress, it is added/updated, otherwise it is removed
     * @param record TimeRecord
     */
    private void updateCache(TimeRecord record) {
        int empId = record.getEmployeeId();
        activeRecordCache.acquireWriteLockOnKey(empId);
        try {
            Element elem = activeRecordCache.get(empId);
            if (elem != null) {
                TimeRecordCacheCollection cachedRecs = (TimeRecordCacheCollection) elem.getObjectValue();
                if (record.isActive() && TimeRecordStatus.inProgress().contains(record.getRecordStatus())) {
                    cachedRecs.update(record);
                } else {
                    cachedRecs.remove(record.getTimeRecordId());
                }
            }
        } finally {
            activeRecordCache.releaseWriteLockOnKey(empId);
        }
    }

    /**
     * Ensures that the given time record contains entries for each day covered.
     * @param timeRecord - TimeRecord
     */
    private void initializeEntries(TimeRecord timeRecord) {
        RangeMap<LocalDate, PayType> payTypeMap = null;
        for (LocalDate entryDate = timeRecord.getBeginDate(); !entryDate.isAfter(timeRecord.getEndDate());
             entryDate = entryDate.plusDays(1)) {
            if (!timeRecord.containsEntry(entryDate)) {
                if (payTypeMap == null) {
                    TransactionHistory transHistory = transService.getTransHistory(timeRecord.getEmployeeId());
                    payTypeMap = RangeUtils.toRangeMap(
                            transHistory.getEffectivePayTypes(timeRecord.getDateRange()), timeRecord.getEndDate());
                }
                timeRecord.addTimeEntry(new TimeEntry(timeRecord, payTypeMap.get(entryDate), entryDate));
            }
            if (timeRecord.getEntry(entryDate).getPayType() != PayType.TE) {
                // Set holiday hours if applicable
                Optional<Holiday> holiday = holidayService.getHoliday(entryDate);
                timeRecord.getEntry(entryDate).setHolidayHours(
                        holiday.map(Holiday::getHours).orElse(null));
            }
        }
    }
}