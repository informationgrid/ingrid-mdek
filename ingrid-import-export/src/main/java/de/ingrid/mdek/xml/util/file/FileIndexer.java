package de.ingrid.mdek.xml.util.file;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import de.ingrid.mdek.xml.importer.IImporterCallback;
import edu.emory.mathcs.backport.java.util.Collections;


public class FileIndexer {
	private final static Logger log = Logger.getLogger(FileIndexer.class);

	private final static String XML_ENCODING = "ISO-8859-1";
	
	private static final char START_TAG = '<';
	private static final char END_TAG = '>';

	private final File file;
	private PushbackReader reader;
	private long charsRead;
	private Map<String, FileIndex> objectIndexMap = null;
	private Map<String, FileIndex> addressIndexMap = null;
	private FileIndex additionalFieldsIndex = null;
	
	private IImporterCallback importerCallback;
	private String currentUserUuid;

	public FileIndexer(File file, IImporterCallback importerCallback, String userUuid) {
		this.file = file;
		this.importerCallback = importerCallback;
		this.currentUserUuid = userUuid;
	}

	public Map<String, FileIndex> getObjectIndexMap() {
		if (objectIndexMap == null) {
			objectIndexMap = createObjectIndexMap();
		}

		return objectIndexMap;
	}

	public Map<String, FileIndex> getAddressIndexMap() {
		if (addressIndexMap == null) {
			addressIndexMap = createAddressIndexMap();
		}

		return addressIndexMap;
	}

	public FileIndex getAdditionalFieldsIndex() {
		if (additionalFieldsIndex == null) {
			additionalFieldsIndex = createAdditionalFieldsIndex();
		}

		return additionalFieldsIndex;
	}

	private FileIndex createAdditionalFieldsIndex() {
		FileIndex additionalFieldsIndex;
		try {
			reader = new PushbackReader(new BufferedReader(new InputStreamReader(new FileInputStream(file), XML_ENCODING)));
			charsRead = 0;
			additionalFieldsIndex = createAdditionalFieldsIndexOrThrow();
		} catch (Exception e) {
			// Exception while creating the index. Return null
			additionalFieldsIndex = null;
			log.error("Error creating AdditionalFields index map from file.", e);
			importerCallback.writeImportInfoMessage(e.toString() + "(AdditionalFields)", currentUserUuid);
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				// Ignore the IOException. If close fails, we can't really do much about it.
			}
		}

