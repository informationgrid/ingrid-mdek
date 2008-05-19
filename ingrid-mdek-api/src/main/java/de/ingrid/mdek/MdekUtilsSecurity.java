package de.ingrid.mdek;

import java.util.List;

import de.ingrid.utils.IngridDocument;



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
		/**
		 * Is this Role "above" given role ? e.g.<br>
		 * CATALOG_ADMINISTRATOR.isAbove(METADATA_ADMINISTRATOR) -> true<br>
		 * CATALOG_ADMINISTRATOR.isAbove(METADATA_AUTHOR) -> true<br>
		 * METADATA_ADMINISTRATOR.isAbove(METADATA_AUTHOR) -> true<br>
		 * METADATA_ADMINISTRATOR.isAbove(CATALOG_ADMINISTRATOR) -> false<br>
		 * METADATA_ADMINISTRATOR.isAbove(METADATA_ADMINISTRATOR) -> false<br>
		 * METADATA_AUTHOR.isAbove(METADATA_AUTHOR) -> false<br>
		 */
		public boolean isAbove(IdcRole inRole) {
			if (inRole == null) {
				return false;
			}
			if (this.getDbValue() < inRole.getDbValue()) {
				return true;
			}
			return false;
		}
		Integer dbValue;
		String description;
	}

	/** Client side representation of Permissions !!! */
	public enum IdcPermission implements IMdekEnum {
		WRITE_SINGLE("write", "Einzelberechtigung"),
		WRITE_TREE("writeTree", "Teilbaumberechtigung"),
		CREATE_ROOT("createRoot", "Root anlegen"),
		QUALITY_ASSURANCE("qA", "Qualitätssicherung");

		/**
		 * @param dbValue THIS IS THE client side STRING USED IN MAP TO IDENTIFY PERMISSION
		 * @param description arbitrary description
		 */
		IdcPermission(String dbValue, String description) {
			this.dbValue = dbValue;
			this.description = description;
		}
		/** THIS IS THE client side STRING USED IN MAP TO IDENTIFY PERMISSION !!! */
		public String getDbValue() {
			return dbValue;
		}
		public String toString() {
			return description;
		}
		String dbValue;
		String description;
	}

	/**
	 * Checks whether permission list contains permission to write object (tree or single)
	 * @param idcPermissionsFromMap permission list delivered from backend
	 * @return true=has permission
	 */
	public static boolean hasWritePermission(List<IngridDocument> idcPermissionsFromMap) {
		if (idcPermissionsFromMap != null) {
			for (IngridDocument idcPermDoc : idcPermissionsFromMap) {
				IdcPermission idcPerm = EnumUtil.mapDatabaseToEnumConst(IdcPermission.class, idcPermDoc.get(MdekKeysSecurity.IDC_PERMISSION));
				if (idcPerm == IdcPermission.WRITE_SINGLE) {
					return true;
				}
				if (idcPerm == IdcPermission.WRITE_TREE) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Checks whether permission list contains permission to manipulate tree
	 * @param idcPermissionsFromMap permission list delivered from backend
	 * @return true=has permission
	 */
	public static boolean hasWriteTreePermission(List<IngridDocument> idcPermissionsFromMap) {
		if (idcPermissionsFromMap != null) {
			for (IngridDocument idcPermDoc : idcPermissionsFromMap) {
				IdcPermission idcPerm = EnumUtil.mapDatabaseToEnumConst(IdcPermission.class, idcPermDoc.get(MdekKeysSecurity.IDC_PERMISSION));
				if (idcPerm == IdcPermission.WRITE_TREE) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Checks whether permission list contains permission to manipulate single entity (NO tree)
	 * @param idcPermissionsFromMap permission list delivered from backend
	 * @return true=has permission
	 */
	public static boolean hasWriteSinglePermission(List<IngridDocument> idcPermissionsFromMap) {
		if (idcPermissionsFromMap != null) {
			for (IngridDocument idcPermDoc : idcPermissionsFromMap) {
				IdcPermission idcPerm = EnumUtil.mapDatabaseToEnumConst(IdcPermission.class, idcPermDoc.get(MdekKeysSecurity.IDC_PERMISSION));
				if (idcPerm == IdcPermission.WRITE_SINGLE) {
					return true;
				}
			}			
		}

		return false;
	}
}
