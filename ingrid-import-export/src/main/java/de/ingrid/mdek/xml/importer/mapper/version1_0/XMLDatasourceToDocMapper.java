package de.ingrid.mdek.xml.importer.mapper.version1_0;

import static de.ingrid.mdek.xml.util.IngridDocUtils.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.xml.importer.mapper.AbstractXMLToDocMapper;
import de.ingrid.mdek.xml.util.XPathUtils;
import de.ingrid.utils.IngridDocument;

public class XMLDatasourceToDocMapper extends AbstractXMLToDocMapper {

	private static final String X_OBJECT_IDENTIFIER = "//data-source/general/object-identifier/text()";
	private static final String X_CATALOGUE_IDENTIFIER = "//data-source/general/catalogue-identifier/text()";
	private static final String X_MODIFICATOR_IDENTIFIER = "//data-source/general/modificator-identifier/text()";
	private static final String X_RESPONSIBLE_IDENTIFIER = "//data-source/general/responsible-identifier/text()";
	private static final String X_OBJECT_CLASS = "//data-source/general/object-class/@id";
	private static final String X_TITLE = "//data-source/general/title/text()";
	private static final String X_ABSTRACT = "//data-source/general/abstract/text()";
	private static final String X_DATE_OF_LAST_MODIFICATION = "//data-source/general/date-of-last-modification/text()";
	private static final String X_DATE_OF_CREATION = "//data-source/general/date-of-creation/text()";
	private static final String X_ORIGINAL_CONTROL_IDENTIFIER = "//data-source/general/original-control-identifier/text()";
	private static final String X_GENERAL_ADDITIONAL_VALUE_LIST = "//data-source/general/general-additional-values/general-additional-value";
	private static final String X_GENERAL_ADDITIONAL_VALUE_ID = "@id";
	private static final String X_GENERAL_ADDITIONAL_VALUE_FIELD_NAME = "field-name";
	private static final String X_GENERAL_ADDITIONAL_VALUE_FIELD_VALUE = "field-value";
	private static final String X_METADATA_STANDARD_NAME = "//data-source/general/metadata/metadata-standard-name/text()";
	private static final String X_METADATA_STANDARD_VERSION = "//data-source/general/metadata/metadata-standard-version/text()";
	private static final String X_METADATA_CHARACTER_SET = "//data-source/general/metadata/metadata-character-set/@iso-code";
	private static final String X_DATASET_ALTERNATE_NAME = "//data-source/general/dataset-alternate-name/text()";
	private static final String X_DATASET_CHARACTER_SET = "//data-source/general/dataset-character-set/@iso-code";
	private static final String X_TOPIC_CATEGORIES = "//data-source/general/topic-categories/topic-category/@id";
	private static final String X_IS_CATALOG = "//data-source/general/env-information/is-catalog/text()";
	private static final String X_ENV_TOPICS = "//data-source/general/env-information/env-topic/@id";
	private static final String X_ENV_CATEGORIES = "//data-source/general/env-information/env-category/@id";
	private static final String X_TECHNICAL_DOMAIN_DATASET = "//data-source/technical-domain/dataset";
	private static final String X_TECHNICAL_DOMAIN_SERVICE = "//data-source/technical-domain/service";
	private static final String X_TECHNICAL_DOMAIN_DOCUMENT = "//data-source/technical-domain/document";
	private static final String X_TECHNICAL_DOMAIN_MAP = "//data-source/technical-domain/map";
	private static final String X_TECHNICAL_DOMAIN_PROJECT = "//data-source/technical-domain/project";
	private static final String X_TECHNICAL_DOMAIN_DESCRIPTION_OF_TECH_DOMAIN = "description-of-tech-domain";
	private static final String X_DATASET_PARAMETER_LIST = "//data-source/technical-domain/dataset/dataset-parameter";
	private static final String X_DATASET_PARAMETER_PARAMETER = "parameter";
	private static final String X_DATASET_PARAMETER_SUPPLEMENTARY_INFORMATION = "supplementary-information";
	private static final String X_DATASET_METHOD = "method";
	private static final String X_SERVICE_CLASSIFICATION_KEY = "service-classification/@id";
	private static final String X_SERVICE_CLASSIFICATION = "service-classification/text()";
	private static final String X_SERVICE_TYPE_LIST = "service-type";
	private static final String X_PUBLICATION_SCALE_LIST = "publication-scale";
	private static final String X_PUBLICATION_SCALE_SCALE = "scale";
	private static final String X_PUBLICATION_SCALE_RES_GROUND = "resolution-ground";
	private static final String X_PUBLICATION_SCALE_RES_SCAN = "resolution-scale";
	private static final String X_SERVICE_SYSTEM_HISTORY = "system-history/text()";
	private static final String X_SERVICE_DATABASE_OF_SYSTEM = "database-of-system/text()";
	private static final String X_SERVICE_SYSTEM_ENVIRONMENT = "system-environment/text()";
	private static final String X_SERVICE_VERSION_LIST = "service-version";
	private static final String X_SERVICE_OPERATION_LIST = "service-operation";
	private static final String X_SERVICE_OPERATION_NAME = "operation-name/text()";
	private static final String X_SERVICE_OPERATION_NAME_KEY = "operation-name/@id";
	private static final String X_SERVICE_OPERATION_DESCRIPTION = "description-of-operation/text()";
	private static final String X_SERVICE_INVOCATION_NAME = "invocation-name/text()";
	private static final String X_SERVICE_PLATFORM_LIST = "platform";
	private static final String X_SERVICE_CONNECTION_POINT_LIST = "connection-point";
	private static final String X_SERVICE_OPERATION_PARAMETER_LIST = "parameter-of-operation";
	private static final String X_SERVICE_OPERATION_PARAMETER_NAME = "name/text()";
	private static final String X_SERVICE_OPERATION_PARAMETER_OPTIONAL = "optional/text()";
	private static final String X_SERVICE_OPERATION_PARAMETER_REPEATABILITY = "repeatability/text()";
	private static final String X_SERVICE_OPERATION_PARAMETER_DIRECTION = "direction/text()";
	private static final String X_SERVICE_OPERATION_PARAMETER_DESCRIPTION_OF_PARAMETER = "description-of-parameter/text()";
	private static final String X_SERVICE_DEPENDS_ON_LIST = "depends-on";
	private static final String X_DOCUMENT_PUBLISHER = "publisher/text()";
	private static final String X_DOCUMENT_PUBLISHING_PLACE = "publishing-place/text()";
	private static final String X_DOCUMENT_YEAR = "year/text()";
	private static final String X_DOCUMENT_ISBN = "isbn/text()";
	private static final String X_DOCUMENT_SOURCE = "source/text()";
	private static final String X_DOCUMENT_TYPE_OF_DOCUMENT = "type-of-document/text()";
	private static final String X_DOCUMENT_TYPE_OF_DOCUMENT_KEY = "type-of-document/@id";
	private static final String X_DOCUMENT_EDITOR = "editor/text()";
	private static final String X_DOCUMENT_AUTHOR = "author/text()";
	private static final String X_DOCUMENT_ADDITIONAL_BIBLIOGRAPHIC_INFO = "additional-bibliographic-info/text()";
	private static final String X_DOCUMENT_LOCATION = "location/text()";
	private static final String X_DOCUMENT_PAGES = "pages/text()";
	private static final String X_DOCUMENT_VOLUME = "volume/text()";
	private static final String X_DOCUMENT_PUBLISHED_IN = "published-in/text()";
	private static final String X_MAP_HIERARCHY_LEVEL = "hierarchy-level/@iso-code";
	private static final String X_MAP_DATA = "data/text()";
	private static final String X_MAP_RESOLUTION = "resolution/text()";
	private static final String X_MAP_COORDINATE_SYSTEM = "coordinate-system/text()";
	private static final String X_MAP_COORDINATE_SYSTEM_KEY = "coordinate-system/@id";
	private static final String X_MAP_KEY_CATALOGUE_LIST = "key-catalogue";
	private static final String X_MAP_KEY_CATALOGUE = "key-cat/text()";
	private static final String X_MAP_KEY_CATALOGUE_KEY = "key-cat/@id";
	private static final String X_MAP_KEY_CATALOGUE_DATE = "key-date/text()";
	private static final String X_MAP_KEY_CATALOGUE_EDITION = "edition/text()";
	private static final String X_MAP_DEGREE_OF_RECORD = "degree-of-record/text()";
	private static final String X_MAP_METHOD_OF_PRODUCTION = "method-of-production/text()";
	private static final String X_MAP_TECHNICAL_BASE = "technical-base/text()";
	private static final String X_MAP_SYMBOL_CATALOGUE_LIST = "symbol-catalogue";
	private static final String X_MAP_SYMBOL_CATALOGUE = "symbol-cat/text()";
	private static final String X_MAP_SYMBOL_CATALOGUE_KEY = "symbol-cat/@id";
	private static final String X_MAP_SYMBOL_CATALOGUE_DATE = "symbol-date/text()";
	private static final String X_MAP_SYMBOL_CATALOGUE_EDITION = "edition/text()";
	private static final String X_MAP_SPATIAL_REPRESENTATION_TYPE_LIST = "spatial-representation-type/@iso-code";
	private static final String X_MAP_VECTOR_TOPOLOGY_LEVEL = "vector-format/vector-topology-level/@iso-code";
	private static final String X_MAP_GEO_VECTOR_LIST = "vector-format/geo-vector";
	private static final String X_MAP_GEO_VECTOR_OBJECT_COUNT = "geometric-object-count/text()";
	private static final String X_MAP_GEO_VECTOR_OBJECT_TYPE = "geometric-object-type/@iso-code";
	private static final String X_MAP_POS_ACCURACY_VERTICAL = "pos-accuracy-vertical/text()";
	private static final String X_MAP_KEYC_INCL_W_DATASET = "keyc-incl-w-dataset/text()";
	private static final String X_MAP_FEATURE_TYPE_LIST = "feature-type";
	private static final String X_MAP_DATASOURCE_IDENTIFICATOR = "datasource-identificator/text()";
	private static final String X_PROJECT_MEMBER_DESCRIPTION = "member-description/text()";
	private static final String X_PROJECT_LEADER_DESCRIPTION = "leader-description/text()";
	private static final String X_ADDITIONAL_DATA_LANGUAGE = "//data-source/additional-information/data-language/@iso-code";
	private static final String X_ADDITIONAL_METADATA_LANGUAGE = "//data-source/additional-information/metadata-language/@iso-code";
	private static final String X_ADDITIONAL_EXPORT_TO_LIST = "//data-source/additional-information/export-to";
	private static final String X_ADDITIONAL_LEGISLATION_LIST = "//data-source/additional-information/legislation";
	private static final String X_ADDITIONAL_DATASET_INTENTIONS = "//data-source/additional-information/dataset-intentions/text()";
	private static final String X_ADDITIONAL_ACCESS_CONSTRAINT_LIST = "//data-source/additional-information/access-constraint";
	private static final String X_ADDITIONAL_ACCESS_CONSTRAINT_RESTRICTION = "restriction/text()";
	private static final String X_ADDITIONAL_ACCESS_CONSTRAINT_RESTRICTION_KEY = "restriction/@id";
	private static final String X_ADDITIONAL_ACCESS_CONSTRAINT_TERMS_OF_USE = "terms-of-use";
	private static final String X_ADDITIONAL_MEDIUM_OPTION_LIST = "//data-source/additional-information/medium-option";
	private static final String X_ADDITIONAL_MEDIUM_OPTION_NAME = "medium-name/@iso-code";
	private static final String X_ADDITIONAL_MEDIUM_OPTION_NOTE = "medium-note/text()";
	private static final String X_ADDITIONAL_MEDIUM_OPTION_TRANSFER_SIZE = "transfer-size/text()";
	private static final String X_ADDITIONAL_DATA_FORMAT_LIST = "//data-source/additional-information/data-format";
	private static final String X_ADDITIONAL_DATA_FORMAT_NAME = "format-name/text()";
	private static final String X_ADDITIONAL_DATA_FORMAT_NAME_KEY = "format-name/@id";
	private static final String X_ADDITIONAL_DATA_FORMAT_VERSION = "version/text()";
	private static final String X_ADDITIONAL_DATA_FORMAT_SPECIFICATION = "specification/text()";
	private static final String X_ADDITIONAL_DATA_FORMAT_FILE_DECOMPRESSION_TECHNIQUE = "file-decompression-technique/text()";
	private static final String X_ADDITIONAL_PUBLICATION_CONDITION = "//data-source/additional-information/publication-condition/text()";
	private static final String X_ADDITIONAL_DATASET_USAGE = "//data-source/additional-information/dataset-usage/text()";
	private static final String X_ADDITIONAL_ORDERING_INSTRUCTION = "//data-source/additional-information/ordering-instructions/text()";
	private static final String X_ADDITIONAL_COMMENT_LIST = "//data-source/additional-information/comment";
	private static final String X_ADDITIONAL_COMMENT_CONTENT = "comment-content/text()";
	private static final String X_ADDITIONAL_COMMENT_CREATOR = "creator-identifier/text()";
	private static final String X_ADDITIONAL_COMMENT_DATE_OF_CREATION = "date-of-creation/text()";
	private static final String X_ADDITIONAL_CONFORMITY_LIST = "//data-source/additional-information/conformity";
	private static final String X_ADDITIONAL_CONFORMITY_SPECIFICATION = "conformity-specification/text()";
	private static final String X_ADDITIONAL_CONFORMITY_DEGREE = "conformity-degree/text()";
	private static final String X_ADDITIONAL_CONFORMITY_DEGREE_KEY = "conformity-degree/@id";
	private static final String X_ADDITIONAL_CONFORMITY_PUBLICATION_DATE = "conformity-publication-date/text()";
	private static final String X_SPATIAL_DESCRIPTION = "//data-source/spatial-domain/description-of-spatial-domain/text()";
	private static final String X_SPATIAL_VERTICAL_EXTENT_MINIMUM = "//data-source/spatial-domain/vertical-extent/vertical-extent-minimum/text()";
	private static final String X_SPATIAL_VERTICAL_EXTENT_MAXIMUM = "//data-source/spatial-domain/vertical-extent/vertical-extent-maximum/text()";
	private static final String X_SPATIAL_VERTICAL_EXTENT_UNIT = "//data-source/spatial-domain/vertical-extent/vertical-extent-unit/@id";
	private static final String X_SPATIAL_VERTICAL_EXTENT_VDATUM = "//data-source/spatial-domain/vertical-extent/vertical-extent-vdatum/@id";
	private static final String X_SPATIAL_GEO_LIST = "//data-source/spatial-domain/geo-location";
	private static final String X_SPATIAL_GEO_CONTROLLED = "controlled-location";
	private static final String X_SPATIAL_GEO_UNCONTROLLED = "uncontrolled-location";
	private static final String X_SPATIAL_GEO_LOCATION_NAME = "location-name/text()";
	private static final String X_SPATIAL_GEO_LOCATION_NAME_KEY = "location-name/@id";
	private static final String X_SPATIAL_GEO_TOPIC_TYPE = "topic-type/text()";
	private static final String X_SPATIAL_GEO_LOCATION_CODE = "location-code/text()";
	private static final String X_SPATIAL_GEO_SNS_TOPIC_TYPE = "sns-topic-type/text()";
	private static final String X_SPATIAL_GEO_BOUND_WEST = "bounding-coordinates/west-bounding-coordinate/text()";
	private static final String X_SPATIAL_GEO_BOUND_EAST = "bounding-coordinates/east-bounding-coordinate/text()";
	private static final String X_SPATIAL_GEO_BOUND_NORTH = "bounding-coordinates/north-bounding-coordinate/text()";
	private static final String X_SPATIAL_GEO_BOUND_SOUTH = "bounding-coordinates/south-bounding-coordinate/text()";
	private static final String X_TEMPORAL_DESCRIPTION = "//data-source/temporal-domain/description-of-temporal-domain/text()";
	private static final String X_TEMPORAL_BEGINNING_DATE = "//data-source/temporal-domain/beginning-date/text()";
	private static final String X_TEMPORAL_ENDING_DATE = "//data-source/temporal-domain/ending-date/text()";
	private static final String X_TEMPORAL_TIME_STEP = "//data-source/temporal-domain/time-step/text()";
	private static final String X_TEMPORAL_TIME_SCALE = "//data-source/temporal-domain/time-scale/text()";
	private static final String X_TEMPORAL_TIME_PERIOD = "//data-source/temporal-domain/time-period/@iso-code";
	private static final String X_TEMPORAL_TIME_STATUS = "//data-source/temporal-domain/time-status/@iso-code";
	private static final String X_TEMPORAL_TIME_TYPE = "//data-source/temporal-domain/time-type/text()";
	private static final String X_TEMPORAL_DATASET_REFERENCE_LIST = "//data-source/temporal-domain/dataset-reference";
	private static final String X_TEMPORAL_DATASET_REFERENCE_DATE = "dataset-reference-date/text()";
	private static final String X_TEMPORAL_DATASET_REFERENCE_TYPE = "dataset-reference-type/@iso-code";
	private static final String X_SUBJECT_TERMS = "//data-source/subject-terms";
	private static final String X_LINK_LIST = "//data-source/available-linkage";
	private static final String X_LINK_NAME = "linkage-name/text()";
	private static final String X_LINK_URL = "linkage-url/text()";
	private static final String X_LINK_URL_TYPE = "linkage-url-type/text()";
	private static final String X_LINK_REFERENCE = "linkage-reference/text()";
	private static final String X_LINK_REFERENCE_KEY = "linkage-reference/@id";
	private static final String X_LINK_DESCRIPTION = "linkage-description/text()";
	private static final String X_PARENT_IDENTIFIER = "//data-source/parent-data-source/object-identifier/text()";
	private static final String X_RELATED_ADDRESS_LIST = "//data-source/related-address";
	private static final String X_RELATED_ADDRESS_TYPE_OF_RELATION = "type-of-relation/text()";
	private static final String X_RELATED_ADDRESS_TYPE_OF_RELATION_LIST_KEY = "type-of-relation/@list-id";
	private static final String X_RELATED_ADDRESS_TYPE_OF_RELATION_ENTRY_KEY = "type-of-relation/@entry-id";
	private static final String X_RELATED_ADDRESS_IDENTIFIER = "address-identifier/text()";
	private static final String X_RELATED_ADDRESS_DATE_OF_LAST_MODIFICATION = "date-of-last-modification/text()";
	private static final String X_RELATED_DS_LIST = "//data-source/link-data-source";
	private static final String X_RELATED_DS_TYPE = "object-link-type/text()";
	private static final String X_RELATED_DS_TYPE_KEY = "object-link-type/@id";
	private static final String X_RELATED_DS_DESCRIPTION = "object-link-description/text()";
	private static final String X_RELATED_DS_IDENTIFIER = "object-identifier/text()";

