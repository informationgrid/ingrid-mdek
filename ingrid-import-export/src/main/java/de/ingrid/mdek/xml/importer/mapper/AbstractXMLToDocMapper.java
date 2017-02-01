/*
 * **************************************************-
 * ingrid-import-export
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
package de.ingrid.mdek.xml.importer.mapper;

import static de.ingrid.mdek.xml.util.IngridDocUtils.putDocList;
import static de.ingrid.mdek.xml.util.IngridDocUtils.putInt;
import static de.ingrid.mdek.xml.util.IngridDocUtils.putString;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekUtils.SearchtermType;
import de.ingrid.mdek.xml.util.XPathUtils;
import de.ingrid.utils.IngridDocument;

abstract public class AbstractXMLToDocMapper {

	protected final static String X_ATTRIBUTE_ID = "@id";
	protected final static String X_ATTRIBUTE_GEMET_ID = "@gemet-id";
	protected final static String X_ATTRIBUTE_ALTERNATE_NAME = "@alternate-name";
	protected final static String X_ATTRIBUTE_SOURCE = "@source";

	private static final String CONTROLLED_TERM = "controlled-term";
	private static final String UNCONTROLLED_TERM = "uncontrolled-term";

	private static final String UMTHES = "UMTHES";
	private static final String GEMET = "GEMET";
	private static final String INSPIRE = "INSPIRE";
	private static final String TOPIC = "Topic";
	private static final String OPENDATA = "OpenData";

	protected static void mapSubjectTerms(NodeList terms, IngridDocument target) {
		List<IngridDocument> termList = new ArrayList<IngridDocument>();
		List<IngridDocument> termInspireList = new ArrayList<IngridDocument>();
		List<IngridDocument> openDataList = new ArrayList<IngridDocument>();
		List<Integer> envTopicsList = new ArrayList<Integer>();

		for (int index = 0; index < terms.getLength(); index++) {
			Node term = terms.item(index);
			IngridDocument termDoc = new IngridDocument();

			putString(MdekKeys.TERM_NAME, term.getTextContent(), termDoc);

			String nodeName = term.getNodeName();
			if (CONTROLLED_TERM.equals(nodeName)) {
				String type = XPathUtils.getString(term, X_ATTRIBUTE_SOURCE);

				if (UMTHES.equals(type)) {
					putString(MdekKeys.TERM_TYPE, SearchtermType.UMTHES.getDbValue(), termDoc);
					putString(MdekKeys.TERM_SNS_ID, XPathUtils.getString(term, X_ATTRIBUTE_ID), termDoc);
					termList.add(termDoc);
				} else if (GEMET.equals(type)) {
					putString(MdekKeys.TERM_TYPE, SearchtermType.GEMET.getDbValue(), termDoc);
					putString(MdekKeys.TERM_SNS_ID, XPathUtils.getString(term, X_ATTRIBUTE_ID), termDoc);
					putString(MdekKeys.TERM_GEMET_ID, XPathUtils.getString(term, X_ATTRIBUTE_GEMET_ID), termDoc);
					putString(MdekKeys.TERM_ALTERNATE_NAME, XPathUtils.getString(term, X_ATTRIBUTE_ALTERNATE_NAME), termDoc);
					termList.add(termDoc);
				} else if (INSPIRE.equals(type)) {
					putString(MdekKeys.TERM_TYPE, SearchtermType.INSPIRE.getDbValue(), termDoc);
					putInt(MdekKeys.TERM_ENTRY_ID, XPathUtils.getInt(term, X_ATTRIBUTE_ID), termDoc);
					termInspireList.add(termDoc);
    			} else if (TOPIC.equals(type)) {
    			    envTopicsList.add(XPathUtils.getInt(term, X_ATTRIBUTE_ID));
    			} else if (OPENDATA.equals(type)) {
    			    putInt(MdekKeys.OPEN_DATA_CATEGORY_KEY, XPathUtils.getInt(term, X_ATTRIBUTE_ID), termDoc);
    			    openDataList.add(termDoc);
    			}

			} else if (UNCONTROLLED_TERM.equals(nodeName)) {
				putString(MdekKeys.TERM_TYPE, MdekUtils.SearchtermType.FREI.getDbValue(), termDoc);
				termList.add(termDoc);
			}
		}

		putDocList(MdekKeys.SUBJECT_TERMS, termList, target);
		putDocList(MdekKeys.SUBJECT_TERMS_INSPIRE, termInspireList, target);
        // NOTICE:
        // Env Topic category is only written as <subject-terms> when ISO Import.
        // With IGE export it is written as <env-topic>.
        // So we only add here if NOT empty to avoid deletion of already mapped <env-topic> !
        if (!envTopicsList.isEmpty()) {
            target.put(MdekKeys.ENV_TOPICS, envTopicsList);            
        }
		// NOTICE:
		// OpenData category is only written as <subject-terms> when ISO Import.
		// With IGE export it is written as <open-data-categories>.
		// So we only add here if NOT empty to avoid deletion of already mapped <open-data-categories> !
		if (!openDataList.isEmpty()) {
	        target.put(MdekKeys.OPEN_DATA_CATEGORY_LIST, openDataList);
		}
	}
}
