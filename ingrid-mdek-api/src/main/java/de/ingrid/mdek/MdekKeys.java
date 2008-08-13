package de.ingrid.mdek;

/**
 * Class encapsulating static keys for accessing data in IngridDocument.
 * 
 * @author Martin
 */
public class MdekKeys {

    // ADDITIONAL INFO IN REQUEST
    // -------------------------
    /** Specifies current user for tracking running jobs<br>
     *  Value: String */
    public final static String USER_ID = "user-id";

    /** Refetch an entity after an operation was performed ?<br>
     *  Value: Boolean */
    public final static String REQUESTINFO_REFETCH_ENTITY = "requestinfo_refetchEntity";
    /** Also copy subtree when an entity is copied ?<br>
     *  Value: Boolean */
    public final static String REQUESTINFO_COPY_SUBTREE = "requestinfo_copySubtree";
    /** when copying or moving address indicates whether copy/move is to a "free address" ?<br>
     *  Value: Boolean */
    public final static String REQUESTINFO_TARGET_IS_FREE_ADDRESS = "requestinfo_targetIsFreeAddress";
    /** Perform check before executing requested action ? (e.g. when moving node check
     * whether subnodes don't have working copy)<br>
     *  Value: Boolean */
    public final static String REQUESTINFO_PERFORM_CHECK = "requestinfo_performCheck";
    /** Apply publication condition to subnodes ?<br>
     *  Value: Boolean */
    public final static String REQUESTINFO_FORCE_PUBLICATION_CONDITION = "requestinfo_forcePublicationCondition";
    /** Fetch only free addresses or only NOT free addresses ?<br>
     *  Value: Boolean */
    public final static String REQUESTINFO_ONLY_FREE_ADDRESSES = "requestinfo_onlyFreeAddresses";
    /** if entity is deleted also delete references ? if false and references exist causes error<br>
     *  Value: Boolean */
    public final static String REQUESTINFO_FORCE_DELETE_REFERENCES = "requestinfo_forceDeleteReferences";

    /** Value: String */
    public final static String LANGUAGE = "language";

    // SEARCH / QUERY (Recherche)
    // --------------------------
    // REQUEST
    /** hit to start with (first hit == 0)<br>
     * Value: Integer */
    public final static String SEARCH_START_HIT = "search-start-hit";
    /** number of hits requested starting at start hit<br>
     * Value: Integer */
    public final static String SEARCH_NUM_HITS = "search-num-hits";
    /** Value: IngridDocument encapsulating all search parameters in REQUEST */
    public final static String SEARCH_PARAMS = "search-params";
    /** Value: IngridDocument encapsulating all search parameters for extended search in REQUEST */
    public final static String SEARCH_EXT_PARAMS = "search-ext-params";
    /** Value: String */
    public final static String SEARCH_TERM = "search_term";
    /** HQL query string for querying objects/addresses<br>
     * Value: String */
    public final static String HQL_QUERY = "hql-query";
    
    /** term for extended search querying objects/addresses<br>
     * Value: String */
	public static final String QUERY_TERM = "query-term";
    /** relation (and = 0, or = 1) for terms for extended search querying objects/addresses<br>
     * Value: Integer */
	public static final String RELATION = "relation";
	/** object classes for extended search querying objects<br>
	/** Value: List of Integer */
	public static final String OBJ_CLASSES = "obj-classes";
    /** thesaurus for extended search querying objects (only the term-sns-id will be supplied)<br>
	/** Value: List of IngridDocuments */
	public static final Object THESAURUS_TERMS = "thesaurus-terms";
    /** relation (and = 0, or = 1) for thesaurus term relation for extended search querying objects<br>
     * Value: Integer */
	public static final String THESAURUS_RELATION = "thesaurus-relation";
    /** geo thesaurus for extended search querying objects (only the term-sns-id will be supplied)<br>
	/** Value: List of IngridDocuments */
	public static final Object GEO_THESAURUS_TERMS = "geo-thesaurus-terms";
    /** relation (and = 0, or = 1) for geo thesaurus term relation for extended search querying objects<br>
     * Value: Integer */
	public static final String GEO_THESAURUS_RELATION = "geo-thesaurus-relation";
    /** free spatial reference for extended search querying objects<br>
	/** Value: Integer */
	public static final Object CUSTOM_LOCATION = "custom-location";
    /** temporal reference for extended search querying objects<br>
	/** Value: String */
	public static final String TIME_FROM = "time-from";
    /** temporal reference for extended search querying objects<br>
	/** Value: String */
	public static final String TIME_TO = "time-to";
    /** temporal reference for extended search querying objects<br>
	/** Value: String */
	public static final String TIME_AT = "time-at";
    /** temporal reference for extended search querying objects<br>
	/** Value: Boolean */
	public static final String TIME_INTERSECT = "time-intersect";
    /** temporal reference for extended search querying objects<br>
	/** Value: Boolean */
	public static final String TIME_CONTAINS = "time-contains";
    /** address: extended search: subject: search mode ('whole word'(0), 'partial word'(1))<br>
     * Value: Integer */
	public static final String SEARCH_TYPE = "search-type";
    /** address: extended search: subject: search range ('all text fields'(0), 'institutions, person, description, keywords'(1))<br>
     * Value: Integer */
	public static final String SEARCH_RANGE = "search-range";
    

