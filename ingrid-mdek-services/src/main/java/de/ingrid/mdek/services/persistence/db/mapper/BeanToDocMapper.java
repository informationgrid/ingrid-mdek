/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2016 wemove digital solutions GmbH
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
package de.ingrid.mdek.services.persistence.db.mapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekUtils.IdcEntityType;
import de.ingrid.mdek.MdekUtils.IdcEntityVersion;
import de.ingrid.mdek.MdekUtils.UserOperation;
import de.ingrid.mdek.MdekUtils.WorkState;
import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.IEntity;
import de.ingrid.mdek.services.persistence.db.dao.IAddressNodeDao;
import de.ingrid.mdek.services.persistence.db.model.AdditionalFieldData;
import de.ingrid.mdek.services.persistence.db.model.AddressComment;
import de.ingrid.mdek.services.persistence.db.model.AddressMetadata;
import de.ingrid.mdek.services.persistence.db.model.AddressNode;
import de.ingrid.mdek.services.persistence.db.model.ObjectAccess;
import de.ingrid.mdek.services.persistence.db.model.ObjectComment;
import de.ingrid.mdek.services.persistence.db.model.ObjectConformity;
import de.ingrid.mdek.services.persistence.db.model.ObjectDataQuality;
import de.ingrid.mdek.services.persistence.db.model.ObjectFormatInspire;
import de.ingrid.mdek.services.persistence.db.model.ObjectMetadata;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;
import de.ingrid.mdek.services.persistence.db.model.ObjectOpenDataCategory;
import de.ingrid.mdek.services.persistence.db.model.ObjectReference;
import de.ingrid.mdek.services.persistence.db.model.ObjectTypesCatalogue;
import de.ingrid.mdek.services.persistence.db.model.ObjectUse;
import de.ingrid.mdek.services.persistence.db.model.ObjectUseConstraint;
import de.ingrid.mdek.services.persistence.db.model.SearchtermAdr;
import de.ingrid.mdek.services.persistence.db.model.SearchtermObj;
import de.ingrid.mdek.services.persistence.db.model.SearchtermSns;
import de.ingrid.mdek.services.persistence.db.model.SearchtermValue;
import de.ingrid.mdek.services.persistence.db.model.SpatialRefSns;
import de.ingrid.mdek.services.persistence.db.model.SpatialRefValue;
import de.ingrid.mdek.services.persistence.db.model.SpatialReference;
import de.ingrid.mdek.services.persistence.db.model.SpatialSystem;
import de.ingrid.mdek.services.persistence.db.model.SysGenericKey;
import de.ingrid.mdek.services.persistence.db.model.SysList;
import de.ingrid.mdek.services.persistence.db.model.T0110AvailFormat;
import de.ingrid.mdek.services.persistence.db.model.T0112MediaOption;
import de.ingrid.mdek.services.persistence.db.model.T0113DatasetReference;
import de.ingrid.mdek.services.persistence.db.model.T0114EnvTopic;
import de.ingrid.mdek.services.persistence.db.model.T011ObjData;
import de.ingrid.mdek.services.persistence.db.model.T011ObjDataPara;
import de.ingrid.mdek.services.persistence.db.model.T011ObjGeo;
import de.ingrid.mdek.services.persistence.db.model.T011ObjGeoScale;
import de.ingrid.mdek.services.persistence.db.model.T011ObjGeoSpatialRep;
import de.ingrid.mdek.services.persistence.db.model.T011ObjGeoSupplinfo;
import de.ingrid.mdek.services.persistence.db.model.T011ObjGeoSymc;
import de.ingrid.mdek.services.persistence.db.model.T011ObjGeoVector;
import de.ingrid.mdek.services.persistence.db.model.T011ObjLiterature;
import de.ingrid.mdek.services.persistence.db.model.T011ObjProject;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServ;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServOpConnpoint;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServOpDepends;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServOpPara;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServOpPlatform;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServOperation;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServScale;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServType;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServUrl;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServVersion;
import de.ingrid.mdek.services.persistence.db.model.T011ObjTopicCat;
import de.ingrid.mdek.services.persistence.db.model.T012ObjAdr;
import de.ingrid.mdek.services.persistence.db.model.T014InfoImpart;
import de.ingrid.mdek.services.persistence.db.model.T015Legist;
import de.ingrid.mdek.services.persistence.db.model.T017UrlRef;
import de.ingrid.mdek.services.persistence.db.model.T01Object;
import de.ingrid.mdek.services.persistence.db.model.T021Communication;
import de.ingrid.mdek.services.persistence.db.model.T02Address;
import de.ingrid.mdek.services.persistence.db.model.T03Catalogue;
import de.ingrid.utils.IngridDocument;

/**
 * Singleton encapsulating methods for mapping hibernate beans to ingrid documents.
 * 
 * @author Martin
 */
public class BeanToDocMapper implements IMapper {

	private static final Logger LOG = Logger.getLogger(BeanToDocMapper.class);

	private static BeanToDocMapper myInstance;

	private IAddressNodeDao daoAddressNode;

	/** Get The Singleton */
	public static synchronized BeanToDocMapper getInstance(DaoFactory daoFactory) {
		if (myInstance == null) {
	        myInstance = new BeanToDocMapper(daoFactory);
	      }
		return myInstance;
	}

	private BeanToDocMapper(DaoFactory daoFactory) {
		daoAddressNode = daoFactory.getAddressNodeDao();
	}

	public IngridDocument mapUserOperation(ObjectNode oN, IngridDocument objectDoc) {
		if (oN == null) {
			return objectDoc;
		}

		T01Object o = oN.getT01ObjectWork();
		
		// first check bearbeitung, will be overwritten if special state (NEW, DELETED)
		if (!WorkState.VEROEFFENTLICHT.getDbValue().equals(o.getWorkState())) {
			objectDoc.put(MdekKeys.RESULTINFO_USER_OPERATION, UserOperation.EDITED);
		}

		if (oN.getObjIdPublished() == null) {
			objectDoc.put(MdekKeys.RESULTINFO_USER_OPERATION, UserOperation.NEW);
		}

		// highest priority
		if (MdekUtils.YES.equals(o.getObjectMetadata().getMarkDeleted())) {
			objectDoc.put(MdekKeys.RESULTINFO_USER_OPERATION, UserOperation.DELETED);
		}

		return objectDoc;
	}

	public IngridDocument mapUserOperation(AddressNode aN, IngridDocument addressDoc) {
		if (aN == null) {
			return addressDoc;
		}

		T02Address a = aN.getT02AddressWork();
		
		// first check bearbeitung, will be overwritten if special state (NEW, DELETED)
		if (!WorkState.VEROEFFENTLICHT.getDbValue().equals(a.getWorkState())) {
			addressDoc.put(MdekKeys.RESULTINFO_USER_OPERATION, UserOperation.EDITED);
		}

		if (aN.getAddrIdPublished() == null) {
			addressDoc.put(MdekKeys.RESULTINFO_USER_OPERATION, UserOperation.NEW);
		}

		// highest priority
		if (MdekUtils.YES.equals(a.getAddressMetadata().getMarkDeleted())) {
			addressDoc.put(MdekKeys.RESULTINFO_USER_OPERATION, UserOperation.DELETED);
		}

		return addressDoc;
	}

	/**
	 * Transfer structural info ("hasChild") to passed doc.
	 * @return doc containing additional data.
	 */
	public IngridDocument mapObjectNode(ObjectNode oNIn, IngridDocument objectDoc,
		MappingQuantity howMuch) {
		if (oNIn == null) {
			return objectDoc;
		}

		// published info
		boolean isPublished = (oNIn.getObjIdPublished() == null) ? false : true;
		objectDoc.putBoolean(MdekKeys.IS_PUBLISHED, isPublished);

		// for testing. Needed in Exporter/Importer ?
		objectDoc.put(MdekKeys.PARENT_UUID, oNIn.getFkObjUuid());

		if (howMuch == MappingQuantity.TREE_ENTITY ||
			howMuch == MappingQuantity.COPY_ENTITY) {
			// child info
	    	boolean hasChild = (oNIn.getObjectNodeChildren().size() > 0) ? true : false;
			objectDoc.putBoolean(MdekKeys.HAS_CHILD, hasChild);

			// NOTICE: NO MAPPING OF UUID ! IS MAPPED VIA T01Object, so we can track, whether
			// a specific version exists (working or published version)
		}

		return objectDoc;
	}

	/**
	 * Transfer structural info ("hasChild") to passed doc.
	 * @return doc containing additional data.
	 */
	public IngridDocument mapAddressNode(AddressNode aNIn, IngridDocument addressDoc,
		MappingQuantity howMuch) {
		if (aNIn == null) {
			return addressDoc;
		}

		// published info
		boolean isPublished = (aNIn.getAddrIdPublished() == null) ? false : true;
		addressDoc.putBoolean(MdekKeys.IS_PUBLISHED, isPublished);

		// for testing. Needed in Exporter/Importer ?
		addressDoc.put(MdekKeys.PARENT_UUID, aNIn.getFkAddrUuid());

		if (howMuch == MappingQuantity.TREE_ENTITY ||
			howMuch == MappingQuantity.COPY_ENTITY) {
			// child info
	    	boolean hasChild = (aNIn.getAddressNodeChildren().size() > 0) ? true : false;
			addressDoc.putBoolean(MdekKeys.HAS_CHILD, hasChild);
			
			// NOTICE: NO MAPPING OF UUID ! IS MAPPED VIA T02Address, so we can track, whether
			// a specific version exists (working or published version)
		}

		return addressDoc;
	}

