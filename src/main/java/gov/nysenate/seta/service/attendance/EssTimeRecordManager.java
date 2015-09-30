package gov.nysenate.seta.service.attendance;

import com.google.common.collect.*;
import com.google.common.eventbus.Subscribe;
import gov.nysenate.common.DateUtils;
import gov.nysenate.common.RangeUtils;
import gov.nysenate.common.SortOrder;
import gov.nysenate.common.WorkInProgress;
import gov.nysenate.seta.dao.attendance.TimeRecordDao;
import gov.nysenate.seta.dao.personnel.EmployeeDao;
import gov.nysenate.seta.model.attendance.TimeEntry;
import gov.nysenate.seta.model.attendance.TimeRecord;
import gov.nysenate.seta.model.attendance.TimeRecordScope;
import gov.nysenate.seta.model.attendance.TimeRecordStatus;
import gov.nysenate.seta.model.payroll.PayType;
import gov.nysenate.seta.model.period.PayPeriod;
import gov.nysenate.seta.model.period.PayPeriodType;
import gov.nysenate.seta.model.personnel.Employee;
import gov.nysenate.seta.model.transaction.TransactionHistory;
import gov.nysenate.seta.model.transaction.TransactionHistoryUpdateEvent;
import gov.nysenate.seta.model.transaction.TransactionRecord;
import gov.nysenate.seta.service.period.PayPeriodService;
import gov.nysenate.seta.service.personnel.EmployeeInfoService;
import gov.nysenate.seta.service.transaction.EmpTransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static gov.nysenate.seta.model.transaction.TransactionCode.*;

@WorkInProgress(author = "Sam", since = "2015/09/15",
        desc = "currently in testing.  Dependencies EmpTransactionService and EmployeeInfoService still have some bugs")
@Service
public class EssTimeRecordManager implements TimeRecordManager {

    private static final Logger logger = LoggerFactory.getLogger(EssTimeRecordManager.class);

    private static final ImmutableSet recordAlteringTransCodes = ImmutableSet.of(SUP, TYP, EMP, RSH);

    @Autowired TimeRecordService timeRecordService;
    @Autowired TimeRecordDao timeRecordDao;
    @Autowired EmployeeDao employeeDao;
    @Autowired PayPeriodService payPeriodService;
    @Autowired EmpTransactionService transService;
    @Autowired EmployeeInfoService empInfoService;

    /** {@inheritDoc} */
    @Override
    public int ensureRecords(int empId) {
        List<PayPeriod> payPeriods = payPeriodService.getOpenPayPeriods(PayPeriodType.AF, empId, SortOrder.ASC);
        List<TimeRecord> existingRecords =
                timeRecordService.getTimeRecords(Collections.singleton(empId), payPeriods, TimeRecordStatus.getAll());
        return ensureRecords(empId, payPeriods, existingRecords);
    }

    @Override
    public void ensureAllActiveRecords() {
        // Get all employees with open attendance periods, also get all active time records
        Set<Integer> empIds = employeeDao.getActiveEmployeeIds();
        logger.info("getting active time records...");
        ListMultimap<Integer, TimeRecord> existingRecordMap = timeRecordDao.getAllActiveRecords();

        // Create and patch records for each employee
        int totalSaved = empIds.stream()
                .sorted()
                .map(empId -> ensureRecords(empId,
                        payPeriodService.getOpenPayPeriods(PayPeriodType.AF, empId, SortOrder.ASC),
                        Optional.of(existingRecordMap.get(empId)).orElse(Collections.emptyList()) ))
                .reduce(0, Integer::sum);
        logger.info("saved {} records", totalSaved);
    }

    /**
     * invokes the ensureAllActiveRecords method according to the configured cron value
     * @see #ensureAllActiveRecords()
     */
    @Scheduled(cron = "${scheduler.timerecord.ensureall}")
    public synchronized void scheduledEnsureAll() {
        ensureAllActiveRecords();
    }

    /**
     * Modifies records when new transactions are posted
     * @param event TransactionHistoryUpdateEvent
     */
    @Subscribe
    @WorkInProgress(author = "sam", since = "9/30/2015", desc = "has not been tested, need to simulate transaction posts")
    public synchronized void handleTransactionHistoryUpdateEvent(TransactionHistoryUpdateEvent event) {
        event.getTransRecs().stream()
                .filter(transRec -> recordAlteringTransCodes.contains(transRec.getTransCode()))
                .map(TransactionRecord::getEmployeeId)
                .distinct()
                .forEach(this::ensureRecords);
    }

