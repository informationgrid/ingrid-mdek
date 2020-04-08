package de.ingrid.mdek.job.mapping.validation.iso.util;

import de.ingrid.mdek.job.protocol.ProtocolHandler;
import de.ingrid.mdek.job.protocol.ProtocolHandler.Type;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.DOMReader;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalQuery;
import java.util.*;

import static java.time.format.DateTimeFormatter.*;

public final class IsoImportValidationUtil {

    private static final Logger LOG = Logger.getLogger(IsoImportValidationUtil.class);

    private static final String GMD_XSD_FILESYSTEM_LOCATION = "org/isotc211/2005/gmd/gmd.xsd";

    public static final String ISO_ELEMENTS_RESOURCE_BUNDLE = "de.ingrid.mdek.job.mapping.validation.iso.Elements";
    public static final String ISO_MESSAGES_RESOURCE_BUNDLE = "de.ingrid.mdek.job.mapping.validation.iso.Messages";
    public static final String CODELIST_BASE_URL = "http://standards.iso.org/iso/19139/resources/gmxCodelists.xml";

    private static final String UUID_REGEX_PATTERN = "\\p{XDigit}{8}-?\\p{XDigit}{4}-?\\p{XDigit}{4}-?\\p{XDigit}{4}-?\\p{XDigit}{12}";

    private final Document doc;
    private final ProtocolHandler protocolHandler;

    private final List<ResourceBundle> bundles;

    private Node startNode;
    private String xpath;
    private String tagKey;
    private String stringParam;
    private Type logLevel;

    public IsoImportValidationUtil(org.w3c.dom.Document w3cDoc, ProtocolHandler protocolHandler, String... bundleBaseNames) {
        DOMReader reader = new DOMReader();
        this.doc = reader.read(w3cDoc);
        this.protocolHandler = protocolHandler;

        bundles = new ArrayList<>();
        for(String baseName: bundleBaseNames) {
            bundles.add(PropertyResourceBundle.getBundle(baseName));
        }

        reset();
    }

    public IsoImportValidationUtil withStartNode(Node startNode) {
        this.startNode = startNode;
        return this;
    }

    public IsoImportValidationUtil withXpath(String xpath) {
        this.xpath = xpath;
        return this;
    }

    public IsoImportValidationUtil withTagKey(String tagKey) {
        this.tagKey = tagKey;
        return this;
    }

    public IsoImportValidationUtil withStringParameter(String param) {
        this.stringParam = param;
        return this;
    }

    public IsoImportValidationUtil withLogLevel(Type logLevel) {
        this.logLevel = logLevel;
        return this;
    }

