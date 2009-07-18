package de.ingrid.mdek.xml.importer.mapper.version105;

import static de.ingrid.mdek.xml.util.IngridDocUtils.*;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.xml.util.XPathUtils;
import de.ingrid.utils.IngridDocument;

public class XMLAdditionalFieldsToDocMapper {

	private static final String X_FIELD_LIST_ITEM = "field-list-item";
	private static final String X_ATTRIBUTE_LANGUAGE_CODE = "@language-code";
	private static final String X_FIELD_LIST_ITEMS = "field-list-items";
	private static final String X_FIELD_NAME = "field-name/text()";
	private static final String X_FIELD_TYPE = "field-type/text()";
	private static final String X_FIELD_LENGTH = "field-length/text()";
	private static final String X_FIELD_IDENTIFIER = "field-identifier/text()";
	private static final String X_ADDITIONAL_FIELD_DEFINITION = "//data-model-extensions/general-additional-field-definition";

	public static IngridDocument map(Document source) {
		IngridDocument additionalFields = new IngridDocument();
		NodeList dataModelExtensions = XPathUtils.getNodeList(source, X_ADDITIONAL_FIELD_DEFINITION);

		for (int index = 0; index < dataModelExtensions.getLength(); ++index) {
			Node fieldDefinition = dataModelExtensions.item(index);
			Long fieldIdentifier = XPathUtils.getLong(fieldDefinition, X_FIELD_IDENTIFIER);
			Integer fieldLength = XPathUtils.getInt(fieldDefinition, X_FIELD_LENGTH);
			String fieldType = XPathUtils.getString(fieldDefinition, X_FIELD_TYPE);
			String fieldName = XPathUtils.getString(fieldDefinition, X_FIELD_NAME);

			IngridDocument fieldDefinitionContainer = new IngridDocument();
			putLong(MdekKeys.SYS_ADDITIONAL_FIELD_IDENTIFIER, fieldIdentifier, fieldDefinitionContainer);
			putInt(MdekKeys.SYS_ADDITIONAL_FIELD_LENGTH, fieldLength, fieldDefinitionContainer);
			putString(MdekKeys.SYS_ADDITIONAL_FIELD_TYPE, fieldType, fieldDefinitionContainer);
			putString(MdekKeys.SYS_ADDITIONAL_FIELD_NAME, fieldName, fieldDefinitionContainer);
			mapListItems(fieldDefinition, fieldDefinitionContainer);
			String fieldDefinitionKey = MdekKeys.SYS_ADDITIONAL_FIELD_KEY_PREFIX + fieldIdentifier;
			additionalFields.put(fieldDefinitionKey, fieldDefinitionContainer);
		}

		return additionalFields;
	}

	public static void mapListItems(Node fieldDefinitionContext, IngridDocument target) {
		NodeList listItemsRoots = XPathUtils.getNodeList(fieldDefinitionContext, X_FIELD_LIST_ITEMS);
		for (int index = 0; index < listItemsRoots.getLength(); ++index) {
			Node listItemsContext = listItemsRoots.item(index);
			String languageCode = XPathUtils.getString(listItemsContext, X_ATTRIBUTE_LANGUAGE_CODE);
			String listItemsKey = MdekKeys.SYS_ADDITIONAL_FIELD_LIST_ITEMS_KEY_PREFIX + languageCode;
			mapStringArray(listItemsContext, X_FIELD_LIST_ITEM, target, listItemsKey);
		}
	}

	private static void mapStringArray(Object source, String xPathExpression, IngridDocument target, String key) {
		NodeList nodeList = XPathUtils.getNodeList(source, xPathExpression);
		String[] targetList = new String[nodeList.getLength()];

		for (int index = 0; index < nodeList.getLength(); ++index) {
			Node item = nodeList.item(index);
			targetList[index] = item.getTextContent();
		}

		putStringArray(key, targetList, target);
	}
}
