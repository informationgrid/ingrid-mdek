package de.ingrid.mdek.services.persistence.db.mapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.MdekUtils.IdcEntityType;
import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.services.catalog.MdekKeyValueService;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.IEntity;
import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.dao.ISearchtermSnsDao;
import de.ingrid.mdek.services.persistence.db.dao.ISearchtermValueDao;
import de.ingrid.mdek.services.persistence.db.dao.ISpatialRefSnsDao;
import de.ingrid.mdek.services.persistence.db.dao.ISpatialRefValueDao;
import de.ingrid.mdek.services.persistence.db.model.AddressComment;
import de.ingrid.mdek.services.persistence.db.model.AddressNode;
import de.ingrid.mdek.services.persistence.db.model.ObjectComment;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;
import de.ingrid.mdek.services.persistence.db.model.ObjectReference;
import de.ingrid.mdek.services.persistence.db.model.SearchtermAdr;
import de.ingrid.mdek.services.persistence.db.model.SearchtermObj;
import de.ingrid.mdek.services.persistence.db.model.SearchtermSns;
import de.ingrid.mdek.services.persistence.db.model.SearchtermValue;
import de.ingrid.mdek.services.persistence.db.model.SpatialRefSns;
import de.ingrid.mdek.services.persistence.db.model.SpatialRefValue;
import de.ingrid.mdek.services.persistence.db.model.SpatialReference;
import de.ingrid.mdek.services.persistence.db.model.T0110AvailFormat;
import de.ingrid.mdek.services.persistence.db.model.T0112MediaOption;
import de.ingrid.mdek.services.persistence.db.model.T0113DatasetReference;
import de.ingrid.mdek.services.persistence.db.model.T0114EnvCategory;
import de.ingrid.mdek.services.persistence.db.model.T0114EnvTopic;
import de.ingrid.mdek.services.persistence.db.model.T011ObjData;
import de.ingrid.mdek.services.persistence.db.model.T011ObjDataPara;
import de.ingrid.mdek.services.persistence.db.model.T011ObjGeo;
import de.ingrid.mdek.services.persistence.db.model.T011ObjGeoKeyc;
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
 * Singleton encapsulating methods for mapping ingrid documents to hibernate beans.
 * 
 * @author Martin
 */
public class DocToBeanMapper implements IMapper {

	private static final Logger LOG = Logger.getLogger(DocToBeanMapper.class);

	private static DocToBeanMapper myInstance;
	
	private ISpatialRefSnsDao daoSpatialRefSns;
	private ISpatialRefValueDao daoSpatialRefValue;
	private ISearchtermSnsDao daoSearchtermSns;
	private ISearchtermValueDao daoSearchtermValue;

	private IGenericDao<IEntity> daoSpatialReference;
	private IGenericDao<IEntity> daoSearchtermObj;
	private IGenericDao<IEntity> daoSearchtermAdr;
	private IGenericDao<IEntity> daoT021Communication;
	private IGenericDao<IEntity> daoT012ObjAdr;
	private IGenericDao<IEntity> daoObjectReference;
	private IGenericDao<IEntity> daoT017UrlRef;
	private IGenericDao<IEntity> daoT0113DatasetReference;
	private IGenericDao<IEntity> daoT014InfoImpart;
	private IGenericDao<IEntity> daoT011ObjGeo;
	private IGenericDao<IEntity> daoT011ObjGeoKeyc;
	private IGenericDao<IEntity> daoT011ObjGeoScale;
	private IGenericDao<IEntity> daoT011ObjGeoSymc;
	private IGenericDao<IEntity> daoT011ObjGeoSupplinfo;
	private IGenericDao<IEntity> daoT011ObjGeoVector;
	private IGenericDao<IEntity> daoT011ObjGeoSpatialRep;
	private IGenericDao<IEntity> daoT015Legist;
	private IGenericDao<IEntity> daoT0110AvailFormat;
	private IGenericDao<IEntity> daoT0112MediaOption;
	private IGenericDao<IEntity> daoT0114EnvCategory;
	private IGenericDao<IEntity> daoT0114EnvTopic;
	private IGenericDao<IEntity> daoT011ObjTopicCat;
	private IGenericDao<IEntity> daoT011ObjData;
	private IGenericDao<IEntity> daoT011ObjDataPara;
	private IGenericDao<IEntity> daoT011ObjProject;
	private IGenericDao<IEntity> daoT011ObjLiterature;
	private IGenericDao<IEntity> daoObjectComment;
	private IGenericDao<IEntity> daoAddressComment;
	private IGenericDao<IEntity> daoT011ObjServ;
	private IGenericDao<IEntity> daoT011ObjServVersion;
	private IGenericDao<IEntity> daoT011ObjServOperation;
	private IGenericDao<IEntity> daoT011ObjServOpPlatform;
	private IGenericDao<IEntity> daoT011ObjServOpDepends;
	private IGenericDao<IEntity> daoT011ObjServOpConnpoint;
	private IGenericDao<IEntity> daoT011ObjServOpPara;

	private MdekKeyValueService keyValueService;

	/** Get The Singleton */
	public static synchronized DocToBeanMapper getInstance(DaoFactory daoFactory) {
		if (myInstance == null) {
	        myInstance = new DocToBeanMapper(daoFactory);
	      }
		return myInstance;
	}

	private DocToBeanMapper(DaoFactory daoFactory) {
		daoSpatialRefSns = daoFactory.getSpatialRefSnsDao();
		daoSpatialRefValue = daoFactory.getSpatialRefValueDao();
		daoSearchtermSns = daoFactory.getSearchtermSnsDao();
		daoSearchtermValue = daoFactory.getSearchtermValueDao();

		daoSpatialReference = daoFactory.getDao(SpatialReference.class);
		daoSearchtermObj = daoFactory.getDao(SearchtermObj.class);
		daoSearchtermAdr = daoFactory.getDao(SearchtermAdr.class);
		daoT021Communication = daoFactory.getDao(T021Communication.class);
		daoT012ObjAdr = daoFactory.getDao(T012ObjAdr.class);
		daoObjectReference = daoFactory.getDao(ObjectReference.class);
		daoT017UrlRef = daoFactory.getDao(T017UrlRef.class);
		daoT0113DatasetReference = daoFactory.getDao(T0113DatasetReference.class);
		daoT014InfoImpart = daoFactory.getDao(T014InfoImpart.class);
		daoT011ObjGeo = daoFactory.getDao(T011ObjGeo.class);
		daoT011ObjGeoKeyc = daoFactory.getDao(T011ObjGeoKeyc.class);
		daoT011ObjGeoScale = daoFactory.getDao(T011ObjGeoScale.class);
		daoT011ObjGeoSymc = daoFactory.getDao(T011ObjGeoSymc.class);
		daoT011ObjGeoSupplinfo = daoFactory.getDao(T011ObjGeoSupplinfo.class);
		daoT011ObjGeoVector = daoFactory.getDao(T011ObjGeoVector.class);
		daoT011ObjGeoSpatialRep= daoFactory.getDao(T011ObjGeoSpatialRep.class);
		daoT015Legist = daoFactory.getDao(T015Legist.class);
		daoT0110AvailFormat = daoFactory.getDao(T0110AvailFormat.class);
		daoT0112MediaOption = daoFactory.getDao(T0112MediaOption.class);
		daoT0114EnvCategory = daoFactory.getDao(T0114EnvCategory.class);
		daoT0114EnvTopic = daoFactory.getDao(T0114EnvTopic.class);
		daoT011ObjTopicCat = daoFactory.getDao(T011ObjTopicCat.class);
		daoT011ObjData = daoFactory.getDao(T011ObjData.class);
		daoT011ObjDataPara = daoFactory.getDao(T011ObjDataPara.class);
		daoT011ObjProject = daoFactory.getDao(T011ObjProject.class);
		daoT011ObjLiterature = daoFactory.getDao(T011ObjLiterature.class);
		daoObjectComment = daoFactory.getDao(ObjectComment.class);
		daoAddressComment = daoFactory.getDao(AddressComment.class);
		daoT011ObjServ = daoFactory.getDao(T011ObjServ.class);
		daoT011ObjServVersion = daoFactory.getDao(T011ObjServVersion.class);
		daoT011ObjServOperation = daoFactory.getDao(T011ObjServOperation.class);
		daoT011ObjServOpPlatform = daoFactory.getDao(T011ObjServOpPlatform.class);
		daoT011ObjServOpDepends = daoFactory.getDao(T011ObjServOpDepends.class);
		daoT011ObjServOpConnpoint = daoFactory.getDao(T011ObjServOpConnpoint.class);
		daoT011ObjServOpPara = daoFactory.getDao(T011ObjServOpPara.class);

		keyValueService = MdekKeyValueService.getInstance(daoFactory);
	}


	public T03Catalogue mapT03Catalog(IngridDocument inDoc, T03Catalogue cat) {
		cat.setCatUuid(inDoc.getString(MdekKeys.UUID));
		cat.setCatName(inDoc.getString(MdekKeys.CATALOG_NAME));
		cat.setPartnerName(inDoc.getString(MdekKeys.PARTNER_NAME));
		cat.setProviderName(inDoc.getString(MdekKeys.PROVIDER_NAME));
		cat.setCountryCode(inDoc.getString(MdekKeys.COUNTRY));
		cat.setLanguageCode(inDoc.getString(MdekKeys.LANGUAGE));
		cat.setWorkflowControl(inDoc.getString(MdekKeys.WORKFLOW_CONTROL));
		cat.setExpiryDuration((Integer) inDoc.get(MdekKeys.EXPIRY_DURATION));
		String creationDate = (String) inDoc.get(MdekKeys.DATE_OF_CREATION);
		if (creationDate != null) {
			cat.setCreateTime(creationDate);				
		}
		cat.setModTime((String) inDoc.get(MdekKeys.DATE_OF_LAST_MODIFICATION));
		cat.setModUuid(extractModUserUuid(inDoc));

		updateSpatialRefValueOfCatalogue(inDoc, cat);

		return cat;
	}

	/**
	 * Transfer data of passed doc to passed bean according to mapping type.
	 */
	public ObjectNode mapObjectNode(IngridDocument oDocIn, ObjectNode oNodeIn) {
		oNodeIn.setObjUuid((String) oDocIn.get(MdekKeys.UUID));
		if (oDocIn.containsKey(MdekKeys.PARENT_UUID)) {
			// NOTICE: parent may be null, then root node !
			String parentUuid = (String) oDocIn.get(MdekKeys.PARENT_UUID);
			oNodeIn.setFkObjUuid(parentUuid);
		}

		return oNodeIn;
	}

