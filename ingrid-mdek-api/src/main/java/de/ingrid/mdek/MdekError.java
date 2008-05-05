package de.ingrid.mdek;

import java.io.Serializable;

import de.ingrid.utils.IngridDocument;

/**
 * Class describing a known mdek error. 
 * 
 * @author Martin
 */
public class MdekError implements Serializable {

	/** Error Codes ! */
	public enum MdekErrorType implements IMdekEnum {
		/** Another user changed an entity in between */
		ENTITY_CHANGED_IN_BETWEEN("1"),
		/** e.g. unique entity should be created but already exists (e.g. group with a given name) */
		ENTITY_ALREADY_EXISTS("2"),
		/** generic error if entity wasn't found (e.g. security group etc.) */
		ENTITY_NOT_FOUND("3"),

		/** No user set in request ! */
		USER_ID_NOT_SET("5"),
		/** There are still running jobs of user */
		USER_HAS_RUNNING_JOBS("6"),
		/** User canceled job */
		USER_CANCELED_JOB("7"),

		UUID_NOT_FOUND("10"),
		FROM_UUID_NOT_FOUND("11"),
		TO_UUID_NOT_FOUND("12"),
		/** No catalog data found (e.g. entity catalog association is missing) */
		CATALOG_NOT_FOUND("13"),

		/** e.g. publish of child not allowed when parent not published */
		PARENT_NOT_PUBLISHED("20"),
		/** e.g. move with unpublished node not allowed  */
		ENTITY_NOT_PUBLISHED("21"),

		/** e.g. move of tree node to subnode not allowed */
		TARGET_IS_SUBNODE_OF_SOURCE("30"),
		/** e.g. then no move allowed ! Also thrown if top node has WorkingCopy ! */
		SUBTREE_HAS_WORKING_COPIES("31"),
		/** e.g. when object is published and sub publication conditions don't fit */
		SUBTREE_HAS_LARGER_PUBLICATION_CONDITION("32"),
		/** e.g. when object is published and publication condition doesn't fit to parent */
		PARENT_HAS_SMALLER_PUBLICATION_CONDITION("33"),

		/** when free address is NOT a root node */
		FREE_ADDRESS_WITH_PARENT("41"),
		/** when free address has subnodes (e.g. copy of node with subnodes to free address) */
		FREE_ADDRESS_WITH_SUBTREE("42"),
		/** for all type of address type conflicts, e.g. when child type doesn't fit to parent ... */
		ADDRESS_TYPE_CONFLICT("43"),

		/** e.g. when object is deleted and and is referenced by other objects<br>
		 * contains detailed error info about referenced and referencing entities:<br>
		 * <b>errorInfo-Map</b> = referenced entity (object or address)<br>
		 * <b>errorInfo-Map.OBJ_ENTITIES</b> = List of object maps (referencing objects)
		 */
		ENTITY_REFERENCED_BY_OBJ("51"),

		/** entered HQL not valid for "Datenbanksuche" */
		HQL_NOT_VALID("61"),

		/** List key has to be -1 if "freier Eintrag", never NULL ! */
		LIST_KEY_NULL_NOT_ALLOWED("101"),
		/** List data in record has no key AND no value !  Has to have one of both ! */
		LIST_NO_KEY_NO_VALUE("102"),
		
		/** try to store a user that has not the catalog admin role without a valid parent*/
		USER_HAS_NO_VALID_PARENT("1000"),
		/** user has no permission for executing the operation */
		USER_HAS_NO_PERMISSION("1001"),
		
		/** try to remove a group that has still users attached */
		GROUP_HAS_USERS("1021"), 
		/** try to remove a group that has still permissions (address, object) attached */
		GROUP_HAS_PERMISSIONS("1022"),

		/** An object has multiple permissions set (in a group). errorInfo: object (in list) */
		MULTIPLE_PERMISSIONS_ON_OBJECT("1051"),
		/** "write-tree" object permissions are nested (in a group). errorInfo: order of objects determines parent/child ! */
		TREE_BELOW_TREE_OBJECT_PERMISSION("1052"),
		/** "write" object permission beneath "write-tree" permission (in a group). errorInfo: order of objects determines parent/child ! */
		SINGLE_BELOW_TREE_OBJECT_PERMISSION("1053"),
		/** An address has multiple permissions set (in a group). errorInfo: address (in list) */
		MULTIPLE_PERMISSIONS_ON_ADDRESS("1061"),
		/** "write-tree" address permissions are nested (in a group). errorInfo: order of address determines parent/child ! */
		TREE_BELOW_TREE_ADDRESS_PERMISSION("1062"),
		/** "write" address permission beneath "write-tree" permission (in a group). errorInfo: order of address determines parent/child ! */
		SINGLE_BELOW_TREE_ADDRESS_PERMISSION("1063")
		;

		MdekErrorType(String errorCode) {
			this.errorCode = errorCode;
		}
		/** represents the error code of this enumeration constant.
		 * @see de.ingrid.mdek.IMdekEnum#getDbValue()
		 */
		public String getDbValue() {
			return errorCode;
		}
		String errorCode;
	}

	protected MdekErrorType errorType;
	protected IngridDocument errorInfo;

    private MdekError() {}

	/** Constructs an exception containing the passed error. */
    public MdekError(MdekErrorType errorType) {
    	this.errorType = errorType;
    }
	/** Constructs an exception containing the passed error and error information. */
    public MdekError(MdekErrorType errorType, IngridDocument errorInfo) {
    	this.errorType = errorType;
    	this.errorInfo = errorInfo;
    }

	public MdekErrorType getErrorType() {
		return errorType;
	}
	public IngridDocument getErrorInfo() {
		return errorInfo;
	}

	public String toString() {
		String retStr = "[";
		retStr += errorType;
		retStr += ", ";
		retStr += errorInfo;		
		retStr += "]";

		return retStr;
	}
}
