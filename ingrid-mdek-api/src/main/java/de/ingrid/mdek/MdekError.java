/*
 * **************************************************-
 * ingrid-mdek-api
 * ==================================================
 * Copyright (C) 2014 - 2019 wemove digital solutions GmbH
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
		FROM_UUID_EQUALS_TO_UUID("13"),

		/** No catalog data found (e.g. entity catalog association is missing) */
		CATALOG_NOT_FOUND("16"),
		/** e.g. when storing catalog and required data is missing */
		CATALOG_DATA_MISSING("17"),

		/** e.g. publish of child not allowed when parent not published */
		PARENT_NOT_PUBLISHED("20"),
		/** e.g. move with unpublished node not allowed  */
		ENTITY_NOT_PUBLISHED("21"),
		/** e.g. publish object without any address referenced (of any type, see INGRID32-46) */
		REFERENCED_ADDRESS_NOT_SET("25"),
		/** publish object with referenced unpublished addresses */
		REFERENCED_ADDRESSES_NOT_PUBLISHED("27"),

		/** e.g. move of tree node to subnode not allowed */
		TARGET_IS_SUBNODE_OF_SOURCE("30"),
		/** e.g. then no move allowed ! Also thrown if top node has WorkingCopy ! */
		SUBTREE_HAS_WORKING_COPIES("31"),
		/** e.g. when object is published and sub publication conditions don't fit */
		SUBTREE_HAS_LARGER_PUBLICATION_CONDITION("32"),
		/** e.g. when object is published and publication condition doesn't fit to parent */
		PARENT_HAS_SMALLER_PUBLICATION_CONDITION("33"),
		/** e.g. when address is published and its publication condition doesn't fit to objects referencing the address.
		 * contains detailed error info about referenced address and referencing objects:<br>
		 * <b>errorInfo-Map</b> = address map (of address referenced by objects)<br>
		 * <b>errorInfo-Map.OBJ_ENTITIES</b> = List of object maps (referencing objects causing error)
		 */
		REFERENCING_OBJECTS_HAVE_LARGER_PUBLICATION_CONDITION("34"),
		/** e.g. when object is published and its publication condition doesn't fit to referenced addresses.
		 * contains detailed error info about object and referenced addresses:<br>
		 * <b>errorInfo-Map</b> = object map (of object referencing addresses)<br>
		 * <b>errorInfo-Map.ADR_ENTITIES</b> = List of address maps (referenced addresses causing error)
		 */
		REFERENCED_ADDRESSES_HAVE_SMALLER_PUBLICATION_CONDITION("35"),
		/** e.g. when address is replaced (Gesamtkatalogmanagement), not possible if subnodes */
		NODE_HAS_SUBNODES("37"),

		/** when free address is NOT a root node */
		FREE_ADDRESS_WITH_PARENT("41"),
		/** when free address has subnodes (e.g. copy of node with subnodes to free address) */
		FREE_ADDRESS_WITH_SUBTREE("42"),
		/** for all type of address type conflicts, e.g. when child type doesn't fit to parent ... */
		ADDRESS_TYPE_CONFLICT("43"),
		/** e.g. when address to delete is an address of an IdcUser */
		ADDRESS_IS_IDCUSER_ADDRESS("45"),
		/** DEPRECATED: not used anymore ! no special handling of VERWALTER, see INGRID32-46 */
		ADDRESS_IS_VERWALTER("46"),
		/** when address is published without email */
		ADDRESS_HAS_NO_EMAIL("47"),

		/** e.g. when object is deleted and is referenced by other objects<br>
		 * contains detailed error info about referenced and referencing entities:<br>
		 * <b>errorInfo-Map</b> = referenced entity (object or address)<br>
		 * <b>errorInfo-Map.OBJ_ENTITIES</b> = List of object maps (referencing objects)
		 */
		ENTITY_REFERENCED_BY_OBJ("51"),

		/** entered HQL not valid for "Datenbanksuche" */
		HQL_NOT_VALID("61"),
		/** problems writing csv files */
		CSV_WRITER_PROBLEMS("62"),

		/** List key has to be -1 if "freier Eintrag", never NULL ! */
		LIST_KEY_NULL_NOT_ALLOWED("101"),
		/** List data in record has no key AND no value !  Has to have one of both ! */
		LIST_NO_KEY_NO_VALUE("102"),
		/** geothesaurus spatial references have to have sns topic id ! */
		SNS_SPATIAL_REFERENCE_WITHOUT_TOPIC_ID("103"),
		
		/** the user who called mdek backend could not be loaded via its passed AddrUuid ! */
		CALLING_USER_NOT_FOUND("1000"),
		/** e.g. parent is null and user is NOT catalog admin */
		USER_HAS_NO_VALID_PARENT("1010"),
		/** user has no PERMISSION for executing the operation on an entity (write, create root etc.) */
		USER_HAS_NO_PERMISSION_ON_ENTITY("1011"),
		/** user role doesn't allow the operation (e.g. create new MD_ADMIN as MD_ADMIN ...) */
		USER_HAS_WRONG_ROLE("1012"),
		/** e.g. parent of user to store/create not valid compared to calling user (not subuser of calling user) */
		USER_HIERARCHY_WRONG("1013"),
		/** e.g. user to delete has subusers ! */
		USER_HAS_SUBUSERS("1014"),
		/** e.g. user to delete is catalog admin ! */
		USER_IS_CATALOG_ADMIN("1015"),
		/** user has no PERMISSION concerning WORKFLOW for executing the operation on an entity (e.g. publish only as QA etc.) */
		USER_HAS_NO_WORKFLOW_PERMISSION_ON_ENTITY("1020"),
		
		/** try to remove a group that has still users attached. errorInfo: list of attached users !
		 * ALSO DELIVERS IDC USERS OF GROUP */
		GROUP_HAS_USERS("2021"), 
		/** a user write permission (in group) on an object has been removed although the user is still working on the object!
		 * ALSO DELIVERS USER-ADDRESS AND OBJECT */
		USER_EDITING_OBJECT_PERMISSION_MISSING("2022"),
		/** a user write permission (in group) on an object has been removed although the user is responsible for the object!
		 * ALSO DELIVERS USER-ADDRESS AND OBJECT */
		USER_RESPONSIBLE_FOR_OBJECT_PERMISSION_MISSING("2023"),
		/** a user write permission (in group) on an address has been removed although the user is still working on the address !
		 * ALSO DELIVERS USER-ADDRESS AND ADDRESS */
		USER_EDITING_ADDRESS_PERMISSION_MISSING("2024"),
		/** a user write permission (in group) on an address has been removed although the user is responsible for the address!
		 * ALSO DELIVERS USER-ADDRESS AND ADDRESS */
		USER_RESPONSIBLE_FOR_ADDRESS_PERMISSION_MISSING("2025"),
		/** An object has multiple permissions set (in a group). errorInfo: single object (in list)
		 * ALSO DELIVERS DATA OF OBJECT */
		MULTIPLE_PERMISSIONS_ON_OBJECT("2051"),
		/** "write-tree" object permissions are nested (in a group). errorInfo: two objects, order determines parent/child !
		 * ALSO DELIVERS DATA OF PARENT AND SUB OBJECT */
		TREE_BELOW_TREE_OBJECT_PERMISSION("2052"),
		/** "write" object permission beneath "write-tree" permission (in a group). errorInfo: two objects, order determines parent/child !
		 * ALSO DELIVERS DATA OF PARENT AND SUB OBJECT */
		SINGLE_BELOW_TREE_OBJECT_PERMISSION("2053"),
		/** An address has multiple permissions set (in a group). errorInfo: single address (in list)
		 * ALSO DELIVERS DATA OF ADDRESS */
		MULTIPLE_PERMISSIONS_ON_ADDRESS("2061"),
		/** "write-tree" address permissions are nested (in a group). errorInfo: two addresses, order determines parent/child !
		 * ALSO DELIVERS DATA OF PARENT AND SUB ADDRESS */
		TREE_BELOW_TREE_ADDRESS_PERMISSION("2062"),
		/** "write" address permission beneath "write-tree" permission (in a group). errorInfo: two addresses, order determines parent/child !
		 * ALSO DELIVERS DATA OF PARENT AND SUB ADDRESS */
		SINGLE_BELOW_TREE_ADDRESS_PERMISSION("2063"),
		/** user cannot remove address permission when not having write access on address */
		NO_RIGHT_TO_REMOVE_OBJECT_PERMISSION("2071"),
		/** user cannot remove object permission when not having write access on object */
		NO_RIGHT_TO_REMOVE_ADDRESS_PERMISSION("2072"),
		/** user cannot remove user permission when not having same user permission */
		NO_RIGHT_TO_REMOVE_USER_PERMISSION("2073"),
		/** user cannot add address permission when not having write access on address */
		NO_RIGHT_TO_ADD_OBJECT_PERMISSION("2074"),
		/** user cannot add object permission when not having write access on object */
		NO_RIGHT_TO_ADD_ADDRESS_PERMISSION("2075"),
		/** user cannot add user permission when not having same user permission */
		NO_RIGHT_TO_ADD_USER_PERMISSION("2076"),

		/** a problem during import occured. delivers message describing problem. */
		IMPORT_PROBLEM("4000"),
		/** Objects to import already exist in catalog and COPY IS NOT ALLOWED.<br>
		 * errorInfo: existing objects (in list), ALSO DELIVERS DATA OF OBJECTS */
		IMPORT_OBJECTS_ALREADY_EXIST("4010"),
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
	/** Various additional info. NOTICE: can be set up via methods in Services
	 * e.g. MdekAddressService.setupErrorInfoAddr(...) 
	 */
	protected IngridDocument errorInfo;
	protected String errorMessage;

    private MdekError() {}

	/** Constructs an error of the given type. */
    public MdekError(MdekErrorType errorType) {
    	this(errorType, null, null);
    }
    /** Constructs an error of the given type containing detailed error information.
     * @param errorType defined Error type
     * @param errorInfo defined additional Info. NOTICE: can be set up via methods in Services
	 * 			e.g. MdekAddressService.setupErrorInfoAddr(...)
     */
    public MdekError(MdekErrorType errorType, IngridDocument errorInfo) {
    	this(errorType, null, errorInfo);
    }
    /** Constructs an error of the given type containing additional error message.
     * @param errorType defined Error type
     * @param errorMessage arbitrary additional message
     */
    public MdekError(MdekErrorType errorType, String errorMessage) {
    	this(errorType, errorMessage, null);
    }
    /** Constructs an error of the given type containing additional error message and detailed information.
     * @param errorType defined Error type
     * @param errorMessage arbitrary additional message
     * @param errorInfo defined additional Info. NOTICE: can be set up via methods in Services
	 * 			e.g. MdekAddressService.setupErrorInfoAddr(...)
     */
    public MdekError(MdekErrorType errorType, String errorMessage, IngridDocument errorInfo) {
    	this.errorType = errorType;
    	this.errorMessage = errorMessage;
    	this.errorInfo = errorInfo;
    }

	public MdekErrorType getErrorType() {
		return errorType;
	}
	public IngridDocument getErrorInfo() {
		return errorInfo;
	}
	public String getErrorMessage() {
		return errorMessage;
	}

	public String toString() {
		String retStr = "[";
		retStr += errorType;
		if (errorMessage != null) {
			retStr += ", " + errorMessage;
		}
		if (errorInfo != null) {
			retStr += ", " + errorInfo;
		}
		retStr += "]";

		return retStr;
	}
}