    public void doChecks(ValidationType... checks) {
        if (xpath == null || tagKey == null) {
            throw new IllegalArgumentException("Either xpath or tagKey argument is missing");
        }

        List<Node> nodes;
        if (startNode == null) {
            nodes = selectNodes(xpath);
        } else {
            nodes = selectNodes(startNode, xpath);
        }

        for(ValidationType check: checks) {
            if (check == ValidationType.EXACTLY_ONE_NODE_EXISTS && nodes.size() != 1) {
                String msgKey = "node.count.wrong";
                String tagName = tagNameFor(tagKey);
                String defaultMsg = tagName + ": Expecting only one node but found " + nodes.size();
                error(msgKey, defaultMsg, tagName, nodes.size(), xpath);
                continue;
            } else if (check == ValidationType.ONE_OR_MORE_NODES_EXIST && nodes.isEmpty()) {
                String msgKey = "node.missing";
                String tagName = tagNameFor(tagKey);
                String defaultMsg = tagName + ": Node is missing.";
                error(msgKey, defaultMsg, tagName, xpath);
                continue;
            } else if (check == ValidationType.ONE_OR_MORE_DESCENDANTS_EXIST && nodes.isEmpty()) {
                String msgKey = "descendant.missing";
                String tagName = tagNameFor(tagKey);
                String defaultMsg = tagName + ": Descendant is missing.";
                error(msgKey, defaultMsg, tagName, xpath, startNode.getUniquePath());
                continue;
            } else if (check == ValidationType.TEXT_CONTENT_MATCHES_PATTERN_AT_LEAST_ONCE) {
                checkAtLeastOneNodeMatchesPattern(nodes, stringParam, xpath, tagKey);
                continue;
            }

            for(Node n: nodes) {
                if (check == ValidationType.TEXT_CONTENT_EQUALS) {
                    checkNodeTextIsExactValue(n, stringParam, tagKey, logLevel);
                } else if (check == ValidationType.TEXT_CONTENT_IS_FLOATING_POINT_NUMBER) {
                    checkNodeTextIsFloatingPointNumber(n, tagKey);
                } else if (check == ValidationType.TEXT_CONTENT_IS_LESS_THAN_OR_EQUAL_TO) {
                    checkNodeTextIsLessThanOrEqualTo(n, tagKey, stringParam, logLevel);
                } else if (check == ValidationType.TEXT_CONTENT_IS_GREATER_THAN_OR_EQUAL_TO) {
                    checkNodeTextIsGreaterThanOrEqualTo(n, tagKey, stringParam, logLevel);
                } else if (check == ValidationType.TEXT_CONTENT_IS_UUID) {
                    checkNodeTextIsValidUuid(n, tagKey);
                } else if (check == ValidationType.TEXT_CONTENT_MATCHES_PATTERN_FOR_ALL_INSTANCES) {
                    checkNodeTextMatchesPattern(n, stringParam, tagKey, logLevel);
                } else if (check == ValidationType.TEXT_CONTENT_IS_ISO_8601_STRING) {
                    checkNodeTextIsValidIso8601String(n, tagKey);
                }
            }
        }

        reset();
    }

    private void reset() {
        startNode = null;
        xpath = null;
        tagKey = null;
        stringParam = "";
        logLevel = Type.ERROR;
    }

    public void validateXmlSchema(org.w3c.dom.Document doc) {
        Validator validator = getIsoSchemaValidator();
        try {
            validator.validate(new DOMSource(doc));
            IsoValidationErrorHandler errorHandler = (IsoValidationErrorHandler) validator.getErrorHandler();

            if (!errorHandler.hasValidationErrors) {
                String msgKey = "xml.schema.validation.pass";
                String defaultMsg = "XML schema validation successful";
                info(msgKey, defaultMsg);
            }
        } catch (SAXException e) {
            String msgKey = "xml.schema.validation.error";
            String defaultMsg = "XML schema validation error: " + e.getMessage();
            error(msgKey, defaultMsg, e.getMessage());
        } catch (IOException e) {
            String msgKey = "xml.schema.validation.io_exception";
            String defaultMsg = "XML document couldn't be read: " + e.getMessage();
            error(msgKey, defaultMsg, e.getMessage());
        }
    }

    private Validator getIsoSchemaValidator() {
        try {
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            URL url = getClass().getClassLoader().getResource(GMD_XSD_FILESYSTEM_LOCATION);
            Schema schema = sf.newSchema(url);

            Validator validator = schema.newValidator();
            validator.setErrorHandler(new IsoValidationErrorHandler());

            return validator;
        } catch (SAXException ex) {
            String msgKey = "xml.schema.definition.invalid";
            String defaultMsg = "ISO GMD Schema at following location is invalid: " + GMD_XSD_FILESYSTEM_LOCATION;
            String msg = getLocalisedString(
                    msgKey,
                    defaultMsg,
                    GMD_XSD_FILESYSTEM_LOCATION);
            warn(msgKey, defaultMsg);
            LOG.error(msg, ex);
        }
        return null;
    }


    private void checkNodeTextIsExactValue(Node node, String expectedValue, String tagKey, Type logLevel) {
        String tagName = tagNameFor(tagKey);
        String value = node.getText();
        if (!value.equals(expectedValue)) {
            String msgKey = "node.value.invalid";
            String defaultMsg = tagName + ": Node has invalid value: " + value;
            pushMessage(logLevel, msgKey, defaultMsg, tagName, expectedValue, value, node.getUniquePath());
        }
    }