	public AddressNode mapAddressNode(IngridDocument docIn, AddressNode nodeIn) {
		nodeIn.setAddrUuid((String) docIn.get(MdekKeys.UUID));
		if (docIn.containsKey(MdekKeys.PARENT_UUID)) {
			// NOTICE: parent may be null, then root node !
			String parentUuid = (String) docIn.get(MdekKeys.PARENT_UUID);
			nodeIn.setFkAddrUuid(parentUuid);
		}

		return nodeIn;
	}

	/**
	 * Transfer data of passed doc to passed bean according to mapping type.
	 */
	public T01Object mapT01Object(IngridDocument oDocIn, T01Object oIn, MappingQuantity howMuch) {

		oIn.setObjUuid((String) oDocIn.get(MdekKeys.UUID));
		oIn.setObjClass((Integer) oDocIn.get(MdekKeys.CLASS));
		oIn.setObjName((String) oDocIn.get(MdekKeys.TITLE));
		oIn.setWorkState((String) oDocIn.get(MdekKeys.WORK_STATE));
		String creationDate = (String) oDocIn.get(MdekKeys.DATE_OF_CREATION);
		if (creationDate != null) {
			oIn.setCreateTime(creationDate);				
		}
		oIn.setModTime((String) oDocIn.get(MdekKeys.DATE_OF_LAST_MODIFICATION));

		if (howMuch == MappingQuantity.DETAIL_ENTITY ||
				howMuch == MappingQuantity.COPY_ENTITY)
		{
			oIn.setDatasetAlternateName((String) oDocIn.get(MdekKeys.DATASET_ALTERNATE_NAME));
			oIn.setObjDescr((String) oDocIn.get(MdekKeys.ABSTRACT));

			oIn.setVerticalExtentMinimum((Double) oDocIn.get(MdekKeys.VERTICAL_EXTENT_MINIMUM));
			oIn.setVerticalExtentMaximum((Double) oDocIn.get(MdekKeys.VERTICAL_EXTENT_MAXIMUM));
			oIn.setVerticalExtentUnit((Integer) oDocIn.get(MdekKeys.VERTICAL_EXTENT_UNIT));
			oIn.setVerticalExtentVdatum((Integer) oDocIn.get(MdekKeys.VERTICAL_EXTENT_VDATUM));
			oIn.setLocDescr((String) oDocIn.get(MdekKeys.DESCRIPTION_OF_SPATIAL_DOMAIN));

			oIn.setTimeType((String) oDocIn.get(MdekKeys.TIME_TYPE));
			oIn.setTimeFrom((String) oDocIn.get(MdekKeys.BEGINNING_DATE));
			oIn.setTimeTo((String) oDocIn.get(MdekKeys.ENDING_DATE));
			oIn.setTimeStatus((Integer) oDocIn.get(MdekKeys.TIME_STATUS));
			oIn.setTimePeriod((Integer) oDocIn.get(MdekKeys.TIME_PERIOD));
			oIn.setTimeInterval((String) oDocIn.get(MdekKeys.TIME_STEP));
			oIn.setTimeAlle((String) oDocIn.get(MdekKeys.TIME_SCALE));
			oIn.setTimeDescr((String) oDocIn.get(MdekKeys.DESCRIPTION_OF_TEMPORAL_DOMAIN));
			
			oIn.setMetadataLanguageCode((String) oDocIn.get(MdekKeys.METADATA_LANGUAGE));
			oIn.setDataLanguageCode((String) oDocIn.get(MdekKeys.DATA_LANGUAGE));
			oIn.setPublishId((Integer) oDocIn.get(MdekKeys.PUBLICATION_CONDITION));
			oIn.setInfoNote((String) oDocIn.get(MdekKeys.DATASET_INTENSIONS));
			oIn.setDatasetUsage((String) oDocIn.get(MdekKeys.DATASET_USAGE));

			oIn.setOrderingInstructions((String) oDocIn.get(MdekKeys.ORDERING_INSTRUCTIONS));
			oIn.setAvailAccessNote((String) oDocIn.get(MdekKeys.USE_CONSTRAINTS));
			oIn.setFees((String) oDocIn.get(MdekKeys.FEES));
			oIn.setIsCatalogData(oDocIn.getString(MdekKeys.IS_CATALOG_DATA));

			oIn.setModUuid(extractModUserUuid(oDocIn));
			oIn.setResponsibleUuid(extractResponsibleUserUuid(oDocIn));

			// update associations
			updateObjectReferences(oDocIn, oIn);
			updateT012ObjAdrs(oDocIn, oIn, howMuch);
			updateSpatialReferences(oDocIn, oIn);
			updateSearchtermObjs(oDocIn, oIn);
			updateT017UrlRefs(oDocIn, oIn);
			updateT0113DatasetReferences(oDocIn, oIn);
			updateT014InfoImparts(oDocIn, oIn);
			updateT015Legists(oDocIn, oIn);
			updateT0110AvailFormats(oDocIn, oIn);
			updateT0112MediaOptions(oDocIn, oIn);
			updateT0114EnvCategorys(oDocIn, oIn);
			updateT0114EnvTopics(oDocIn, oIn);
			updateT011ObjTopicCats(oDocIn, oIn);

			// technical domain map
			updateT011ObjGeo(oDocIn, oIn);
			// technical domain literature
			updateT011ObjLiterature(oDocIn, oIn);
			// technical domain service
			updateT011ObjServ(oDocIn, oIn);
			// technical domain project
			updateT011ObjProject(oDocIn, oIn);
			// technical domain dataset
			updateT011ObjData(oDocIn, oIn);

			// comments
			updateObjectComments(oDocIn, oIn);		
		}

		if (howMuch == MappingQuantity.COPY_ENTITY) {
			oIn.setOrgObjId((String) oDocIn.get(MdekKeys.ORIGINAL_CONTROL_IDENTIFIER));
			oIn.setCatId((Long) oDocIn.get(MdekKeys.CATALOGUE_IDENTIFIER));
			oIn.setDatasetCharacterSet((Integer) oDocIn.get(MdekKeys.DATASET_CHARACTER_SET));
			oIn.setMetadataCharacterSet((Integer) oDocIn.get(MdekKeys.METADATA_CHARACTER_SET));
			oIn.setMetadataStandardName((String) oDocIn.get(MdekKeys.METADATA_STANDARD_NAME));
			oIn.setMetadataStandardVersion((String) oDocIn.get(MdekKeys.METADATA_STANDARD_VERSION));
			oIn.setLastexportTime((String) oDocIn.get(MdekKeys.LASTEXPORT_TIME));
			oIn.setExpiryTime((String) oDocIn.get(MdekKeys.EXPIRY_TIME));
			oIn.setWorkVersion((Integer) oDocIn.get(MdekKeys.WORK_VERSION));
			oIn.setMarkDeleted((String) oDocIn.get(MdekKeys.MARK_DELETED));
		}

		return oIn;
	}

	public T02Address mapT02Address(IngridDocument aDocIn, T02Address aIn, MappingQuantity howMuch) {

		aIn.setAdrUuid(aDocIn.getString(MdekKeys.UUID));
		aIn.setAdrType((Integer) aDocIn.get(MdekKeys.CLASS));
		aIn.setInstitution(aDocIn.getString(MdekKeys.ORGANISATION));
		aIn.setLastname(aDocIn.getString(MdekKeys.NAME));
		aIn.setFirstname(aDocIn.getString(MdekKeys.GIVEN_NAME));
		aIn.setTitleValue(aDocIn.getString(MdekKeys.TITLE_OR_FUNCTION));
		aIn.setTitleKey((Integer)aDocIn.get(MdekKeys.TITLE_OR_FUNCTION_KEY));
		aIn.setWorkState((String) aDocIn.get(MdekKeys.WORK_STATE));
		String creationDate = (String) aDocIn.get(MdekKeys.DATE_OF_CREATION);
		if (creationDate != null) {
			aIn.setCreateTime(creationDate);				
		}
		aIn.setModTime((String) aDocIn.get(MdekKeys.DATE_OF_LAST_MODIFICATION));

		if (howMuch == MappingQuantity.DETAIL_ENTITY ||
				howMuch == MappingQuantity.COPY_ENTITY)
		{
			aIn.setStreet(aDocIn.getString(MdekKeys.STREET));
			aIn.setCountryCode(aDocIn.getString(MdekKeys.POSTAL_CODE_OF_COUNTRY));
			aIn.setPostcode(aDocIn.getString(MdekKeys.POSTAL_CODE));
			aIn.setCity(aDocIn.getString(MdekKeys.CITY));
			aIn.setPostboxPc(aDocIn.getString(MdekKeys.POST_BOX_POSTAL_CODE));
			aIn.setPostbox(aDocIn.getString(MdekKeys.POST_BOX));

			aIn.setJob(aDocIn.getString(MdekKeys.FUNCTION));
			aIn.setAddressValue(aDocIn.getString(MdekKeys.NAME_FORM));
			aIn.setAddressKey((Integer)aDocIn.get(MdekKeys.NAME_FORM_KEY));
			aIn.setDescr(aDocIn.getString(MdekKeys.ADDRESS_DESCRIPTION));

			aIn.setModUuid(extractModUserUuid(aDocIn));
			aIn.setResponsibleUuid(extractResponsibleUserUuid(aDocIn));

			// update associations
			updateT021Communications(aDocIn, aIn);
			updateSearchtermAdrs(aDocIn, aIn);
			updateAddressComments(aDocIn, aIn);
		}

		if (howMuch == MappingQuantity.COPY_ENTITY) {
			aIn.setOrgAdrId(aDocIn.getString(MdekKeys.ORIGINAL_ADDRESS_IDENTIFIER));
			aIn.setLastexportTime(aDocIn.getString(MdekKeys.LASTEXPORT_TIME));
			aIn.setExpiryTime(aDocIn.getString(MdekKeys.EXPIRY_TIME));
			aIn.setWorkVersion((Integer) aDocIn.get(MdekKeys.WORK_VERSION));
			aIn.setMarkDeleted(aDocIn.getString(MdekKeys.MARK_DELETED));
		}

		keyValueService.processKeyValue(aIn);

		return aIn;
	}

	public String extractModUserUuid(IngridDocument inDoc) {
		return extractUserUuid(inDoc, MdekKeys.MOD_USER);
	}

	public String extractResponsibleUserUuid(IngridDocument inDoc) {
		return extractUserUuid(inDoc, MdekKeys.RESPONSIBLE_USER);
	}

	private String extractUserUuid(IngridDocument inDoc, String userKeyInMap) {
		String userUuid = null;
		
		IngridDocument userDoc = (IngridDocument) inDoc.get(userKeyInMap);
		if (userDoc != null) {
			userUuid = userDoc.getString(MdekKeys.UUID);
		}
		
		return userUuid;
	}

