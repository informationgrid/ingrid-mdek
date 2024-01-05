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
package de.ingrid.mdek.xml.importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekKeysSecurity;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekUtils.IdcEntityVersion;
import de.ingrid.mdek.caller.IMdekCaller.FetchQuantity;
import de.ingrid.mdek.caller.IMdekCallerAddress;
import de.ingrid.mdek.caller.IMdekCallerObject;
import de.ingrid.mdek.caller.IMdekClientCaller;
import de.ingrid.mdek.caller.MdekCaller;
import de.ingrid.mdek.caller.MdekCallerAddress;
import de.ingrid.mdek.caller.MdekCallerCatalog;
import de.ingrid.mdek.caller.MdekCallerObject;
import de.ingrid.mdek.caller.MdekClientCaller;
import de.ingrid.mdek.xml.importer.mapper.IngridXMLMapper;
import de.ingrid.mdek.xml.importer.mapper.IngridXMLMapperFactory;
import de.ingrid.mdek.xml.util.IngridXMLUtils;
import de.ingrid.utils.IngridDocument;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

public class TestXMLStreamReader {

	private static IngridXMLMapper mapper;
	private static IngridXMLStreamReader streamReader;
	private static Set<String> objectUuids;
	private static Set<String> addressUuids;

	private static IMdekClientCaller mdekClientCaller;
	private static IMdekCallerObject mdekCallerObject;
	private static IMdekCallerAddress mdekCallerAddress;
	private static String plugId;
	private static final String userId = "admin";

	@BeforeAll
	public static void setup() throws XMLStreamException, IOException {
		setupImporter();
		setupConnection();
	}