	/**
	 * Transfer object data of passed bean to passed doc.
	 * Also includes all related data (e.g. addresses etc) dependent from MappingQuantity.
	 * @return doc containing additional data.
	 */
	public IngridDocument mapT01Object(T01Object o, IngridDocument objectDoc,
			MappingQuantity howMuch) {
		if (o == null) {
			return objectDoc;
		}

		// supply initial data when entity is created
		if (howMuch == MappingQuantity.INITIAL_ENTITY) {
			objectDoc.put(MdekKeys.PUBLICATION_CONDITION, o.getPublishId());
			// map associations
			mapSearchtermObjs(o.getSearchtermObjs(), objectDoc, howMuch);
			mapT012ObjAdrs(o.getT012ObjAdrs(), objectDoc, howMuch);
			mapT011ObjTopicCats(o.getT011ObjTopicCats(), objectDoc); // INGRID33-6

			return objectDoc;
		}

		// also ID !!! e.g. for fetching related data afterwards or tracking ID in test suite !
		objectDoc.put(MdekKeys.ID, o.getId());
		objectDoc.put(MdekKeys.UUID, o.getObjUuid());
		objectDoc.put(MdekKeys.CLASS, o.getObjClass());
		objectDoc.put(MdekKeys.TITLE, o.getObjName());
		objectDoc.put(MdekKeys.WORK_STATE, o.getWorkState());
		// needed when copying objects ! we always map to track in test suite !
		objectDoc.put(MdekKeys.CATALOGUE_IDENTIFIER, o.getCatId());
		objectDoc.put(MdekKeys.DATE_OF_LAST_MODIFICATION, o.getModTime());
		objectDoc.put(MdekKeys.PUBLICATION_CONDITION, o.getPublishId());

		if (howMuch == MappingQuantity.TREE_ENTITY ||
			howMuch == MappingQuantity.DETAIL_ENTITY ||
			howMuch == MappingQuantity.COPY_DATA ||
			howMuch == MappingQuantity.COPY_ENTITY) 
		{
			// mark deleted also needed in "tree view"
			mapObjectMetadata(o.getObjectMetadata(), objectDoc, MappingQuantity.INITIAL_ENTITY);			
		}

		if (howMuch == MappingQuantity.DETAIL_ENTITY ||
			howMuch == MappingQuantity.COPY_DATA ||
			howMuch == MappingQuantity.COPY_ENTITY) 
		{
			objectDoc.put(MdekKeys.ORIGINAL_CONTROL_IDENTIFIER, o.getOrgObjId());
			objectDoc.put(MdekKeys.DATASET_ALTERNATE_NAME, o.getDatasetAlternateName());
			objectDoc.put(MdekKeys.ABSTRACT, o.getObjDescr());
			objectDoc.put(MdekKeys.DATE_OF_CREATION, o.getCreateTime());

			objectDoc.put(MdekKeys.VERTICAL_EXTENT_MINIMUM, o.getVerticalExtentMinimum());
			objectDoc.put(MdekKeys.VERTICAL_EXTENT_MAXIMUM, o.getVerticalExtentMaximum());
			objectDoc.put(MdekKeys.VERTICAL_EXTENT_UNIT, o.getVerticalExtentUnit());
			objectDoc.put(MdekKeys.VERTICAL_EXTENT_VDATUM_KEY, o.getVerticalExtentVdatumKey());
			objectDoc.put(MdekKeys.VERTICAL_EXTENT_VDATUM_VALUE, o.getVerticalExtentVdatumValue());
			objectDoc.put(MdekKeys.DESCRIPTION_OF_SPATIAL_DOMAIN, o.getLocDescr());

			objectDoc.put(MdekKeys.TIME_TYPE, o.getTimeType());
			objectDoc.put(MdekKeys.BEGINNING_DATE, o.getTimeFrom());
			objectDoc.put(MdekKeys.ENDING_DATE, o.getTimeTo());
			objectDoc.put(MdekKeys.TIME_STATUS, o.getTimeStatus());
			objectDoc.put(MdekKeys.TIME_PERIOD, o.getTimePeriod());
			objectDoc.put(MdekKeys.TIME_STEP, o.getTimeInterval());
			objectDoc.put(MdekKeys.TIME_SCALE, o.getTimeAlle());
			objectDoc.put(MdekKeys.DESCRIPTION_OF_TEMPORAL_DOMAIN, o.getTimeDescr());

			objectDoc.put(MdekKeys.METADATA_LANGUAGE_CODE, o.getMetadataLanguageKey());
			objectDoc.put(MdekKeys.METADATA_LANGUAGE_NAME, o.getMetadataLanguageValue());
			objectDoc.put(MdekKeys.DATA_LANGUAGE_CODE, o.getDataLanguageKey());
			objectDoc.put(MdekKeys.DATA_LANGUAGE_NAME, o.getDataLanguageValue());
			objectDoc.put(MdekKeys.DATASET_INTENTIONS, o.getInfoNote());
			objectDoc.put(MdekKeys.DATASET_USAGE, o.getDatasetUsage());
			objectDoc.put(MdekKeys.DATASET_CHARACTER_SET, o.getDatasetCharacterSet());

			objectDoc.put(MdekKeys.ORDERING_INSTRUCTIONS, o.getOrderingInstructions());
			objectDoc.put(MdekKeys.IS_CATALOG_DATA, o.getIsCatalogData());
			objectDoc.put(MdekKeys.IS_INSPIRE_RELEVANT, o.getIsInspireRelevant());
			objectDoc.put(MdekKeys.IS_OPEN_DATA, o.getIsOpenData());

			// map associations		
			mapObjectReferences(o.getObjectReferences(), objectDoc);
			mapT012ObjAdrs(o.getT012ObjAdrs(), objectDoc, howMuch);
			mapSpatialReferences(o.getSpatialReferences(), objectDoc);
			mapSearchtermObjs(o.getSearchtermObjs(), objectDoc, howMuch);
			mapT017UrlRefs(o.getT017UrlRefs(), objectDoc);
			mapT0113DatasetReferences(o.getT0113DatasetReferences(), objectDoc);
			mapT014InfoImparts(o.getT014InfoImparts(), objectDoc);
			mapT015Legists(o.getT015Legists(), objectDoc);
			mapT0110AvailFormats(o.getT0110AvailFormats(), objectDoc);
			mapT0112MediaOptions(o.getT0112MediaOptions(), objectDoc);
			mapT0114EnvTopics(o.getT0114EnvTopics(), objectDoc);
			mapT011ObjTopicCats(o.getT011ObjTopicCats(), objectDoc);

			// technical domain map (class 1)
			mapT011ObjGeo(o, objectDoc);
			// technical domain document (class 2)
			mapT011ObjLiterature(o.getT011ObjLiteratures(), objectDoc);
			// NOTICE: T011ObjServ is used for the object classes "Geodatendienst" (class 3) AND
			// "Informationssystem/Dienst/Anwendung" (class 6) with DIFFERENT content !
			// we don't distinguish here and map all stuff available !
			mapT011ObjServ(o.getT011ObjServs(), objectDoc);
			// technical domain project (class 4)
			mapT011ObjProject(o.getT011ObjProjects(), objectDoc);
			// technical domain dataset (class 5)
			mapT011ObjData(o, objectDoc);

			// object comments
			mapObjectComments(o.getObjectComments(), objectDoc);
			// additional fields
			mapAdditionalFieldDatas(o.getAdditionalFieldDatas(), objectDoc);
			mapObjectConformitys(o.getObjectConformitys(), objectDoc);
			mapObjectAccesses(o.getObjectAccesss(), objectDoc);
			mapObjectUses(o.getObjectUses(), objectDoc);
            mapObjectUseConstraints(o.getObjectUseConstraints(), objectDoc);
			mapObjectOpenDataCategorys(o.getObjectOpenDataCategorys(), objectDoc);
			mapObjectDataQualitys(o.getObjectDataQualitys(), objectDoc);
			mapObjectFormatInspires(o.getObjectFormatInspires(), objectDoc);
			mapSpatialSystems(o.getSpatialSystems(), objectDoc);

			// map only with initial data ! call mapping method explicitly if more data wanted.
			mapModUser(o.getModUuid(), objectDoc, MappingQuantity.INITIAL_ENTITY);
			mapResponsibleUser(o.getResponsibleUuid(), objectDoc, MappingQuantity.INITIAL_ENTITY);
		}

		if (howMuch == MappingQuantity.COPY_DATA ||
			howMuch == MappingQuantity.COPY_ENTITY) {
			objectDoc.put(MdekKeys.METADATA_CHARACTER_SET, o.getMetadataCharacterSet());
			objectDoc.put(MdekKeys.METADATA_STANDARD_NAME, o.getMetadataStandardName());
			objectDoc.put(MdekKeys.METADATA_STANDARD_VERSION, o.getMetadataStandardVersion());			
		}

		return objectDoc;
	}

	public List<IngridDocument> mapT01Objects(List<T01Object> objs, MappingQuantity howMuch) {
		ArrayList<IngridDocument> objDocs = new ArrayList<IngridDocument>(objs.size());
		for (T01Object obj : objs) {
			IngridDocument objDoc = new IngridDocument();
			mapT01Object(obj, objDoc, howMuch);

			objDocs.add(objDoc);
		}
		
		return objDocs;
	}

	/**
	 * Transfer address data of passed bean to passed doc.
	 * Also includes communication etc. dependent from MappingQuantity.
	 * @return doc containing additional data.
	 */
	public IngridDocument mapT02Address(T02Address a, IngridDocument addressDoc,
			MappingQuantity howMuch) {
		if (a == null) {
			return addressDoc;
		}

		// supply initial data when entity is created
		if (howMuch == MappingQuantity.INITIAL_ENTITY) {
			addressDoc.put(MdekKeys.PUBLICATION_CONDITION, a.getPublishId());
			addressDoc.put(MdekKeys.STREET, a.getStreet());
			addressDoc.put(MdekKeys.COUNTRY_CODE, a.getCountryKey());
			addressDoc.put(MdekKeys.COUNTRY_NAME, a.getCountryValue());
			addressDoc.put(MdekKeys.POSTAL_CODE, a.getPostcode());
			addressDoc.put(MdekKeys.CITY, a.getCity());
			addressDoc.put(MdekKeys.POST_BOX_POSTAL_CODE, a.getPostboxPc());
			addressDoc.put(MdekKeys.POST_BOX, a.getPostbox());

			// map associations
			mapT021Communications(a.getT021Communications(), addressDoc);
			mapSearchtermAdrs(a.getSearchtermAdrs(), addressDoc, howMuch);

			return addressDoc;
		}

		// also ID !!! e.g. for fetching related data afterwards or tracking ID in test suite !
		addressDoc.put(MdekKeys.ID, a.getId());
		addressDoc.put(MdekKeys.UUID, a.getAdrUuid());
		addressDoc.put(MdekKeys.CLASS, a.getAdrType());
		addressDoc.put(MdekKeys.ORGANISATION, a.getInstitution());
		addressDoc.put(MdekKeys.NAME, a.getLastname());
		addressDoc.put(MdekKeys.GIVEN_NAME, a.getFirstname());
		addressDoc.put(MdekKeys.TITLE_OR_FUNCTION, a.getTitleValue());
		addressDoc.put(MdekKeys.TITLE_OR_FUNCTION_KEY, a.getTitleKey());
		addressDoc.put(MdekKeys.WORK_STATE, a.getWorkState());
		addressDoc.put(MdekKeys.DATE_OF_LAST_MODIFICATION, a.getModTime());
		addressDoc.put(MdekKeys.HIDE_ADDRESS, a.getHideAddress());
		addressDoc.put(MdekKeys.PUBLICATION_CONDITION, a.getPublishId());

		if (howMuch == MappingQuantity.TREE_ENTITY ||
			howMuch == MappingQuantity.DETAIL_ENTITY ||
			howMuch == MappingQuantity.COPY_DATA ||
			howMuch == MappingQuantity.COPY_ENTITY)
		{
			// mark deleted also needed in "tree view"
			mapAddressMetadata(a.getAddressMetadata(), addressDoc, MappingQuantity.INITIAL_ENTITY);
		}

		if (howMuch == MappingQuantity.TABLE_ENTITY ||
			howMuch == MappingQuantity.DETAIL_ENTITY ||
			howMuch == MappingQuantity.COPY_DATA ||
			howMuch == MappingQuantity.COPY_ENTITY)
		{
			addressDoc.put(MdekKeys.STREET, a.getStreet());
			addressDoc.put(MdekKeys.COUNTRY_CODE, a.getCountryKey());
			addressDoc.put(MdekKeys.COUNTRY_NAME, a.getCountryValue());
			addressDoc.put(MdekKeys.POSTAL_CODE, a.getPostcode());
			addressDoc.put(MdekKeys.CITY, a.getCity());
			addressDoc.put(MdekKeys.POST_BOX_POSTAL_CODE, a.getPostboxPc());
			addressDoc.put(MdekKeys.POST_BOX, a.getPostbox());

			// map associations
			mapT021Communications(a.getT021Communications(), addressDoc);
		}

		if (howMuch == MappingQuantity.DETAIL_ENTITY ||
			howMuch == MappingQuantity.COPY_DATA ||
			howMuch == MappingQuantity.COPY_ENTITY)
		{
			addressDoc.put(MdekKeys.DATE_OF_CREATION, a.getCreateTime());
			addressDoc.put(MdekKeys.FUNCTION, a.getJob());
			addressDoc.put(MdekKeys.HOURS_OF_SERVICE, a.getHoursOfService());
			addressDoc.put(MdekKeys.NAME_FORM, a.getAddressValue());
			addressDoc.put(MdekKeys.NAME_FORM_KEY, a.getAddressKey());

			// map associations
			mapSearchtermAdrs(a.getSearchtermAdrs(), addressDoc, howMuch);
			mapAddressComments(a.getAddressComments(), addressDoc);

			// map only with initial data ! call mapping method explicitly if more data wanted.
			mapModUser(a.getModUuid(), addressDoc, MappingQuantity.INITIAL_ENTITY);
			mapResponsibleUser(a.getResponsibleUuid(), addressDoc, MappingQuantity.INITIAL_ENTITY);
		}
/*
		if (howMuch == MappingQuantity.COPY_DATA ||
			howMuch == MappingQuantity.COPY_ENTITY) {
			// no further data !
		}
*/
		if (howMuch == MappingQuantity.COPY_ENTITY) {
			addressDoc.put(MdekKeys.ORIGINAL_ADDRESS_IDENTIFIER, a.getOrgAdrId());
		}

		return addressDoc;
	}

