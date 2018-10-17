/*-
 * **************************************************-
 * InGrid mdek-job
 * ==================================================
 * Copyright (C) 2014 - 2018 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
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