	public static IngridDocument map(Document source) {
		IngridDocument dataSource = new IngridDocument();

		mapGeneral(source, dataSource);
		mapTechnicalDomain(source, dataSource);
		mapAdditionalInformation(source, dataSource);
		mapSpatialDomain(source, dataSource);
		mapTemporalDomain(source, dataSource);
		mapSubjectTerms(source, dataSource);
		mapAvailableLinkages(source, dataSource);
		mapParentDataSource(source, dataSource);
		mapRelatedAddresses(source, dataSource);
		mapLinkDataSources(source, dataSource);

		return dataSource;
	}

	private static void mapGeneral(Document source, IngridDocument target) {
		putString(MdekKeys.UUID, XPathUtils.getString(source, X_OBJECT_IDENTIFIER), target);
		putLong(MdekKeys.CATALOGUE_IDENTIFIER, XPathUtils.getLong(source, X_CATALOGUE_IDENTIFIER), target);
		putString(new String[] {MdekKeys.MOD_USER, MdekKeys.UUID}, XPathUtils.getString(source, X_MODIFICATOR_IDENTIFIER), target);
		putString(new String[] {MdekKeys.RESPONSIBLE_USER, MdekKeys.UUID}, XPathUtils.getString(source, X_RESPONSIBLE_IDENTIFIER), target);
		putInt(MdekKeys.CLASS, XPathUtils.getInt(source, X_OBJECT_CLASS), target);
		putString(MdekKeys.TITLE, XPathUtils.getString(source, X_TITLE), target);
		putString(MdekKeys.ABSTRACT, XPathUtils.getString(source, X_ABSTRACT), target);
		putString(MdekKeys.DATE_OF_LAST_MODIFICATION, XPathUtils.getString(source, X_DATE_OF_LAST_MODIFICATION), target);
		putString(MdekKeys.DATE_OF_CREATION, XPathUtils.getString(source, X_DATE_OF_CREATION), target);
		putString(MdekKeys.ORIGINAL_CONTROL_IDENTIFIER, XPathUtils.getString(source, X_ORIGINAL_CONTROL_IDENTIFIER), target);
		mapGeneralAdditionalValues(source, target);
		putString(MdekKeys.METADATA_STANDARD_NAME, XPathUtils.getString(source, X_METADATA_STANDARD_NAME), target);
		putString(MdekKeys.METADATA_STANDARD_VERSION, XPathUtils.getString(source, X_METADATA_STANDARD_VERSION), target);
		putInt(MdekKeys.METADATA_CHARACTER_SET, XPathUtils.getInt(source, X_METADATA_CHARACTER_SET), target);
		putString(MdekKeys.DATASET_ALTERNATE_NAME, XPathUtils.getString(source, X_DATASET_ALTERNATE_NAME), target);
		putInt(MdekKeys.DATASET_CHARACTER_SET, XPathUtils.getInt(source, X_DATASET_CHARACTER_SET), target);
		mapTopicCategories(source, target);
		putString(MdekKeys.IS_CATALOG_DATA, XPathUtils.getString(source, X_IS_CATALOG), target);
		mapEnvTopics(source, target);
		mapEnvCategories(source, target);
	}