	public List<IngridDocument> mapT02Addresses(List<T02Address> addrs, MappingQuantity howMuch) {
		ArrayList<IngridDocument> addrDocs = new ArrayList<IngridDocument>(addrs.size());
		for (T02Address addr : addrs) {
			IngridDocument addrDoc = new IngridDocument();
			mapT02Address(addr, addrDoc, howMuch);

			addrDocs.add(addrDoc);
		}
		
		return addrDocs;
	}

	/** Transfer data to merge from given T02Address to given IngridDoc. */
	public IngridDocument mergeT02Address(T02Address mergeSource, IngridDocument mergeTargetDoc) {
		mergeTargetDoc.put(MdekKeys.STREET, mergeSource.getStreet());
		mergeTargetDoc.put(MdekKeys.COUNTRY_CODE, mergeSource.getCountryKey());
		mergeTargetDoc.put(MdekKeys.COUNTRY_NAME, mergeSource.getCountryValue());
		mergeTargetDoc.put(MdekKeys.POSTAL_CODE, mergeSource.getPostcode());
		mergeTargetDoc.put(MdekKeys.CITY, mergeSource.getCity());
		mergeTargetDoc.put(MdekKeys.POST_BOX_POSTAL_CODE, mergeSource.getPostboxPc());
		mergeTargetDoc.put(MdekKeys.POST_BOX, mergeSource.getPostbox());

		return mergeTargetDoc;
	}

	/** Set passed user as mod user in passed doc.
	 * Quantity determines how much (only uuid or address data). */
	public IngridDocument mapModUser(String userAddrUuid, IngridDocument inDoc,
			MappingQuantity howMuch) {
		// we don't throw Exception if mod user doesn't exist, caused problems, because mod-user
		// isn't checked (and should not ?) when address is deleted 
		IngridDocument userDoc = mapUserAddress(userAddrUuid, new IngridDocument(), howMuch, false);
		inDoc.put(MdekKeys.MOD_USER, userDoc);

		return inDoc;
	}

	/** Set passed address as mod user in passed doc. Map full address data */
	public IngridDocument mapModUser(T02Address aUser, IngridDocument inDoc) {
		IngridDocument userDoc = mapT02Address(aUser, new IngridDocument(), MappingQuantity.BASIC_ENTITY);			
		inDoc.put(MdekKeys.MOD_USER, userDoc);

		return inDoc;
	}

	/** Set passed user as responsible user in passed doc.
	 * Quantity determines how much (only uuid or address data). */
	public IngridDocument mapResponsibleUser(String userAddrUuid, IngridDocument inDoc,
			MappingQuantity howMuch) {
		// we throw Exception if responsible user doesn't exist ! 
		IngridDocument userDoc = mapUserAddress(userAddrUuid, new IngridDocument(), howMuch, true);
		inDoc.put(MdekKeys.RESPONSIBLE_USER, userDoc);

		return inDoc;
	}

	/** Set passed address as responsible user in passed doc. Map full address data */
	public IngridDocument mapResponsibleUser(T02Address aUser, IngridDocument inDoc) {
		IngridDocument userDoc = mapT02Address(aUser, new IngridDocument(), MappingQuantity.BASIC_ENTITY);			
		inDoc.put(MdekKeys.RESPONSIBLE_USER, userDoc);

		return inDoc;
	}

	/** Set passed user as assigner user (to QA) in passed doc.
	 * Quantity determines how much (only uuid or address data). */
	public IngridDocument mapAssignerUser(String userAddrUuid, IngridDocument inDoc,
			MappingQuantity howMuch) {
		// we throw Exception if responsible user doesn't exist ! 
		IngridDocument userDoc = mapUserAddress(userAddrUuid, new IngridDocument(), howMuch, false);
		inDoc.put(MdekKeys.ASSIGNER_USER, userDoc);

		return inDoc;
	}

	private IngridDocument mapUserAddress(String userAddrUuid, IngridDocument inDoc,
			MappingQuantity howMuch,
			boolean throwException) {
		if (userAddrUuid == null) {
			return inDoc;
		}

		if (howMuch == MappingQuantity.INITIAL_ENTITY) {
			inDoc.put(MdekKeys.UUID, userAddrUuid);
			return inDoc;
		}

		AddressNode aN = daoAddressNode.loadByUuid(userAddrUuid, IdcEntityVersion.WORKING_VERSION);
		if (aN == null) {
			LOG.warn("User AddressUuid not found ! userAddrUuid='" + userAddrUuid + "'. We throw UUID_NOT_FOUND Exception.");
			if (throwException) {
				throw new MdekException(new MdekError(MdekErrorType.UUID_NOT_FOUND));				
			}
		} else {
			// map basic data ! WE DON'T NEED MORE !
			mapT02Address(aN.getT02AddressWork(), inDoc, MappingQuantity.BASIC_ENTITY);			
		}
		
		return inDoc;
	}

	public IngridDocument mapObjectComments(Set<ObjectComment> refs, IngridDocument objectDoc) {
		if (refs == null) {
			return objectDoc;
		}
		ArrayList<IngridDocument> docList = new ArrayList<IngridDocument>(refs.size());
		for (ObjectComment ref : refs) {
			IngridDocument refDoc = new IngridDocument();
			refDoc.put(MdekKeys.COMMENT, ref.getComment());
			refDoc.put(MdekKeys.CREATE_TIME, ref.getCreateTime());

			// map "create user"
			// we don't throw Exception if user doesn't exist, may be the case, because isn't replaced when importing
			IngridDocument userDoc = mapUserAddress(ref.getCreateUuid(), new IngridDocument(), MappingQuantity.DETAIL_ENTITY,
					false);
			refDoc.put(MdekKeys.CREATE_USER, userDoc);

			docList.add(refDoc);					
		}
		objectDoc.put(MdekKeys.COMMENT_LIST, docList);
		return objectDoc;
	}

	public IngridDocument mapAdditionalFieldData(AdditionalFieldData ref, IngridDocument refDoc) {
		if (ref == null) {
			return refDoc;
		}

		refDoc.put(MdekKeys.ADDITIONAL_FIELD_KEY, ref.getFieldKey());
		if (ref.getData() != null) {
			refDoc.put(MdekKeys.ADDITIONAL_FIELD_DATA, ref.getData());				
		}
		if (ref.getListItemId() != null) {
			refDoc.put(MdekKeys.ADDITIONAL_FIELD_LIST_ITEM_ID, ref.getListItemId());				
		}
		if (ref.getAdditionalFieldDatas() != null &&
				ref.getAdditionalFieldDatas().size() > 0) {
			List<List<IngridDocument>> rows =
				mapAdditionalFieldDatas(ref.getAdditionalFieldDatas());
			refDoc.put(MdekKeys.ADDITIONAL_FIELD_ROWS, rows);
		}

		return refDoc;
	}
	public IngridDocument mapAdditionalFieldDatas(Set<AdditionalFieldData> refs, IngridDocument objectDoc) {
		if (refs == null) {
			return objectDoc;
		}
		ArrayList<IngridDocument> docList = new ArrayList<IngridDocument>(refs.size());
		for (AdditionalFieldData ref : refs) {
			IngridDocument refDoc = new IngridDocument();
			mapAdditionalFieldData(ref, refDoc);
			docList.add(refDoc);					
		}
		
		objectDoc.put(MdekKeys.ADDITIONAL_FIELDS, docList);
		return objectDoc;			
	}
	
	/** Return rows as List of List<Columns> */ 
	public List<List<IngridDocument>> mapAdditionalFieldDatas(Set<AdditionalFieldData> refs) {
		ArrayList<List<IngridDocument>> rowList = new ArrayList<List<IngridDocument>>();

		int currentRow = -1;
		// List of columns in row !
		ArrayList<IngridDocument> row = null;
		for (AdditionalFieldData ref : refs) {
			int nextRow = ref.getSort();

			// check whether all data of a row is read !
			boolean rowChange = false;
			if (currentRow != -1 && currentRow != nextRow) {
				// row changed, process finished row
				rowChange = true;
				rowList.add(row);
			}

			if (currentRow == -1 || rowChange) {
				// set up next row
				currentRow = nextRow;
				row = new ArrayList<IngridDocument>();
			}
			
			// add column to row
			IngridDocument refDoc = new IngridDocument();
			mapAdditionalFieldData(ref, refDoc);
			row.add(refDoc);
		}
		// also add last row ! not done in loop due to end of loop !
		if (row != null) {
			rowList.add(row);
		}
		
		return rowList;
	}


	public IngridDocument mapAddressComments(Set<AddressComment> refs, IngridDocument addressDoc) {
		if (refs == null) {
			return addressDoc;
		}
		ArrayList<IngridDocument> docList = new ArrayList<IngridDocument>(refs.size());
		for (AddressComment ref : refs) {
			IngridDocument refDoc = new IngridDocument();
			refDoc.put(MdekKeys.COMMENT, ref.getComment());
			refDoc.put(MdekKeys.CREATE_TIME, ref.getCreateTime());

			// map "create user"
			// we don't throw Exception if user doesn't exist, may be the case, because isn't replaced when importing
			IngridDocument userDoc = mapUserAddress(ref.getCreateUuid(), new IngridDocument(), MappingQuantity.DETAIL_ENTITY,
					false);
			refDoc.put(MdekKeys.CREATE_USER, userDoc);

			docList.add(refDoc);					
		}
		addressDoc.put(MdekKeys.COMMENT_LIST, docList);
		return addressDoc;
	}

	public IngridDocument mapAddressMetadata(AddressMetadata ref, IngridDocument refDoc,
			MappingQuantity howMuch) {
		if (ref == null) {
			return refDoc;
		}

		refDoc.put(MdekKeys.ENTITY_METADATA_ID, ref.getId());
		refDoc.put(MdekKeys.LASTEXPORT_TIME, ref.getLastexportTime());
		refDoc.put(MdekKeys.EXPIRY_STATE, ref.getExpiryState());
		refDoc.put(MdekKeys.MARK_DELETED, ref.getMarkDeleted());
		refDoc.put(MdekKeys.ASSIGNER_UUID, ref.getAssignerUuid());
		refDoc.put(MdekKeys.ASSIGN_TIME, ref.getAssignTime());
		refDoc.put(MdekKeys.REASSIGNER_UUID, ref.getReassignerUuid());
		refDoc.put(MdekKeys.REASSIGN_TIME, ref.getReassignTime());

		// also detailed assigner user if requested !
		mapAssignerUser(ref.getAssignerUuid(), refDoc, howMuch);

		return refDoc;
	}

	/**
	 * Transfer object relation data of passed bean to passed doc.
	 * @return doc containing additional data.
	 */
	private IngridDocument mapObjectReference(ObjectReference oR, IngridDocument objectDoc) {
		if (oR == null) {
			return objectDoc;
		}

		objectDoc.put(MdekKeys.RELATION_TYPE_REF, oR.getSpecialRef());
		objectDoc.put(MdekKeys.RELATION_TYPE_NAME, oR.getSpecialName());
		objectDoc.put(MdekKeys.RELATION_DESCRIPTION, oR.getDescr());

		return objectDoc;
	}

	private IngridDocument mapObjectReferences(Set<ObjectReference> oRefs, IngridDocument objectDoc) {
		if (oRefs == null) {
			return objectDoc;
		}
		ArrayList<IngridDocument> objsList = new ArrayList<IngridDocument>(oRefs.size());
		for (ObjectReference oRef : oRefs) {
			IngridDocument oToDoc = new IngridDocument();
			mapObjectReference(oRef, oToDoc);
			ObjectNode oNode = oRef.getObjectNode();
			if (oNode != null) {
				T01Object oTo = oNode.getT01ObjectWork();
				mapT01Object(oTo, oToDoc, MappingQuantity.TABLE_ENTITY);
				objsList.add(oToDoc);					
			} else {
				LOG.warn("Object " + oRef.getObjToUuid() + " has no ObjectNode !!! We skip this object reference.");
			}
		}
		objectDoc.put(MdekKeys.OBJ_REFERENCES_TO, objsList);
		
		return objectDoc;
	}