    // RESULT
    /** total number of hits<br>
     * Value: Long */
    public final static String SEARCH_TOTAL_NUM_HITS = "search-total-num-hits";
    /** Value: String (csv lines, first line contains "titles") */
    public final static String CSV_RESULT = "csv-result";


    // VERSIONING (read from properties file)
    // ----------
    /** Value: String */
    public final static String API_BUILD_NAME = "api.build.name";
    /** Value: String */
    public final static String API_BUILD_VERSION = "api.build.version";
    /** Value: String */
    public final static String API_BUILD_NUMBER = "api.build.number";
    /** Value: String */
    public final static String API_BUILD_TIMESTAMP = "api.build.timestamp";
    /** Value: String */
    public final static String SERVER_BUILD_NAME = "server.build.name";
    /** Value: String */
    public final static String SERVER_BUILD_VERSION = "server.build.version";
    /** Value: String */
    public final static String SERVER_BUILD_NUMBER = "server.build.number";
    /** Value: String */
    public final static String SERVER_BUILD_TIMESTAMP = "server.build.timestamp";

    // SYS LISTS
    // ---------

    /** Value: Integer[] -> all ids of syslists to fetch */
    public final static String SYS_LIST_IDS = "sys-list-ids";
    /** Prefix for all sys-lists in result, e.g. sys-list-1100 */
    public final static String SYS_LIST_KEY_PREFIX = "sys-list-";
    /** Value: Integer */
    public final static String LST_ID = "lst-id";
    /** Value: List of IngridDocs */
    public final static String LST_ENTRY_LIST = "lst-entry-list";
    /** Value: Integer */
    public final static String ENTRY_ID = "entry-id";
    /** Value: String */
    public final static String ENTRY_NAME = "entry-name";
    /** Value: String ("Y"/"N")*/
    public final static String IS_DEFAULT = "is-default";

    // Top Result Lists
    // ----------------

    /** Value: List of IngridDocs */
    public final static String OBJ_ENTITIES = "objEntities";
    /** Value: List of IngridDocs */
    public final static String ADR_ENTITIES = "adrEntities";
    /** When fetching object references via paging, start with this object (first object is index 0)<br>
     * Value: Integer */
    public final static String OBJ_REFERENCES_FROM_START_INDEX = "objReferencesFrom_startIndex";
    /** When fetching object references via paging, max number of objects to fetch<br>
     * Value: Integer */
    public final static String OBJ_REFERENCES_FROM_MAX_NUM = "objReferencesFrom_maxNum";
    /** When fetching object references via paging, total number of objects referencing address<br> 
     * Value: Integer */
    public final static String OBJ_REFERENCES_FROM_TOTAL_NUM = "objReferencesFrom_totalNum";
    /** Object references from working version<br>
     * Value: List of IngridDocs */
    public final static String OBJ_REFERENCES_FROM = "objReferencesFrom";
    /** Object references only from published version (deleted in work version !)<br>
     * Value: List of IngridDocs */
    public final static String OBJ_REFERENCES_FROM_PUBLISHED_ONLY = "objReferencesFromPublishedOnly";
    /** Value: List of IngridDocs */
    public final static String OBJ_REFERENCES_TO = "objReferencesTo";
    /** Value: List of IngridDocs */
    public final static String ADR_REFERENCES_TO = "adrReferencesTo";
    /** Path in tree to entity containing uuids<br>
     * Value: List of Strings (uuids) */
    public final static String PATH = "path";
    /** Path in tree to entity containing organizations etc.<br>
     * Value: List of IngridDocs containing organization/adrType */
    public final static String PATH_ORGANISATIONS = "path_organisations";

