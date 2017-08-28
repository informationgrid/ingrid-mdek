/*
 * **************************************************-
 * Ingrid Portal MDEK Application
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
package de.ingrid.mdek.job.mapping;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import de.ingrid.iplug.dsc.index.DatabaseConnection;
import de.ingrid.iplug.dsc.utils.DOMUtils;
import de.ingrid.iplug.dsc.utils.DatabaseConnectionUtils;
import de.ingrid.iplug.dsc.utils.SQLUtils;
import de.ingrid.iplug.dsc.utils.TransformationUtils;
import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.job.protocol.ProtocolHandler;
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

public class ScriptImportDataMapper implements ImportDataMapper, IConfigurable {
	
	final protected static Log log = LogFactory.getLog(ScriptImportDataMapper.class);
	
	private ScriptEngine engine = null;

	// Injected by Spring
	private Resource[] mapperScript;
	
	// Injected by Spring
	private Resource template;

    private MdekCatalogService catalogService;

    private Connection dbConnection;

    private XPathUtils xpathUtils;

    private DatabaseConnection internalDatabaseConnection;

    @Autowired
	public ScriptImportDataMapper(DaoFactory daoFactory) {
        catalogService = MdekCatalogService.getInstance(daoFactory);
        
        ConfigurableNamespaceContext cnc = new ConfigurableNamespaceContext();
        cnc.addNamespaceContext(new IDFNamespaceContext());
        cnc.addNamespaceContext(new IgcProfileNamespaceContext());
        
        xpathUtils = new XPathUtils(cnc);
    }
    
    @Override
    public void configure(PlugDescription plugDescription) {
        internalDatabaseConnection = (DatabaseConnection) plugDescription.getConnection();
    }
	
	public InputStream convert(InputStream data, ProtocolHandler protocolHandler)
	throws MdekException {
		Map<String, Object> parameters = new Hashtable<String, Object>();
		InputStream targetStream = null;
		
		try {
		    // open database connection
		    dbConnection = DatabaseConnectionUtils.getInstance().openConnection(internalDatabaseConnection);
			// get DOM-tree from XML-file
			Document doc = getDomFromSourceData(data, true);
			// close the input file after it was read
			data.close();
			if (log.isDebugEnabled()) {
				String sourceString = XMLUtils.toString(doc);
				log.debug("Input XML:" + sourceString);
			}
			
			// get DOM-tree from template-file
			Document docTarget = getDomFromSourceData(template.getInputStream(), false);
            // create utils for script
            SQLUtils sqlUtils = new SQLUtils(dbConnection);
            // get initialized XPathUtils (see above)
            TransformationUtils trafoUtils = new TransformationUtils(sqlUtils);
            DOMUtils domUtils = new DOMUtils(docTarget, xpathUtils);
			
			preProcessMapping(docTarget);
			
		    parameters.put("source", doc);
		    parameters.put("target", docTarget);
            parameters.put("protocolHandler", protocolHandler );
		    parameters.put("codeListService", catalogService);
		    parameters.put("javaVersion", System.getProperty( "java.version" ));
		    parameters.put("SQL", sqlUtils);
		    parameters.put("XPATH", xpathUtils);
		    parameters.put("TRANSF", trafoUtils);
		    parameters.put("DOM", domUtils);
		    parameters.put("log", log);

		    // the template represents only one object!
		    // Better if docTarget is only header and footer where
		    // new objects made from template will be put into?
		    //parameters.put("template", template);
			doMap(parameters);
			
			mapAdditionalFields(docTarget);

			String targetString = XMLUtils.toString(docTarget);
			if (log.isDebugEnabled()) {
				log.debug("Resulting XML:" + targetString);
			}
			targetStream = new ByteArrayInputStream(targetString.getBytes("UTF-8"));
		} catch (Exception e) {
			log.error("Error while converting the input data!", e);
			String msg = "Problems converting import file: " + e;
//			String msg = "Problems converting file '" + protocolHandler.getCurrentFilename() + "': " + e;
//			protocolHandler.addMessage(msg + "\n");
			throw new MdekException(new MdekError(MdekErrorType.IMPORT_PROBLEM, msg));
		} finally {
            try {
                dbConnection.close();
            } catch (SQLException e) {
                log.error( "Error closing database connection", e );
            }
        }
		
		return targetStream;
	}
	
	private void mapAdditionalFields(Document docTarget) throws Exception {
        PreparedStatement ps = this.dbConnection.prepareStatement( "SELECT value_string AS igc_profile FROM sys_generic_key WHERE key_name='profileXML'" );
        ResultSet rs = ps.executeQuery();
        rs.next();
        String igcProfileStr = rs.getString("igc_profile");
        if (log.isDebugEnabled()) {
            log.debug("igc profile found: " + igcProfileStr);
        }
        ps.close();
        
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
            engine.put("igcProfile", igcProfile);

            for (int i=0; i<igcProfileCswMappingImports.getLength(); i++) {
                Node igcProfileNode = igcProfileCswMappingImports.item( i ).getOwnerDocument().getDocumentElement();
                String igcProfileCswMapping = igcProfileCswMappingImports.item(i).getTextContent();
                
                engine.put("igcProfile", igcProfile);
                // ScriptEngine.execute(this.mappingScripts, parameters, compile);
                engine.eval( new StringReader(igcProfileCswMapping) );
            }
        }
        
    }

    /**
	 * After the mapping the document will be checked for importan information
	 * that have to be available for saving it.
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
	
	/** Set current xml exchange format from Versioning in import-export.jar ! */
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
	 * @param nodeList
	 * @param value
	 */
	private void setValueInNodeList(NodeList nodeList, String value) {
		for (int i=0; i<nodeList.getLength(); i++) {
			XMLUtils.createOrReplaceTextNode(nodeList.item(i), value);
		}
	}
	
	/**
	 * Set one value in all attribute of tags of the given node list.
	 * @param nodeList
	 * @param attr
	 * @param value
	 */
	private void setAttributesInNodeList(NodeList nodeList, String attr, String value) {
		for (int i=0; i<nodeList.getLength(); i++) {
			XMLUtils.createOrReplaceAttribute(nodeList.item(i), attr, value);
		}
	}
	
	private void doMap(Map<String, Object> parameters) throws Exception {
        try {
	        ScriptEngine engine = this.getScriptEngine();
			
	        // pass all parameters
	        for(String param : parameters.keySet())
	        	engine.put(param, parameters.get(param));

	
			// execute the mapping
	        for (Resource resource : mapperScript) {
	            log.debug("Mapping with script: " + resource);
	            engine.eval(new InputStreamReader(resource.getInputStream()));
            }
	        
		} catch (ScriptException e) {
			log.error("Error while evaluating the script!", e);
			throw e;
		} catch (IOException e) {
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
		} catch ( Exception e ) {
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
	protected ScriptEngine getScriptEngine()  {
		if (engine == null) {
			ScriptEngineManager manager = new ScriptEngineManager();
	        engine = manager.getEngineByName("JavaScript");
		}
		return engine;
	}
	
	public void setMapperScript(Resource[] scripts) {
		this.mapperScript = scripts;
	}
	
	public void setTemplate(Resource tpl) {
		this.template = tpl;
	}
}
