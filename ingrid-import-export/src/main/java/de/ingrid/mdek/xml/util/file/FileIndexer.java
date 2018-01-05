/*
 * **************************************************-
 * ingrid-import-export
 * ==================================================
 * Copyright (C) 2014 - 2018 wemove digital solutions GmbH
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
package de.ingrid.mdek.xml.util.file;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.ingrid.mdek.xml.importer.IImporterCallback;
import edu.emory.mathcs.backport.java.util.Collections;


public class FileIndexer {
	private final static Logger log = LogManager.getLogger(FileIndexer.class);

	private final static String XML_ENCODING = "ISO-8859-1";
	
	private static final char START_TAG = '<';
	private static final char END_TAG = '>';

	private final File file;
	private PushbackReader reader;
	private long charsRead;
	/** uuid -> instances (instance start-/end-index in file). If size of list > 1 then order is "Bearbeitungsinstanz", "veröffentlichte Instanz". */
	private Map<String, List<FileIndex>> objectIndexMap = null;
	/** uuid -> instances (instance start-/end-index in file). If size of list > 1 then order is "Bearbeitungsinstanz", "veröffentlichte Instanz". */
	private Map<String, List<FileIndex>> addressIndexMap = null;
	
	private IImporterCallback importerCallback;
	private String currentUserUuid;

	public FileIndexer(File file, IImporterCallback importerCallback, String userUuid) {
		this.file = file;
		this.importerCallback = importerCallback;
		this.currentUserUuid = userUuid;
	}

	public Map<String, List<FileIndex>> getObjectIndexMap() {
		if (objectIndexMap == null) {
			objectIndexMap = createObjectIndexMap();
		}

		return objectIndexMap;
	}

	public Map<String, List<FileIndex>> getAddressIndexMap() {
		if (addressIndexMap == null) {
			addressIndexMap = createAddressIndexMap();
		}

		return addressIndexMap;
	}

	private Map<String, List<FileIndex>> createObjectIndexMap() {
		Map<String, List<FileIndex>> fileIndexMap;
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

	private Map<String, List<FileIndex>> createAddressIndexMap() {
		Map<String, List<FileIndex>> fileIndexMap;
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

	private Map<String, List<FileIndex>> createObjectIndexMapOrThrow() throws IOException {
		Map<String, List<FileIndex>> fileIndex = new HashMap<String, List<FileIndex>>();

		try {
			skipToNext("<data-sources>");			
		} catch (IOException e) {
			// NO OBJECTS in import file
			return fileIndex;
		}

		do {
			List<FileIndex> fileIndexList = new ArrayList<FileIndex>();
			String uuid = "";

			String tagRead = skipToOneOfNext(new String[] {"<data-source>", "</data-sources>"});
			if (tagRead.equals("</data-sources>")) {
				break;
			}

			do {
				// NOTICE: no closing ">" for data-source-instance search, HAS ATTRIBUTE !
				tagRead = skipToOneOfNext(new String[] {"<data-source-instance", "</data-source>"});
				if (tagRead.equals("</data-source>")) {
					break;
				}

				long beginOfOpeningInstanceTag = charsRead - tagRead.length();
				if (uuid.length() == 0) {
					skipToNext("<general>");
					skipToOneOfNext(new String[] {"<object-identifier>", "<original-control-identifier>"});
					uuid = readTagText();
				}
				skipToNext("</data-source-instance>");
				long endOfClosingInstanceTag = charsRead;
				
				fileIndexList.add(new FileIndex(beginOfOpeningInstanceTag, endOfClosingInstanceTag));

			} while (true);
			

			if (uuid != null && uuid.length() > 0) {
				fileIndex.put(uuid, fileIndexList);
			}

		} while (true);

		reader.close();
		return fileIndex;
	}

	private Map<String, List<FileIndex>> createAddressIndexMapOrThrow() throws IOException {
		Map<String, List<FileIndex>> fileIndex = new HashMap<String, List<FileIndex>>();

		try {
			skipToNext("<addresses>");			
		} catch (IOException e) {
			// NO ADDRESSES in import file
			return fileIndex;
		}

		do {
			List<FileIndex> fileIndexList = new ArrayList<FileIndex>();
			String uuid = "";

			String tagRead = skipToOneOfNext(new String[] {"<address>", "</addresses>"});
			if (tagRead.equals("</addresses>")) {
				break;
			}

			do {
				// NOTICE: no closing ">" for address-instance search, HAS ATTRIBUTE !
				tagRead = skipToOneOfNext(new String[] {"<address-instance", "</address>"});
				if (tagRead.equals("</address>")) {
					break;
				}

				long beginOfOpeningInstanceTag = charsRead - tagRead.length();
				if (uuid.length() == 0) {
					skipToOneOfNext(new String[] {"<address-identifier>", "<original-address-identifier>"});
					uuid = readTagText();
				}
				skipToNext("</address-instance>");
				long endOfClosingInstanceTag = charsRead;
				
				fileIndexList.add(new FileIndex(beginOfOpeningInstanceTag, endOfClosingInstanceTag));

			} while (true);

			if (uuid != null && uuid.length() > 0) {
				fileIndex.put(uuid, fileIndexList);
			}

		} while (true);

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
		boolean tagFound = false; 
		do {
			nextTag = nextTag(tagInfo);
			
			// check whether found next tag STARTS WITH one of our searched tags !
			// NOTICE: searched tags may have no closing ">" if tag has attribute !!!
			tagFound = false; 
			for (String tag : tags) {
				if (nextTag.startsWith(tag)) {
					tagFound = true;
					break;
				}
			}
		} while (!tagFound);
		return nextTag;
	}

	private void skipToNext(String tag) throws IOException {
		String nextTag;
		do {
			nextTag = nextTag(tag);
		} while (!nextTag.startsWith(tag));
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
