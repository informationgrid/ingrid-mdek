package de.ingrid.mdek;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

/**
 * Class encapsulating utility methods.
 * 
 * @author Martin
 */
public class MdekUtils {

	private static final Logger LOG = Logger.getLogger(MdekUtils.class);

	private final static SimpleDateFormat timestampFormatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");
	private final static SimpleDateFormat displayDateFormatter = new SimpleDateFormat("dd.MM.yyyy");

	/** WorkState of entities */
	public enum WorkState implements IMdekEnum {
		VEROEFFENTLICHT("V", "ver\u00f6ffentlicht"),
		IN_BEARBEITUNG("B", "in Bearbeitung"),
		QS_UEBERWIESEN("Q", "an Qualit\u00e4tssicherung zugewiesen"),
		QS_RUECKUEBERWIESEN("R", "von Qualit\u00e4tssicherung r\u00fcck\u00fcberwiesen");

		WorkState(String dbValue, String description) {
			this.dbValue = dbValue;
			this.description = description;
		}
		public String getDbValue() {
			return dbValue;
		}
		public String toString() {
			return description;
		}
		String dbValue;
		String description;
	}

/*
	private static MdekUtils myInstance;

	public static synchronized MdekUtils getInstance() {
		if (myInstance == null) {
	        myInstance = new MdekUtils();
	      }
		return myInstance;
	}

	private MdekUtils() {}
*/

	/** Format database timestamp to displayable date. */
	public static String timestampToDisplayDate(String yyyyMMddHHmmssSSS) {
		try {
			Date in = timestampFormatter.parse(yyyyMMddHHmmssSSS);
			String out = displayDateFormatter.format(in);
			return out;
		} catch (Exception ex){
			LOG.warn("Problems parsing timestamp from database: " + yyyyMMddHHmmssSSS, ex);
			return "";
		}
	}
	/** Format date to database timestamp. */
	public static String dateToTimestamp(Date date) {
		try {
			String out = timestampFormatter.format(date);
			return out;
		} catch (Exception ex){
			LOG.warn("Problems formating date to timestamp: " + date, ex);
			return "";
		}
	}
}
