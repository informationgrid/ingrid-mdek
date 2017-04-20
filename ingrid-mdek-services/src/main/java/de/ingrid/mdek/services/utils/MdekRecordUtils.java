/*
 * **************************************************-
 * InGrid mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2017 wemove digital solutions GmbH
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
package de.ingrid.mdek.services.utils;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import de.ingrid.utils.dsc.Record;
import de.ingrid.utils.idf.IdfTool;

public class MdekRecordUtils {
    
    static Logger log = LogManager.getLogger( MdekRecordUtils.class );
    
    public static final String XSL_IDF_TO_ISO_FULL = "idf_1_0_0_to_iso_metadata.xsl";

    public static Document convertRecordToDocument(Record record) {
        try {
            String idfDataFromRecord = IdfTool.getIdfDataFromRecord(record);
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setNamespaceAware(true);
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            return builder.parse(new InputSource(new StringReader(idfDataFromRecord)));
        } catch (Exception ex) {
            log.error( "Could not convert record to Document Node: ", ex );
        }
        return null;
    }
}
