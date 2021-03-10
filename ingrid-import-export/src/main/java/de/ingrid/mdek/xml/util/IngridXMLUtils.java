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
package de.ingrid.mdek.xml.util;

import java.io.Reader;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import de.ingrid.mdek.xml.XMLKeys;

public class IngridXMLUtils {

	private IngridXMLUtils() {}

	public static String getVersion(Reader reader) throws XMLStreamException {
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		XMLEventReader xmlEventReader = inputFactory.createXMLEventReader(reader);
		Attribute attribute = getElementAttribute(xmlEventReader, new QName("http://informationgrid.eu/igc-import", XMLKeys.IGC), new QName(XMLKeys.EXCHANGE_FORMAT));
		xmlEventReader.close();
		if (attribute != null) {
			return attribute.getValue();
		}

		return null;
	}

	private static Attribute getElementAttribute(XMLEventReader reader, QName element, QName attribute) throws XMLStreamException {
		while (reader.hasNext()) {
			XMLEvent event = reader.nextEvent();
			if (event.isStartElement()) {
				StartElement startElement = event.asStartElement();
				if (startElement.getName().equals(element)) {
					return startElement.getAttributeByName(attribute);
				}
			}
		}

		return null;
	}
}