    // ADDITIONAL INFO IN RESULT
    // -------------------------
    /** indicates whether a deleted entity was fully deleted (e.g. delete working copy without published version) 
     *  Value: Boolean */
    public final static String RESULTINFO_WAS_FULLY_DELETED = "resultinfo_wasFullyDeleted";
    /** indicates whether a tree has subnodes with working copies, e.g. to determin whether move is allowed 
     *  Value: Boolean */
    public final static String RESULTINFO_HAS_WORKING_COPY = "resultinfo_hasWorkingCopy";
    /** How many entities were processed (e.g. when checking subtree state, copying tree etc.) 
     *  Value: Integer */
    public final static String RESULTINFO_NUMBER_OF_PROCESSED_ENTITIES = "resultinfo_numberOfProcessedEntities";
    /** uuid of the entity which was found (e.g. when looking for working copy in subtree) 
     *  Value: String */
    public final static String RESULTINFO_UUID_OF_FOUND_ENTITY = "resultinfo_uuidOfFoundEntity";
    
    // INFO ABOUT RUNNING JOBS
    // -----------------------
    /** Plain text description of job which is still running on server<br>
     *  Value: String */
    public final static String RUNNINGJOB_DESCRIPTION = "runningjob-description";
    /** Additional info: How many entities already processed ?<br>
     *  Value: Integer */
    public final static String RUNNINGJOB_NUMBER_PROCESSED_ENTITIES = "runningjob-number-processed-entities";
    /** Additional info: Total number of entities to process<br>
     *  Value: Integer */
    public final static String RUNNINGJOB_NUMBER_TOTAL_ENTITIES = "runningjob-number-total-entities";
    /** Additional info: Was the currently running job canceled by user<br>
     *  Value: Boolean */
    public final static String RUNNINGJOB_CANCELED_BY_USER = "runningjob-canceled-by-user";

    // Generic
    // -------
    /** Value: Long */
    public final static String ID = "id";
    /** the uuid of the entity (object or address)<br>
     *	Value: String */
    public final static String UUID = "uuid";
    /** the uuid of the parent (object or address)<br>
     *	Value: String */
    public final static String PARENT_UUID = "parent-uuid";
    /** Value: String */
    public final static String FROM_UUID = "from-uuid";
    /** Value: String */
    public final static String TO_UUID = "to-uuid";
    /** the class of the entity (object or address)<br>
     *	Value: Integer */
    public final static String CLASS = "class";
    /** Value: Boolean */
    public final static String HAS_CHILD = "hasChild";
    /** Value: Boolean */
    public final static String IS_PUBLISHED = "isPublished";
    /** database column "type"<br>
     *  Value: Integer */
    public final static String RELATION_TYPE_ID = "relation-type-id";
    /** database column "special_name"<br> 
     * Value: String */
    public final static String RELATION_TYPE_NAME = "relation-type-name";
    /** database column "special_ref"<br>
     *  Value: Integer */
    public final static String RELATION_TYPE_REF = "relation-type-ref";
    /** Value: String */
    public final static String DATE_OF_CREATION = "date-of-creation";
    /** Value: String */
    public final static String DATE_OF_LAST_MODIFICATION = "date-of-last-modification";
    /** Value: String */
    public final static String WORK_STATE = "work-state";

    /** Infos about the parent of an object/address 
     * Value: IngridDoc */
    public final static String PARENT_INFO = "parent-info";
    
    // OBJECT DATA
    // -----------

    /** Value: String */
    public final static String TITLE = "title";
    /** Value: String */
    public final static String ABSTRACT = "abstract";
    /** description of obj-obj association<br>
     * Value: String */
    public final static String RELATION_DESCRIPTION = "relation-description";
    /** Value: String */
    public final static String DATASET_ALTERNATE_NAME = "dataset-alternate-name";    

    // GEO