	/**
	 * Transfer data of passed doc to passed bean.
	 * @param oFrom from object
	 * @param oToDoc the to doc containing to object data
	 * @param oO the bean to transfer data to, pass new Bean or null if new one
	 * @param line additional line data for bean
	 * @return the passed bean containing all mapped data
	 */
	private ObjectReference mapObjectReference(T01Object oFrom,
		IngridDocument oToDoc,
		ObjectReference oRef, 
		int line) 
	{
		oRef.setObjFromId(oFrom.getId());
		oRef.setObjToUuid((String) oToDoc.get(MdekKeys.UUID));
		oRef.setLine(line);
		oRef.setSpecialName((String) oToDoc.get(MdekKeys.RELATION_TYPE_NAME));
		oRef.setSpecialRef((Integer) oToDoc.get(MdekKeys.RELATION_TYPE_REF));
		oRef.setDescr((String) oToDoc.get(MdekKeys.RELATION_DESCRIPTION));
		keyValueService.processKeyValue(oRef);

		return oRef;
	}

	private void updateObjectReferences(IngridDocument oDocIn, T01Object oIn) {
		List<IngridDocument> oDocsTo = (List) oDocIn.get(MdekKeys.OBJ_REFERENCES_TO);
		if (oDocsTo == null) {
			oDocsTo = new ArrayList<IngridDocument>(0);
		}
		Set<ObjectReference> oRefs = oIn.getObjectReferences();
		ArrayList<ObjectReference> oRefs_unprocessed = new ArrayList<ObjectReference>(oRefs);
		int line = 1;
		for (IngridDocument oDocTo : oDocsTo) {
			String oToUuid = (String) oDocTo.get(MdekKeys.UUID);
			boolean found = false;
			for (ObjectReference oRef : oRefs) {
				if (oRef.getObjToUuid().equals(oToUuid)) {
					mapObjectReference(oIn, oDocTo, oRef, line);
					oRefs_unprocessed.remove(oRef);
					found = true;
					break;
				}
			}
			if (!found) {
				// add new one
				ObjectReference oRef = mapObjectReference(oIn, oDocTo, new ObjectReference(), line);
				oRefs.add(oRef);
			}
			line++;
		}
		// remove the ones not processed, will be deleted by hibernate (delete-orphan set in parent)
		for (ObjectReference oR : oRefs_unprocessed) {
			oRefs.remove(oR);
			// delete-orphan doesn't work !!!?????
			daoObjectReference.makeTransient(oR);
		}		
	}

	/**
	 * Transfer data of passed doc to passed bean.
	 * @param oFrom from object
	 * @param aToDoc the to doc containing to address data
	 * @param oA the bean to transfer data to !
	 * @param line additional line data for bean
	 * @param howMuch how much data to transfer
	 * @return the passed bean containing all mapped data
	 */
	private T012ObjAdr mapT012ObjAdr(T01Object oFrom,
		IngridDocument aToDoc,
		T012ObjAdr oA, 
		int line,
		MappingQuantity howMuch) 
	{
		oA.setObjId(oFrom.getId());
		oA.setT01Object(oFrom);
		oA.setAdrUuid((String) aToDoc.get(MdekKeys.UUID));
		oA.setType((Integer) aToDoc.get(MdekKeys.RELATION_TYPE_ID));
		oA.setSpecialName((String) aToDoc.get(MdekKeys.RELATION_TYPE_NAME));
		oA.setSpecialRef((Integer) aToDoc.get(MdekKeys.RELATION_TYPE_REF));
		oA.setLine(line);

		if (howMuch == MappingQuantity.COPY_ENTITY) {
			oA.setModTime((String) aToDoc.get(MdekKeys.RELATION_DATE_OF_LAST_MODIFICATION));
		} else {
			// set modification time only when creating a new object !
			if (oA.getId() == null) {
				String currentTime = MdekUtils.dateToTimestamp(new Date()); 
				oA.setModTime(currentTime);				
			}
		}

		keyValueService.processKeyValue(oA);

		return oA;
	}

	private void updateT012ObjAdrs(IngridDocument oDocIn, T01Object oIn, MappingQuantity howMuch) {
		List<IngridDocument> aDocsTo = (List) oDocIn.get(MdekKeys.ADR_REFERENCES_TO);
		if (aDocsTo == null) {
			aDocsTo = new ArrayList<IngridDocument>(0);
		}
		Set<T012ObjAdr> oAs = oIn.getT012ObjAdrs();
		ArrayList<T012ObjAdr> oAs_unprocessed = new ArrayList<T012ObjAdr>(oAs);
		int line = 1;
		for (IngridDocument aDocTo : aDocsTo) {
			String inUuidTo = (String) aDocTo.get(MdekKeys.UUID);
			Integer inRelType = (Integer) aDocTo.get(MdekKeys.RELATION_TYPE_ID);
			boolean found = false;
			for (T012ObjAdr oA : oAs) {
				if (oA.getAdrUuid().equals(inUuidTo) &&
					oA.getType().equals(inRelType))
				{
					mapT012ObjAdr(oIn, aDocTo, oA, line, howMuch);
					oAs_unprocessed.remove(oA);
					found = true;
					break;
				}
			}
			if (!found) {
				// add new one
				T012ObjAdr oA = mapT012ObjAdr(oIn, aDocTo, new T012ObjAdr(), line, howMuch);
				oAs.add(oA);
			}
			line++;
		}
		// remove the ones not processed, will be deleted by hibernate (delete-orphan set in parent)
		for (T012ObjAdr oA : oAs_unprocessed) {
			oAs.remove(oA);
			// delete-orphan doesn't work !!!?????
			daoT012ObjAdr.makeTransient(oA);
		}		
	}

	/**
	 * Transfer data to passed bean.
	 */
	private SpatialReference mapSpatialReference(T01Object oFrom,
		SpatialRefValue spRefValue,
		SpatialReference spRef,
		int line) 
	{
		spRef.setObjId(oFrom.getId());
		spRef.setSpatialRefValue(spRefValue);			
		spRef.setSpatialRefId(spRefValue.getId());
		spRef.setLine(line);

		return spRef;
	}
	/**
	 * Transfer data to passed bean.
	 */
	private SpatialRefValue mapSpatialRefValue(SpatialRefSns spRefSns,
		IngridDocument locDoc,
		SpatialRefValue spRefValue) 
	{
		spRefValue.setNameValue(locDoc.getString(MdekKeys.LOCATION_NAME));
		spRefValue.setNameKey((Integer)locDoc.get(MdekKeys.LOCATION_NAME_KEY));
		spRefValue.setType((String) locDoc.get(MdekKeys.LOCATION_TYPE));
		spRefValue.setNativekey((String) locDoc.get(MdekKeys.LOCATION_CODE));
		spRefValue.setX1((Double) locDoc.get(MdekKeys.WEST_BOUNDING_COORDINATE));
		spRefValue.setY1((Double) locDoc.get(MdekKeys.SOUTH_BOUNDING_COORDINATE));
		spRefValue.setX2((Double) locDoc.get(MdekKeys.EAST_BOUNDING_COORDINATE));
		spRefValue.setY2((Double) locDoc.get(MdekKeys.NORTH_BOUNDING_COORDINATE));

		Long spRefSnsId = null;
		if (spRefSns != null) {
			spRefSnsId = spRefSns.getId();			
		}
		spRefValue.setSpatialRefSns(spRefSns);			
		spRefValue.setSpatialRefSnsId(spRefSnsId);
		keyValueService.processKeyValue(spRefValue);

		return spRefValue;
	}
	private void updateSpatialReferences(IngridDocument oDocIn, T01Object oIn) {
		List<IngridDocument> locList = (List) oDocIn.get(MdekKeys.LOCATIONS);
		if (locList == null) {
			locList = new ArrayList<IngridDocument>(0);
		}

		Set<SpatialReference> spatialRefs = oIn.getSpatialReferences();
		ArrayList<SpatialReference> spatialRefs_unprocessed = new ArrayList<SpatialReference>(spatialRefs);

		int line = 1;
		for (IngridDocument loc : locList) {
			boolean found = false;
			for (SpatialReference spRef : spatialRefs) {
				SpatialRefValue spRefValue = spRef.getSpatialRefValue();
				if (spRefValue != null) {
					found = updateSpatialRefValueViaDoc(loc, spRefValue);
					if (found) {
						// update line
						spRef.setLine(line);
						spatialRefs_unprocessed.remove(spRef);
						break;
					}
				}
			}
			if (!found) {
				SpatialRefValue spRefValue = loadOrCreateSpatialRefValueViaDoc(loc, oIn.getId());

				// then create SpatialReference
				SpatialReference spRef = new SpatialReference();
				mapSpatialReference(oIn, spRefValue, spRef, line);
				spatialRefs.add(spRef);
			}
			line++;
		}
		// remove the ones not processed, will be deleted by hibernate (delete-orphan set in parent)
		for (SpatialReference spRef : spatialRefs_unprocessed) {
			spatialRefs.remove(spRef);
			// delete-orphan doesn't work !!!?????
			daoSpatialReference.makeTransient(spRef);
		}		
	}

	/** Update SpatialRefValue of catalog. NOTICE: CATALOG SPATIAL REF IS ALWAYS FROM GEOTHESAURUS !!! */
	private void updateSpatialRefValueOfCatalogue(IngridDocument cDocIn, T03Catalogue cIn) {
		IngridDocument locDoc = (IngridDocument) cDocIn.get(MdekKeys.CATALOG_LOCATION);

		String locSnsId = locDoc.getString(MdekKeys.LOCATION_SNS_ID);
		if (locSnsId == null) {
			throw new MdekException(new MdekError(MdekErrorType.CATALOG_DATA_MISSING));
		}

		SpatialRefValue spRefValue = cIn.getSpatialRefValue();

		boolean updated = false;
		if (spRefValue != null) {
			updated = updateSpatialRefValueViaDoc(locDoc, spRefValue);
		}
		if (!updated) {
			spRefValue = loadOrCreateSpatialRefValueViaDoc(locDoc, null);

			cIn.setSpatialRefId(spRefValue.getId());
			cIn.setSpatialRefValue(spRefValue);
		}
	}

