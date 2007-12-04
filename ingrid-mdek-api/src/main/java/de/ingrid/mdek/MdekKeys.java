package de.ingrid.mdek;

/**
 * Class encapsulating static keys for accessing data in IngridDocument.
 * 
 * @author Martin
 */
public class MdekKeys {

    /** List of Object Entities<br>
     *	<b>Datatype: List of IngridDocs</b> */
    public final static String OBJ_ENTITIES = "objEntities";

    /** List of Address Entities<br>
     *	<b>Datatype: List of IngridDocs</b> */
    public final static String ADR_ENTITIES = "adrEntities";

    /** the uuid of the entity (object or address)<br>
     *	<b>Datatype: String</b> */
    public final static String UUID = "uuid";

    /** the class of the entity (object or address)<br>
     *	<b>Datatype: Integer</b> */
    public final static String CLASS = "class";

    /** <b>Datatype: Boolean</b> */
    public final static String HAS_CHILD = "hasChild";

    // OBJECT DATA
    // -----------

    /** <b>Datatype: String</b> */
    public final static String TITLE = "title";
    /** <b>Datatype: String</b> */
    public final static String ABSTRACT = "abstract";

    // ADDRESS DATA
    // ------------

    /** <b>Datatype: String</b> */
    public final static String ORGANISATION = "organisation";
    /** <b>Datatype: String</b> */
    public final static String NAME = "name";
    /** <b>Datatype: String</b> */
    public final static String GIVEN_NAME = "given-name";
    /** <b>Datatype: String</b> */
    public final static String NAME_FORM = "name-form";
    /** <b>Datatype: String</b> */
    public final static String TITLE_OR_FUNCTION = "title-or-function";
    /** <b>Datatype: String</b> */
    public final static String STREET = "street";
    /** <b>Datatype: String</b> */
    public final static String POSTAL_CODE_OF_COUNTRY = "postal-code-of-country";
    /** <b>Datatype: String</b> */
    public final static String CITY = "city";
    /** <b>Datatype: String</b> */
    public final static String POST_BOX_POSTAL_CODE = "post-box-postal-code";
    /** <b>Datatype: String</b> */
    public final static String POST_BOX = "post-box";
    /** <b>Datatype: String</b> */
    public final static String FUNCTION = "function";
    /** <b>Datatype: String</b> */
    public final static String ADDRESS_DESCRIPTION = "address-description";
}
