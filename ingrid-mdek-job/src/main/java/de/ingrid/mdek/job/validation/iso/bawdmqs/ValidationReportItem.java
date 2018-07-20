package de.ingrid.mdek.job.validation.iso.bawdmqs;

/**
 * The result of a single validation check.
 */
// immutable
public final class ValidationReportItem {
    private final  ReportLevel level;
    private final String message;

    ValidationReportItem(ValidationReportItem.ReportLevel level,
                         String messageKey,
                         String defaultMessage,
                         Object... parameters) {
        this.level = level;
        this.message = ValidationReportHelper
                .getLocalisedString(messageKey, defaultMessage, parameters);
    }

    /**
     * Retrieve the report level of this report item.
     *
     * @return the report level of this report item.
     */
    public ReportLevel getLevel() {
        return level;
    }

    /**
     * Retrieve the message text of this report item.
     *
     * @return the message text of this report item
     */
    public String getMessage() {
        return message;
    }

    /**
     * The level of the validation report item.
     */
    public enum ReportLevel {
        /**
         * Test passed without problems.
         */
        PASS("[PASS] "),

        /**
         * Test passed but with warnings.
         */
        WARN("[WARN] "),

        /**
         * Test failed.
         */
        FAIL("[FAIL] ");

        private final String prefix;

        ReportLevel(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public String toString() {
            return prefix;
        }
    }
}