	/** Checks whether passed location doc represents passed SpatialRefValue and updates
	 * data in entity if so.
	 * @param locationDoc data describing SpatialRefValue
	 * @param spRefValue SpatialRefValue entity
	 * @return true=spRefValue entity was updated<br>
	 * false=spRefValue entity is different from locationDoc and was NOT updated
	 */
	private boolean updateSpatialRefValueViaDoc(IngridDocument locationDoc, SpatialRefValue spRefValue) {
		String locNameValue = (String) locationDoc.get(MdekKeys.LOCATION_NAME);
		String locNameValue_notNull = (locNameValue == null) ? "" : locNameValue;
		Integer locNameKey = (Integer) locationDoc.get(MdekKeys.LOCATION_NAME_KEY);
		Integer locNameKey_notNull = (locNameKey == null) ? new Integer(-1) : locNameKey;
		String locType = (String) locationDoc.get(MdekKeys.LOCATION_TYPE);
		String locSnsId = (String) locationDoc.get(MdekKeys.LOCATION_SNS_ID);
		String locSnsId_notNull = (locSnsId == null) ? "" : locSnsId;
		String locCode = (String) locationDoc.get(MdekKeys.LOCATION_CODE);
		String locCode_notNull = (locCode == null) ? "" : locCode;

		SpatialRefSns spRefSns = spRefValue.getSpatialRefSns();

		String refNameValue_notNull = (spRefValue.getNameValue() == null) ? "" : spRefValue.getNameValue();
		Integer refNameKey_notNull = (spRefValue.getNameKey() == null) ? new Integer(-1) : spRefValue.getNameKey();
		String refType = spRefValue.getType();
		String refSnsId_notNull = (spRefSns == null) ? "" : spRefSns.getSnsId();
		String refCode_notNull = (spRefValue.getNativekey() == null) ? "" : spRefValue.getNativekey();

		boolean updated = false;
		if (locNameValue_notNull.equals(refNameValue_notNull) &&
			locNameKey_notNull.intValue() == refNameKey_notNull.intValue() &&
			locType.equals(refType) &&
			locSnsId_notNull.equals(refSnsId_notNull) &&
			locCode_notNull.equals(refCode_notNull))
		{
			mapSpatialRefValue(spRefSns, locationDoc, spRefValue);
			updated = true;
		}					
		
		return updated;
	}

	/** Get the SpatialRefValue entity according to the passed location document.
	 * @param locationDoc data describing SpatialRefValue
	 * @param objectId SpatialRef is connected to this object, PASS NULL IF CONNECTION DOESN'T MATTER
	 * @return persistent SpatialRefValue (with Id)
	 */
	private SpatialRefValue loadOrCreateSpatialRefValueViaDoc(IngridDocument locationDoc, Long objectId) {
		// first load/create SpatialRefSns
		String locSnsId = (String) locationDoc.get(MdekKeys.LOCATION_SNS_ID);
		SpatialRefSns spRefSns = null;
		if (locSnsId != null) {
			spRefSns = daoSpatialRefSns.loadOrCreate(locSnsId);
		}

		String locNameValue = (String) locationDoc.get(MdekKeys.LOCATION_NAME);
		Integer locNameKey = (Integer) locationDoc.get(MdekKeys.LOCATION_NAME_KEY);
		String locType = (String) locationDoc.get(MdekKeys.LOCATION_TYPE);
		String locCode = (String) locationDoc.get(MdekKeys.LOCATION_CODE);

		// then load/create SpatialRefValue
		// NOTICE: Freie Raumbezuege (SpatialRefValue) werden IMMER neu angelegt, wenn die Objektbeziehung nicht vorhanden ist.
		// Selbst wenn der identische Freie Raumbezug vorhanden ist. Beim Loeschen des Objektes wird nur die Referenz (SpatialReference)
		// geloescht (cascade nicht moeglich, da hier auch Thesaurusbegriffe drin stehen, die erhalten bleiben sollen ! bei denen wird
		// der vorhandene Thesaurus Begriff genommen, wenn schon da; dies ist bei Freien nicht moeglich, da die ja Objektspezifisch
		// geaendert werden koennen). -> Aufraeum Job noetig ! 
		// TODO: Aufraeum Job noetig, der Freie Raumbezug Leichen (in SpatialRefValue) beseitigt !!!
		SpatialRefValue spRefValue = daoSpatialRefValue.loadOrCreate(locType, locNameValue, locNameKey, spRefSns, locCode, objectId);
		mapSpatialRefValue(spRefSns, locationDoc, spRefValue);
		
		return spRefValue;
	}

	private void updateObjectComments(IngridDocument oDocIn, T01Object oIn) {
		Set<ObjectComment> refs = oIn.getObjectComments();
		ArrayList<ObjectComment> refs_unprocessed = new ArrayList<ObjectComment>(refs);
		// remove all !
		for (ObjectComment ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			daoObjectComment.makeTransient(ref);			
		}		
		
		List<IngridDocument> refDocs = (List) oDocIn.get(MdekKeys.COMMENT_LIST);
		if (refDocs != null) {
			// and add all new ones !
			String now = MdekUtils.dateToTimestamp(new Date());
			for (IngridDocument refDoc : refDocs) {
				ObjectComment ref = new ObjectComment();
				ref.setObjId(oIn.getId());
				ref.setComment(refDoc.getString(MdekKeys.COMMENT));
				String createTime = refDoc.getString(MdekKeys.CREATE_TIME);
				if (createTime == null) {
					createTime = now;
				}
				ref.setCreateTime(createTime);
				ref.setCreateUuid(extractUserUuid(refDoc, MdekKeys.CREATE_USER));

				refs.add(ref);
			}
		}
	}
	
	private void updateAddressComments(IngridDocument aDocIn, T02Address aIn) {
		Set<AddressComment> refs = aIn.getAddressComments();
		ArrayList<AddressComment> refs_unprocessed = new ArrayList<AddressComment>(refs);
		// remove all !
		for (AddressComment ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			daoAddressComment.makeTransient(ref);			
		}		
		
		List<IngridDocument> refDocs = (List) aDocIn.get(MdekKeys.COMMENT_LIST);
		if (refDocs != null) {
			// and add all new ones !
			String now = MdekUtils.dateToTimestamp(new Date());
			for (IngridDocument refDoc : refDocs) {
				AddressComment ref = new AddressComment();
				ref.setAddrId(aIn.getId());
				ref.setComment(refDoc.getString(MdekKeys.COMMENT));
				String createTime = refDoc.getString(MdekKeys.CREATE_TIME);
				if (createTime == null) {
					createTime = now;
				}
				ref.setCreateTime(createTime);
				ref.setCreateUuid(extractUserUuid(refDoc, MdekKeys.CREATE_USER));

				refs.add(ref);
			}
		}
	}

	private void updateT021Communications(IngridDocument aDocIn, T02Address aIn) {
		Set<T021Communication> refs = aIn.getT021Communications();
		ArrayList<T021Communication> refs_unprocessed = new ArrayList<T021Communication>(refs);
		// remove all !
		for (T021Communication ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			daoT021Communication.makeTransient(ref);			
		}

		// and add new ones !
		List<IngridDocument> refDocs = (List) aDocIn.get(MdekKeys.COMMUNICATION);
		if (refDocs == null) {
			refDocs = new ArrayList<IngridDocument>(0);
		}
		// and add all new ones !
		int line = 1;
		for (IngridDocument refDoc : refDocs) {
			T021Communication ref = mapT021Communication(aIn, refDoc, new T021Communication(), line);
			refs.add(ref);
			line++;
		}
	}
	private T021Communication mapT021Communication(T02Address aFrom,
		IngridDocument refDoc,
		T021Communication ref,
		int line)
	{
		ref.setAdrId(aFrom.getId());
		ref.setCommtypeValue(refDoc.getString(MdekKeys.COMMUNICATION_MEDIUM));
		ref.setCommtypeKey((Integer)refDoc.get(MdekKeys.COMMUNICATION_MEDIUM_KEY));
		ref.setCommValue(refDoc.getString(MdekKeys.COMMUNICATION_VALUE));
		ref.setDescr(refDoc.getString(MdekKeys.COMMUNICATION_DESCRIPTION));
		ref.setLine(line);
		keyValueService.processKeyValue(ref);

		return ref;
	}

	/**
	 * Transfer data to passed bean.
	 */
	private T017UrlRef mapT017UrlRef(T01Object oFrom,
		IngridDocument urlDoc,
		T017UrlRef urlRef, 
		int line) 
	{
		urlRef.setObjId(oFrom.getId());
		urlRef.setUrlLink((String) urlDoc.get(MdekKeys.LINKAGE_URL));
		urlRef.setSpecialRef((Integer) urlDoc.get(MdekKeys.LINKAGE_REFERENCE_ID));
		urlRef.setSpecialName((String) urlDoc.get(MdekKeys.LINKAGE_REFERENCE));
		urlRef.setDatatypeValue((String) urlDoc.get(MdekKeys.LINKAGE_DATATYPE));
		urlRef.setDatatypeKey((Integer)urlDoc.get(MdekKeys.LINKAGE_DATATYPE_KEY));
		urlRef.setVolume((String) urlDoc.get(MdekKeys.LINKAGE_VOLUME));
		urlRef.setIcon((String) urlDoc.get(MdekKeys.LINKAGE_ICON_URL));
		urlRef.setIconText((String) urlDoc.get(MdekKeys.LINKAGE_ICON_TEXT));
		urlRef.setDescr((String) urlDoc.get(MdekKeys.LINKAGE_DESCRIPTION));
		urlRef.setContent((String) urlDoc.get(MdekKeys.LINKAGE_NAME));
		urlRef.setUrlType((Integer) urlDoc.get(MdekKeys.LINKAGE_URL_TYPE));		
		urlRef.setLine(line);
		keyValueService.processKeyValue(urlRef);

		return urlRef;
	}

	private void updateT017UrlRefs(IngridDocument oDocIn, T01Object oIn) {
		List<IngridDocument> urlDocs = (List) oDocIn.get(MdekKeys.LINKAGES);
		if (urlDocs == null) {
			urlDocs = new ArrayList<IngridDocument>(0);
		}
		Set<T017UrlRef> urlRefs = oIn.getT017UrlRefs();
		ArrayList<T017UrlRef> refs_unprocessed = new ArrayList<T017UrlRef>(urlRefs);
		// remove all !
		for (T017UrlRef ref : refs_unprocessed) {
			urlRefs.remove(ref);
			// delete-orphan doesn't work !!!?????
			daoT017UrlRef.makeTransient(ref);			
		}		
		// and add all new ones !
		int line = 1;
		for (IngridDocument urlDoc : urlDocs) {
			// add all as new ones
			T017UrlRef urlRef = mapT017UrlRef(oIn, urlDoc, new T017UrlRef(), line);
			urlRefs.add(urlRef);
			line++;
		}
	}

