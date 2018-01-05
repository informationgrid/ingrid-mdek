/*
 * **************************************************-
 * ingrid-import-export
 * ==================================================
 * Copyright (C) 2014 - 2018 wemove digital solutions GmbH
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
package de.ingrid.mdek.xml.importer.mapper.version404;

import static de.ingrid.mdek.xml.util.IngridDocUtils.getOrCreateNew;
import static de.ingrid.mdek.xml.util.IngridDocUtils.putDocList;
import static de.ingrid.mdek.xml.util.IngridDocUtils.putDouble;
import static de.ingrid.mdek.xml.util.IngridDocUtils.putInt;
import static de.ingrid.mdek.xml.util.IngridDocUtils.putIntList;
import static de.ingrid.mdek.xml.util.IngridDocUtils.putLong;
import static de.ingrid.mdek.xml.util.IngridDocUtils.putString;
import static de.ingrid.mdek.xml.util.IngridDocUtils.putStringList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.xml.importer.mapper.AbstractXMLToDocMapper;
import de.ingrid.mdek.xml.util.XPathUtils;
import de.ingrid.utils.IngridDocument;

public class XMLDatasourceToDocMapper extends AbstractXMLToDocMapper {
	
	private static final String X_DATA_SOURCE = "//data-source-instance";

	private static final String X_WORK_STATE = X_DATA_SOURCE + "/@work-state";
	private static final String X_OBJECT_IDENTIFIER = X_DATA_SOURCE + "/general/object-identifier/text()";
	private static final String X_CATALOGUE_IDENTIFIER = X_DATA_SOURCE + "/general/catalogue-identifier/text()";
	private static final String X_MODIFICATOR_IDENTIFIER = X_DATA_SOURCE + "/general/modificator-identifier/text()";
	private static final String X_RESPONSIBLE_IDENTIFIER = X_DATA_SOURCE + "/general/responsible-identifier/text()";
	private static final String X_OBJECT_CLASS = X_DATA_SOURCE + "/general/object-class/@id";
	private static final String X_TITLE = X_DATA_SOURCE + "/general/title/text()";
	private static final String X_ABSTRACT = X_DATA_SOURCE + "/general/abstract/text()";
	private static final String X_DATE_OF_LAST_MODIFICATION = X_DATA_SOURCE + "/general/date-of-last-modification/text()";
	private static final String X_DATE_OF_CREATION = X_DATA_SOURCE + "/general/date-of-creation/text()";
	private static final String X_ORIGINAL_CONTROL_IDENTIFIER = X_DATA_SOURCE + "/general/original-control-identifier/text()";
	private static final String X_GENERAL_ADDITIONAL_VALUE_LIST = X_DATA_SOURCE + "/general/general-additional-values/general-additional-value";
	private static final String X_GENERAL_ADDITIONAL_VALUE_ID = "@id";
	private static final String X_GENERAL_ADDITIONAL_VALUE_LINE = "@line";
	private static final String X_GENERAL_ADDITIONAL_VALUE_FIELD_KEY = "field-key";
	private static final String X_GENERAL_ADDITIONAL_VALUE_FIELD_DATA = "field-data";
	private static final String X_GENERAL_ADDITIONAL_VALUE_FIELD_KEY_PARENT = "field-key-parent";
	private static final String X_METADATA_STANDARD_NAME = X_DATA_SOURCE + "/general/metadata/metadata-standard-name/text()";
	private static final String X_METADATA_STANDARD_VERSION = X_DATA_SOURCE + "/general/metadata/metadata-standard-version/text()";
	private static final String X_METADATA_CHARACTER_SET = X_DATA_SOURCE + "/general/metadata/metadata-character-set/@iso-code";
	private static final String X_DATASET_ALTERNATE_NAME = X_DATA_SOURCE + "/general/dataset-alternate-name/text()";
	private static final String X_DATASET_CHARACTER_SET = X_DATA_SOURCE + "/general/dataset-character-set/@iso-code";
	private static final String X_TOPIC_CATEGORIES = X_DATA_SOURCE + "/general/topic-categories/topic-category/@id";
	private static final String X_IS_CATALOG = X_DATA_SOURCE + "/general/env-information/is-catalog/text()";
	private static final String X_ENV_TOPICS = X_DATA_SOURCE + "/general/env-information/env-topic/@id";
	private static final String X_IS_INSPIRE_RELEVANT = X_DATA_SOURCE + "/general/is-inspire-relevant/text()";
	private static final String X_IS_ADV_COMPATIBLE = X_DATA_SOURCE + "/general/is-adv-compatible/text()";
	private static final String X_ADV_PRODUCT_GROUP_ITEMS = X_DATA_SOURCE + "/general/adv-product-group/item/text()";
	private static final String X_IS_OPEN_DATA = X_DATA_SOURCE + "/general/is-open-data/text()";
	private static final String X_OPEN_DATA_CATEGORIES = X_DATA_SOURCE + "/general/open-data-categories/open-data-category";
	private static final String X_TECHNICAL_DOMAIN_DATASET = X_DATA_SOURCE + "/technical-domain/dataset";
	private static final String X_TECHNICAL_DOMAIN_SERVICE = X_DATA_SOURCE + "/technical-domain/service";
	private static final String X_TECHNICAL_DOMAIN_DOCUMENT = X_DATA_SOURCE + "/technical-domain/document";
	private static final String X_TECHNICAL_DOMAIN_MAP = X_DATA_SOURCE + "/technical-domain/map";
	private static final String X_TECHNICAL_DOMAIN_PROJECT = X_DATA_SOURCE + "/technical-domain/project";
	private static final String X_TECHNICAL_DOMAIN_DESCRIPTION_OF_TECH_DOMAIN = "description-of-tech-domain";
	private static final String X_DATASET_PARAMETER_LIST = "dataset-parameter";
	private static final String X_DATASET_PARAMETER_PARAMETER = "parameter";
	private static final String X_DATASET_PARAMETER_SUPPLEMENTARY_INFORMATION = "supplementary-information";
	private static final String X_DATASET_METHOD = "method";
	private static final String X_SERVICE_TYPE_KEY = "service-type/@id";
	private static final String X_SERVICE_TYPE = "service-type/text()";
	private static final String X_COUPLING_TYPE = "coupling-type/text()";
	private static final String X_SERVICE_CLASSIFICATION_LIST = "service-classification";
	private static final String X_PUBLICATION_SCALE_LIST = "publication-scale";
	private static final String X_PUBLICATION_SCALE_SCALE = "scale";
	private static final String X_PUBLICATION_SCALE_RES_GROUND = "resolution-ground";
	private static final String X_PUBLICATION_SCALE_RES_SCAN = "resolution-scan";
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
	private static final String X_SERVICE_PLATFORM = "text()";
	private static final String X_SERVICE_PLATFORM_KEY = "@id";
	private static final String X_SERVICE_CONNECTION_POINT_LIST = "connection-point";
	private static final String X_SERVICE_OPERATION_PARAMETER_LIST = "parameter-of-operation";
	private static final String X_SERVICE_OPERATION_PARAMETER_NAME = "name/text()";
	private static final String X_SERVICE_OPERATION_PARAMETER_OPTIONAL = "optional/text()";
	private static final String X_SERVICE_OPERATION_PARAMETER_REPEATABILITY = "repeatability/text()";
	private static final String X_SERVICE_OPERATION_PARAMETER_DIRECTION = "direction/text()";
	private static final String X_SERVICE_OPERATION_PARAMETER_DESCRIPTION_OF_PARAMETER = "description-of-parameter/text()";
	private static final String X_SERVICE_DEPENDS_ON_LIST = "depends-on";
	private static final String X_SERVICE_HAS_ACCESS_CONSTRAINT = "has-access-constraint/text()";
	private static final String X_SERVICE_HAS_ATOM_DOWNLOAD = "has-atom-download/text()";
	private static final String X_SERVICE_NAME = "name/text()";
	private static final String X_SERVICE_URL_LIST = "service-url";
	private static final String X_SERVICE_URL = "url/text()";
	private static final String X_SERVICE_URL_DESCRIPTION = "description/text()";
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
	private static final String X_MAP_AND_DATASET_KEY_CATALOGUE_LIST = "key-catalogue";
	private static final String X_MAP_AND_DATASET_KEY_CATALOGUE = "key-cat/text()";
	private static final String X_MAP_AND_DATASET_KEY_CATALOGUE_KEY = "key-cat/@id";
	private static final String X_MAP_AND_DATASET_KEY_CATALOGUE_DATE = "key-date/text()";
	private static final String X_MAP_AND_DATASET_KEY_CATALOGUE_EDITION = "edition/text()";
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
	private static final String X_MAP_GEO_GRID_TRANSFORM_PARAM = "grid-format/grid-transform-param";
	private static final String X_MAP_GEO_GRID_NUM_DIMENSIONS = "grid-format/grid-num-dimensions";
	private static final String X_MAP_GEO_GRID_AXIS_NAME = "grid-format/grid-axis-name";
	private static final String X_MAP_GEO_GRID_AXIS_SIZE = "grid-format/grid-axis-size";
	private static final String X_MAP_GEO_GRID_CELL_GEOMETRY = "grid-format/grid-cell-geometry";
	private static final String X_MAP_GEO_GRID_GEO_RECTIFIED = "grid-format/grid-geo-rectified";
	private static final String X_MAP_GEO_GRID_RECT_CHECKPOINT = "grid-format/grid-rect-checkpoint";
	private static final String X_MAP_GEO_GRID_RECT_DESCRIPTION = "grid-format/grid-rect-description";
	private static final String X_MAP_GEO_GRID_RECT_CORNER_POINT = "grid-format/grid-rect-corner-point";
	private static final String X_MAP_GEO_GRID_RECT_POINT_IN_PIXEL = "grid-format/grid-rect-point-in-pixel";
	private static final String X_MAP_GEO_GRID_REF_CONTROL_POINT = "grid-format/grid-ref-control-point";
	private static final String X_MAP_GEO_GRID_REF_ORIENTATION_PARAM = "grid-format/grid-ref-orientation-param";
	private static final String X_MAP_GEO_GRID_REF_PARAMETER = "grid-format/grid-ref-referenced-param";
	private static final String X_MAP_GEO_VECTOR_OBJECT_COUNT = "geometric-object-count/text()";
	private static final String X_MAP_GEO_VECTOR_OBJECT_TYPE = "geometric-object-type/@iso-code";
	private static final String X_MAP_POS_ACCURACY_VERTICAL = "pos-accuracy-vertical/text()";
	private static final String X_MAP_KEYC_INCL_W_DATASET = "keyc-incl-w-dataset/text()";
	private static final String X_MAP_FEATURE_TYPE_LIST = "feature-type";
	private static final String X_MAP_DATASOURCE_IDENTIFICATOR = "datasource-identificator/text()";
	private static final String X_PROJECT_MEMBER_DESCRIPTION = "member-description/text()";
	private static final String X_PROJECT_LEADER_DESCRIPTION = "leader-description/text()";
    private static final String X_ADDITIONAL_DATA_LANGUAGE_LIST = X_DATA_SOURCE + "/additional-information/data-language";
	private static final String X_ADDITIONAL_METADATA_LANGUAGE = X_DATA_SOURCE + "/additional-information/metadata-language/text()";
	private static final String X_ADDITIONAL_METADATA_LANGUAGE_KEY = X_DATA_SOURCE + "/additional-information/metadata-language/@id";
	private static final String X_ADDITIONAL_EXPORT_TO_LIST = X_DATA_SOURCE + "/additional-information/export-to";
	private static final String X_ADDITIONAL_LEGISLATION_LIST = X_DATA_SOURCE + "/additional-information/legislation";
	private static final String X_ADDITIONAL_DATASET_INTENTIONS = X_DATA_SOURCE + "/additional-information/dataset-intentions/text()";
	private static final String X_ADDITIONAL_ACCESS_CONSTRAINT_LIST = X_DATA_SOURCE + "/additional-information/access-constraint";
	private static final String X_ADDITIONAL_ACCESS_CONSTRAINT_RESTRICTION = "restriction/text()";
	private static final String X_ADDITIONAL_ACCESS_CONSTRAINT_RESTRICTION_KEY = "restriction/@id";
	private static final String X_ADDITIONAL_USE_LIMITATION_LIST = X_DATA_SOURCE + "/additional-information/use-limitation";
	private static final String X_ADDITIONAL_USE_LIMITATION_TERMS_OF_USE = "terms-of-use/text()";
	private static final String X_ADDITIONAL_USE_LIMITATION_TERMS_OF_USE_KEY = "terms-of-use/@id";
    private static final String X_ADDITIONAL_USE_CONSTRAINT_LIST = X_DATA_SOURCE + "/additional-information/use-constraint";
    private static final String X_ADDITIONAL_USE_CONSTRAINT_LICENSE = "license/text()";
    private static final String X_ADDITIONAL_USE_CONSTRAINT_LICENSE_KEY = "license/@id";
	private static final String X_ADDITIONAL_MEDIUM_OPTION_LIST = X_DATA_SOURCE + "/additional-information/medium-option";
	private static final String X_ADDITIONAL_MEDIUM_OPTION_NAME = "medium-name/@iso-code";
	private static final String X_ADDITIONAL_MEDIUM_OPTION_NOTE = "medium-note/text()";
	private static final String X_ADDITIONAL_MEDIUM_OPTION_TRANSFER_SIZE = "transfer-size/text()";
	private static final String X_ADDITIONAL_DATA_FORMAT_LIST = X_DATA_SOURCE + "/additional-information/data-format";
	private static final String X_ADDITIONAL_DATA_FORMAT_NAME = "format-name/text()";
	private static final String X_ADDITIONAL_DATA_FORMAT_NAME_KEY = "format-name/@id";
	private static final String X_ADDITIONAL_DATA_FORMAT_VERSION = "version/text()";
	private static final String X_ADDITIONAL_DATA_FORMAT_SPECIFICATION = "specification/text()";
	private static final String X_ADDITIONAL_DATA_FORMAT_FILE_DECOMPRESSION_TECHNIQUE = "file-decompression-technique/text()";
	private static final String X_ADDITIONAL_DATA_FORMAT_INSPIRE_LIST = X_DATA_SOURCE + "/additional-information/data-format-inspire";
	private static final String X_ADDITIONAL_DATA_FORMAT_INSPIRE_NAME = "format-inspire-name/text()";
	private static final String X_ADDITIONAL_DATA_FORMAT_INSPIRE_NAME_KEY = "format-inspire-name/@id";
	private static final String X_ADDITIONAL_PUBLICATION_CONDITION = X_DATA_SOURCE + "/additional-information/publication-condition/text()";
	private static final String X_ADDITIONAL_DATASET_USAGE = X_DATA_SOURCE + "/additional-information/dataset-usage/text()";
	private static final String X_ADDITIONAL_ORDERING_INSTRUCTION = X_DATA_SOURCE + "/additional-information/ordering-instructions/text()";
	private static final String X_ADDITIONAL_COMMENT_LIST = X_DATA_SOURCE + "/additional-information/comment";
	private static final String X_ADDITIONAL_COMMENT_CONTENT = "comment-content/text()";
	private static final String X_ADDITIONAL_COMMENT_CREATOR = "creator-identifier/text()";
	private static final String X_ADDITIONAL_COMMENT_DATE_OF_CREATION = "date-of-creation/text()";
	private static final String X_ADDITIONAL_CONFORMITY_LIST = X_DATA_SOURCE + "/additional-information/conformity";
	private static final String X_ADDITIONAL_CONFORMITY_SPECIFICATION = "conformity-specification/text()";
	private static final String X_ADDITIONAL_CONFORMITY_SPECIFICATION_KEY = "conformity-specification/@id";
	private static final String X_ADDITIONAL_CONFORMITY_DEGREE = "conformity-degree/text()";
	private static final String X_ADDITIONAL_CONFORMITY_DEGREE_KEY = "conformity-degree/@id";
	private static final String X_ADDITIONAL_DQ_LIST = X_DATA_SOURCE + "/additional-information/data-quality";
	private static final String X_ADDITIONAL_DQ_ELEMENT_ID = "dq-element-id/text()";
	private static final String X_ADDITIONAL_DQ_NAME_OF_MEASURE_KEY = "dq-name-of-measure/@id";
	private static final String X_ADDITIONAL_DQ_NAME_OF_MEASURE_VALUE = "dq-name-of-measure/text()";
	private static final String X_ADDITIONAL_DQ_RESULT_VALUE = "dq-result-value/text()";
	private static final String X_ADDITIONAL_DQ_MEASURE_DESCRIPTION = "dq-measure-description/text()";
	private static final String X_SPATIAL_COORDINATE_SYSTEM_LIST = X_DATA_SOURCE + "/spatial-domain/coordinate-system";
	private static final String X_SPATIAL_DESCRIPTION = X_DATA_SOURCE + "/spatial-domain/description-of-spatial-domain/text()";
	private static final String X_SPATIAL_VERTICAL_EXTENT_MINIMUM = X_DATA_SOURCE + "/spatial-domain/vertical-extent/vertical-extent-minimum/text()";
	private static final String X_SPATIAL_VERTICAL_EXTENT_MAXIMUM = X_DATA_SOURCE + "/spatial-domain/vertical-extent/vertical-extent-maximum/text()";
	private static final String X_SPATIAL_VERTICAL_EXTENT_UNIT = X_DATA_SOURCE + "/spatial-domain/vertical-extent/vertical-extent-unit/@id";
	private static final String X_SPATIAL_VERTICAL_EXTENT_VDATUM_VALUE = X_DATA_SOURCE + "/spatial-domain/vertical-extent/vertical-extent-vdatum/text()";
	private static final String X_SPATIAL_VERTICAL_EXTENT_VDATUM_KEY = X_DATA_SOURCE + "/spatial-domain/vertical-extent/vertical-extent-vdatum/@id";
	private static final String X_SPATIAL_GEO_LIST = X_DATA_SOURCE + "/spatial-domain/geo-location";
	private static final String X_SPATIAL_GEO_CONTROLLED = "controlled-location";
	private static final String X_SPATIAL_GEO_UNCONTROLLED = "uncontrolled-location";
	private static final String X_SPATIAL_GEO_LOCATION_NAME = "location-name/text()";
	private static final String X_SPATIAL_GEO_LOCATION_NAME_KEY = "location-name/@id";
	private static final String X_SPATIAL_GEO_TOPIC_TYPE = "topic-type/text()";
	private static final String X_SPATIAL_GEO_LOCATION_CODE = "location-code/text()";
	private static final String X_SPATIAL_GEO_BOUND_WEST = "bounding-coordinates/west-bounding-coordinate/text()";
	private static final String X_SPATIAL_GEO_BOUND_EAST = "bounding-coordinates/east-bounding-coordinate/text()";
	private static final String X_SPATIAL_GEO_BOUND_NORTH = "bounding-coordinates/north-bounding-coordinate/text()";
	private static final String X_SPATIAL_GEO_BOUND_SOUTH = "bounding-coordinates/south-bounding-coordinate/text()";
	private static final String X_TEMPORAL_DESCRIPTION = X_DATA_SOURCE + "/temporal-domain/description-of-temporal-domain/text()";
	private static final String X_TEMPORAL_BEGINNING_DATE = X_DATA_SOURCE + "/temporal-domain/beginning-date/text()";
	private static final String X_TEMPORAL_ENDING_DATE = X_DATA_SOURCE + "/temporal-domain/ending-date/text()";
	private static final String X_TEMPORAL_TIME_STEP = X_DATA_SOURCE + "/temporal-domain/time-step/text()";
	private static final String X_TEMPORAL_TIME_SCALE = X_DATA_SOURCE + "/temporal-domain/time-scale/text()";
	private static final String X_TEMPORAL_TIME_PERIOD = X_DATA_SOURCE + "/temporal-domain/time-period/@iso-code";
	private static final String X_TEMPORAL_TIME_STATUS = X_DATA_SOURCE + "/temporal-domain/time-status/@iso-code";
	private static final String X_TEMPORAL_TIME_TYPE = X_DATA_SOURCE + "/temporal-domain/time-type/text()";
	private static final String X_TEMPORAL_DATASET_REFERENCE_LIST = X_DATA_SOURCE + "/temporal-domain/dataset-reference";
	private static final String X_TEMPORAL_DATASET_REFERENCE_DATE = "dataset-reference-date/text()";
	private static final String X_TEMPORAL_DATASET_REFERENCE_TYPE = "dataset-reference-type/@iso-code";
	private static final String X_SUBJECT_TERMS = X_DATA_SOURCE + "/subject-terms";
	private static final String X_LINK_LIST = X_DATA_SOURCE + "/available-linkage";
	private static final String X_LINK_NAME = "linkage-name/text()";
	private static final String X_LINK_URL = "linkage-url/text()";
	private static final String X_LINK_URL_TYPE = "linkage-url-type/text()";
	private static final String X_LINK_REFERENCE = "linkage-reference/text()";
	private static final String X_LINK_REFERENCE_KEY = "linkage-reference/@id";
	private static final String X_LINK_DATATYPE = "linkage-datatype/text()";
	private static final String X_LINK_DATATYPE_KEY = "linkage-datatype/@id";
	private static final String X_LINK_DESCRIPTION = "linkage-description/text()";
	private static final String X_PARENT_IDENTIFIER = X_DATA_SOURCE + "/parent-data-source/object-identifier/text()";
	private static final String X_RELATED_ADDRESS_LIST = X_DATA_SOURCE + "/related-address";
	private static final String X_RELATED_ADDRESS_TYPE_OF_RELATION = "type-of-relation/text()";
	private static final String X_RELATED_ADDRESS_TYPE_OF_RELATION_LIST_KEY = "type-of-relation/@list-id";
	private static final String X_RELATED_ADDRESS_TYPE_OF_RELATION_ENTRY_KEY = "type-of-relation/@entry-id";
	private static final String X_RELATED_ADDRESS_IDENTIFIER = "address-identifier/text()";
	private static final String X_RELATED_ADDRESS_DATE_OF_LAST_MODIFICATION = "date-of-last-modification/text()";
	private static final String X_RELATED_DS_LIST = X_DATA_SOURCE + "/link-data-source";
	private static final String X_RELATED_DS_TYPE = "object-link-type/text()";
	private static final String X_RELATED_DS_TYPE_KEY = "object-link-type/@id";
	private static final String X_RELATED_DS_DESCRIPTION = "object-link-description/text()";
	private static final String X_RELATED_DS_IDENTIFIER = "object-identifier/text()";

	public static IngridDocument map(Document source) {
		IngridDocument dataSource = new IngridDocument();

		putString(MdekKeys.WORK_STATE, XPathUtils.getString(source, X_WORK_STATE), dataSource);

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
		putString(MdekKeys.IS_INSPIRE_RELEVANT, XPathUtils.getString(source, X_IS_INSPIRE_RELEVANT), target);
		putString(MdekKeys.IS_ADV_COMPATIBLE, XPathUtils.getString(source, X_IS_ADV_COMPATIBLE), target);
		putString(MdekKeys.IS_OPEN_DATA, XPathUtils.getString(source, X_IS_OPEN_DATA), target);
		mapOpenDataCategories(source, target);
		mapEnvTopics(source, target);
		mapProductGroup(source, target);
	}

	private static void mapGeneralAdditionalValues(Document source, IngridDocument target) {
		NodeList additionalValues = XPathUtils.getNodeList(source, X_GENERAL_ADDITIONAL_VALUE_LIST);
		List<IngridDocument> additionalValuesList = new ArrayList<IngridDocument>();

		Map<String, IngridDocument> fieldKeyToDocMap = new HashMap<String, IngridDocument>();
		for (int index = 0; index < additionalValues.getLength(); ++index) {
			Node additionalValue = additionalValues.item(index);
			addAdditionalValue(additionalValue, additionalValuesList, fieldKeyToDocMap);
		}

		putDocList(MdekKeys.ADDITIONAL_FIELDS, additionalValuesList, target);
	}

	private static void addAdditionalValue(Node additionalValue, List<IngridDocument> additionalValuesTopList, Map<String, IngridDocument> fieldKeyToDocMap) {
		IngridDocument additionalValueDoc = new IngridDocument();
		putString(MdekKeys.ADDITIONAL_FIELD_KEY, XPathUtils.getString(additionalValue, X_GENERAL_ADDITIONAL_VALUE_FIELD_KEY), additionalValueDoc);
		Node dataNode = XPathUtils.getNode(additionalValue, X_GENERAL_ADDITIONAL_VALUE_FIELD_DATA);
		if (dataNode != null) {
			putString(MdekKeys.ADDITIONAL_FIELD_DATA, XPathUtils.getString(additionalValue, X_GENERAL_ADDITIONAL_VALUE_FIELD_DATA), additionalValueDoc);
			putString(MdekKeys.ADDITIONAL_FIELD_LIST_ITEM_ID, XPathUtils.getString(dataNode, X_GENERAL_ADDITIONAL_VALUE_ID), additionalValueDoc);
		}

		String parentKey = XPathUtils.getString(additionalValue, X_GENERAL_ADDITIONAL_VALUE_FIELD_KEY_PARENT);
		if (parentKey != null) {
			IngridDocument tableDoc = fieldKeyToDocMap.get(parentKey);
			if (tableDoc == null) {
				tableDoc = new IngridDocument();
				tableDoc.put(MdekKeys.ADDITIONAL_FIELD_KEY, parentKey);
				List<List<IngridDocument>> rowList = new ArrayList<List<IngridDocument>>();
				tableDoc.put(MdekKeys.ADDITIONAL_FIELD_ROWS, rowList);
				fieldKeyToDocMap.put(parentKey, tableDoc);
				// also add to top list if not present (no table in table)
				additionalValuesTopList.add(tableDoc);
			}
			Integer line = XPathUtils.getInt(additionalValue, X_GENERAL_ADDITIONAL_VALUE_LINE);
			List<List<IngridDocument>> rowList = (List<List<IngridDocument>>) tableDoc.get(MdekKeys.ADDITIONAL_FIELD_ROWS);
			while (rowList.size() < line) {
				rowList.add(new ArrayList<IngridDocument>());
			}
			List<IngridDocument> colList = rowList.get(line-1);
			colList.add(additionalValueDoc);
		} else {
			additionalValuesTopList.add(additionalValueDoc);
		}
	}

	private static void mapTopicCategories(Document source, IngridDocument target) {
		mapIntList(source, X_TOPIC_CATEGORIES, target, MdekKeys.TOPIC_CATEGORIES);
	}

	private static void mapOpenDataCategories(Document source, IngridDocument target) {
		NodeList openDataCats = XPathUtils.getNodeList(source, X_OPEN_DATA_CATEGORIES);
		List<IngridDocument> openDataCatList = new ArrayList<IngridDocument>();

		for (int index = 0; index < openDataCats.getLength(); index++) {
			Node openDataCat = openDataCats.item(index);
			IngridDocument openDataCatDoc = new IngridDocument();
			putString(MdekKeys.OPEN_DATA_CATEGORY_VALUE, openDataCat.getTextContent(), openDataCatDoc);
			putInt(MdekKeys.OPEN_DATA_CATEGORY_KEY, XPathUtils.getInt(openDataCat, X_ATTRIBUTE_ID), openDataCatDoc);
			openDataCatList.add(openDataCatDoc);
		}

		putDocList(MdekKeys.OPEN_DATA_CATEGORY_LIST, openDataCatList, target);
	}

	private static void mapEnvTopics(Document source, IngridDocument target) {
		mapIntList(source, X_ENV_TOPICS, target, MdekKeys.ENV_TOPICS);
	}
	
	private static void mapProductGroup(Document source, IngridDocument target) {
	    NodeList productItems = XPathUtils.getNodeList(source, X_ADV_PRODUCT_GROUP_ITEMS);
	    List<IngridDocument> productList = new ArrayList<IngridDocument>();
	    
	    for (int index = 0; index < productItems.getLength(); index++) {
            Node item = productItems.item(index);
            IngridDocument docProduct = new IngridDocument();
            putString(MdekKeys.ADV_PRODUCT_VALUE, item.getTextContent(), docProduct);
            productList.add( docProduct );
	    }
	    putDocList(MdekKeys.ADV_PRODUCT_LIST, productList, target);
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
		mapKeyCatalogue(dataset, target, MdekKeys.TECHNICAL_DOMAIN_DATASET);
		mapDatasetParameter(dataset, target);
		putString(new String[] {MdekKeys.TECHNICAL_DOMAIN_DATASET, MdekKeys.METHOD},
				XPathUtils.getString(dataset, X_DATASET_METHOD), target);
	}

	private static void mapDatasetParameter(Node domainNode, IngridDocument target) {
		NodeList parameterNodeList = XPathUtils.getNodeList(domainNode, X_DATASET_PARAMETER_LIST);
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
				XPathUtils.getInt(service, X_SERVICE_TYPE_KEY), target);
		putString(new String[] {MdekKeys.TECHNICAL_DOMAIN_SERVICE, MdekKeys.SERVICE_TYPE},
				XPathUtils.getString(service, X_SERVICE_TYPE), target);
		putString(new String[] {MdekKeys.TECHNICAL_DOMAIN_SERVICE, MdekKeys.COUPLING_TYPE},
		        XPathUtils.getString(service, X_COUPLING_TYPE), target);
		mapServiceClassifications(service, target);
		mapPublicationScales(service, (IngridDocument) target.get(MdekKeys.TECHNICAL_DOMAIN_SERVICE));
		putString(new String[] {MdekKeys.TECHNICAL_DOMAIN_SERVICE, MdekKeys.SYSTEM_HISTORY},
				XPathUtils.getString(service, X_SERVICE_SYSTEM_HISTORY), target);
		putString(new String[] {MdekKeys.TECHNICAL_DOMAIN_SERVICE, MdekKeys.DATABASE_OF_SYSTEM},
				XPathUtils.getString(service, X_SERVICE_DATABASE_OF_SYSTEM), target);
		putString(new String[] {MdekKeys.TECHNICAL_DOMAIN_SERVICE, MdekKeys.SYSTEM_ENVIRONMENT},
				XPathUtils.getString(service, X_SERVICE_SYSTEM_ENVIRONMENT), target);
		mapServiceVersions(service, target);
		mapServiceOperations(service, target);

		putString(new String[] {MdekKeys.TECHNICAL_DOMAIN_SERVICE, MdekKeys.HAS_ACCESS_CONSTRAINT},
				XPathUtils.getString(service, X_SERVICE_HAS_ACCESS_CONSTRAINT), target);
		putString(new String[] {MdekKeys.TECHNICAL_DOMAIN_SERVICE, MdekKeys.HAS_ATOM_DOWNLOAD},
				XPathUtils.getString(service, X_SERVICE_HAS_ATOM_DOWNLOAD), target);
		mapServiceUrls(service, (IngridDocument) target.get(MdekKeys.TECHNICAL_DOMAIN_SERVICE));
	}

	private static void mapServiceClassifications(Node service, IngridDocument target) {
		NodeList serviceClassificNodeList = XPathUtils.getNodeList(service, X_SERVICE_CLASSIFICATION_LIST);
		List<IngridDocument> serviceClassificDocs = new ArrayList<IngridDocument>();

		for (int index = 0; index < serviceClassificNodeList.getLength(); index++) {
			Node serviceClassific = serviceClassificNodeList.item(index);
			IngridDocument serviceClassificDoc = new IngridDocument();
			putInt(MdekKeys.SERVICE_TYPE2_KEY, XPathUtils.getInt(serviceClassific, X_ATTRIBUTE_ID), serviceClassificDoc);
			putString(MdekKeys.SERVICE_TYPE2_VALUE, serviceClassific.getTextContent(), serviceClassificDoc);
			serviceClassificDocs.add(serviceClassificDoc);
		}

		putDocList(new String[] {MdekKeys.TECHNICAL_DOMAIN_SERVICE, MdekKeys.SERVICE_TYPE2_LIST}, serviceClassificDocs, target);
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

    private static void mapServiceVersions(Node service, IngridDocument target) {
        NodeList serviceVersionNodeList = XPathUtils.getNodeList(service, X_SERVICE_VERSION_LIST);
        List<IngridDocument> serviceVersionDocs = new ArrayList<IngridDocument>();

        for (int index = 0; index < serviceVersionNodeList.getLength(); index++) {
            Node serviceVersion = serviceVersionNodeList.item(index);
            IngridDocument serviceVersionDoc = new IngridDocument();
            putInt(MdekKeys.SERVICE_VERSION_KEY, XPathUtils.getInt(serviceVersion, X_ATTRIBUTE_ID), serviceVersionDoc);
            putString(MdekKeys.SERVICE_VERSION_VALUE, serviceVersion.getTextContent(), serviceVersionDoc);
            serviceVersionDocs.add(serviceVersionDoc);
        }

        putDocList(new String[] {MdekKeys.TECHNICAL_DOMAIN_SERVICE, MdekKeys.SERVICE_VERSION_LIST}, serviceVersionDocs, target);
    }

	private static void mapServiceOperations(Node service, IngridDocument target) {
		NodeList serviceOperationList = XPathUtils.getNodeList(service, X_SERVICE_OPERATION_LIST);
		ArrayList<IngridDocument> serviceOperations = new ArrayList<IngridDocument>();

		for (int index = 0; index < serviceOperationList.getLength(); index++) {
			Node serviceOperation = serviceOperationList.item(index);
			IngridDocument serviceOperationDoc = new IngridDocument();
			putString(MdekKeys.SERVICE_OPERATION_NAME, XPathUtils.getString(serviceOperation, X_SERVICE_OPERATION_NAME), serviceOperationDoc);
			putInt(MdekKeys.SERVICE_OPERATION_NAME_KEY, XPathUtils.getInt(serviceOperation, X_SERVICE_OPERATION_NAME_KEY), serviceOperationDoc);
			putString(MdekKeys.SERVICE_OPERATION_DESCRIPTION, XPathUtils.getString(serviceOperation, X_SERVICE_OPERATION_DESCRIPTION), serviceOperationDoc);
			putString(MdekKeys.INVOCATION_NAME, XPathUtils.getString(serviceOperation, X_SERVICE_INVOCATION_NAME), serviceOperationDoc);
			mapPlatformsOfOperation(serviceOperation, serviceOperationDoc);
			mapStringList(serviceOperation, X_SERVICE_CONNECTION_POINT_LIST, serviceOperationDoc, MdekKeys.CONNECT_POINT_LIST);
			mapParametersOfOperation(serviceOperation, serviceOperationDoc);
			mapStringList(serviceOperation, X_SERVICE_DEPENDS_ON_LIST, serviceOperationDoc, MdekKeys.DEPENDS_ON_LIST);
			serviceOperations.add(serviceOperationDoc);
		}

		putDocList(new String[] {MdekKeys.TECHNICAL_DOMAIN_SERVICE, MdekKeys.SERVICE_OPERATION_LIST}, serviceOperations, target);
	}

	private static void mapPlatformsOfOperation(Node operationContext, IngridDocument operationDoc) {
		List<IngridDocument> platformsList = new ArrayList<IngridDocument>();
		NodeList platformsOfOperation = XPathUtils.getNodeList(operationContext, X_SERVICE_PLATFORM_LIST);

		for (int index = 0; index < platformsOfOperation.getLength(); index++) {
			Node platform = platformsOfOperation.item(index);
			IngridDocument platformDoc = new IngridDocument();
			putString(MdekKeys.PLATFORM_VALUE, XPathUtils.getString(platform, X_SERVICE_PLATFORM), platformDoc);
			putInt(MdekKeys.PLATFORM_KEY, XPathUtils.getInt(platform, X_SERVICE_PLATFORM_KEY), platformDoc);
			platformsList.add(platformDoc);
		}

		putDocList(MdekKeys.PLATFORM_LIST, platformsList, operationDoc);
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

	private static void mapServiceUrls(Node context, IngridDocument target) {
		NodeList serviceUrlNodeList = XPathUtils.getNodeList(context, X_SERVICE_URL_LIST);
		List<IngridDocument> serviceUrlDocs = new ArrayList<IngridDocument>();

		for (int index = 0; index < serviceUrlNodeList.getLength(); index++) {
			Node serviceUrl = serviceUrlNodeList.item(index);
			IngridDocument serviceUrlDoc = new IngridDocument();
			putString(MdekKeys.NAME, XPathUtils.getString(serviceUrl, X_SERVICE_NAME), serviceUrlDoc);
			putString(MdekKeys.URL, XPathUtils.getString(serviceUrl, X_SERVICE_URL), serviceUrlDoc);
			putString(MdekKeys.DESCRIPTION, XPathUtils.getString(serviceUrl, X_SERVICE_URL_DESCRIPTION), serviceUrlDoc);
			serviceUrlDocs.add(serviceUrlDoc);
		}

		putDocList(MdekKeys.URL_LIST, serviceUrlDocs, target);
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
		mapPublicationScales(map, (IngridDocument) target.get(MdekKeys.TECHNICAL_DOMAIN_MAP));
		mapKeyCatalogue(map, target, MdekKeys.TECHNICAL_DOMAIN_MAP);
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
		mapGeoGrid(map, target);
		putDouble(new String[] {MdekKeys.TECHNICAL_DOMAIN_MAP, MdekKeys.POS_ACCURACY_VERTICAL},
				XPathUtils.getDouble(map, X_MAP_POS_ACCURACY_VERTICAL), target);
		putInt(new String[] {MdekKeys.TECHNICAL_DOMAIN_MAP, MdekKeys.KEYC_INCL_W_DATASET},
				XPathUtils.getInt(map, X_MAP_KEYC_INCL_W_DATASET), target);
		mapStringList(map, X_MAP_FEATURE_TYPE_LIST, target, new String[] {MdekKeys.TECHNICAL_DOMAIN_MAP, MdekKeys.FEATURE_TYPE_LIST});
		putString(new String[] {MdekKeys.TECHNICAL_DOMAIN_MAP, MdekKeys.DATASOURCE_UUID},
				XPathUtils.getString(map, X_MAP_DATASOURCE_IDENTIFICATOR), target);
	}

	private static void mapKeyCatalogue(Node domainNode, IngridDocument target, String keyOfTechnicalDomain) {
		NodeList keyCatalogues = XPathUtils.getNodeList(domainNode, X_MAP_AND_DATASET_KEY_CATALOGUE_LIST);
		List<IngridDocument> keyCatalogueList = new ArrayList<IngridDocument>();

		for (int index = 0; index < keyCatalogues.getLength(); index++) {
			Node keyCatalogue = keyCatalogues.item(index);
			IngridDocument keyCatalogueDoc = new IngridDocument();
			putString(MdekKeys.SUBJECT_CAT, XPathUtils.getString(keyCatalogue, X_MAP_AND_DATASET_KEY_CATALOGUE), keyCatalogueDoc);
			putInt(MdekKeys.SUBJECT_CAT_KEY, XPathUtils.getInt(keyCatalogue, X_MAP_AND_DATASET_KEY_CATALOGUE_KEY), keyCatalogueDoc);
			putString(MdekKeys.KEY_DATE, XPathUtils.getString(keyCatalogue, X_MAP_AND_DATASET_KEY_CATALOGUE_DATE), keyCatalogueDoc);
			putString(MdekKeys.EDITION, XPathUtils.getString(keyCatalogue, X_MAP_AND_DATASET_KEY_CATALOGUE_EDITION), keyCatalogueDoc);

			keyCatalogueList.add(keyCatalogueDoc);
		}

		putDocList(new String[]{keyOfTechnicalDomain, MdekKeys.KEY_CATALOG_LIST}, keyCatalogueList, target);
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
	
	private static void mapGeoGrid(Node mapContext, IngridDocument target) {
	    
	    putString(new String[] {MdekKeys.TECHNICAL_DOMAIN_MAP, MdekKeys.TRANSFORMATION_PARAMETER}, XPathUtils.getString(mapContext, X_MAP_GEO_GRID_TRANSFORM_PARAM), target);
	    putInt(new String[] {MdekKeys.TECHNICAL_DOMAIN_MAP, MdekKeys.NUM_DIMENSIONS}, XPathUtils.getInt(mapContext, X_MAP_GEO_GRID_NUM_DIMENSIONS), target);
	    putString(new String[] {MdekKeys.TECHNICAL_DOMAIN_MAP, MdekKeys.AXIS_DIM_NAME}, XPathUtils.getString(mapContext, X_MAP_GEO_GRID_AXIS_NAME), target);
	    putInt(new String[] {MdekKeys.TECHNICAL_DOMAIN_MAP, MdekKeys.AXIS_DIM_SIZE}, XPathUtils.getInt(mapContext, X_MAP_GEO_GRID_AXIS_SIZE), target);
	    putString(new String[] {MdekKeys.TECHNICAL_DOMAIN_MAP, MdekKeys.CELL_GEOMETRY}, XPathUtils.getString(mapContext, X_MAP_GEO_GRID_CELL_GEOMETRY), target);
	    putString(new String[] {MdekKeys.TECHNICAL_DOMAIN_MAP, MdekKeys.GEO_RECTIFIED}, XPathUtils.getString(mapContext, X_MAP_GEO_GRID_GEO_RECTIFIED), target);
	    putString(new String[] {MdekKeys.TECHNICAL_DOMAIN_MAP, MdekKeys.GEO_RECT_CHECKPOINT}, XPathUtils.getString(mapContext, X_MAP_GEO_GRID_RECT_CHECKPOINT), target);
	    putString(new String[] {MdekKeys.TECHNICAL_DOMAIN_MAP, MdekKeys.GEO_RECT_DESCRIPTION}, XPathUtils.getString(mapContext, X_MAP_GEO_GRID_RECT_DESCRIPTION), target);
	    putString(new String[] {MdekKeys.TECHNICAL_DOMAIN_MAP, MdekKeys.GEO_RECT_CORNER_POINT}, XPathUtils.getString(mapContext, X_MAP_GEO_GRID_RECT_CORNER_POINT), target);
	    putString(new String[] {MdekKeys.TECHNICAL_DOMAIN_MAP, MdekKeys.GEO_RECT_POINT_IN_PIXEL}, XPathUtils.getString(mapContext, X_MAP_GEO_GRID_RECT_POINT_IN_PIXEL), target);
	    putString(new String[] {MdekKeys.TECHNICAL_DOMAIN_MAP, MdekKeys.GEO_REF_CONTROL_POINT}, XPathUtils.getString(mapContext, X_MAP_GEO_GRID_REF_CONTROL_POINT), target);
	    putString(new String[] {MdekKeys.TECHNICAL_DOMAIN_MAP, MdekKeys.GEO_REF_ORIENTATION_PARAM}, XPathUtils.getString(mapContext, X_MAP_GEO_GRID_REF_ORIENTATION_PARAM), target);
	    putString(new String[] {MdekKeys.TECHNICAL_DOMAIN_MAP, MdekKeys.GEO_REF_PARAMETER}, XPathUtils.getString(mapContext, X_MAP_GEO_GRID_REF_PARAMETER), target);
	    
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
        mapDataLanguages(source, target);
		putString(MdekKeys.METADATA_LANGUAGE_NAME,
				XPathUtils.getString(source, X_ADDITIONAL_METADATA_LANGUAGE), target);
		putInt(MdekKeys.METADATA_LANGUAGE_CODE,
				XPathUtils.getInt(source, X_ADDITIONAL_METADATA_LANGUAGE_KEY), target);
		mapExportTos(source, target);
		mapLegislations(source, target);
		putString(MdekKeys.DATASET_INTENTIONS,
				XPathUtils.getString(source, X_ADDITIONAL_DATASET_INTENTIONS), target);
		mapAccessConstraints(source, target);
        mapUseLimitations(source, target);
		mapUseConstraints(source, target);
		mapMediumOptions(source, target);
		mapDataFormats(source, target);
		mapDataFormatsInspire(source, target);
		putInt(MdekKeys.PUBLICATION_CONDITION,
				XPathUtils.getInt(source, X_ADDITIONAL_PUBLICATION_CONDITION), target);
		putString(MdekKeys.DATASET_USAGE,
				XPathUtils.getString(source, X_ADDITIONAL_DATASET_USAGE), target);
		putString(MdekKeys.ORDERING_INSTRUCTIONS,
				XPathUtils.getString(source, X_ADDITIONAL_ORDERING_INSTRUCTION), target);
		mapComments(source, target);
		mapConformities(source, target);
		mapDQs(source, target);
	}

    private static void mapIdTextNodesToDocs(Document source, IngridDocument target,
            String xPathNodes,
            String docIdKey, String docValueKey, String docListKey) {
        NodeList myNodeList = XPathUtils.getNodeList(source, xPathNodes);
        List<IngridDocument> myDocList = new ArrayList<IngridDocument>();

        for (int index = 0; index < myNodeList.getLength(); index++) {
            Node myNode = myNodeList.item(index);
            IngridDocument myDoc = new IngridDocument();
            putString(docValueKey, myNode.getTextContent(), myDoc);
            putInt(docIdKey, XPathUtils.getInt(myNode, X_ATTRIBUTE_ID), myDoc);
            myDocList.add(myDoc);
        }

        putDocList(docListKey, myDocList, target);
        
    }

    private static void mapDataLanguages(Document source, IngridDocument target) {
        mapIdTextNodesToDocs(source, target,
                X_ADDITIONAL_DATA_LANGUAGE_LIST,
                MdekKeys.DATA_LANGUAGE_CODE, MdekKeys.DATA_LANGUAGE_NAME, MdekKeys.DATA_LANGUAGE_LIST);
    }

	private static void mapExportTos(Document source, IngridDocument target) {
        mapIdTextNodesToDocs(source, target,
                X_ADDITIONAL_EXPORT_TO_LIST,
                MdekKeys.EXPORT_CRITERION_KEY, MdekKeys.EXPORT_CRITERION_VALUE, MdekKeys.EXPORT_CRITERIA);
	}

	private static void mapLegislations(Document source, IngridDocument target) {
        mapIdTextNodesToDocs(source, target,
                X_ADDITIONAL_LEGISLATION_LIST,
                MdekKeys.LEGISLATION_KEY, MdekKeys.LEGISLATION_VALUE, MdekKeys.LEGISLATIONS);
	}

	private static void mapAccessConstraints(Document source, IngridDocument target) {
		NodeList accessConstraints = XPathUtils.getNodeList(source, X_ADDITIONAL_ACCESS_CONSTRAINT_LIST);
		List<IngridDocument> accessConstraintList = new ArrayList<IngridDocument>();

		for (int index = 0; index < accessConstraints.getLength(); index++) {
			Node accessConstraint = accessConstraints.item(index);
			IngridDocument accessConstraintDoc = new IngridDocument();
			putString(MdekKeys.ACCESS_RESTRICTION_VALUE, XPathUtils.getString(accessConstraint, X_ADDITIONAL_ACCESS_CONSTRAINT_RESTRICTION), accessConstraintDoc);
			putInt(MdekKeys.ACCESS_RESTRICTION_KEY, XPathUtils.getInt(accessConstraint, X_ADDITIONAL_ACCESS_CONSTRAINT_RESTRICTION_KEY), accessConstraintDoc);

			accessConstraintList.add(accessConstraintDoc);
		}

		putDocList(MdekKeys.ACCESS_LIST, accessConstraintList, target);
	}

    private static void mapUseLimitations(Document source, IngridDocument target) {
        NodeList useLimitations = XPathUtils.getNodeList(source, X_ADDITIONAL_USE_LIMITATION_LIST);
        List<IngridDocument> useLimitationList = new ArrayList<IngridDocument>();

        for (int index = 0; index < useLimitations.getLength(); index++) {
            Node useLimitation = useLimitations.item(index);
            IngridDocument useLimitationDoc = new IngridDocument();
            putString(MdekKeys.USE_TERMS_OF_USE_VALUE, XPathUtils.getString(useLimitation, X_ADDITIONAL_USE_LIMITATION_TERMS_OF_USE), useLimitationDoc);
            putInt(MdekKeys.USE_TERMS_OF_USE_KEY, XPathUtils.getInt(useLimitation, X_ADDITIONAL_USE_LIMITATION_TERMS_OF_USE_KEY), useLimitationDoc);

            useLimitationList.add(useLimitationDoc);
        }

        putDocList(MdekKeys.USE_LIST, useLimitationList, target);
    }

	private static void mapUseConstraints(Document source, IngridDocument target) {
		NodeList useConstraints = XPathUtils.getNodeList(source, X_ADDITIONAL_USE_CONSTRAINT_LIST);
		List<IngridDocument> useConstraintList = new ArrayList<IngridDocument>();

		for (int index = 0; index < useConstraints.getLength(); index++) {
			Node useConstraint = useConstraints.item(index);
			IngridDocument useConstraintDoc = new IngridDocument();
			putString(MdekKeys.USE_LICENSE_VALUE, XPathUtils.getString(useConstraint, X_ADDITIONAL_USE_CONSTRAINT_LICENSE), useConstraintDoc);
			putInt(MdekKeys.USE_LICENSE_KEY, XPathUtils.getInt(useConstraint, X_ADDITIONAL_USE_CONSTRAINT_LICENSE_KEY), useConstraintDoc);

			useConstraintList.add(useConstraintDoc);
		}

		putDocList(MdekKeys.USE_CONSTRAINTS, useConstraintList, target);
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

	private static void mapDataFormatsInspire(Document source, IngridDocument target) {
		NodeList formats = XPathUtils.getNodeList(source, X_ADDITIONAL_DATA_FORMAT_INSPIRE_LIST);
		List<IngridDocument> formatList = new ArrayList<IngridDocument>();

		for (int index = 0; index < formats.getLength(); index++) {
			Node format = formats.item(index);
			IngridDocument formatDoc = new IngridDocument();
			putString(MdekKeys.FORMAT_VALUE, XPathUtils.getString(format, X_ADDITIONAL_DATA_FORMAT_INSPIRE_NAME), formatDoc);
			putInt(MdekKeys.FORMAT_KEY, XPathUtils.getInt(format, X_ADDITIONAL_DATA_FORMAT_INSPIRE_NAME_KEY), formatDoc);

			formatList.add(formatDoc);
		}

		putDocList(MdekKeys.FORMAT_INSPIRE_LIST, formatList, target);
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
			putString(MdekKeys.CONFORMITY_SPECIFICATION_VALUE, XPathUtils.getString(conformity, X_ADDITIONAL_CONFORMITY_SPECIFICATION), conformityDoc);
			putInt(MdekKeys.CONFORMITY_SPECIFICATION_KEY, XPathUtils.getInt(conformity, X_ADDITIONAL_CONFORMITY_SPECIFICATION_KEY), conformityDoc);
			putString(MdekKeys.CONFORMITY_DEGREE_VALUE, XPathUtils.getString(conformity, X_ADDITIONAL_CONFORMITY_DEGREE), conformityDoc);
			putInt(MdekKeys.CONFORMITY_DEGREE_KEY, XPathUtils.getInt(conformity, X_ADDITIONAL_CONFORMITY_DEGREE_KEY), conformityDoc);

			conformityList.add(conformityDoc);
		}

		putDocList(MdekKeys.CONFORMITY_LIST, conformityList, target);
	}

	private static void mapDQs(Document source, IngridDocument target) {
		NodeList nodeList = XPathUtils.getNodeList(source, X_ADDITIONAL_DQ_LIST);
		List<IngridDocument> dqList = new ArrayList<IngridDocument>();

		for (int index = 0; index < nodeList.getLength(); index++) {
			Node node = nodeList.item(index);
			IngridDocument dqDoc = new IngridDocument();
			putInt(MdekKeys.DQ_ELEMENT_ID, XPathUtils.getInt(node, X_ADDITIONAL_DQ_ELEMENT_ID), dqDoc);
			putInt(MdekKeys.NAME_OF_MEASURE_KEY, XPathUtils.getInt(node, X_ADDITIONAL_DQ_NAME_OF_MEASURE_KEY), dqDoc);
			putString(MdekKeys.NAME_OF_MEASURE_VALUE, XPathUtils.getString(node, X_ADDITIONAL_DQ_NAME_OF_MEASURE_VALUE), dqDoc);
			putString(MdekKeys.RESULT_VALUE, XPathUtils.getString(node, X_ADDITIONAL_DQ_RESULT_VALUE), dqDoc);
			putString(MdekKeys.MEASURE_DESCRIPTION, XPathUtils.getString(node, X_ADDITIONAL_DQ_MEASURE_DESCRIPTION), dqDoc);

			dqList.add(dqDoc);
		}

		putDocList(MdekKeys.DATA_QUALITY_LIST, dqList, target);
	}

	private static void mapSpatialDomain(Document source, IngridDocument target) {
		mapCoordinateSystems(source, target);
		putString(MdekKeys.DESCRIPTION_OF_SPATIAL_DOMAIN, XPathUtils.getString(source, X_SPATIAL_DESCRIPTION), target);
		putDouble(MdekKeys.VERTICAL_EXTENT_MAXIMUM, XPathUtils.getDouble(source, X_SPATIAL_VERTICAL_EXTENT_MAXIMUM), target);
		putDouble(MdekKeys.VERTICAL_EXTENT_MINIMUM, XPathUtils.getDouble(source, X_SPATIAL_VERTICAL_EXTENT_MINIMUM), target);
		putInt(MdekKeys.VERTICAL_EXTENT_UNIT, XPathUtils.getInt(source, X_SPATIAL_VERTICAL_EXTENT_UNIT), target);
		putString(MdekKeys.VERTICAL_EXTENT_VDATUM_VALUE, XPathUtils.getString(source, X_SPATIAL_VERTICAL_EXTENT_VDATUM_VALUE), target);
		putInt(MdekKeys.VERTICAL_EXTENT_VDATUM_KEY, XPathUtils.getInt(source, X_SPATIAL_VERTICAL_EXTENT_VDATUM_KEY), target);
		mapGeoLocations(source, target);
	}

	private static void mapCoordinateSystems(Document source, IngridDocument target) {
        mapIdTextNodesToDocs(source, target,
                X_SPATIAL_COORDINATE_SYSTEM_LIST,
                MdekKeys.REFERENCESYSTEM_ID, MdekKeys.COORDINATE_SYSTEM, MdekKeys.SPATIAL_SYSTEM_LIST);
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
			putString(MdekKeys.LINKAGE_DATATYPE, XPathUtils.getString(linkage, X_LINK_DATATYPE), linkageDoc);
			putInt(MdekKeys.LINKAGE_DATATYPE_KEY, XPathUtils.getInt(linkage, X_LINK_DATATYPE_KEY), linkageDoc);
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
