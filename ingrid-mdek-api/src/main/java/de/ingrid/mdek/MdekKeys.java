package de.ingrid.mdek;

/**
 * Class encapsulating static keys for accessing data in IngridDocument.
 * 
 * @author Martin
 */
public class MdekKeys {

    /** List of Object Entities<br>
     *	<b>Datatype: List of IngridDocs</b> 
     */
    public final static String OBJ_ENTITIES = "objEntities";

    /** List of Address Entities<br>
     *	<b>Datatype: List of IngridDocs</b> 
     */
    public final static String ADR_ENTITIES = "adrEntities";

    /** the uuid of the entity (object or address)<br>
     *	<b>Datatype: String</b> 
     */
    public final static String ENTITY_UUID = "entityUuid";

    /** the type of an entity (object or address)<br>
     *	<b>Datatype: String</b> 
     */
    public final static String ENTITY_TYPE = "entityType";

    /** Name of the Entity<br>
     *	<b>Datatype: String</b> 
     */
    public final static String ENTITY_NAME = "entityName";

    /** Description of the Entity<br>
     *	<b>Datatype: String</b> 
     */
    public final static String ENTITY_DESCRIPTION = "entityDescr";

    /** e.g. the depth of a tree to fetch etc.<br>
     *	<b>Datatype: Integer</b> 
     */
    public final static String DEPTH = "depth";
}
