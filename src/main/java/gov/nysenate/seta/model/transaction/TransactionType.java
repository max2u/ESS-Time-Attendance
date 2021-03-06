package gov.nysenate.seta.model.transaction;

/**
 * Simple enumeration of the different types of transactions.
 */
public enum TransactionType
{
    PER("Personnel"),
    PAY("Payroll");

    String desc;

    TransactionType(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
