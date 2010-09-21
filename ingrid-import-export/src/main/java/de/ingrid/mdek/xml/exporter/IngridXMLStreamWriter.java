package de.ingrid.mdek.xml.exporter;

import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import de.ingrid.mdek.xml.Versioning;
import de.ingrid.mdek.xml.XMLKeys;
import de.ingrid.mdek.xml.util.IngridXMLBuilder;
import de.ingrid.mdek.xml.util.XMLElement;
import de.ingrid.utils.IngridDocument;

public class IngridXMLStreamWriter {

	public final static String XML_ENCODING = "UTF-8";
	public final static String XML_VERSION = "1.0";
	public final static String XMLNS = "http://www.portalu.de/igc-import";
	public final static String EXCHANGE_FORMAT_VERSION = Versioning.CURRENT_VERSION;

	private final XMLStreamWriter xmlStreamWriter;
	private StringBuffer indentation;

	public IngridXMLStreamWriter(XMLStreamWriter xmlStreamWriter) {
		this.xmlStreamWriter = xmlStreamWriter;
		this.indentation = new StringBuffer();
	}

	public void writeStartIngridObjects() throws XMLStreamException {
		writeStartElementWithIndentation(XMLKeys.DATA_SOURCES);
	}

	public void writeIngridObject(IngridDocument document) throws XMLStreamException {
		writeElement(IngridXMLBuilder.createXMLForObject(document));
	}

	public void writeEndIngridObjects() throws XMLStreamException {
		writeEndElementWithIndentation();
	}

	public void writeStartIngridAddresses() throws XMLStreamException {
		writeStartElementWithIndentation(XMLKeys.ADDRESSES);
	}

	public void writeIngridAddress(IngridDocument document) throws XMLStreamException {
		writeElement(IngridXMLBuilder.createXMLForAddress(document));
	}

	public void writeEndIngridAddresses() throws XMLStreamException {
		writeEndElementWithIndentation();
	}

	public void writeStartAdditionalFields() throws XMLStreamException {
		writeStartElementWithIndentation(XMLKeys.DATA_MODEL_EXTENSIONS);
	}

	public void writeAdditionalField(IngridDocument additionalField) throws XMLStreamException {
		writeElement(IngridXMLBuilder.createXMLForAdditionalField(additionalField));
	}

	public void writeEndAdditionalFields() throws XMLStreamException {
		writeEndElementWithIndentation();
	}

	private void writeStartElementWithIndentation(String localName) throws XMLStreamException {
		writeCharacters(indentation.toString());
		writeStartElement(localName);
		writeCharacters("\n");
		indentation.append("\t");
	}

	private void writeEndElementWithIndentation() throws XMLStreamException {
		indentation.setLength(indentation.length() - 1);
		writeCharacters(indentation.toString());
		writeEndElement();
		writeCharacters("\n");
	}

	public void writeStartDocument() throws XMLStreamException {
		xmlStreamWriter.writeStartDocument(XML_ENCODING, XML_VERSION);
		writeCharacters("\n");
		writeStartElement(XMLKeys.IGC);
		writeAttribute("xmlns", XMLNS);
		writeAttribute(XMLKeys.EXCHANGE_FORMAT, EXCHANGE_FORMAT_VERSION);
		indentation.append("\t");
		writeCharacters("\n");
	}

	private void writeStartElement(String localName) throws XMLStreamException {
		xmlStreamWriter.writeStartElement(localName);
	}

	private void writeCharacters(String text) throws XMLStreamException {
		xmlStreamWriter.writeCharacters(text);
	}

	private void writeElement(XMLElement xmlElement) throws XMLStreamException {
		writeCharacters(indentation.toString());

		if (isEmptyElement(xmlElement)) {
			writeEmptyElement(xmlElement);

		} else {
			writeStartElement(xmlElement.getName());
	
			for (Map.Entry<String, String> entry : xmlElement.getAttributes()) {
				writeAttribute(entry.getKey(), entry.getValue());
			}
	
			writeCharacters(xmlElement.getText());
	
			if (xmlElement.hasChildren()) {
				writeCharacters("\n");
				indentation.append('\t');
				for (XMLElement childElement : xmlElement.getChildren()) {
					writeElement(childElement);
				}
				indentation.setLength(indentation.length() - 1);
				writeCharacters(indentation.toString());
				writeEndElement();
				writeCharacters("\n");
	
			} else {
				writeEndElement();
				writeCharacters("\n");
			}
		}
	}

	private void writeAttribute(String localName, String value) throws XMLStreamException {
		xmlStreamWriter.writeAttribute(localName, value);
	}

	private void writeEmptyElement(XMLElement xmlElement) throws XMLStreamException {
		writeEmptyElement(xmlElement.getName());
		for (Map.Entry<String, String> entry : xmlElement.getAttributes()) {
			writeAttribute(entry.getKey(), entry.getValue());
		}
		writeCharacters("\n");
	}

	private void writeEmptyElement(String localName) throws XMLStreamException {
		xmlStreamWriter.writeEmptyElement(localName);
	}

	private void writeEndElement() throws XMLStreamException {
		xmlStreamWriter.writeEndElement();
	}

	public void writeEndDocument() throws XMLStreamException {
		indentation.setLength(indentation.length() - 1);
		writeEndElement();
		xmlStreamWriter.writeEndDocument();
	}

	public void flush() throws XMLStreamException {
		xmlStreamWriter.flush();
	}

	public void close() throws XMLStreamException {
		xmlStreamWriter.close();
	}
	
	private static boolean isEmptyElement(XMLElement xmlElement) {
		return !xmlElement.hasChildren() && !xmlElement.hasTextContent();
	}
}
