/*
 * **************************************************-
 * ingrid-import-export
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
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
