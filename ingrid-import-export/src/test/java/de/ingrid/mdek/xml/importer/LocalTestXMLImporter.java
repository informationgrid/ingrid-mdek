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
package de.ingrid.mdek.xml.importer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.junit.Test;

import de.ingrid.mdek.MdekUtils.IdcEntityType;
import de.ingrid.utils.IngridDocument;

public class LocalTestXMLImporter {

	@Test
	public void testXMLImporterFunction() throws IOException {
		XMLImporter xmlImporter = new XMLImporter(new IImporterCallback() {

			@Override
			public void writeAddress(List<IngridDocument> addrDocs, String userUuid) {
				System.out.println("writeAddress('...', "+userUuid+")");
			}

			@Override
			public void writeObject(List<IngridDocument> objDocs, String userUuid) {
				System.out.println("writeObject('...', "+userUuid+")");
			}
			
			@Override
			public void writeImportInfo(IdcEntityType whichType, int numImported, int totalNum, String userUuid) {
				System.out.println("Importing '"+whichType+"'. Count: "+numImported);
			}
			@Override
			public void writeImportInfoMessage(String message, String userUuid) {
				System.out.println("writeImportInfoMessage '"+message+"'");
			}});

		FileInputStream in = new FileInputStream("src/test/resources/test.xml");
		xmlImporter.importEntities(compress(in), "admin");
	}

	// Compress (zip) any data on InputStream and write it to a ByteArrayOutputStream
	public static byte[] compress(InputStream is) throws IOException {
		BufferedInputStream bin = new BufferedInputStream(is);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		GZIPOutputStream gzout = new GZIPOutputStream(new BufferedOutputStream(out));

		final int BUFFER = 2048;
		int count;
		byte data[] = new byte[BUFFER];
		while((count = bin.read(data, 0, BUFFER)) != -1) {
		   gzout.write(data, 0, count);
		}

		gzout.close();
		return out.toByteArray();
	}
}