    /** Value: Double */
    public final static String VERTICAL_EXTENT_MINIMUM = "vertical-extent-minimum";
    /** Value: Double */
    public final static String VERTICAL_EXTENT_MAXIMUM = "vertical-extent-maximum";
    /** Value: Integer */
    public final static String VERTICAL_EXTENT_UNIT = "vertical-extent-unit";
    /** Value: Integer */
    public final static String VERTICAL_EXTENT_VDATUM = "vertical-extent-vdatum";
    /** Value: String */
    public final static String DESCRIPTION_OF_SPATIAL_DOMAIN = "description-of-spatial-domain";

    // SPATIAL REFERENCES

    /** Value: List of IngridDocs */
    public final static String LOCATIONS = "locations";
    /** Value: String */
    public final static String LOCATION_NAME = "location-name";
    /** Value: String */
    public final static String LOCATION_NAME_KEY = "location-name-key";
    /** Value: String */
    public final static String LOCATION_TYPE = "location-type";
    /** Value: String */
    public final static String LOCATION_CODE = "location-code";
    /** Value: String */
    public final static String LOCATION_SNS_ID = "location-sns-id";
    /** Value: Double */
    public final static String WEST_BOUNDING_COORDINATE = "west-bounding-coordinate";
    /** Value: Double */
    public final static String SOUTH_BOUNDING_COORDINATE = "south-bounding-coordinate";
    /** Value: Double */
    public final static String EAST_BOUNDING_COORDINATE = "east-bounding-coordinate";
    /** Value: Double */
    public final static String NORTH_BOUNDING_COORDINATE = "north-bounding-coordinate";
    /** Value: String */
    public final static String SNS_TOPIC_TYPE = "sns-topic-type";

    
    // SEARCHTERMS

    /** Value: List of IngridDocs */
    public final static String SUBJECT_TERMS = "subject-terms";
    /** Value: String */
    public final static String TERM_NAME = "term-name";
    /** Value: String */
    public final static String TERM_TYPE = "term-type";
    /** Value: String */
    public final static String TERM_SNS_ID = "term-sns-id";

    // TIME

    /** Value: String (Auswahllisten Eintrag, nicht ID) */
    public final static String TIME_TYPE = "time-type";
    /** Value: String */
    public final static String BEGINNING_DATE = "beginning-date";
    /** Value: String */
    public final static String ENDING_DATE = "ending-date";
    /** Value: Integer */
    public final static String TIME_STATUS = "time-status";
    /** Value: Integer */
    public final static String TIME_PERIOD = "time-period";
    /** Value: String */
    public final static String TIME_STEP = "time-step";
    /** Value: String */
    public final static String TIME_SCALE = "time-scale";
    /** Value: String */
    public final static String DESCRIPTION_OF_TEMPORAL_DOMAIN = "description-of-temporal-domain";
    /** Value: String */
    public final static String METADATA_LANGUAGE = "metadata-language";
    /** Value: String */
    public final static String DATA_LANGUAGE = "data-language";
    /** Value: Integer */
    public final static String PUBLICATION_CONDITION = "publication-condition";
    /** Value: String */
    public final static String DATASET_INTENSIONS = "dataset-intensions";
    /** Value: String */
    public final static String DATASET_USAGE = "dataset-usage";
    /** Value: String */
    public final static String ORDERING_INSTRUCTIONS = "ordering-instructions";
    /** Value: String */
    public final static String IS_CATALOG_DATA = "is-catalog-data";

    // DATA REFERENCE
    /** Value: List of IngridDocs */
    public final static String DATASET_REFERENCES = "dataset-references";
    /** Value: String (timestamp) */
    public final static String DATASET_REFERENCE_DATE = "dataset-reference-date";
    /** Value: Integer */
    public final static String DATASET_REFERENCE_TYPE = "dataset-reference-type";

    // EXPORTS
    /** Value: List of IngridDocs */
    public final static String EXPORTS = "exports";

    /** Value: Integer  */
    public final static String EXPORT_KEY = "export-key";
    /** Value: Integer  */
    public final static String EXPORT_VALUE = "export-value";
    
    // TECHNICAL DOMAIN MAP
    /** Value: IngridDocument encapsulating all MAP data */
    public final static String TECHNICAL_DOMAIN_MAP = "technical-domain-map";
    
