package gov.nysenate.seta.dao.base;

/**
 * Enumeration of different schema names to be used in within sql queries.
 */
public enum DbSchema
{
    MASTER_SFMS("SASS_OWNER", DbVendor.ORACLE),
    TIMESHEET_SFMS("TS_OWNER", DbVendor.ORACLE),
    TIMESHEET_LOCAL("ts", DbVendor.POSTGRES);

    private String schemaName;
    private DbVendor vendor;

    DbSchema(String schemaName, DbVendor vendor) {
        this.schemaName = schemaName;
        this.vendor = vendor;
    }

    @Override
    public String toString() {
        return schemaName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public DbVendor getVendor() {
        return vendor;
    }
}