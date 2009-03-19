package de.ingrid.mdek.job;

import de.ingrid.mdek.IMdekEnum;
import de.ingrid.utils.IngridDocument;

public interface IJob {

	/** Type of Job Operations. Used for tracking jobs. */
	public enum JobType implements IMdekEnum {
		/** e.g. generate working copy, assign entity to QA, store group, syslists ...*/
		STORE("STORE"),
		/** publish Object/Address ...*/
		PUBLISH("PUBLISH"),
		COPY("COPY"),
		MOVE("MOVE"),
		DELETE("DELETE"),
		CHECK("CHECK"),
		EXPORT("EXPORT"),
		IMPORT("IMPORT"),
		URL("URL"),
		ANALYZE("ANALYZE"),
		/** e.g. replace address ...*/
		REPLACE("REPLACE"),
		/** update all syslist data in entities/index */
		REBUILD_SYSLISTS("REBUILD_SYSLISTS");

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