	/**
	 * Transfer From-objectReferences (passed beans) to passed doc.
	 * @param oNodesFrom "from-object references" in an array of lists !<br>
	 * - index 0: list of objects referencing the given uuid ONLY in their published
	 * 		version (and NOT in their work version -> ref deleted in work version)<br>
	 * - index 1: list of objects referencing the given uuid in their working version
	 * 		(which might equal the published version)<br>
	 * @param oNodesFrom_startIndex index of first object (when paging) -> pass null if no paging
	 * @param oNodesFrom_totalNum total num of objects (when paging) -> pass null if no paging
	 * @param toEntityType type of to entity (object or address) 
	 * @param toEntityUuid uuid of to entity
	 * @param toEntityDoc doc of to entity where data is added.
	 * @param howMuch how much data should be added
	 * @return toEntityDoc containing additional data.
	 */
	public IngridDocument mapObjectReferencesFrom(List<ObjectNode> oNodesFrom[],
			Integer oNodesFrom_startIndex,
			Integer oNodesFrom_totalNum,
			IdcEntityType toEntityType,
			String toEntityUuid,
			IngridDocument toEntityDoc,
			MappingQuantity howMuch) {
		if (oNodesFrom == null) {
			return toEntityDoc;
		}

		int INDEX_REFS_PUBLISHED = 0;
		int INDEX_REFS_WORK = 1;

		List<IngridDocument> docLists[] = new ArrayList[oNodesFrom.length];
		for (int i=0; i < oNodesFrom.length; i++) {
			List<ObjectNode> nodeList = oNodesFrom[i];
			List<IngridDocument> refDocList = new ArrayList<IngridDocument>(nodeList.size());
			// map every node to IngridDoc
			for (ObjectNode oN : nodeList) {
				// map working or published version, dependent from list !
				T01Object oFrom;
				if (i == INDEX_REFS_PUBLISHED) {
					oFrom = oN.getT01ObjectPublished();
				} else {
					oFrom = oN.getT01ObjectWork();
				}
				if (toEntityType == IdcEntityType.OBJECT) {
					// for objects add EVERY reference and also map relation info !!!
					Set<ObjectReference> oRefs = oFrom.getObjectReferences();
					for (ObjectReference oRef : oRefs) {
						if (toEntityUuid.equals(oRef.getObjToUuid())) {
							IngridDocument oFromDoc = new IngridDocument();
							mapT01Object(oFrom, oFromDoc, howMuch);
							mapObjectReference(oRef, oFromDoc);
							refDocList.add(oFromDoc);
						}
					}					
				} else {
					// for addresses just deliver the from object, no additional info, no matter how often referencing !
					IngridDocument oFromDoc = new IngridDocument();
					mapT01Object(oFrom, oFromDoc, howMuch);
					refDocList.add(oFromDoc);
				}
			}
			docLists[i] = refDocList;
		}

		toEntityDoc.put(MdekKeys.OBJ_REFERENCES_FROM_PUBLISHED_ONLY, docLists[INDEX_REFS_PUBLISHED]);
		toEntityDoc.put(MdekKeys.OBJ_REFERENCES_FROM, docLists[INDEX_REFS_WORK]);

		// also return index of first object and total num (needed when paging)
		if (toEntityType == IdcEntityType.ADDRESS) {
			toEntityDoc.put(MdekKeys.OBJ_REFERENCES_FROM_START_INDEX, oNodesFrom_startIndex);
			toEntityDoc.put(MdekKeys.OBJ_REFERENCES_FROM_TOTAL_NUM, oNodesFrom_totalNum);
		}

		return toEntityDoc;
	}

	/**
	 * Transfer relation data of passed bean to passed doc.
	 * @return doc containing additional data.
	 */
	public IngridDocument mapT012ObjAdr(T012ObjAdr oA, IngridDocument adressDoc) {
		if (oA == null) {
			return adressDoc;
		}

		adressDoc.put(MdekKeys.RELATION_TYPE_ID, oA.getType());
		adressDoc.put(MdekKeys.RELATION_TYPE_NAME, oA.getSpecialName());
		adressDoc.put(MdekKeys.RELATION_TYPE_REF, oA.getSpecialRef());
		adressDoc.put(MdekKeys.RELATION_DATE_OF_LAST_MODIFICATION, oA.getModTime());

		return adressDoc;
	}

	private IngridDocument mapT012ObjAdrs(Set<T012ObjAdr> oAs, IngridDocument objectDoc,
			MappingQuantity howMuch) {
		if (oAs == null) {
			return objectDoc;
		}
		ArrayList<IngridDocument> adrsList = new ArrayList<IngridDocument>(oAs.size());
		for (T012ObjAdr oA : oAs) {
			
			if (howMuch == MappingQuantity.INITIAL_ENTITY) {
				// only take over "Ansprechpartner" addresses !
				if (!MdekUtils.OBJ_ADR_TYPE_POINT_OF_CONTACT_ID.equals(oA.getType())) {
					continue;
				}
			}

			IngridDocument aDoc = new IngridDocument();
			mapT012ObjAdr(oA, aDoc);
			AddressNode aNode = oA.getAddressNode();
			if (aNode != null) {
				T02Address a = aNode.getT02AddressWork();
				mapT02Address(a, aDoc, MappingQuantity.TABLE_ENTITY);
				adrsList.add(aDoc);					
			} else {
				LOG.warn("Address " + oA.getAdrUuid() + " has no AddressNode !!! We skip this address reference.");
			}
		}
		objectDoc.put(MdekKeys.ADR_REFERENCES_TO, adrsList);
		
		return objectDoc;
	}

	private IngridDocument mapT021Communications(Set<T021Communication> refs, IngridDocument inDoc) {
		if (refs == null || refs.size() == 0) {
			return inDoc;
		}

		ArrayList<IngridDocument> refList = new ArrayList<IngridDocument>(refs.size());
		for (T021Communication ref : refs) {
			IngridDocument refDoc = new IngridDocument();
			mapT021Communication(ref, refDoc);
			refList.add(refDoc);
		}

		inDoc.put(MdekKeys.COMMUNICATION, refList);
		
		return inDoc;
	}
	public IngridDocument mapT021Communication(T021Communication c, IngridDocument commDoc) {
		if (c == null) {
			return commDoc;
		}

		commDoc.put(MdekKeys.COMMUNICATION_MEDIUM, c.getCommtypeValue());
		commDoc.put(MdekKeys.COMMUNICATION_MEDIUM_KEY, c.getCommtypeKey());
		commDoc.put(MdekKeys.COMMUNICATION_VALUE, c.getCommValue());
		commDoc.put(MdekKeys.COMMUNICATION_DESCRIPTION, c.getDescr());

		return commDoc;
	}

	public IngridDocument mapSpatialRefValues(List<SpatialRefValue> refValues, IngridDocument inDoc) {
		if (refValues == null) {
			return inDoc;
		}

		ArrayList<IngridDocument> refDocs = new ArrayList<IngridDocument>();
		for (SpatialRefValue refValue : refValues) {
			IngridDocument refDoc = mapSpatialRefValue(refValue, new IngridDocument());
			mapSpatialRefSns(refValue.getSpatialRefSns(), refDoc);

			refDocs.add(refDoc);
		}
		inDoc.put(MdekKeys.LOCATIONS, refDocs);
		
		return inDoc;
	}

	/**
	 * Transfer data of passed bean to passed doc.
	 * @return doc containing additional data.
	 */
	private IngridDocument mapSpatialRefValue(SpatialRefValue spatRefValue, IngridDocument locDoc) {
		if (spatRefValue == null) {
			return locDoc;
		}

		locDoc.put(MdekKeys.LOCATION_NAME, spatRefValue.getNameValue());
		locDoc.put(MdekKeys.LOCATION_NAME_KEY, spatRefValue.getNameKey());
		locDoc.put(MdekKeys.LOCATION_TYPE, spatRefValue.getType());
		locDoc.put(MdekKeys.LOCATION_CODE, spatRefValue.getNativekey());
		locDoc.put(MdekKeys.WEST_BOUNDING_COORDINATE, spatRefValue.getX1());
		locDoc.put(MdekKeys.SOUTH_BOUNDING_COORDINATE, spatRefValue.getY1());
		locDoc.put(MdekKeys.EAST_BOUNDING_COORDINATE, spatRefValue.getX2());
		locDoc.put(MdekKeys.NORTH_BOUNDING_COORDINATE, spatRefValue.getY2());
		locDoc.put(MdekKeys.SNS_TOPIC_TYPE, spatRefValue.getTopicType());

		return locDoc;
	}
	/**
	 * Transfer data of passed bean to passed doc.
	 * @return doc containing additional data.
	 */
	private IngridDocument mapSpatialRefSns(SpatialRefSns spatRefSns, IngridDocument locDoc) {
		if (spatRefSns == null) {
			return locDoc;
		}

		locDoc.put(MdekKeys.LOCATION_SNS_ID, spatRefSns.getSnsId());
		locDoc.put(MdekKeys.LOCATION_EXPIRED_AT, spatRefSns.getExpiredAt());

		return locDoc;
	}
	private IngridDocument mapSpatialReferences(Set<SpatialReference> spatRefs, IngridDocument objectDoc) {
		if (spatRefs == null) {
			return objectDoc;
		}
		ArrayList<IngridDocument> locList = new ArrayList<IngridDocument>(spatRefs.size());
		for (SpatialReference spatRef : spatRefs) {
			IngridDocument locDoc = new IngridDocument();
			SpatialRefValue spatRefValue = spatRef.getSpatialRefValue();
			if (spatRefValue != null) {
				mapSpatialRefValue(spatRefValue, locDoc);
				SpatialRefSns spatRefSns = spatRefValue.getSpatialRefSns();
				mapSpatialRefSns(spatRefSns, locDoc);
				locList.add(locDoc);					
			} else {
				LOG.warn("SpatialReference " + spatRef.getSpatialRefId() + " has no SpatialRefValue !!! We skip this SpatialReference.");
			}
		}
		objectDoc.put(MdekKeys.LOCATIONS, locList);
		
		return objectDoc;
	}

	private IngridDocument mapT017UrlRef(T017UrlRef url, IngridDocument urlDoc) {
		if (url == null) {
			return urlDoc;
		}

		urlDoc.put(MdekKeys.LINKAGE_URL, url.getUrlLink());
		urlDoc.put(MdekKeys.LINKAGE_REFERENCE_ID, url.getSpecialRef());
		urlDoc.put(MdekKeys.LINKAGE_REFERENCE, url.getSpecialName());
		urlDoc.put(MdekKeys.LINKAGE_DATATYPE_KEY, url.getDatatypeKey());
		urlDoc.put(MdekKeys.LINKAGE_DATATYPE, url.getDatatypeValue());
		urlDoc.put(MdekKeys.LINKAGE_DESCRIPTION, url.getDescr());
		urlDoc.put(MdekKeys.LINKAGE_NAME, url.getContent());
		urlDoc.put(MdekKeys.LINKAGE_URL_TYPE, url.getUrlType());

		return urlDoc;
	}

	private IngridDocument mapT017UrlRefs(Set<T017UrlRef> urlRefs, IngridDocument objectDoc) {
		if (urlRefs == null) {
			return objectDoc;
		}
		ArrayList<IngridDocument> urlList = new ArrayList<IngridDocument>(urlRefs.size());
		for (T017UrlRef url : urlRefs) {
			IngridDocument urlDoc = new IngridDocument();
			mapT017UrlRef(url, urlDoc);
			urlList.add(urlDoc);
		}
		objectDoc.put(MdekKeys.LINKAGES, urlList);
		
		return objectDoc;
	}

	private IngridDocument mapT0113DatasetReference(T0113DatasetReference ref, IngridDocument refDoc) {
		if (ref == null) {
			return refDoc;
		}

		refDoc.put(MdekKeys.DATASET_REFERENCE_DATE, ref.getReferenceDate());
		refDoc.put(MdekKeys.DATASET_REFERENCE_TYPE, ref.getType());

		return refDoc;
	}
	private IngridDocument mapT0113DatasetReferences(Set<T0113DatasetReference> refs, IngridDocument objectDoc) {
		if (refs == null) {
			return objectDoc;
		}
		ArrayList<IngridDocument> refList = new ArrayList<IngridDocument>(refs.size());
		for (T0113DatasetReference ref : refs) {
			IngridDocument refDoc = new IngridDocument();
			mapT0113DatasetReference(ref, refDoc);
			refList.add(refDoc);
		}
		objectDoc.put(MdekKeys.DATASET_REFERENCES, refList);
		
		return objectDoc;
	}

