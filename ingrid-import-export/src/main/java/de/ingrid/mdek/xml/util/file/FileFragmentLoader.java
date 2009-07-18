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
