package de.ingrid.mdek.services.catalog;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;
import de.ingrid.utils.IngridDocument;
import de.ingrid.utils.xml.ConfigurableNamespaceContext;
import de.ingrid.utils.xml.Csw202NamespaceContext;
import de.ingrid.utils.xpath.XPathUtils;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ImportLinkHandler {

    private final MdekObjectService objectService;

    public ImportLinkHandler(MdekObjectService objectService) {
        this.objectService = objectService;
    }

    public void handleCoupledResources(IngridDocument inDoc) {

        List<Object> linkages = inDoc.getArrayList(MdekKeys.LINKAGES);
        List<Object> handled = new ArrayList<>();

        for (Object linkage : linkages) {
            IngridDocument linkageDoc = (IngridDocument) linkage;
            boolean hasUuid = linkageDoc.containsKey(MdekKeys.LINKAGE_UUID);
            if (hasUuid) {
                String uuid = linkageDoc.getString(MdekKeys.LINKAGE_UUID);
                String uuidExists = checkUuidExists(uuid);

                if (uuidExists != null) {
                    addDocumentRelation(uuidExists, inDoc);
                    handled.add(linkage);
                    continue;
                }
            }

            String link = linkageDoc.getString(MdekKeys.LINKAGE_URL);
            Document content = null;
            try {
                content = getDocumentFromUrl(link, true);

                if (content != null) {
                    ConfigurableNamespaceContext ns = new ConfigurableNamespaceContext();
                    ns.addNamespaceContext(new Csw202NamespaceContext());
                    XPathUtils xPathUtils = new XPathUtils(ns);

                    boolean isDataset = "dataset".equals(xPathUtils.getString(content, "//gmd:MD_Metadata/gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue"));
                    if (isDataset) {
                        String fileIdentifier = xPathUtils.getString(content, "//gmd:MD_Metadata/gmd:fileIdentifier/gco:CharacterString");
                        String linkedDocExists = checkUuidExists(fileIdentifier);
                        if (linkedDocExists != null) {
                            addDocumentRelation(linkedDocExists, inDoc);
                            handled.add(linkage);
                        } else {
                            String title = xPathUtils.getString(content, "//gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString");
                            linkageDoc.put(MdekKeys.LINKAGE_NAME, title);
                            linkageDoc.putInt(MdekKeys.LINKAGE_REFERENCE_ID, 3600);
                            linkageDoc.put(MdekKeys.LINKAGE_REFERENCE, "coupledResource");
                            linkageDoc.put(MdekKeys.LINKAGE_DESCRIPTION, "Coupled Resource / operates on");
                        }
                    } else {
                        linkageDoc.put(MdekKeys.LINKAGE_NAME, "Verweis aus importiertem operatesOn Element");
                        linkageDoc.put(MdekKeys.LINKAGE_DESCRIPTION, "Dieser Verweis stammt aus dem Element 'srv:operatesOn@xlink:href' eines importierten Datensatzes.");
                    }
                }

            } catch (SAXException | IOException | ParserConfigurationException e) {
                e.printStackTrace();
            }
        }

        linkages.removeAll(handled);
    }

    private Document getDocumentFromUrl(String urlStr, boolean namespaceAware) throws SAXException, IOException, ParserConfigurationException {
        URL url = new URL(urlStr);
        // get the content in UTF-8 format, to avoid "MalformedByteSequenceException: Invalid byte 1 of 1-byte UTF-8 sequence"
        InputStream input = checkForUtf8BOMAndDiscardIfAny(url.openStream());
        Reader reader = new InputStreamReader(input, StandardCharsets.UTF_8);
        InputSource inputSource = new InputSource(reader);
        // Build a document from the xml response
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // nameSpaceAware is false by default. Otherwise we would have to
        // query for the correct namespace for every evaluation
        factory.setNamespaceAware(namespaceAware);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(inputSource);
    }

    /**
     * If an input stream starts with a BOM then it has to be removed/read before(!)
     * we parse it for XML handling.
     *
     * @param inputStream is the stream with the content to check for the BOM
     * @return an input stream with the correct starting position for reading
     * @throws IOException
     */
    private static InputStream checkForUtf8BOMAndDiscardIfAny(InputStream inputStream) throws IOException {
        PushbackInputStream pushbackInputStream = new PushbackInputStream(new BufferedInputStream(inputStream), 3);
        byte[] bom = new byte[3];
        if (pushbackInputStream.read(bom) != -1 && !(bom[0] == (byte) 0xEF && bom[1] == (byte) 0xBB && bom[2] == (byte) 0xBF)) {
            pushbackInputStream.unread(bom);
        }
        return pushbackInputStream;
    }

    private String getContentFromLink(String link) {
        try {
            return IOUtils.toString(new URL(link));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String checkUuidExists(String uuid) {
        ObjectNode objectNode = objectService.loadByUuid(uuid, MdekUtils.IdcEntityVersion.ALL_VERSIONS);
        if (objectNode == null) {
            objectNode = objectService.loadByOrigId(uuid, MdekUtils.IdcEntityVersion.ALL_VERSIONS);
        }

        // return the uuid in case object was found by its origId
        return objectNode != null ? objectNode.getObjUuid() : null;
    }

    private void addDocumentRelation(String uuid, IngridDocument inDoc) {
        IngridDocument doc = new IngridDocument();
        doc.put(MdekKeys.UUID, uuid);
        doc.put(MdekKeys.RELATION_TYPE_NAME, "Gekoppelte Daten");
        doc.putInt(MdekKeys.RELATION_TYPE_REF, 3600);

        inDoc.addToList(MdekKeys.OBJ_REFERENCES_TO, doc);
    }

}
