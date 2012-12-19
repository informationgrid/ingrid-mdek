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