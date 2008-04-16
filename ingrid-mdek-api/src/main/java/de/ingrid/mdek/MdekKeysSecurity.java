package de.ingrid.mdek;

/**
 * Class encapsulating static keys for accessing data in IngridDocument
 * concerning SECURITY / USER MANAGEMENT.
 */
public class MdekKeysSecurity extends MdekKeys {

	// GROUP(S)
    // --------

    /** Value: List of IngridDocs (Group Maps) */
    public final static String GROUPS = "groups";
	
    /** Specifies role<br>
	 *  Value: Integer */
	public final static String IDC_ROLE = "idc-role";
	
	/** Specifies the id of the parent of a user<br>
	 *  Value: Long */
	public final static String PARENT_IDC_USER_ID = "parent-idc-user-id";
	
	/** Specifies the id of a idc group<br>
	 *  Value: Long */
	public final static String IDC_GROUP_ID = "idc-group-id";

	/** Specifies the id of a idc user<br>
	 *  Value: Long */
	public final static String IDC_USER_ID = "idc-user-id";
	
	/** Specifies the address uuid of a idc user<br>
	 *  Value: String */
	public final static String IDC_USER_ADDR_UUID = "idc-user-addr-uuid";

	public static int IDC_ROLE_CATALOG_ADMINISTRATOR = 1;

	public static int IDC_ROLE_METADATA_ADMINISTRATOR = 2;

	public static int IDC_ROLE_METADATA_AUTHOR = 3;
}