	private static void mapGeneralAdditionalValues(Document source, IngridDocument target) {
		NodeList additionalValues = XPathUtils.getNodeList(source, X_GENERAL_ADDITIONAL_VALUE_LIST);
		List<IngridDocument> additionalValuesList = new ArrayList<IngridDocument>();
		
		// TODO
/*
		for (int index = 0; index < additionalValues.getLength(); ++index) {
			Node additionalValue = additionalValues.item(index);
			IngridDocument additionalValueDoc = new IngridDocument();
			putLong(MdekKeys.SYS_ADDITIONAL_FIELD_IDENTIFIER, XPathUtils.getLong(additionalValue, X_GENERAL_ADDITIONAL_VALUE_ID), additionalValueDoc);
			putString(MdekKeys.SYS_ADDITIONAL_FIELD_NAME, XPathUtils.getString(additionalValue, X_GENERAL_ADDITIONAL_VALUE_FIELD_NAME), additionalValueDoc);
			putString(MdekKeys.ADDITIONAL_FIELD_VALUE, XPathUtils.getString(additionalValue, X_GENERAL_ADDITIONAL_VALUE_FIELD_VALUE), additionalValueDoc);
			additionalValuesList.add(additionalValueDoc);
		}
*/
		putDocList(MdekKeys.ADDITIONAL_FIELDS, additionalValuesList, target);
	}

	private static void mapTopicCategories(Document source, IngridDocument target) {
		mapIntList(source, X_TOPIC_CATEGORIES, target, MdekKeys.TOPIC_CATEGORIES);
	}

	private static void mapEnvTopics(Document source, IngridDocument target) {
		mapIntList(source, X_ENV_TOPICS, target, MdekKeys.ENV_TOPICS);
	}

	private static void mapEnvCategories(Document source, IngridDocument target) {
		// TODO: ENV_CATEGORIES removed with version 311 
//		mapIntList(source, X_ENV_CATEGORIES, target, MdekKeys.ENV_CATEGORIES);
	}

	private static void mapTechnicalDomain(Document source, IngridDocument target) {
		if (XPathUtils.nodeExists(source, X_TECHNICAL_DOMAIN_DATASET)) {
			mapDataset(source, target);
		}

		if (XPathUtils.nodeExists(source, X_TECHNICAL_DOMAIN_SERVICE)) {
			mapService(source, target);
		}

		if (XPathUtils.nodeExists(source, X_TECHNICAL_DOMAIN_DOCUMENT)) {
			mapDocument(source, target);
		}

		if (XPathUtils.nodeExists(source, X_TECHNICAL_DOMAIN_MAP)) {
			mapMap(source, target);
		}

		if (XPathUtils.nodeExists(source, X_TECHNICAL_DOMAIN_PROJECT)) {
			mapProject(source, target);
		}
	}

	private static void mapDataset(Document source, IngridDocument target) {
		Node dataset = XPathUtils.getNode(source, X_TECHNICAL_DOMAIN_DATASET);

		putString(new String[] {MdekKeys.TECHNICAL_DOMAIN_DATASET, MdekKeys.DESCRIPTION_OF_TECH_DOMAIN},
				XPathUtils.getString(dataset, X_TECHNICAL_DOMAIN_DESCRIPTION_OF_TECH_DOMAIN), target);
		mapDatasetParameter(source, target);
		putString(new String[] {MdekKeys.TECHNICAL_DOMAIN_DATASET, MdekKeys.METHOD},
				XPathUtils.getString(dataset, X_DATASET_METHOD), target);
	}

	private static void mapDatasetParameter(Document source, IngridDocument target) {
		NodeList parameterNodeList = XPathUtils.getNodeList(source, X_DATASET_PARAMETER_LIST);
		List<IngridDocument> parameters = new ArrayList<IngridDocument>();
		for (int index = 0; index < parameterNodeList.getLength(); ++index) {
			Node parameter = parameterNodeList.item(index);
			IngridDocument parameterDoc = new IngridDocument();
			putString(MdekKeys.PARAMETER, XPathUtils.getString(parameter, X_DATASET_PARAMETER_PARAMETER), parameterDoc);
			putString(MdekKeys.SUPPLEMENTARY_INFORMATION, XPathUtils.getString(parameter, X_DATASET_PARAMETER_SUPPLEMENTARY_INFORMATION), parameterDoc);
			parameters.add(parameterDoc);
		}

		putDocList(new String[] {MdekKeys.TECHNICAL_DOMAIN_DATASET, MdekKeys.PARAMETERS}, parameters, target);
	}

