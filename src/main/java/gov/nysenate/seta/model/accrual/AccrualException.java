package gov.nysenate.seta.model.accrual;

public class AccrualException extends Exception
{
    protected int empId;
    protected AccrualExceptionType errorType;

    public AccrualException(int empId, AccrualExceptionType errorType) {
        super(errorType.message);
        this.errorType = errorType;
        this.empId = empId;
    }

    public int getEmpId() {
        return empId;
    }

    public AccrualExceptionType getErrorType() {
        return errorType;
    }
}