	private IngridDocument mapT014InfoImparts(Set<T014InfoImpart> refs, IngridDocument objectDoc) {
		if (refs == null) {
			return objectDoc;
		}

		ArrayList<IngridDocument> refList = new ArrayList<IngridDocument>(refs.size());
		for (T014InfoImpart ref : refs) {
			IngridDocument refDoc = new IngridDocument();
			mapT014InfoImpart(ref, refDoc);
			refList.add(refDoc);
		}
		objectDoc.put(MdekKeys.EXPORT_CRITERIA, refList);
		
		return objectDoc;
	}

	private IngridDocument mapT014InfoImpart(T014InfoImpart ref, IngridDocument refDoc) {
		if (ref == null) {
			return refDoc;
		}

		refDoc.put(MdekKeys.EXPORT_CRITERION_KEY, ref.getImpartKey());
		refDoc.put(MdekKeys.EXPORT_CRITERION_VALUE, ref.getImpartValue());

		return refDoc;
	}

	private IngridDocument mapT011ObjGeo(T01Object obj, IngridDocument objectDoc) {
		Set<T011ObjGeo> objGeos = obj.getT011ObjGeos();

		if (objGeos == null || objGeos.size() == 0) {
			return objectDoc;
		}
		IngridDocument domainDoc = new IngridDocument();
		
		// there should only be one object in the set because of the 1:1 relation between tables 
		// get first object from iterator
		T011ObjGeo objGeo = objGeos.iterator().next();
		domainDoc.put(MdekKeys.TECHNICAL_BASE, objGeo.getSpecialBase());
		domainDoc.put(MdekKeys.DATA, objGeo.getDataBase());
		domainDoc.put(MdekKeys.METHOD_OF_PRODUCTION, objGeo.getMethod());
		domainDoc.put(MdekKeys.RESOLUTION, objGeo.getRecExact());
		domainDoc.put(MdekKeys.DEGREE_OF_RECORD, objGeo.getRecGrade());
		domainDoc.put(MdekKeys.HIERARCHY_LEVEL, objGeo.getHierarchyLevel());
		domainDoc.put(MdekKeys.VECTOR_TOPOLOGY_LEVEL, objGeo.getVectorTopologyLevel());
		domainDoc.put(MdekKeys.POS_ACCURACY_VERTICAL, objGeo.getPosAccuracyVertical());
		domainDoc.put(MdekKeys.KEYC_INCL_W_DATASET, objGeo.getKeycInclWDataset());
		domainDoc.put(MdekKeys.DATASOURCE_UUID, objGeo.getDatasourceUuid());

		objectDoc.put(MdekKeys.TECHNICAL_DOMAIN_MAP, domainDoc);
		
		// add key catalogs
		mapObjectTypesCatalogues(obj.getObjectTypesCatalogues(), domainDoc);
		// add publication scales
		mapT011ObjGeoScales(objGeo.getT011ObjGeoScales(), domainDoc);
		// add symbol catalogs
		mapT011ObjGeoSymcs(objGeo.getT011ObjGeoSymcs(), domainDoc);
		// add feature types
		mapT011ObjGeoSupplinfos(objGeo.getT011ObjGeoSupplinfos(), domainDoc);
		// add vector formats geo vector list
		mapT011ObjGeoVectors(objGeo.getT011ObjGeoVectors(), domainDoc);
		// add vector formats geo vector list
		mapT011ObjGeoSpatialReps(objGeo.getT011ObjGeoSpatialReps(), domainDoc);
		
		return objectDoc;
	}

	private IngridDocument mapT011ObjGeoScales(Set<T011ObjGeoScale> refs, IngridDocument objectDoc) {
		if (refs == null) {
			return objectDoc;
		}
		ArrayList<IngridDocument> locList = new ArrayList<IngridDocument>(refs.size());
		for (T011ObjGeoScale ref : refs) {
			IngridDocument doc = new IngridDocument();
			doc.put(MdekKeys.SCALE, ref.getScale());
			doc.put(MdekKeys.RESOLUTION_GROUND, ref.getResolutionGround());
			doc.put(MdekKeys.RESOLUTION_SCAN, ref.getResolutionScan());
			locList.add(doc);					
		}
		objectDoc.put(MdekKeys.PUBLICATION_SCALE_LIST, locList);
		
		return objectDoc;
	}
	
	private IngridDocument mapT011ObjGeoSymcs(Set<T011ObjGeoSymc> refs, IngridDocument objectDoc) {
		if (refs == null) {
			return objectDoc;
		}
		ArrayList<IngridDocument> locList = new ArrayList<IngridDocument>(refs.size());
		for (T011ObjGeoSymc ref : refs) {
			IngridDocument doc = new IngridDocument();
			doc.put(MdekKeys.SYMBOL_CAT, ref.getSymbolCatValue());
			doc.put(MdekKeys.SYMBOL_CAT_KEY, ref.getSymbolCatKey());
			doc.put(MdekKeys.SYMBOL_DATE, ref.getSymbolDate());
			doc.put(MdekKeys.SYMBOL_EDITION, ref.getEdition());
			locList.add(doc);					
		}
		objectDoc.put(MdekKeys.SYMBOL_CATALOG_LIST, locList);
		
		return objectDoc;
	}

	private IngridDocument mapT011ObjGeoSupplinfos(Set<T011ObjGeoSupplinfo> refs, IngridDocument objectDoc) {
		if (refs == null) {
			return objectDoc;
		}
		ArrayList<String> locList = new ArrayList<String>(refs.size());
		for (T011ObjGeoSupplinfo ref : refs) {
			locList.add(ref.getFeatureType());
		}
		objectDoc.put(MdekKeys.FEATURE_TYPE_LIST, locList);
		
		return objectDoc;
	}

	private IngridDocument mapT011ObjGeoVectors(Set<T011ObjGeoVector> refs, IngridDocument objectDoc) {
		if (refs == null) {
			return objectDoc;
		}
		ArrayList<IngridDocument> locList = new ArrayList<IngridDocument>(refs.size());
		for (T011ObjGeoVector ref : refs) {
			IngridDocument doc = new IngridDocument();
			doc.put(MdekKeys.GEOMETRIC_OBJECT_TYPE, ref.getGeometricObjectType());
			doc.put(MdekKeys.GEOMETRIC_OBJECT_COUNT, ref.getGeometricObjectCount());
			locList.add(doc);					
		}
		objectDoc.put(MdekKeys.GEO_VECTOR_LIST, locList);
		
		return objectDoc;
	}

	private IngridDocument mapT011ObjGeoSpatialReps(Set<T011ObjGeoSpatialRep> refs, IngridDocument objectDoc) {
		if (refs == null) {
			return objectDoc;
		}
		ArrayList<Integer> locList = new ArrayList<Integer>(refs.size());
		for (T011ObjGeoSpatialRep ref : refs) {
			locList.add(ref.getType());
		}
		objectDoc.put(MdekKeys.SPATIAL_REPRESENTATION_TYPE_LIST, locList);
		
		return objectDoc;
	}
	

	private IngridDocument mapT011ObjLiterature(Set<T011ObjLiterature> refs, IngridDocument objectDoc) {
		if (refs == null || refs.size() == 0) {
			return objectDoc;
		}
		IngridDocument refDoc = new IngridDocument();
		
		// there should only be one object in the set because of the 1:1 relation between tables 
		// get first object from iterator
		T011ObjLiterature ref = refs.iterator().next();

		refDoc.put(MdekKeys.AUTHOR, ref.getAuthor());
		refDoc.put(MdekKeys.SOURCE, ref.getBase());
		refDoc.put(MdekKeys.DESCRIPTION_OF_TECH_DOMAIN, ref.getDescription());
		refDoc.put(MdekKeys.ADDITIONAL_BIBLIOGRAPHIC_INFO, ref.getDocInfo());
		refDoc.put(MdekKeys.ISBN, ref.getIsbn());
		refDoc.put(MdekKeys.LOCATION, ref.getLoc());
		refDoc.put(MdekKeys.EDITOR, ref.getPublisher());
		refDoc.put(MdekKeys.PUBLISHED_IN, ref.getPublishIn());
		refDoc.put(MdekKeys.PUBLISHER, ref.getPublishing());
		refDoc.put(MdekKeys.PUBLISHING_PLACE, ref.getPublishLoc());
		refDoc.put(MdekKeys.YEAR, ref.getPublishYear());
		refDoc.put(MdekKeys.PAGES, ref.getSides());
		refDoc.put(MdekKeys.TYPE_OF_DOCUMENT, ref.getTypeValue());
		refDoc.put(MdekKeys.TYPE_OF_DOCUMENT_KEY, ref.getTypeKey());
		refDoc.put(MdekKeys.VOLUME, ref.getVolume());

		objectDoc.put(MdekKeys.TECHNICAL_DOMAIN_DOCUMENT, refDoc);
		
		return objectDoc;
	}	
	
	
	private IngridDocument mapT015Legists(Set<T015Legist> refs, IngridDocument objectDoc) {
		if (refs == null) {
			return objectDoc;
		}
		
		ArrayList<IngridDocument> refList = new ArrayList<IngridDocument>(refs.size());
		for (T015Legist ref : refs) {
			IngridDocument refDoc = new IngridDocument();
			mapT015Legist(ref, refDoc);
			refList.add(refDoc);
		}
		objectDoc.put(MdekKeys.LEGISLATIONS, refList);
		
		return objectDoc;
	}

	private IngridDocument mapT015Legist(T015Legist ref, IngridDocument refDoc) {
		if (ref == null) {
			return refDoc;
		}
		refDoc.put(MdekKeys.LEGISLATION_VALUE, ref.getLegistValue());
		refDoc.put(MdekKeys.LEGISLATION_KEY, ref.getLegistKey());
		
		return refDoc;
	}

	private IngridDocument mapT0110AvailFormat(T0110AvailFormat ref, IngridDocument refDoc) {
		if (ref == null) {
			return refDoc;
		}

		refDoc.put(MdekKeys.FORMAT_NAME, ref.getFormatValue());
		refDoc.put(MdekKeys.FORMAT_NAME_KEY, ref.getFormatKey());
		refDoc.put(MdekKeys.FORMAT_VERSION, ref.getVer());
		refDoc.put(MdekKeys.FORMAT_SPECIFICATION, ref.getSpecification());
		refDoc.put(MdekKeys.FORMAT_FILE_DECOMPRESSION_TECHNIQUE, ref.getFileDecompressionTechnique());

		return refDoc;
	}
	private IngridDocument mapT0110AvailFormats(Set<T0110AvailFormat> refs, IngridDocument objectDoc) {
		if (refs == null) {
			return objectDoc;
		}
		ArrayList<IngridDocument> refList = new ArrayList<IngridDocument>(refs.size());
		for (T0110AvailFormat ref : refs) {
			IngridDocument refDoc = new IngridDocument();
			mapT0110AvailFormat(ref, refDoc);
			refList.add(refDoc);
		}
		objectDoc.put(MdekKeys.DATA_FORMATS, refList);
		
		return objectDoc;
	}

	private IngridDocument mapT0112MediaOption(T0112MediaOption ref, IngridDocument refDoc) {
		if (ref == null) {
			return refDoc;
		}

		refDoc.put(MdekKeys.MEDIUM_NAME, ref.getMediumName());
		refDoc.put(MdekKeys.MEDIUM_TRANSFER_SIZE, ref.getTransferSize());
		refDoc.put(MdekKeys.MEDIUM_NOTE, ref.getMediumNote());

		return refDoc;
	}
	private IngridDocument mapT0112MediaOptions(Set<T0112MediaOption> refs, IngridDocument objectDoc) {
		if (refs == null) {
			return objectDoc;
		}
		ArrayList<IngridDocument> refList = new ArrayList<IngridDocument>(refs.size());
		for (T0112MediaOption ref : refs) {
			IngridDocument refDoc = new IngridDocument();
			mapT0112MediaOption(ref, refDoc);
			refList.add(refDoc);
		}
		objectDoc.put(MdekKeys.MEDIUM_OPTIONS, refList);
		
		return objectDoc;
	}