	private static void mapService(Document source, IngridDocument target) {
		Node service = XPathUtils.getNode(source, X_TECHNICAL_DOMAIN_SERVICE);

		putString(new String[] {MdekKeys.TECHNICAL_DOMAIN_SERVICE, MdekKeys.DESCRIPTION_OF_TECH_DOMAIN},
				XPathUtils.getString(service, X_TECHNICAL_DOMAIN_DESCRIPTION_OF_TECH_DOMAIN), target);
		putInt(new String[] {MdekKeys.TECHNICAL_DOMAIN_SERVICE, MdekKeys.SERVICE_TYPE_KEY},
				XPathUtils.getInt(service, X_SERVICE_CLASSIFICATION_KEY), target);
		putString(new String[] {MdekKeys.TECHNICAL_DOMAIN_SERVICE, MdekKeys.SERVICE_TYPE},
				XPathUtils.getString(service, X_SERVICE_CLASSIFICATION), target);
		mapServiceTypes(source, target);
		mapPublicationScales(service, (IngridDocument) target.get(MdekKeys.TECHNICAL_DOMAIN_SERVICE));
		putString(new String[] {MdekKeys.TECHNICAL_DOMAIN_SERVICE, MdekKeys.SYSTEM_HISTORY},
				XPathUtils.getString(service, X_SERVICE_SYSTEM_HISTORY), target);
		putString(new String[] {MdekKeys.TECHNICAL_DOMAIN_SERVICE, MdekKeys.DATABASE_OF_SYSTEM},
				XPathUtils.getString(service, X_SERVICE_DATABASE_OF_SYSTEM), target);
		putString(new String[] {MdekKeys.TECHNICAL_DOMAIN_SERVICE, MdekKeys.SYSTEM_ENVIRONMENT},
				XPathUtils.getString(service, X_SERVICE_SYSTEM_ENVIRONMENT), target);
		mapStringList(service, X_SERVICE_VERSION_LIST, target,
				new String[] {MdekKeys.TECHNICAL_DOMAIN_SERVICE, MdekKeys.SERVICE_VERSION_LIST});
		mapServiceOperations(source, target);
	}

	private static void mapServiceTypes(Document source, IngridDocument target) {
		Node service = XPathUtils.getNode(source, X_TECHNICAL_DOMAIN_SERVICE);
		NodeList serviceTypeNodeList = XPathUtils.getNodeList(service, X_SERVICE_TYPE_LIST);
		List<IngridDocument> serviceTypes = new ArrayList<IngridDocument>();

		for (int index = 0; index < serviceTypeNodeList.getLength(); index++) {
			Node serviceType = serviceTypeNodeList.item(index);
			IngridDocument serviceTypeDoc = new IngridDocument();
			putInt(MdekKeys.SERVICE_TYPE2_KEY, XPathUtils.getInt(serviceType, X_ATTRIBUTE_ID), serviceTypeDoc);
			putString(MdekKeys.SERVICE_TYPE2_VALUE, serviceType.getTextContent(), serviceTypeDoc);
			serviceTypes.add(serviceTypeDoc);
		}

		putDocList(new String[] {MdekKeys.TECHNICAL_DOMAIN_SERVICE, MdekKeys.SERVICE_TYPE2_LIST}, serviceTypes, target);
	}

	private static void mapPublicationScales(Node context, IngridDocument target) {
		NodeList publicationScaleNodeList = XPathUtils.getNodeList(context, X_PUBLICATION_SCALE_LIST);
		List<IngridDocument> publicationScales = new ArrayList<IngridDocument>();

		for (int index = 0; index < publicationScaleNodeList.getLength(); index++) {
			Node publicationScale = publicationScaleNodeList.item(index);
			IngridDocument publicationScaleDoc = new IngridDocument();
			putInt(MdekKeys.SCALE, XPathUtils.getInt(publicationScale, X_PUBLICATION_SCALE_SCALE), publicationScaleDoc);
			putDouble(MdekKeys.RESOLUTION_GROUND, XPathUtils.getDouble(publicationScale, X_PUBLICATION_SCALE_RES_GROUND), publicationScaleDoc);
			putDouble(MdekKeys.RESOLUTION_SCAN, XPathUtils.getDouble(publicationScale, X_PUBLICATION_SCALE_RES_SCAN), publicationScaleDoc);
			publicationScales.add(publicationScaleDoc);
		}

		putDocList(MdekKeys.PUBLICATION_SCALE_LIST, publicationScales, target);
	}

	private static void mapServiceOperations(Document source, IngridDocument target) {
		Node service = XPathUtils.getNode(source, X_TECHNICAL_DOMAIN_SERVICE);
		NodeList serviceOperationList = XPathUtils.getNodeList(service, X_SERVICE_OPERATION_LIST);
		ArrayList<IngridDocument> serviceOperations = new ArrayList<IngridDocument>();

		for (int index = 0; index < serviceOperationList.getLength(); index++) {
			Node serviceOperation = serviceOperationList.item(index);
			IngridDocument serviceOperationDoc = new IngridDocument();
			putString(MdekKeys.SERVICE_OPERATION_NAME, XPathUtils.getString(serviceOperation, X_SERVICE_OPERATION_NAME), serviceOperationDoc);
			putInt(MdekKeys.SERVICE_OPERATION_NAME_KEY, XPathUtils.getInt(serviceOperation, X_SERVICE_OPERATION_NAME_KEY), serviceOperationDoc);
			putString(MdekKeys.SERVICE_OPERATION_DESCRIPTION, XPathUtils.getString(serviceOperation, X_SERVICE_OPERATION_DESCRIPTION), serviceOperationDoc);
			putString(MdekKeys.INVOCATION_NAME, XPathUtils.getString(serviceOperation, X_SERVICE_INVOCATION_NAME), serviceOperationDoc);
			mapStringList(serviceOperation, X_SERVICE_PLATFORM_LIST, serviceOperationDoc, MdekKeys.PLATFORM_LIST);
			mapStringList(serviceOperation, X_SERVICE_CONNECTION_POINT_LIST, serviceOperationDoc, MdekKeys.CONNECT_POINT_LIST);
			mapParametersOfOperation(serviceOperation, serviceOperationDoc);
			mapStringList(serviceOperation, X_SERVICE_DEPENDS_ON_LIST, serviceOperationDoc, MdekKeys.DEPENDS_ON_LIST);
			serviceOperations.add(serviceOperationDoc);
		}

		putDocList(new String[] {MdekKeys.TECHNICAL_DOMAIN_SERVICE, MdekKeys.SERVICE_OPERATION_LIST}, serviceOperations, target);
	}

	private static void mapParametersOfOperation(Node operationContext, IngridDocument operationDoc) {
		List<IngridDocument> parameterList = new ArrayList<IngridDocument>();
		NodeList parametersOfOperation = XPathUtils.getNodeList(operationContext, X_SERVICE_OPERATION_PARAMETER_LIST);

		for (int index = 0; index < parametersOfOperation.getLength(); index++) {
			Node parameter = parametersOfOperation.item(index);
			IngridDocument parameterDoc = new IngridDocument();
			putString(MdekKeys.PARAMETER_NAME, XPathUtils.getString(parameter, X_SERVICE_OPERATION_PARAMETER_NAME), parameterDoc);
			putInt(MdekKeys.OPTIONALITY, XPathUtils.getInt(parameter, X_SERVICE_OPERATION_PARAMETER_OPTIONAL), parameterDoc);
			putInt(MdekKeys.REPEATABILITY, XPathUtils.getInt(parameter, X_SERVICE_OPERATION_PARAMETER_REPEATABILITY), parameterDoc);
			putString(MdekKeys.DIRECTION, XPathUtils.getString(parameter, X_SERVICE_OPERATION_PARAMETER_DIRECTION), parameterDoc);
			putString(MdekKeys.DESCRIPTION, XPathUtils.getString(parameter, X_SERVICE_OPERATION_PARAMETER_DESCRIPTION_OF_PARAMETER), parameterDoc);
			parameterList.add(parameterDoc);
		}

		putDocList(MdekKeys.PARAMETER_LIST, parameterList, operationDoc);
	}

