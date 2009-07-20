package de.ingrid.mdek.xml.util;

import java.util.Iterator;


public class XMLTreeCleaner {
	public static void removeEmptyChildElements(XMLElement root) {
		if (root != null && root.hasChildren()) {
			for (Iterator<XMLElement> childrenIterator = root.getChildren().iterator(); childrenIterator.hasNext();) {
				XMLElement currentElement = childrenIterator.next();
				removeEmptyChildElements(currentElement);
				if (elementIsEmpty(currentElement)) {
					childrenIterator.remove();
				}
			}
		}
	}

	private static boolean elementIsEmpty(XMLElement element) {
		return !element.hasChildren() && !element.hasAttributes() && element.getText().length() == 0;
	}
}
