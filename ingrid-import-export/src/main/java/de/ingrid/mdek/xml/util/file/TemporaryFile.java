/*
 * **************************************************-
 * ingrid-import-export
 * ==================================================
 * Copyright (C) 2014 wemove digital solutions GmbH
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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

public class TemporaryFile {

	private final static String FILE_SUFFIX = "ingridTemp";

	private final File file;

	public TemporaryFile() throws IOException {
		file = File.createTempFile(createRandomFileName(), null);
		file.deleteOnExit();
	}

	private String createRandomFileName() {
		Random r = new Random();
		return FILE_SUFFIX + r.nextInt();
	}

	public File getFile() {
		return file;
	}

	public void write(InputStream in) throws IOException {
		OutputStream out = new BufferedOutputStream(new FileOutputStream(file));

		final int BUFFER = 2048;
		int count;
		byte data[] = new byte[BUFFER];
		while((count = in.read(data, 0, BUFFER)) != -1) {
		   out.write(data, 0, count);
		}

		out.close();
		in.close();
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			if (file.exists()) {
				file.delete();
			}
		} finally {
			super.finalize();
		}
	}
}