	private static void mapDocument(Document source, IngridDocument target) {
		Node document = XPathUtils.getNode(source, X_TECHNICAL_DOMAIN_DOCUMENT);

		putString(new String[] {MdekKeys.TECHNICAL_DOMAIN_DOCUMENT, MdekKeys.DESCRIPTION_OF_TECH_DOMAIN},
				XPathUtils.getString(document, X_TECHNICAL_DOMAIN_DESCRIPTION_OF_TECH_DOMAIN), target);
		putString(new String[] {MdekKeys.TECHNICAL_DOMAIN_DOCUMENT, MdekKeys.PUBLISHER},
				XPathUtils.getString(document, X_DOCUMENT_PUBLISHER), target);
		putString(new String[] {MdekKeys.TECHNICAL_DOMAIN_DOCUMENT, MdekKeys.PUBLISHING_PLACE},
				XPathUtils.getString(document, X_DOCUMENT_PUBLISHING_PLACE), target);
		putString(new String[] {MdekKeys.TECHNICAL_DOMAIN_DOCUMENT, MdekKeys.YEAR},
				XPathUtils.getString(document, X_DOCUMENT_YEAR), target);
		putString(new String[] {MdekKeys.TECHNICAL_DOMAIN_DOCUMENT, MdekKeys.ISBN},
				XPathUtils.getString(document, X_DOCUMENT_ISBN), target);
		putString(new String[] {MdekKeys.TECHNICAL_DOMAIN_DOCUMENT, MdekKeys.SOURCE},
				XPathUtils.getString(document, X_DOCUMENT_SOURCE), target);
		putString(new String[] {MdekKeys.TECHNICAL_DOMAIN_DOCUMENT, MdekKeys.TYPE_OF_DOCUMENT},
				XPathUtils.getString(document, X_DOCUMENT_TYPE_OF_DOCUMENT), target);
		putInt(new String[] {MdekKeys.TECHNICAL_DOMAIN_DOCUMENT, MdekKeys.TYPE_OF_DOCUMENT_KEY},
				XPathUtils.getInt(document, X_DOCUMENT_TYPE_OF_DOCUMENT_KEY), target);
		putString(new String[] {MdekKeys.TECHNICAL_DOMAIN_DOCUMENT, MdekKeys.EDITOR},
				XPathUtils.getString(document, X_DOCUMENT_EDITOR), target);
		putString(new String[] {MdekKeys.TECHNICAL_DOMAIN_DOCUMENT, MdekKeys.AUTHOR},
				XPathUtils.getString(document, X_DOCUMENT_AUTHOR), target);
		putString(new String[] {MdekKeys.TECHNICAL_DOMAIN_DOCUMENT, MdekKeys.ADDITIONAL_BIBLIOGRAPHIC_INFO},
				XPathUtils.getString(document, X_DOCUMENT_ADDITIONAL_BIBLIOGRAPHIC_INFO), target);
		putString(new String[] {MdekKeys.TECHNICAL_DOMAIN_DOCUMENT, MdekKeys.LOCATION},
				XPathUtils.getString(document, X_DOCUMENT_LOCATION), target);
		putString(new String[] {MdekKeys.TECHNICAL_DOMAIN_DOCUMENT, MdekKeys.PAGES},
				XPathUtils.getString(document, X_DOCUMENT_PAGES), target);
		putString(new String[] {MdekKeys.TECHNICAL_DOMAIN_DOCUMENT, MdekKeys.VOLUME},
				XPathUtils.getString(document, X_DOCUMENT_VOLUME), target);
		putString(new String[] {MdekKeys.TECHNICAL_DOMAIN_DOCUMENT, MdekKeys.PUBLISHED_IN},
				XPathUtils.getString(document, X_DOCUMENT_PUBLISHED_IN), target);
	}

	private static void mapMap(Document source, IngridDocument target) {
		Node map = XPathUtils.getNode(source, X_TECHNICAL_DOMAIN_MAP);

		putInt(new String[] {MdekKeys.TECHNICAL_DOMAIN_MAP, MdekKeys.HIERARCHY_LEVEL},
				XPathUtils.getInt(map, X_MAP_HIERARCHY_LEVEL), target);
		putString(new String[] {MdekKeys.TECHNICAL_DOMAIN_MAP, MdekKeys.DESCRIPTION_OF_TECH_DOMAIN},
				XPathUtils.getString(map, X_TECHNICAL_DOMAIN_DESCRIPTION_OF_TECH_DOMAIN), target);
		putString(new String[] {MdekKeys.TECHNICAL_DOMAIN_MAP, MdekKeys.DATA},
				XPathUtils.getString(map, X_MAP_DATA), target);
		putDouble(new String[] {MdekKeys.TECHNICAL_DOMAIN_MAP, MdekKeys.RESOLUTION},
				XPathUtils.getDouble(map, X_MAP_RESOLUTION), target);
		putString(new String[] {MdekKeys.TECHNICAL_DOMAIN_MAP, MdekKeys.COORDINATE_SYSTEM},
				XPathUtils.getString(map, X_MAP_COORDINATE_SYSTEM), target);
		putInt(new String[] {MdekKeys.TECHNICAL_DOMAIN_MAP, MdekKeys.REFERENCESYSTEM_ID},
				XPathUtils.getInt(map, X_MAP_COORDINATE_SYSTEM_KEY), target);
		mapPublicationScales(map, (IngridDocument) target.get(MdekKeys.TECHNICAL_DOMAIN_MAP));
		mapKeyCatalogue(map, target);
		putDouble(new String[] {MdekKeys.TECHNICAL_DOMAIN_MAP, MdekKeys.DEGREE_OF_RECORD},
				XPathUtils.getDouble(map, X_MAP_DEGREE_OF_RECORD), target);
		putString(new String[] {MdekKeys.TECHNICAL_DOMAIN_MAP, MdekKeys.METHOD_OF_PRODUCTION},
				XPathUtils.getString(map, X_MAP_METHOD_OF_PRODUCTION), target);
		putString(new String[] {MdekKeys.TECHNICAL_DOMAIN_MAP, MdekKeys.TECHNICAL_BASE},
				XPathUtils.getString(map, X_MAP_TECHNICAL_BASE), target);
		mapSymbolCatalogue(map, target);
		mapIntList(map, X_MAP_SPATIAL_REPRESENTATION_TYPE_LIST, target,
				new String[] {MdekKeys.TECHNICAL_DOMAIN_MAP, MdekKeys.SPATIAL_REPRESENTATION_TYPE_LIST});
		putInt(new String[] {MdekKeys.TECHNICAL_DOMAIN_MAP, MdekKeys.VECTOR_TOPOLOGY_LEVEL},
				XPathUtils.getInt(map, X_MAP_VECTOR_TOPOLOGY_LEVEL), target);
		mapGeoVectors(map, target);
		putDouble(new String[] {MdekKeys.TECHNICAL_DOMAIN_MAP, MdekKeys.POS_ACCURACY_VERTICAL},
				XPathUtils.getDouble(map, X_MAP_POS_ACCURACY_VERTICAL), target);
		putInt(new String[] {MdekKeys.TECHNICAL_DOMAIN_MAP, MdekKeys.KEYC_INCL_W_DATASET},
				XPathUtils.getInt(map, X_MAP_KEYC_INCL_W_DATASET), target);
		mapStringList(map, X_MAP_FEATURE_TYPE_LIST, target, new String[] {MdekKeys.TECHNICAL_DOMAIN_MAP, MdekKeys.FEATURE_TYPE_LIST});
		putString(new String[] {MdekKeys.TECHNICAL_DOMAIN_MAP, MdekKeys.DATASOURCE_UUID},
				XPathUtils.getString(map, X_MAP_DATASOURCE_IDENTIFICATOR), target);
	}

	private static void mapKeyCatalogue(Node mapContext, IngridDocument target) {
		NodeList keyCatalogues = XPathUtils.getNodeList(mapContext, X_MAP_KEY_CATALOGUE_LIST);
		List<IngridDocument> keyCatalogueList = new ArrayList<IngridDocument>();

		for (int index = 0; index < keyCatalogues.getLength(); index++) {
			Node keyCatalogue = keyCatalogues.item(index);
			IngridDocument keyCatalogueDoc = new IngridDocument();
			putString(MdekKeys.SUBJECT_CAT, XPathUtils.getString(keyCatalogue, X_MAP_KEY_CATALOGUE), keyCatalogueDoc);
			putInt(MdekKeys.SUBJECT_CAT_KEY, XPathUtils.getInt(keyCatalogue, X_MAP_KEY_CATALOGUE_KEY), keyCatalogueDoc);
			putString(MdekKeys.KEY_DATE, XPathUtils.getString(keyCatalogue, X_MAP_KEY_CATALOGUE_DATE), keyCatalogueDoc);
			putString(MdekKeys.EDITION, XPathUtils.getString(keyCatalogue, X_MAP_KEY_CATALOGUE_EDITION), keyCatalogueDoc);

			keyCatalogueList.add(keyCatalogueDoc);
		}

		putDocList(new String[]{MdekKeys.TECHNICAL_DOMAIN_MAP, MdekKeys.KEY_CATALOG_LIST}, keyCatalogueList, target);
	}