    /** Value: String  */
    public final static String TECHNICAL_BASE = "technical-base";
    /** Value: String  */
    public final static String DATA = "data";
    /** Value: String  */
    public final static String METHOD_OF_PRODUCTION = "method-of-production";
    /** Value: String  */
    public final static String COORDINATE_SYSTEM = "coordinate-system";
    /** Value: Double  */
    public final static String RESOLUTION = "resolution";
    /** Value: Double  */
    public final static String DEGREE_OF_RECORD = "degree-of-record";
    /** Value: Integer  */
    public final static String HIERARCHY_LEVEL = "hierarchy-level";
    /** Value: Integer  */
    public final static String VECTOR_TOPOLOGY_LEVEL = "vector-topology-level";
    /** Value: Integer  */
    public final static String REFERENCESYSTEM_ID = "referencesystem-id";
    /** Value: Double  */
    public final static String POS_ACCURACY_VERTICAL = "pos-accuracy-vertical";
    /** Value: Integer  */
    public final static String KEYC_INCL_W_DATASET = "keyc-incl-w-dataset";
    /** Value: String  */
    public final static String DATASOURCE_UUID = "datasource-uuid";
    
    // TECHNICAL DOMAIN MAP - KEY CATALOG
    /** Value: List of IngridDocs  */
    public final static String KEY_CATALOG_LIST = "key-catalog-list";
    /** Value: String  */
    public final static String SUBJECT_CAT = "subject-cat";
    /** Value: Integer  */
    public final static String SUBJECT_CAT_KEY = "subject-cat-key";
    /** Value: String  */
    public final static String KEY_DATE = "key-date";
    /** Value: String  */
    public final static String EDITION = "edition";

    // TECHNICAL DOMAIN MAP - PUBLICATION SCALE
    /** Value: List of IngridDocs  */
    public final static String PUBLICATION_SCALE_LIST = "publication-scale-list";
    /** Value: Integer  */
    public final static String SCALE = "scale";
    /** Value: Double  */
    public final static String RESOLUTION_GROUND = "resolution-ground";
    /** Value: Double  */
    public final static String RESOLUTION_SCAN = "resolution-scan";
    
    // TECHNICAL DOMAIN MAP - SYMBOL CATALOG
    /** Value: List of IngridDocs  */
    public final static String SYMBOL_CATALOG_LIST = "symbol-catalog-list";
    /** Value: String  */
    public final static String SYMBOL_CAT = "symbol-cat";
    /** Value: Integer */
    public final static String SYMBOL_CAT_KEY = "symbol-cat-key";
    /** Value: String  */
    public final static String SYMBOL_DATE = "symbol-date";
    /** Value: String  */
    public final static String SYMBOL_EDITION = "symbol-edition";
    
    // TECHNICAL DOMAIN MAP - FEATURE TYPE
    /** Value: List of Strings  */
    public final static String FEATURE_TYPE_LIST = "feature-type-list";

    // TECHNICAL DOMAIN MAP - VECTOR FORMAT GEO VECTOR
    /** Value: List of IngridDocs  */
    public final static String GEO_VECTOR_LIST = "geo-vector-list";
    /** Value: Integer  */
    public final static String GEOMETRIC_OBJECT_TYPE = "geometric-object-type";
    /** Value: Integer  */
    public final static String GEOMETRIC_OBJECT_COUNT = "geometric-object-count";

    // TECHNICAL DOMAIN MAP - SPATIAL REPRESENTATION TYPE
    /** Value: List of Integer  */
    public final static String SPATIAL_REPRESENTATION_TYPE_LIST = "spatial-representation-type-list";
    
    // TECHNICAL DOMAIN DOCUMENT (LITERATURE)
    /** Value: IngridDocument encapsulating all LITERATURE data */
    public final static String TECHNICAL_DOMAIN_DOCUMENT = "technical-domain-document";
    /** Value: String  */
    public final static String AUTHOR = "author";
    /** Value: String  */
    public final static String EDITOR = "editor";
    /** Value: String  */
    public final static String TYPE_OF_DOCUMENT = "type-of-document";
    /** Value: Integer  */
    public final static String TYPE_OF_DOCUMENT_KEY = "type-of-document-key";
    /** Value: String  */
    public final static String PUBLISHED_IN = "published-in";
    /** Value: String  */
    public final static String VOLUME = "volume";
    /** Value: String  */
    public final static String PAGES = "pages";
    /** Value: String  */
    public final static String YEAR = "year";
    /** Value: String  */
    public final static String PUBLISHING_PLACE = "publishing-place";
    /** Value: String  */
    public final static String LOCATION = "location";
    /** Value: String  */
    public final static String ADDITIONAL_BIBLIOGRAPHIC_INFO = "additional-bibliographic-info";
    /** Value: String  */
    public final static String SOURCE = "source";
    /** Value: String  */
    public final static String ISBN = "isbn";
    /** Value: String  */
    public final static String PUBLISHER = "publisher";
    /** Value: String  */
    // defined below
    //public final static String DESCRIPTION_OF_TECH_DOMAIN = "description-of-tech-domain";

