package gov.nysenate.seta.client.response.error;

public enum ErrorCode
{
    APPLICATION_ERROR(1, "An error occurred while processing your request"),
    INVALID_ARGUMENTS(2, "The necessary arguments were not provided in the correct format."),
    MISSING_PARAMETERS(3, "The necessary parameters were not provided."),
    INVALID_TIME_RECORD(4, "The provided time record contained invalid data")
    ;

    private int code;
    private String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
