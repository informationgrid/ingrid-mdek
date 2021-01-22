/*
 * **************************************************-
 * ingrid-mdek-services
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
package de.ingrid.mdek.services.utils;

import java.io.StringReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.services.catalog.MdekCatalogService;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.dao.ISysGenericKeyDao;
import de.ingrid.mdek.services.persistence.db.model.SysGenericKey;
import de.ingrid.utils.xml.ConfigurableNamespaceContext;
import de.ingrid.utils.xml.IgcProfileNamespaceContext;
import de.ingrid.utils.xml.XPathUtils;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;


/**
 * Handles access to IGC Profile. 
 */
public class MdekIgcProfileHandler {

	private static final Logger LOG = LogManager.getLogger(MdekIgcProfileHandler.class);

	private static MdekIgcProfileHandler myInstance;

	private static String CACHE_PROFILE = "services-IGCProfile";
	private static String SYS_GENERIC_KEY_PROFILE = "profileXML";

	private ISysGenericKeyDao daoSysGenericKey;

	private CacheManager cacheManager;
	private Cache profileCache;

	/** Get The Singleton */
	public static synchronized MdekIgcProfileHandler getInstance(DaoFactory daoFactory) {
		if (myInstance == null) {
	        myInstance = new MdekIgcProfileHandler(daoFactory);
	      }
		return myInstance;
	}

	private MdekIgcProfileHandler(DaoFactory daoFactory) {
		daoSysGenericKey = daoFactory.getSysGenericKeyDao();

		URL url = getClass().getResource(MdekCatalogService.CACHE_CONFIG_FILE);
		cacheManager = new CacheManager(url);
		profileCache = cacheManager.getCache(CACHE_PROFILE);

		// initialize XPath Util
        ConfigurableNamespaceContext cnc = new ConfigurableNamespaceContext();
        cnc.addNamespaceContext(new IgcProfileNamespaceContext());
        XPathUtils.getXPathInstance().setNamespaceContext(cnc);
	}

	/** Get selection list of given field from profile as MAP. USES CACHE !<br>
	 * @param FieldKey unique key of field !
	 * @param language requested language
	 * @return Map with list items in requested language or empty map !
	 */
	public Map<String, String> getFieldSelectionListMap(String fieldKey, String language) {
		Map<String, String> map = null;

		// get map from cache !
		String cacheElemKey = fieldKey + language;
		net.sf.ehcache.Element cacheElem = cacheElem = profileCache.get(cacheElemKey);
		if (cacheElem != null) {
			return (Map<String, String>) cacheElem.getObjectValue();
		}
		
		// not cached -> create map and cache
		map = extractSelectionListFromProfile(fieldKey, language);
		if (!map.isEmpty()) {
			profileCache.put(new net.sf.ehcache.Element(cacheElemKey, map));			
		}

		return map;
	}

	/** Extract selection list via XPath from profile (xml). */
	private Map<String, String> extractSelectionListFromProfile(String fieldKey, String language) {
		Map<String, String> map = new HashMap<>();
		
		try {
			Document profile = getProfileDOM();
	        Node idNode = XPathUtils.getNode(profile, "//igcp:controls//igcp:id[text()='" + fieldKey + "']");

	        if (idNode == null) {
	        	LOG.debug("FieldKey not found in profile: " + fieldKey);
	        	return map;
			}

	        Node controlNode = idNode.getParentNode();
            NodeList listItems = XPathUtils.getNodeList(controlNode, "igcp:selectionList/igcp:items[@lang='" + language + "']/igcp:item");
            for (int i = 0; i < listItems.getLength(); i++) {
                Node localizedItem = listItems.item(i);
                String key = localizedItem.getAttributes().getNamedItem("id").getNodeValue();
                String value = localizedItem.getTextContent();
                map.put(key, value);
            }

		} catch (Exception exc) {
			String msg = "Problems extracting field selection list from profile, " +
				"fieldKey='" + fieldKey + "', language='" + language + "', we return empty map !\n";
			LOG.warn(msg, exc);
		}
		
		return map;
	}

	/** Get profile from cache or read from datatbase if not cached anymore. */
	private Document getProfileDOM() throws Exception {
		Document profile = null;

		// get profile from cache!
		String cacheElemKey = "igcProfileDOM";
		net.sf.ehcache.Element cacheElem = profileCache.get(cacheElemKey);
		if (cacheElem != null) {
			return (Document) cacheElem.getObjectValue();
		}

		// not cached -> read XML from database
		String profileXML = readProfile();
		// and create DOM
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db;
        db = dbf.newDocumentBuilder();
        profile = db.parse(new InputSource(new StringReader(profileXML)));
        // cache result
		profileCache.put(new net.sf.ehcache.Element(cacheElemKey, profile));

		return profile;
	}

	/** Read Profile (xml) from datatbase. Thows MdekException if problems. */
	private String readProfile() {
		List<SysGenericKey> sysGenericKeyList =
			daoSysGenericKey.getSysGenericKeys(new String[]{SYS_GENERIC_KEY_PROFILE});
		if (sysGenericKeyList.size() == 0) {
			String msg = "Problems reading Profile from database via SysGenericKey: '" +SYS_GENERIC_KEY_PROFILE + "'";
			LOG.error(msg);
			throw new MdekException(msg);
		}
		
		return sysGenericKeyList.get(0).getValueString();
	}
}