    private void checkNodeTextIsFloatingPointNumber(Node node, String tagKey) {
        String value = node.getText();
        try {
            Double.parseDouble(value);
        } catch (NumberFormatException ignored) {
            String tagName = tagNameFor(tagKey);
            String msgKey = "node.value.invalid.floating_point_number";
            String defaultMsg = tagName + ": Value isn't a valid number: " + value;
            error(msgKey, defaultMsg, tagName, value, node.getUniquePath());
        }
    }

    private void checkNodeTextIsLessThanOrEqualTo(Node node, String tagKey, String arg, Type logLevel) {
        // Convert to double and compare the double values
        String text = node.getText();
        double nodeValue = textToNumber(text);
        double argValue = textToNumber(arg);
        if (nodeValue > argValue) {
            String tagName = tagNameFor(tagKey);
            String msgKey = "node.value.invalid.lte";
            String defaultMsg = String.format("%s: Text value '%s' isn't less than or equal to '%s'", tagName, text, arg);
            pushMessage(logLevel, msgKey, defaultMsg, tagName, arg, text, node.getUniquePath());
        }
    }

    private void checkNodeTextIsGreaterThanOrEqualTo(Node node, String tagKey, String arg, Type logLevel) {
        // Convert to double and compare the double values
        String text = node.getText();
        double nodeValue = textToNumber(text);
        double argValue = textToNumber(arg);
        if (nodeValue < argValue) {
            String tagName = tagNameFor(tagKey);
            String msgKey = "node.value.invalid.gte";
            String defaultMsg = String.format("%s: Text value '%s' isn't greater than or equal to '%s'", tagName, text, arg);
            pushMessage(logLevel, msgKey, defaultMsg, tagName, arg, text, node.getUniquePath());
        }
    }

    private double textToNumber(String arg) {
        try {
            return Double.parseDouble(arg);
        } catch (NumberFormatException ignored) {
            throw new IllegalArgumentException("Not a valid number: " + arg);
        }
    }

    private void checkNodeTextMatchesPattern(Node node, String pattern, String tagKey, Type logLevel) {
        String tagName = tagNameFor(tagKey);
        String value = node.getText();
        if (!value.matches(pattern)) {
            String msgKey = "node.value.doesnt.match.pattern";
            String defaultMsg = tagName + ": Node has invalid value: " + value;
            pushMessage(logLevel, msgKey, defaultMsg, tagName, value, pattern, node.getUniquePath());
        }
    }

    private void checkAtLeastOneNodeMatchesPattern(List<Node> nodes, String pattern, String xpath, String tagKey) {
        boolean found = false;
        for(int i=0; !found && i<nodes.size(); i++) { // breaks as soon as found == true
            String text = nodes.get(i).getText();
            found = text.matches(pattern);
        }
        if (!found) {
            String tagName = tagNameFor(tagKey);
            String msgKey = "node.value.none.matches.pattern";
            String defaultMsg = xpath + ": None of the nodes matches the pattern: " + pattern;
            error(msgKey, defaultMsg, tagName, pattern, xpath);
        }
    }

    private void checkNodeTextIsValidUuid(Node node, String tagKey) {
        if (!node.getText().matches(UUID_REGEX_PATTERN)) {
            String msgKey = "uuid.invalid";
            String tagName = tagNameFor(tagKey);
            String defaultMsg = tagName + ": Node has invalid UUID value.";
            error(msgKey, defaultMsg, tagName, node.getUniquePath());
        }
    }

    private void checkNodeTextIsValidIso8601String(Node node, String tagKey) {
        String dateString = node.getText();

        // Dates without time
        boolean valid = canParse(dateString, LocalDate::from, ISO_DATE, BASIC_ISO_DATE) // Dates without time
                || canParse(dateString, LocalDateTime::from, ISO_LOCAL_DATE_TIME) // Dates with time but no time zone
                || canParse(dateString, ZonedDateTime::from,  ISO_OFFSET_DATE_TIME, ISO_ZONED_DATE_TIME, ISO_INSTANT); // Dates with time and time zone

        String tagName = tagNameFor(tagKey);
        String xpath = node.getUniquePath();
        if (!valid) {
            String msgKey = "node.value.invalid.date_time";
            String defaultMsg = tagName + ": Node text isn't a valid date/date-time.";
            error(msgKey, defaultMsg, tagName, dateString, xpath);
        }
    }

