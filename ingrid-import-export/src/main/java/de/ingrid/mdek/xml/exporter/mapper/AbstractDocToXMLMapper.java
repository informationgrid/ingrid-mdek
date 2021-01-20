/*
 * **************************************************-
 * ingrid-import-export
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
package de.ingrid.mdek.xml.exporter.mapper;

import static de.ingrid.mdek.xml.XMLKeys.*;

import java.util.ArrayList;
import java.util.List;

import de.ingrid.mdek.EnumUtil;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils.SearchtermType;
import de.ingrid.mdek.xml.util.IngridDocUtils;
import de.ingrid.mdek.xml.util.XMLElement;
import de.ingrid.utils.IngridDocument;

abstract public class AbstractDocToXMLMapper {

	protected IngridDocument documentRoot;

	public AbstractDocToXMLMapper(IngridDocument document) {
		documentRoot = document;
	}

	protected XMLElement createSubjectTerms() {
		XMLElement subjectTerms = new XMLElement(SUBJECT_TERMS);
		subjectTerms.addChildren(createTerms());
		return subjectTerms;
	}

	protected List<XMLElement> createTerms() {
		List<XMLElement> terms = new ArrayList<XMLElement>();
		List<IngridDocument> termList = getIngridDocumentListForKey(MdekKeys.SUBJECT_TERMS);
		for (IngridDocument term : termList) {
			if (isControlledTerm(term)) {
				terms.add(createControlledTerm(term));
			} else {
				terms.add(createUncontrolledTerm(term));
			}
		}
		termList = getIngridDocumentListForKey(MdekKeys.SUBJECT_TERMS_INSPIRE);
		for (IngridDocument term : termList) {
			terms.add(createControlledTerm(term));
		}
		return terms;
	}

	protected boolean isControlledTerm(IngridDocument term) {
		SearchtermType termType = 
			EnumUtil.mapDatabaseToEnumConst(SearchtermType.class, getStringForKey(MdekKeys.TERM_TYPE, term));
		return (termType == SearchtermType.UMTHES ||
				termType == SearchtermType.GEMET ||
				termType == SearchtermType.INSPIRE);
	}

	protected XMLElement createControlledTerm(IngridDocument controlledTermContext) {
		XMLElement controlledTerm = new XMLElement(CONTROLLED_TERM, getStringForKey(MdekKeys.TERM_NAME, controlledTermContext));
		SearchtermType termType = 
			EnumUtil.mapDatabaseToEnumConst(SearchtermType.class, getStringForKey(MdekKeys.TERM_TYPE, controlledTermContext));
		if (termType == SearchtermType.UMTHES) {
			controlledTerm.addAttribute(ID, getStringForKey(MdekKeys.TERM_SNS_ID, controlledTermContext));
			controlledTerm.addAttribute(SOURCE, UMTHES);
		} else if (termType == SearchtermType.GEMET) {
			controlledTerm.addAttribute(ID, getStringForKey(MdekKeys.TERM_SNS_ID, controlledTermContext));
			controlledTerm.addAttribute(GEMET_ID, getStringForKey(MdekKeys.TERM_GEMET_ID, controlledTermContext));
			controlledTerm.addAttribute(ALTERNATE_NAME, getStringForKey(MdekKeys.TERM_ALTERNATE_NAME, controlledTermContext));
			controlledTerm.addAttribute(SOURCE, GEMET);
		} else if (termType == SearchtermType.INSPIRE) {
			controlledTerm.addAttribute(ID, getIntegerForKey(MdekKeys.TERM_ENTRY_ID, controlledTermContext));
			controlledTerm.addAttribute(SOURCE, INSPIRE);
		}
		return controlledTerm;
	}

	protected XMLElement createUncontrolledTerm(IngridDocument uncontrolledTermContext) {
		return new XMLElement(UNCONTROLLED_TERM, getStringForKey(MdekKeys.TERM_NAME, uncontrolledTermContext));
	}

	protected Integer getIntegerForKey(String key) {
		return getIntegerForKey(key, documentRoot);
	}

	protected Long getLongForKey(String key) {
		return getLongForKey(key, documentRoot);
	}

	protected Double getDoubleForKey(String key) {
		return getDoubleForKey(key, documentRoot);
	}

	protected String getStringForKey(String key) {
		return getStringForKey(key, documentRoot);
	}

	protected List<String> getStringListForKey(String key) {
		return getStringListForKey(key, documentRoot);
	}

	protected List<Integer> getIntegerListForKey(String key) {
		return getIntegerListForKey(key, documentRoot);
	}

	protected List<IngridDocument> getIngridDocumentListForKey(String key) {
		return getIngridDocumentListForKey(key, documentRoot);
	}

	protected IngridDocument getIngridDocumentForKey(String key) {
		return getIngridDocumentForKey(key, documentRoot);
	}

	protected static Integer getIntegerForKey(String key, IngridDocument context) {
		return IngridDocUtils.getIntegerForKey(key, context);
	}

	protected static Double getDoubleForKey(String key, IngridDocument context) {
		return IngridDocUtils.getDoubleForKey(key, context);
	}

	protected static Long getLongForKey(String key, IngridDocument context) {
		return IngridDocUtils.getLongForKey(key, context);
	}

	protected static String getStringForKey(String key, IngridDocument context) {
		return IngridDocUtils.getStringForKey(key, context);
	}

	protected static List<String> getStringListForKey(String key, IngridDocument context) {
		return IngridDocUtils.getStringListForKey(key, context);
	}

	protected static List<Integer> getIntegerListForKey(String key, IngridDocument context) {
		return IngridDocUtils.getIntegerListForKey(key, context);
	}

	protected static List<IngridDocument> getIngridDocumentListForKey(String key, IngridDocument context) {
		return IngridDocUtils.getIngridDocumentListForKey(key, context);
	}

	protected static IngridDocument getIngridDocumentForKey(String key, IngridDocument context) {
		return IngridDocUtils.getIngridDocumentForKey(key, context);
	}
}
