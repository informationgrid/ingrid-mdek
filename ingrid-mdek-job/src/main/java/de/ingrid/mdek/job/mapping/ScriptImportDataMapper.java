/*
 * **************************************************-
 * Ingrid Portal MDEK Application
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.mdek.job.mapping;

import de.ingrid.iplug.dsc.index.DatabaseConnection;
import de.ingrid.iplug.dsc.utils.*;
import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.job.protocol.ProtocolHandler;
import de.ingrid.mdek.job.util.IgeCswFolderUtil;
import de.ingrid.mdek.services.catalog.MdekCatalogService;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.xml.Versioning;
import de.ingrid.mdek.xml.XMLKeys;
import de.ingrid.utils.IConfigurable;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.xml.ConfigurableNamespaceContext;
import de.ingrid.utils.xml.IDFNamespaceContext;
import de.ingrid.utils.xml.IgcProfileNamespaceContext;
import de.ingrid.utils.xml.XMLUtils;
import de.ingrid.utils.xpath.XPathUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ScriptImportDataMapper implements ImportDataMapper<Document, Document>, IConfigurable {

    final protected static Log log = LogFactory.getLog(ScriptImportDataMapper.class);

    // Injected by Spring
    private Resource[] mapperScript;

    // Injected by Spring
    private Resource template;

    private MdekCatalogService catalogService;

    private XPathUtils xpathUtils;

    private DatabaseConnection internalDatabaseConnection;

    private IgeCswFolderUtil igeCswFolderUtil;

    @Autowired
    public ScriptImportDataMapper(DaoFactory daoFactory, IgeCswFolderUtil igeCswFolderUtil) {
        catalogService = MdekCatalogService.getInstance(daoFactory);

        ConfigurableNamespaceContext cnc = new ConfigurableNamespaceContext();
        cnc.addNamespaceContext(new IDFNamespaceContext());
        cnc.addNamespaceContext(new IgcProfileNamespaceContext());

        xpathUtils = new XPathUtils(cnc);

        this.igeCswFolderUtil = igeCswFolderUtil;
    }

    @Override
    public void configure(PlugDescription plugDescription) {
        internalDatabaseConnection = (DatabaseConnection) plugDescription.getConnection();
    }

    @Override
    public void convert(Document sourceIso, Document targetIgc, ProtocolHandler protocolHandler) throws MdekException {
        Map<String, Object> parameters = new ConcurrentHashMap<>();

        try (Connection conn = DatabaseConnectionUtils.getInstance().openConnection(internalDatabaseConnection)) {
            // get DOM-tree from template-file. Ignore the provided document
            Document templateXml = getDomFromSourceData(template.getInputStream(), false);
            Node importNode = targetIgc.importNode(templateXml.getDocumentElement(), true);
            targetIgc.appendChild(importNode);

            if (log.isDebugEnabled()) {
                log.debug("Target XML template:\n" + XMLUtils.toString(targetIgc));
            }
            // create utils for script (use function to be able to mock it)
            SQLUtils sqlUtils = getSqlUtils(conn);
            // get initialized XPathUtils (see above)
            TransformationUtils trafoUtils = new TransformationUtils(sqlUtils);
            DOMUtils domUtils = new DOMUtils(targetIgc, xpathUtils);

            preProcessMapping(targetIgc);

            parameters.put("source", sourceIso);
            parameters.put("target", targetIgc);
            parameters.put("protocolHandler", protocolHandler);
            parameters.put("codeListService", catalogService);
            parameters.put("javaVersion", System.getProperty("java.version"));
            parameters.put("SQL", sqlUtils);
            parameters.put("XPATH", xpathUtils);
            parameters.put("TRANSF", trafoUtils);
            parameters.put("DOM", domUtils);
            parameters.put("igeCswFolderUtil", igeCswFolderUtil);
            parameters.put("IDF", new IdfUtils(sqlUtils, domUtils, xpathUtils));

            parameters.put("log", log);

            // the template represents only one object!
            // Better if docTarget is only header and footer where
            // new objects made from template will be put into?
            //parameters.put("template", template);
            doMap(parameters, targetIgc);

            String targetString = XMLUtils.toString(targetIgc);
            if (log.isDebugEnabled()) {
                log.debug("Resulting XML:\n" + targetString);
            }
        } catch (Exception e) {
            log.error("Error while converting the input data!", e);
            String msg = "Problems converting import file: " + e;
            throw new MdekException(new MdekError(MdekErrorType.IMPORT_PROBLEM, msg));
        }
    }

    public SQLUtils getSqlUtils(Connection conn) {
        return new SQLUtils(conn);
    }

    private void mapAdditionalFields(Document docTarget, Context engine) throws Exception {
        String igcProfileStr = null;
        try (Connection conn = DatabaseConnectionUtils.getInstance().openConnection(internalDatabaseConnection)) {
            try (PreparedStatement ps = conn.prepareStatement("SELECT value_string AS igc_profile FROM sys_generic_key WHERE key_name='profileXML'")) {
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    igcProfileStr = rs.getString("igc_profile");
                    if (log.isDebugEnabled()) {
                        log.debug("igc profile found: " + igcProfileStr);
                    }
                }
            }
        }

        if (igcProfileStr != null) {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db;
            db = dbf.newDocumentBuilder();
            Document igcProfile = db.parse(new InputSource(new StringReader(igcProfileStr)));
            NodeList igcProfileCswMappingImports = xpathUtils.getNodeList(igcProfile, "//igcp:controls/*/igcp:scriptedCswMappingImport");
            if (log.isDebugEnabled()) {
                log.debug("cswMappingImport found: " + igcProfileCswMappingImports.getLength());
            }
            engine.getBindings("js").putMember("igcProfile", igcProfile);

            for (int i = 0; i < igcProfileCswMappingImports.getLength(); i++) {
                String igcProfileCswMapping = igcProfileCswMappingImports.item(i).getTextContent();

                // ScriptEngine.execute(this.mappingScripts, parameters, compile);
                engine.eval(Source.newBuilder("js", new StringReader(igcProfileCswMapping), "additionalMapping").build());
            }
        }
    }

    /**
     * After the mapping the document will be checked for importan information
     * that have to be available for saving it.
     *
     * @param docTarget, the mapped document
     */
    private void preProcessMapping(Document docTarget) {
        // write current exchange format from jar !
        setDocumentExchangeFormat(docTarget);

//		String title = XPathUtils.getString(docTarget, "/igc/data-sources/data-source/general/title");

        // generate uuid for each object (here only one for each import)
        // written in backend already
        //NodeList nodeList = docTarget.getElementsByTagName(XMLKeys.OBJECT_IDENTIFIER);
        //setValueInNodeList(nodeList, EntityHelper.getInstance().generateUuid());

        // generate dates for certain fields
        // written in backend already
        //setDocumentDates(docTarget);

        // write language information
        setDocumentLanguage(docTarget);

        // write responsible information
        // written in backend already
        //setDocumentUuids(docTarget);


    }

    /**
     * Set current xml exchange format from Versioning in import-export.jar !
     */
    private void setDocumentExchangeFormat(Document docTarget) {
        NodeList nodeList = docTarget.getElementsByTagName(XMLKeys.IGC);

        // !!! NOTICE !!!
        // Will be linked STATICALLY at compile time (all frontend class files) !!!
        // So will not be read from import-export jar at runtime !!!
        String exchangeFormat = Versioning.CURRENT_IMPORT_EXPORT_VERSION;

        setAttributesInNodeList(nodeList, XMLKeys.EXCHANGE_FORMAT, exchangeFormat);
    }

    private void setDocumentLanguage(Document docTarget) {
        List<NodeList> langNodesList = new ArrayList<NodeList>();
        langNodesList.add(docTarget.getElementsByTagName(XMLKeys.DATA_LANGUAGE));
        langNodesList.add(docTarget.getElementsByTagName(XMLKeys.METADATA_LANGUAGE));

        // receive the default values for a syslist ... here language
        String langId = catalogService.getInitialKeyFromListId(99999999).toString();
        String langString = catalogService.getInitialValueFromListId(99999999);

        for (NodeList nodeList : langNodesList) {
            setValueInNodeList(nodeList, langString);
            setAttributesInNodeList(nodeList, XMLKeys.ID, langId.toString());
        }

    }

    public void setCatalogService(MdekCatalogService catalogService) {
        this.catalogService = catalogService;
    }

    /**
     * Set one value in all tags of the given node list.
     *
     * @param nodeList
     * @param value
     */
    private void setValueInNodeList(NodeList nodeList, String value) {
        for (int i = 0; i < nodeList.getLength(); i++) {
            XMLUtils.createOrReplaceTextNode(nodeList.item(i), value);
        }
    }

    /**
     * Set one value in all attribute of tags of the given node list.
     *
     * @param nodeList
     * @param attr
     * @param value
     */
    private void setAttributesInNodeList(NodeList nodeList, String attr, String value) {
        for (int i = 0; i < nodeList.getLength(); i++) {
            XMLUtils.createOrReplaceAttribute(nodeList.item(i), attr, value);
        }
    }

    private void doMap(Map<String, Object> parameters, Document targetIgc) throws Exception {
        try (Context engine = this.getScriptEngine()) {

            // pass all parameters
            for (String param : parameters.keySet())
                engine.getBindings("js").putMember(param, parameters.get(param));


            // execute the mapping
            for (Resource resource : mapperScript) {
                log.debug("Mapping with script: " + resource);
                Reader inputStreamReader = new InputStreamReader(resource.getInputStream());
                engine.eval(Source.newBuilder("js", inputStreamReader, "importer-script").build());
            }

            mapAdditionalFields(targetIgc, engine);

        } catch (Exception e) {
            log.error("Error while accessing the mapper script!", e);
            throw e;
        }

    }

    private Document getDomFromSourceData(InputStream data, boolean isNameSpaceAware)
            throws Exception {
        Document doc = null;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(isNameSpaceAware);
            //dbf.setValidating(true);
            DocumentBuilder db = dbf.newDocumentBuilder();

            doc = db.parse(data);
        } catch (Exception e) {
            log.error("Problems extracting DOM from file!", e);
            e.printStackTrace();
            throw e;
        }
        return doc;
    }

    /**
     * Get the script engine (JavaScript). It returns always the same instance once initialized.
     *
     * @return script engine.
     */
    protected Context getScriptEngine() {
        return Context.newBuilder("js").allowAllAccess(true).build();
        /*context.getBindings("js").putMember("polyglot.js.allowAllAccess", true);
        
        if (engine == null) {
            ScriptEngineManager manager = new ScriptEngineManager();
            engine = manager.getEngineByName("JavaScript");
            engine.createBindings().put("polyglot.js.allowAllAccess", true);
        }
        return engine;*/
    }

    public void setMapperScript(Resource[] scripts) {
        this.mapperScript = scripts;
    }

    public void setTemplate(Resource tpl) {
        this.template = tpl;
    }
}