	private static void mapSymbolCatalogue(Node mapContext, IngridDocument target) {
		NodeList symbolCatalogues = XPathUtils.getNodeList(mapContext, X_MAP_SYMBOL_CATALOGUE_LIST);
		List<IngridDocument> symbolCatalogueList = new ArrayList<IngridDocument>();

		for (int index = 0; index < symbolCatalogues.getLength(); index++) {
			Node symbolCatalogue = symbolCatalogues.item(index);
			IngridDocument symbolCatalogueDoc = new IngridDocument();
			putString(MdekKeys.SYMBOL_CAT, XPathUtils.getString(symbolCatalogue, X_MAP_SYMBOL_CATALOGUE), symbolCatalogueDoc);
			putInt(MdekKeys.SYMBOL_CAT_KEY, XPathUtils.getInt(symbolCatalogue, X_MAP_SYMBOL_CATALOGUE_KEY), symbolCatalogueDoc);
			putString(MdekKeys.SYMBOL_DATE, XPathUtils.getString(symbolCatalogue, X_MAP_SYMBOL_CATALOGUE_DATE), symbolCatalogueDoc);
			putString(MdekKeys.SYMBOL_EDITION, XPathUtils.getString(symbolCatalogue, X_MAP_SYMBOL_CATALOGUE_EDITION), symbolCatalogueDoc);

			symbolCatalogueList.add(symbolCatalogueDoc);
		}

		putDocList(new String[]{MdekKeys.TECHNICAL_DOMAIN_MAP, MdekKeys.SYMBOL_CATALOG_LIST}, symbolCatalogueList, target);
	}

	private static void mapGeoVectors(Node mapContext, IngridDocument target) {
		NodeList geoVectors = XPathUtils.getNodeList(mapContext, X_MAP_GEO_VECTOR_LIST);
		List<IngridDocument> geoVectorList = new ArrayList<IngridDocument>();

		for (int index = 0; index < geoVectors.getLength(); index++) {
			Node geoVector = geoVectors.item(index);
			IngridDocument geoVectorDoc = new IngridDocument();
			putInt(MdekKeys.GEOMETRIC_OBJECT_COUNT, XPathUtils.getInt(geoVector, X_MAP_GEO_VECTOR_OBJECT_COUNT), geoVectorDoc);
			putInt(MdekKeys.GEOMETRIC_OBJECT_TYPE, XPathUtils.getInt(geoVector, X_MAP_GEO_VECTOR_OBJECT_TYPE), geoVectorDoc);

			geoVectorList.add(geoVectorDoc);
		}

		putDocList(new String[]{MdekKeys.TECHNICAL_DOMAIN_MAP, MdekKeys.GEO_VECTOR_LIST}, geoVectorList, target);
	}

	private static void mapProject(Document source, IngridDocument target) {
		Node project = XPathUtils.getNode(source, X_TECHNICAL_DOMAIN_PROJECT);

		putString(new String[] {MdekKeys.TECHNICAL_DOMAIN_PROJECT, MdekKeys.DESCRIPTION_OF_TECH_DOMAIN},
				XPathUtils.getString(project, X_TECHNICAL_DOMAIN_DESCRIPTION_OF_TECH_DOMAIN), target);
		putString(new String[] {MdekKeys.TECHNICAL_DOMAIN_PROJECT, MdekKeys.MEMBER_DESCRIPTION},
				XPathUtils.getString(project, X_PROJECT_MEMBER_DESCRIPTION), target);
		putString(new String[] {MdekKeys.TECHNICAL_DOMAIN_PROJECT, MdekKeys.LEADER_DESCRIPTION},
				XPathUtils.getString(project, X_PROJECT_LEADER_DESCRIPTION), target);
	}

	private static void mapAdditionalInformation(Document source, IngridDocument target) {
		// TODO: REMOVED IN VERSION 1.0.5. !!! Fix if import of older formats needed !
//		putString(MdekKeys.DATA_LANGUAGE, XPathUtils.getString(source, X_ADDITIONAL_DATA_LANGUAGE), target);
//		putString(MdekKeys.METADATA_LANGUAGE, XPathUtils.getString(source, X_ADDITIONAL_METADATA_LANGUAGE), target);

		mapExportTos(source, target);
		mapLegislations(source, target);
		putString(MdekKeys.DATASET_INTENTIONS,
				XPathUtils.getString(source, X_ADDITIONAL_DATASET_INTENTIONS), target);
		mapAccessConstraints(source, target);
		mapMediumOptions(source, target);
		mapDataFormats(source, target);
		putInt(MdekKeys.PUBLICATION_CONDITION,
				XPathUtils.getInt(source, X_ADDITIONAL_PUBLICATION_CONDITION), target);
		putString(MdekKeys.DATASET_USAGE,
				XPathUtils.getString(source, X_ADDITIONAL_DATASET_USAGE), target);
		putString(MdekKeys.ORDERING_INSTRUCTIONS,
				XPathUtils.getString(source, X_ADDITIONAL_ORDERING_INSTRUCTION), target);
		mapComments(source, target);
		mapConformities(source, target);
	}

	private static void mapExportTos(Document source, IngridDocument target) {
		NodeList exportTos = XPathUtils.getNodeList(source, X_ADDITIONAL_EXPORT_TO_LIST);
		List<IngridDocument> exportToList = new ArrayList<IngridDocument>();

		for (int index = 0; index < exportTos.getLength(); index++) {
			Node exportTo = exportTos.item(index);
			IngridDocument exportToDoc = new IngridDocument();
			putString(MdekKeys.EXPORT_CRITERION_VALUE, exportTo.getTextContent(), exportToDoc);
			putInt(MdekKeys.EXPORT_CRITERION_KEY, XPathUtils.getInt(exportTo, X_ATTRIBUTE_ID), exportToDoc);
			exportToList.add(exportToDoc);
		}

		putDocList(MdekKeys.EXPORT_CRITERIA, exportToList, target);
	}

	private static void mapLegislations(Document source, IngridDocument target) {
		NodeList legislations = XPathUtils.getNodeList(source, X_ADDITIONAL_LEGISLATION_LIST);
		List<IngridDocument> legislationList = new ArrayList<IngridDocument>();

		for (int index = 0; index < legislations.getLength(); index++) {
			Node legislation = legislations.item(index);
			IngridDocument legislationDoc = new IngridDocument();
			putString(MdekKeys.LEGISLATION_VALUE, legislation.getTextContent(), legislationDoc);
			putInt(MdekKeys.LEGISLATION_KEY, XPathUtils.getInt(legislation, X_ATTRIBUTE_ID), legislationDoc);
			legislationList.add(legislationDoc);
		}

		putDocList(MdekKeys.LEGISLATIONS, legislationList, target);
	}

	private static void mapAccessConstraints(Document source, IngridDocument target) {
		NodeList accessConstraints = XPathUtils.getNodeList(source, X_ADDITIONAL_ACCESS_CONSTRAINT_LIST);
		List<IngridDocument> accessConstraintList = new ArrayList<IngridDocument>();

		for (int index = 0; index < accessConstraints.getLength(); index++) {
			Node accessConstraint = accessConstraints.item(index);
			IngridDocument accessConstraintDoc = new IngridDocument();
			putString(MdekKeys.ACCESS_RESTRICTION_VALUE, XPathUtils.getString(accessConstraint, X_ADDITIONAL_ACCESS_CONSTRAINT_RESTRICTION), accessConstraintDoc);
			putInt(MdekKeys.ACCESS_RESTRICTION_KEY, XPathUtils.getInt(accessConstraint, X_ADDITIONAL_ACCESS_CONSTRAINT_RESTRICTION_KEY), accessConstraintDoc);
			// TODO: REMOVED IN VERSION 1.0.8. !!! Fix if import of older formats needed !
//			putString(MdekKeys.ACCESS_TERMS_OF_USE, XPathUtils.getString(accessConstraint, X_ADDITIONAL_ACCESS_CONSTRAINT_TERMS_OF_USE), accessConstraintDoc);

			accessConstraintList.add(accessConstraintDoc);
		}

		putDocList(MdekKeys.ACCESS_LIST, accessConstraintList, target);
	}

	private static void mapMediumOptions(Document source, IngridDocument target) {
		NodeList mediumOptions = XPathUtils.getNodeList(source, X_ADDITIONAL_MEDIUM_OPTION_LIST);
		List<IngridDocument> mediumOptionList = new ArrayList<IngridDocument>();

		for (int index = 0; index < mediumOptions.getLength(); index++) {
			Node mediumOption = mediumOptions.item(index);
			IngridDocument mediumOptionDoc = new IngridDocument();
			putInt(MdekKeys.MEDIUM_NAME, XPathUtils.getInt(mediumOption, X_ADDITIONAL_MEDIUM_OPTION_NAME), mediumOptionDoc);
			putString(MdekKeys.MEDIUM_NOTE, XPathUtils.getString(mediumOption, X_ADDITIONAL_MEDIUM_OPTION_NOTE), mediumOptionDoc);
			putDouble(MdekKeys.MEDIUM_TRANSFER_SIZE, XPathUtils.getDouble(mediumOption, X_ADDITIONAL_MEDIUM_OPTION_TRANSFER_SIZE), mediumOptionDoc);

			mediumOptionList.add(mediumOptionDoc);
		}

		putDocList(MdekKeys.MEDIUM_OPTIONS, mediumOptionList, target);
	}

