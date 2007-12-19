package de.ingrid.mdek;

/**
 * Class encapsulating static keys for accessing data in IngridDocument.
 * 
 * @author Martin
 */
public class MdekKeys {

    /** List of Object Entities<br>
     *	Value: List of IngridDocs */
    public final static String OBJ_ENTITIES = "objEntities";

    /** List of Address Entities<br>
     *	Value: List of IngridDocs */
    public final static String ADR_ENTITIES = "adrEntities";

    /** the uuid of the entity (object or address)<br>
     *	Value: String */
    public final static String UUID = "uuid";

    /** the class of the entity (object or address)<br>
     *	Value: Integer */
    public final static String CLASS = "class";

    /** Value: Boolean */
    public final static String HAS_CHILD = "hasChild";

    /** Value: Integer */
    public final static String TYPE_OF_RELATION = "type-of-relation";

    // OBJECT DATA
    // -----------

    /** Value: String */
    public final static String TITLE = "title";
    /** Value: String */
    public final static String ABSTRACT = "abstract";

    // ADDRESS DATA
    // ------------

    /** Value: String */
    public final static String ORGANISATION = "organisation";
    /** Value: String */
    public final static String NAME = "name";
    /** Value: String */
    public final static String GIVEN_NAME = "given-name";
    /** Value: String */
    public final static String NAME_FORM = "name-form";
    /** Value: String */
    public final static String TITLE_OR_FUNCTION = "title-or-function";
    /** Value: String */
    public final static String STREET = "street";
    /** Value: String */
    public final static String POSTAL_CODE_OF_COUNTRY = "postal-code-of-country";
    /** Value: String */
    public final static String CITY = "city";
    /** Value: String */
    public final static String POST_BOX_POSTAL_CODE = "post-box-postal-code";
    /** Value: String */
    public final static String POST_BOX = "post-box";
    /** Value: String */
    public final static String FUNCTION = "function";
    /** Value: String */
    public final static String ADDRESS_DESCRIPTION = "address-description";

    /** Value: List of IngridDocs */
    public final static String COMMUNICATION = "communication";
    /** Value: String */
    public final static String COMMUNICATION_MEDIUM = "communication-medium";
    /** Value: String */
    public final static String COMMUNICATION_VALUE = "communication-value";
    /** Value: String */
    public final static String COMMUNICATION_DESCRIPTION = "communication-description";


}
