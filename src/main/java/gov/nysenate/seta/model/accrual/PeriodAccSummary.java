package gov.nysenate.seta.model.accrual;

import gov.nysenate.seta.model.period.PayPeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

/**
 * Helper class to store accrual summary information per pay period.
 * Also contains the accrual rates and expected hours, which are not
 * included in the AnnualAccSummary.
 */
public class PeriodAccSummary extends AccrualSummary
{
    private static final Logger logger = LoggerFactory.getLogger(PeriodAccSummary.class);

    /**
     * The base pay period is a previous pay period that contains the summary data for
     * which these accruals are based off of. Basically the accrual information for a given pay
     * period should reflect the hours available from the previous pay period (not including
     * the hours accrued in the current pay period).
     */
    protected PayPeriod basePayPeriod;

    protected int year;
    protected BigDecimal prevTotalHours;
    protected BigDecimal expectedTotalHours;
    protected BigDecimal expectedBiweekHours;

    /**
     * The rates should reflect the current pay period, not the base pay period.
     */
    protected BigDecimal sickRate;
    protected BigDecimal vacRate;

    /** --- Constructors --- */

    public PeriodAccSummary() {}

    public PeriodAccSummary(AccrualSummary summary) {
        super(summary);
    }

    /** --- Functional Getters/Setters --- */

    public LocalDate getEndDate() {
        if (basePayPeriod != null) {
            return basePayPeriod.getEndDate();
        }
        throw new IllegalStateException("Base pay period was not set in period accrual summary.");
    }

    /** --- Basic Getters/Setters --- */

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public PayPeriod getBasePayPeriod() {
        return basePayPeriod;
    }

    public void setBasePayPeriod(PayPeriod basePayPeriod) {
        this.basePayPeriod = basePayPeriod;
    }

    public BigDecimal getPrevTotalHours() {
        return prevTotalHours;
    }

    public void setPrevTotalHours(BigDecimal prevTotalHours) {
        this.prevTotalHours = prevTotalHours;
    }

    public BigDecimal getExpectedTotalHours() {
        return expectedTotalHours;
    }

    public void setExpectedTotalHours(BigDecimal expectedTotalHours) {
        this.expectedTotalHours = expectedTotalHours;
    }

    public BigDecimal getExpectedBiweekHours() {
        return expectedBiweekHours;
    }

    public void setExpectedBiweekHours(BigDecimal expectedBiweekHours) {
        this.expectedBiweekHours = expectedBiweekHours;
    }

    public BigDecimal getSickRate() {
        return sickRate;
    }

    public void setSickRate(BigDecimal sickRate) {
        this.sickRate = sickRate;
    }

    public BigDecimal getVacRate() {
        return vacRate;
    }

    public void setVacRate(BigDecimal vacRate) {
        this.vacRate = vacRate;
    }

    @Deprecated
    public static int getWorkingDaysBetweenDates(Date startDate, Date endDate) {
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(startDate);

        Calendar endCal = Calendar.getInstance();
        endCal.setTime(endDate);
        Calendar curCal = Calendar.getInstance();

        curCal.setTime(startCal.getTime());
        int workDays = 0;

        // if start and end are the same and it is a weekday, return 1
        // if start and end are the same and it is a weekend, return 0
        if (startCal.getTimeInMillis() == endCal.getTimeInMillis()) {
            if (curCal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && curCal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                return 1;
            }
            else {
                return 0;
            }
        }
        //Return -1 if the start is later than the end date which indicates an error
        else if (startCal.getTimeInMillis() > endCal.getTimeInMillis()) {
            return -1;
        }

        if (startCal.getTimeInMillis() > endCal.getTimeInMillis()) {
            startCal.setTime(endDate);
            endCal.setTime(startDate);
        }
        /*
        * Subtracting dates leaves one day lower than actual work days. We simply add one day
        * to the end date to get the correct work days.
        * EX:   Subtracting Dates        Subtract Value    Real Work Days
        *        1/1/14 - 1/1/14         0                 1  (0 +1)
        *        6/9/14(Mo)-6/13/14(Fr)  4                 5  (4 +1)
        *        6/9/14(Mo)-6/14/14(Sa)  5                 5  (5 + 0)
        *        6/9/14(Mo)-6/15/14(Su)  5                 5  (5[Mo-Fr] + 0)
        *        6/9/14(Mo)-6/15/14(Mo)  6                 6  (5 + 1)
        *
         */
        //endCal.add(Calendar.DATE, 1);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("E");
        SimpleDateFormat simpleDateFormat0 = new SimpleDateFormat("MM/dd/yyyy");
        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");

        do {
            if (curCal.getTime().equals(endCal.getTime())) {
                logger.debug("IN DO WHILE Current Date equals End Date");
            }
            //excluding start date
            if (curCal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && curCal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                ++workDays;
            }
            logger.debug(simpleDateFormat0.format(curCal.getTime())+" "+simpleDateFormat.format(curCal.getTime())+" Workdays:"+workDays);
            curCal.add(Calendar.DATE,1);
        } while (curCal.getTime().equals(endCal.getTime())||curCal.getTime().before(endCal.getTime()));

        return workDays;
    }
}