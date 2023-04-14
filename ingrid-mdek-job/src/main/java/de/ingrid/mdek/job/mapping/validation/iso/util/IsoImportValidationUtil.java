/*-
 * **************************************************-
 * InGrid mdek-job
 * ==================================================
 * Copyright (C) 2014 - 2023 wemove digital solutions GmbH
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
package de.ingrid.mdek.job.mapping.validation.iso.util;

import de.ingrid.mdek.job.protocol.ProtocolHandler;
import de.ingrid.mdek.job.protocol.ProtocolHandler.Type;
import org.apache.log4j.Logger;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;

@Deprecated
public final class IsoImportValidationUtil {

    private static final Logger LOG = Logger.getLogger(IsoImportValidationUtil.class);

    private static final String GMD_XSD_URL = "http://schemas.opengis.net/csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd";

    public static final String ISO_ELEMENTS_RESOURCE_BUNDLE = "de.ingrid.mdek.job.mapping.validation.iso.Elements";
    public static final String ISO_MESSAGES_RESOURCE_BUNDLE = "de.ingrid.mdek.job.mapping.validation.iso.Messages";
    public static final String CODELIST_BASE_URL = "http://standards.iso.org/iso/19139/resources/gmxCodelists.xml";

    private final ProtocolHandler protocolHandler;

    private final List<ResourceBundle> bundles;

    public IsoImportValidationUtil(org.w3c.dom.Document w3cDoc, ProtocolHandler protocolHandler, String... bundleBaseNames) {
        this.protocolHandler = protocolHandler;

        bundles = new ArrayList<>();
        for(String baseName: bundleBaseNames) {
            bundles.add(PropertyResourceBundle.getBundle(baseName));
        }

        reset();
    }

    private void reset() {
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
            URL url = new URL(GMD_XSD_URL);
            Schema schema = sf.newSchema(url);

            Validator validator = schema.newValidator();
            validator.setErrorHandler(new IsoValidationErrorHandler());

            return validator;
        } catch (SAXException ex) {
            String msgKey = "xml.schema.definition.invalid";
            String defaultMsg = "ISO GMD Schema at following location is invalid: " + GMD_XSD_URL;
            String msg = getLocalisedString(
                    msgKey,
                    defaultMsg,
                    GMD_XSD_URL);
            warn(msgKey, defaultMsg);
            LOG.error(msg, ex);
        } catch (MalformedURLException ignored) {
            // Ignore because we know that the URL is okay
        }
        return null;
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

    private String getLocalisedString(String key, String defaultValue, Object... params) {
        for(ResourceBundle b: bundles) {
            try {
                return MessageFormat.format(b.getString(key), params);
            } catch (MissingResourceException ignored) {}
        }
        return defaultValue;
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