	public IngridDocument mapSearchtermValues(List<SearchtermValue> termValues, IngridDocument inDoc) {
		if (termValues == null) {
			return inDoc;
		}

		ArrayList<IngridDocument> termDocs = new ArrayList<IngridDocument>();
		for (SearchtermValue termValue : termValues) {
			IngridDocument termDoc = mapSearchtermValue(termValue, new IngridDocument());
			mapSearchtermSns(termValue.getSearchtermSns(), termDoc);

			termDocs.add(termDoc);
		}
		inDoc.put(MdekKeys.SUBJECT_TERMS, termDocs);
		
		return inDoc;
	}

	private IngridDocument mapSearchtermValue(SearchtermValue ref, IngridDocument refDoc) {
		if (ref == null) {
			return refDoc;
		}

		refDoc.put(MdekKeys.TERM_NAME, ref.getTerm());
		refDoc.put(MdekKeys.TERM_ALTERNATE_NAME, ref.getAlternateTerm());
		refDoc.put(MdekKeys.TERM_TYPE, ref.getType());
		refDoc.put(MdekKeys.TERM_ENTRY_ID, ref.getEntryId());

		return refDoc;
	}
	private IngridDocument mapSearchtermSns(SearchtermSns ref, IngridDocument refDoc) {
		if (ref == null) {
			return refDoc;
		}

		refDoc.put(MdekKeys.TERM_SNS_ID, ref.getSnsId());
		refDoc.put(MdekKeys.TERM_GEMET_ID, ref.getGemetId());

		return refDoc;
	}
	private IngridDocument mapSearchterms(IdcEntityType entityType,
			List<IEntity> termEntityRefs, IngridDocument docIn,
			MappingQuantity howMuch)
	{
		if (termEntityRefs == null) {
			return docIn;
		}
		ArrayList<IngridDocument> terms = new ArrayList<IngridDocument>();
		ArrayList<IngridDocument> termsInspire = new ArrayList<IngridDocument>();

		String THESAURUS_TYPE = MdekUtils.SearchtermType.UMTHES.getDbValue();
		String GEMET_TYPE = MdekUtils.SearchtermType.GEMET.getDbValue();
		String INSPIRE_TYPE = MdekUtils.SearchtermType.INSPIRE.getDbValue();
		for (IEntity termEntityRef : termEntityRefs) {
			SearchtermValue termValue;
			if (entityType == IdcEntityType.OBJECT) {
				termValue = ((SearchtermObj)termEntityRef).getSearchtermValue();
			} else {
				termValue = ((SearchtermAdr)termEntityRef).getSearchtermValue();
			}
			if (termValue == null) {
				continue;
			}

			String termType = termValue.getType();
			if (howMuch == MappingQuantity.INITIAL_ENTITY) {
				// only take over thesaurus and inspire terms from parent on initial creation !
				if (!THESAURUS_TYPE.equals(termType) &&
					!GEMET_TYPE.equals(termType) &&
					!INSPIRE_TYPE.equals(termType)) {
					continue;
				}
			}

			IngridDocument termDoc = new IngridDocument();
			mapSearchtermValue(termValue, termDoc);
			mapSearchtermSns(termValue.getSearchtermSns(), termDoc);
			
			if (INSPIRE_TYPE.equals(termType)) {
				termsInspire.add(termDoc);
			} else {
				terms.add(termDoc);
			}
		}
		docIn.put(MdekKeys.SUBJECT_TERMS, terms);
		docIn.put(MdekKeys.SUBJECT_TERMS_INSPIRE, termsInspire);
		
		return docIn;
	}
	private IngridDocument mapSearchtermObjs(Set<SearchtermObj> refs, IngridDocument objectDoc,
		MappingQuantity howMuch)
	{
		ArrayList<IEntity> termEntityRefs = new ArrayList<IEntity>(refs);
		return mapSearchterms(IdcEntityType.OBJECT, termEntityRefs, objectDoc, howMuch);
	}
	private IngridDocument mapSearchtermAdrs(Set<SearchtermAdr> refs, IngridDocument addressDoc,
		MappingQuantity howMuch)
	{
		ArrayList<IEntity> termEntityRefs = new ArrayList<IEntity>(refs);
		return mapSearchterms(IdcEntityType.ADDRESS, termEntityRefs, addressDoc, howMuch);
	}

	private IngridDocument mapT0114EnvTopics(Set<T0114EnvTopic> refs, IngridDocument objectDoc) {
		if (refs == null) {
			return objectDoc;
		}
		ArrayList<Integer> refList = new ArrayList<Integer>(refs.size());
		for (T0114EnvTopic ref : refs) {
			refList.add(ref.getTopicKey());				
		}
		objectDoc.put(MdekKeys.ENV_TOPICS, refList);
		
		return objectDoc;
	}

	private IngridDocument mapT011ObjTopicCats(Set<T011ObjTopicCat> refs, IngridDocument objectDoc) {
		if (refs == null) {
			return objectDoc;
		}
		ArrayList<Integer> refList = new ArrayList<Integer>(refs.size());
		for (T011ObjTopicCat ref : refs) {
			refList.add(ref.getTopicCategory());				
		}
		objectDoc.put(MdekKeys.TOPIC_CATEGORIES, refList);
		
		return objectDoc;
	}

	private IngridDocument mapT011ObjData(T011ObjData ref, IngridDocument refDoc) {
		if (ref == null) {
			return refDoc;
		}

		refDoc.put(MdekKeys.METHOD, ref.getBase());
		refDoc.put(MdekKeys.DESCRIPTION_OF_TECH_DOMAIN, ref.getDescription());

		return refDoc;
	}
	private IngridDocument mapT011ObjData(T01Object obj, IngridDocument objectDoc) {
		Set<T011ObjData> objDatas = obj.getT011ObjDatas();
		if (objDatas == null || objDatas.size() == 0) {
			return objectDoc;
		}

		IngridDocument domainDoc = new IngridDocument();
		mapT011ObjData(objDatas.iterator().next(), domainDoc);
		objectDoc.put(MdekKeys.TECHNICAL_DOMAIN_DATASET, domainDoc);

		mapT011ObjDataParas(obj.getT011ObjDataParas(), domainDoc);
		mapObjectTypesCatalogues(obj.getObjectTypesCatalogues(), domainDoc);

		return objectDoc;
	}
	private IngridDocument mapT011ObjDataPara(T011ObjDataPara ref, IngridDocument refDoc) {
		if (ref == null) {
			return refDoc;
		}

		refDoc.put(MdekKeys.PARAMETER, ref.getParameter());
		refDoc.put(MdekKeys.SUPPLEMENTARY_INFORMATION, ref.getUnit());

		return refDoc;
	}
	private IngridDocument mapT011ObjDataParas(Set<T011ObjDataPara> refs, IngridDocument inDoc) {
		if (refs == null || refs.size() == 0) {
			return inDoc;
		}

		ArrayList<IngridDocument> refList = new ArrayList<IngridDocument>(refs.size());
		for (T011ObjDataPara ref : refs) {
			IngridDocument refDoc = new IngridDocument();
			mapT011ObjDataPara(ref, refDoc);
			refList.add(refDoc);
		}

		inDoc.put(MdekKeys.PARAMETERS, refList);
		
		return inDoc;
	}
	private IngridDocument mapObjectTypesCatalogue(ObjectTypesCatalogue ref, IngridDocument refDoc) {
		if (ref == null) {
			return refDoc;
		}

		refDoc.put(MdekKeys.SUBJECT_CAT_KEY, ref.getTitleKey());
		refDoc.put(MdekKeys.SUBJECT_CAT, ref.getTitleValue());
		refDoc.put(MdekKeys.KEY_DATE, ref.getTypeDate());
		refDoc.put(MdekKeys.EDITION, ref.getTypeVersion());

		return refDoc;
	}
	private IngridDocument mapObjectTypesCatalogues(Set<ObjectTypesCatalogue> refs, IngridDocument inDoc) {
		if (refs == null || refs.size() == 0) {
			return inDoc;
		}
		ArrayList<IngridDocument> refList = new ArrayList<IngridDocument>(refs.size());
		for (ObjectTypesCatalogue ref : refs) {
			IngridDocument refDoc = new IngridDocument();
			mapObjectTypesCatalogue(ref, refDoc);
			refList.add(refDoc);
		}
		inDoc.put(MdekKeys.KEY_CATALOG_LIST, refList);
		
		return inDoc;
	}


	private IngridDocument mapT011ObjProject(T011ObjProject ref, IngridDocument refDoc) {
		if (ref == null) {
			return refDoc;
		}

		refDoc.put(MdekKeys.LEADER_DESCRIPTION, ref.getLeader());
		refDoc.put(MdekKeys.MEMBER_DESCRIPTION, ref.getMember());
		refDoc.put(MdekKeys.DESCRIPTION_OF_TECH_DOMAIN, ref.getDescription());

		return refDoc;
	}
	private IngridDocument mapT011ObjProject(Set<T011ObjProject> refs, IngridDocument objectDoc) {
		if (refs == null || refs.size() == 0) {
			return objectDoc;
		}

		IngridDocument domainDoc = new IngridDocument();
		mapT011ObjProject(refs.iterator().next(), domainDoc);
		objectDoc.put(MdekKeys.TECHNICAL_DOMAIN_PROJECT, domainDoc);
		
		return objectDoc;
	}

