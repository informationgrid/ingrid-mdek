/*-
 * **************************************************-
 * InGrid mdek-job
 * ==================================================
 * Copyright (C) 2014 - 2020 wemove digital solutions GmbH
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

import java.text.MessageFormat;
import java.util.*;

/**
 * Helper class for reporting validity of ISO 19115 XML documents. This class
 * can be used to collect reports related to the validity of ISO 19115 documents
 * in XML format and logging them using the provided logger.
 *
 * @author Vikram Notay
 */
class ValidationReportHelper {

    private static final String UUID_REGEX_PATTERN = "\\p{XDigit}{8}-?\\p{XDigit}{4}-?\\p{XDigit}{4}-?\\p{XDigit}{4}-?\\p{XDigit}{12}";
    private static final ResourceBundle BUNDLE =  PropertyResourceBundle.getBundle("de.ingrid.mdek.job.validation.iso.bawdmqs.Messages");
    private final List<ValidationReportItem> reports;

    /**
     * Initialise the report helper.
     *
     */
    ValidationReportHelper() {
        reports = new ArrayList<>();
    }

    static boolean isValidUuid(String str) {
        if (str == null) {
            return false;
        } else {
            return str.matches(UUID_REGEX_PATTERN);
        }
    }

    static String getLocalisedString(String key, String defaultValue, Object... params) {
        try {
            return MessageFormat.format(BUNDLE.getString(key), params);
        } catch (MissingResourceException unused) {
            return defaultValue;
        }
    }

    List<ValidationReportItem> getReport() {
        return Collections.unmodifiableList(reports);
    }

    /**
     * Add a check passed message to report.
     *
     * @param messageKey the key for localised string in the package's resource
     * bundle
     * @param defaultMessage the default message to print, in case there is no
     * item with the {@code messageKey} in the package's resource bundle
     * @param parameters the parameters for placeholders in the localised string
     */
    void pass(
            String messageKey,
            String defaultMessage,
            Object... parameters) {
        pushMessage(ValidationReportItem.ReportLevel.PASS, messageKey, defaultMessage, parameters);
    }

    /**
     * Add a check passed with warnings message to report.
     *
     * @param messageKey the key for localised string in the package's resource
     * bundle
     * @param defaultMessage the default message to print, in case there is no
     * item with the {@code messageKey} in the package's resource bundle
     * @param parameters the parameters for placeholders in the localised string
     */
    void warn(
            String messageKey,
            String defaultMessage,
            Object... parameters) {
        pushMessage(ValidationReportItem.ReportLevel.WARN, messageKey, defaultMessage, parameters);
    }

    /**
     * Add a check failed message to report.
     *
     * @param messageKey the key for localised string in the package's resource
     * bundle
     * @param defaultMessage the default message to print, in case there is no
     * item with the {@code messageKey} in the package's resource bundle
     * @param parameters the parameters for placeholders in the localised string
     */
    void fail(
            String messageKey,
            String defaultMessage,
            Object... parameters) {
        pushMessage(ValidationReportItem.ReportLevel.FAIL, messageKey, defaultMessage, parameters);
    }

    private void pushMessage(
            ValidationReportItem.ReportLevel level,
            String messageKey,
            String defaultMessage,
            Object... parameters) {
        reports.add(new ValidationReportItem(level, messageKey, defaultMessage, parameters));
    }

}
