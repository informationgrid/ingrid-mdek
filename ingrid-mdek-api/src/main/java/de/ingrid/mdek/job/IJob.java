/*
 * **************************************************-
 * ingrid-mdek-api
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
		IMPORT_ANALYZE("IMPORT_ANALYZE"),
		URL("URL"),
		ANALYZE("ANALYZE"),
		/** e.g. replace address ...*/
		REPLACE("REPLACE"),
		/** update all syslist data in entities / update index */
		REBUILD_SYSLISTS("REBUILD_SYSLISTS"),
		/** SNS update:  update searchterm data in entities */
		UPDATE_SEARCHTERMS("UPDATE_SEARCHTERMS"),
		/** SNS update:  update spatial reference data in entities */
		UPDATE_SPATIAL_REFERENCES("UPDATE_SPATIAL_REFERENCES");

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