	private T0113DatasetReference mapT0113DatasetReference(T01Object oFrom,
		IngridDocument refDoc,
		T0113DatasetReference ref, 
		int line) 
	{
		ref.setObjId(oFrom.getId());
		ref.setReferenceDate((String) refDoc.get(MdekKeys.DATASET_REFERENCE_DATE));
		ref.setType((Integer) refDoc.get(MdekKeys.DATASET_REFERENCE_TYPE));
		ref.setLine(line);

		return ref;
	}
	private void updateT0113DatasetReferences(IngridDocument oDocIn, T01Object oIn) {
		List<IngridDocument> refDocs = (List) oDocIn.get(MdekKeys.DATASET_REFERENCES);
		if (refDocs == null) {
			refDocs = new ArrayList<IngridDocument>(0);
		}
		Set<T0113DatasetReference> refs = oIn.getT0113DatasetReferences();
		ArrayList<T0113DatasetReference> refs_unprocessed = new ArrayList<T0113DatasetReference>(refs);
		// remove all !
		for (T0113DatasetReference ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			daoT0113DatasetReference.makeTransient(ref);			
		}		
		// and add all new ones !
		int line = 1;
		for (IngridDocument refDoc : refDocs) {
			// add all as new ones
			T0113DatasetReference ref = mapT0113DatasetReference(oIn, refDoc, new T0113DatasetReference(), line);
			refs.add(ref);
			line++;
		}
	}

	private T014InfoImpart mapT014InfoImpart(T01Object oFrom,
			IngridDocument refDoc,
			T014InfoImpart ref, 
			int line) 
	{
		ref.setObjId(oFrom.getId());
		ref.setImpartValue(refDoc.getString(MdekKeys.EXPORT_VALUE));
		ref.setImpartKey((Integer)refDoc.get(MdekKeys.EXPORT_KEY));
		ref.setLine(line);
		keyValueService.processKeyValue(ref);

		return ref;
	}
	private void updateT014InfoImparts(IngridDocument oDocIn, T01Object oIn) {
		List<IngridDocument> refDocs = (List) oDocIn.get(MdekKeys.EXPORTS);
		if (refDocs == null) {
			refDocs = new ArrayList<IngridDocument>(0);
		}
		Set<T014InfoImpart> refs = oIn.getT014InfoImparts();
		ArrayList<T014InfoImpart> refs_unprocessed = new ArrayList<T014InfoImpart>(refs);
		// remove all !
		for (T014InfoImpart ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			daoT014InfoImpart.makeTransient(ref);			
		}		
		// and add all new ones !
		int line = 1;
		for (IngridDocument refDoc : refDocs) {
			// add all as new ones
			T014InfoImpart ref = mapT014InfoImpart(oIn, refDoc, new T014InfoImpart(), line);
			refs.add(ref);
			line++;
		}
	}

	private void updateT011ObjGeo(IngridDocument oDocIn, T01Object oIn) {
		Set<T011ObjGeo> refs = oIn.getT011ObjGeos();
		ArrayList<T011ObjGeo> refs_unprocessed = new ArrayList<T011ObjGeo>(refs);
		// remove all !
		for (T011ObjGeo ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			daoT011ObjGeo.makeTransient(ref);			
		}
		
		IngridDocument refDoc = (IngridDocument)oDocIn.get(MdekKeys.TECHNICAL_DOMAIN_MAP);
		if (refDoc != null) {
			// and the new one, should be only one, because of the 1:1 relation of tables 
			T011ObjGeo ref = new T011ObjGeo();
			ref.setObjId(oIn.getId());
			ref.setSpecialBase(refDoc.getString(MdekKeys.TECHNICAL_BASE));
			ref.setDataBase(refDoc.getString(MdekKeys.DATA));
			ref.setMethod(refDoc.getString(MdekKeys.METHOD_OF_PRODUCTION));
			ref.setReferencesystemValue(refDoc.getString(MdekKeys.COORDINATE_SYSTEM));
			ref.setRecExact((Double)refDoc.get(MdekKeys.RESOLUTION));
			ref.setRecGrade((Double)refDoc.get(MdekKeys.DEGREE_OF_RECORD));
			ref.setHierarchyLevel((Integer)refDoc.get(MdekKeys.HIERARCHY_LEVEL));
			ref.setVectorTopologyLevel((Integer)refDoc.get(MdekKeys.VECTOR_TOPOLOGY_LEVEL));
			ref.setReferencesystemKey((Integer)refDoc.get(MdekKeys.REFERENCESYSTEM_ID));
			ref.setPosAccuracyVertical((Double)refDoc.get(MdekKeys.POS_ACCURACY_VERTICAL));
			ref.setKeycInclWDataset((Integer)refDoc.get(MdekKeys.KEYC_INCL_W_DATASET));
			
			// save the object and get ID from database (cascading insert do not work??)
			daoT011ObjGeo.makePersistent(ref);
			
			// map 1:N relations
			updateT011ObjGeoKeycs(refDoc, ref);
			updateT011ObjGeoScales(refDoc, ref);
			updateT011ObjGeoSymcs(refDoc, ref);
			updateT011ObjGeoSupplinfos(refDoc, ref);
			updateT011ObjGeoVectors(refDoc, ref);
			updateT011ObjGeoSpatialReps(refDoc, ref);
			
			refs.add(ref);
		}
		
	}
	
	private void updateT011ObjGeoKeycs(IngridDocument docIn, T011ObjGeo in) {
		Set<T011ObjGeoKeyc> refs = in.getT011ObjGeoKeycs();
		ArrayList<T011ObjGeoKeyc> refs_unprocessed = new ArrayList<T011ObjGeoKeyc>(refs);
		// remove all !
		for (T011ObjGeoKeyc ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			daoT011ObjGeoKeyc.makeTransient(ref);			
		}		
		
		List<IngridDocument> refDocs = (List<IngridDocument>)docIn.get(MdekKeys.KEY_CATALOG_LIST);
		if (refDocs != null) {
			// and add all new ones !
			int line = 1;
			for (IngridDocument refDoc : refDocs) {
				// add all as new ones
				T011ObjGeoKeyc ref = new T011ObjGeoKeyc();
				ref.setObjGeoId(in.getId());
				ref.setKeycValue(refDoc.getString(MdekKeys.SUBJECT_CAT));
				ref.setKeycKey((Integer)refDoc.get(MdekKeys.SUBJECT_CAT_KEY));
				ref.setKeyDate(refDoc.getString(MdekKeys.KEY_DATE));
				ref.setEdition(refDoc.getString(MdekKeys.EDITION));
				ref.setLine(line);
				keyValueService.processKeyValue(ref);
				refs.add(ref);
				line++;
			}
		}
	}

	private void updateT011ObjGeoScales(IngridDocument docIn, T011ObjGeo in) {
		Set<T011ObjGeoScale> refs = in.getT011ObjGeoScales();
		ArrayList<T011ObjGeoScale> refs_unprocessed = new ArrayList<T011ObjGeoScale>(refs);
		// remove all !
		for (T011ObjGeoScale ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			daoT011ObjGeoScale.makeTransient(ref);			
		}		
		
		List<IngridDocument> refDocs = (List<IngridDocument>)docIn.get(MdekKeys.PUBLICATION_SCALE_LIST);
		if (refDocs != null) {
			// and add all new ones !
			int line = 1;
			for (IngridDocument refDoc : refDocs) {
				// add all as new ones
				T011ObjGeoScale ref = new T011ObjGeoScale();
				ref.setObjGeoId(in.getId());
				ref.setScale((Integer)refDoc.get(MdekKeys.SCALE));
				ref.setResolutionGround((Double)refDoc.get(MdekKeys.RESOLUTION_GROUND));
				ref.setResolutionScan((Double)refDoc.get(MdekKeys.RESOLUTION_SCAN));
				ref.setLine(line);
				refs.add(ref);
				line++;
			}
		}
	}	

	private void updateT011ObjGeoSymcs(IngridDocument docIn, T011ObjGeo in) {
		Set<T011ObjGeoSymc> refs = in.getT011ObjGeoSymcs();
		ArrayList<T011ObjGeoSymc> refs_unprocessed = new ArrayList<T011ObjGeoSymc>(refs);
		// remove all !
		for (T011ObjGeoSymc ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			daoT011ObjGeoSymc.makeTransient(ref);			
		}		
		
		List<IngridDocument> refDocs = (List<IngridDocument>)docIn.get(MdekKeys.SYMBOL_CATALOG_LIST);
		if (refDocs != null) {
			// and add all new ones !
			int line = 1;
			for (IngridDocument refDoc : refDocs) {
				// add all as new ones
				T011ObjGeoSymc ref = new T011ObjGeoSymc();
				ref.setObjGeoId(in.getId());
				ref.setSymbolCatValue(refDoc.getString(MdekKeys.SYMBOL_CAT));
				ref.setSymbolCatKey((Integer)refDoc.get(MdekKeys.SYMBOL_CAT_KEY));
				ref.setSymbolDate(refDoc.getString(MdekKeys.SYMBOL_DATE));
				ref.setEdition(refDoc.getString(MdekKeys.SYMBOL_EDITION));
				ref.setLine(line);
				keyValueService.processKeyValue(ref);
				refs.add(ref);
				line++;
			}
		}
	}
	
	private void updateT011ObjGeoSupplinfos(IngridDocument docIn, T011ObjGeo in) {
		Set<T011ObjGeoSupplinfo> refs = in.getT011ObjGeoSupplinfos();
		ArrayList<T011ObjGeoSupplinfo> refs_unprocessed = new ArrayList<T011ObjGeoSupplinfo>(refs);
		// remove all !
		for (T011ObjGeoSupplinfo ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			daoT011ObjGeoSupplinfo.makeTransient(ref);			
		}		
		
		List<String> refStrs = (List<String>)docIn.get(MdekKeys.FEATURE_TYPE_LIST);
		if (refStrs != null) {
			// and add all new ones !
			int line = 1;
			for (String refStr : refStrs) {
				// add all as new ones
				T011ObjGeoSupplinfo ref = new T011ObjGeoSupplinfo();
				ref.setObjGeoId(in.getId());
				ref.setFeatureType(refStr);
				ref.setLine(line);
				refs.add(ref);
				line++;
			}
		}
	}	

	private void updateT011ObjGeoVectors(IngridDocument docIn, T011ObjGeo in) {
		Set<T011ObjGeoVector> refs = in.getT011ObjGeoVectors();
		ArrayList<T011ObjGeoVector> refs_unprocessed = new ArrayList<T011ObjGeoVector>(refs);
		// remove all !
		for (T011ObjGeoVector ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			daoT011ObjGeoVector.makeTransient(ref);			
		}		
		
		List<IngridDocument> refDocs = (List<IngridDocument>)docIn.get(MdekKeys.GEO_VECTOR_LIST);
		if (refDocs != null) {
			// and add all new ones !
			int line = 1;
			for (IngridDocument refDoc : refDocs) {
				// add all as new ones
				T011ObjGeoVector ref = new T011ObjGeoVector();
				ref.setObjGeoId(in.getId());
				ref.setGeometricObjectType((Integer)refDoc.get(MdekKeys.GEOMETRIC_OBJECT_TYPE));
				ref.setGeometricObjectCount((Integer)refDoc.get(MdekKeys.GEOMETRIC_OBJECT_COUNT));
				ref.setLine(line);
				refs.add(ref);
				line++;
			}
		}
	}	

