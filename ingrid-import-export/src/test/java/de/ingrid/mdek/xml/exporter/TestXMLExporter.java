package de.ingrid.mdek.xml.exporter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import de.ingrid.mdek.xml.exporter.util.RemoteExporterCallback;

public class TestXMLExporter {

	private static XMLExporter xmlExporter;

	@BeforeClass
	public static void setupExporter() {
		xmlExporter = new XMLExporter(new RemoteExporterCallback());		
	}

	@Test
	public void testXMLAddressExporter() throws IOException {
		List<String> adrUuids = new ArrayList<String>();
		adrUuids.add("15C69BD6-FE15-11D2-AF34-0060084A4596");
		byte[] zippedResult = xmlExporter.exportAddresses(adrUuids, true, "admin");

		String result = decompressZippedByteArray(zippedResult);
		System.out.println(result);
	}

	@Test
	public void testXMLAddressExporterWorksWithIllegalUuid() throws IOException {
		List<String> adrUuids = new ArrayList<String>();
		adrUuids.add("12345");
		byte[] zippedResult = xmlExporter.exportAddresses(adrUuids, true, "admin");

		String result = decompressZippedByteArray(zippedResult);
		System.out.println(result);
	}

	@Test
	public void testXMLObjectExporter() throws IOException {
		List<String> objUuids = new ArrayList<String>();
		objUuids.add("15C69C20-FE15-11D2-AF34-0060084A4596");
		//objUuids.add("4EFBE5F6-C049-11D4-87DF-0060084A4596");
		// Results in an error: subobjects(81171714-018E-11D5-87AF-00600852CACF)
		byte[] zippedResult = xmlExporter.exportObjects(objUuids, true, "admin");

		System.out.println("Size of zipped result: "+((zippedResult.length) / 1024)+"KB");

		String result = decompressZippedByteArray(zippedResult);
		System.out.println(result);
	}

	@Test
	public void testXMLObjectExporterWorksWithIllegalUuid() throws IOException {
		List<String> objUuids = new ArrayList<String>();
		objUuids.add("12345");
		byte[] zippedResult = xmlExporter.exportObjects(objUuids, true, "admin");

		String result = decompressZippedByteArray(zippedResult);
		System.out.println(result);
	}

	@Test
	public void testXMLObjectsExporter() throws IOException {
		List<String> objUuids = new ArrayList<String>();
		objUuids.add("15C69C01-FE15-11D2-AF34-0060084A4596");
		objUuids.add("12345");
		objUuids.add("0F5D7569-1F40-11D3-AF4F-0060084A4596");
		byte[] zippedResult = xmlExporter.exportObjects(objUuids, false, "admin");

		String result = decompressZippedByteArray(zippedResult);
		System.out.println(result);
	}

	private static String decompressZippedByteArray(byte[] zippedData) throws IOException {
		ByteArrayOutputStream baos = decompress(new ByteArrayInputStream(zippedData));
		return baos.toString("UTF-8");
	}

	// Decompress (unzip) data on InputStream (has to contain zipped data) and write it to a ByteArrayOutputStream
	public static ByteArrayOutputStream decompress(InputStream is) throws IOException {
		GZIPInputStream gzin = new GZIPInputStream(new BufferedInputStream(is));
		ByteArrayOutputStream baout = new ByteArrayOutputStream();
		BufferedOutputStream out = new BufferedOutputStream(baout);

		final int BUFFER = 2048;
		int count;
		byte data[] = new byte[BUFFER];
		while((count = gzin.read(data, 0, BUFFER)) != -1) {
		   out.write(data, 0, count);
		}

		out.close();
		return baout;
	}
}