    /** --- Internal Methods --- */

    /**
     * Ensure that the employee has up to date records that cover all given pay periods
     * Existing records are split/modified as needed to ensure correctness
     * If createTempRecords is false, then records will only be created for periods with annual pay work days
     */
    private int ensureRecords(int empId, Collection<PayPeriod> payPeriods, Collection<TimeRecord> existingRecords) {
        logger.info("Generating records for {} over {} pay periods with {} existing records",
                empId, payPeriods.size(), existingRecords.size());

        // Get a set of ranges for which there should be time records
        LinkedHashSet<Range<LocalDate>> recordRanges = getRecordRanges(payPeriods, empId);
        List<TimeRecord> recordsToSave = new LinkedList<>();
        TransactionHistory transHistory = transService.getTransHistory(empId);

        // Get the latest submitted record.  New records will not be created for dates before this record
        Optional<TimeRecord> latestSubmitted = existingRecords.stream()
                .filter(record -> record.getRecordStatus().getScope() != TimeRecordScope.EMPLOYEE)
                .max(TimeRecord::compareTo);

        // Check that existing records correspond to the record ranges
        // Split any records that span multiple ranges
        //  also ensure that existing records and entries contain up to date information
        // Remove ranges that are covered by existing records
        List<TimeRecord> patchedRecords = patchExistingRecords(empId, recordRanges, existingRecords);
        recordsToSave.addAll(patchedRecords);
        long patchedRecordsSaved = patchedRecords.size();

        // Create new records for all ranges not covered by existing records
        long newRecordsSaved = 0;
        if (transHistory.isFullyAppointed()) {
            newRecordsSaved = recordRanges.stream()
                    .filter(range -> DateUtils.startOfDateRange(range).isAfter(
                            latestSubmitted.map(TimeRecord::getEndDate).orElse(LocalDate.MIN)))
                    .map(range -> createTimeRecord(empId, range))
                    .peek(recordsToSave::add)
                    .count();
        }

        recordsToSave.forEach(timeRecordService::saveRecord);

        if (recordsToSave.isEmpty()) {
            logger.info("empId {}: no changes", empId);
        } else {
            logger.info("empId {}:\t{} periods\t{} existing\t{} saved:\t{} new\t{} patched/split",
                    empId, payPeriods.size(), existingRecords.size(), recordsToSave.size(), newRecordsSaved, patchedRecordsSaved);
        }
        return recordsToSave.size();
    }

    /**
     * Check existing records to make sure that records correspond with the computed record date ranges,
     *  and contain correct information
     * Records that are already approved by personnel will not be patched
     * As existing records are checked, corresponding covered ranges are removed from recordRanges
     * Records that do not check out are modified accordingly
     * @return List<TimeRecord> - a list of existing records that were modified
     */
    private List<TimeRecord> patchExistingRecords(
            int empId, LinkedHashSet<Range<LocalDate>> recordRanges, Collection<TimeRecord> existingRecords) {
        List<TimeRecord> recordsToSave = new LinkedList<>();
        existingRecords.stream()
                .filter(record -> TimeRecordStatus.inProgress().contains(record.getRecordStatus()))
                .forEach(record -> {
                    List<Range<LocalDate>> rangesUnderRecord = recordRanges.stream()
                            .filter(range -> range.isConnected(record.getDateRange()) &&
                                    !range.intersection(record.getDateRange()).isEmpty())
                            .collect(Collectors.toList());
                    if (rangesUnderRecord.size() != 1 ||
                            !rangesUnderRecord.get(0).equals(record.getDateRange())) {
                        recordsToSave.addAll(splitRecord(rangesUnderRecord, record, empId));
                    } else if (patchRecord(record)) {
                        recordsToSave.add(record);
                    }
                    recordRanges.removeAll(rangesUnderRecord);
                });
        return recordsToSave;
    }

    /**
     * Generate a new time record for the given employee id spanning the given range
     */
    private TimeRecord createTimeRecord(int empId, Range<LocalDate> dateRange) {
        return new TimeRecord(
                empInfoService.getEmployee(empId, DateUtils.startOfDateRange(dateRange)),
                dateRange,
                payPeriodService.getPayPeriod(PayPeriodType.AF, DateUtils.startOfDateRange(dateRange))
        );
    }

