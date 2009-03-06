package de.ingrid.mdek.job;

import de.ingrid.mdek.IMdekEnum;
import de.ingrid.utils.IngridDocument;

public interface IJob {

	/** Type of Job Operations. Used for tracking jobs. */
	public enum JobType implements IMdekEnum {
		STORE("STORE"),
		PUBLISH("PUBLISH"),
		COPY("COPY"),
		MOVE("MOVE"),
		DELETE("DELETE"),
		CHECK("CHECK"),
		EXPORT("EXPORT"),
		IMPORT("IMPORT"),
		URL("URL"),
		ANALYZE("ANALYZE"),
		REPLACE("REPLACE");

		JobType(String dbValue) {
			this.dbValue = dbValue;
		}
		public String getDbValue() {
			return dbValue;
		}
		String dbValue;
	}

	IngridDocument getResults();
}
