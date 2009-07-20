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

import edu.emory.mathcs.backport.java.util.Collections;


public class FileIndexer {

	private final static String XML_ENCODING = "ISO-8859-1";
	
	private static final char START_TAG = '<';
	private static final char END_TAG = '>';

	private final File file;
	private PushbackReader reader;
	private long charsRead;
	private Map<String, FileIndex> objectIndexMap = null;
	private Map<String, FileIndex> addressIndexMap = null;
	private FileIndex additionalFieldsIndex = null;

	public FileIndexer(File file) {
		this.file = file;
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

		skipToNext("<data-sources>");

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

		skipToNext("<addresses>");

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
		skipToNext("<data-model-extensions>");
		long beginIndex = charsRead - "<data-model-extensions>".length();
		skipToNext("</data-model-extensions>");
		long endIndex = charsRead;

		reader.close();
		return new FileIndex(beginIndex, endIndex);
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
		String nextTag;
		do {
			nextTag = nextTag();
		} while (!Arrays.asList(tags).contains(nextTag));
		return nextTag;
	}

	private void skipToNext(String tag) throws IOException {
		String nextTag;
		do {
			nextTag = nextTag();
		} while (!nextTag.equals(tag));
	}

	private String nextTag() throws IOException {
		StringBuilder stringBuilder = new StringBuilder();

		int c = -1;
		do {
			c = reader.read();
			charsRead++;

			if (c == -1) {
				throw new EOFException("Tried to find the start of the next xml tag but end of stream was reached.");
			}

		} while (c != START_TAG);

		do {
			stringBuilder.append((char) c);
			c = reader.read();
			charsRead++;

			if (c == -1) {
				throw new EOFException("Tried to read the end of the current xml tag but end of stream was reached.");
			}

		} while (c != END_TAG);
		stringBuilder.append((char) c);

		return stringBuilder.toString().trim();
	}
}
