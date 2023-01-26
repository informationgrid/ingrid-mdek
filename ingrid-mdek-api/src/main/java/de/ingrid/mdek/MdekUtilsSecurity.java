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
package de.ingrid.mdek;

import java.util.List;

import de.ingrid.utils.IngridDocument;



/**
 * Class encapsulating utility methods for security.
 * 
 * @author Martin
 */
public class MdekUtilsSecurity {

	public static String GROUP_NAME_ADMINISTRATORS = "administrators";

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
		// Entity Permissions
		WRITE_SINGLE("write", "Einzelberechtigung"),
		WRITE_TREE("write-tree", "Teilbaumberechtigung"),
		WRITE_SUBNODE("write-subnode", "Unterknotenberechtigung"),
		// NOT PERSISTABLE !!! used to tell frontend no write permission on entity, but on subtree ! 
		// (e.g. user not QA but has write-tree permission on entity in state Q)  
		DUMMY_WRITE_SUBTREE("write-subtree", "Unterbaumberechtigung"),

		// User Permissions (bound to group)
		CREATE_ROOT("create-root", "Root anlegen"),
		QUALITY_ASSURANCE("qa", "Qualitätssicherung");

		/**
		 * @param dbValue THIS IS ALSO THE client side STRING USED IN MAP TO IDENTIFY PERMISSION
		 * @param description arbitrary description
		 */
		IdcPermission(String dbValue, String description) {
			this.dbValue = dbValue;
			this.description = description;
		}
		/** THIS IS ALSO THE client side STRING USED IN MAP TO IDENTIFY PERMISSION !!! */
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
		return hasOneOfPermissions(idcPermissionsFromMap, new IdcPermission[]{
				IdcPermission.WRITE_SINGLE,
				IdcPermission.WRITE_TREE});
	}

	/**
	 * Checks whether permission list contains permission to manipulate tree
	 * @param idcPermissionsFromMap permission list delivered from backend
	 * @return true=has permission
	 */
	public static boolean hasWriteTreePermission(List<IngridDocument> idcPermissionsFromMap) {
		return hasOneOfPermissions(idcPermissionsFromMap, new IdcPermission[]{IdcPermission.WRITE_TREE});
	}

	/**
	 * Checks whether permission list contains permission to create a subnode
	 * @param idcPermissionsFromMap permission list delivered from backend
	 * @return true=has permission
	 */
	public static boolean hasWriteSubNodePermission(List<IngridDocument> idcPermissionsFromMap) {
		return hasOneOfPermissions(idcPermissionsFromMap, new IdcPermission[]{IdcPermission.WRITE_SUBNODE});
	}

    /**
     * Checks whether permission list contains permission to move a node.
     * @param idcPermissionsFromMap permission list delivered from backend
     * @return true=has permission
     */
    public static boolean hasMovePermission(List<IngridDocument> idcPermissionsFromMap) {
		return hasOneOfPermissions(idcPermissionsFromMap, new IdcPermission[]{IdcPermission.WRITE_TREE});
    }
	
	/**
	 * Checks whether permission list contains permission to manipulate single entity (NO tree)
	 * @param idcPermissionsFromMap permission list delivered from backend
	 * @return true=has permission
	 */
	public static boolean hasWriteSinglePermission(List<IngridDocument> idcPermissionsFromMap) {
		return hasOneOfPermissions(idcPermissionsFromMap, new IdcPermission[]{IdcPermission.WRITE_SINGLE});
	}
	
	/**
	 * Checks whether permission list contains permission to manipulate a SUB tree (due to root node is QA)
	 * @param idcPermissionsFromMap permission list delivered from backend
	 * @return true=has permission
	 */
	public static boolean hasWriteSubTreePermission(List<IngridDocument> idcPermissionsFromMap) {
		return hasOneOfPermissions(idcPermissionsFromMap, new IdcPermission[]{IdcPermission.DUMMY_WRITE_SUBTREE});
	}
	
	private static boolean hasOneOfPermissions(List<IngridDocument> idcPermissionsFromMap, IdcPermission[] permissionsToHaveOne) {
		if (idcPermissionsFromMap != null) {
			for (IngridDocument idcPermDoc : idcPermissionsFromMap) {
				IdcPermission idcPerm = EnumUtil.mapDatabaseToEnumConst(IdcPermission.class, idcPermDoc.get(MdekKeysSecurity.IDC_PERMISSION));
				for (IdcPermission permToHaveOne : permissionsToHaveOne) {
					if (idcPerm == permToHaveOne) {
						return true;
					}					
				}
			}			
		}

		return false;
	}
}
