package de.ingrid.mdek;


/**
 * Class encapsulating utility methods for security.
 * 
 * @author Martin
 */
public class MdekUtilsSecurity {

	public enum IdcRole implements IMdekEnum {
		CATALOG_ADMINISTRATOR(1, "Katalog-Administrator"),
		METADATA_ADMINISTRATOR(2, "Metadaten-Administrator"),
		METADATA_AUTHOR(3, "Metadaten-Autor");

		IdcRole(Integer dbValue, String description) {
			this.dbValue = dbValue;
			this.description = description;
		}
		/** returns role ID */
		public Integer getDbValue() {
			return dbValue;
		}
		public String toString() {
			return description;
		}
		Integer dbValue;
		String description;
	}
}