	private void updateT011ObjGeoSpatialReps(IngridDocument docIn, T011ObjGeo in) {
		Set<T011ObjGeoSpatialRep> refs = in.getT011ObjGeoSpatialReps();
		ArrayList<T011ObjGeoSpatialRep> refs_unprocessed = new ArrayList<T011ObjGeoSpatialRep>(refs);
		// remove all !
		for (T011ObjGeoSpatialRep ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			daoT011ObjGeoSpatialRep.makeTransient(ref);			
		}		
		
		List<Integer> refInts = (List<Integer>)docIn.get(MdekKeys.SPATIAL_REPRESENTATION_TYPE_LIST);
		if (refInts != null) {
			// and add all new ones !
			int line = 1;
			for (Integer refInt : refInts) {
				// add all as new ones
				T011ObjGeoSpatialRep ref = new T011ObjGeoSpatialRep();
				ref.setObjGeoId(in.getId());
				ref.setType(refInt);
				ref.setLine(line);
				refs.add(ref);
				line++;
			}
		}
	}	
	

	private void updateT011ObjLiterature(IngridDocument oDocIn, T01Object oIn) {
		Set<T011ObjLiterature> refs = oIn.getT011ObjLiteratures();
		ArrayList<T011ObjLiterature> refs_unprocessed = new ArrayList<T011ObjLiterature>(refs);
		// remove all !
		for (T011ObjLiterature ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			daoT011ObjLiterature.makeTransient(ref);			
		}
		
		IngridDocument refDoc = (IngridDocument)oDocIn.get(MdekKeys.TECHNICAL_DOMAIN_DOCUMENT);
		if (refDoc != null) {
			// and the new one, should be only one, because of the 1:1 relation of tables 
			T011ObjLiterature ref = new T011ObjLiterature();
			ref.setObjId(oIn.getId());
			ref.setAuthor(refDoc.getString(MdekKeys.AUTHOR));
			ref.setBase(refDoc.getString(MdekKeys.SOURCE));
			ref.setDescription(refDoc.getString(MdekKeys.DESCRIPTION_OF_TECH_DOMAIN));
			ref.setDocInfo(refDoc.getString(MdekKeys.ADDITIONAL_BIBLIOGRAPHIC_INFO));
			ref.setIsbn(refDoc.getString(MdekKeys.ISBN));
			ref.setLoc(refDoc.getString(MdekKeys.LOCATION));
			ref.setPublisher(refDoc.getString(MdekKeys.EDITOR));
			ref.setPublishIn(refDoc.getString(MdekKeys.PUBLISHED_IN));
			ref.setPublishing(refDoc.getString(MdekKeys.PUBLISHER));
			ref.setPublishLoc(refDoc.getString(MdekKeys.PUBLISHING_PLACE));
			ref.setPublishYear(refDoc.getString(MdekKeys.YEAR));
			ref.setSides(refDoc.getString(MdekKeys.PAGES));
			ref.setTypeValue(refDoc.getString(MdekKeys.TYPE_OF_DOCUMENT));
			ref.setTypeKey((Integer)refDoc.get(MdekKeys.TYPE_OF_DOCUMENT_KEY));
			ref.setVolume(refDoc.getString(MdekKeys.VOLUME));
			keyValueService.processKeyValue(ref);
			refs.add(ref);
		}
		
	}	
	
	private T015Legist mapT015Legist(T01Object oFrom,
			IngridDocument refDoc,
			T015Legist ref, 
			int line)
	{
		ref.setObjId(oFrom.getId());
		ref.setLegistValue(refDoc.getString(MdekKeys.LEGISLATION_VALUE));
		ref.setLegistKey((Integer)refDoc.get(MdekKeys.LEGISLATION_KEY));
		ref.setLine(line);
		keyValueService.processKeyValue(ref);

		return ref;
	}
	private void updateT015Legists(IngridDocument oDocIn, T01Object oIn) {
		List<IngridDocument> refDocs = (List) oDocIn.get(MdekKeys.LEGISLATIONS);
		if (refDocs == null) {
			refDocs = new ArrayList<IngridDocument>(0);
		}
		Set<T015Legist> refs = oIn.getT015Legists();
		ArrayList<T015Legist> refs_unprocessed = new ArrayList<T015Legist>(refs);
		// remove all !
		for (T015Legist ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			daoT015Legist.makeTransient(ref);			
		}		
		// and add all new ones !
		int line = 1;
		for (IngridDocument refDoc : refDocs) {
			// add all as new ones
			T015Legist ref = mapT015Legist(oIn, refDoc, new T015Legist(), line);
			refs.add(ref);
			line++;
		}
	}

	private T0110AvailFormat mapT0110AvailFormat(T01Object oFrom,
			IngridDocument refDoc,
			T0110AvailFormat ref, 
			int line) 
	{
		ref.setObjId(oFrom.getId());
		ref.setFormatValue((String) refDoc.get(MdekKeys.FORMAT_NAME));
		ref.setFormatKey((Integer) refDoc.get(MdekKeys.FORMAT_NAME_KEY));
		ref.setVer((String) refDoc.get(MdekKeys.FORMAT_VERSION));
		ref.setSpecification((String) refDoc.get(MdekKeys.FORMAT_SPECIFICATION));
		ref.setFileDecompressionTechnique((String) refDoc.get(MdekKeys.FORMAT_FILE_DECOMPRESSION_TECHNIQUE));
		ref.setLine(line);
		keyValueService.processKeyValue(ref);

		return ref;
	}
	private void updateT0110AvailFormats(IngridDocument oDocIn, T01Object oIn) {
		List<IngridDocument> refDocs = (List) oDocIn.get(MdekKeys.DATA_FORMATS);
		if (refDocs == null) {
			refDocs = new ArrayList<IngridDocument>(0);
		}
		Set<T0110AvailFormat> refs = oIn.getT0110AvailFormats();
		ArrayList<T0110AvailFormat> refs_unprocessed = new ArrayList<T0110AvailFormat>(refs);
		// remove all !
		for (T0110AvailFormat ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			daoT0110AvailFormat.makeTransient(ref);			
		}		
		// and add all new ones !
		int line = 1;
		for (IngridDocument refDoc : refDocs) {
			// add all as new ones
			T0110AvailFormat ref = mapT0110AvailFormat(oIn, refDoc, new T0110AvailFormat(), line);
			refs.add(ref);
			line++;
		}
	}

	private T0112MediaOption mapT0112MediaOption(T01Object oFrom,
			IngridDocument refDoc,
			T0112MediaOption ref, 
			int line) 
	{
		ref.setObjId(oFrom.getId());
		ref.setMediumName((Integer) refDoc.get(MdekKeys.MEDIUM_NAME));
		ref.setTransferSize((Double) refDoc.get(MdekKeys.MEDIUM_TRANSFER_SIZE));
		ref.setMediumNote((String) refDoc.get(MdekKeys.MEDIUM_NOTE));
		ref.setLine(line);

		return ref;
	}
	private void updateT0112MediaOptions(IngridDocument oDocIn, T01Object oIn) {
		List<IngridDocument> refDocs = (List) oDocIn.get(MdekKeys.MEDIUM_OPTIONS);
		if (refDocs == null) {
			refDocs = new ArrayList<IngridDocument>(0);
		}
		Set<T0112MediaOption> refs = oIn.getT0112MediaOptions();
		ArrayList<T0112MediaOption> refs_unprocessed = new ArrayList<T0112MediaOption>(refs);
		// remove all !
		for (T0112MediaOption ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			daoT0112MediaOption.makeTransient(ref);			
		}		
		// and add all new ones !
		int line = 1;
		for (IngridDocument refDoc : refDocs) {
			// add all as new ones
			T0112MediaOption ref = mapT0112MediaOption(oIn, refDoc, new T0112MediaOption(), line);
			refs.add(ref);
			line++;
		}
	}

	private SearchtermObj mapSearchtermObj(T01Object oFrom,
		SearchtermValue refValue,
		SearchtermObj ref,
		int line) 
	{
		ref.setObjId(oFrom.getId());
		ref.setSearchtermValue(refValue);			
		ref.setSearchtermId(refValue.getId());
		ref.setLine(line);

		return ref;
	}
	private SearchtermAdr mapSearchtermAdr(T02Address aFrom,
		SearchtermValue refValue,
		SearchtermAdr ref,
		int line)
	{
		ref.setAdrId(aFrom.getId());
		ref.setSearchtermValue(refValue);			
		ref.setSearchtermId(refValue.getId());
		ref.setLine(line);

		return ref;
	}

	private SearchtermValue mapSearchtermValue(SearchtermSns refSns,
		IngridDocument refDoc,
		SearchtermValue refValue) 
	{
		refValue.setTerm((String) refDoc.get(MdekKeys.TERM_NAME));
		refValue.setType((String) refDoc.get(MdekKeys.TERM_TYPE));

		Long refSnsId = null;
		if (refSns != null) {
			refSnsId = refSns.getId();			
		}
		refValue.setSearchtermSns(refSns);			
		refValue.setSearchtermSnsId(refSnsId);

		return refValue;
	}

	private void updateSearchtermObjs(IngridDocument oDocIn, T01Object oIn) {
		List<IngridDocument> refDocs = (List) oDocIn.get(MdekKeys.SUBJECT_TERMS);
		if (refDocs == null) {
			refDocs = new ArrayList<IngridDocument>(0);
		}
		Set<SearchtermObj> refs = oIn.getSearchtermObjs();
		ArrayList<SearchtermObj> refs_unprocessed = new ArrayList<SearchtermObj>(refs);
		int line = 1;
		for (IngridDocument refDoc : refDocs) {
			String refDocName = (String) refDoc.get(MdekKeys.TERM_NAME);
			String refDocName_notNull = (refDocName == null) ? "" : refDocName;
			String refDocType = (String) refDoc.get(MdekKeys.TERM_TYPE);
			String refDocSnsId = (String) refDoc.get(MdekKeys.TERM_SNS_ID);
			String refDocSnsId_notNull = (refDocSnsId == null) ? "" : refDocSnsId;
			boolean found = false;
			for (SearchtermObj ref : refs) {
				SearchtermValue refValue = ref.getSearchtermValue();
				if (refValue != null) {
					SearchtermSns refSns = refValue.getSearchtermSns();

					String refName_notNull = (refValue.getTerm() == null) ? "" : refValue.getTerm();
					String refType = refValue.getType();
					String refSnsId_notNull = (refSns == null) ? "" : refSns.getSnsId();
					if (refDocName_notNull.equals(refName_notNull) &&
						refDocType.equals(refType) &&
						refDocSnsId_notNull.equals(refSnsId_notNull))
					{
						mapSearchtermValue(refSns, refDoc, refValue);
						// update line
						ref.setLine(line);
						refs_unprocessed.remove(ref);
						found = true;
						break;
					}					
				}
			}
			if (!found) {
				// add new one
				
				// first load/create SpatialRefSns
				SearchtermSns refSns = null;
				if (refDocSnsId != null) {
					refSns = daoSearchtermSns.loadOrCreate(refDocSnsId);
				}

				// then load/create SpatialRefValue
				SearchtermValue refValue = daoSearchtermValue.loadOrCreate(refDocType, refDocName, refSns,
					oIn.getId(), IdcEntityType.OBJECT);
				mapSearchtermValue(refSns, refDoc, refValue);

				// then create SpatialReference
				SearchtermObj ref = new SearchtermObj();
				mapSearchtermObj(oIn, refValue, ref, line);
				refs.add(ref);
			}
			line++;
		}
		// remove the ones not processed, will be deleted by hibernate (delete-orphan set in parent)
		for (SearchtermObj ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			daoSearchtermObj.makeTransient(ref);
		}		
	}

