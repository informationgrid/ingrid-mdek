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
