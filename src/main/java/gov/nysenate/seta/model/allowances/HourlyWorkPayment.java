package gov.nysenate.seta.model.allowances;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * A class that models a payment transaction for an hourly temporary employee
 */
public class HourlyWorkPayment {

    private LocalDate effectDate;
    private LocalDate endDate;
    private LocalDateTime auditDate;

    private BigDecimal hoursPaid;

    /** Total money paid */
    private BigDecimal moneyPaid;

    /** Money paid for the year prior to the year of endDate */
    private BigDecimal prevYearMoneyPaid;

    public HourlyWorkPayment() {
        this.hoursPaid = BigDecimal.ZERO;
        this.hoursPaid = BigDecimal.ZERO;
        this.hoursPaid = BigDecimal.ZERO;
    }

    public HourlyWorkPayment(LocalDateTime auditDate, LocalDate effectDate, LocalDate endDate,
                             BigDecimal hoursPaid, BigDecimal moneyPaid, BigDecimal prevYearMoneyPaid) {
        this.auditDate = auditDate;
        this.effectDate = effectDate;
        this.endDate = endDate;
        this.hoursPaid = hoursPaid;
        this.moneyPaid = moneyPaid;
        this.prevYearMoneyPaid = Optional.of(prevYearMoneyPaid).orElse(BigDecimal.ZERO);
    }

    /** --- Functional Getters / Setters */

    public int getYear() {
        return endDate.getYear();
    }

    /**
     * Return the amount of money from this transaction that was paid for the given year
     */
    public BigDecimal getMoneyPaidForYear(int year) {
        if (year == endDate.getYear()) {
            return moneyPaid.subtract(prevYearMoneyPaid);
        } else if (year == effectDate.getYear()) {
            return prevYearMoneyPaid;
        }
        return BigDecimal.ZERO;
    }

    //FIXME
    public BigDecimal getHoursWorkedForYear(int year) {
        return BigDecimal.ZERO;
//        return getMoneyPaidForYear(year).divide(hourlySalary);
    }

    /** Return the range of work dates that this payment is compensating for */
    public Range<LocalDate> getWorkingRange() {
        return Range.closed(effectDate, endDate);
    }

    /** --- Getters / Setters --- */

    public LocalDate getEffectDate() {
        return effectDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public LocalDateTime getAuditDate() {
        return auditDate;
    }

    public BigDecimal getHoursPaid() {
        return hoursPaid;
    }

    public BigDecimal getMoneyPaid() {
        return moneyPaid;
    }

    public BigDecimal getPrevYearMoneyPaid() {
        return prevYearMoneyPaid;
    }
}
