/*
 * **************************************************-
 * ingrid-import-export
 * ==================================================
 * Copyright (C) 2014 - 2016 wemove digital solutions GmbH
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
package de.ingrid.mdek.xml.importer.mapper.version105;

import static de.ingrid.mdek.xml.util.IngridDocUtils.*;

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

	private final static String X_ADDRESS_IDENTIFIER = "//address/address-identifier/text()";
	private final static String X_MODIFICATOR_IDENTIFIER = "//address/modificator-identifier/text()";
	private final static String X_RESPONSIBLE_IDENTIFIER = "//address/responsible-identifier/text()";
	private final static String X_TYPE_OF_ADDRESS = "//address/type-of-address/@id";
	private final static String X_ORGANISATION = "//address/organisation/text()";
	private final static String X_NAME = "//address/name/text()";
	private final static String X_NAME_FORM = "//address/name-form/text()";
	private final static String X_NAME_FORM_KEY = "//address/name-form/@id";
	private final static String X_TITLE_OR_FUNCTION = "//address/title-or-function/text()";
	private final static String X_TITLE_OR_FUNCTION_KEY = "//address/title-or-function/@id";
	private final static String X_GIVEN_NAME = "//address/given-name/text()";
	private final static String X_COUNTRY = "//address/country/text()";
	private final static String X_COUNTRY_KEY = "//address/country/@id";
	private final static String X_POSTAL_CODE = "//address/postal-code/text()";
	private final static String X_STREET = "//address/street/text()";
	private final static String X_CITY = "//address/city/text()";
	private final static String X_POST_BOX_POSTAL_CODE = "//address/post-box-postal-code/text()";
	private final static String X_POST_BOX = "//address/post-box/text()";
	private final static String X_COMMUNICATION_LIST = "//address/communication";
	private final static String X_COMMUNICATION_MEDIUM = "communication-medium/text()";
	private final static String X_COMMUNICATION_MEDIUM_KEY = "communication-medium/@id";
	private final static String X_COMMUNICATION_VALUE = "communication-value/text()";
	private final static String X_COMMUNICATION_DESCRIPTION = "communication-description/text()";
	private final static String X_ORIGINAL_ADDRESS_IDENTIFIER = "//address/original-address-identifier/text()";
	private final static String X_FUNCTION = "//address/function/text()";
	private final static String X_ADDRESS_DESCRIPTION = "//address/address-description/text()";
	private static final String X_SUBJECT_TERMS = "//address/subject-terms";
	private static final String X_COMMENT_LIST = "//address/comment";
	private static final String X_COMMENT_CONTENT = "comment-content/text()";
	private static final String X_COMMENT_CREATOR = "creator-identifier/text()";
	private static final String X_COMMENT_DATE_OF_CREATION = "date-of-creation/text()";
	private static final String X_DATE_OF_LAST_MODIFICATION = "//address/date-of-last-modification/text()";
	private static final String X_DATE_OF_CREATION = "//address/date-of-creation/text()";
	private static final String X_PARENT_ADDRESS = "//address/parent-address/address-identifier/text()";

	public static IngridDocument map(Document source) {
		IngridDocument address = new IngridDocument();

		putString(MdekKeys.UUID, XPathUtils.getString(source, X_ADDRESS_IDENTIFIER), address);
		putString(new String[] {MdekKeys.MOD_USER, MdekKeys.UUID}, XPathUtils.getString(source, X_MODIFICATOR_IDENTIFIER), address);
		putString(new String[] {MdekKeys.RESPONSIBLE_USER, MdekKeys.UUID}, XPathUtils.getString(source, X_RESPONSIBLE_IDENTIFIER), address);
		putInt(MdekKeys.CLASS, XPathUtils.getInt(source, X_TYPE_OF_ADDRESS), address);
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
		// TODO: ADDRESS_DESCRIPTION removed with version 330 
//		putString(MdekKeys.ADDRESS_DESCRIPTION, XPathUtils.getString(source, X_ADDRESS_DESCRIPTION), address);
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
