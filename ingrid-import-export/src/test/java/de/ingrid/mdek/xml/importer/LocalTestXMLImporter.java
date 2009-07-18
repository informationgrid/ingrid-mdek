package de.ingrid.mdek.xml.importer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPOutputStream;

import org.junit.Test;

import de.ingrid.mdek.MdekUtils.IdcEntityType;
import de.ingrid.utils.IngridDocument;

public class LocalTestXMLImporter {

	@Test
	public void testXMLImporterFunction() throws IOException {
		XMLImporter xmlImporter = new XMLImporter(new IImporterCallback() {

			@Override
			public void writeAddress(IngridDocument addrDoc, String userUuid) {
				System.out.println("writeAddress('...', "+userUuid+")");
			}

			@Override
			public void writeObject(IngridDocument objDoc, String userUuid) {
				System.out.println("writeObject('...', "+userUuid+")");
			}
			
			@Override
			public void writeImportInfo(IdcEntityType whichType, int numImported,
					int totalNum, String userUuid) {
				System.out.println("Importing '"+whichType+"'. Count: "+numImported);
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
