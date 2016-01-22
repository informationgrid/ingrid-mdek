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
