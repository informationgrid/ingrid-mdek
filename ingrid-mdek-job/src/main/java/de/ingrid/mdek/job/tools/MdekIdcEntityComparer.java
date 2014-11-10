/*
 * **************************************************-
 * ingrid-mdek-job
 * ==================================================
 * Copyright (C) 2014 wemove digital solutions GmbH
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
/**
 * 
 */
package de.ingrid.mdek.job.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import de.ingrid.utils.IngridDocument;

/**
 * Compares Ingrid Entities (Objects, Addresses).
 * 
 * 
 * @author joachim
 * 
 */
public class MdekIdcEntityComparer {

	private static final Logger log = Logger.getLogger(MdekIdcEntityComparer.class);

	public static boolean compareObjectMaps(IngridDocument doc1, IngridDocument doc2, String[] ignoredKeysArray) {
		List<String> ignoredKeys;
		if (ignoredKeysArray != null) {
			ignoredKeys = new ArrayList<String>(Arrays.asList(ignoredKeysArray));
		} else {
			ignoredKeys = new ArrayList<String>();
		}
		ignoredKeys.add("date-of-last-modification");
		ignoredKeys.add("requestinfo_refetchEntity");
		ignoredKeys.add("user-id");
		try {
			compareDocuments(doc1, doc2, ignoredKeys);
		} catch (MdekIdcObjectCompareException e) {
			if (log.isDebugEnabled()) {
				log.debug(e.getMessage());
			}
			return false;
		} catch (Exception e) {
			log.error("General error while comparing objects.", e);
		}
		return true;
	}

	public static boolean compareAddressMaps(IngridDocument doc1, IngridDocument doc2, String[] ignoredKeysArray) {
		List<String> ignoredKeys;
		if (ignoredKeysArray != null) {
			ignoredKeys = new ArrayList<String>(Arrays.asList(ignoredKeysArray));
		} else {
			ignoredKeys = new ArrayList<String>();
		}
		ignoredKeys.add("date-of-last-modification");
		ignoredKeys.add("requestinfo_refetchEntity");
		ignoredKeys.add("user-id");
		try {
			compareDocuments(doc1, doc2, ignoredKeys);
		} catch (MdekIdcObjectCompareException e) {
			if (log.isDebugEnabled()) {
				log.debug(e.getMessage());
			}
			return false;
		} catch (Exception e) {
			log.error("General error while comparing addresses.", e);
		}
		return true;
	}

	private static void compareDocuments(IngridDocument doc1, IngridDocument doc2, List ignoredKeys) throws Exception {
		Set keysDoc1 = doc1.keySet();
		Set keysDoc2 = doc2.keySet();

		ArrayList<Object> missingPropertiesDoc1 = new ArrayList<Object>();
		ArrayList<Object> missingPropertiesDoc2 = new ArrayList<Object>();
		for (Object key : keysDoc2) {
			if (!keysDoc1.contains(key) && !ignoredKeys.contains(key)) {
				missingPropertiesDoc1.add(key);
			}
		}
		for (Object key : keysDoc1) {
			if (!keysDoc2.contains(key) && !ignoredKeys.contains(key)) {
				missingPropertiesDoc2.add(key);
			}
		}

		String missing1 = "";
		String missing2 = "";
		if (missingPropertiesDoc1.size() > 0) {
			missing1 = "(";
			for (Object p : missingPropertiesDoc1) {
				missing1 = missing1.concat("'" + p.toString() + "', ");
			}
			missing1 = missing1.substring(0, missing1.length() - 2).concat(")");
			throw new MdekIdcObjectCompareException(
					"Entities differ in properties. Properties exist in doc2, but not in doc1: " + missing1 + ".");
		}

		if (missingPropertiesDoc2.size() > 0) {
			missing2 = "(";
			for (Object p : missingPropertiesDoc2) {
				missing2 = missing2.concat("'" + p.toString() + "', ");
			}
			missing2 = missing2.substring(0, missing2.length() - 2).concat(")");
			throw new MdekIdcObjectCompareException(
					"Entities differ in properties. Properties exist in doc1, but not in doc2: " + missing2 + ".");
		}

		for (Object key : keysDoc1) {
			if (key instanceof String && !ignoredKeys.contains(key)) {
				if (doc1.get(key) instanceof String) {
					compareProperty((String) key, doc1, doc2, ignoredKeys);
				} else if (doc1.get(key) instanceof Integer) {
					compareProperty((String) key, doc1, doc2, ignoredKeys);
				} else if (doc1.get(key) instanceof Double) {
					compareProperty((String) key, doc1, doc2, ignoredKeys);
				} else if (doc1.get(key) instanceof Boolean) {
					compareProperty((String) key, doc1, doc2, ignoredKeys);
				} else if (doc1.get(key) instanceof List) {
					compareList((String) key, doc1, doc2, ignoredKeys);
				} else if (doc1.get(key) instanceof IngridDocument) {
					compareDocuments((String) key, doc1, doc2, ignoredKeys);
				}
			}
		}
	}

	private static void compareProperty(String key, IngridDocument doc1, IngridDocument doc2, List ignoredKeys)
			throws Exception {
		if (ignoredKeys.contains(key)) {
			return;
		}
		if (doc2.get(key) == null) {
			throw new MdekIdcObjectCompareException("Entities differ in property '" + key
					+ "'. (Property does not exists in doc2.)");
		}

		if (!doc1.get(key).toString().equals(doc2.get(key).toString())) {
			throw new MdekIdcObjectCompareException("Entities differ in property '" + key + "'. ('" + doc1.get(key)
					+ "' != '" + doc2.get(key) + "')");
		}
	}

	private static void compareList(String key, IngridDocument doc1, IngridDocument doc2, List ignoredKeys)
			throws Exception {
		if (ignoredKeys.contains(key)) {
			return;
		}
		List doc1List = (List) doc1.get(key);
		List doc2List = (List) doc2.get(key);

		if (doc1List.size() != doc2List.size()) {
			throw new MdekIdcObjectCompareException("List for property '" + key
					+ "' has different sizes for compares Lists. ('" + doc1List.size() + "' != '" + doc2List.size()
					+ "')");
		}

		for (int i = 0; i < doc1List.size(); i++) {
			if (doc1List.get(i) instanceof IngridDocument && doc2List.get(i) instanceof IngridDocument) {
				compareDocuments((IngridDocument) doc1List.get(i), (IngridDocument) doc2List.get(i), ignoredKeys);
			} else {
				log.error("Invalid list type for key '" + key + "'. It's not a IngridDocument!");
			}
		}
	}

	private static void compareDocuments(String key, IngridDocument doc1, IngridDocument doc2, List ignoredKeys)
			throws Exception {
		if (ignoredKeys.contains(key)) {
			return;
		}
		compareDocuments((IngridDocument) doc1.get(key), (IngridDocument) doc2.get(key), ignoredKeys);
	}

	private static class MdekIdcObjectCompareException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = -7919634623892970544L;

		public MdekIdcObjectCompareException(String msg) {
			super(msg);
		}

	}

}
