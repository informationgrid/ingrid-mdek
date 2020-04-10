package de.ingrid.mdek.job.mapping;

import de.ingrid.iplug.dsc.om.DatabaseSourceRecord;
import de.ingrid.iplug.dsc.om.SourceRecord;
import de.ingrid.iplug.dsc.record.mapper.IIdfMapper;
import de.ingrid.iplug.dsc.record.mapper.ScriptedIdfMapper;
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

import javax.xml.transform.TransformerException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class IsoMetadataDateMapper implements IIdfMapper {

    public static final String[] ELEMENTS_REMOVED_FROM_ISO_BEFORE_FINGERPRINTING = {"/gmd:MD_Metadata/gmd:dateStamp", "//@gml:id"};

    private XsltUtils xsltUtils;
    private XPathUtils xpathUtils;

    private static final Logger LOG = Logger.getLogger(ScriptedIdfMapper.class);

    public IsoMetadataDateMapper() {
        this.xsltUtils = new XsltUtils();

        ConfigurableNamespaceContext cnc = new ConfigurableNamespaceContext();
        cnc.addNamespaceContext(new IDFNamespaceContext());
        cnc.addNamespaceContext(new IgcProfileNamespaceContext());

        xpathUtils = new XPathUtils(cnc);
    }

    @Override
    public void map(SourceRecord record, Document idf) throws Exception {
        if (!(record instanceof DatabaseSourceRecord)) {
            throw new IllegalArgumentException("Record is no DatabaseRecord!");
        }

        if (!(xpathUtils.nodeExists(idf, "//idf:html"))) {
            throw new IllegalArgumentException("Document is no IDF!");
        }

        // transform IDF to ISO
        Document iso = idf2iso(idf);

        // remove dynamic data like metadata date, GML ids
        prepareIsoForFingerprinting((Document) iso);

        // create fingerprint from ISO data
        String isoFingerprint = createFingerprint((Document) iso);

        // DO NOT CLOSE. Connection will be closed by the the DatabaseSourceRecord in DscRecordCreator
        Connection connection = (Connection) record.get(DatabaseSourceRecord.CONNECTION);

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


        // get stored fingerprint if any
        String sql = "SELECT iso_hash FROM t01_object WHERE id=?";

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
            XMLUtils.createOrReplaceTextNode(xpathUtils.getNode(idf, "/idf:html/idf:body/idf:idfMdMetadata/gmd:dateStamp/gco:Date"), UtilsCSWDate.mapFromIgcToIso8601(nowIgcDate));

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
