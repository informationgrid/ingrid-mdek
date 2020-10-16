/*
 * **************************************************-
 * ingrid-import-export
 * ==================================================
 * Copyright (C) 2014 - 2020 wemove digital solutions GmbH
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
package de.ingrid.mdek.xml.exporter.mapper;

import static de.ingrid.mdek.xml.XMLKeys.*;

import java.util.ArrayList;
import java.util.List;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.xml.util.XMLElement;
import de.ingrid.mdek.xml.util.XMLTreeCleaner;
import de.ingrid.utils.IngridDocument;

public class DatasourceDocToXMLMapper extends AbstractDocToXMLMapper {

	public DatasourceDocToXMLMapper(IngridDocument document) {
		super(document);
	}

	public static XMLElement createDataSource() {
		XMLElement dataSource = new XMLElement(DATA_SOURCE);
		return dataSource;
	}

	public XMLElement createDataSourceInstance() {
		XMLElement dataSource = new XMLElement(DATA_SOURCE_INSTANCE);
		dataSource.addAttribute(WORK_STATE, getStringForKey(MdekKeys.WORK_STATE));
		dataSource.addChild(createGeneral());
		dataSource.addChild(createTechnicalDomain());
		dataSource.addChild(createAdditionalInformation());
		dataSource.addChild(createSpatialDomain());
		dataSource.addChild(createTemporalDomain());
		dataSource.addChild(createSubjectTerms());
		dataSource.addChildren(createAvailableLinkages());
		dataSource.addChild(createParentDataSource());
		dataSource.addChildren(createRelatedAddresses());
		dataSource.addChildren(createLinkDataSources());

		XMLTreeCleaner.removeEmptyChildElements(dataSource);

		return dataSource;
	}

	private XMLElement createGeneral() {
		XMLElement general = new XMLElement(GENERAL);
		general.addChild(createObjectIdentifier());
		general.addChild(createParentIdentifier());
		general.addChild(createCatalogueIdentifier());
		general.addChild(createModificatorIdentifier());
		general.addChild(createResponsibleIdentifier());
		general.addChild(createObjectClass());
		general.addChild(createTitle());
		general.addChild(createAbstract());
        general.addChild(createMetadataDate());
		general.addChild(createDateOfLastModification());
		general.addChild(createDateOfCreation());
		general.addChild(createOriginalControlIdentifier());
		general.addChild(createGeneralAdditionalValues());
		general.addChild(createMetadata());
		general.addChild(createDatasetAlternateName());
		general.addChild(createDatasetCharacterSet());
		general.addChild(createTopicCategories());
		general.addChild(createEnvInformation());
		general.addChild(createIsInspireRelevant());
		general.addChild(createIsAdvCompatible());
		general.addChild(createAdvProductGroup());
		general.addChild(createIsOpenData());
		general.addChild(createOpenDataCategorys());
		return general;
	}

	private XMLElement createParentIdentifier() {
		return new XMLElement(PARENT_IDENTIFIER_EXTERN, getStringForKey(MdekKeys.PARENT_IDENTIFIER));
	}

	private XMLElement createObjectIdentifier() {
		return new XMLElement(OBJECT_IDENTIFIER, getStringForKey(MdekKeys.UUID));
	}

	private XMLElement createCatalogueIdentifier() {
		return new XMLElement(CATALOGUE_IDENTIFIER, getLongForKey(MdekKeys.CATALOGUE_IDENTIFIER));
	}

	private XMLElement createModificatorIdentifier() {
		IngridDocument modUser = getIngridDocumentForKey(MdekKeys.MOD_USER);
		String uuid = getStringForKey(MdekKeys.UUID, modUser);
		return new XMLElement(MODIFICATOR_IDENTIFIER, uuid);
	}

	private XMLElement createResponsibleIdentifier() {
		IngridDocument responsibleUser = getIngridDocumentForKey(MdekKeys.RESPONSIBLE_USER);
		String uuid = getStringForKey(MdekKeys.UUID, responsibleUser);
		return new XMLElement(RESPONSIBLE_IDENTIFIER, uuid);
	}

	private XMLElement createObjectClass() {
		XMLElement objectClass = new XMLElement(OBJECT_CLASS);
		objectClass.addAttribute(ID, getIntegerForKey(MdekKeys.CLASS));
		return objectClass;
	}

	private XMLElement createTitle() {
		return new XMLElement(TITLE, getStringForKey(MdekKeys.TITLE));
	}

	private XMLElement createAbstract() {
		return new XMLElement(ABSTRACT, getStringForKey(MdekKeys.ABSTRACT));
	}

    private XMLElement createMetadataDate() {
        return new XMLElement(METADATA_DATE, getStringForKey(MdekKeys.METADATA_DATE));
    }

	private XMLElement createDateOfLastModification() {
		return new XMLElement(DATE_OF_LAST_MODIFICATION, getStringForKey(MdekKeys.DATE_OF_LAST_MODIFICATION));
	}

	private XMLElement createDateOfCreation() {
		return new XMLElement(DATE_OF_CREATION, getStringForKey(MdekKeys.DATE_OF_CREATION));
	}

	private XMLElement createOriginalControlIdentifier() {
		return new XMLElement(ORIGINAL_CONTROL_IDENTIFIER, getStringForKey(MdekKeys.ORIGINAL_CONTROL_IDENTIFIER));
	}

	private XMLElement createGeneralAdditionalValues() {
		XMLElement generalAdditionalValues = new XMLElement(GENERAL_ADDITIONAL_VALUES);
		List<IngridDocument> additionalFields = getIngridDocumentListForKey(MdekKeys.ADDITIONAL_FIELDS);
		createAdditionalValues(generalAdditionalValues, additionalFields, null);
		return generalAdditionalValues;
	}

	private XMLElement createAdditionalValues(XMLElement parent, List<IngridDocument> additionalFields, Integer line) {
		for (IngridDocument additionalField : additionalFields) {
			if (additionalField.get(MdekKeys.ADDITIONAL_FIELD_ROWS) != null) {
				String tableKey = getStringForKey(MdekKeys.ADDITIONAL_FIELD_KEY, additionalField);

				List<List<IngridDocument>> rows =
					(List<List<IngridDocument>>) additionalField.get(MdekKeys.ADDITIONAL_FIELD_ROWS);
				int rowNum = 0;
				for (List<IngridDocument> row : rows) {
					rowNum++;
					for (IngridDocument col : row) {
					    if (col.get(MdekKeys.ADDITIONAL_FIELD_ROWS) != null) {
                            List<IngridDocument> listDyn = new ArrayList<>();
					        listDyn.add( col );
					        createAdditionalValues(parent, listDyn, rowNum);
					    }
						parent.addChild(createGeneralAdditionalValue(col, rowNum, tableKey));
					}
				}

			} else {
				parent.addChild(createGeneralAdditionalValue(additionalField, line, null));
			}
		}
		return parent;
	}

	private XMLElement createGeneralAdditionalValue(IngridDocument context, Integer line, String parentKey) {
		XMLElement generalAdditionalValue = new XMLElement(GENERAL_ADDITIONAL_VALUE);
		if (line != null) {
			generalAdditionalValue.addAttribute(LINE, line.toString());
		}

		generalAdditionalValue.addChild(
				new XMLElement(FIELD_KEY, getStringForKey(MdekKeys.ADDITIONAL_FIELD_KEY, context)));

		String data = getStringForKey(MdekKeys.ADDITIONAL_FIELD_DATA, context);
		String listItemId = getStringForKey(MdekKeys.ADDITIONAL_FIELD_LIST_ITEM_ID, context);
		if (data != null || listItemId != null) {
			XMLElement generalAdditionalData = new XMLElement(FIELD_DATA, data);
			if (listItemId != null) {
				generalAdditionalData.addAttribute(ID, listItemId);
			}
			generalAdditionalValue.addChild(generalAdditionalData);
		}

		if (parentKey != null) {
			generalAdditionalValue.addChild(new XMLElement(FIELD_KEY_PARENT, parentKey));
		}

		return generalAdditionalValue;
	}

	private XMLElement createMetadata() {
		XMLElement metadata = new XMLElement(METADATA);
		metadata.addChild(
				new XMLElement(METADATA_STANDARD_NAME, getStringForKey(MdekKeys.METADATA_STANDARD_NAME)));
		metadata.addChild(
				new XMLElement(METADATA_STANDARD_VERSION, getStringForKey(MdekKeys.METADATA_STANDARD_VERSION)));
		XMLElement metadataCharacterSet = new XMLElement(METADATA_CHARACTER_SET);
		metadataCharacterSet.addAttribute(ISO_CODE, getIntegerForKey(MdekKeys.METADATA_CHARACTER_SET));
		metadata.addChild(metadataCharacterSet);
		return metadata;
	}

	private XMLElement createDatasetAlternateName() {
		return new XMLElement(DATASET_ALTERNATE_NAME, getStringForKey(MdekKeys.DATASET_ALTERNATE_NAME));
	}
	
	private XMLElement createDatasetCharacterSet() {
		XMLElement datasetCharacterSet = new XMLElement(DATASET_CHARACTER_SET);
		datasetCharacterSet.addAttribute(ISO_CODE, getIntegerForKey(MdekKeys.DATASET_CHARACTER_SET));
		return datasetCharacterSet;
	}

	private XMLElement createTopicCategories() {
		XMLElement topicCategories = new XMLElement(TOPIC_CATEGORIES);
		List<Integer> topicCategoryIds = getIntegerListForKey(MdekKeys.TOPIC_CATEGORIES);

		for (Integer categoryId : topicCategoryIds) {
			XMLElement topicCategoryElement = new XMLElement(TOPIC_CATEGORY);
			topicCategoryElement.addAttribute(ID, categoryId);
			topicCategories.addChild(topicCategoryElement);
		}
		return topicCategories;
	}

	private XMLElement createEnvInformation() {
		XMLElement envInformation = new XMLElement(ENV_INFORMATION);
		envInformation.addChild(new XMLElement(IS_CATALOG, getStringForKey(MdekKeys.IS_CATALOG_DATA)));
		envInformation.addChildren(createEnvTopics());
		return envInformation;
	}

	private XMLElement createIsInspireRelevant() {
		return new XMLElement(IS_INSPIRE_RELEVANT, getStringForKey(MdekKeys.IS_INSPIRE_RELEVANT));
	}
	
    private XMLElement createIsAdvCompatible() {
        return new XMLElement(IS_ADV_COMPATIBLE, getStringForKey(MdekKeys.IS_ADV_COMPATIBLE));
    }
    
    private XMLElement createAdvProductGroup() {
        List<IngridDocument> productList = getIngridDocumentListForKey( MdekKeys.ADV_PRODUCT_LIST );
        List<XMLElement> products = new ArrayList<>();
        if (productList != null && productList.size() > 0) {
            for (IngridDocument product : productList) {
                XMLElement xmlElement = new XMLElement( ADV_PRODUCT_GROUP_ITEM );
                xmlElement.setText( product.getString( MdekKeys.ADV_PRODUCT_VALUE ) );
                products.add( xmlElement );
            }
        }
        XMLElement parent = new XMLElement(ADV_PRODUCT_GROUP);
        parent.addChildren( products );
        return parent;
    }
	
	private XMLElement createIsOpenData() {
		return new XMLElement(IS_OPEN_DATA, getStringForKey(MdekKeys.IS_OPEN_DATA));
	}
	
	private List<XMLElement> createEnvTopics() {
		List<XMLElement> envTopics = new ArrayList<>();
		List<Integer> envTopicIds = getIntegerListForKey(MdekKeys.ENV_TOPICS);

		for (Integer envTopicId : envTopicIds) {
			XMLElement envTopicElement = new XMLElement(ENV_TOPIC);
			envTopicElement.addAttribute(ID, envTopicId);
			envTopics.add(envTopicElement);
		}
		return envTopics;
	}
	
	private XMLElement createTechnicalDomain() {
		XMLElement technicalDomain = new XMLElement(TECHNICAL_DOMAIN);
		technicalDomain.addChild(createDataset(getIngridDocumentForKey(MdekKeys.TECHNICAL_DOMAIN_DATASET)));
		technicalDomain.addChild(createService(getIngridDocumentForKey(MdekKeys.TECHNICAL_DOMAIN_SERVICE)));
		technicalDomain.addChild(createDocument(getIngridDocumentForKey(MdekKeys.TECHNICAL_DOMAIN_DOCUMENT)));
		technicalDomain.addChild(createMap(getIngridDocumentForKey(MdekKeys.TECHNICAL_DOMAIN_MAP)));
		technicalDomain.addChild(createProject(getIngridDocumentForKey(MdekKeys.TECHNICAL_DOMAIN_PROJECT)));
		return technicalDomain;
	}

	private XMLElement createDataset(IngridDocument datasetContext) {
		XMLElement dataset = new XMLElement(DATASET);
		dataset.addChild(new XMLElement(DESCRIPTION_OF_TECH_DOMAIN,
						getStringForKey(MdekKeys.DESCRIPTION_OF_TECH_DOMAIN, datasetContext)));
		dataset.addChildren(createKeyCatalogues(datasetContext));
		dataset.addChildren(createDatasetParameters(datasetContext));
		dataset.addChild(new XMLElement(METHOD, getStringForKey(MdekKeys.METHOD, datasetContext)));

		return dataset;
	}

	private List<XMLElement> createDatasetParameters(IngridDocument context) {
		List<IngridDocument> parameterList = getIngridDocumentListForKey(MdekKeys.PARAMETERS, context);
		List<XMLElement> resultList = new ArrayList<>();
		for (IngridDocument parameter : parameterList) {
			resultList.add(createDatasetParameter(parameter));
		}
		return resultList;
	}

	private XMLElement createDatasetParameter(IngridDocument parameterContext) {
		XMLElement datasetParameter = new XMLElement(DATASET_PARAMETER);
		datasetParameter.addChild(
				new XMLElement(PARAMETER, getStringForKey(MdekKeys.PARAMETER, parameterContext)));
		datasetParameter.addChild(
				new XMLElement(SUPPLEMENTARY_INFORMATION, getStringForKey(MdekKeys.SUPPLEMENTARY_INFORMATION, parameterContext)));
		return datasetParameter;
	}

	private XMLElement createService(IngridDocument serviceContext) {
		XMLElement service = new XMLElement(SERVICE);
		service.addChild(
				new XMLElement(DESCRIPTION_OF_TECH_DOMAIN,
						getStringForKey(MdekKeys.DESCRIPTION_OF_TECH_DOMAIN, serviceContext)));
		service.addChild(createServiceType(serviceContext));
		service.addChild(createCouplingType(serviceContext));
		service.addChildren(createServiceClassifications(serviceContext));
		service.addChildren(createPublicationScales(serviceContext));
		service.addChild(
				new XMLElement(SYSTEM_HISTORY,
						getStringForKey(MdekKeys.SYSTEM_HISTORY, serviceContext)));
		service.addChild(
				new XMLElement(DATABASE_OF_SYSTEM,
						getStringForKey(MdekKeys.DATABASE_OF_SYSTEM, serviceContext)));
		service.addChild(
				new XMLElement(SYSTEM_ENVIRONMENT,
						getStringForKey(MdekKeys.SYSTEM_ENVIRONMENT, serviceContext)));
		service.addChildren(createServiceVersions(serviceContext));
		service.addChildren(createServiceOperations(serviceContext));

		service.addChild(new XMLElement(HAS_ACCESS_CONSTRAINT,
			getStringForKey(MdekKeys.HAS_ACCESS_CONSTRAINT, serviceContext)));
		service.addChild(new XMLElement(HAS_ATOM_DOWNLOAD,
				getStringForKey(MdekKeys.HAS_ATOM_DOWNLOAD, serviceContext)));
		service.addChildren(createServiceUrls(serviceContext));

		return service;
	}

	private XMLElement createServiceType(IngridDocument serviceContext) {
		XMLElement serviceType = new XMLElement(SERVICE_TYPE, getStringForKey(MdekKeys.SERVICE_TYPE, serviceContext));
		serviceType.addAttribute(ID, getIntegerForKey(MdekKeys.SERVICE_TYPE_KEY, serviceContext));
		return serviceType;
	}
	
	private XMLElement createCouplingType(IngridDocument serviceContext) {
        XMLElement serviceType = new XMLElement(COUPLING_TYPE, getStringForKey(MdekKeys.COUPLING_TYPE, serviceContext));
        //serviceType.addAttribute(ID, getIntegerForKey(MdekKeys.COUPLING_TYPE, serviceContext));
        return serviceType;
    }

	private List<XMLElement> createServiceClassifications(IngridDocument serviceContext) {
		List<XMLElement> serviceClassifications = new ArrayList<>();
		List<IngridDocument> serviceClassificationDocs = getIngridDocumentListForKey(MdekKeys.SERVICE_TYPE2_LIST, serviceContext);
		for (IngridDocument serviceClassificationDoc : serviceClassificationDocs) {
			serviceClassifications.add(createServiceClassification(serviceClassificationDoc));
		}
		return serviceClassifications;
	}

	private XMLElement createServiceClassification(IngridDocument serviceClassificContext) {
		XMLElement serviceClassific = new XMLElement(SERVICE_CLASSIFICATION, getStringForKey(MdekKeys.SERVICE_TYPE2_VALUE, serviceClassificContext));
		serviceClassific.addAttribute(ID, getIntegerForKey(MdekKeys.SERVICE_TYPE2_KEY, serviceClassificContext));
		return serviceClassific;
	}

	private List<XMLElement> createGridFormatAxis(IngridDocument serviceContext) {
		List<XMLElement> axisDimensions = new ArrayList<>();
		List<IngridDocument> axisDimensionList = getIngridDocumentListForKey(MdekKeys.AXIS_DIMENSION_LIST, serviceContext);
		for (IngridDocument axisDimension : axisDimensionList) {
			axisDimensions.add(createAxisDimension(axisDimension));
		}
		return axisDimensions;
	}

	private XMLElement createAxisDimension(IngridDocument axisDimContext) {
		XMLElement axisDim = new XMLElement(GRID_AXIS);
		axisDim.addChild(new XMLElement(GRID_AXIS_NAME, getStringForKey(MdekKeys.AXIS_DIM_NAME, axisDimContext)));
		axisDim.addChild(new XMLElement(GRID_AXIS_SIZE, getIntegerForKey(MdekKeys.AXIS_DIM_SIZE, axisDimContext)));
		axisDim.addChild(new XMLElement(GRID_AXIS_RESOLUTION, getDoubleForKey(MdekKeys.AXIS_DIM_RESOLUTION, axisDimContext)));
		return axisDim;
	}

	private List<XMLElement> createPublicationScales(IngridDocument serviceContext) {
		List<XMLElement> publicationScales = new ArrayList<>();
		List<IngridDocument> publicationScaleDocs = getIngridDocumentListForKey(MdekKeys.PUBLICATION_SCALE_LIST, serviceContext);
		for (IngridDocument publicationScale : publicationScaleDocs) {
			publicationScales.add(createPublicationScale(publicationScale));
		}
		return publicationScales;
	}

	private XMLElement createPublicationScale(IngridDocument publicationScaleContext) {
		XMLElement publicationScale = new XMLElement(PUBLICATION_SCALE);
		publicationScale.addChild(new XMLElement(SCALE, getIntegerForKey(MdekKeys.SCALE, publicationScaleContext)));
		publicationScale.addChild(new XMLElement(RESOLUTION_GROUND, getDoubleForKey(MdekKeys.RESOLUTION_GROUND, publicationScaleContext)));
		publicationScale.addChild(new XMLElement(RESOLUTION_SCALE, getDoubleForKey(MdekKeys.RESOLUTION_SCAN, publicationScaleContext)));
		return publicationScale;
	}

	private List<XMLElement> createServiceVersions(IngridDocument serviceContext) {
		List<XMLElement> resultList = new ArrayList<>();
		List<IngridDocument> serviceVersionList = getIngridDocumentListForKey(MdekKeys.SERVICE_VERSION_LIST, serviceContext);
		for (IngridDocument serviceDoc : serviceVersionList) {
            resultList.add(createServiceVersion(serviceDoc));
		}
		return resultList;
	}
    private XMLElement createServiceVersion(IngridDocument serviceDoc) {
        XMLElement serviceVersionElem = new XMLElement(SERVICE_VERSION, getStringForKey(MdekKeys.SERVICE_VERSION_VALUE, serviceDoc));
        serviceVersionElem.addAttribute(ID, getIntegerForKey(MdekKeys.SERVICE_VERSION_KEY, serviceDoc));
        return serviceVersionElem;
    }


	private List<XMLElement> createServiceOperations(IngridDocument serviceContext) {
		List<XMLElement> serviceOperations = new ArrayList<>();
		List<IngridDocument> serviceOperationList = getIngridDocumentListForKey(MdekKeys.SERVICE_OPERATION_LIST, serviceContext);
		for (IngridDocument serviceOperation : serviceOperationList) {
			serviceOperations.add(createServiceOperation(serviceOperation));
		}
		return serviceOperations;
	}

	private XMLElement createServiceOperation(IngridDocument serviceOperationContext) {
		XMLElement serviceOperation = new XMLElement(SERVICE_OPERATION);
		serviceOperation.addChild(createOperationName(serviceOperationContext));
		serviceOperation.addChild(new XMLElement(DESCRIPTION_OF_OPERATION, getStringForKey(MdekKeys.SERVICE_OPERATION_DESCRIPTION, serviceOperationContext)));
		serviceOperation.addChild(new XMLElement(INVOCATION_NAME, getStringForKey(MdekKeys.INVOCATION_NAME, serviceOperationContext)));
		serviceOperation.addChildren(createPlatforms(serviceOperationContext));
		serviceOperation.addChildren(createConnectionPoints(serviceOperationContext));
		serviceOperation.addChildren(createParametersOfOperation(serviceOperationContext));
		serviceOperation.addChildren(createDependsOn(serviceOperationContext));
		return serviceOperation;
	}

	private XMLElement createOperationName(IngridDocument serviceOperationContext) {
		XMLElement serviceOperationName = new XMLElement(OPERATION_NAME, getStringForKey(MdekKeys.SERVICE_OPERATION_NAME, serviceOperationContext));
		serviceOperationName.addAttribute(ID, getIntegerForKey(MdekKeys.SERVICE_OPERATION_NAME_KEY, serviceOperationContext));
		return serviceOperationName;
	}

	private List<XMLElement> createPlatforms(IngridDocument serviceOperationContext) {
		List<XMLElement> resultList = new ArrayList<>();
		List<IngridDocument> platformList = getIngridDocumentListForKey(MdekKeys.PLATFORM_LIST, serviceOperationContext);
		for (IngridDocument platformDoc : platformList) {
			resultList.add(createPlatform(platformDoc));
		}
		return resultList;
	}
	private XMLElement createPlatform(IngridDocument platformDoc) {
		XMLElement platformElem = new XMLElement(PLATFORM, getStringForKey(MdekKeys.PLATFORM_VALUE, platformDoc));
		platformElem.addAttribute(ID, getIntegerForKey(MdekKeys.PLATFORM_KEY, platformDoc));
		return platformElem;
	}

	private List<XMLElement> createConnectionPoints(IngridDocument serviceOperationContext) {
		List<XMLElement> resultList = new ArrayList<>();
		List<String> connectionPointIds = getStringListForKey(MdekKeys.CONNECT_POINT_LIST, serviceOperationContext);
		for (String connectionPointId : connectionPointIds) {
			resultList.add(new XMLElement(CONNECTION_POINT, connectionPointId));
		}
		return resultList;
	}

	private List<XMLElement> createParametersOfOperation(IngridDocument serviceOperationContext) {
		List<XMLElement> parametersOfOperation = new ArrayList<>();
		List<IngridDocument> parameterOfOperationList = getIngridDocumentListForKey(MdekKeys.PARAMETER_LIST, serviceOperationContext);
		for (IngridDocument parameterOfOperation : parameterOfOperationList) {
			parametersOfOperation.add(createParameterOfOperation(parameterOfOperation));
		}
		return parametersOfOperation;
	}

	private XMLElement createParameterOfOperation(IngridDocument parameterContext) {
		XMLElement parameterOfOperation = new XMLElement(PARAMETER_OF_OPERATION);
		parameterOfOperation.addChild(new XMLElement(NAME, getStringForKey(MdekKeys.PARAMETER_NAME, parameterContext)));
		parameterOfOperation.addChild(new XMLElement(OPTIONAL, getIntegerForKey(MdekKeys.OPTIONALITY, parameterContext)));
		parameterOfOperation.addChild(new XMLElement(REPEATABILITY, getIntegerForKey(MdekKeys.REPEATABILITY, parameterContext)));
		parameterOfOperation.addChild(new XMLElement(DIRECTION, getStringForKey(MdekKeys.DIRECTION, parameterContext)));
		parameterOfOperation.addChild(new XMLElement(DESCRIPTION_OF_PARAMETER, getStringForKey(MdekKeys.DESCRIPTION, parameterContext)));
		return parameterOfOperation;
	}

	private List<XMLElement> createDependsOn(IngridDocument serviceOperationContext) {
		List<XMLElement> resultList = new ArrayList<>();
		List<String> dependsOnIds = getStringListForKey(MdekKeys.DEPENDS_ON_LIST, serviceOperationContext);
		for (String dependsOnId : dependsOnIds) {
			resultList.add(new XMLElement(DEPENDS_ON, dependsOnId));
		}
		return resultList;
	}

	private List<XMLElement> createServiceUrls(IngridDocument contextDoc) {
		List<XMLElement> elements = new ArrayList<>();
		List<IngridDocument> docs = getIngridDocumentListForKey(MdekKeys.URL_LIST, contextDoc);
		for (IngridDocument doc : docs) {
			elements.add(createServiceUrl(doc));
		}
		return elements;
	}

	private XMLElement createServiceUrl(IngridDocument contextDoc) {
		XMLElement element = new XMLElement(SERVICE_URL);
		element.addChild(new XMLElement(NAME, getStringForKey(MdekKeys.NAME, contextDoc)));
		element.addChild(new XMLElement(URL, getStringForKey(MdekKeys.URL, contextDoc)));
		element.addChild(new XMLElement(DESCRIPTION, getStringForKey(MdekKeys.DESCRIPTION, contextDoc)));
		return element;
	}

	private XMLElement createDocument(IngridDocument documentContext) {
		XMLElement document = new XMLElement(DOCUMENT);
		document.addChild(new XMLElement(DESCRIPTION_OF_TECH_DOMAIN, getStringForKey(MdekKeys.DESCRIPTION_OF_TECH_DOMAIN, documentContext)));
		document.addChild(new XMLElement(PUBLISHER, getStringForKey(MdekKeys.PUBLISHER, documentContext)));
		document.addChild(new XMLElement(PUBLISHING_PLACE, getStringForKey(MdekKeys.PUBLISHING_PLACE, documentContext)));
		document.addChild(new XMLElement(YEAR, getStringForKey(MdekKeys.YEAR, documentContext)));
		document.addChild(new XMLElement(ISBN, getStringForKey(MdekKeys.ISBN, documentContext)));
		document.addChild(new XMLElement(SOURCE, getStringForKey(MdekKeys.SOURCE, documentContext)));
		document.addChild(createTypeOfDocument(documentContext));
		document.addChild(new XMLElement(EDITOR, getStringForKey(MdekKeys.EDITOR, documentContext)));
		document.addChild(new XMLElement(AUTHOR, getStringForKey(MdekKeys.AUTHOR, documentContext)));
		document.addChild(new XMLElement(ADDITIONAL_BIBLIOGRAPHIC_INFO, getStringForKey(MdekKeys.ADDITIONAL_BIBLIOGRAPHIC_INFO, documentContext)));
		document.addChild(new XMLElement(LOCATION, getStringForKey(MdekKeys.LOCATION, documentContext)));
		document.addChild(new XMLElement(PAGES, getStringForKey(MdekKeys.PAGES, documentContext)));
		document.addChild(new XMLElement(VOLUME, getStringForKey(MdekKeys.VOLUME, documentContext)));
		document.addChild(new XMLElement(PUBLISHED_IN, getStringForKey(MdekKeys.PUBLISHED_IN, documentContext)));
		return document;
	}

	private XMLElement createTypeOfDocument(IngridDocument documentContext) {
		XMLElement typeOfDocument = new XMLElement(TYPE_OF_DOCUMENT, getStringForKey(MdekKeys.TYPE_OF_DOCUMENT, documentContext));
		typeOfDocument.addAttribute(ID, getIntegerForKey(MdekKeys.TYPE_OF_DOCUMENT_KEY, documentContext));
		return typeOfDocument;
	}

	private XMLElement createMap(IngridDocument mapContext) {
		XMLElement map = new XMLElement(MAP);
		map.addChild(createHierarchyLevel(mapContext));
		map.addChild(new XMLElement(DESCRIPTION_OF_TECH_DOMAIN, getStringForKey(MdekKeys.DESCRIPTION_OF_TECH_DOMAIN, mapContext)));
		map.addChild(new XMLElement(RESOLUTION, getDoubleForKey(MdekKeys.RESOLUTION, mapContext)));
		map.addChildren(createPublicationScales(mapContext));
		map.addChildren(createKeyCatalogues(mapContext));
		map.addChild(new XMLElement(DEGREE_OF_RECORD, getDoubleForKey(MdekKeys.DEGREE_OF_RECORD, mapContext)));
		map.addChild(new XMLElement(METHOD_OF_PRODUCTION, getStringForKey(MdekKeys.METHOD_OF_PRODUCTION, mapContext)));
		map.addChild(new XMLElement(TECHNICAL_BASE, getStringForKey(MdekKeys.TECHNICAL_BASE, mapContext)));
		map.addChildren(createSymbolCatalogues(mapContext));
		map.addChildren(createSpatialRepresentationTypes(mapContext));
		map.addChild(createVectorFormat(mapContext));
		map.addChild(createGridFormat(mapContext));
		map.addChild(new XMLElement(POS_ACCURACY_VERTICAL, getDoubleForKey(MdekKeys.POS_ACCURACY_VERTICAL, mapContext)));
		map.addChild(new XMLElement(KEYC_INCL_W_DATASET, getIntegerForKey(MdekKeys.KEYC_INCL_W_DATASET, mapContext)));
		map.addChildren(createFeatureTypes(mapContext));
		map.addChildren(createDataBases(mapContext));
		map.addChild(new XMLElement(DATASOURCE_IDENTIFICATOR, getStringForKey(MdekKeys.DATASOURCE_UUID, mapContext)));
		return map;
	}

	private XMLElement createHierarchyLevel(IngridDocument mapContext) {
		XMLElement hierarchyLevel = new XMLElement(HIERARCHY_LEVEL);
		hierarchyLevel.addAttribute(ISO_CODE, getIntegerForKey(MdekKeys.HIERARCHY_LEVEL, mapContext));
		return hierarchyLevel;
	}

	private List<XMLElement> createKeyCatalogues(IngridDocument mapContext) {
		List<XMLElement> keyCatalogues = new ArrayList<>();
		List<IngridDocument> keyCatalogueList = getIngridDocumentListForKey(MdekKeys.KEY_CATALOG_LIST, mapContext);
		for (IngridDocument keyCatalogue : keyCatalogueList) {
			keyCatalogues.add(createKeyCatalogue(keyCatalogue));
		}
		return keyCatalogues;
	}

	private XMLElement createKeyCatalogue(IngridDocument keyCatalogueContext) {
		XMLElement keyCatalogue = new XMLElement(KEY_CATALOGUE);
		keyCatalogue.addChild(createKeyCat(keyCatalogueContext));
		keyCatalogue.addChild(new XMLElement(KEY_DATE, getStringForKey(MdekKeys.KEY_DATE, keyCatalogueContext)));
		keyCatalogue.addChild(new XMLElement(EDITION, getStringForKey(MdekKeys.EDITION, keyCatalogueContext)));
		return keyCatalogue;
	}

	private XMLElement createKeyCat(IngridDocument keyCatalogueContext) {
		XMLElement keyCat = new XMLElement(KEY_CAT, getStringForKey(MdekKeys.SUBJECT_CAT, keyCatalogueContext));
		keyCat.addAttribute(ID, getIntegerForKey(MdekKeys.SUBJECT_CAT_KEY, keyCatalogueContext));
		return keyCat;
	}

	private List<XMLElement> createSymbolCatalogues(IngridDocument mapContext) {
		List<XMLElement> symbolCatalogues = new ArrayList<>();
		List<IngridDocument> symbolCatalogueList = getIngridDocumentListForKey(MdekKeys.SYMBOL_CATALOG_LIST, mapContext);
		for (IngridDocument symbolCatalogue : symbolCatalogueList) {
			symbolCatalogues.add(createSymbolCatalogue(symbolCatalogue));
		}
		return symbolCatalogues;
	}

	private XMLElement createSymbolCatalogue(IngridDocument symbolCatalogueContext) {
		XMLElement symbolCatalogue = new XMLElement(SYMBOL_CATALOGUE);
		symbolCatalogue.addChild(createSymbolCat(symbolCatalogueContext));
		symbolCatalogue.addChild(new XMLElement(SYMBOL_DATE, getStringForKey(MdekKeys.SYMBOL_DATE, symbolCatalogueContext)));
		symbolCatalogue.addChild(new XMLElement(EDITION, getStringForKey(MdekKeys.SYMBOL_EDITION, symbolCatalogueContext)));
		return symbolCatalogue;
	}

	private XMLElement createSymbolCat(IngridDocument symbolCatalogueContext) {
		XMLElement symbolCat = new XMLElement(SYMBOL_CAT, getStringForKey(MdekKeys.SYMBOL_CAT, symbolCatalogueContext));
		symbolCat.addAttribute(ID, getIntegerForKey(MdekKeys.SYMBOL_CAT_KEY, symbolCatalogueContext));
		return symbolCat;
	}

	private List<XMLElement> createSpatialRepresentationTypes(IngridDocument mapContext) {
		List<XMLElement> spatialRepresentationTypes = new ArrayList<>();
		List<Integer> spatialRepIds = getIntegerListForKey(MdekKeys.SPATIAL_REPRESENTATION_TYPE_LIST, mapContext);
		for (Integer spatialRepId : spatialRepIds) {
			XMLElement spatialRepType = new XMLElement(SPATIAL_REPRESENTATION_TYPE);
			spatialRepType.addAttribute(ISO_CODE, spatialRepId);
			spatialRepresentationTypes.add(spatialRepType);
		}
		return spatialRepresentationTypes;
	}

	private XMLElement createVectorFormat(IngridDocument mapContext) {
		XMLElement vectorFormat = new XMLElement(VECTOR_FORMAT);
		vectorFormat.addChildren(createGeoVectors(mapContext));
		return vectorFormat;
	}
	
	private XMLElement createGridFormat(IngridDocument mapContext) {
	    XMLElement gridFormat = new XMLElement(GRID_FORMAT);
	    gridFormat.addChild(new XMLElement(GRID_TRANSFORM_PARAMETER, getStringForKey(MdekKeys.TRANSFORMATION_PARAMETER, mapContext)));
	    gridFormat.addChild(new XMLElement(GRID_NUM_DIMENSIONS, getIntegerForKey(MdekKeys.NUM_DIMENSIONS, mapContext)));
//	    gridFormat.addChild(new XMLElement(GRID_AXIS_NAME, getStringForKey(MdekKeys.AXIS_DIM_NAME, mapContext)));
//	    gridFormat.addChild(new XMLElement(GRID_AXIS_SIZE, getIntegerForKey(MdekKeys.AXIS_DIM_SIZE, mapContext)));
		gridFormat.addChildren(createGridFormatAxis(mapContext));
	    gridFormat.addChild(new XMLElement(GRID_CELL_GEOMETRY, getStringForKey(MdekKeys.CELL_GEOMETRY, mapContext)));
	    gridFormat.addChild(new XMLElement(GRID_GEO_RECTIFIED, getStringForKey(MdekKeys.GEO_RECTIFIED, mapContext)));
	    gridFormat.addChild(new XMLElement(GRID_RECT_CHECKPOINT, getStringForKey(MdekKeys.GEO_RECT_CHECKPOINT, mapContext)));
	    gridFormat.addChild(new XMLElement(GRID_RECT_DESCRIPTION, getStringForKey(MdekKeys.GEO_RECT_DESCRIPTION, mapContext)));
	    gridFormat.addChild(new XMLElement(GRID_RECT_CORNER_POINT, getStringForKey(MdekKeys.GEO_RECT_CORNER_POINT, mapContext)));
	    gridFormat.addChild(new XMLElement(GRID_RECT_POINT_IN_PIXEL, getStringForKey(MdekKeys.GEO_RECT_POINT_IN_PIXEL, mapContext)));
	    gridFormat.addChild(new XMLElement(GRID_REF_CONTROL_POINT, getStringForKey(MdekKeys.GEO_REF_CONTROL_POINT, mapContext)));
	    gridFormat.addChild(new XMLElement(GRID_REF_ORIENTATION_PARAM, getStringForKey(MdekKeys.GEO_REF_ORIENTATION_PARAM, mapContext)));
	    gridFormat.addChild(new XMLElement(GRID_REF_REFERENCED_PARAM, getStringForKey(MdekKeys.GEO_REF_PARAMETER, mapContext)));
	    return gridFormat;
	}

	private XMLElement createVectorTopologyLevel(IngridDocument mapContext) {
		XMLElement vectorTopologyLevel = new XMLElement(VECTOR_TOPOLOGY_LEVEL);
		vectorTopologyLevel.addAttribute(ISO_CODE, getIntegerForKey(MdekKeys.VECTOR_TOPOLOGY_LEVEL, mapContext));
		return vectorTopologyLevel;
	}

	private List<XMLElement> createGeoVectors(IngridDocument mapContext) {
		List<XMLElement> geoVectors = new ArrayList<>();
		List<IngridDocument> geoVectorList = getIngridDocumentListForKey(MdekKeys.GEO_VECTOR_LIST, mapContext);
		for (IngridDocument geoVector : geoVectorList) {
			geoVectors.add(createGeoVector(geoVector));
		}
		return geoVectors;
	}

	private XMLElement createGeoVector(IngridDocument geoVectorContext) {
		XMLElement geoVector = new XMLElement(GEO_VECTOR);
		geoVector.addChild(createVectorTopologyLevel(geoVectorContext));
		geoVector.addChild(createGeometricObjectType(geoVectorContext));
		geoVector.addChild(new XMLElement(GEOMETRIC_OBJECT_COUNT, getIntegerForKey(MdekKeys.GEOMETRIC_OBJECT_COUNT, geoVectorContext)));
		return geoVector;
	}

	private XMLElement createGeometricObjectType(IngridDocument geoVectorContext) {
		XMLElement geometricObjectType = new XMLElement(GEOMETRIC_OBJECT_TYPE);
		geometricObjectType.addAttribute(ISO_CODE, getIntegerForKey(MdekKeys.GEOMETRIC_OBJECT_TYPE, geoVectorContext));
		return geometricObjectType;
	}

	private List<XMLElement> createFeatureTypes(IngridDocument mapContext) {
		List<XMLElement> featureTypes = new ArrayList<>();
		List<String> featureTypeList = getStringListForKey(MdekKeys.FEATURE_TYPE_LIST, mapContext);
		for (String featureType : featureTypeList) {
			featureTypes.add(new XMLElement(FEATURE_TYPE, featureType));
		}
		return featureTypes;
	}


	private List<XMLElement> createDataBases(IngridDocument mapContext) {
		List<XMLElement> resultList = new ArrayList<>();
		List<String> data_bases = getStringListForKey(MdekKeys.DATA, mapContext);
		for (String data_base : data_bases) {
			resultList.add(new XMLElement(DATA, data_base));
		}
		return resultList;
	}


	private XMLElement createProject(IngridDocument projectContext) {
		XMLElement project = new XMLElement(PROJECT);
		project.addChild(new XMLElement("description-of-tech-domain", getStringForKey(MdekKeys.DESCRIPTION_OF_TECH_DOMAIN, projectContext)));
		project.addChild(new XMLElement("member-description", getStringForKey(MdekKeys.MEMBER_DESCRIPTION, projectContext)));
		project.addChild(new XMLElement("leader-description", getStringForKey(MdekKeys.LEADER_DESCRIPTION, projectContext)));
		return project;
	}

	private XMLElement createAdditionalInformation() {
		XMLElement additionalInformation = new XMLElement(ADDITIONAL_INFORMATION);
		additionalInformation.addChildren(createDataLanguages());
		additionalInformation.addChild(createMetaDataLanguage());
		additionalInformation.addChildren(createExportTos());
		additionalInformation.addChildren(createLegislations());
		additionalInformation.addChild(createDatasetIntentions());
		additionalInformation.addChildren(createAccessConstraints());
        additionalInformation.addChildren(createUseLimitations());
		additionalInformation.addChildren(createUseConstraints());
		additionalInformation.addChildren(createMediumOptions());
		additionalInformation.addChildren(createDataFormats());
		additionalInformation.addChild(createPublicationCondition());
		additionalInformation.addChild(createDatasetUsage());
		additionalInformation.addChild(createOrderingInstructions());
		additionalInformation.addChildren(createComments());
		additionalInformation.addChildren(createConformities());
		additionalInformation.addChildren(createDQs());
		return additionalInformation;
	}

    private List<XMLElement> createDataLanguages() {
        List<XMLElement> dataLanguages = new ArrayList<>();
        List<IngridDocument> dataLanguageDocs = getIngridDocumentListForKey(MdekKeys.DATA_LANGUAGE_LIST);
        for (IngridDocument dataLanguageDoc : dataLanguageDocs) {
            dataLanguages.add(createDataLanguage(dataLanguageDoc));
        }
        return dataLanguages;
    }
    private XMLElement createDataLanguage(IngridDocument dataLanguageDoc) {
        XMLElement dataLanguage = new XMLElement(DATA_LANGUAGE, getStringForKey(MdekKeys.DATA_LANGUAGE_NAME, dataLanguageDoc));
        dataLanguage.addAttribute(ID, getIntegerForKey(MdekKeys.DATA_LANGUAGE_CODE, dataLanguageDoc));
        return dataLanguage;
    }

	private XMLElement createMetaDataLanguage() {
		XMLElement metaDataLanguage = new XMLElement(METADATA_LANGUAGE, getStringForKey(MdekKeys.METADATA_LANGUAGE_NAME));
		metaDataLanguage.addAttribute(ID, getIntegerForKey(MdekKeys.METADATA_LANGUAGE_CODE));
		return metaDataLanguage;
	}

	private List<XMLElement> createExportTos() {
		List<XMLElement> exportTos = new ArrayList<>();
		List<IngridDocument> exportToIds = getIngridDocumentListForKey(MdekKeys.EXPORT_CRITERIA);
		for (IngridDocument exportTo : exportToIds) {
			exportTos.add(createExportTo(exportTo));
		}
		return exportTos;
	}

	private XMLElement createExportTo(IngridDocument exportToContext) {
		XMLElement exportTo = new XMLElement(EXPORT_TO, getStringForKey(MdekKeys.EXPORT_CRITERION_VALUE, exportToContext));
		exportTo.addAttribute(ID, getIntegerForKey(MdekKeys.EXPORT_CRITERION_KEY, exportToContext));
		return exportTo;
	}
	
	private List<XMLElement> createLegislations() {
		List<XMLElement> legislations = new ArrayList<>();
		List<IngridDocument> legislationIds = getIngridDocumentListForKey(MdekKeys.LEGISLATIONS);
		for (IngridDocument legislation : legislationIds) {
			legislations.add(createLegislation(legislation));
		}
		return legislations;
	}

	private XMLElement createLegislation(IngridDocument legislationContext) {
		XMLElement legislation = new XMLElement(LEGISLATION, getStringForKey(MdekKeys.LEGISLATION_VALUE, legislationContext));
		legislation.addAttribute(ID, getIntegerForKey(MdekKeys.LEGISLATION_KEY, legislationContext));
		return legislation;
	}

	private XMLElement createDatasetIntentions() {
		return new XMLElement(DATASET_INTENTIONS, getStringForKey(MdekKeys.DATASET_INTENTIONS));
	}

	private List<XMLElement> createAccessConstraints() {
		List<XMLElement> accessConstraints = new ArrayList<>();
		List<IngridDocument> accessConstraintList = getIngridDocumentListForKey(MdekKeys.ACCESS_LIST);
		for (IngridDocument accessConstraint : accessConstraintList) {
			accessConstraints.add(createAccessConstraint(accessConstraint));
		}
		return accessConstraints;
	}

	private XMLElement createAccessConstraint(IngridDocument accessConstraintContext) {
		XMLElement accessConstraint = new XMLElement(ACCESS_CONSTRAINT);
		accessConstraint.addChild(createRestriction(accessConstraintContext));
		return accessConstraint;
	}

	private XMLElement createRestriction(IngridDocument accessConstraintContext) {
		XMLElement restriction = new XMLElement(RESTRICTION, getStringForKey(MdekKeys.ACCESS_RESTRICTION_VALUE, accessConstraintContext));
		restriction.addAttribute(ID, getIntegerForKey(MdekKeys.ACCESS_RESTRICTION_KEY, accessConstraintContext));
		return restriction;
	}

    private List<XMLElement> createUseLimitations() {
        List<XMLElement> useLimitations = new ArrayList<>();
        List<IngridDocument> useLimitationList = getIngridDocumentListForKey(MdekKeys.USE_LIST);
        for (IngridDocument useLimitation : useLimitationList) {
            useLimitations.add(createUseLimitation(useLimitation));
        }
        return useLimitations;
    }

    private XMLElement createUseLimitation(IngridDocument useLimitationContext) {
        XMLElement useLimitation = new XMLElement(USE_LIMITATION);
        useLimitation.addChild(createTermsOfUse(useLimitationContext));
        return useLimitation;
    }
    
    private XMLElement createTermsOfUse(IngridDocument useLimitationContext) {
        XMLElement termsOfUse = new XMLElement(TERMS_OF_USE, getStringForKey(MdekKeys.USE_TERMS_OF_USE_VALUE, useLimitationContext));
        termsOfUse.addAttribute(ID, getIntegerForKey(MdekKeys.USE_TERMS_OF_USE_KEY, useLimitationContext));
        return termsOfUse;
    }

	private List<XMLElement> createUseConstraints() {
		List<XMLElement> useConstraints = new ArrayList<>();
		List<IngridDocument> useConstraintList = getIngridDocumentListForKey(MdekKeys.USE_CONSTRAINTS);
		for (IngridDocument useConstraint : useConstraintList) {
			useConstraints.add(createUseConstraint(useConstraint));
		}
		return useConstraints;
	}

	private XMLElement createUseConstraint(IngridDocument useConstraintContext) {
		XMLElement useConstraint = new XMLElement(USE_CONSTRAINT);
		useConstraint.addChild(createLicense(useConstraintContext));
		return useConstraint;
	}

	private XMLElement createLicense(IngridDocument useConstraintContext) {
		XMLElement license = new XMLElement(LICENSE, getStringForKey(MdekKeys.USE_LICENSE_VALUE, useConstraintContext));
		license.addAttribute(ID, getIntegerForKey(MdekKeys.USE_LICENSE_KEY, useConstraintContext));
		license.addAttribute(SOURCE_NOTE, getStringForKey(MdekKeys.USE_LICENSE_SOURCE, useConstraintContext));
		return license;
	}

	private XMLElement createOpenDataCategorys() {
		XMLElement openDataCategorys = new XMLElement(OPEN_DATA_CATEGORIES);

		List<IngridDocument> openDataCategoryDocs = getIngridDocumentListForKey(MdekKeys.OPEN_DATA_CATEGORY_LIST);
		for (IngridDocument openDataCategoryDoc : openDataCategoryDocs) {
			openDataCategorys.addChild(createOpenDataCategory(openDataCategoryDoc));
		}
		return openDataCategorys;
	}
	private XMLElement createOpenDataCategory(IngridDocument openDataCategoryContext) {
		XMLElement openDataCategory = new XMLElement(OPEN_DATA_CATEGORY, getStringForKey(MdekKeys.OPEN_DATA_CATEGORY_VALUE, openDataCategoryContext));
		openDataCategory.addAttribute(ID, getIntegerForKey(MdekKeys.OPEN_DATA_CATEGORY_KEY, openDataCategoryContext));
		return openDataCategory;
	}

	private List<XMLElement> createMediumOptions() {
		List<XMLElement> mediumOptions = new ArrayList<>();
		List<IngridDocument> mediumOptionList = getIngridDocumentListForKey(MdekKeys.MEDIUM_OPTIONS);
		for (IngridDocument mediumOption : mediumOptionList) {
			mediumOptions.add(createMediumOption(mediumOption));
		}
		return mediumOptions;
	}

	private XMLElement createMediumOption(IngridDocument mediumOptionContext) {
		XMLElement mediumOption = new XMLElement(MEDIUM_OPTION);
		mediumOption.addChild(createMediumName(mediumOptionContext));
		mediumOption.addChild(new XMLElement(MEDIUM_NOTE, getStringForKey(MdekKeys.MEDIUM_NOTE, mediumOptionContext)));
		mediumOption.addChild(new XMLElement(TRANSFER_SIZE, getDoubleForKey(MdekKeys.MEDIUM_TRANSFER_SIZE, mediumOptionContext)));
		return mediumOption;
	}

	private XMLElement createMediumName(IngridDocument mediumOptionContext) {
		XMLElement mediumName = new XMLElement(MEDIUM_NAME);
		mediumName.addAttribute(ISO_CODE, getIntegerForKey(MdekKeys.MEDIUM_NAME, mediumOptionContext));
		return mediumName;
	}

	private List<XMLElement> createDataFormats() {
		List<XMLElement> dataFormats = new ArrayList<>();
		List<IngridDocument> dataFormatList = getIngridDocumentListForKey(MdekKeys.DATA_FORMATS);
		for (IngridDocument dataFormat : dataFormatList) {
			dataFormats.add(createDataFormat(dataFormat));
		}
		return dataFormats;
	}

	private XMLElement createDataFormat(IngridDocument dataFormatContext) {
		XMLElement dataFormat = new XMLElement(DATA_FORMAT);
		dataFormat.addChild(createFormatName(dataFormatContext));
		dataFormat.addChild(new XMLElement(VERSION, getStringForKey(MdekKeys.FORMAT_VERSION, dataFormatContext)));
		dataFormat.addChild(new XMLElement(SPECIFICATION, getStringForKey(MdekKeys.FORMAT_SPECIFICATION, dataFormatContext)));
		dataFormat.addChild(new XMLElement(FILE_DECOMPRESSION_TECHNIQUE, getStringForKey(MdekKeys.FORMAT_FILE_DECOMPRESSION_TECHNIQUE, dataFormatContext)));
		return dataFormat;
	}

	private XMLElement createFormatName(IngridDocument dataFormatContext) {
		XMLElement formatName = new XMLElement(FORMAT_NAME, getStringForKey(MdekKeys.FORMAT_NAME, dataFormatContext));
		formatName.addAttribute(ID, getIntegerForKey(MdekKeys.FORMAT_NAME_KEY, dataFormatContext));
		return formatName;
	}

	private XMLElement createPublicationCondition() {
		return new XMLElement(PUBLICATION_CONDITION, getIntegerForKey(MdekKeys.PUBLICATION_CONDITION));
	}

	private XMLElement createDatasetUsage() {
		return new XMLElement(DATASET_USAGE, getStringForKey(MdekKeys.DATASET_USAGE));
	}

	private XMLElement createOrderingInstructions() {
		return new XMLElement(ORDERING_INSTRUCTIONS, getStringForKey(MdekKeys.ORDERING_INSTRUCTIONS));
	}

	private List<XMLElement> createComments() {
		List<XMLElement> comments = new ArrayList<>();
		List<IngridDocument> commentList = getIngridDocumentListForKey(MdekKeys.COMMENT_LIST);
		for (IngridDocument comment : commentList) {
			comments.add(createComment(comment));
		}
		return comments;
	}

	private XMLElement createComment(IngridDocument commentContext) {
		XMLElement comment = new XMLElement(COMMENT);
		comment.addChild(new XMLElement(COMMENT_CONTENT, getStringForKey(MdekKeys.COMMENT, commentContext)));
		comment.addChild(createCreatorIdentifier(commentContext));
		comment.addChild(new XMLElement(DATE_OF_CREATION, getStringForKey(MdekKeys.CREATE_TIME, commentContext)));
		return comment;
	}

	private XMLElement createCreatorIdentifier(IngridDocument commentContext) {
		IngridDocument createUser = getIngridDocumentForKey(MdekKeys.CREATE_USER, commentContext);
		return new XMLElement(CREATOR_IDENTIFIER, getStringForKey(MdekKeys.UUID, createUser));
	}

	private List<XMLElement> createConformities() {
		List<XMLElement> conformities = new ArrayList<>();
		List<IngridDocument> conformityList = getIngridDocumentListForKey(MdekKeys.CONFORMITY_LIST);
		for (IngridDocument conformity : conformityList) {
			conformities.add(createConformity(conformity));
		}
		return conformities;
	}

	private XMLElement createConformity(IngridDocument conformityContext) {
		XMLElement conformity = new XMLElement(CONFORMITY);
		conformity.addChild(createConformityIsInspire(conformityContext));
		conformity.addChild(createConformitySpecification(conformityContext));
		conformity.addChild(createConformityDegree(conformityContext));
		conformity.addChild(createConformityPublicationDate(conformityContext));
		conformity.addChild(createConformityExplanation(conformityContext));
		return conformity;
	}

	private XMLElement createConformityIsInspire(IngridDocument conformityContext) {
		XMLElement conformityIsInspire = new XMLElement(CONFORMITY_IS_INSPIRE, getStringForKey(MdekKeys.CONFORMITY_IS_INSPIRE, conformityContext));
		return conformityIsInspire;
	}

	private XMLElement createConformitySpecification(IngridDocument conformityContext) {
		XMLElement conformitySpecification = new XMLElement(CONFORMITY_SPECIFICATION, getStringForKey(MdekKeys.CONFORMITY_SPECIFICATION_VALUE, conformityContext));
		conformitySpecification.addAttribute(ID, getIntegerForKey(MdekKeys.CONFORMITY_SPECIFICATION_KEY, conformityContext));
		return conformitySpecification;
	}

	private XMLElement createConformityDegree(IngridDocument conformityContext) {
		XMLElement conformityDegree = new XMLElement(CONFORMITY_DEGREE, getStringForKey(MdekKeys.CONFORMITY_DEGREE_VALUE, conformityContext));
		conformityDegree.addAttribute(ID, getIntegerForKey(MdekKeys.CONFORMITY_DEGREE_KEY, conformityContext));
		return conformityDegree;
	}

	private XMLElement createConformityPublicationDate(IngridDocument conformityContext) {
		XMLElement conformityPublicationDate = new XMLElement(CONFORMITY_PUBLICATION_DATE, getStringForKey(MdekKeys.CONFORMITY_PUBLICATION_DATE, conformityContext));
		return conformityPublicationDate;
	}

	private XMLElement createConformityExplanation(IngridDocument conformityContext) {
		XMLElement conformityExplanation = new XMLElement(CONFORMITY_EXPLANATION, getStringForKey(MdekKeys.CONFORMITY_EXPLANATION, conformityContext));
		return conformityExplanation;
	}

	private List<XMLElement> createDQs() {
		List<XMLElement> elementList = new ArrayList<>();
		List<IngridDocument> dqList = getIngridDocumentListForKey(MdekKeys.DATA_QUALITY_LIST);
		for (IngridDocument dq : dqList) {
			elementList.add(createDQ(dq));
		}
		return elementList;
	}

	private XMLElement createDQ(IngridDocument dqDoc) {
		XMLElement dqElem = new XMLElement(DATA_QUALITY);
		dqElem.addChild(new XMLElement(DQ_ELEMENT_ID, getIntegerForKey(MdekKeys.DQ_ELEMENT_ID, dqDoc)));
		dqElem.addChild(createNameOfMeasure(dqDoc));
		dqElem.addChild(new XMLElement(DQ_RESULT_VALUE, getStringForKey(MdekKeys.RESULT_VALUE, dqDoc)));
		dqElem.addChild(new XMLElement(DQ_MEASURE_DESCRIPTION, getStringForKey(MdekKeys.MEASURE_DESCRIPTION, dqDoc)));
		return dqElem;
	}

	private XMLElement createNameOfMeasure(IngridDocument dqDoc) {
		XMLElement nameOfMeasureElem = new XMLElement(DQ_NAME_OF_MEASURE, getStringForKey(MdekKeys.NAME_OF_MEASURE_VALUE, dqDoc));
		nameOfMeasureElem.addAttribute(ID, getIntegerForKey(MdekKeys.NAME_OF_MEASURE_KEY, dqDoc));
		return nameOfMeasureElem;
	}


	private XMLElement createSpatialDomain() {
		XMLElement spatialDomain = new XMLElement(SPATIAL_DOMAIN);
		spatialDomain.addChildren(createCoordinateSystems());
		spatialDomain.addChild(createDescriptionOfSpatialDomain());
		spatialDomain.addChild(createVerticalExtent());
		spatialDomain.addChildren(createGeoLocations());
		return spatialDomain;
	}

	private List<XMLElement> createCoordinateSystems() {
		List<XMLElement> elements = new ArrayList<>();
		List<IngridDocument> docs = getIngridDocumentListForKey(MdekKeys.SPATIAL_SYSTEM_LIST);
		for (IngridDocument doc : docs) {
			elements.add(createCoordinateSystem(doc));
		}
		return elements;
	}

	private XMLElement createCoordinateSystem(IngridDocument coordDoc) {
		XMLElement coordinateSystem = new XMLElement(COORDINATE_SYSTEM, getStringForKey(MdekKeys.COORDINATE_SYSTEM, coordDoc));
		coordinateSystem.addAttribute(ID, getIntegerForKey(MdekKeys.REFERENCESYSTEM_ID, coordDoc));
		return coordinateSystem;
	}
	
	private XMLElement createDescriptionOfSpatialDomain() {
		return new XMLElement(DESCRIPTION_OF_SPATIAL_DOMAIN, getStringForKey(MdekKeys.DESCRIPTION_OF_SPATIAL_DOMAIN));
	}

	private XMLElement createVerticalExtent() {
		XMLElement verticalExtent = new XMLElement(VERTICAL_EXTENT);
		verticalExtent.addChild(new XMLElement(VERTICAL_EXTENT_MINIMUM, getDoubleForKey(MdekKeys.VERTICAL_EXTENT_MINIMUM)));
		verticalExtent.addChild(new XMLElement(VERTICAL_EXTENT_MAXIMUM, getDoubleForKey(MdekKeys.VERTICAL_EXTENT_MAXIMUM)));
		verticalExtent.addChild(createVerticalExtentUnit());
		verticalExtent.addChild(createVerticalExtentVDatum());
		return verticalExtent;
	}

	private XMLElement createVerticalExtentUnit() {
		XMLElement verticalExtentUnit = new XMLElement(VERTICAL_EXTENT_UNIT);
		verticalExtentUnit.addAttribute(ID, getIntegerForKey(MdekKeys.VERTICAL_EXTENT_UNIT));
		return verticalExtentUnit;
	}
	
	private XMLElement createVerticalExtentVDatum() {
		XMLElement verticalExtentVDatum = new XMLElement(VERTICAL_EXTENT_VDATUM, getStringForKey(MdekKeys.VERTICAL_EXTENT_VDATUM_VALUE));
		verticalExtentVDatum.addAttribute(ID, getIntegerForKey(MdekKeys.VERTICAL_EXTENT_VDATUM_KEY));
		return verticalExtentVDatum;
	}

	private List<XMLElement> createGeoLocations() {
		List<XMLElement> geoLocations = new ArrayList<>();
		List<IngridDocument> geoLocationList = getIngridDocumentListForKey(MdekKeys.LOCATIONS);
		for (IngridDocument geoLocation : geoLocationList) {
			geoLocations.add(createGeoLocation(geoLocation));
		}
		return geoLocations;
	}

	private XMLElement createGeoLocation(IngridDocument geoLocationContext) {
		XMLElement geoLocation = new XMLElement(GEO_LOCATION);
		if (isControlledLocation(geoLocationContext)) {
			geoLocation.addChild(createControlledLocation(geoLocationContext));

		} else {
			geoLocation.addChild(createUncontrolledLocation(geoLocationContext));
		}
		geoLocation.addChild(createBoundingCoordinates(geoLocationContext));
		return geoLocation;
	}

	private boolean isControlledLocation(IngridDocument location) {
		String locationType = MdekUtils.SpatialReferenceType.GEO_THESAURUS.getDbValue();
		return locationType.equals(getStringForKey(MdekKeys.LOCATION_TYPE, location));
	}

	private XMLElement createControlledLocation(IngridDocument geoLocationContext) {
		XMLElement controlledLocation = new XMLElement(CONTROLLED_LOCATION);
		controlledLocation.addChild(createLocationName(geoLocationContext));
		controlledLocation.addChild(new XMLElement(TOPIC_TYPE, getStringForKey(MdekKeys.SNS_TOPIC_TYPE, geoLocationContext)));
		controlledLocation.addChild(new XMLElement(LOCATION_CODE, getStringForKey(MdekKeys.LOCATION_CODE, geoLocationContext)));
		return controlledLocation;
	}
	
	private XMLElement createLocationName(IngridDocument geoLocationContext) {
		XMLElement locationName = new XMLElement(LOCATION_NAME, getStringForKey(MdekKeys.LOCATION_NAME, geoLocationContext));
		if (isControlledLocation(geoLocationContext)) {
			locationName.addAttribute(ID, getStringForKey(MdekKeys.LOCATION_SNS_ID, geoLocationContext));

		} else {
			locationName.addAttribute(ID, getIntegerForKey(MdekKeys.LOCATION_NAME_KEY, geoLocationContext));
		}
		return locationName;
	}

	private XMLElement createUncontrolledLocation(IngridDocument geoLocationContext) {
		XMLElement uncontrolledLocation = new XMLElement(UNCONTROLLED_LOCATION);
		uncontrolledLocation.addChild(createLocationName(geoLocationContext));
		return uncontrolledLocation;
	}

	private XMLElement createBoundingCoordinates(IngridDocument geoLocationContext) {
		XMLElement boundingCoordinates = new XMLElement(BOUNDING_COORDINATES);
		boundingCoordinates.addChild(new XMLElement(WEST_BOUNDING_COORDINATE, getDoubleForKey(MdekKeys.WEST_BOUNDING_COORDINATE, geoLocationContext)));
		boundingCoordinates.addChild(new XMLElement(EAST_BOUNDING_COORDINATE, getDoubleForKey(MdekKeys.EAST_BOUNDING_COORDINATE, geoLocationContext)));
		boundingCoordinates.addChild(new XMLElement(NORTH_BOUNDING_COORDINATE, getDoubleForKey(MdekKeys.NORTH_BOUNDING_COORDINATE, geoLocationContext)));
		boundingCoordinates.addChild(new XMLElement(SOUTH_BOUNDING_COORDINATE, getDoubleForKey(MdekKeys.SOUTH_BOUNDING_COORDINATE, geoLocationContext)));
		return boundingCoordinates;
	}

	private XMLElement createTemporalDomain() {
		XMLElement temporalDomain = new XMLElement(TEMPORAL_DOMAIN);
		temporalDomain.addChild(createDescriptionOfTemporalDomain());
		temporalDomain.addChild(createBeginningDate());
		temporalDomain.addChild(createEndingDate());
		temporalDomain.addChild(createTimeStep());
		temporalDomain.addChild(createTimeScale());
		temporalDomain.addChild(createTimePeriod());
		temporalDomain.addChild(createTimeStatus());
		temporalDomain.addChild(createTimeType());
		temporalDomain.addChildren(createDatasetReferences());
		return temporalDomain;
	}

	private XMLElement createDescriptionOfTemporalDomain() {
		return new XMLElement(DESCRIPTION_OF_TEMPORAL_DOMAIN, getStringForKey(MdekKeys.DESCRIPTION_OF_TEMPORAL_DOMAIN));
	}

	private XMLElement createBeginningDate() {
		return new XMLElement(BEGINNING_DATE, getStringForKey(MdekKeys.BEGINNING_DATE));
	}

	private XMLElement createEndingDate() {
		return new XMLElement(ENDING_DATE, getStringForKey(MdekKeys.ENDING_DATE));
	}

	private XMLElement createTimeStep() {
		return new XMLElement(TIME_STEP, getStringForKey(MdekKeys.TIME_STEP));
	}

	private XMLElement createTimeScale() {
		return new XMLElement(TIME_SCALE, getStringForKey(MdekKeys.TIME_SCALE));
	}

	private XMLElement createTimePeriod() {
		XMLElement timePeriod = new XMLElement(TIME_PERIOD);
		timePeriod.addAttribute(ISO_CODE, getIntegerForKey(MdekKeys.TIME_PERIOD));
		return timePeriod;
	}

	private XMLElement createTimeStatus() {
		XMLElement timeStatus = new XMLElement(TIME_STATUS);
		timeStatus.addAttribute(ISO_CODE, getIntegerForKey(MdekKeys.TIME_STATUS));
		return timeStatus;
	}

	private XMLElement createTimeType() {
		return new XMLElement(TIME_TYPE, getStringForKey(MdekKeys.TIME_TYPE));
	}

	private List<XMLElement> createDatasetReferences() {
		List<XMLElement> datasetReferences = new ArrayList<>();
		List<IngridDocument> datasetReferenceList = getIngridDocumentListForKey(MdekKeys.DATASET_REFERENCES);
		for (IngridDocument datasetReference : datasetReferenceList) {
			datasetReferences.add(createDatasetReference(datasetReference));
		}
		return datasetReferences;
	}

	private XMLElement createDatasetReference(IngridDocument datasetReferenceContext) {
		XMLElement datasetReference = new XMLElement(DATASET_REFERENCE);
		datasetReference.addChild(new XMLElement(DATASET_REFERENCE_DATE, getStringForKey(MdekKeys.DATASET_REFERENCE_DATE, datasetReferenceContext)));
		datasetReference.addChild(createDatasetReferenceType(datasetReferenceContext));
		return datasetReference;
	}

	private XMLElement createDatasetReferenceType(IngridDocument datasetReferenceContext) {
		XMLElement datasetReferenceType = new XMLElement(DATASET_REFERENCE_TYPE);
		datasetReferenceType.addAttribute(ISO_CODE, getIntegerForKey(MdekKeys.DATASET_REFERENCE_TYPE, datasetReferenceContext));
		return datasetReferenceType;
	}

	private List<XMLElement> createAvailableLinkages() {
		List<XMLElement> availableLinkages = new ArrayList<>();
		List<IngridDocument> availableLinkageList = getIngridDocumentListForKey(MdekKeys.LINKAGES);
		for (IngridDocument linkage : availableLinkageList) {
			availableLinkages.add(createAvailableLinkage(linkage));
		}
		return availableLinkages;
	}

	private XMLElement createAvailableLinkage(IngridDocument availableLinkageContext) {
		XMLElement availableLinkage = new XMLElement(AVAILABLE_LINKAGE);
		availableLinkage.addChild(new XMLElement(LINKAGE_NAME, getStringForKey(MdekKeys.LINKAGE_NAME, availableLinkageContext)));
		availableLinkage.addChild(new XMLElement(LINKAGE_URL, getStringForKey(MdekKeys.LINKAGE_URL, availableLinkageContext)));
		availableLinkage.addChild(new XMLElement(LINKAGE_URL_TYPE, getIntegerForKey(MdekKeys.LINKAGE_URL_TYPE, availableLinkageContext)));
		availableLinkage.addChild(createLinkageReference(availableLinkageContext));
		availableLinkage.addChild(createLinkageDatatype(availableLinkageContext));
		availableLinkage.addChild(new XMLElement(LINKAGE_DESCRIPTION, getStringForKey(MdekKeys.LINKAGE_DESCRIPTION, availableLinkageContext)));
		return availableLinkage;
	}

	private XMLElement createLinkageReference(IngridDocument linkageContext) {
		XMLElement linkageReference = new XMLElement(LINKAGE_REFERENCE, getStringForKey(MdekKeys.LINKAGE_REFERENCE, linkageContext));
		linkageReference.addAttribute(ID, getIntegerForKey(MdekKeys.LINKAGE_REFERENCE_ID, linkageContext));
		return linkageReference;
	}

	private XMLElement createLinkageDatatype(IngridDocument linkageContext) {
		XMLElement linkageDatatype = new XMLElement(LINKAGE_DATATYPE, getStringForKey(MdekKeys.LINKAGE_DATATYPE, linkageContext));
		linkageDatatype.addAttribute(ID, getIntegerForKey(MdekKeys.LINKAGE_DATATYPE_KEY, linkageContext));
		return linkageDatatype;
	}

	private XMLElement createParentDataSource() {
		XMLElement parentDataSource = new XMLElement(PARENT_DATA_SOURCE);
		parentDataSource.addChild(new XMLElement(OBJECT_IDENTIFIER, getStringForKey(MdekKeys.PARENT_UUID)));
		return parentDataSource;
	}

	private List<XMLElement> createRelatedAddresses() {
		List<XMLElement> relatedAddresses = new ArrayList<>();
		List<IngridDocument> relatedAddressList = getIngridDocumentListForKey(MdekKeys.ADR_REFERENCES_TO);

		for (IngridDocument relatedAddress : relatedAddressList) {
			relatedAddresses.add(createRelatedAddress(relatedAddress));
		}
		return relatedAddresses;
	}

	private XMLElement createRelatedAddress(IngridDocument addressContext) {
		XMLElement relatedAddress = new XMLElement(RELATED_ADDRESS);
		relatedAddress.addChild(createTypeOfRelation(addressContext));
		relatedAddress.addChild(new XMLElement(ADDRESS_IDENTIFIER, getStringForKey(MdekKeys.UUID, addressContext)));
		relatedAddress.addChild(new XMLElement(DATE_OF_LAST_MODIFICATION, getStringForKey(MdekKeys.RELATION_DATE_OF_LAST_MODIFICATION, addressContext)));
		return relatedAddress;
	}

	private XMLElement createTypeOfRelation(IngridDocument addressContext) {
		XMLElement typeOfRelation = new XMLElement(TYPE_OF_RELATION, getStringForKey(MdekKeys.RELATION_TYPE_NAME, addressContext));
		typeOfRelation.addAttribute(LIST_ID, getIntegerForKey(MdekKeys.RELATION_TYPE_REF, addressContext));
		typeOfRelation.addAttribute(ENTRY_ID, getIntegerForKey(MdekKeys.RELATION_TYPE_ID, addressContext));
		return typeOfRelation;
	}

	private List<XMLElement> createLinkDataSources() {
		List<XMLElement> linkDataSources = new ArrayList<>();
		List<IngridDocument> linkDataSourceList = getIngridDocumentListForKey(MdekKeys.OBJ_REFERENCES_TO);
		for (IngridDocument linkDataSource : linkDataSourceList) {
			linkDataSources.add(createLinkDataSource(linkDataSource));
		}
		return linkDataSources;
	}

	private XMLElement createLinkDataSource(IngridDocument linkDataSourceContext) {
		XMLElement linkDataSource = new XMLElement(LINK_DATA_SOURCE);
		linkDataSource.addChild(createObjectLinkType(linkDataSourceContext));
		linkDataSource.addChild(new XMLElement(OBJECT_LINK_DESCRIPTION, getStringForKey(MdekKeys.RELATION_DESCRIPTION, linkDataSourceContext)));
		linkDataSource.addChild(new XMLElement(OBJECT_IDENTIFIER, getStringForKey(MdekKeys.UUID, linkDataSourceContext)));
		return linkDataSource;
	}
	
	private XMLElement createObjectLinkType(IngridDocument linkDataSourceContext) {
		XMLElement objectLinkType = new XMLElement(OBJECT_LINK_TYPE, getStringForKey(MdekKeys.RELATION_TYPE_NAME, linkDataSourceContext));
		objectLinkType.addAttribute(ID, getIntegerForKey(MdekKeys.RELATION_TYPE_REF, linkDataSourceContext));
		return objectLinkType;
	}

}
