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

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;

class ValidatorTestsTemplateHelper {
    private static final String TEMPLATE_PATH = "src/test/resources/de/ingrid/mdek/job/validation/iso/bawdmqs/baw_template.xml.txt";

    static String fetchTemplateString() throws IOException {
        Path path = new File(TEMPLATE_PATH).toPath();
        return String.join("\n", Files.readAllLines(path));
    }

    static Document defaultDocument() throws SAXException, IOException, ParserConfigurationException {
        return documentWithValues(getDefaultValues());
    }

    static Document documentWithReplacedValues(int invalidFieldIndex, String invalidFieldValue) throws SAXException, IOException, ParserConfigurationException {
        String[] vals = getDefaultValues();
        vals[invalidFieldIndex] = invalidFieldValue;
        return documentWithValues(vals);
    }

    private static Document documentWithValues(Object[] values) throws SAXException, IOException, ParserConfigurationException {
        String template = fetchTemplateString();
        return documentFromTemplateWithValues(template, values);
    }

    static Document documentFromTemplate(String template) throws SAXException, IOException, ParserConfigurationException {
        return documentFromTemplateWithValues(template, getDefaultValues());
    }

    static Document documentFromTemplateWithReplacedValues(String template, int invalidFieldIndex, String invalidFieldValue) throws SAXException, IOException, ParserConfigurationException {
        String[] vals = getDefaultValues();
        vals[invalidFieldIndex] = invalidFieldValue;
        return documentFromTemplateWithValues(template, vals);
    }

    private static Document documentFromTemplateWithValues(String template, Object[] values) throws SAXException, IOException, ParserConfigurationException {
        String xml = MessageFormat.format(template, values);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);

        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }
    /* ************************************************************************
     * Ugly initialisation code
     **************************************************************************/

    static final int IDX_FILE_IDENTIFIER                  =  0;
    static final int IDX_MD_LANG_CODELIST                 =  1;
    static final int IDX_MD_LANG_VALUE                    =  2;
    static final int IDX_MD_CHARSET_CODELIST              =  3;
    static final int IDX_MD_CHARSET_VALUE                 =  4;
    static final int IDX_PARENT_IDENTIFIER                =  5;
    static final int IDX_MD_HIERARCHY_LEVEL_CODELIST      =  6;
    static final int IDX_MD_HIERARCHY_LEVEL_VALUE         =  7;
    static final int IDX_MD_HIERARCHY_LEVEL_NAME_VALUE    =  8;
    static final int IDX_MD_CONTACT_UUID                  =  9;
    static final int IDX_MD_CONTACT_ORG_NAME              = 10;
    static final int IDX_MD_CONTACT_ORG_EMAIL             = 11;
    static final int IDX_MD_CONTACT_ORG_URL               = 12;
    static final int IDX_MD_ROLE_CODE_LIST                = 13;
    static final int IDX_MD_ROLE_CODE_VALUE               = 14;
    static final int IDX_MD_DATESTAMP                     = 15;
    static final int IDX_MD_STANDARD_NAME                 = 16;
    static final int IDX_MD_STANDARD_VERSION              = 17;
    static final int IDX_DS_TITLE                         = 18;
    static final int IDX_DS_DATESTAMP                     = 19;
    static final int IDX_DS_DATE_TYPE_CODELIST            = 20;
    static final int IDX_DS_DATE_TYPE_VALUE               = 21;
    static final int IDX_DS_ABSTRACT                      = 22;
    static final int IDX_DS_CONTACT_UUID                  = 23;
    static final int IDX_DS_CONTACT_ORG_NAME              = 24;
    static final int IDX_DS_CONTACT_ORG_EMAIL             = 25;
    static final int IDX_DS_CONTACT_ORG_URL               = 26;
    static final int IDX_DS_ROLE_CODE_LIST                = 27;
    static final int IDX_DS_ROLE_CODE_VALUE               = 28;
    static final int IDX_BAW_AUFTRAGS_NR                  = 29;
    static final int IDX_DS_LANG_CODELIST                 = 30;
    static final int IDX_DS_LANG_VALUE                    = 31;
    static final int IDX_DS_CHARSET_CODELIST              = 32;
    static final int IDX_DS_CHARSET_VALUE                 = 33;
    static final int IDX_DS_GEOGRAPHIC_IDENTIFIER         = 34;
    static final int IDX_DS_EAST_BOUND                    = 35;
    static final int IDX_DS_SOUTH_BOUND                   = 36;
    static final int IDX_LINEAGE_STATEMENT                = 37;
    static final int IDX_TEMPORAL_RESOLUTION_UNITS        = 38;
    static final int IDX_BAW_DGS_PARAMETER_NAME           = 39;
    static final int IDX_BAW_DGS_ROLE                     = 40;

    private static String[] getDefaultValues() {
        return new String[] {
                "01234567-89ab-cdef-0123-456789abcdef",
                "http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#LanguageCode",
                "ger",
                "http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#MD_CharacterSetCode",
                "utf8",
                "01234567-89ab-cdef-0123-456789abcdef",
                "http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#MD_ScopeCode",
                "dataset",
                "Simulationslauf",
                "89abcdef-0123-4567-89ab-cdef01234567",
                "Bundesanstalt für Wasserbau",
                "info@baw.de",
                "http://www.baw.de/",
                "http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#CI_RoleCode",
                "pointOfContact",
                "2017-01-01T03:15:00+01:00",
                "ISO19115:2003;GDI-BAW",
                "2003(E)/Cor.1:2006(E);1.2:2016",
                "Test title for simulation",
                "2016-07-01T03:15:00+02:00",
                "http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#CI_DateTypeCode",
                "creation",
                "Test abstract for simulation",
                "89abcdef-0123-4567-89ab-cdef01234567",
                "Bundesanstalt für Wasserbau",
                "info@baw.de",
                "http://www.baw.de/",
                "http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#CI_RoleCode",
                "pointOfContact",
                "B3953.05.30.10018",
                "http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#LanguageCode",
                "ger",
                "http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#MD_CharacterSetCode",
                "utf8",
                "3901-729-776",
                "6.79836627101571",
                "51.1662831753112",
                "Some lineage statement",
                "s",
                "Durchfluss",
                "Randbedingung"
        };
    }

}