    /**
     * Splits an existing time record according to the given date ranges
     * @param ranges List<Range<LocalDate>> - ranges corresponding to dates for which there should be distinct time records
     *               These ranges should all intersect with the existing time record
     * @param record TimeRecord
     * @param empId int
     * @return List<TimeRecord> - the records resulting from the split
     */
    private List<TimeRecord> splitRecord(List<Range<LocalDate>> ranges, TimeRecord record, int empId) {
        if (ranges.stream().anyMatch(range -> !RangeUtils.intersects(range, record.getDateRange()))) {
            throw new IllegalArgumentException("split ranges should all intersect with the record to be split");
        }

        Iterator<Range<LocalDate>> rangeIterator = ranges.iterator();
        List<TimeRecord> splitResult = new LinkedList<>();

        if (rangeIterator.hasNext()) {
            // Adjust the begin and end dates of the existing record to match the first range
            // patch the existing record + entries, ensuring correct supervisor and pay types
            record.setDateRange(rangeIterator.next());
            patchRecord(record);

            // Prune any existing entries with dates outside of the first range,
            // saving them to be added to any appropriate new records that are created
            TreeMap<LocalDate, TimeEntry> existingEntryMap = new TreeMap<>();
            record.getTimeEntries().stream()
                    .map(TimeEntry::getDate)
                    .filter(date -> !record.getDateRange().contains(date))
                    .map(record::removeEntry)
                    .forEach(entry -> existingEntryMap.put(entry.getDate(), entry));

            splitResult.add(record);

            // Generate time records for the remaining ranges, adding the existing time records as appropriate
            rangeIterator.forEachRemaining(range -> {
                TimeRecord newRecord = createTimeRecord(empId, range);
                existingEntryMap.subMap(newRecord.getBeginDate(), true, newRecord.getEndDate(), true)
                        .values().forEach(newRecord::addTimeEntry);
                splitResult.add(newRecord);
            });
        }

        return splitResult;
    }

    /**
     * Verify that the given time record contains correct data
     * If not the record will be patched
     * @return true iff the record was patched
     */
    private boolean patchRecord(TimeRecord record) {
        boolean modifiedRecord = false;
        Employee empInfo = empInfoService.getEmployee(record.getEmployeeId(), record.getBeginDate());
        if (!record.checkEmployeeInfo(empInfo)) {
            modifiedRecord = true;
            record.setEmpInfo(empInfo);
        }

        return patchEntries(record) || modifiedRecord;
    }

    /**
     * Verify the time entries of the given record, patching them if they have incorrect pay types
     * @return true if one or more entries were patched
     */
    private boolean patchEntries(TimeRecord record) {
        boolean modifiedEntries = false;
        // Get effective pay types for the record
        RangeMap<LocalDate, PayType> payTypes = getPayTypeRangeMap(record.getEmployeeId());
        // Check the pay types for each entry
        for (TimeEntry entry : record.getTimeEntries()) {
            PayType correctPayType = payTypes.get(entry.getDate());
            if (!Objects.equals(entry.getPayType(), correctPayType)) {
                modifiedEntries = true;
                entry.setPayType(correctPayType);
            }
        }
        return modifiedEntries;
    }

    /**
     * Get a range map containing the effective pay types for all employed dates of the given employee
     */
    private RangeMap<LocalDate, PayType> getPayTypeRangeMap(int empId) {
        return RangeUtils.toRangeMap(
                transService.getTransHistory(empId).getEffectivePayTypes(Range.all()));
    }

    /**
     * Get ranges corresponding to record dates for over a range of dates
     * Determined by pay periods, supervisor changes, and active dates of service
     */
    private LinkedHashSet<Range<LocalDate>> getRecordRanges(Collection<PayPeriod> periods, int empId) {
        TransactionHistory transHistory = transService.getTransHistory(empId);

        // Get dates when there was a change of supervisor
        Set<LocalDate> newSupDates = transHistory.getEffectiveSupervisorIds(DateUtils.ALL_DATES).keySet();

        // Get active dates of service
        RangeSet<LocalDate> activeDates = empInfoService.getEmployeeActiveDatesService(empId);

        return periods.stream()
                .sorted()
                .map(PayPeriod::getDateRange)
                // split any ranges that contain dates where there was a supervisor change
                .flatMap(periodRange -> RangeUtils.splitRange(periodRange, newSupDates).stream())
                // get the intersection of each range with the active dates of service
                .flatMap(range -> activeDates.subRangeSet(range).asRanges().stream())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