	private IngridDocument mapT011ObjServ(Set<T011ObjServ> refs, IngridDocument objectDoc) {
		if (refs == null || refs.size() == 0) {
			return objectDoc;
		}

		// NOTICE: This container is used for the object classes "Geodatendienst" AND
		// "Informationssystem/Dienst/Anwendung" with DIFFERENT content !
		// BUT WE ALWAYS MAP EVERYTHING ASSUMING EVERY CLASS HAS ITS RIGHT CONTENT !

		// there should only be one object in the set because of the 1:1 relation between tables 
		// get first object from iterator
		T011ObjServ ref = refs.iterator().next();

		IngridDocument domainDoc = new IngridDocument();
		mapT011ObjServ(ref, domainDoc);
		objectDoc.put(MdekKeys.TECHNICAL_DOMAIN_SERVICE, domainDoc);

		// add service versions
		mapT011ObjServVersions(ref.getT011ObjServVersions(), domainDoc);
		
		// following stuff should only be present in class "Geodatendienst" !
		// add geoservice operations
		mapT011ObjServOperations(ref.getT011ObjServOperations(), domainDoc);
		// add geoservice types ("Klassifikation")
		mapT011ObjServTypes(ref.getT011ObjServTypes(), domainDoc);
		// add geoservice publication scales ("Erstellungsmassstab")
		mapT011ObjServScales(ref.getT011ObjServScales(), domainDoc);

		// following stuff should only be present in class "Informationssystem/Dienst/Anwendung" !
		// add URLs
		mapT011ObjServUrls(ref.getT011ObjServUrls(), domainDoc);

		return objectDoc;
	}
	private IngridDocument mapT011ObjServ(T011ObjServ ref, IngridDocument refDoc) {
		if (ref == null) {
			return refDoc;
		}

		refDoc.put(MdekKeys.SERVICE_TYPE, ref.getTypeValue());
		refDoc.put(MdekKeys.SERVICE_TYPE_KEY, ref.getTypeKey());
		refDoc.put(MdekKeys.COUPLING_TYPE, ref.getCouplingType());
		refDoc.put(MdekKeys.SYSTEM_HISTORY, ref.getHistory());
		refDoc.put(MdekKeys.SYSTEM_ENVIRONMENT, ref.getEnvironment());
		refDoc.put(MdekKeys.DATABASE_OF_SYSTEM, ref.getBase());
		refDoc.put(MdekKeys.DESCRIPTION_OF_TECH_DOMAIN, ref.getDescription());
		refDoc.put(MdekKeys.HAS_ACCESS_CONSTRAINT, ref.getHasAccessConstraint());
		refDoc.put(MdekKeys.HAS_ATOM_DOWNLOAD, ref.getHasAtomDownload());

		return refDoc;
	}
	private IngridDocument mapT011ObjServVersions(Set<T011ObjServVersion> refs, IngridDocument inDoc) {
		if (refs == null || refs.size() == 0) {
			return inDoc;
		}
		ArrayList<IngridDocument> refList = new ArrayList<IngridDocument>(refs.size());
		for (T011ObjServVersion ref : refs) {
            IngridDocument refDoc = new IngridDocument();
            mapT011ObjServVersion(ref, refDoc);
            refList.add(refDoc);
		}
		inDoc.put(MdekKeys.SERVICE_VERSION_LIST, refList);
		
		return inDoc;
	}
    private IngridDocument mapT011ObjServVersion(T011ObjServVersion ref, IngridDocument refDoc) {
        if (ref == null) {
            return refDoc;
        }
        refDoc.put(MdekKeys.SERVICE_VERSION_KEY, ref.getVersionKey());
        refDoc.put(MdekKeys.SERVICE_VERSION_VALUE, ref.getVersionValue());
        return refDoc;
    }
	private IngridDocument mapT011ObjServOperations(Set<T011ObjServOperation> refs, IngridDocument inDoc) {
		if (refs == null || refs.size() == 0) {
			return inDoc;
		}

		ArrayList<IngridDocument> refList = new ArrayList<IngridDocument>(refs.size());
		for (T011ObjServOperation ref : refs) {
			IngridDocument refDoc = new IngridDocument();
			mapT011ObjServOperation(ref, refDoc);

			// 1:n relations
			mapT011ObjServOpPlatforms(ref.getT011ObjServOpPlatforms(), refDoc);
			mapT011ObjServOpDependss(ref.getT011ObjServOpDependss(), refDoc);
			mapT011ObjServOpConnpoints(ref.getT011ObjServOpConnpoints(), refDoc);
			mapT011ObjServOpParas(ref.getT011ObjServOpParas(), refDoc);

			refList.add(refDoc);
		}

		inDoc.put(MdekKeys.SERVICE_OPERATION_LIST, refList);
		
		return inDoc;
	}
	private IngridDocument mapT011ObjServOperation(T011ObjServOperation ref, IngridDocument refDoc) {
		if (ref == null) {
			return refDoc;
		}

		refDoc.put(MdekKeys.SERVICE_OPERATION_NAME, ref.getNameValue());
		refDoc.put(MdekKeys.SERVICE_OPERATION_NAME_KEY, ref.getNameKey());
		refDoc.put(MdekKeys.SERVICE_OPERATION_DESCRIPTION, ref.getDescr());
		refDoc.put(MdekKeys.INVOCATION_NAME, ref.getInvocationName());

		return refDoc;
	}
	private IngridDocument mapT011ObjServOpPlatforms(Set<T011ObjServOpPlatform> refs, IngridDocument inDoc) {
		if (refs == null || refs.size() == 0) {
			return inDoc;
		}
		ArrayList<IngridDocument> refList = new ArrayList<IngridDocument>(refs.size());
		for (T011ObjServOpPlatform ref : refs) {
			IngridDocument refDoc = new IngridDocument();
			mapT011ObjServOpPlatform(ref, refDoc);
			refList.add(refDoc);
		}
		inDoc.put(MdekKeys.PLATFORM_LIST, refList);
		
		return inDoc;
	}
	private IngridDocument mapT011ObjServOpPlatform(T011ObjServOpPlatform ref, IngridDocument refDoc) {
		if (ref == null) {
			return refDoc;
		}
		refDoc.put(MdekKeys.PLATFORM_KEY, ref.getPlatformKey());
		refDoc.put(MdekKeys.PLATFORM_VALUE, ref.getPlatformValue());
		return refDoc;
	}
	private IngridDocument mapT011ObjServOpDependss(Set<T011ObjServOpDepends> refs, IngridDocument inDoc) {
		if (refs == null) {
			return inDoc;
		}
		ArrayList<String> refList = new ArrayList<String>(refs.size());
		for (T011ObjServOpDepends ref : refs) {
			refList.add(ref.getDependsOn());				
		}
		inDoc.put(MdekKeys.DEPENDS_ON_LIST, refList);
		
		return inDoc;
	}
	private IngridDocument mapT011ObjServOpConnpoints(Set<T011ObjServOpConnpoint> refs, IngridDocument inDoc) {
		if (refs == null) {
			return inDoc;
		}
		ArrayList<String> refList = new ArrayList<String>(refs.size());
		for (T011ObjServOpConnpoint ref : refs) {
			refList.add(ref.getConnectPoint());				
		}
		inDoc.put(MdekKeys.CONNECT_POINT_LIST, refList);
		
		return inDoc;
	}
	private IngridDocument mapT011ObjServOpParas(Set<T011ObjServOpPara> refs, IngridDocument inDoc) {
		if (refs == null || refs.size() == 0) {
			return inDoc;
		}

		ArrayList<IngridDocument> refList = new ArrayList<IngridDocument>(refs.size());
		for (T011ObjServOpPara ref : refs) {
			IngridDocument refDoc = new IngridDocument();
			mapT011ObjServOpPara(ref, refDoc);
			refList.add(refDoc);
		}

		inDoc.put(MdekKeys.PARAMETER_LIST, refList);
		
		return inDoc;
	}
	private IngridDocument mapT011ObjServOpPara(T011ObjServOpPara ref, IngridDocument refDoc) {
		if (ref == null) {
			return refDoc;
		}

		refDoc.put(MdekKeys.PARAMETER_NAME, ref.getName());
		refDoc.put(MdekKeys.DIRECTION, ref.getDirection());
		refDoc.put(MdekKeys.DESCRIPTION, ref.getDescr());
		refDoc.put(MdekKeys.OPTIONALITY, ref.getOptional());
		refDoc.put(MdekKeys.REPEATABILITY, ref.getRepeatability());

		return refDoc;
	}
	private IngridDocument mapT011ObjServTypes(Set<T011ObjServType> refs, IngridDocument inDoc) {
		if (refs == null || refs.size() == 0) {
			return inDoc;
		}

		ArrayList<IngridDocument> refList = new ArrayList<IngridDocument>(refs.size());
		for (T011ObjServType ref : refs) {
			IngridDocument refDoc = new IngridDocument();
			mapT011ObjServType(ref, refDoc);
			refList.add(refDoc);
		}

		inDoc.put(MdekKeys.SERVICE_TYPE2_LIST, refList);
		
		return inDoc;
	}
	private IngridDocument mapT011ObjServType(T011ObjServType ref, IngridDocument refDoc) {
		if (ref == null) {
			return refDoc;
		}

		refDoc.put(MdekKeys.SERVICE_TYPE2_KEY, ref.getServTypeKey());
		refDoc.put(MdekKeys.SERVICE_TYPE2_VALUE, ref.getServTypeValue());

		return refDoc;
	}
	private IngridDocument mapT011ObjServScales(Set<T011ObjServScale> refs, IngridDocument inDoc) {
		if (refs == null || refs.size() == 0) {
			return inDoc;
		}

		ArrayList<IngridDocument> refList = new ArrayList<IngridDocument>(refs.size());
		for (T011ObjServScale ref : refs) {
			IngridDocument refDoc = new IngridDocument();
			mapT011ObjServScale(ref, refDoc);
			refList.add(refDoc);
		}

		inDoc.put(MdekKeys.PUBLICATION_SCALE_LIST, refList);
		
		return inDoc;
	}
	private IngridDocument mapT011ObjServScale(T011ObjServScale ref, IngridDocument refDoc) {
		if (ref == null) {
			return refDoc;
		}

		refDoc.put(MdekKeys.SCALE, ref.getScale());
		refDoc.put(MdekKeys.RESOLUTION_GROUND, ref.getResolutionGround());
		refDoc.put(MdekKeys.RESOLUTION_SCAN, ref.getResolutionScan());

		return refDoc;
	}
	private IngridDocument mapT011ObjServUrls(Set<T011ObjServUrl> refs, IngridDocument inDoc) {
		if (refs == null || refs.size() == 0) {
			return inDoc;
		}

		ArrayList<IngridDocument> refList = new ArrayList<IngridDocument>(refs.size());
		for (T011ObjServUrl ref : refs) {
			IngridDocument refDoc = new IngridDocument();
			mapT011ObjServUrl(ref, refDoc);
			refList.add(refDoc);
		}

		inDoc.put(MdekKeys.URL_LIST, refList);
		
		return inDoc;
	}
	private IngridDocument mapT011ObjServUrl(T011ObjServUrl ref, IngridDocument refDoc) {
		if (ref == null) {
			return refDoc;
		}

		refDoc.put(MdekKeys.NAME, ref.getName());
		refDoc.put(MdekKeys.URL, ref.getUrl());
		refDoc.put(MdekKeys.DESCRIPTION, ref.getDescription());

		return refDoc;
	}

	public IngridDocument mapObjectParentData(T01Object parentObject, IngridDocument resultDoc) {
		if (parentObject == null) {
			return resultDoc;
		}
		IngridDocument refDoc = new IngridDocument();
		refDoc.put(MdekKeys.PUBLICATION_CONDITION, parentObject.getPublishId());
		resultDoc.put(MdekKeys.PARENT_INFO, refDoc);
		return resultDoc;
	}
	
	public IngridDocument mapAddressParentData(T02Address parentAddress, IngridDocument resultDoc) {
		if (parentAddress == null) {
			return resultDoc;
		}
		IngridDocument refDoc = new IngridDocument();
		refDoc.put(MdekKeys.PUBLICATION_CONDITION, parentAddress.getPublishId());
		refDoc.put(MdekKeys.CLASS, parentAddress.getAdrType());
		resultDoc.put(MdekKeys.PARENT_INFO, refDoc);
		return resultDoc;
	}
	
	public IngridDocument mapT03Catalog(T03Catalogue cat, IngridDocument resultDoc) {
		resultDoc.put(MdekKeys.UUID, cat.getCatUuid());
		resultDoc.put(MdekKeys.CATALOG_NAME, cat.getCatName());
		resultDoc.put(MdekKeys.CATALOG_NAMESPACE, cat.getCatNamespace());
		resultDoc.put(MdekKeys.CATALOG_ATOM_URL, cat.getAtomDownloadUrl());
		resultDoc.put(MdekKeys.PARTNER_NAME, cat.getPartnerName());
		resultDoc.put(MdekKeys.PROVIDER_NAME, cat.getProviderName());
		resultDoc.put(MdekKeys.COUNTRY_CODE, cat.getCountryKey());
		resultDoc.put(MdekKeys.COUNTRY_NAME, cat.getCountryValue());
		resultDoc.put(MdekKeys.LANGUAGE_CODE, cat.getLanguageKey());
		resultDoc.put(MdekKeys.LANGUAGE_NAME, cat.getLanguageValue());
		resultDoc.put(MdekKeys.WORKFLOW_CONTROL, cat.getWorkflowControl());
		resultDoc.put(MdekKeys.EXPIRY_DURATION, cat.getExpiryDuration());
		resultDoc.put(MdekKeys.DATE_OF_CREATION, cat.getCreateTime());
		resultDoc.put(MdekKeys.DATE_OF_LAST_MODIFICATION, cat.getModTime());

		SpatialRefValue spRefVal = cat.getSpatialRefValue();
		if (spRefVal != null) {
			IngridDocument locDoc = new IngridDocument();
			mapSpatialRefValue(spRefVal, locDoc);
			SpatialRefSns spatRefSns = spRefVal.getSpatialRefSns();
			mapSpatialRefSns(spatRefSns, locDoc);
			resultDoc.put(MdekKeys.CATALOG_LOCATION, locDoc);
		}

		// map only with initial data ! call mapping method explicitly if more data wanted.
		mapModUser(cat.getModUuid(), resultDoc, MappingQuantity.INITIAL_ENTITY);

		return resultDoc;
	}

