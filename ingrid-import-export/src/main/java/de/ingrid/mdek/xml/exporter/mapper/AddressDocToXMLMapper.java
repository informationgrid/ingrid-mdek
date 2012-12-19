package de.ingrid.mdek.xml.exporter.mapper;

import static de.ingrid.mdek.xml.XMLKeys.*;

import java.util.ArrayList;
import java.util.List;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.xml.util.XMLElement;
import de.ingrid.mdek.xml.util.XMLTreeCleaner;
import de.ingrid.utils.IngridDocument;

public class AddressDocToXMLMapper extends AbstractDocToXMLMapper {

	public AddressDocToXMLMapper(IngridDocument address) {
		super(address);
	}

	public static XMLElement createAddress() {
		XMLElement address = new XMLElement(ADDRESS);
		return address;
	}

	public XMLElement createAddressInstance() {
		XMLElement address = new XMLElement(ADDRESS_INSTANCE);
		address.addAttribute(WORK_STATE, getStringForKey(MdekKeys.WORK_STATE));
		address.addChild(new XMLElement(ADDRESS_IDENTIFIER, getStringForKey(MdekKeys.UUID)));
		address.addChild(createModificatorIdentifier());
		address.addChild(createResponsibleIdentifier());
		address.addChild(createTypeOfAddress());
		address.addChild(new XMLElement(HIDE_ADDRESS, getStringForKey(MdekKeys.HIDE_ADDRESS)));
		address.addChild(new XMLElement(PUBLICATION_CONDITION, getIntegerForKey(MdekKeys.PUBLICATION_CONDITION)));
		address.addChild(new XMLElement(ORGANISATION, getStringForKey(MdekKeys.ORGANISATION)));
		address.addChild(new XMLElement(NAME, getStringForKey(MdekKeys.NAME)));
		address.addChild(createNameForm());
		address.addChild(createTitleOrFunction());
		address.addChild(new XMLElement(GIVEN_NAME, getStringForKey(MdekKeys.GIVEN_NAME)));
		address.addChild(createCountry());
		address.addChild(new XMLElement(POSTAL_CODE, getStringForKey(MdekKeys.POSTAL_CODE)));
		address.addChild(new XMLElement(STREET, getStringForKey(MdekKeys.STREET)));
		address.addChild(new XMLElement(CITY, getStringForKey(MdekKeys.CITY)));
		address.addChild(new XMLElement(POST_BOX_POSTAL_CODE, getStringForKey(MdekKeys.POST_BOX_POSTAL_CODE)));
		address.addChild(new XMLElement(POST_BOX, getStringForKey(MdekKeys.POST_BOX)));
		address.addChildren(createCommunications());
		address.addChild(new XMLElement(ORIGINAL_ADDRESS_IDENTIFIER, getStringForKey(MdekKeys.ORIGINAL_ADDRESS_IDENTIFIER)));
		address.addChild(new XMLElement(FUNCTION, getStringForKey(MdekKeys.FUNCTION)));
		address.addChild(createSubjectTerms());
		address.addChildren(createComments());
		address.addChild(new XMLElement(DATE_OF_LAST_MODIFICATION, getStringForKey(MdekKeys.DATE_OF_LAST_MODIFICATION)));
		address.addChild(new XMLElement(DATE_OF_CREATION, getStringForKey(MdekKeys.DATE_OF_CREATION)));
		address.addChild(createParentAddress());

		XMLTreeCleaner.removeEmptyChildElements(address);
		
		return address;
	}

	private XMLElement createModificatorIdentifier() {
		IngridDocument modUser = getIngridDocumentForKey(MdekKeys.MOD_USER);
		String uuid = getStringForKey(MdekKeys.UUID, modUser);
		return new XMLElement(MODIFICATOR_IDENTIFIER, uuid);
	}

	private XMLElement createResponsibleIdentifier() {
		IngridDocument responsibleUser = getIngridDocumentForKey(MdekKeys.RESPONSIBLE_USER);
		String uuid = getStringForKey(MdekKeys.UUID, responsibleUser);
		return new XMLElement(RESPONSIBLE_IDENTIFIER, uuid);
	}