		return additionalFieldsIndex;
	}

	private Map<String, FileIndex> createObjectIndexMap() {
		Map<String, FileIndex> fileIndexMap;
		try {
			reader = new PushbackReader(new BufferedReader(new InputStreamReader(new FileInputStream(file), XML_ENCODING)));
			charsRead = 0;
			fileIndexMap = createObjectIndexMapOrThrow();

		} catch (Exception e) {
			// Exception while creating the index map. Return an empty map
			log.error("Error creating Object index map from file.", e);
			importerCallback.writeImportInfoMessage(e.toString() + "(ObjectIndex)", currentUserUuid);
			fileIndexMap = Collections.emptyMap();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				// Ignore the IOException. If close fails, we can't really do much about it.
			}
		}

		return fileIndexMap;
	}

	private Map<String, FileIndex> createAddressIndexMap() {
		Map<String, FileIndex> fileIndexMap;
		try {
			reader = new PushbackReader(new BufferedReader(new InputStreamReader(new FileInputStream(file), XML_ENCODING)));
			charsRead = 0;
			fileIndexMap = createAddressIndexMapOrThrow();

		} catch (Exception e) {
			// Exception while creating the index map. Return an empty map
			log.error("Error creating Address index map from file.", e);
			importerCallback.writeImportInfoMessage(e.toString() + "(AddressIndex)", currentUserUuid);
			fileIndexMap = Collections.emptyMap();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				// Ignore the IOException. If close fails, we can't really do much about it.
			}
		}

		return fileIndexMap;
	}

	private Map<String, FileIndex> createObjectIndexMapOrThrow() throws IOException {
		Map<String, FileIndex> fileIndex = new HashMap<String, FileIndex>();

		try {
			skipToNext("<data-sources>");			
		} catch (IOException e) {
			// NO OBJECTS in import file
			return fileIndex;
		}

		do {
			long beginOfOpeningDatasourceTag = -1;
			long endOfClosingDataSourceTag = -1;
			String uuid = "";

			String dataSourceBeginTag = skipToOneOfNext(new String[] {"<data-source>", "</data-sources>"});
			if (dataSourceBeginTag.equals("</data-sources>")) {
				break;
			}

			beginOfOpeningDatasourceTag = charsRead - "<data-source>".length();
			skipToNext("<general>");
			skipToOneOfNext(new String[] {"<object-identifier>", "<original-control-identifier>"});
			uuid = readTagText();
			skipToNext("</data-source>");
			endOfClosingDataSourceTag = charsRead;
			fileIndex.put(uuid, new FileIndex(beginOfOpeningDatasourceTag, endOfClosingDataSourceTag));

		} while (true);

		reader.close();
		return fileIndex;
	}

	private Map<String, FileIndex> createAddressIndexMapOrThrow() throws IOException {
		Map<String, FileIndex> fileIndex = new HashMap<String, FileIndex>();

		try {
			skipToNext("<addresses>");			
		} catch (IOException e) {
			// NO ADDRESSES in import file
			return fileIndex;
		}

		do {
			long beginOfOpeningAddressTag = -1;
			long endOfClosingAddressTag = -1;
			String uuid = "";

			String dataSourceBeginTag = skipToOneOfNext(new String[] {"<address>", "</addresses>"});
			if (dataSourceBeginTag.equals("</addresses>")) {
				break;
			}

			beginOfOpeningAddressTag = charsRead - "<address>".length();
			skipToOneOfNext(new String[] {"<address-identifier>", "<original-address-identifier>"});
			uuid = readTagText();
			skipToNext("</address>");
			endOfClosingAddressTag = charsRead;
			fileIndex.put(uuid, new FileIndex(beginOfOpeningAddressTag, endOfClosingAddressTag));

		} while (true);

		reader.close();
		return fileIndex;
	}

	private FileIndex createAdditionalFieldsIndexOrThrow() throws IOException {
		FileIndex fileIndex = null;

		try {
			skipToNext("<data-model-extensions>");
		} catch (IOException e) {
			// NO ADDITIONAL FIELDS in import file
			return fileIndex;
		}

		long beginIndex = charsRead - "<data-model-extensions>".length();
		skipToNext("</data-model-extensions>");
		long endIndex = charsRead;
		fileIndex = new FileIndex(beginIndex, endIndex);

		reader.close();
		return fileIndex;
	}

	
	private String readTagText() throws IOException {
		StringBuilder stringBuilder = new StringBuilder();

		int c = -1;
		while (c != START_TAG) {
			c = reader.read();
			charsRead++;
			stringBuilder.append((char) c);
		}
		reader.unread(c);
		charsRead--;
		stringBuilder.setLength(stringBuilder.length() - 1);

		return stringBuilder.toString().trim();
	}

	private String skipToOneOfNext(String[] tags) throws IOException {
		String tagInfo = "";
		for (String tag : tags) {
			if (tagInfo.length() != 0) {
				tagInfo += ", ";
			}
			tagInfo += tag;
		}

		String nextTag;
		do {
			nextTag = nextTag(tagInfo);
		} while (!Arrays.asList(tags).contains(nextTag));
		return nextTag;
	}

	private void skipToNext(String tag) throws IOException {
		String nextTag;
		do {
			nextTag = nextTag(tag);
		} while (!nextTag.equals(tag));
	}

	private String nextTag(String tagInfoForMsg) throws IOException {
		StringBuilder stringBuilder = new StringBuilder();

		int c = -1;
		do {
			c = reader.read();
			charsRead++;

			if (c == -1) {
				throw new EOFException("Tried to find the start of the next xml tag but end of stream was reached (" + tagInfoForMsg + ").");
			}

		} while (c != START_TAG);

		do {
			stringBuilder.append((char) c);
			c = reader.read();
			charsRead++;

			if (c == -1) {
				throw new EOFException("Tried to read the end of the current xml tag but end of stream was reached (" + tagInfoForMsg + ").");
			}

		} while (c != END_TAG);
		stringBuilder.append((char) c);

		return stringBuilder.toString().trim();
	}
}
