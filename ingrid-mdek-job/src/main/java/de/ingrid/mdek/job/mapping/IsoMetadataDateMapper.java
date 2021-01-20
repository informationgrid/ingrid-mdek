/*-
 * **************************************************-
 * InGrid mdek-job
 * ==================================================
 * Copyright (C) 2014 - 2021 wemove digital solutions GmbH
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
package de.ingrid.mdek.job.mapping;

import de.ingrid.iplug.dsc.om.DatabaseSourceRecord;
import de.ingrid.iplug.dsc.om.SourceRecord;
import de.ingrid.iplug.dsc.record.mapper.IIdfMapper;
import de.ingrid.iplug.dsc.record.mapper.ScriptedIdfMapper;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.services.utils.MdekRecordUtils;
import de.ingrid.utils.tool.XsltUtils;
import de.ingrid.utils.udk.UtilsCSWDate;
import de.ingrid.utils.xml.ConfigurableNamespaceContext;
import de.ingrid.utils.xml.IDFNamespaceContext;
import de.ingrid.utils.xml.IgcProfileNamespaceContext;
import de.ingrid.utils.xml.XMLUtils;
import de.ingrid.utils.xpath.XPathUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.StringReader;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * <p>IDF Mapper to adjust the metadata date (gmd:dateStamp) according to the
 * changes to the ISO 19139 XML format compared to the previous export. Therefore
 * a fingerprint is taken from the ISO 19139 XML format and stored in the
 * database. The Mapper compares the generated ISO 19139 XML format with the
 * stored fingerprint. If the fingerprints differ, the metadata date is changed
 * to the current date. This applies only to published datasets.</p>
 * <p>See https://redmine.informationgrid.eu/issues/1084 for details.</p>
 *
 */
public class IsoMetadataDateMapper implements IIdfMapper {

    public static final String[] ELEMENTS_REMOVED_FROM_ISO_BEFORE_FINGERPRINTING = {"/gmd:MD_Metadata/gmd:dateStamp", "//@gml:id"};

    private XsltUtils xsltUtils;
    private XPathUtils xpathUtils;
    private DocumentBuilderFactory dbf = null;
    private DocumentBuilder db = null;

    private static final Logger LOG = Logger.getLogger(ScriptedIdfMapper.class);

    public IsoMetadataDateMapper() throws ParserConfigurationException {
        this.xsltUtils = new XsltUtils();

        ConfigurableNamespaceContext cnc = new ConfigurableNamespaceContext();
        cnc.addNamespaceContext(new IDFNamespaceContext());
        cnc.addNamespaceContext(new IgcProfileNamespaceContext());

        xpathUtils = new XPathUtils(cnc);

        dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        db = dbf.newDocumentBuilder();

    }