	private static void mapDataFormats(Document source, IngridDocument target) {
		NodeList dataFormats = XPathUtils.getNodeList(source, X_ADDITIONAL_DATA_FORMAT_LIST);
		List<IngridDocument> dataFormatList = new ArrayList<IngridDocument>();

		for (int index = 0; index < dataFormats.getLength(); index++) {
			Node dataFormat = dataFormats.item(index);
			IngridDocument dataFormatDoc = new IngridDocument();
			putString(MdekKeys.FORMAT_NAME, XPathUtils.getString(dataFormat, X_ADDITIONAL_DATA_FORMAT_NAME), dataFormatDoc);
			putInt(MdekKeys.FORMAT_NAME_KEY, XPathUtils.getInt(dataFormat, X_ADDITIONAL_DATA_FORMAT_NAME_KEY), dataFormatDoc);
			putString(MdekKeys.FORMAT_VERSION, XPathUtils.getString(dataFormat, X_ADDITIONAL_DATA_FORMAT_VERSION), dataFormatDoc);
			putString(MdekKeys.FORMAT_SPECIFICATION, XPathUtils.getString(dataFormat, X_ADDITIONAL_DATA_FORMAT_SPECIFICATION), dataFormatDoc);
			putString(MdekKeys.FORMAT_FILE_DECOMPRESSION_TECHNIQUE, XPathUtils.getString(dataFormat, X_ADDITIONAL_DATA_FORMAT_FILE_DECOMPRESSION_TECHNIQUE), dataFormatDoc);

			dataFormatList.add(dataFormatDoc);
		}

		putDocList(MdekKeys.DATA_FORMATS, dataFormatList, target);
	}

	private static void mapComments(Document source, IngridDocument target) {
		NodeList comments = XPathUtils.getNodeList(source, X_ADDITIONAL_COMMENT_LIST);
		List<IngridDocument> commentList = new ArrayList<IngridDocument>();

		for (int index = 0; index < comments.getLength(); index++) {
			Node comment = comments.item(index);
			IngridDocument commentDoc = new IngridDocument();
			putString(MdekKeys.COMMENT, XPathUtils.getString(comment, X_ADDITIONAL_COMMENT_CONTENT), commentDoc);
			putString(new String[] {MdekKeys.CREATE_USER, MdekKeys.UUID}, XPathUtils.getString(comment, X_ADDITIONAL_COMMENT_CREATOR), commentDoc);
			putString(MdekKeys.CREATE_TIME, XPathUtils.getString(comment, X_ADDITIONAL_COMMENT_DATE_OF_CREATION), commentDoc);

			commentList.add(commentDoc);
		}

		putDocList(MdekKeys.COMMENT_LIST, commentList, target);
	}

	private static void mapConformities(Document source, IngridDocument target) {
		NodeList conformities = XPathUtils.getNodeList(source, X_ADDITIONAL_CONFORMITY_LIST);
		List<IngridDocument> conformityList = new ArrayList<IngridDocument>();

		for (int index = 0; index < conformities.getLength(); index++) {
			Node conformity = conformities.item(index);
			IngridDocument conformityDoc = new IngridDocument();
			// TODO: CHANGED IN VERSION 3.2.0. !!! Fix if import of older formats needed !
//			putString(MdekKeys.CONFORMITY_SPECIFICATION, XPathUtils.getString(conformity, X_ADDITIONAL_CONFORMITY_SPECIFICATION), conformityDoc);
			putString(MdekKeys.CONFORMITY_DEGREE_VALUE, XPathUtils.getString(conformity, X_ADDITIONAL_CONFORMITY_DEGREE), conformityDoc);
			putInt(MdekKeys.CONFORMITY_DEGREE_KEY, XPathUtils.getInt(conformity, X_ADDITIONAL_CONFORMITY_DEGREE_KEY), conformityDoc);
			// TODO: REMOVED IN VERSION 3.2.0. !!! Fix if import of older formats needed !
//			putString(MdekKeys.CONFORMITY_PUBLICATION_DATE, XPathUtils.getString(conformity, X_ADDITIONAL_CONFORMITY_PUBLICATION_DATE), conformityDoc);

			conformityList.add(conformityDoc);
		}

		putDocList(MdekKeys.CONFORMITY_LIST, conformityList, target);
	}

	private static void mapSpatialDomain(Document source, IngridDocument target) {
		putString(MdekKeys.DESCRIPTION_OF_SPATIAL_DOMAIN,
				XPathUtils.getString(source, X_SPATIAL_DESCRIPTION), target);
		putDouble(MdekKeys.VERTICAL_EXTENT_MAXIMUM,
				XPathUtils.getDouble(source, X_SPATIAL_VERTICAL_EXTENT_MAXIMUM), target);
		putDouble(MdekKeys.VERTICAL_EXTENT_MINIMUM,
				XPathUtils.getDouble(source, X_SPATIAL_VERTICAL_EXTENT_MINIMUM), target);
		putInt(MdekKeys.VERTICAL_EXTENT_UNIT,
				XPathUtils.getInt(source, X_SPATIAL_VERTICAL_EXTENT_UNIT), target);
		// TODO: REMOVED IN VERSION 3.0.0. !!! Fix if import of older formats needed !
//		putInt(MdekKeys.VERTICAL_EXTENT_VDATUM, XPathUtils.getInt(source, X_SPATIAL_VERTICAL_EXTENT_VDATUM), target);
		mapGeoLocations(source, target);
	}

	private static void mapGeoLocations(Document source, IngridDocument target) {
		NodeList geoLocations = XPathUtils.getNodeList(source, X_SPATIAL_GEO_LIST);
		List<IngridDocument> geoLocationList = new ArrayList<IngridDocument>();

		for (int index = 0; index < geoLocations.getLength(); index++) {
			Node geoLocation = geoLocations.item(index);
			IngridDocument geoLocationDoc = new IngridDocument();
			if (XPathUtils.nodeExists(geoLocation, X_SPATIAL_GEO_CONTROLLED)) {
				mapControlledLocation(XPathUtils.getNode(geoLocation, X_SPATIAL_GEO_CONTROLLED), geoLocationDoc);

			} else if (XPathUtils.nodeExists(geoLocation, X_SPATIAL_GEO_UNCONTROLLED)) {
				mapUncontrolledLocation(XPathUtils.getNode(geoLocation, X_SPATIAL_GEO_UNCONTROLLED), geoLocationDoc);
			}
			putDouble(MdekKeys.WEST_BOUNDING_COORDINATE,
					XPathUtils.getDouble(geoLocation, X_SPATIAL_GEO_BOUND_WEST), geoLocationDoc);
			putDouble(MdekKeys.EAST_BOUNDING_COORDINATE,
					XPathUtils.getDouble(geoLocation, X_SPATIAL_GEO_BOUND_EAST), geoLocationDoc);
			putDouble(MdekKeys.NORTH_BOUNDING_COORDINATE,
					XPathUtils.getDouble(geoLocation, X_SPATIAL_GEO_BOUND_NORTH), geoLocationDoc);
			putDouble(MdekKeys.SOUTH_BOUNDING_COORDINATE,
					XPathUtils.getDouble(geoLocation, X_SPATIAL_GEO_BOUND_SOUTH), geoLocationDoc);

			geoLocationList.add(geoLocationDoc);
		}

		putDocList(MdekKeys.LOCATIONS, geoLocationList, target);
	}

	private static void mapControlledLocation(Node locationContext, IngridDocument locationDoc) {
		if (XPathUtils.getString(locationContext, X_SPATIAL_GEO_LOCATION_NAME) != null) {
			putString(MdekKeys.LOCATION_NAME,
					XPathUtils.getString(locationContext, X_SPATIAL_GEO_LOCATION_NAME), locationDoc);
		} else {
			putString(MdekKeys.LOCATION_NAME, "", locationDoc);
		}
		putString(MdekKeys.LOCATION_SNS_ID,
				XPathUtils.getString(locationContext, X_SPATIAL_GEO_LOCATION_NAME_KEY), locationDoc);
		putString(MdekKeys.LOCATION_TYPE,
				MdekUtils.SpatialReferenceType.GEO_THESAURUS.getDbValue(), locationDoc);
		putString(MdekKeys.LOCATION_CODE,
				XPathUtils.getString(locationContext, X_SPATIAL_GEO_LOCATION_CODE), locationDoc);
		putString(MdekKeys.SNS_TOPIC_TYPE,
				XPathUtils.getString(locationContext, X_SPATIAL_GEO_TOPIC_TYPE), locationDoc);
	}

	private static void mapUncontrolledLocation(Node locationContext, IngridDocument locationDoc) {
		putString(MdekKeys.LOCATION_NAME,
				XPathUtils.getString(locationContext, X_SPATIAL_GEO_LOCATION_NAME), locationDoc);
		putInt(MdekKeys.LOCATION_NAME_KEY,
				XPathUtils.getInt(locationContext, X_SPATIAL_GEO_LOCATION_NAME_KEY), locationDoc);
		putString(MdekKeys.LOCATION_TYPE,
				MdekUtils.SpatialReferenceType.FREI.getDbValue(), locationDoc);
	}

