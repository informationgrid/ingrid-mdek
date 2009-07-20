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
		Attribute attribute = getElementAttribute(xmlEventReader, new QName("http://www.portalu.de/igc-import", XMLKeys.IGC), new QName(XMLKeys.EXCHANGE_FORMAT));
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
