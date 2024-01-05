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
package de.ingrid.mdek.xml.util.file;

/**
 * Encapsulates begin and end index of an instance of an entity !
 */
public class FileIndex {

	private final long beginIndex;
	private final long endIndex;

	public FileIndex(long beginIndex, long endIndex) {
		this.beginIndex = beginIndex;
		this.endIndex = endIndex;
	}

	public long getBeginIndex() {
		return beginIndex;
	}

	public long getEndIndex() {
		return endIndex;
	}
}