    // TECHNICAL DOMAIN SERVICE
    /** Value: IngridDocument encapsulating all SERVICE data */
    public final static String TECHNICAL_DOMAIN_SERVICE = "technical-domain-service";
    /** Value: String */
    public final static String SERVICE_TYPE = "service-type";
    /** Value: Integer */
    public final static String SERVICE_TYPE_KEY = "service-type-key";
    /** Value: String */
    public final static String SYSTEM_HISTORY = "system-history";
    /** Value: String */
    public final static String SYSTEM_ENVIRONMENT = "system-environment";
    /** Value: String */
    public final static String DATABASE_OF_SYSTEM = "database-of-system";
    // defined below
//    /** Value: String  */
//    public final static String DESCRIPTION_OF_TECH_DOMAIN = "description-of-tech-domain";
    // TECHNICAL DOMAIN SERVICE - VERSIONS
    /** Value: List of Strings  */
    public final static String SERVICE_VERSION_LIST = "service-version-list";
    // TECHNICAL DOMAIN SERVICE - OPERATIONS
    /** Value: List of IngridDocs  */
    public final static String SERVICE_OPERATION_LIST = "service-operation-list";
    /** Value: String */
    public final static String SERVICE_OPERATION_NAME = "service-operation-name";
    /** Value: String */
    public final static String SERVICE_OPERATION_NAME_KEY = "service-operation-name-key";
    /** Value: String */
    public final static String SERVICE_OPERATION_DESCRIPTION = "service-operation-description";
    /** Value: String */
    public final static String INVOCATION_NAME = "invocation-name";
    // TECHNICAL DOMAIN SERVICE OPERATION - PLATFORMS
    /** Value: List of Strings  */
    public final static String PLATFORM_LIST = "platform-list";
    // TECHNICAL DOMAIN SERVICE OPERATION - DEPENDS ONS
    /** Value: List of Strings  */
    public final static String DEPENDS_ON_LIST = "depends-on-list";
    // TECHNICAL DOMAIN SERVICE OPERATION - CONNPOINTS
    /** Value: List of Strings  */
    public final static String CONNECT_POINT_LIST = "connect-point-list";
    // TECHNICAL DOMAIN SERVICE - OPERATION - PARAMETERS
    /** Value: List of IngridDocs  */
    public final static String PARAMETER_LIST = "parameter-list";
    /** Value: String */
    public final static String PARAMETER_NAME = "parameter-name";
    /** Value: String */
    public final static String DIRECTION = "direction";
    /** Value: String */
    public final static String DESCRIPTION = "description";
    /** Value: Integer */
    public final static String OPTIONALITY = "optionality";
    /** Value: Integer */
    public final static String REPEATABILITY = "repeatability";
    // TECHNICAL DOMAIN SERVICE - TYPES ("subtypes")
    /** Value: List of IngridDocs  */
    public final static String SERVICE_TYPE2_LIST = "service-type2-list";
    /** Value: Integer */
    public final static String SERVICE_TYPE2_KEY = "service-type2-key";
    /** Value: String */
    public final static String SERVICE_TYPE2_VALUE = "service-type2-value";
    
    // TECHNICAL DOMAIN PROJECT
    /** Value: IngridDocument encapsulating all PROJECT data */
    public final static String TECHNICAL_DOMAIN_PROJECT = "technical-domain-project";
    /** Value: String  */
    public final static String LEADER_DESCRIPTION = "leader-description";
    /** Value: String  */
    public final static String MEMBER_DESCRIPTION = "member-description";
    /** Value: String  */
    public final static String DESCRIPTION_OF_TECH_DOMAIN = "description-of-tech-domain";
	
