/*
 * **************************************************-
 * ingrid-import-export
 * ==================================================
 * Copyright (C) 2014 - 2022 wemove digital solutions GmbH
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

import de.ingrid.mdek.MdekUtils.IdcEntityVersion;
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
		System.out.println("Export PUBLISHED_VERSION ");
		byte[] zippedResult = xmlExporter.exportObjects(adrUuids, IdcEntityVersion.PUBLISHED_VERSION, true, "admin");
		System.out.println("Size of zipped result: "+((zippedResult.length) / 1024)+"KB");
		String result = decompressZippedByteArray(zippedResult);
		System.out.println(result);

		System.out.println("Export ALL_VERSIONS ");
		zippedResult = xmlExporter.exportObjects(adrUuids, IdcEntityVersion.ALL_VERSIONS, true, "admin");
		System.out.println("Size of zipped result: "+((zippedResult.length) / 1024)+"KB");
		result = decompressZippedByteArray(zippedResult);
		System.out.println(result);

		System.out.println("Export WORKING_VERSION ");
		zippedResult = xmlExporter.exportObjects(adrUuids, IdcEntityVersion.WORKING_VERSION, true, "admin");
		System.out.println("Size of zipped result: "+((zippedResult.length) / 1024)+"KB");
		result = decompressZippedByteArray(zippedResult);
		System.out.println(result);
	}

	@Test
	public void testXMLAddressExporterWorksWithIllegalUuid() throws IOException {
		List<String> adrUuids = new ArrayList<String>();
		adrUuids.add("12345");
		System.out.println("Export PUBLISHED_VERSION ");
		byte[] zippedResult = xmlExporter.exportObjects(adrUuids, IdcEntityVersion.PUBLISHED_VERSION, true, "admin");
		String result = decompressZippedByteArray(zippedResult);
		System.out.println(result);

		System.out.println("Export ALL_VERSIONS ");
		zippedResult = xmlExporter.exportObjects(adrUuids, IdcEntityVersion.ALL_VERSIONS, true, "admin");
		result = decompressZippedByteArray(zippedResult);
		System.out.println(result);

		System.out.println("Export WORKING_VERSION ");
		zippedResult = xmlExporter.exportObjects(adrUuids, IdcEntityVersion.WORKING_VERSION, true, "admin");
		result = decompressZippedByteArray(zippedResult);
		System.out.println(result);
	}

	@Test
	public void testXMLObjectExporter() throws IOException {
		List<String> objUuids = new ArrayList<String>();
		objUuids.add("15C69C20-FE15-11D2-AF34-0060084A4596");
		//objUuids.add("4EFBE5F6-C049-11D4-87DF-0060084A4596");
		// Results in an error: subobjects(81171714-018E-11D5-87AF-00600852CACF)
		System.out.println("Export PUBLISHED_VERSION ");
		byte[] zippedResult = xmlExporter.exportObjects(objUuids, IdcEntityVersion.PUBLISHED_VERSION, true, "admin");
		System.out.println("Size of zipped result: "+((zippedResult.length) / 1024)+"KB");
		String result = decompressZippedByteArray(zippedResult);
		System.out.println(result);

		System.out.println("Export ALL_VERSIONS ");
		zippedResult = xmlExporter.exportObjects(objUuids, IdcEntityVersion.ALL_VERSIONS, true, "admin");
		System.out.println("Size of zipped result: "+((zippedResult.length) / 1024)+"KB");
		result = decompressZippedByteArray(zippedResult);
		System.out.println(result);

		System.out.println("Export WORKING_VERSION ");
		zippedResult = xmlExporter.exportObjects(objUuids, IdcEntityVersion.WORKING_VERSION, true, "admin");
		System.out.println("Size of zipped result: "+((zippedResult.length) / 1024)+"KB");
		result = decompressZippedByteArray(zippedResult);
		System.out.println(result);
	}

	@Test
	public void testXMLObjectExporterWorksWithIllegalUuid() throws IOException {
		List<String> objUuids = new ArrayList<String>();
		objUuids.add("12345");
		System.out.println("Export PUBLISHED_VERSION ");
		byte[] zippedResult = xmlExporter.exportObjects(objUuids, IdcEntityVersion.PUBLISHED_VERSION, true, "admin");
		String result = decompressZippedByteArray(zippedResult);
		System.out.println(result);

		System.out.println("Export ALL_VERSIONS ");
		zippedResult = xmlExporter.exportObjects(objUuids, IdcEntityVersion.ALL_VERSIONS, true, "admin");
		result = decompressZippedByteArray(zippedResult);
		System.out.println(result);

		System.out.println("Export WORKING_VERSION ");
		zippedResult = xmlExporter.exportObjects(objUuids, IdcEntityVersion.WORKING_VERSION, true, "admin");
		result = decompressZippedByteArray(zippedResult);
		System.out.println(result);
	}

	@Test
	public void testXMLObjectsExporter() throws IOException {
		List<String> objUuids = new ArrayList<String>();
		objUuids.add("15C69C01-FE15-11D2-AF34-0060084A4596");
		objUuids.add("12345");
		objUuids.add("0F5D7569-1F40-11D3-AF4F-0060084A4596");
		System.out.println("Export PUBLISHED_VERSION ");
		byte[] zippedResult = xmlExporter.exportObjects(objUuids, IdcEntityVersion.PUBLISHED_VERSION, false, "admin");
		String result = decompressZippedByteArray(zippedResult);
		System.out.println(result);

		System.out.println("Export ALL_VERSIONS ");
		zippedResult = xmlExporter.exportObjects(objUuids, IdcEntityVersion.ALL_VERSIONS, true, "admin");
		result = decompressZippedByteArray(zippedResult);
		System.out.println(result);

		System.out.println("Export WORKING_VERSION ");
		zippedResult = xmlExporter.exportObjects(objUuids, IdcEntityVersion.WORKING_VERSION, true, "admin");
		result = decompressZippedByteArray(zippedResult);
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
