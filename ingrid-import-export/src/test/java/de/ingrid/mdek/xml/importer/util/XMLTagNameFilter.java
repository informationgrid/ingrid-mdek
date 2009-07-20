package de.ingrid.mdek.xml.importer.util;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

public class XMLTagNameFilter extends XMLFilterImpl {

	private String tagName;
	private boolean filter;

	public XMLTagNameFilter(XMLReader parent, String tagName) {
		super(parent);
		this.tagName = tagName;
		this.filter = true;
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if (!filter) {
			super.characters(ch, start, length);
		}
	}

	@Override
	public void endElement(String uri, String localName, String name)
			throws SAXException {
		if (!filter) {
			super.endElement(uri, localName, name);
			if (localName.equals(tagName)) {
				filter = true;
			}
		}
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
		if (!filter) {
			super.ignorableWhitespace(ch, start, length);
		}
	}

	@Override
	public void startElement(String uri, String localName, String name,
			Attributes atts) throws SAXException {
		if (localName.equals(tagName)) {
			filter = false;
		}

		if (!filter) {
			super.startElement(uri, localName, name, atts);
		}
	}
}