    // TECHNICAL DOMAIN DATASET
    /** Value: IngridDocument encapsulating all DATASET data */
    public final static String TECHNICAL_DOMAIN_DATASET = "technical-domain-dataset";
    /** Value: String  */
    public final static String METHOD = "method";
    /** Value: String  */
    // defined above
//    public final static String DESCRIPTION_OF_TECH_DOMAIN = "description-of-tech-domain";
    // TECHNICAL DOMAIN DATASET - PARAMETERS
    /** Value: List of IngridDocs  */
    public final static String PARAMETERS = "parameters";
    /** Value: String  */
    public final static String PARAMETER = "parameter";
    /** Value: String  */
    public final static String SUPPLEMENTARY_INFORMATION = "supplementary-information";

    // LEGISLATIONS
    /** Value: List of IngridDocument */
    public final static String LEGISLATIONS = "legislations";

    /** Value: String  */
    public final static String LEGISLATION_VALUE = "legislation-value";
    /** Value: Integer  */
    public final static String LEGISLATION_KEY = "legislation-key";
    
    
    // ENVIRONMENT-TOPICS / -CATEGORIES
    /** Value: List of Integer */
    public final static String ENV_CATEGORIES = "env-categories";
    /** Value: List of Integer */
    public final static String ENV_TOPICS = "env-topics";

    // TOPIC-CATEGORIES
    /** Value: List of Integer */
    public final static String TOPIC_CATEGORIES = "topic-categories";

    // DATA FORMATS
    /** Value: List of IngridDocs */
    public final static String DATA_FORMATS = "data-formats";
    /** Value: String */
    public final static String FORMAT_NAME = "format-name";
    /** Value: Integer */
    public final static String FORMAT_NAME_KEY = "format-name-key";
    /** Value: String */
    public final static String FORMAT_VERSION = "format-version";
    /** Value: String */
    public final static String FORMAT_SPECIFICATION = "format-specification";
    /** Value: String */
    public final static String FORMAT_FILE_DECOMPRESSION_TECHNIQUE = "format-file-decompression-technique";

    // MEDIUM OPTIONS
    /** Value: List of IngridDocs */
    public final static String MEDIUM_OPTIONS = "medium-options";
    /** Value: Integer */
    public final static String MEDIUM_NAME = "medium-name";
    /** Value: Double */
    public final static String MEDIUM_TRANSFER_SIZE = "medium-transfer-size";
    /** Value: String */
    public final static String MEDIUM_NOTE = "medium-note";

    // URL REF
    /** Value: List of IngridDocs */
    public final static String LINKAGES = "linkages";
    /** Value: String */
    public final static String LINKAGE_URL = "linkage-url";
    /** Value: Integer */
    public final static String LINKAGE_REFERENCE_ID = "linkage-reference-id";
    /** Value: String */
    public final static String LINKAGE_REFERENCE = "linkage-reference";
    /** Value: String */
    public final static String LINKAGE_DATATYPE = "linkage-datatype";
    /** Value: Integer */
    public final static String LINKAGE_DATATYPE_KEY = "linkage-datatype-key";
    /** Value: String */
    public final static String LINKAGE_VOLUME = "linkage-volume";
    /** Value: String */
    public final static String LINKAGE_ICON_URL = "linkage-icon-url";
    /** Value: String */
    public final static String LINKAGE_ICON_TEXT = "linkage-icon-text";
    /** Value: String */
    public final static String LINKAGE_DESCRIPTION = "linkage-description";
    /** Value: String */
    public final static String LINKAGE_NAME = "linkage-name";
    /** Value: Integer */
    public final static String LINKAGE_URL_TYPE = "linkage-url-type";

    /** Value: String */
    public final static String ORIGINAL_CONTROL_IDENTIFIER = "original-control-identifier";
    /** Value: Long */
    public final static String CATALOGUE_IDENTIFIER = "catalogue-identifier";
    /** Value: Integer */
    public final static String DATASET_CHARACTER_SET = "dataset-character-set";
    /** Value: Integer */
    public final static String METADATA_CHARACTER_SET = "metadata-character-set";
    /** Value: String */
    public final static String METADATA_STANDARD_NAME = "metadata-standard-name";
    /** Value: String */
    public final static String METADATA_STANDARD_VERSION = "metadata-standard-version";
    /** Value: String */
    public final static String LASTEXPORT_TIME = "lastexport-time";
    /** Value: String */
    public final static String EXPIRY_TIME = "expiry-time";
    /** Value: Integer */
    public final static String WORK_VERSION = "work-version";
    /** Value: String */
    public final static String MARK_DELETED = "mark-deleted";
    /** Value: IngridDocument (Address Map) */
    public final static String MOD_USER = "mod-user";
    /** Value: IngridDocument (Address Map) */
    public final static String RESPONSIBLE_USER = "responsible-user";


