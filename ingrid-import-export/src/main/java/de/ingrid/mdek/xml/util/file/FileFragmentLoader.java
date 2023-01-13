/*
 * **************************************************-
 * ingrid-import-export
 * ==================================================
 * Copyright (C) 2014 - 2023 wemove digital solutions GmbH
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileFragmentLoader {

	private final RandomAccessFile file;

	public FileFragmentLoader(File file) throws FileNotFoundException {
		this.file = new RandomAccessFile(file, "r");
	}

	public String getStringUTF(FileIndex fileIndex) throws IOException {
		return getString(fileIndex, "UTF-8");
	}

	public String getString(FileIndex fileIndex, String encoding) throws IOException {
		StringBuilder stringBuilder = new StringBuilder();
		file.seek(fileIndex.getBeginIndex());
		byte[] buffer = new byte[(int) (fileIndex.getEndIndex() - fileIndex.getBeginIndex())];
		file.readFully(buffer);
		stringBuilder.append(new String(buffer, encoding));
		return stringBuilder.toString();
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			file.close();
		} finally {
			super.finalize();
		}
	}
}