	/**
	 * Transfer SysList to passed doc.
	 * @param list a fetched SysList
	 * @param listId lst_id of given list
	 * @param listDoc doc where data should be added
	 * @return doc containing additional data.
	 */
	public IngridDocument mapSysList(List<SysList> list,
			int listId,
			IngridDocument listDoc) {
		if (list == null) {
			return listDoc;
		}

		listDoc.put(MdekKeys.LST_ID, listId);
		
		ArrayList<Integer> entryIds  = new ArrayList<Integer>();
		ArrayList<String> entryNames = new ArrayList<String>();
		ArrayList<String> entryData  = new ArrayList<String>();
		for (SysList entry : list) {
			if (!listDoc.containsKey(MdekKeys.LST_MAINTAINABLE)) {
				listDoc.put(MdekKeys.LST_MAINTAINABLE, entry.getMaintainable());
			}

			// NOTICE: entryId should not be null (was read from database)
			if (!entryIds.contains(entry.getEntryId())) {
				entryIds.add(entry.getEntryId());
			}
			int entryIndex = entryIds.indexOf(entry.getEntryId());

			if (MdekUtils.YES.equals(entry.getIsDefault())) {
				listDoc.put(MdekKeys.LST_DEFAULT_ENTRY_INDEX, entryIndex);
			}
			
			addEntryToList(entry.getName(), entryNames, entryIndex);
			addEntryToList(entry.getData(), entryData, entryIndex);
		}
		listDoc.put(MdekKeys.LST_ENTRY_IDS, entryIds.toArray(new Integer[entryIds.size()]));
		if (entryNames.size() > 0) {
			listDoc.put(MdekKeys.LST_ENTRY_NAMES, entryNames.toArray(new String[entryNames.size()]));			
		}
		listDoc.put(MdekKeys.LST_ENTRY_DATA, entryData.toArray(new String[entryData.size()]));

		return listDoc;
	}
	/** Add the given entry to the given list at the the given index. Ensures initialization of "leaks"
	 * in the list. */
	private void addEntryToList(String entry, List<String> list, int index) {
		// initialize "leaks" in array 
		if (index >= list.size()) {
			for (int i=list.size(); i <= index; i++) {
				list.add(i, null);
			}
		}
		list.remove(index);
		list.add(index, entry);
	}

	/**
	 * Transfer SysGenericKeys to passed doc.
	 * @param keyList fetched SysGenericKey beans
	 * @param resultDoc doc where data should be added
	 * @return resultDoc containing additional data (genericKey names are keys into map)
	 */
	public IngridDocument mapSysGenericKeys(List<SysGenericKey> keyList,
			IngridDocument resultDoc) {
		if (keyList == null) {
			return resultDoc;
		}

		for (SysGenericKey genericKey : keyList) {
			resultDoc.put(genericKey.getKeyName(), genericKey.getValueString());
		}

		return resultDoc;
	}

	/** Create default conformity set with passed conformity. e.g. To be mapped to doc. */
	public Set<ObjectConformity> createObjectConformitySet(int specifikationKey, int degreeKey) {
		Set<ObjectConformity> oCs = new HashSet<ObjectConformity>();
		ObjectConformity oC = new ObjectConformity();
		oC.setSpecificationKey(specifikationKey);
		oC.setDegreeKey(degreeKey);
		oCs.add(oC);
		
		return oCs;
	}

	public IngridDocument mapObjectConformitys(Set<ObjectConformity> refs, IngridDocument objectDoc) {
		if (refs == null) {
			return objectDoc;
		}

		ArrayList<IngridDocument> refList = new ArrayList<IngridDocument>(refs.size());
		for (ObjectConformity ref : refs) {
			IngridDocument refDoc = new IngridDocument();
			mapObjectConformity(ref, refDoc);
			refList.add(refDoc);
		}
		objectDoc.put(MdekKeys.CONFORMITY_LIST, refList);
		
		return objectDoc;
	}

	private IngridDocument mapObjectConformity(ObjectConformity ref, IngridDocument refDoc) {
		if (ref == null) {
			return refDoc;
		}

		refDoc.put(MdekKeys.CONFORMITY_SPECIFICATION_KEY, ref.getSpecificationKey());
		refDoc.put(MdekKeys.CONFORMITY_SPECIFICATION_VALUE, ref.getSpecificationValue());
		refDoc.put(MdekKeys.CONFORMITY_DEGREE_KEY, ref.getDegreeKey());
		refDoc.put(MdekKeys.CONFORMITY_DEGREE_VALUE, ref.getDegreeValue());

		return refDoc;
	}

	private IngridDocument mapObjectAccesses(Set<ObjectAccess> refs, IngridDocument objectDoc) {
		if (refs == null) {
			return objectDoc;
		}

		ArrayList<IngridDocument> refList = new ArrayList<IngridDocument>(refs.size());
		for (ObjectAccess ref : refs) {
			IngridDocument refDoc = new IngridDocument();
			mapObjectAccess(ref, refDoc);
			refList.add(refDoc);
		}
		objectDoc.put(MdekKeys.ACCESS_LIST, refList);
		
		return objectDoc;
	}

	private IngridDocument mapObjectAccess(ObjectAccess ref, IngridDocument refDoc) {
		if (ref == null) {
			return refDoc;
		}

		refDoc.put(MdekKeys.ACCESS_RESTRICTION_KEY, ref.getRestrictionKey());
		refDoc.put(MdekKeys.ACCESS_RESTRICTION_VALUE, ref.getRestrictionValue());

		return refDoc;
	}

	private IngridDocument mapObjectUses(Set<ObjectUse> refs, IngridDocument objectDoc) {
		if (refs == null) {
			return objectDoc;
		}

		ArrayList<IngridDocument> refList = new ArrayList<IngridDocument>(refs.size());
		for (ObjectUse ref : refs) {
			IngridDocument refDoc = new IngridDocument();
			mapObjectUse(ref, refDoc);
			refList.add(refDoc);
		}
		objectDoc.put(MdekKeys.USE_LIST, refList);
		
		return objectDoc;
	}

	private IngridDocument mapObjectUse(ObjectUse ref, IngridDocument refDoc) {
		if (ref == null) {
			return refDoc;
		}

		refDoc.put(MdekKeys.USE_TERMS_OF_USE_KEY, ref.getTermsOfUseKey());
		refDoc.put(MdekKeys.USE_TERMS_OF_USE_VALUE, ref.getTermsOfUseValue());

		return refDoc;
	}

    private IngridDocument mapObjectUseConstraints(Set<ObjectUseConstraint> refs, IngridDocument objectDoc) {
        if (refs == null) {
            return objectDoc;
        }

        ArrayList<IngridDocument> refList = new ArrayList<IngridDocument>(refs.size());
        for (ObjectUseConstraint ref : refs) {
            IngridDocument refDoc = new IngridDocument();
            mapObjectUseConstraint(ref, refDoc);
            refList.add(refDoc);
        }
        objectDoc.put(MdekKeys.USE_CONSTRAINTS, refList);
        
        return objectDoc;
    }

    private IngridDocument mapObjectUseConstraint(ObjectUseConstraint ref, IngridDocument refDoc) {
        if (ref == null) {
            return refDoc;
        }

        refDoc.put(MdekKeys.USE_LICENSE_KEY, ref.getLicenseKey());
        refDoc.put(MdekKeys.USE_LICENSE_VALUE, ref.getLicenseValue());

        return refDoc;
    }

	private IngridDocument mapObjectOpenDataCategorys(Set<ObjectOpenDataCategory> refs, IngridDocument objectDoc) {
		if (refs == null) {
			return objectDoc;
		}

		ArrayList<IngridDocument> refList = new ArrayList<IngridDocument>(refs.size());
		for (ObjectOpenDataCategory ref : refs) {
			IngridDocument refDoc = new IngridDocument();
			mapObjectOpenDataCategory(ref, refDoc);
			refList.add(refDoc);
		}
		objectDoc.put(MdekKeys.OPEN_DATA_CATEGORY_LIST, refList);
		
		return objectDoc;
	}
	private IngridDocument mapObjectOpenDataCategory(ObjectOpenDataCategory ref, IngridDocument refDoc) {
		if (ref == null) {
			return refDoc;
		}

		refDoc.put(MdekKeys.OPEN_DATA_CATEGORY_KEY, ref.getCategoryKey());
		refDoc.put(MdekKeys.OPEN_DATA_CATEGORY_VALUE, ref.getCategoryValue());

		return refDoc;
	}

	private IngridDocument mapObjectDataQualitys(Set<ObjectDataQuality> refs, IngridDocument objectDoc) {
		if (refs == null) {
			return objectDoc;
		}

		ArrayList<IngridDocument> refList = new ArrayList<IngridDocument>(refs.size());
		for (ObjectDataQuality ref : refs) {
			IngridDocument refDoc = new IngridDocument();
			mapObjectDataQuality(ref, refDoc);
			refList.add(refDoc);
		}
		objectDoc.put(MdekKeys.DATA_QUALITY_LIST, refList);
		
		return objectDoc;
	}

	private IngridDocument mapObjectDataQuality(ObjectDataQuality ref, IngridDocument refDoc) {
		if (ref == null) {
			return refDoc;
		}

		refDoc.put(MdekKeys.DQ_ELEMENT_ID, ref.getDqElementId());
		refDoc.put(MdekKeys.NAME_OF_MEASURE_KEY, ref.getNameOfMeasureKey());
		refDoc.put(MdekKeys.NAME_OF_MEASURE_VALUE, ref.getNameOfMeasureValue());
		refDoc.put(MdekKeys.RESULT_VALUE, ref.getResultValue());
		refDoc.put(MdekKeys.MEASURE_DESCRIPTION, ref.getMeasureDescription());

		return refDoc;
	}

	private IngridDocument mapObjectFormatInspires(Set<ObjectFormatInspire> refs, IngridDocument objectDoc) {
		if (refs == null) {
			return objectDoc;
		}

		ArrayList<IngridDocument> refList = new ArrayList<IngridDocument>(refs.size());
		for (ObjectFormatInspire ref : refs) {
			IngridDocument refDoc = new IngridDocument();
			mapObjectFormatInspire(ref, refDoc);
			refList.add(refDoc);
		}
		objectDoc.put(MdekKeys.FORMAT_INSPIRE_LIST, refList);
		
		return objectDoc;
	}
	private IngridDocument mapObjectFormatInspire(ObjectFormatInspire ref, IngridDocument refDoc) {
		if (ref == null) {
			return refDoc;
		}

		refDoc.put(MdekKeys.FORMAT_KEY, ref.getFormatKey());
		refDoc.put(MdekKeys.FORMAT_VALUE, ref.getFormatValue());

		return refDoc;
	}

	private IngridDocument mapSpatialSystems(Set<SpatialSystem> refs, IngridDocument objectDoc) {
		if (refs == null) {
			return objectDoc;
		}

		ArrayList<IngridDocument> refList = new ArrayList<IngridDocument>(refs.size());
		for (SpatialSystem ref : refs) {
			IngridDocument refDoc = new IngridDocument();
			mapSpatialSystem(ref, refDoc);
			refList.add(refDoc);
		}
		objectDoc.put(MdekKeys.SPATIAL_SYSTEM_LIST, refList);
		
		return objectDoc;
	}
	private IngridDocument mapSpatialSystem(SpatialSystem ref, IngridDocument refDoc) {
		if (ref == null) {
			return refDoc;
		}

		refDoc.put(MdekKeys.REFERENCESYSTEM_ID, ref.getReferencesystemKey());
		refDoc.put(MdekKeys.COORDINATE_SYSTEM, ref.getReferencesystemValue());

		return refDoc;
	}


	public IngridDocument mapObjectMetadata(ObjectMetadata ref, IngridDocument refDoc,
			MappingQuantity howMuch) {
		if (ref == null) {
			return refDoc;
		}

		refDoc.put(MdekKeys.ENTITY_METADATA_ID, ref.getId());
		refDoc.put(MdekKeys.LASTEXPORT_TIME, ref.getLastexportTime());
		refDoc.put(MdekKeys.EXPIRY_STATE, ref.getExpiryState());
		refDoc.put(MdekKeys.MARK_DELETED, ref.getMarkDeleted());
		refDoc.put(MdekKeys.ASSIGNER_UUID, ref.getAssignerUuid());
		refDoc.put(MdekKeys.ASSIGN_TIME, ref.getAssignTime());
		refDoc.put(MdekKeys.REASSIGNER_UUID, ref.getReassignerUuid());
		refDoc.put(MdekKeys.REASSIGN_TIME, ref.getReassignTime());

		// also detailed assigner user if requested !
		mapAssignerUser(ref.getAssignerUuid(), refDoc, howMuch);

		return refDoc;
	}
}
