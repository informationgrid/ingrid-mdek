package de.ingrid.mdek.xml.importer.util;

import java.io.PrintStream;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class PrintToStreamContentHandler implements ContentHandler {

	private PrintStream out;

	public PrintToStreamContentHandler(PrintStream out) {
		this.out = out;
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		out.println("[ContentHandler] characters("+ "..." +", "+ start +", "+ length +")");
	}

	@Override
	public void endDocument() throws SAXException {
		out.println("[ContentHandler] endDocument()");
	}

	@Override
	public void endElement(String uri, String localName, String name)
			throws SAXException {
		out.println("[ContentHandler] endElement("+ uri +", "+ localName +", "+ name +")");
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {
		out.println("[ContentHandler] endPrefixMapping("+ prefix +")");
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
		out.println("[ContentHandler] ignorableWhitespace("+ "..." +", "+ start +", "+ length +")");
	}

	@Override
	public void processingInstruction(String target, String data)
			throws SAXException {
		out.println("[ContentHandler] processingInstruction("+ target +", "+ data +")");
	}

	@Override
	public void setDocumentLocator(Locator locator) {
		out.println("[ContentHandler] setDocumentLocator("+ locator +")");
	}

	@Override
	public void skippedEntity(String name) throws SAXException {
		out.println("[ContentHandler] skippedEntity("+ name +")");
	}

	@Override
	public void startDocument() throws SAXException {
		out.println("[ContentHandler] startDocument()");
	}

	@Override
	public void startElement(String uri, String localName, String name,
			Attributes atts) throws SAXException {
		out.println("[ContentHandler] startElement("+ uri +", "+ localName +", "+ name +", "+ "..." +")");
	}

	@Override
	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {
		out.println("[ContentHandler] startPrefixMapping("+ prefix +", "+ uri +")");
	}
	
}