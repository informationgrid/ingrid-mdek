/*-
 * **************************************************-
 * InGrid mdek-services
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
package de.ingrid.mdek.services.catalog;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;
import de.ingrid.utils.IngridDocument;
import de.ingrid.utils.xml.ConfigurableNamespaceContext;
import de.ingrid.utils.xml.Csw202NamespaceContext;
import de.ingrid.utils.xpath.XPathUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ImportLinkHandler {

    private static final Logger LOG = LogManager.getLogger(ImportLinkHandler.class);

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

                    // skip documents that do not have a hierarchyLevel
                    Node hierarchyLevelNode = xPathUtils.getNode(content, "//gmd:MD_Metadata/gmd:hierarchyLevel");
                    if (hierarchyLevelNode == null) {
                        LOG.debug("XML Document does not contain hierarchyLevel-element: " + link);
                        continue;
                    }

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

            } catch (SAXException | FileNotFoundException e) {
                LOG.debug("Content could not be parsed, so we assume it's not a coupled resource or not available: " + link);
            } catch (IOException | ParserConfigurationException e) {
                LOG.error("Error handling coupled resource during import", e);
            }
        }

        linkages.removeAll(handled);
    }

    private Document getDocumentFromUrl(String urlStr, boolean namespaceAware) throws SAXException, IOException, ParserConfigurationException {
        URL url = new URL(urlStr);
        URLConnection con = url.openConnection();
        con.setConnectTimeout(3000);
        // get the content in UTF-8 format, to avoid "MalformedByteSequenceException: Invalid byte 1 of 1-byte UTF-8 sequence"
        InputStream input = checkForUtf8BOMAndDiscardIfAny(con.getInputStream());
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