    // COMMENTS
    /** Value: List of IngridDocs */
    public final static String COMMENT_LIST = "comment_list";
    /** Value: String */
    public final static String COMMENT = "comment";
    /** Value: IngridDocument (Address Map) */
    public final static String CREATE_USER = "create-user";
    /** Value: String */
    public final static String CREATE_TIME = "create-time";

    // ADDITIONAL FIELDS
    /** Value: List of IngridDocs */
    public final static String ADDITIONAL_FIELDS = "additional-fields";
    /** Value: Long  */
    public final static String FIELD_IDENTIFIER = "field-identifier";
    /** Value: String  */
    public final static String FIELD_VALUE = "field-value";
    /** Value: String  */
    public final static String FIELD_NAME = "field-name";
    
    // CATALOG DATA
    /** Value: String */
    public final static String CATALOG_NAME = "catalog-name";
    /** Value: String */
    public final static String PARTNER_NAME = "partner-name";
    /** Value: String */
    public final static String PROVIDER_NAME = "provider-name";
    /** Value: String */
    public final static String COUNTRY = "country";
    /** Value: String */
    public final static String WORKFLOW_CONTROL = "workflow-control";
    /** Value: Integer */
    public final static String EXPIRY_DURATION = "expiry-duration";
    /** Value: IngridDocument encapsulating all SPATIAL REFERENCE data */
    public final static String CATALOG_LOCATION = "catalog-location";
    /** Value: String */
    // defined above
    // public final static String DATE_OF_CREATION = "date-of-creation";
    /** Value: String */
    // defined above
    // public final static String MOD_UUID = "mod-uuid";
    /** Value: String */
    // defined above
    // public final static String DATE_OF_LAST_MODIFICATION = "date-of-last-modification";
    
    
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
    /** Value: Integer */
    public final static String NAME_FORM_KEY = "name-form-key";
    /** Value: String */
    public final static String TITLE_OR_FUNCTION = "title-or-function";
    /** Value: Integer */
    public final static String TITLE_OR_FUNCTION_KEY = "title-or-function-key";
    /** Value: String */
    public final static String STREET = "street";
    /** Value: String */
    public final static String POSTAL_CODE_OF_COUNTRY = "postal-code-of-country";
    /** Value: String */
    public final static String POSTAL_CODE = "postal-code";
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
    /** Value: String */
    public final static String ORIGINAL_ADDRESS_IDENTIFIER = "original-address-identifier";

    /** Value: List of IngridDocs */
    public final static String COMMUNICATION = "communication";
    /** Value: String */
    public final static String COMMUNICATION_MEDIUM = "communication-medium";
    /** Value: Integer */
    public final static String COMMUNICATION_MEDIUM_KEY = "communication-medium-key";
    /** Value: String */
    public final static String COMMUNICATION_VALUE = "communication-value";
    /** Value: String */
    public final static String COMMUNICATION_DESCRIPTION = "communication-description";

    /** Value: String */
    public final static String RELATION_DATE_OF_LAST_MODIFICATION = "relation-date-of-last-modification";


    // INSPIRE
    // -------

    /** Value: List of IngridDocs */
    public final static String CONFORMITY_LIST = "conformity_list";
    /** Value: String */
    public final static String CONFORMITY_SPECIFICATION = "conformity-specification";
    /** Value: Integer */
    public final static String CONFORMITY_DEGREE_KEY = "conformity-degree-key";
    /** Value: String */
    public final static String CONFORMITY_DEGREE_VALUE = "conformity-degree-value";

    /** Value: List of IngridDocs */
    public final static String ACCESS_LIST = "access_list";
    /** Value: Integer */
    public final static String ACCESS_RESTRICTION_KEY = "access-restriction-key";
    /** Value: String */
    public final static String ACCESS_RESTRICTION_VALUE = "access-restriction-value";
    /** Value: String */
    public final static String ACCESS_TERMS_OF_USE = "access-terms-of-use";

}
