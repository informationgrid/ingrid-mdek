package de.ingrid.mdek.services.utils;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import de.ingrid.utils.dsc.Record;
import de.ingrid.utils.idf.IdfTool;

public class MdekRecordUtils {
    
    static Logger log = Logger.getLogger( MdekRecordUtils.class );
    
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