	private static void mapTemporalDomain(Document source, IngridDocument target) {
		putString(MdekKeys.DESCRIPTION_OF_TEMPORAL_DOMAIN,
				XPathUtils.getString(source, X_TEMPORAL_DESCRIPTION), target);
		putString(MdekKeys.BEGINNING_DATE,
				XPathUtils.getString(source, X_TEMPORAL_BEGINNING_DATE), target);
		putString(MdekKeys.ENDING_DATE,
				XPathUtils.getString(source, X_TEMPORAL_ENDING_DATE), target);
		putString(MdekKeys.TIME_STEP,
				XPathUtils.getString(source, X_TEMPORAL_TIME_STEP), target);
		putString(MdekKeys.TIME_SCALE,
				XPathUtils.getString(source, X_TEMPORAL_TIME_SCALE), target);
		putInt(MdekKeys.TIME_PERIOD,
				XPathUtils.getInt(source, X_TEMPORAL_TIME_PERIOD), target);
		putInt(MdekKeys.TIME_STATUS,
				XPathUtils.getInt(source, X_TEMPORAL_TIME_STATUS), target);
		putString(MdekKeys.TIME_TYPE,
				XPathUtils.getString(source, X_TEMPORAL_TIME_TYPE), target);
		mapDatasetReferences(source, target);
	}

	private static void mapDatasetReferences(Document source, IngridDocument target) {
		NodeList datasetReferences = XPathUtils.getNodeList(source, X_TEMPORAL_DATASET_REFERENCE_LIST);
		List<IngridDocument> datasetReferenceList = new ArrayList<IngridDocument>();

		for (int index = 0; index < datasetReferences.getLength(); index++) {
			Node datasetReference = datasetReferences.item(index);
			IngridDocument datasetReferenceDoc = new IngridDocument();
			putString(MdekKeys.DATASET_REFERENCE_DATE,
					XPathUtils.getString(datasetReference, X_TEMPORAL_DATASET_REFERENCE_DATE), datasetReferenceDoc);
			putInt(MdekKeys.DATASET_REFERENCE_TYPE,
					XPathUtils.getInt(datasetReference, X_TEMPORAL_DATASET_REFERENCE_TYPE), datasetReferenceDoc);

			datasetReferenceList.add(datasetReferenceDoc);
		}

		putDocList(MdekKeys.DATASET_REFERENCES, datasetReferenceList, target);
	}

	private static void mapSubjectTerms(Document source, IngridDocument target) {
		Node subjectTermsNode = XPathUtils.getNode(source, X_SUBJECT_TERMS);
		if (subjectTermsNode != null) {
			mapSubjectTerms(subjectTermsNode.getChildNodes(), target);			
		}
	}

	private static void mapAvailableLinkages(Document source, IngridDocument target) {
		NodeList linkages = XPathUtils.getNodeList(source, X_LINK_LIST);
		List<IngridDocument> linkagesList = new ArrayList<IngridDocument>();

		for (int index = 0; index < linkages.getLength(); index++) {
			Node linkage = linkages.item(index);
			IngridDocument linkageDoc = new IngridDocument();
			putString(MdekKeys.LINKAGE_NAME, XPathUtils.getString(linkage, X_LINK_NAME), linkageDoc);
			putString(MdekKeys.LINKAGE_URL, XPathUtils.getString(linkage, X_LINK_URL), linkageDoc);
			putInt(MdekKeys.LINKAGE_URL_TYPE, XPathUtils.getInt(linkage, X_LINK_URL_TYPE), linkageDoc);
			putString(MdekKeys.LINKAGE_REFERENCE, XPathUtils.getString(linkage, X_LINK_REFERENCE), linkageDoc);
			putInt(MdekKeys.LINKAGE_REFERENCE_ID, XPathUtils.getInt(linkage, X_LINK_REFERENCE_KEY), linkageDoc);
			putString(MdekKeys.LINKAGE_DESCRIPTION, XPathUtils.getString(linkage, X_LINK_DESCRIPTION), linkageDoc);

			linkagesList.add(linkageDoc);
		}

		putDocList(MdekKeys.LINKAGES, linkagesList, target);
	}

	private static void mapParentDataSource(Document source, IngridDocument target) {
		putString(MdekKeys.PARENT_UUID, XPathUtils.getString(source, X_PARENT_IDENTIFIER), target);
	}

	private static void mapRelatedAddresses(Document source, IngridDocument target) {
		NodeList relatedAddresses = XPathUtils.getNodeList(source, X_RELATED_ADDRESS_LIST);
		List<IngridDocument> relatedAddressList = new ArrayList<IngridDocument>();

		for (int index = 0; index < relatedAddresses.getLength(); index++) {
			Node relatedAddress = relatedAddresses.item(index);
			IngridDocument relatedAddressDoc = new IngridDocument();
			putString(MdekKeys.RELATION_TYPE_NAME, XPathUtils.getString(relatedAddress, X_RELATED_ADDRESS_TYPE_OF_RELATION), relatedAddressDoc);
			putInt(MdekKeys.RELATION_TYPE_REF, XPathUtils.getInt(relatedAddress, X_RELATED_ADDRESS_TYPE_OF_RELATION_LIST_KEY), relatedAddressDoc);
			putInt(MdekKeys.RELATION_TYPE_ID, XPathUtils.getInt(relatedAddress, X_RELATED_ADDRESS_TYPE_OF_RELATION_ENTRY_KEY), relatedAddressDoc);
			putString(MdekKeys.UUID, XPathUtils.getString(relatedAddress, X_RELATED_ADDRESS_IDENTIFIER), relatedAddressDoc);
			putString(MdekKeys.RELATION_DATE_OF_LAST_MODIFICATION, XPathUtils.getString(relatedAddress, X_RELATED_ADDRESS_DATE_OF_LAST_MODIFICATION), relatedAddressDoc);

			relatedAddressList.add(relatedAddressDoc);
		}

		putDocList(MdekKeys.ADR_REFERENCES_TO, relatedAddressList, target);
	}

	private static void mapLinkDataSources(Document source, IngridDocument target) {
		NodeList relatedDatasources = XPathUtils.getNodeList(source, X_RELATED_DS_LIST);
		List<IngridDocument> relatedDSList = new ArrayList<IngridDocument>();

		for (int index = 0; index < relatedDatasources.getLength(); index++) {
			Node relatedDS = relatedDatasources.item(index);
			IngridDocument relatedDSDoc = new IngridDocument();
			putString(MdekKeys.RELATION_TYPE_NAME, XPathUtils.getString(relatedDS, X_RELATED_DS_TYPE), relatedDSDoc);
			putInt(MdekKeys.RELATION_TYPE_REF, XPathUtils.getInt(relatedDS, X_RELATED_DS_TYPE_KEY), relatedDSDoc);
			putString(MdekKeys.RELATION_DESCRIPTION, XPathUtils.getString(relatedDS, X_RELATED_DS_DESCRIPTION), relatedDSDoc);
			putString(MdekKeys.UUID, XPathUtils.getString(relatedDS, X_RELATED_DS_IDENTIFIER), relatedDSDoc);

			relatedDSList.add(relatedDSDoc);
		}

		putDocList(MdekKeys.OBJ_REFERENCES_TO, relatedDSList, target);
	}


	
	private static void mapIntList(Object source, String xPathExpression, IngridDocument target, String[] keyPath) {
		if (keyPath.length == 1) {
			mapIntList(source, xPathExpression, target, keyPath[0]);

		} else {
			IngridDocument containerDoc = getOrCreateNew(keyPath[0], target);
			mapIntList(source, xPathExpression, containerDoc, Arrays.copyOfRange(keyPath, 1, keyPath.length));
		}
	}

	private static void mapIntList(Object source, String xPathExpression, IngridDocument target, String key) {
		NodeList nodeList = XPathUtils.getNodeList(source, xPathExpression);
		List<Integer> targetList = new ArrayList<Integer>();

		for (int index = 0; index < nodeList.getLength(); ++index) {
			Node item = nodeList.item(index);
			targetList.add(Integer.valueOf(item.getTextContent()));
		}

		putIntList(key, targetList, target);
	}

	private static void mapStringList(Object source, String xPathExpression, IngridDocument target, String[] keyPath) {
		if (keyPath.length == 1) {
			mapStringList(source, xPathExpression, target, keyPath[0]);

		} else {
			IngridDocument containerDoc = getOrCreateNew(keyPath[0], target);
			mapStringList(source, xPathExpression, containerDoc, Arrays.copyOfRange(keyPath, 1, keyPath.length));
		}
	}

	private static void mapStringList(Object source, String xPathExpression, IngridDocument target, String key) {
		NodeList nodeList = XPathUtils.getNodeList(source, xPathExpression);
		List<String> targetList = new ArrayList<String>();

		for (int index = 0; index < nodeList.getLength(); ++index) {
			Node item = nodeList.item(index);
			targetList.add(item.getTextContent());
		}

		putStringList(key, targetList, target);
	}
}