    private boolean canParse(String dateString, TemporalQuery<?> query, DateTimeFormatter... formatters) {
        boolean valid = false;
        for (int i=0; !valid && i<formatters.length; i++) { // breaks as soon as valid = true
            DateTimeFormatter formatter = formatters[i];
            try {
                formatter.parse(dateString, query);
                valid = true;
            } catch (DateTimeParseException ignored) {}
        }
        return valid;
    }

    @SuppressWarnings("unchecked")
    public List<Node> selectNodes(String xpath) {
        return doc.selectNodes(xpath);
    }

    @SuppressWarnings("unchecked")
    public List<Node> selectNodes(Node ancestor, String xpath) {
        return ancestor.selectNodes(xpath);
    }

    private void info(String msgKey, String defaultMsg, Object... params) {
        pushMessage(Type.INFO, msgKey, defaultMsg, params);
    }

    private void warn(String msgKey, String defaultMsg, Object... params) {
        pushMessage(Type.WARN, msgKey, defaultMsg, params);
    }

    private void error(String msgKey, String defaultMsg, Object... params) {
        pushMessage(Type.ERROR, msgKey, defaultMsg, params);
    }

    private void pushMessage(Type level, String msgKey, String defaultMsg, Object... params) {
        String msg = getLocalisedString(msgKey, defaultMsg, params);
        protocolHandler.addMessage(level, msg);
    }

    private String tagNameFor(String tagKey) {
        return getLocalisedString(tagKey, "???" + tagKey + "???");
    }

    private String getLocalisedString(String key, String defaultValue, Object... params) {
        for(ResourceBundle b: bundles) {
            try {
                return MessageFormat.format(b.getString(key), params);
            } catch (MissingResourceException ignored) {}
        }
        return defaultValue;
    }

    public enum ValidationType {
        EXACTLY_ONE_NODE_EXISTS,
        ONE_OR_MORE_NODES_EXIST,
        ONE_OR_MORE_DESCENDANTS_EXIST,
        TEXT_CONTENT_IS_NOT_EMPTY,
        TEXT_CONTENT_EQUALS,
        TEXT_CONTENT_IS_FLOATING_POINT_NUMBER,
        TEXT_CONTENT_IS_LESS_THAN_OR_EQUAL_TO,
        TEXT_CONTENT_IS_GREATER_THAN_OR_EQUAL_TO,
        TEXT_CONTENT_MATCHES_PATTERN_FOR_ALL_INSTANCES,
        TEXT_CONTENT_MATCHES_PATTERN_AT_LEAST_ONCE,
        TEXT_CONTENT_IS_UUID,
        TEXT_CONTENT_IS_ISO_8601_STRING;
    }

    private class IsoValidationErrorHandler implements ErrorHandler {

        private boolean hasValidationErrors = false;

        @Override
        public void warning(SAXParseException e) throws SAXException {
            protocolHandler.addMessage(Type.WARN, e.getMessage());
        }

        @Override
        public void error(SAXParseException e) throws SAXException {
            hasValidationErrors = true;
            String ignored = "cvc-complex-type.2.4.a: Invalid content was found starting with element 'gmx:Anchor'. One of '{\"http://www.isotc211.org/2005/gco\":CharacterString}' is expected.";
            String message = e.getMessage();
            if (!ignored.equals(message)) { // TODO find way to modify xml schema instead of comparing strings
                protocolHandler.addMessage(Type.ERROR, message);
            }
        }
        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            hasValidationErrors = true;
            protocolHandler.addMessage(Type.ERROR, e.getMessage());
            throw e;
        }

    }

}