    /**
     * <p>Modify the IDF of published records as follows:</p>
     * <ul>
     *     <li>Check if the database record is published, skip if not.</li>
     *     <li>Create a prepared ISO XML document (strip metadata date and random data link gml:id elements).</li>
     *     <li>Create a finger print (SHA256) from the prepared ISO XML.</li>
     *     <li>Compare the finger print with the one stored in DB.</li>
     *     <li>If different, store the new finger print and the current date in database. Set metadata date in IDF to current date.</li>
     * </ul>
     *
     * @param record
     * @param idf
     * @throws Exception
     */
    @Override
    public void map(SourceRecord record, Document idf) throws Exception {
        if (!(record instanceof DatabaseSourceRecord)) {
            throw new IllegalArgumentException("Record is no DatabaseRecord!");
        }

        Object idObj = record.get("id");
        Number id = null;
        if (idObj instanceof String) {
            id = Long.decode((String) idObj);
        } else if (idObj instanceof Number) {
            id = (Number) idObj;
        } else {
            LOG.error("Error understanding database ID: " + idObj + ". Skipping record!");
            return;
        }

        // DO NOT CLOSE. Connection will be closed by the DatabaseSourceRecord in DscRecordCreator
        Connection connection = (Connection) record.get(DatabaseSourceRecord.CONNECTION);

        // test if dataset is published, skip if not
        String sql = "SELECT work_state FROM t01_object WHERE id=?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id.longValue());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String workState = rs.getString(1);
                    if (!workState.equals(MdekUtils.WorkState.VEROEFFENTLICHT.getDbValue())) {
                        return;
                    }
                }
            }
        }


        if (!(xpathUtils.nodeExists(idf, "//idf:html"))) {
            throw new IllegalArgumentException("Document is no IDF!");
        }
        // transform IDF to ISO

        /* Strange behavior: The transformation of the idf document
           directly to iso fails. It's not clear why.
           When the idf is transformed to a string representation
           and then parsed to a Document idfClone, it works.

           Only visible difference: The idf document is from typ DocumentImpl
           while the idfClone is from type DeferredDocumentImpl.

           Also the following error messages appear on system out:

                SystemId Unknown; Line #60; Column #64; Attempting to generate a namespace prefix with a null URI
                SystemId Unknown; Line #38; Column #46; org.w3c.dom.DOMException: NAMESPACE_ERR: An attempt is made to create or change an object in a way which is incorrect with regard to namespaces.

           which refer to the XSLT source file.

           TODO: Find the cause and spare the transformation to string and back.
        */
        String idfString = XMLUtils.toString(idf);
        Document idfClone = db.parse(new InputSource(new StringReader(idfString)));

        Document iso = idf2iso(idfClone);

        // remove dynamic data like metadata date, GML ids
        prepareIsoForFingerprinting((Document) iso);

        // create fingerprint from ISO data
        String isoFingerprint = createFingerprint((Document) iso);

        // get stored fingerprint if any
        sql = "SELECT iso_hash FROM t01_object WHERE id=?";

        String storedFingerprint = null;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id.longValue());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    storedFingerprint = rs.getString(1);
                }
            }
        }

        // fingerprints differ or no fingerprint was found in DB
        if (storedFingerprint == null || !storedFingerprint.equals(isoFingerprint)) {
            // set metadata date in IDF
            String nowIgcDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
            if (xpathUtils.nodeExists(idf, "/idf:html/idf:body/idf:idfMdMetadata/gmd:dateStamp/gco:Date")) {
                XMLUtils.createOrReplaceTextNode(xpathUtils.getNode(idf, "/idf:html/idf:body/idf:idfMdMetadata/gmd:dateStamp/gco:Date"), UtilsCSWDate.mapFromIgcToIso8601(UtilsCSWDate.getDateWithoutTime(nowIgcDate)));
            } else if (xpathUtils.nodeExists(idf, "/idf:html/idf:body/idf:idfMdMetadata/gmd:dateStamp/gco:DateTime")) {
                XMLUtils.createOrReplaceTextNode(xpathUtils.getNode(idf, "/idf:html/idf:body/idf:idfMdMetadata/gmd:dateStamp/gco:DateTime"), UtilsCSWDate.mapFromIgcToIso8601(nowIgcDate));
            } else {
                LOG.error("No compatible node found to add metadata date to the IDF. Supported nodes are: '/idf:html/idf:body/idf:idfMdMetadata/gmd:dateStamp[./gco:Date or ./gco:DateTime]'");
                throw new RuntimeException("No compatible node found to add metadata date to the IDF. Supported nodes are: '/idf:html/idf:body/idf:idfMdMetadata/gmd:dateStamp[./gco:Date or ./gco:DateTime]'");
            }

            sql = "UPDATE t01_object SET iso_hash=?, metadata_time=? WHERE id=?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, isoFingerprint);
                ps.setString(2, nowIgcDate);
                ps.setLong(3, id.longValue());
                ps.execute();
            }
        }
    }

    public Document idf2iso(Document idf) throws Exception {
        return (Document) xsltUtils.transform(idf, MdekRecordUtils.XSL_IDF_TO_ISO_FULL);
    }


    public void prepareIsoForFingerprinting(Document doc) {
        for (String xpath : ELEMENTS_REMOVED_FROM_ISO_BEFORE_FINGERPRINTING) {
            removeNodes(doc, xpath);
        }
    }

    public String createFingerprint(Document doc) throws TransformerException, NoSuchAlgorithmException {
        String isoDocAsString = XMLUtils.toString(doc, false);
        return DigestUtils.sha256Hex(isoDocAsString);
    }

    private void removeNodes(Node isoDoc, String xpath) {
        NodeList nodes = xpathUtils.getNodeList(isoDoc, xpath);
        for (int i = 0; i < nodes.getLength(); i++) {
            XMLUtils.remove(nodes.item(i));
        }
    }

}
