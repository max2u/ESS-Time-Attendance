package gov.nysenate.seta.model.transaction;

import java.util.*;

/**
 * The TransactionHistory provides an ordered collection of TransactionRecords. This class is intended to be
 * used in methods that need to know about the history of a specific TransactionCode for an employee.
 */
public class TransactionHistory
{
    protected int employeeId;
    protected Map<TransactionCode, List<TransactionRecord>> recordHistory;

    public TransactionHistory(int empId) {
        this.employeeId = empId;
        this.recordHistory = new HashMap<>();
    }

    /**
     * Returns true if any records exist in the history.
     * @return boolean
     */
    public boolean hasRecords() {
        return !recordHistory.isEmpty();
    }

    /**
     * Returns true if records exist for a given transaction code.
     * @param code TransactionCode
     * @return boolean
     */
    public boolean hasRecords(TransactionCode code) {
        return recordHistory.get(code) != null && !recordHistory.get(code).isEmpty();
    }

    /** --- Functional Getters/Setters --- */

    /**
     * Adds a transaction record to the history queue.
     * @param record TransactionRecord
     */
    public void addTransactionRecord(TransactionRecord record) {
        if (record != null) {
            TransactionCode code = record.getTransCode();
            if (!recordHistory.containsKey(code)) {
                recordHistory.put(code, new LinkedList<TransactionRecord>());
            }
            this.recordHistory.get(code).add(record);
        }
    }

    /**
     * Adds a collection of transaction records to their respective history queue.
     * @param recordsList List<TransactionRecord>
     */
    public void addTransactionRecords(List<TransactionRecord> recordsList) {
        for (TransactionRecord record : recordsList) {
            this.addTransactionRecord(record);
        }
    }

    /**
     * See overloaded method. This provides the option to change the sort order for the returned list of records.
     * @param code TransactionCode
     * @param sortByDateAsc boolean - If true, list will be ordered by earliest effect date first.
     * @return LinkedList<TransactionRecord>
     */
    public LinkedList<TransactionRecord> getTransRecords(TransactionCode code, boolean sortByDateAsc) {
        return getTransRecords(new HashSet<>(Arrays.asList(code)), sortByDateAsc);
    }

    /**
     * Returns a single ordered LinkedList containing a set of transaction records. This is useful if
     * you need all the transaction records to be ordered into a single collection.
     * @param transCodes Set<TransactionCode> - The set of transaction codes to return in the list
     * @param sortByDateAsc boolean - If true, list will be ordered by earliest effect date first.
     * @return LinkedList<TransactionRecord>
     */
    public LinkedList<TransactionRecord> getTransRecords(Set<TransactionCode> transCodes, boolean sortByDateAsc) {
        LinkedList<TransactionRecord> sortedRecList = new LinkedList<>();
        for (TransactionCode code : recordHistory.keySet()) {
            if (transCodes.contains(code)) {
                sortedRecList.addAll(recordHistory.get(code));
            }
        }
        Collections.sort(sortedRecList, (sortByDateAsc) ? new TransDateAscending() : new TransDateDescending());
        return sortedRecList;
    }

    /**
     * Shorthand method to retrieve every available transaction code.
     * @param sortByDateAsc boolean - If true, list will be ordered by earliest effect date first.
     * @return LinkedList<TransactionRecord>
     */
    public LinkedList<TransactionRecord> getAllTransRecords(boolean sortByDateAsc) {
        return getTransRecords(recordHistory.keySet(), sortByDateAsc);
    }

    /** --- Local classes --- */

    protected static class TransDateAscending implements Comparator<TransactionRecord> {
        @Override
        public int compare(TransactionRecord o1, TransactionRecord o2) {
            int dateCompare = o1.getEffectDate().compareTo(o2.getEffectDate());
            return (dateCompare != 0) ? dateCompare : o1.getOriginalDate().compareTo(o2.originalDate);
        }
    }

    protected static class TransDateDescending implements Comparator<TransactionRecord> {
        @Override
        public int compare(TransactionRecord o1, TransactionRecord o2) {
            int dateCompare = o2.getEffectDate().compareTo(o1.getEffectDate());
            return (dateCompare != 0) ? dateCompare : o2.getOriginalDate().compareTo(o1.originalDate);
        }
    }

    /** --- Basic Getters/Setters --- */

    public int getEmployeeId() {
        return employeeId;
    }
}
