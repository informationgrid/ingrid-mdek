package de.ingrid.mdek.xml.exporter.mapper;

import static de.ingrid.mdek.xml.XMLKeys.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.xml.util.XMLElement;
import de.ingrid.mdek.xml.util.XMLTreeCleaner;
import de.ingrid.utils.IngridDocument;

public class AdditionalFieldDocToXMLMapper extends AbstractDocToXMLMapper {

	public AdditionalFieldDocToXMLMapper(IngridDocument additionalField) {
		super(additionalField);
	}

	public XMLElement createAdditionalFieldDefinition() {
		XMLElement additionalFieldDef = new XMLElement(GENERAL_ADDITIONAL_FIELD_DEFINITION);
		additionalFieldDef.addChild(new XMLElement(FIELD_IDENTIFIER, getLongForKey(MdekKeys.SYS_ADDITIONAL_FIELD_IDENTIFIER)));
		additionalFieldDef.addChild(new XMLElement(FIELD_NAME, getStringForKey(MdekKeys.SYS_ADDITIONAL_FIELD_NAME)));
		additionalFieldDef.addChild(new XMLElement(FIELD_LENGTH, getIntegerForKey(MdekKeys.SYS_ADDITIONAL_FIELD_LENGTH)));
		additionalFieldDef.addChild(new XMLElement(FIELD_TYPE, getStringForKey(MdekKeys.SYS_ADDITIONAL_FIELD_TYPE)));
		additionalFieldDef.addChild(new XMLElement(FIELD_LIST_TYPE, getStringForKey(MdekKeys.SYS_ADDITIONAL_FIELD_LIST_TYPE)));
		additionalFieldDef.addChildren(createFieldListItems());

		XMLTreeCleaner.removeEmptyChildElements(additionalFieldDef);
		return additionalFieldDef;
	}

	private List<XMLElement> createFieldListItems() {
		List<XMLElement> listItems = new ArrayList<XMLElement>();

		for (Map.Entry<String, Object> entry : (Set<Map.Entry<String, Object>>) documentRoot.entrySet()) {
			String key = entry.getKey();
			if (key.startsWith(MdekKeys.SYS_ADDITIONAL_FIELD_LIST_ITEMS_KEY_PREFIX)) {
				String languageCode = extractLanguageCodeFromKey(key);
				XMLElement listItemRoot = new XMLElement(FIELD_LIST_ITEMS);
				listItemRoot.addAttribute(LANGUAGE_CODE, languageCode);
				listItemRoot.addChildren(createListItems((String[]) entry.getValue()));
				listItems.add(listItemRoot);
			}
		}
		return listItems;
	}

	private List<XMLElement> createListItems(String[] listItemList) {
		List<XMLElement> listItems = new ArrayList<XMLElement>();

		for (String listItem : listItemList) {
			XMLElement listItemElement = new XMLElement(FIELD_LIST_ITEM, listItem);
			listItems.add(listItemElement);
		}
		return listItems;
	}

	private static String extractLanguageCodeFromKey(String key) {
		int languageCodeBeginIndex = MdekKeys.SYS_ADDITIONAL_FIELD_LIST_ITEMS_KEY_PREFIX.length();
		int languageCodeEndIndex = key.length();
		return key.substring(languageCodeBeginIndex, languageCodeEndIndex);
	}
}
