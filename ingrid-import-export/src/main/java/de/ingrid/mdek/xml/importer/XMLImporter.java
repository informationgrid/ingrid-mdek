package de.ingrid.mdek.xml.importer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.xml.stream.XMLStreamException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

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

	private int importObjectCount = 0;
	private int importAddressCount = 0;
	private int totalNumObjects = 0;
	private int totalNumAddresses = 0;

	public XMLImporter(IImporterCallback importerCallback) {
		this.importerCallback = importerCallback;
	}

	@Override
	public void countEntities(List<byte[]> importDataList, String userUuid) {
		for (byte[] importData : importDataList) {
			try {
				InputStream in = new GZIPInputStream(new ByteArrayInputStream(importData));
				streamReader = new IngridXMLStreamReader(in, importerCallback, currentUserUuid);

				List<String> objectWriteSequence = getObjectWriteSequence();
				totalNumObjects = totalNumObjects + objectWriteSequence.size();			

				List<String> addressWriteSequence = getAddressWriteSequence();
				totalNumAddresses = totalNumAddresses + addressWriteSequence.size();			

			} catch (IOException ex) {
				log.error("Error counting entities.", ex);
				importerCallback.writeImportInfoMessage("Error counting entities.", currentUserUuid);
			}
		}

		// and initialize Job Info in backend !
		importerCallback.writeImportInfo(IdcEntityType.OBJECT, 0, totalNumObjects, userUuid);
		importerCallback.writeImportInfo(IdcEntityType.ADDRESS, 0, totalNumAddresses, userUuid);
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
			// addresses before objects, so object-address relations won't get lost !
			importAddresses();
			importObjects();

		} catch (XMLStreamException ex) {
			log.error("Error reading file version.", ex);
			importerCallback.writeImportInfoMessage("Error reading file version.", currentUserUuid);

		} catch (IOException ex) {
			log.error("Error importing entities.", ex);
			importerCallback.writeImportInfoMessage("Error importing entities.", currentUserUuid);
		}
	}

	private void importObjects() {
		List<String> objectWriteSequence = getObjectWriteSequence();
		
		// only update total number if not set yet ! Maybe was already counted for multiple files !
		if (totalNumObjects == 0) {
			totalNumObjects = totalNumObjects + objectWriteSequence.size();			
		}

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
			importerCallback.writeObject(dataSource, currentUserUuid);
			importObjectCount++;
			importerCallback.writeImportInfo(IdcEntityType.OBJECT, importObjectCount, totalNumObjects, currentUserUuid);
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

		// only update total number if not set yet ! Maybe was already counted for multiple files !
		if (totalNumAddresses == 0) {
			totalNumAddresses = totalNumAddresses + addressWriteSequence.size();			
		}

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
			importerCallback.writeImportInfo(IdcEntityType.ADDRESS, importAddressCount, totalNumAddresses, currentUserUuid);
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
