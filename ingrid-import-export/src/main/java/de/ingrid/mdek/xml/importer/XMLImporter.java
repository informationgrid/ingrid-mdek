package de.ingrid.mdek.xml.importer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import javax.xml.stream.XMLStreamException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import de.ingrid.mdek.MdekUtils.IdcEntityType;
import de.ingrid.mdek.xml.importer.mapper.IngridXMLMapper;
import de.ingrid.mdek.xml.importer.mapper.IngridXMLMapperFactory;
import de.ingrid.mdek.xml.util.IngridXMLUtils;
import de.ingrid.utils.IngridDocument;

public class XMLImporter implements IImporter {

	private final static Logger log = Logger.getLogger(XMLImporter.class);
	private final IImporterCallback importerCallback;
	private IngridXMLMapper mapper;
	private IngridXMLStreamReader streamReader;	

	private String currentUserUuid;
	private IngridDocument additionalFields;

	private int importObjectCount;
	private int importAddressCount;
	private int totalNumObjects;
	private int totalNumAddresses;

	public XMLImporter(IImporterCallback importerCallback) {
		this.importerCallback = importerCallback;
	}

	@Override
	public void importEntities(byte[] importData,
			String userUuid) {
		this.currentUserUuid = userUuid;

		try {
			String version = getVersion(importData);
			mapper = IngridXMLMapperFactory.getIngridXMLMapper(version);

			InputStream in = new GZIPInputStream(new ByteArrayInputStream(importData));
			streamReader = new IngridXMLStreamReader(in, importerCallback, currentUserUuid);
			readAdditionalFieldDefinition();
			// addresses before objects, so object-address relations won't get lost !
			importAddresses();
			importObjects();

		} catch (XMLStreamException ex) {
			log.error("Error reading file version.", ex);
			importerCallback.writeImportInfoMessage("Error reading file version.", currentUserUuid);

		} catch (SAXException ex) {
			log.error("Error reading additional fields.", ex);
			importerCallback.writeImportInfoMessage("Error reading additional fields.", currentUserUuid);
		} catch (IOException ex) {
			log.error("Error importing entities.", ex);
			importerCallback.writeImportInfoMessage("Error importing entities.", currentUserUuid);
		}
	}

	private void readAdditionalFieldDefinition() throws SAXException, IOException {
		Document document = streamReader.getDomForAdditionalFieldDefinitions();
		if (mapper == null) {
			log.error("Oops! mapper should not be null!");
		}
		additionalFields = mapper.mapAdditionalFields(document);
	}

	private void importObjects() {
		List<String> objectWriteSequence = getObjectWriteSequence();
		totalNumObjects = totalNumObjects + objectWriteSequence.size();

		for (String objUuid : objectWriteSequence) {
			importObject(objUuid);
		}
	}

	private final List<String> getObjectWriteSequence() {
		try {
			return streamReader.getObjectWriteSequence();

		} catch (Exception ex) {
			log.error("Could not create object write sequence.", ex);
			throw new RuntimeException("Could not create object write sequence.", ex);
		}
	}

	private void importObject(String objUuid) {
		IngridDocument dataSource = getObject(objUuid);

		if (dataSource != null) {
			addAdditionalFieldDefinitionsToObject(dataSource);
			importerCallback.writeObject(dataSource, currentUserUuid);
			importObjectCount++;
			importerCallback.writeImportInfo(IdcEntityType.OBJECT, importObjectCount, importAddressCount, totalNumObjects, totalNumAddresses, currentUserUuid);
		}
	}

	private void addAdditionalFieldDefinitionsToObject(IngridDocument obj) {
		for (Map.Entry<String, Object> entry : (Set<Map.Entry<String, Object>>) additionalFields.entrySet()) {
			obj.put(entry.getKey(), entry.getValue());
		}
	}

	private IngridDocument getObject(String objUuid) {
		try {
			Document doc = streamReader.getDomForObject(objUuid);
			return mapper.mapDataSource(doc);

		} catch (Exception ex) {
			log.error("Error reading/mapping object with uuid '"+objUuid+"'", ex);
			throw new RuntimeException("Error reading/mapping object with uuid '"+objUuid+"'", ex);
		}
	}


	private void importAddresses() {
		List<String> addressWriteSequence = getAddressWriteSequence();
		totalNumAddresses = totalNumAddresses + addressWriteSequence.size();

		for (String adrUuid : addressWriteSequence) {
			importAddress(adrUuid);
		}
	}

	private final List<String> getAddressWriteSequence() {
		try {
			return streamReader.getAddressWriteSequence();

		} catch (Exception ex) {
			log.error("Could not create address write sequence.", ex);
			throw new RuntimeException("Could not create address write sequence.", ex);
		}
	}

	private void importAddress(String adrUuid) {
		IngridDocument address = getAddress(adrUuid);

		if (address != null) {
			importerCallback.writeAddress(address, currentUserUuid);
			importAddressCount++;
			importerCallback.writeImportInfo(IdcEntityType.ADDRESS, importObjectCount, importAddressCount, totalNumObjects, totalNumAddresses, currentUserUuid);
		}
	}

	private IngridDocument getAddress(String adrUuid) {
		try {
			Document doc = streamReader.getDomForAddress(adrUuid);
			return mapper.mapAddress(doc);

		} catch (Exception ex) {
			log.error("Error reading/mapping address with uuid '"+adrUuid+"'", ex);
			throw new RuntimeException("Error reading/mapping address with uuid '"+adrUuid+"'", ex);
		}
	}


	private static String getVersion(byte[] importData) throws XMLStreamException, IOException {
		InputStream in = new GZIPInputStream(new ByteArrayInputStream(importData));
		Reader reader = new InputStreamReader(in, Charset.forName("UTF-8"));
		String version = IngridXMLUtils.getVersion(reader);

		closeReader(reader);
		closeInputStream(in);

		return version;
	}

	private static void closeInputStream(InputStream in) {
		try {
			in.close();

		} catch (IOException ex) {
			// Log exception and continue
			log.error("Error closing input stream.", ex);
		}
	}

	private static void closeReader(Reader reader) {
		try {
			reader.close();

		} catch (IOException ex) {
			// Log exception and continue
			log.error("Error closing reader.", ex);
		}
	}
}
