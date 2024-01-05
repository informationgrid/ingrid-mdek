/*
 * **************************************************-
 * ingrid-import-export
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
package de.ingrid.mdek.xml.importer.mapper.version330;

import static de.ingrid.mdek.xml.util.IngridDocUtils.putDocList;
import static de.ingrid.mdek.xml.util.IngridDocUtils.putInt;
import static de.ingrid.mdek.xml.util.IngridDocUtils.putString;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.xml.importer.mapper.AbstractXMLToDocMapper;
import de.ingrid.mdek.xml.util.XPathUtils;
import de.ingrid.utils.IngridDocument;

public class XMLAddressToDocMapper extends AbstractXMLToDocMapper {

	private static final String X_ADDRESS = "//address-instance";

	private static final String X_WORK_STATE = X_ADDRESS + "/@work-state";
	private final static String X_ADDRESS_IDENTIFIER = X_ADDRESS + "/address-identifier/text()";
	private final static String X_MODIFICATOR_IDENTIFIER = X_ADDRESS + "/modificator-identifier/text()";
	private final static String X_RESPONSIBLE_IDENTIFIER = X_ADDRESS + "/responsible-identifier/text()";
	private final static String X_TYPE_OF_ADDRESS = X_ADDRESS + "/type-of-address/@id";
	private final static String X_HIDE_ADDRESS = X_ADDRESS + "/hide-address/text()";
	private static final String X_PUBLICATION_CONDITION = X_ADDRESS + "/publication-condition/text()";
	private final static String X_ORGANISATION = X_ADDRESS + "/organisation/text()";
	private final static String X_NAME = X_ADDRESS + "/name/text()";
	private final static String X_NAME_FORM = X_ADDRESS + "/name-form/text()";
	private final static String X_NAME_FORM_KEY = X_ADDRESS + "/name-form/@id";
	private final static String X_TITLE_OR_FUNCTION = X_ADDRESS + "/title-or-function/text()";
	private final static String X_TITLE_OR_FUNCTION_KEY = X_ADDRESS + "/title-or-function/@id";
	private final static String X_GIVEN_NAME = X_ADDRESS + "/given-name/text()";
	private final static String X_COUNTRY = X_ADDRESS + "/country/text()";
	private final static String X_COUNTRY_KEY = X_ADDRESS + "/country/@id";
	private final static String X_POSTAL_CODE = X_ADDRESS + "/postal-code/text()";
	private final static String X_STREET = X_ADDRESS + "/street/text()";
	private final static String X_CITY = X_ADDRESS + "/city/text()";
	private final static String X_POST_BOX_POSTAL_CODE = X_ADDRESS + "/post-box-postal-code/text()";
	private final static String X_POST_BOX = X_ADDRESS + "/post-box/text()";
	private final static String X_COMMUNICATION_LIST = X_ADDRESS + "/communication";
	private final static String X_COMMUNICATION_MEDIUM = "communication-medium/text()";
	private final static String X_COMMUNICATION_MEDIUM_KEY = "communication-medium/@id";
	private final static String X_COMMUNICATION_VALUE = "communication-value/text()";
	private final static String X_COMMUNICATION_DESCRIPTION = "communication-description/text()";
	private final static String X_ORIGINAL_ADDRESS_IDENTIFIER = X_ADDRESS + "/original-address-identifier/text()";
	private final static String X_FUNCTION = X_ADDRESS + "/function/text()";
	private static final String X_SUBJECT_TERMS = X_ADDRESS + "/subject-terms";
	private static final String X_COMMENT_LIST = X_ADDRESS + "/comment";
	private static final String X_COMMENT_CONTENT = "comment-content/text()";
	private static final String X_COMMENT_CREATOR = "creator-identifier/text()";
	private static final String X_COMMENT_DATE_OF_CREATION = "date-of-creation/text()";
	private static final String X_DATE_OF_LAST_MODIFICATION = X_ADDRESS + "/date-of-last-modification/text()";
	private static final String X_DATE_OF_CREATION = X_ADDRESS + "/date-of-creation/text()";
	private static final String X_PARENT_ADDRESS = X_ADDRESS + "/parent-address/address-identifier/text()";

	public static IngridDocument map(Document source) {
		IngridDocument address = new IngridDocument();

		putString(MdekKeys.WORK_STATE, XPathUtils.getString(source, X_WORK_STATE), address);

		putString(MdekKeys.UUID, XPathUtils.getString(source, X_ADDRESS_IDENTIFIER), address);
		putString(new String[] {MdekKeys.MOD_USER, MdekKeys.UUID}, XPathUtils.getString(source, X_MODIFICATOR_IDENTIFIER), address);
		putString(new String[] {MdekKeys.RESPONSIBLE_USER, MdekKeys.UUID}, XPathUtils.getString(source, X_RESPONSIBLE_IDENTIFIER), address);
		putInt(MdekKeys.CLASS, XPathUtils.getInt(source, X_TYPE_OF_ADDRESS), address);
		putString(MdekKeys.HIDE_ADDRESS, XPathUtils.getString(source, X_HIDE_ADDRESS), address);
		putInt(MdekKeys.PUBLICATION_CONDITION, XPathUtils.getInt(source, X_PUBLICATION_CONDITION), address);
		putString(MdekKeys.ORGANISATION, XPathUtils.getString(source, X_ORGANISATION), address);
		putString(MdekKeys.NAME, XPathUtils.getString(source, X_NAME), address);
		putString(MdekKeys.NAME_FORM, XPathUtils.getString(source, X_NAME_FORM), address);
		putInt(MdekKeys.NAME_FORM_KEY, XPathUtils.getInt(source, X_NAME_FORM_KEY), address);
		putString(MdekKeys.TITLE_OR_FUNCTION, XPathUtils.getString(source, X_TITLE_OR_FUNCTION), address);
		putInt(MdekKeys.TITLE_OR_FUNCTION_KEY, XPathUtils.getInt(source, X_TITLE_OR_FUNCTION_KEY), address);
		putString(MdekKeys.GIVEN_NAME, XPathUtils.getString(source, X_GIVEN_NAME), address);
		putString(MdekKeys.COUNTRY_NAME, XPathUtils.getString(source, X_COUNTRY), address);
		putInt(MdekKeys.COUNTRY_CODE, XPathUtils.getInt(source, X_COUNTRY_KEY), address);
		putString(MdekKeys.POSTAL_CODE, XPathUtils.getString(source, X_POSTAL_CODE), address);
		putString(MdekKeys.STREET, XPathUtils.getString(source, X_STREET), address);
		putString(MdekKeys.CITY, XPathUtils.getString(source, X_CITY), address);
		putString(MdekKeys.POST_BOX_POSTAL_CODE, XPathUtils.getString(source, X_POST_BOX_POSTAL_CODE), address);
		putString(MdekKeys.POST_BOX, XPathUtils.getString(source, X_POST_BOX), address);
		mapCommunications(source, address);
		putString(MdekKeys.ORIGINAL_ADDRESS_IDENTIFIER, XPathUtils.getString(source, X_ORIGINAL_ADDRESS_IDENTIFIER), address);
		putString(MdekKeys.FUNCTION, XPathUtils.getString(source, X_FUNCTION), address);
		mapSubjectTerms(source, address);
		mapComments(source, address);
		putString(MdekKeys.DATE_OF_LAST_MODIFICATION, XPathUtils.getString(source, X_DATE_OF_LAST_MODIFICATION), address);
		putString(MdekKeys.DATE_OF_CREATION, XPathUtils.getString(source, X_DATE_OF_CREATION), address);
		putString(MdekKeys.PARENT_UUID, XPathUtils.getString(source, X_PARENT_ADDRESS), address);

		return address;
	}

	private static void mapCommunications(Document source, IngridDocument target) {
		NodeList communications = XPathUtils.getNodeList(source, X_COMMUNICATION_LIST);
		List<IngridDocument> communicationList = new ArrayList<IngridDocument>();

		for (int index = 0; index < communications.getLength(); ++index) {
			Node communication = communications.item(index);
			IngridDocument communicationDoc = new IngridDocument();
			putString(MdekKeys.COMMUNICATION_MEDIUM, XPathUtils.getString(communication, X_COMMUNICATION_MEDIUM), communicationDoc);
			putInt(MdekKeys.COMMUNICATION_MEDIUM_KEY, XPathUtils.getInt(communication, X_COMMUNICATION_MEDIUM_KEY), communicationDoc);
			putString(MdekKeys.COMMUNICATION_VALUE, XPathUtils.getString(communication, X_COMMUNICATION_VALUE), communicationDoc);
			putString(MdekKeys.COMMUNICATION_DESCRIPTION, XPathUtils.getString(communication, X_COMMUNICATION_DESCRIPTION), communicationDoc);

			communicationList.add(communicationDoc);
		}

		putDocList(MdekKeys.COMMUNICATION, communicationList, target);
	}

	private static void mapSubjectTerms(Document source, IngridDocument target) {
		Node subjectTermsNode = XPathUtils.getNode(source, X_SUBJECT_TERMS);
		if (subjectTermsNode != null) {
			mapSubjectTerms(subjectTermsNode.getChildNodes(), target);			
		}
	}

	private static void mapComments(Document source, IngridDocument target) {
		NodeList comments = XPathUtils.getNodeList(source, X_COMMENT_LIST);
		List<IngridDocument> commentList = new ArrayList<IngridDocument>();

		for (int index = 0; index < comments.getLength(); index++) {
			Node comment = comments.item(index);
			IngridDocument commentDoc = new IngridDocument();
			putString(MdekKeys.COMMENT, XPathUtils.getString(comment, X_COMMENT_CONTENT), commentDoc);
			putString(new String[] {MdekKeys.CREATE_USER, MdekKeys.UUID}, XPathUtils.getString(comment, X_COMMENT_CREATOR), commentDoc);
			putString(MdekKeys.CREATE_TIME, XPathUtils.getString(comment, X_COMMENT_DATE_OF_CREATION), commentDoc);

			commentList.add(commentDoc);
		}

		putDocList(MdekKeys.COMMENT_LIST, commentList, target);
	}
}