	private XMLElement createTypeOfAddress() {
		XMLElement typeOfAddress = new XMLElement(TYPE_OF_ADDRESS);
		typeOfAddress.addAttribute(ID, getIntegerForKey(MdekKeys.CLASS));
		return typeOfAddress;
	}

	private XMLElement createNameForm() {
		XMLElement nameForm = new XMLElement(NAME_FORM, getStringForKey(MdekKeys.NAME_FORM));
		nameForm.addAttribute(ID, getIntegerForKey(MdekKeys.NAME_FORM_KEY));
		return nameForm;
	}

	private XMLElement createTitleOrFunction() {
		XMLElement titleOrFunction = new XMLElement(TITLE_OR_FUNCTION, getStringForKey(MdekKeys.TITLE_OR_FUNCTION));
		titleOrFunction.addAttribute(ID, getIntegerForKey(MdekKeys.TITLE_OR_FUNCTION_KEY));
		return titleOrFunction;
	}

	private XMLElement createCountry() {
		XMLElement country = new XMLElement(COUNTRY, getStringForKey(MdekKeys.COUNTRY_NAME));
		country.addAttribute(ID, getIntegerForKey(MdekKeys.COUNTRY_CODE));
		return country;
	}

	private List<XMLElement> createCommunications() {
		List<XMLElement> communications = new ArrayList<XMLElement>();
		List<IngridDocument> communicationList = getIngridDocumentListForKey(MdekKeys.COMMUNICATION);
		for (IngridDocument communication : communicationList) {
			communications.add(createCommunication(communication));
		}
		return communications;
	}

	private XMLElement createCommunication(IngridDocument communicationContext) {
		XMLElement communication = new XMLElement(COMMUNICATION);
		communication.addChild(createCommunicationMedium(communicationContext));
		communication.addChild(new XMLElement(COMMUNICATION_VALUE, getStringForKey(MdekKeys.COMMUNICATION_VALUE, communicationContext)));
		communication.addChild(new XMLElement(COMMUNICATION_DESCRIPTION, getStringForKey(MdekKeys.COMMUNICATION_DESCRIPTION, communicationContext)));
		return communication;
	}

	private XMLElement createCommunicationMedium(IngridDocument communicationContext) {
		XMLElement communicationMedium = new XMLElement(COMMUNICATION_MEDIUM, getStringForKey(MdekKeys.COMMUNICATION_MEDIUM, communicationContext));
		communicationMedium.addAttribute(ID, getIntegerForKey(MdekKeys.COMMUNICATION_MEDIUM_KEY, communicationContext));
		return communicationMedium;
	}

	private List<XMLElement> createComments() {
		List<XMLElement> comments = new ArrayList<XMLElement>();
		List<IngridDocument> commentList = getIngridDocumentListForKey(MdekKeys.COMMENT_LIST);
		for (IngridDocument comment : commentList) {
			comments.add(createComment(comment));
		}
		return comments;
	}

	private XMLElement createComment(IngridDocument commentContext) {
		XMLElement comment = new XMLElement(COMMENT);
		comment.addChild(new XMLElement(COMMENT_CONTENT, getStringForKey(MdekKeys.COMMENT)));
		comment.addChild(createCreatorIdentifier(commentContext));
		comment.addChild(new XMLElement(DATE_OF_CREATION, getStringForKey(MdekKeys.CREATE_TIME)));
		return comment;
	}

	private XMLElement createCreatorIdentifier(IngridDocument commentContext) {
		IngridDocument createUser = getIngridDocumentForKey(MdekKeys.CREATE_USER, commentContext);
		return new XMLElement(CREATOR_IDENTIFIER, getStringForKey(MdekKeys.UUID, createUser));
	}

	private XMLElement createParentAddress() {
		XMLElement parentAddress = new XMLElement(PARENT_ADDRESS);
		parentAddress.addChild(new XMLElement(ADDRESS_IDENTIFIER, getStringForKey(MdekKeys.PARENT_UUID)));
		return parentAddress;
	}
}
