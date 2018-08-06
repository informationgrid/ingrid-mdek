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

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.net.URL;
import java.util.List;

/**
 * Interface for defining rules for validating ISO 19115:2003/Corrigendum
 * 1:2006(E) XML-files.
 *
 * @author Vikram Notay
 */
public final class ISO_19115_2003_SchemaValidator extends AbstractIsoValidator {

    private static final Logger LOG = Logger.getLogger(ISO_19115_2003_SchemaValidator.class);

    private static final String GMD_XSD_FILESYSTEM_LOCATION = "org/isotc211/2005/gmd/gmd.xsd";

    public ISO_19115_2003_SchemaValidator() {
    }

    @Override
    List<ValidationReportItem> validate(Document w3cDoc) {
        ValidationReportHelper reportHelper = new ValidationReportHelper();
        validateAgainstSchema(w3cDoc, reportHelper);
        return reportHelper.getReport();
    }

    private void validateAgainstSchema(Document document, ValidationReportHelper reportHelper) {
        Validator validator = getIsoSchemaValidator();
        try {
            validator.validate(new DOMSource(document));

            reportHelper.pass(
                    "validation.iso.pass.schema_validation",
                    "Schema validation passed.");
        } catch (SAXException ex) {
            reportHelper.fail(
                    "validation.iso.fail.schema_validation",
                    "XML Document is invalid",
                    ex.getMessage());
        } catch (IOException ex) {
            String msg = ValidationReportHelper.getLocalisedString(
                    "validation.iso.xml_document_unreadable",
                    "XML Document cannot be read");
            LOG.error(msg, ex);
        }
    }

    private Validator getIsoSchemaValidator() {
        try {
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            URL url = getClass().getClassLoader().getResource(GMD_XSD_FILESYSTEM_LOCATION);
            Schema schema = sf.newSchema(url);

            return schema.newValidator();
        } catch (SAXException ex) {
            String msg = ValidationReportHelper.getLocalisedString(
                    "validation.iso.invalid_schema_definition",
                    "ISO GMD Schema at following location is invalid: " + GMD_XSD_FILESYSTEM_LOCATION,
                    GMD_XSD_FILESYSTEM_LOCATION);
            LOG.error(msg, ex);
        }
        return null;
    }

}
