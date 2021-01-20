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
package de.ingrid.mdek.xml.exporter;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils.IdcEntityVersion;
import de.ingrid.mdek.caller.IMdekCaller.FetchQuantity;
import de.ingrid.mdek.caller.IMdekCallerAddress;
import de.ingrid.mdek.caller.IMdekCallerCatalog;
import de.ingrid.mdek.caller.IMdekCallerObject;
import de.ingrid.mdek.caller.IMdekCallerQuery;
import de.ingrid.mdek.caller.IMdekCallerSecurity;
import de.ingrid.mdek.caller.IMdekClientCaller;
import de.ingrid.mdek.caller.MdekCaller;
import de.ingrid.mdek.caller.MdekCallerAddress;
import de.ingrid.mdek.caller.MdekCallerCatalog;
import de.ingrid.mdek.caller.MdekCallerObject;
import de.ingrid.mdek.caller.MdekCallerQuery;
import de.ingrid.mdek.caller.MdekCallerSecurity;
import de.ingrid.mdek.caller.MdekClientCaller;
import de.ingrid.utils.IngridDocument;

public class TestMapper {
	private static IMdekClientCaller mdekClientCaller;
	private static IMdekCallerObject mdekCallerObject;
	private static IMdekCallerAddress mdekCallerAddress;
	private static IMdekCallerQuery mdekCallerQuery;
	private static IMdekCallerCatalog mdekCallerCatalog;
	private static IMdekCallerSecurity mdekCallerSecurity;

	private static IngridXMLStreamWriter writer; 
	private static OutputStream out;

	private static String plugId;
	private static String userId = "admin";

	private final static int EXPORT_NUM_ADDRESSES = 10;
	private final static int EXPORT_NUM_OBJECTS = 10;

	@BeforeClass
	public static void setup() {
		setupConnection();
		setupWriter();
	}

	@AfterClass
	public static void shutdown() {
		shutdownConnection();
		shutdownWriter();
	}

	private static void shutdownConnection() {
		MdekCaller.shutdown();
	}

	private static void setupConnection() {
		File communicationProperties = new File("src/test/resources/communication.properties");
		if (communicationProperties == null || !(communicationProperties instanceof File) || !communicationProperties.exists()) {
			throw new IllegalStateException(
					"Please specify the location of the communication.properties file via the Property 'mdekClientCaller.properties' in /src/resources/mdek.properties");
		}
		MdekClientCaller.initialize(communicationProperties);
		mdekClientCaller = MdekClientCaller.getInstance();

		MdekCallerObject.initialize(mdekClientCaller);
		MdekCallerAddress.initialize(mdekClientCaller);
		MdekCallerQuery.initialize(mdekClientCaller);
		MdekCallerCatalog.initialize(mdekClientCaller);
		MdekCallerSecurity.initialize(mdekClientCaller);

		mdekCallerObject = MdekCallerObject.getInstance();
		mdekCallerAddress = MdekCallerAddress.getInstance();
		mdekCallerQuery = MdekCallerQuery.getInstance();
		mdekCallerCatalog = MdekCallerCatalog.getInstance();
		mdekCallerSecurity = MdekCallerSecurity.getInstance();

		waitForConnection();
	}

	private static void waitForConnection() {
		while (mdekClientCaller.getRegisteredIPlugs().size() == 0) {
			try {
				Thread.sleep(1000);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		plugId = mdekClientCaller.getRegisteredIPlugs().get(0);
	}

	private static void setupWriter() {
		try {
			XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
			out = new BufferedOutputStream(new FileOutputStream("src/test/resources/test.xml", false));
			writer = new IngridXMLStreamWriter(outputFactory.createXMLStreamWriter(out, "UTF-8"));
			writer.writeStartDocument();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void shutdownWriter() {
		try {
			writer.writeEndDocument();
			writer.flush();
			writer.close();
			out.flush();
			out.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testWriteObjects() throws XMLStreamException {
		String hqlQuery = "from ObjectNode";
		int startHit = 0;
		int numHits = EXPORT_NUM_OBJECTS;
		IngridDocument objectNodesResponse = mdekCallerQuery.queryHQL(plugId, hqlQuery, startHit, numHits, userId);
		IngridDocument objectNodes = mdekClientCaller.getResultFromResponse(objectNodesResponse);

		List<Object> objEntities = objectNodes.getArrayList(MdekKeys.OBJ_ENTITIES);

		long startTime = System.currentTimeMillis();

		writer.writeStartIngridObjects();
		for (Object objEntity : objEntities) {
			String uuid = ((IngridDocument) objEntity).getString(MdekKeys.UUID);
			IngridDocument obj = getObject(uuid);

			if (obj != null) {
//				System.out.println("Mapping object with uuid '"+uuid+"': "+obj);
				List<IngridDocument> objInstances = new ArrayList<IngridDocument>(1);
				objInstances.add(obj);
				writer.writeIngridObject(objInstances);
			}
		}
		writer.writeEndIngridObjects();

		long endTime = System.currentTimeMillis();
		System.out.format("Object Mapping took %d milliseconds.\n", endTime - startTime);
	}

	@Test
	public void testWriteAddresses() throws XMLStreamException {
		String hqlQuery = "from AddressNode";
		int startHit = 0;
		int numHits = EXPORT_NUM_ADDRESSES;
		IngridDocument addressNodesResponse = mdekCallerQuery.queryHQL(plugId, hqlQuery, startHit, numHits, userId);
		IngridDocument addressNodes = mdekClientCaller.getResultFromResponse(addressNodesResponse);

		List<Object> adrEntities = addressNodes.getArrayList(MdekKeys.ADR_ENTITIES);

		long startTime = System.currentTimeMillis();

		writer.writeStartIngridAddresses();
		for (Object adrEntity : adrEntities) {
			String uuid = ((IngridDocument) adrEntity).getString(MdekKeys.UUID);
			IngridDocument adr = getAddress(uuid);

			if (adr != null) {
//				System.out.println("Mapping address with uuid '"+uuid+"': "+adr);
				List<IngridDocument> addrInstances = new ArrayList<IngridDocument>(1);
				addrInstances.add(adr);
				writer.writeIngridAddress(addrInstances);
			}
		}
		writer.writeEndIngridAddresses();

		long endTime = System.currentTimeMillis();
		System.out.format("Address Mapping took %d milliseconds.\n", endTime - startTime);
	}

	private IngridDocument getObject(String uuid) {
		IngridDocument objDocResponse = mdekCallerObject.fetchObject(plugId, uuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION, userId);
		return mdekClientCaller.getResultFromResponse(objDocResponse);
	}
	private IngridDocument getAddress(String uuid) {
		IngridDocument adrDocResponse = mdekCallerAddress.fetchAddress(plugId, uuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION, 0, 0, userId);
		return mdekClientCaller.getResultFromResponse(adrDocResponse);
	}
}
