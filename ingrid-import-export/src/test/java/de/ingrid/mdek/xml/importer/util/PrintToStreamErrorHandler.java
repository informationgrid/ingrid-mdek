package de.ingrid.mdek.xml.importer.util;

import java.io.PrintStream;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


public class PrintToStreamErrorHandler implements ErrorHandler {

	private PrintStream out;
	
	public PrintToStreamErrorHandler(PrintStream out) {
		this.out = out;
	}

	@Override
	public void error(SAXParseException exception) throws SAXException {
		out.println("Error at line "+exception.getLineNumber()+": "+ exception.getMessage());
	}

	@Override
	public void fatalError(SAXParseException exception) throws SAXException {
		out.println("Fatal Error at line "+exception.getLineNumber()+": "+ exception.getMessage());
	}

	@Override
	public void warning(SAXParseException exception) throws SAXException {
		out.println("Warning at line "+exception.getLineNumber()+": "+ exception.getMessage());
	}
}