	private void updateSearchtermAdrs(IngridDocument aDocIn, T02Address aIn) {
		List<IngridDocument> refDocs = (List) aDocIn.get(MdekKeys.SUBJECT_TERMS);
		if (refDocs == null) {
			refDocs = new ArrayList<IngridDocument>(0);
		}
		Set<SearchtermAdr> refs = aIn.getSearchtermAdrs();
		ArrayList<SearchtermAdr> refs_unprocessed = new ArrayList<SearchtermAdr>(refs);
		int line = 1;
		for (IngridDocument refDoc : refDocs) {
			String refDocName = (String) refDoc.get(MdekKeys.TERM_NAME);
			String refDocName_notNull = (refDocName == null) ? "" : refDocName;
			String refDocType = (String) refDoc.get(MdekKeys.TERM_TYPE);
			String refDocSnsId = (String) refDoc.get(MdekKeys.TERM_SNS_ID);
			String refDocSnsId_notNull = (refDocSnsId == null) ? "" : refDocSnsId;
			boolean found = false;
			for (SearchtermAdr ref : refs) {
				SearchtermValue refValue = ref.getSearchtermValue();
				if (refValue != null) {
					SearchtermSns refSns = refValue.getSearchtermSns();

					String refName_notNull = (refValue.getTerm() == null) ? "" : refValue.getTerm();
					String refType = refValue.getType();
					String refSnsId_notNull = (refSns == null) ? "" : refSns.getSnsId();
					if (refDocName_notNull.equals(refName_notNull) &&
						refDocType.equals(refType) &&
						refDocSnsId_notNull.equals(refSnsId_notNull))
					{
						mapSearchtermValue(refSns, refDoc, refValue);
						// update line
						ref.setLine(line);
						refs_unprocessed.remove(ref);
						found = true;
						break;
					}					
				}
			}
			if (!found) {
				// add new one
				
				// first load/create SpatialRefSns
				SearchtermSns refSns = null;
				if (refDocSnsId != null) {
					refSns = daoSearchtermSns.loadOrCreate(refDocSnsId);
				}

				// then load/create SpatialRefValue
				SearchtermValue refValue =
					daoSearchtermValue.loadOrCreate(refDocType, refDocName, refSns, aIn.getId(), IdcEntityType.ADDRESS);
				mapSearchtermValue(refSns, refDoc, refValue);

				// then create SpatialReference
				SearchtermAdr ref = new SearchtermAdr();
				mapSearchtermAdr(aIn, refValue, ref, line);
				refs.add(ref);
			}
			line++;
		}
		// remove the ones not processed, will be deleted by hibernate (delete-orphan set in parent)
		for (SearchtermAdr ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			daoSearchtermAdr.makeTransient(ref);
		}		
	}

	private T0114EnvCategory mapT0114EnvCategory(T01Object oFrom,
			Integer refValue,
			T0114EnvCategory ref,
			int line)
	{
		ref.setObjId(oFrom.getId());
		ref.setCatKey(refValue);
		ref.setLine(line);

		return ref;
	}
	private void updateT0114EnvCategorys(IngridDocument oDocIn, T01Object oIn) {
		List<Integer> refValues = (List) oDocIn.get(MdekKeys.ENV_CATEGORIES);
		if (refValues == null) {
			refValues = new ArrayList<Integer>(0);
		}
		Set<T0114EnvCategory> refs = oIn.getT0114EnvCategorys();
		ArrayList<T0114EnvCategory> refs_unprocessed = new ArrayList<T0114EnvCategory>(refs);
		// remove all !
		for (T0114EnvCategory ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			daoT0114EnvCategory.makeTransient(ref);			
		}		
		// and add all new ones !
		int line = 1;
		for (Integer refVal : refValues) {
			T0114EnvCategory ref = mapT0114EnvCategory(oIn, refVal, new T0114EnvCategory(), line);
			refs.add(ref);
			line++;
		}
	}

	private T0114EnvTopic mapT0114EnvTopic(T01Object oFrom,
			Integer refValue,
			T0114EnvTopic ref,
			int line)
	{
		ref.setObjId(oFrom.getId());
		ref.setTopicKey(refValue);
		ref.setLine(line);

		return ref;
	}
	private void updateT0114EnvTopics(IngridDocument oDocIn, T01Object oIn) {
		List<Integer> refValues = (List) oDocIn.get(MdekKeys.ENV_TOPICS);
		if (refValues == null) {
			refValues = new ArrayList<Integer>(0);
		}
		Set<T0114EnvTopic> refs = oIn.getT0114EnvTopics();
		ArrayList<T0114EnvTopic> refs_unprocessed = new ArrayList<T0114EnvTopic>(refs);
		// remove all !
		for (T0114EnvTopic ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			daoT0114EnvTopic.makeTransient(ref);			
		}		
		// and add all new ones !
		int line = 1;
		for (Integer refValue : refValues) {
			T0114EnvTopic ref = mapT0114EnvTopic(oIn, refValue, new T0114EnvTopic(), line);
			refs.add(ref);
			line++;
		}
	}

	private T011ObjTopicCat mapT011ObjTopicCat(T01Object oFrom,
			Integer category,
			T011ObjTopicCat ref,
			int line)
	{
		ref.setObjId(oFrom.getId());
		ref.setTopicCategory(category);
		ref.setLine(line);

		return ref;
	}
	private void updateT011ObjTopicCats(IngridDocument oDocIn, T01Object oIn) {
		List<Integer> refCats = (List) oDocIn.get(MdekKeys.TOPIC_CATEGORIES);
		if (refCats == null) {
			refCats = new ArrayList<Integer>(0);
		}
		Set<T011ObjTopicCat> refs = oIn.getT011ObjTopicCats();
		ArrayList<T011ObjTopicCat> refs_unprocessed = new ArrayList<T011ObjTopicCat>(refs);
		// remove all !
		for (T011ObjTopicCat ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			daoT011ObjTopicCat.makeTransient(ref);			
		}		
		// and add all new ones !
		int line = 1;
		for (Integer refCat : refCats) {
			T011ObjTopicCat ref = mapT011ObjTopicCat(oIn, refCat, new T011ObjTopicCat(), line);
			refs.add(ref);
			line++;
		}
	}

	private T011ObjData mapT011ObjData(T01Object oFrom,
			IngridDocument refDoc,
			T011ObjData ref) 
	{
		ref.setObjId(oFrom.getId());
		ref.setBase(refDoc.getString(MdekKeys.METHOD));
		ref.setDescription(refDoc.getString(MdekKeys.DESCRIPTION_OF_TECH_DOMAIN));

		return ref;
	}
	private void updateT011ObjData(IngridDocument oDocIn, T01Object oIn) {
		Set<T011ObjData> refs = oIn.getT011ObjDatas();
		ArrayList<T011ObjData> refs_unprocessed = new ArrayList<T011ObjData>(refs);
		// remove all !
		for (T011ObjData ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			daoT011ObjData.makeTransient(ref);			
		}		
		// and add new one !
		IngridDocument domainDoc = (IngridDocument) oDocIn.get(MdekKeys.TECHNICAL_DOMAIN_DATASET);
		if (domainDoc != null) {
			T011ObjData ref = mapT011ObjData(oIn, domainDoc, new T011ObjData());
			refs.add(ref);
		}

		updateT011ObjDataParas(oDocIn, oIn);
	}

	private T011ObjDataPara mapT011ObjDataPara(T01Object oFrom,
			IngridDocument refDoc,
			T011ObjDataPara ref,
			int line)
	{
		ref.setObjId(oFrom.getId());
		ref.setParameter(refDoc.getString(MdekKeys.PARAMETER));
		ref.setUnit(refDoc.getString(MdekKeys.SUPPLEMENTARY_INFORMATION));
		ref.setLine(line);

		return ref;
	}
	private void updateT011ObjDataParas(IngridDocument oDocIn, T01Object oIn) {
		Set<T011ObjDataPara> refs = oIn.getT011ObjDataParas();
		ArrayList<T011ObjDataPara> refs_unprocessed = new ArrayList<T011ObjDataPara>(refs);
		// remove all !
		for (T011ObjDataPara ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			daoT011ObjDataPara.makeTransient(ref);			
		}

		// and add new ones !
		IngridDocument domainDoc = (IngridDocument) oDocIn.get(MdekKeys.TECHNICAL_DOMAIN_DATASET);
		if (domainDoc != null) {
			List<IngridDocument> refDocs = (List) domainDoc.get(MdekKeys.PARAMETERS);
			if (refDocs == null) {
				refDocs = new ArrayList<IngridDocument>(0);
			}
			// and add all new ones !
			int line = 1;
			for (IngridDocument refDoc : refDocs) {
				T011ObjDataPara ref = mapT011ObjDataPara(oIn, refDoc, new T011ObjDataPara(), line);
				refs.add(ref);
				line++;
			}
		}
	}

	private T011ObjProject mapT011ObjProject(T01Object oFrom,
			IngridDocument refDoc,
			T011ObjProject ref) 
	{
		ref.setObjId(oFrom.getId());
		ref.setLeader(refDoc.getString(MdekKeys.LEADER_DESCRIPTION));
		ref.setMember(refDoc.getString(MdekKeys.MEMBER_DESCRIPTION));
		ref.setDescription(refDoc.getString(MdekKeys.DESCRIPTION_OF_TECH_DOMAIN));

		return ref;
	}
	private void updateT011ObjProject(IngridDocument oDocIn, T01Object oIn) {
		Set<T011ObjProject> refs = oIn.getT011ObjProjects();
		ArrayList<T011ObjProject> refs_unprocessed = new ArrayList<T011ObjProject>(refs);
		// remove all !
		for (T011ObjProject ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			daoT011ObjProject.makeTransient(ref);			
		}		
		// and add new one !
		IngridDocument domainDoc = (IngridDocument) oDocIn.get(MdekKeys.TECHNICAL_DOMAIN_PROJECT);
		if (domainDoc != null) {
			T011ObjProject ref = mapT011ObjProject(oIn, domainDoc, new T011ObjProject());
			refs.add(ref);
		}
	}