	@AfterAll
	public static void shutdown() {
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
		mdekCallerObject = MdekCallerObject.getInstance();
		MdekCallerAddress.initialize(mdekClientCaller);
		mdekCallerAddress = MdekCallerAddress.getInstance();
		MdekCallerCatalog.initialize(mdekClientCaller);

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

	public static void setupImporter() throws XMLStreamException, IOException {
		Reader reader = new FileReader("src/test/resources/test.xml");
		String version = IngridXMLUtils.getVersion(reader);
		mapper = IngridXMLMapperFactory.getIngridXMLMapper(version);

		InputStream in = new FileInputStream("src/test/resources/test.xml");
		IImporterCallback callback = null;
		streamReader = new IngridXMLStreamReader(in, callback, "");
		objectUuids = streamReader.getObjectUuids();
		addressUuids = streamReader.getAddressUuids();
	}

	@Test
	public void testXMLDatasourceImporter() throws IOException, SAXException {
		for (String objUuid : objectUuids) {
			List<Document> docs = streamReader.getDomForObject(objUuid);
			for (Document doc : docs) {
				IngridDocument dataSource = mapper.mapDataSource(doc);
			}
		}
	}

	@Test
	public void testXMLAddressImporter() throws IOException, SAXException  {
		for (String adrUuid : addressUuids) {
			List<Document> docs = streamReader.getDomForAddress(adrUuid);
			for (Document doc : docs) {
				IngridDocument address = mapper.mapAddress(doc);
			}
		}
	}

	@Test
	public void testXMLObjectImporterValidity() throws IOException, SAXException {
		for (String objUuid : objectUuids) {
			List<Document> docs = streamReader.getDomForObject(objUuid);
			for (Document doc : docs) {
				IngridDocument importedDataSource = mapper.mapDataSource(doc);
				IngridDocument originalDataSource = getDataSourceFromDB(objUuid);

				System.out.println("original: "+originalDataSource);
				System.out.println("imported: "+importedDataSource);

				compare(originalDataSource, importedDataSource);
				compare(importedDataSource, originalDataSource);
			}
		}
	}

	@Test
	public void testXMLAddressImporterValidity() throws IOException, SAXException {
		for (String adrUuid : addressUuids) {
			List<Document> docs = streamReader.getDomForAddress(adrUuid);
			for (Document doc : docs) {
				IngridDocument importedAddress = mapper.mapAddress(doc);
				IngridDocument originalAddress = getAddressFromDB(adrUuid);

				System.out.println("original: "+originalAddress);
				System.out.println("imported: "+importedAddress);

				compareAddress(originalAddress, importedAddress);
				compareAddress(importedAddress, originalAddress);
			}
		}
	}

	public static void compareAdditionalFields(IngridDocument expected, IngridDocument actual) {
		for (Map.Entry<Object, Object> entry : (Set<Map.Entry<Object, Object>>) expected.entrySet()) {
			String key = (String) entry.getKey();
			Object expectedValue = entry.getValue();
			Object actualValue = actual.get(key);

			if (expectedValue instanceof IngridDocument) {
				if (actualValue != null) {
					compareAdditionalFields((IngridDocument) expectedValue, (IngridDocument) actualValue);

				} else if (isEmpty((IngridDocument) expectedValue)) {
					continue;

				} else {
					assertNotNull(actualValue, "Actual value for key '"+key+"' must not be null.");
				}

			} else if (expectedValue instanceof String[]) {
				assertArrayEquals((String[]) expectedValue, (String[]) actualValue);

			} else {
				assertThat("Values for key '"+key+"' must be equal.", actualValue, is(expectedValue));
			}
		}
	}

	public static void compareAddress(IngridDocument expected, IngridDocument actual) {
		for (Map.Entry<Object, Object> entry : (Set<Map.Entry<Object, Object>>) expected.entrySet()) {
			String key = (String) entry.getKey();
			Object expectedValue = entry.getValue();
			Object actualValue = actual.get(key);

			if (isAddressKeyExported(key, expected)) {
				if (MdekKeys.MOD_USER.equals(key)) {
					compareModUser((IngridDocument) expected, (IngridDocument) actual);

				} else if (MdekKeys.SUBJECT_TERMS.equals(key)) {
					compareSubjectTerms((List) expectedValue, (List) actualValue);

				} else if (expectedValue instanceof List) {
					compare((List) expectedValue, (List) actualValue, key);

				} else if (expectedValue instanceof IngridDocument) {
					if (actualValue != null) {
						compareAddress((IngridDocument) expectedValue, (IngridDocument) actualValue);

					} else if (isEmpty((IngridDocument) expectedValue)) {
						continue;

					} else {
						assertNotNull(actualValue, "Actual value for key '"+key+"' must not be null.");
					}

				} else {
					assertThat("Values for key '"+key+"' must be equal.", expectedValue, is(actualValue));
				}
			}
		}
	}

	public static void compare(IngridDocument expected, IngridDocument actual) {
		for (Map.Entry<Object, Object> entry : (Set<Map.Entry<Object, Object>>) expected.entrySet()) {
			String key = (String) entry.getKey();
			Object expectedValue = entry.getValue();
			Object actualValue = actual.get(key);

			if (isKeyExported(key, expected)) {
				if (MdekKeys.ADR_REFERENCES_TO.equals(key)) {
					compareAddressReferences((List) expectedValue, (List) actualValue);

				} else if (MdekKeys.OBJ_REFERENCES_TO.equals(key)) {
					compareObjectReferences((List) expectedValue, (List) actualValue);

				} else if (MdekKeys.MOD_USER.equals(key)) {
					compareModUser((IngridDocument) expected, (IngridDocument) actual);

				} else if (MdekKeys.SUBJECT_TERMS.equals(key)) {
					compareSubjectTerms((List) expectedValue, (List) actualValue);

				} else if (MdekKeys.ADDITIONAL_FIELDS.equals(key)) {
					compareAdditionalFields((List) expectedValue, (List) actualValue);

				} else if (expectedValue instanceof List) {
					compare((List) expectedValue, (List) actualValue, key);

				} else if (expectedValue instanceof IngridDocument) {
					if (actualValue != null) {
						compare((IngridDocument) expectedValue, (IngridDocument) actualValue);

					} else if (isEmpty((IngridDocument) expectedValue)) {
						continue;

					} else {
						assertNotNull(actualValue, "Actual value for key '"+key+"' must not be null.");
					}

				} else {
					assertThat("Values for key '"+key+"' must be equal.", expectedValue, is(actualValue));
				}
			}
		}
	}

	public static void compare(List expectedList, List actualList, String key) {
		if (expectedList == null && actualList != null) {
			if (actualList.isEmpty()) {
				return;

			} else {
				assertNotNull(expectedList, "Expected list for key '"+key+"' must not be null.");
			}

		} else if (actualList == null && expectedList != null) {
			if (expectedList.isEmpty()) {
				return;

			} else {
				assertNotNull(actualList, "Actual list for key '"+key+"' must not be null.");
			}
		}

		if (expectedList.size() != actualList.size()) {
			if (MdekKeys.FEATURE_TYPE_LIST.equals(key)) {
				return;

			} else {
				System.err.println("Lists for key '"+key+"' are of different size!");
				assertTrue(false);
			}

		} else {
			for (int index = 0; index < expectedList.size(); index++) {
				Object expectedValue = expectedList.get(index);
				Object actualValue = actualList.get(index);

				if (expectedValue instanceof IngridDocument) {
					compare((IngridDocument) expectedValue, (IngridDocument) actualValue);

				} else {
					assertEquals(expectedValue, actualValue);
				}
			}
		}
	}

	public static void compareAddressReferences(List expectedList, List actualList) {
		for (int index = 0; index < expectedList.size(); index++) {
			IngridDocument expectedAdrRef = (IngridDocument) expectedList.get(index);
			IngridDocument actualAdrRef = (IngridDocument) actualList.get(index);
			compareAddressRef(expectedAdrRef, actualAdrRef);
		}
	}

	public static void compareAddressRef(IngridDocument expectedAdr, IngridDocument actualAdr) {
		assertEquals(expectedAdr.get(MdekKeys.UUID), actualAdr.get(MdekKeys.UUID));
		assertEquals(expectedAdr.get(MdekKeys.DATE_OF_LAST_MODIFICATION), actualAdr.get(MdekKeys.DATE_OF_LAST_MODIFICATION));
		assertEquals(expectedAdr.get(MdekKeys.RELATION_TYPE_NAME), actualAdr.get(MdekKeys.RELATION_TYPE_NAME));
		assertEquals(expectedAdr.get(MdekKeys.RELATION_TYPE_REF), actualAdr.get(MdekKeys.RELATION_TYPE_REF));
		assertEquals(expectedAdr.get(MdekKeys.RELATION_TYPE_ID), actualAdr.get(MdekKeys.RELATION_TYPE_ID));
	}

	public static void compareObjectReferences(List expectedList, List actualList) {
		for (int index = 0; index < expectedList.size(); index++) {
			IngridDocument expectedObjRef = (IngridDocument) expectedList.get(index);
			IngridDocument actualObjRef = (IngridDocument) actualList.get(index);

			assertEquals(expectedObjRef.get(MdekKeys.UUID), actualObjRef.get(MdekKeys.UUID));
			assertEquals(expectedObjRef.get(MdekKeys.RELATION_DESCRIPTION), actualObjRef.get(MdekKeys.RELATION_DESCRIPTION));
			assertEquals(expectedObjRef.get(MdekKeys.RELATION_TYPE_NAME), actualObjRef.get(MdekKeys.RELATION_TYPE_NAME));
			assertEquals(expectedObjRef.get(MdekKeys.RELATION_TYPE_REF), actualObjRef.get(MdekKeys.RELATION_TYPE_REF));
		}
	}

	public static void compareSubjectTerms(List expectedList, List actualList) {
		if (expectedList.size() != actualList.size()) {
			System.err.println("subject term lists are of different size!");
			assertTrue(false);
		}

		for (IngridDocument term : (List<IngridDocument>) expectedList) {
			if (!listContainsTerm(actualList, term)) {
				System.err.println("The term "+term+" was not found in the target list.");
				assertTrue(false);
			}
		}
	}

	public static boolean listContainsTerm(List<IngridDocument> list, IngridDocument term) {
		for (IngridDocument listItem : list) {
			if (termEquals(term, listItem)) {
				return true;
			}
		}
		return false;
	}

	public static boolean termEquals(IngridDocument expectedTerm, IngridDocument actualTerm) {
		if (MdekUtils.SearchtermType.UMTHES.getDbValue().equals(expectedTerm.get(MdekKeys.TERM_TYPE))) {
			if (expectedTerm.get(MdekKeys.TERM_SNS_ID).equals(actualTerm.get(MdekKeys.TERM_SNS_ID)) &&
					expectedTerm.get(MdekKeys.TERM_TYPE).equals(actualTerm.get(MdekKeys.TERM_TYPE)) &&
					expectedTerm.get(MdekKeys.TERM_NAME).equals(actualTerm.get(MdekKeys.TERM_NAME))) {
				return true;

			} else {
				return false;
			}

		} else if (MdekUtils.SearchtermType.GEMET.getDbValue().equals(expectedTerm.get(MdekKeys.TERM_TYPE))) {
				if (expectedTerm.get(MdekKeys.TERM_SNS_ID).equals(actualTerm.get(MdekKeys.TERM_SNS_ID)) &&
						expectedTerm.get(MdekKeys.TERM_GEMET_ID).equals(actualTerm.get(MdekKeys.TERM_GEMET_ID)) &&
						expectedTerm.get(MdekKeys.TERM_TYPE).equals(actualTerm.get(MdekKeys.TERM_TYPE)) &&
						expectedTerm.get(MdekKeys.TERM_NAME).equals(actualTerm.get(MdekKeys.TERM_NAME))) {
					return true;

				} else {
					return false;
				}

		} else if (MdekUtils.SearchtermType.INSPIRE.getDbValue().equals(expectedTerm.get(MdekKeys.TERM_TYPE))) {
			if (expectedTerm.get(MdekKeys.TERM_ENTRY_ID).equals(actualTerm.get(MdekKeys.TERM_ENTRY_ID)) &&
					expectedTerm.get(MdekKeys.TERM_TYPE).equals(actualTerm.get(MdekKeys.TERM_TYPE)) &&
					expectedTerm.get(MdekKeys.TERM_NAME).equals(actualTerm.get(MdekKeys.TERM_NAME))) {
				return true;

			} else {
				return false;
			}

		} else if (MdekUtils.SearchtermType.FREI.getDbValue().equals(expectedTerm.get(MdekKeys.TERM_TYPE))) {
			if (expectedTerm.get(MdekKeys.TERM_TYPE).equals(actualTerm.get(MdekKeys.TERM_TYPE)) &&
					expectedTerm.get(MdekKeys.TERM_NAME).equals(actualTerm.get(MdekKeys.TERM_NAME))) {
				return true;

			} else {
				return false;
			}
		} else {
			System.err.println("Could not determine term type for term "+expectedTerm);
			return false;
		}
	}

	public static void compareAdditionalFields(List expectedList, List actualList) {
		if (expectedList.size() != actualList.size()) {
			System.err.println("additional fields lists are of different size!");
			assertTrue(false);
		}

		for (IngridDocument additionalField : (List<IngridDocument>) expectedList) {
			if (!listContainsAdditionalField(actualList, additionalField)) {
				System.err.println("The additional field "+additionalField+" was not found in the target list.");
				assertTrue(false);
			}
		}
	}

	public static boolean listContainsAdditionalField(List<IngridDocument> list, IngridDocument additionalField) {
		for (IngridDocument listItem : list) {
			if (additionalFieldEquals(additionalField, listItem)) {
				return true;
			}
		}
		return false;
	}

	public static boolean additionalFieldEquals(IngridDocument expectedField, IngridDocument actualField) {
		if (expectedField.get(MdekKeys.ADDITIONAL_FIELD_KEY).equals(actualField.get(MdekKeys.ADDITIONAL_FIELD_KEY))
//				&& expectedField.get(MdekKeys.ADD).equals(actualField.get(MdekKeys.SYS_ADDITIONAL_FIELD_NAME)) &&
//				expectedField.get(MdekKeys.ADDITIONAL_FIELD_VALUE).equals(actualField.get(MdekKeys.ADDITIONAL_FIELD_VALUE))
				) {
			return true;

		} else {
			return false;
		}
	}

	public static void compareModUser(IngridDocument expected, IngridDocument actual) {
		assertEquals(expected.get(MdekKeys.UUID), actual.get(MdekKeys.UUID));
	}

	public static boolean isAddressKeyExported(String key, IngridDocument context) {
		if (MdekKeys.OBJ_REFERENCES_FROM_TOTAL_NUM.equals(key) ||
				MdekKeys.OBJ_REFERENCES_FROM_PUBLISHED_ONLY.equals(key) ||
				MdekKeys.OBJ_REFERENCES_FROM_START_INDEX.equals(key) ||
				MdekKeys.OBJ_REFERENCES_FROM.equals(key) ||
				MdekKeysSecurity.IDC_PERMISSIONS.equals(key) ||
				MdekKeys.PATH_ORGANISATIONS.equals(key) ||
				MdekKeys.EXPIRY_STATE.equals(key) ||
				MdekKeys.MARK_DELETED.equals(key) ||
				MdekKeys.IS_PUBLISHED.equals(key) ||
				MdekKeys.WORK_STATE.equals(key) ||
				MdekKeys.PARENT_INFO.equals(key) ||
				MdekKeys.ID.equals(key)) {
			return false;
		}

		return true;
	}

	public static boolean isKeyExported(String key, IngridDocument context) {
		if (MdekKeys.ASSIGNER_USER.equals(key) ||
				MdekKeys.OBJ_REFERENCES_FROM_PUBLISHED_ONLY.equals(key) ||
				MdekKeys.OBJ_REFERENCES_FROM.equals(key) ||
				MdekKeys.ID.equals(key) ||
				MdekKeysSecurity.IDC_PERMISSIONS.equals(key) ||
				MdekKeys.MARK_DELETED.equals(key) ||
				MdekKeys.WORK_STATE.equals(key) ||
				MdekKeys.IS_PUBLISHED.equals(key) ||
				MdekKeys.PARENT_INFO.equals(key) ||
				MdekKeys.EXPIRY_STATE.equals(key)) {
			return false;
		}

		if (MdekKeys.LOCATION_CODE.equals(key) &&
				MdekUtils.SpatialReferenceType.FREI.getDbValue().equals(context.get(MdekKeys.LOCATION_TYPE))) {
			return false;
		}
		if (MdekKeys.LOCATION_NAME_KEY.equals(key) &&
				MdekUtils.SpatialReferenceType.GEO_THESAURUS.getDbValue().equals(context.get(MdekKeys.LOCATION_TYPE))) {
			return false;
		}

		if (MdekKeys.LOCATION_NAME_KEY.equals(key) &&
				MdekUtils.SpatialReferenceType.GEO_THESAURUS.getDbValue().equals(context.get(MdekKeys.LOCATION_TYPE))) {
			return false;
		}

		return true;
	}

	public static boolean isEmpty(IngridDocument doc) {
		for (Object entry : doc.values()) {
			if (entry != null) {
				return false;
			}
		}
		return true;
	}

	public IngridDocument getDataSourceFromDB(String objUuid) {
		IngridDocument objDocResponse = mdekCallerObject.fetchObject(plugId, objUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION, userId);
		return mdekClientCaller.getResultFromResponse(objDocResponse);
	}
	public IngridDocument getAddressFromDB(String adrUuid) {
		IngridDocument adrDocResponse = mdekCallerAddress.fetchAddress(plugId, adrUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION, 0, 0, userId);
		return mdekClientCaller.getResultFromResponse(adrDocResponse);
	}
}
