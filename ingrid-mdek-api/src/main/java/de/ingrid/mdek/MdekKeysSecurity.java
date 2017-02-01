/*
 * **************************************************-
 * ingrid-mdek-api
 * ==================================================
 * Copyright (C) 2014 - 2017 wemove digital solutions GmbH
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

/**
 * Class encapsulating static keys for accessing data in IngridDocument
 * concerning SECURITY / USER MANAGEMENT.
 */
public class MdekKeysSecurity extends MdekKeys {

	// GROUP(S)
    // --------

    /** also include group of Catalog Admin, e.g. when fetching all groups<br>
     *  Value: Boolean */
    public final static String REQUESTINFO_INCLUDE_CATADMIN_GROUP = "requestinfo_includeCatAdminGroup";

    /** Value: List of IngridDocs (Group Maps) */
    public final static String GROUPS = "groups";
    /** Value: Long[] -> ids of groups connected with */
	public final static String IDC_GROUP_IDS = "idc-group-ids";
    /** delete group also when users exist ? if false and user exist causes error<br>
     *  Value: Boolean */
    public final static String REQUESTINFO_FORCE_DELETE_GROUP_WHEN_USERS = "requestinfo_forceDeleteGroupWhenUsers";


	// USER(S)
    // -------

    /** fetch detailed permissions of the users requested (write-tree, qa, ...) ?<br>
     *  Value: Boolean */
    public final static String REQUESTINFO_GET_DETAILED_PERMISSIONS = "requestinfo_getDetailedPermissions";

    /** Value: List of IngridDocs (IdcUser Maps) */
    public final static String IDC_USERS = "idc-users";
    /** Value: List of IngridDocs (Address Maps) */
    public final static String USER_ADDRESSES = "user-addresses";
    /** Specifies role<br>
	 *  Value: Integer */
	public final static String IDC_ROLE = "idc-role";
	/** Specifies the id of the parent of a user<br>
	 *  Value: Long */
	public final static String PARENT_IDC_USER_ID = "parent-idc-user-id";
	/** Specifies the id of a idc user<br>
	 *  Value: Long */
	public final static String IDC_USER_ID = "idc-user-id";
	/** Specifies the address uuid of a idc user<br>
	 *  Value: String */
	public final static String IDC_USER_ADDR_UUID = "idc-user-addr-uuid";

	// PERMISSION(S)
    // -------------

    /** Value: List of IngridDocs (containing permission) */
    public final static String IDC_PERMISSIONS = "idc-permissions";
    /** Value: List of IngridDocs (containing permission) */
    public final static String IDC_USER_PERMISSIONS = "idc-user-permissions";
    /** Value: List of IngridDocs (containing address uuid and permission) */
    public final static String IDC_ADDRESS_PERMISSIONS = "idc-address-permissions";
    /** Value: List of IngridDocs (containing object uuid and permission) */
    public final static String IDC_OBJECT_PERMISSIONS = "idc-object-permissions";
    /** Specifies a permission via client side permission id, e.g. "write-tree" (defined in Enumeration, see "Utils")<br>
	 *  Value: String */
	public final static String IDC_PERMISSION = "idc-permission";
}