	private void updateT011ObjServ(IngridDocument oDocIn, T01Object oIn) {
		Set<T011ObjServ> refs = oIn.getT011ObjServs();
		ArrayList<T011ObjServ> refs_unprocessed = new ArrayList<T011ObjServ>(refs);
		// remove all !
		for (T011ObjServ ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			daoT011ObjServ.makeTransient(ref);			
		}		
		// and add new one !
		IngridDocument domainDoc = (IngridDocument) oDocIn.get(MdekKeys.TECHNICAL_DOMAIN_SERVICE);
		if (domainDoc != null) {
			T011ObjServ ref = mapT011ObjServ(oIn, domainDoc, new T011ObjServ());

			// save the object and get ID from database (cascading insert do not work??)
			daoT011ObjServ.makePersistent(ref);

			// map 1:N relations
			updateT011ObjServVersions(domainDoc, ref);
			updateT011ObjServOperations(domainDoc, ref);

			refs.add(ref);
		}
	}
	private T011ObjServ mapT011ObjServ(T01Object oFrom,
			IngridDocument refDoc,
			T011ObjServ ref) 
	{
		ref.setObjId(oFrom.getId());
		ref.setTypeValue(refDoc.getString(MdekKeys.SERVICE_TYPE));
		ref.setTypeKey((Integer)refDoc.get(MdekKeys.SERVICE_TYPE_KEY));
		ref.setHistory(refDoc.getString(MdekKeys.SYSTEM_HISTORY));
		ref.setEnvironment(refDoc.getString(MdekKeys.SYSTEM_ENVIRONMENT));
		ref.setBase(refDoc.getString(MdekKeys.DATABASE_OF_SYSTEM));
		ref.setDescription(refDoc.getString(MdekKeys.DESCRIPTION_OF_TECH_DOMAIN));
		keyValueService.processKeyValue(ref);

		return ref;
	}
	private void updateT011ObjServVersions(IngridDocument oDocIn, T011ObjServ oIn) {
		List<String> versions = (List) oDocIn.get(MdekKeys.SERVICE_VERSION_LIST);
		if (versions == null) {
			versions = new ArrayList<String>(0);
		}
		Set<T011ObjServVersion> refs = oIn.getT011ObjServVersions();
		ArrayList<T011ObjServVersion> refs_unprocessed = new ArrayList<T011ObjServVersion>(refs);
		// remove all !
		for (T011ObjServVersion ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			daoT011ObjServVersion.makeTransient(ref);			
		}		
		// and add all new ones !
		int line = 1;
		for (String version : versions) {
			T011ObjServVersion ref = mapT011ObjServVersion(oIn, version, new T011ObjServVersion(), line);
			refs.add(ref);
			line++;
		}
	}
	private T011ObjServVersion mapT011ObjServVersion(T011ObjServ oFrom,
			String version,
			T011ObjServVersion ref,
			int line)
	{
		ref.setObjServId(oFrom.getId());
		ref.setServVersion(version);
		ref.setLine(line);

		return ref;
	}
	private void updateT011ObjServOperations(IngridDocument oDocIn, T011ObjServ oIn) {
		Set<T011ObjServOperation> refs = oIn.getT011ObjServOperations();
		ArrayList<T011ObjServOperation> refs_unprocessed = new ArrayList<T011ObjServOperation>(refs);
		// remove all !
		for (T011ObjServOperation ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			daoT011ObjServOperation.makeTransient(ref);			
		}

		// and add new ones !
		List<IngridDocument> refDocs = (List) oDocIn.get(MdekKeys.SERVICE_OPERATION_LIST);
		if (refDocs == null) {
			refDocs = new ArrayList<IngridDocument>(0);
		}
		// and add all new ones !
		int line = 1;
		for (IngridDocument refDoc : refDocs) {
			T011ObjServOperation ref = mapT011ObjServOperation(oIn, refDoc, new T011ObjServOperation(), line);
			
			// save the object and get ID from database (cascading insert do not work??)
			daoT011ObjServOperation.makePersistent(ref);

			// map 1:N relations
			updateT011ObjServOpPlatforms(refDoc, ref);
			updateT011ObjServOpDependss(refDoc, ref);
			updateT011ObjServOpConnpoints(refDoc, ref);
			updateT011ObjServOpParas(refDoc, ref);

			refs.add(ref);
			line++;
		}
	}
	private T011ObjServOperation mapT011ObjServOperation(T011ObjServ oFrom,
			IngridDocument refDoc,
			T011ObjServOperation ref,
			int line)
	{
		ref.setObjServId(oFrom.getId());
		ref.setNameValue(refDoc.getString(MdekKeys.SERVICE_OPERATION_NAME));
		ref.setNameKey((Integer)refDoc.get(MdekKeys.SERVICE_OPERATION_NAME_KEY));
		ref.setDescr(refDoc.getString(MdekKeys.SERVICE_OPERATION_DESCRIPTION));
		ref.setInvocationName(refDoc.getString(MdekKeys.INVOCATION_NAME));
		ref.setLine(line);
		keyValueService.processKeyValueT011ObjServOperation(ref, oFrom);

		return ref;
	}
	private void updateT011ObjServOpPlatforms(IngridDocument oDocIn, T011ObjServOperation oIn) {
		List<String> platforms = (List) oDocIn.get(MdekKeys.PLATFORM_LIST);
		if (platforms == null) {
			platforms = new ArrayList<String>(0);
		}
		Set<T011ObjServOpPlatform> refs = oIn.getT011ObjServOpPlatforms();
		ArrayList<T011ObjServOpPlatform> refs_unprocessed = new ArrayList<T011ObjServOpPlatform>(refs);
		// remove all !
		for (T011ObjServOpPlatform ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			daoT011ObjServOpPlatform.makeTransient(ref);			
		}		
		// and add all new ones !
		int line = 1;
		for (String platform : platforms) {
			T011ObjServOpPlatform ref = mapT011ObjServOpPlatform(oIn, platform, new T011ObjServOpPlatform(), line);
			refs.add(ref);
			line++;
		}
	}
	private T011ObjServOpPlatform mapT011ObjServOpPlatform(T011ObjServOperation oFrom,
			String platform,
			T011ObjServOpPlatform ref,
			int line)
	{
		ref.setObjServOpId(oFrom.getId());
		ref.setPlatform(platform);
		ref.setLine(line);

		return ref;
	}
	private void updateT011ObjServOpDependss(IngridDocument oDocIn, T011ObjServOperation oIn) {
		List<String> dependsOns = (List) oDocIn.get(MdekKeys.DEPENDS_ON_LIST);
		if (dependsOns == null) {
			dependsOns = new ArrayList<String>(0);
		}
		Set<T011ObjServOpDepends> refs = oIn.getT011ObjServOpDependss();
		ArrayList<T011ObjServOpDepends> refs_unprocessed = new ArrayList<T011ObjServOpDepends>(refs);
		// remove all !
		for (T011ObjServOpDepends ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			daoT011ObjServOpDepends.makeTransient(ref);			
		}		
		// and add all new ones !
		int line = 1;
		for (String dependsOn : dependsOns) {
			T011ObjServOpDepends ref = mapT011ObjServOpDepends(oIn, dependsOn, new T011ObjServOpDepends(), line);
			refs.add(ref);
			line++;
		}
	}
	private T011ObjServOpDepends mapT011ObjServOpDepends(T011ObjServOperation oFrom,
			String dependsOn,
			T011ObjServOpDepends ref,
			int line)
	{
		ref.setObjServOpId(oFrom.getId());
		ref.setDependsOn(dependsOn);
		ref.setLine(line);

		return ref;
	}
	private void updateT011ObjServOpConnpoints(IngridDocument oDocIn, T011ObjServOperation oIn) {
		List<String> connectPoints = (List) oDocIn.get(MdekKeys.CONNECT_POINT_LIST);
		if (connectPoints == null) {
			connectPoints = new ArrayList<String>(0);
		}
		Set<T011ObjServOpConnpoint> refs = oIn.getT011ObjServOpConnpoints();
		ArrayList<T011ObjServOpConnpoint> refs_unprocessed = new ArrayList<T011ObjServOpConnpoint>(refs);
		// remove all !
		for (T011ObjServOpConnpoint ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			daoT011ObjServOpConnpoint.makeTransient(ref);			
		}		
		// and add all new ones !
		int line = 1;
		for (String connectPoint : connectPoints) {
			T011ObjServOpConnpoint ref = mapT011ObjServOpConnpoint(oIn, connectPoint, new T011ObjServOpConnpoint(), line);
			refs.add(ref);
			line++;
		}
	}
	private T011ObjServOpConnpoint mapT011ObjServOpConnpoint(T011ObjServOperation oFrom,
			String connectPoint,
			T011ObjServOpConnpoint ref,
			int line)
	{
		ref.setObjServOpId(oFrom.getId());
		ref.setConnectPoint(connectPoint);
		ref.setLine(line);

		return ref;
	}
	private void updateT011ObjServOpParas(IngridDocument oDocIn, T011ObjServOperation oIn) {
		Set<T011ObjServOpPara> refs = oIn.getT011ObjServOpParas();
		ArrayList<T011ObjServOpPara> refs_unprocessed = new ArrayList<T011ObjServOpPara>(refs);
		// remove all !
		for (T011ObjServOpPara ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			daoT011ObjServOpPara.makeTransient(ref);			
		}

		// and add new ones !
		List<IngridDocument> refDocs = (List) oDocIn.get(MdekKeys.PARAMETER_LIST);
		if (refDocs == null) {
			refDocs = new ArrayList<IngridDocument>(0);
		}
		// and add all new ones !
		int line = 1;
		for (IngridDocument refDoc : refDocs) {
			T011ObjServOpPara ref = mapT011ObjServOpPara(oIn, refDoc, new T011ObjServOpPara(), line);
			refs.add(ref);
			line++;
		}
	}
	private T011ObjServOpPara mapT011ObjServOpPara(T011ObjServOperation oFrom,
			IngridDocument refDoc,
			T011ObjServOpPara ref,
			int line)
	{
		ref.setObjServOpId(oFrom.getId());
		ref.setName(refDoc.getString(MdekKeys.PARAMETER_NAME));
		ref.setDirection(refDoc.getString(MdekKeys.DIRECTION));
		ref.setDescr(refDoc.getString(MdekKeys.DESCRIPTION));
		ref.setOptional((Integer) refDoc.get(MdekKeys.OPTIONALITY));
		ref.setRepeatability((Integer) refDoc.get(MdekKeys.REPEATABILITY));
		ref.setLine(line);

		return ref;
	}
}