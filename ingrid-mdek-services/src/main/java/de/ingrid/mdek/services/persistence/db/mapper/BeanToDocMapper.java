package de.ingrid.mdek.services.persistence.db.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.dao.IAddressNodeDao;
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
import de.ingrid.mdek.services.persistence.db.model.SysList;
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
import de.ingrid.mdek.services.persistence.db.model.T08Attr;
import de.ingrid.mdek.services.persistence.db.model.T08AttrType;
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

		if (howMuch == MappingQuantity.TREE_ENTITY ||
			howMuch == MappingQuantity.COPY_ENTITY) {
			// child info
	    	boolean hasChild = (oNIn.getObjectNodeChildren().size() > 0) ? true : false;
			objectDoc.putBoolean(MdekKeys.HAS_CHILD, hasChild);			
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

		if (howMuch == MappingQuantity.TREE_ENTITY ||
			howMuch == MappingQuantity.COPY_ENTITY) {
			// child info
	    	boolean hasChild = (aNIn.getAddressNodeChildren().size() > 0) ? true : false;
			addressDoc.putBoolean(MdekKeys.HAS_CHILD, hasChild);			
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

			return objectDoc;
		}

		// also ID, just to track ID in test suite !
		objectDoc.put(MdekKeys.ID, o.getId());
		objectDoc.put(MdekKeys.UUID, o.getObjUuid());
		objectDoc.put(MdekKeys.CLASS, o.getObjClass());
		objectDoc.put(MdekKeys.TITLE, o.getObjName());
		objectDoc.put(MdekKeys.WORK_STATE, o.getWorkState());
		// needed when copying objects ! we always map to track in test suite !
		objectDoc.put(MdekKeys.CATALOGUE_IDENTIFIER, o.getCatId());

		if (howMuch == MappingQuantity.DETAIL_ENTITY ||
			howMuch == MappingQuantity.COPY_ENTITY) 
		{
			objectDoc.put(MdekKeys.DATASET_ALTERNATE_NAME, o.getDatasetAlternateName());
			objectDoc.put(MdekKeys.ABSTRACT, o.getObjDescr());
			objectDoc.put(MdekKeys.DATE_OF_CREATION, o.getCreateTime());
			objectDoc.put(MdekKeys.DATE_OF_LAST_MODIFICATION, o.getModTime());

			objectDoc.put(MdekKeys.VERTICAL_EXTENT_MINIMUM, o.getVerticalExtentMinimum());
			objectDoc.put(MdekKeys.VERTICAL_EXTENT_MAXIMUM, o.getVerticalExtentMaximum());
			objectDoc.put(MdekKeys.VERTICAL_EXTENT_UNIT, o.getVerticalExtentUnit());
			objectDoc.put(MdekKeys.VERTICAL_EXTENT_VDATUM, o.getVerticalExtentVdatum());
			objectDoc.put(MdekKeys.DESCRIPTION_OF_SPATIAL_DOMAIN, o.getLocDescr());

			objectDoc.put(MdekKeys.TIME_TYPE, o.getTimeType());
			objectDoc.put(MdekKeys.BEGINNING_DATE, o.getTimeFrom());
			objectDoc.put(MdekKeys.ENDING_DATE, o.getTimeTo());
			objectDoc.put(MdekKeys.TIME_STATUS, o.getTimeStatus());
			objectDoc.put(MdekKeys.TIME_PERIOD, o.getTimePeriod());
			objectDoc.put(MdekKeys.TIME_STEP, o.getTimeInterval());
			objectDoc.put(MdekKeys.TIME_SCALE, o.getTimeAlle());
			objectDoc.put(MdekKeys.DESCRIPTION_OF_TEMPORAL_DOMAIN, o.getTimeDescr());

			objectDoc.put(MdekKeys.METADATA_LANGUAGE, o.getMetadataLanguageCode());
			objectDoc.put(MdekKeys.DATA_LANGUAGE, o.getDataLanguageCode());
			objectDoc.put(MdekKeys.PUBLICATION_CONDITION, o.getPublishId());
			objectDoc.put(MdekKeys.DATASET_INTENSIONS, o.getInfoNote());
			objectDoc.put(MdekKeys.DATASET_USAGE, o.getDatasetUsage());

			objectDoc.put(MdekKeys.ORDERING_INSTRUCTIONS, o.getOrderingInstructions());
			objectDoc.put(MdekKeys.USE_CONSTRAINTS, o.getAvailAccessNote());
			objectDoc.put(MdekKeys.FEES, o.getFees());
			objectDoc.put(MdekKeys.IS_CATALOG_DATA, o.getIsCatalogData());

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
			mapT0114EnvCategorys(o.getT0114EnvCategorys(), objectDoc);
			mapT0114EnvTopics(o.getT0114EnvTopics(), objectDoc);
			mapT011ObjTopicCats(o.getT011ObjTopicCats(), objectDoc);

			// technical domain map
			mapT011ObjGeo(o.getT011ObjGeos(), objectDoc);
			// technical domain document
			mapT011ObjLiterature(o.getT011ObjLiteratures(), objectDoc);
			// technical domain service
			mapT011ObjServ(o.getT011ObjServs(), objectDoc);
			// technical domain project
			mapT011ObjProject(o.getT011ObjProjects(), objectDoc);
			// technical domain dataset
			mapT011ObjData(o, objectDoc);

			// object comments
			mapObjectComments(o.getObjectComments(), objectDoc);
			// additional fields
			mapT08Attrs(o.getT08Attrs(), objectDoc);

			// map only with initial data ! call mapping method explicitly if more data wanted.
			mapModUser(o.getModUuid(), objectDoc, MappingQuantity.INITIAL_ENTITY);
			mapResponsibleUser(o.getResponsibleUuid(), objectDoc, MappingQuantity.INITIAL_ENTITY);
		}

		if (howMuch == MappingQuantity.COPY_ENTITY) {
			objectDoc.put(MdekKeys.ORIGINAL_CONTROL_IDENTIFIER, o.getOrgObjId());
			objectDoc.put(MdekKeys.DATASET_CHARACTER_SET, o.getDatasetCharacterSet());
			objectDoc.put(MdekKeys.METADATA_CHARACTER_SET, o.getMetadataCharacterSet());
			objectDoc.put(MdekKeys.METADATA_STANDARD_NAME, o.getMetadataStandardName());
			objectDoc.put(MdekKeys.METADATA_STANDARD_VERSION, o.getMetadataStandardVersion());
			objectDoc.put(MdekKeys.LASTEXPORT_TIME, o.getLastexportTime());
			objectDoc.put(MdekKeys.EXPIRY_TIME, o.getExpiryTime());
			objectDoc.put(MdekKeys.WORK_VERSION, o.getWorkVersion());
			objectDoc.put(MdekKeys.MARK_DELETED, o.getMarkDeleted());
		}

		return objectDoc;
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
			addressDoc.put(MdekKeys.STREET, a.getStreet());
			addressDoc.put(MdekKeys.POSTAL_CODE_OF_COUNTRY, a.getCountryCode());
			addressDoc.put(MdekKeys.POSTAL_CODE, a.getPostcode());
			addressDoc.put(MdekKeys.CITY, a.getCity());
			addressDoc.put(MdekKeys.POST_BOX_POSTAL_CODE, a.getPostboxPc());
			addressDoc.put(MdekKeys.POST_BOX, a.getPostbox());

			// map associations
			mapT021Communications(a.getT021Communications(), addressDoc);
			mapSearchtermAdrs(a.getSearchtermAdrs(), addressDoc, howMuch);

			return addressDoc;
		}

		// also ID, just to track ID in test suite !
		addressDoc.put(MdekKeys.ID, a.getId());
		addressDoc.put(MdekKeys.UUID, a.getAdrUuid());
		addressDoc.put(MdekKeys.CLASS, a.getAdrType());
		addressDoc.put(MdekKeys.ORGANISATION, a.getInstitution());
		addressDoc.put(MdekKeys.NAME, a.getLastname());
		addressDoc.put(MdekKeys.GIVEN_NAME, a.getFirstname());
		addressDoc.put(MdekKeys.TITLE_OR_FUNCTION, a.getTitleValue());
		addressDoc.put(MdekKeys.TITLE_OR_FUNCTION_KEY, a.getTitleKey());
		addressDoc.put(MdekKeys.WORK_STATE, a.getWorkState());

		if (howMuch == MappingQuantity.TABLE_ENTITY ||
			howMuch == MappingQuantity.DETAIL_ENTITY ||
			howMuch == MappingQuantity.COPY_ENTITY)
		{
			addressDoc.put(MdekKeys.STREET, a.getStreet());
			addressDoc.put(MdekKeys.POSTAL_CODE_OF_COUNTRY, a.getCountryCode());
			addressDoc.put(MdekKeys.POSTAL_CODE, a.getPostcode());
			addressDoc.put(MdekKeys.CITY, a.getCity());
			addressDoc.put(MdekKeys.POST_BOX_POSTAL_CODE, a.getPostboxPc());
			addressDoc.put(MdekKeys.POST_BOX, a.getPostbox());

			// map associations
			mapT021Communications(a.getT021Communications(), addressDoc);
		}

		if (howMuch == MappingQuantity.DETAIL_ENTITY ||
			howMuch == MappingQuantity.COPY_ENTITY)
		{
			addressDoc.put(MdekKeys.DATE_OF_CREATION, a.getCreateTime());
			addressDoc.put(MdekKeys.DATE_OF_LAST_MODIFICATION, a.getModTime());

			addressDoc.put(MdekKeys.FUNCTION, a.getJob());			
			addressDoc.put(MdekKeys.NAME_FORM, a.getAddressValue());
			addressDoc.put(MdekKeys.NAME_FORM_KEY, a.getAddressKey());
			addressDoc.put(MdekKeys.ADDRESS_DESCRIPTION, a.getDescr());			

			// map associations
			mapSearchtermAdrs(a.getSearchtermAdrs(), addressDoc, howMuch);
			mapAddressComments(a.getAddressComments(), addressDoc);

			// map only with initial data ! call mapping method explicitly if more data wanted.
			mapModUser(a.getModUuid(), addressDoc, MappingQuantity.INITIAL_ENTITY);
			mapResponsibleUser(a.getResponsibleUuid(), addressDoc, MappingQuantity.INITIAL_ENTITY);
		}

		if (howMuch == MappingQuantity.COPY_ENTITY) {
			addressDoc.put(MdekKeys.ORIGINAL_ADDRESS_IDENTIFIER, a.getOrgAdrId());
			addressDoc.put(MdekKeys.LASTEXPORT_TIME, a.getLastexportTime());
			addressDoc.put(MdekKeys.EXPIRY_TIME, a.getExpiryTime());
			addressDoc.put(MdekKeys.WORK_VERSION, a.getWorkVersion());
			addressDoc.put(MdekKeys.MARK_DELETED, a.getMarkDeleted());
		}

		return addressDoc;
	}

	/** Set passed user as mod user in passed doc.
	 * Quantity determines how much (only uuid or address data). */
	public IngridDocument mapModUser(String userAddrUuid, IngridDocument inDoc,
			MappingQuantity howMuch) {
		IngridDocument userDoc = new IngridDocument();
		mapUserAddress(userAddrUuid, userDoc, howMuch, true);

		inDoc.put(MdekKeys.MOD_USER, userDoc);

		return inDoc;
	}

	/** Set passed user as responsible user in passed doc.
	 * Quantity determines how much (only uuid or address data). */
	public IngridDocument mapResponsibleUser(String userAddrUuid, IngridDocument inDoc,
			MappingQuantity howMuch) {
		IngridDocument userDoc = new IngridDocument();
		mapUserAddress(userAddrUuid, userDoc, howMuch, true);

		inDoc.put(MdekKeys.RESPONSIBLE_USER, userDoc);

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

		AddressNode aN = daoAddressNode.loadByUuid(userAddrUuid);
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

	private IngridDocument mapObjectComments(Set<ObjectComment> refs, IngridDocument objectDoc) {
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
			IngridDocument userDoc = mapUserAddress(ref.getCreateUuid(), new IngridDocument(), MappingQuantity.DETAIL_ENTITY, false);
			refDoc.put(MdekKeys.CREATE_USER, userDoc);

			docList.add(refDoc);					
		}
		objectDoc.put(MdekKeys.COMMENT_LIST, docList);
		return objectDoc;
	}

	public IngridDocument mapT08Attrs(Set<T08Attr> refs, IngridDocument objectDoc) {
		if (refs == null) {
			return objectDoc;
		}
		ArrayList<IngridDocument> docList = new ArrayList<IngridDocument>(refs.size());
		for (T08Attr ref : refs) {
			IngridDocument refDoc = new IngridDocument();
			refDoc.put(MdekKeys.FIELD_IDENTIFIER, ref.getAttrTypeId());
			refDoc.put(MdekKeys.FIELD_VALUE, ref.getData());
			
			T08AttrType attrType = ref.getT08AttrType();
			if (attrType != null) {
				refDoc.put(MdekKeys.FIELD_NAME, attrType.getName());
			}

			docList.add(refDoc);					
		}
		objectDoc.put(MdekKeys.ADDITIONAL_FIELDS, docList);
		return objectDoc;
	}

	private IngridDocument mapAddressComments(Set<AddressComment> refs, IngridDocument addressDoc) {
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
			IngridDocument userDoc = mapUserAddress(ref.getCreateUuid(), new IngridDocument(), MappingQuantity.DETAIL_ENTITY, false);
			refDoc.put(MdekKeys.CREATE_USER, userDoc);

			docList.add(refDoc);					
		}
		addressDoc.put(MdekKeys.COMMENT_LIST, docList);
		return addressDoc;
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
	 * @param oNodesFrom "from-object references" in an array of lists !
	 * - index 0: list of objects referencing the given uuid in their working version
	 * 		(which might equal the published version)<br>
	 * - index 1: list of objects referencing the given uuid ONLY in their published
	 * 		version (and NOT in their work version -> ref deleted in work version)
	 * @param uuidObjectTo uuid of to object
	 * @param objectDoc doc where data is added
	 * @param howMuch how much data should be added
	 * @return doc containing additional data.
	 */
	public IngridDocument mapObjectReferencesFrom(List<ObjectNode> oNodesFrom[],
			String uuidObjectTo,
			IngridDocument objectDoc,
			MappingQuantity howMuch) {
		if (oNodesFrom == null) {
			return objectDoc;
		}

		int INDEX_REFS_WORK = 0;
		int INDEX_REFS_PUBLISHED = 1;

		List<IngridDocument> docLists[] = new ArrayList[oNodesFrom.length];
		for (int i=0; i < oNodesFrom.length; i++) {
			List<ObjectNode> nodeList = oNodesFrom[i];
			List<IngridDocument> docList = new ArrayList<IngridDocument>(nodeList.size());
			// map every node to IngridDoc
			for (ObjectNode oN : nodeList) {
				IngridDocument oFromDoc = new IngridDocument();
				// map working or published version, dependent from list !
				T01Object oFrom;
				if (i == INDEX_REFS_PUBLISHED) {
					oFrom = oN.getT01ObjectPublished();
				} else {
					oFrom = oN.getT01ObjectWork();
				}
				mapT01Object(oFrom, oFromDoc, howMuch);
				// also map relation info
				Set<ObjectReference> oRefs = oFrom.getObjectReferences();
				for (ObjectReference oRef : oRefs) {
					if (uuidObjectTo.equals(oRef.getObjToUuid())) {
						mapObjectReference(oRef, oFromDoc);
						break;
					}
				}
				docList.add(oFromDoc);
			}
			docLists[i] = docList;
		}

		objectDoc.put(MdekKeys.OBJ_REFERENCES_FROM, docLists[INDEX_REFS_WORK]);
		objectDoc.put(MdekKeys.OBJ_REFERENCES_FROM_PUBLISHED_ONLY, docLists[INDEX_REFS_PUBLISHED]);

		return objectDoc;
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
				// only take over "Auskunft" addresses !
				if (!MdekUtils.OBJ_ADR_TYPE_AUSKUNFT_ID.equals(oA.getType())) {
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
		urlDoc.put(MdekKeys.LINKAGE_DATATYPE, url.getDatatypeValue());
		urlDoc.put(MdekKeys.LINKAGE_DATATYPE_KEY, url.getDatatypeKey());
		urlDoc.put(MdekKeys.LINKAGE_VOLUME, url.getVolume());
		urlDoc.put(MdekKeys.LINKAGE_ICON_URL, url.getIcon());
		urlDoc.put(MdekKeys.LINKAGE_ICON_TEXT, url.getIconText());
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
		objectDoc.put(MdekKeys.EXPORTS, refList);
		
		return objectDoc;
	}

	private IngridDocument mapT014InfoImpart(T014InfoImpart ref, IngridDocument refDoc) {
		if (ref == null) {
			return refDoc;
		}

		refDoc.put(MdekKeys.EXPORT_KEY, ref.getImpartKey());
		refDoc.put(MdekKeys.EXPORT_VALUE, ref.getImpartValue());

		return refDoc;
	}

	private IngridDocument mapT011ObjGeo(Set<T011ObjGeo> refs, IngridDocument objectDoc) {
		if (refs == null || refs.size() == 0) {
			return objectDoc;
		}
		IngridDocument refDoc = new IngridDocument();
		
		// there should only be one object in the set because of the 1:1 relation between tables 
		// get first object from iterator
		T011ObjGeo ref = refs.iterator().next();
		refDoc.put(MdekKeys.TECHNICAL_BASE, ref.getSpecialBase());
		refDoc.put(MdekKeys.DATA, ref.getDataBase());
		refDoc.put(MdekKeys.METHOD_OF_PRODUCTION, ref.getMethod());
		refDoc.put(MdekKeys.COORDINATE_SYSTEM, ref.getReferencesystemValue());
		refDoc.put(MdekKeys.RESOLUTION, ref.getRecExact());
		refDoc.put(MdekKeys.DEGREE_OF_RECORD, ref.getRecGrade());
		refDoc.put(MdekKeys.HIERARCHY_LEVEL, ref.getHierarchyLevel());
		refDoc.put(MdekKeys.VECTOR_TOPOLOGY_LEVEL, ref.getVectorTopologyLevel());
		refDoc.put(MdekKeys.REFERENCESYSTEM_ID, ref.getReferencesystemKey());
		refDoc.put(MdekKeys.POS_ACCURACY_VERTICAL, ref.getPosAccuracyVertical());
		refDoc.put(MdekKeys.KEYC_INCL_W_DATASET, ref.getKeycInclWDataset());

		objectDoc.put(MdekKeys.TECHNICAL_DOMAIN_MAP, refDoc);
		
		// add key catalogs
		mapT011ObjGeoKeycs(ref.getT011ObjGeoKeycs(), refDoc);
		// add publication scales
		mapT011ObjGeoScales(ref.getT011ObjGeoScales(), refDoc);
		// add symbol catalogs
		mapT011ObjGeoSymcs(ref.getT011ObjGeoSymcs(), refDoc);
		// add feature types
		mapT011ObjGeoSupplinfos(ref.getT011ObjGeoSupplinfos(), refDoc);
		// add vector formats geo vector list
		mapT011ObjGeoVectors(ref.getT011ObjGeoVectors(), refDoc);
		// add vector formats geo vector list
		mapT011ObjGeoSpatialReps(ref.getT011ObjGeoSpatialReps(), refDoc);
		
		return objectDoc;
	}

	private IngridDocument mapT011ObjGeoKeycs(Set<T011ObjGeoKeyc> refs, IngridDocument objectDoc) {
		if (refs == null) {
			return objectDoc;
		}
		ArrayList<IngridDocument> locList = new ArrayList<IngridDocument>(refs.size());
		for (T011ObjGeoKeyc ref : refs) {
			IngridDocument doc = new IngridDocument();
			doc.put(MdekKeys.SUBJECT_CAT, ref.getKeycValue());
			doc.put(MdekKeys.SUBJECT_CAT_KEY, ref.getKeycKey());
			doc.put(MdekKeys.KEY_DATE, ref.getKeyDate());
			doc.put(MdekKeys.EDITION, ref.getEdition());
			locList.add(doc);					
		}
		objectDoc.put(MdekKeys.KEY_CATALOG_LIST, locList);
		
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

	private IngridDocument mapSearchtermValue(SearchtermValue ref, IngridDocument refDoc) {
		if (ref == null) {
			return refDoc;
		}

		refDoc.put(MdekKeys.TERM_NAME, ref.getTerm());
		refDoc.put(MdekKeys.TERM_TYPE, ref.getType());

		return refDoc;
	}
	private IngridDocument mapSearchtermSns(SearchtermSns ref, IngridDocument refDoc) {
		if (ref == null) {
			return refDoc;
		}

		refDoc.put(MdekKeys.TERM_SNS_ID, ref.getSnsId());

		return refDoc;
	}
	public IngridDocument mapSearchtermObjs(Set<SearchtermObj> refs, IngridDocument objectDoc,
		MappingQuantity howMuch)
	{
		if (refs == null) {
			return objectDoc;
		}
		ArrayList<IngridDocument> refList = new ArrayList<IngridDocument>(refs.size());
		String THESAURUS_TYPE = MdekUtils.SearchtermType.THESAURUS.getDbValue();
		for (SearchtermObj ref : refs) {
			IngridDocument refDoc = new IngridDocument();
			SearchtermValue refValue = ref.getSearchtermValue();
			if (refValue == null) {
				LOG.warn("SearchtermObj " + ref.getSearchtermId() + " has no SearchtermValue !!! We skip this SearchtermObj.");
				continue;
			}

			if (howMuch == MappingQuantity.INITIAL_ENTITY) {
				// only take over thesaurus terms !
				if (!THESAURUS_TYPE.equals(refValue.getType())) {
					continue;
				}
			}

			mapSearchtermValue(refValue, refDoc);
			SearchtermSns refSns = refValue.getSearchtermSns();
			mapSearchtermSns(refSns, refDoc);
			refList.add(refDoc);					
		}
		objectDoc.put(MdekKeys.SUBJECT_TERMS, refList);
		
		return objectDoc;
	}

	private IngridDocument mapSearchtermAdrs(Set<SearchtermAdr> refs, IngridDocument addressDoc,
		MappingQuantity howMuch)
	{
		if (refs == null) {
			return addressDoc;
		}
		ArrayList<IngridDocument> refList = new ArrayList<IngridDocument>(refs.size());
		String THESAURUS_TYPE = MdekUtils.SearchtermType.THESAURUS.getDbValue();
		for (SearchtermAdr ref : refs) {
			SearchtermValue refValue = ref.getSearchtermValue();
			if (refValue == null) {
				LOG.warn("SearchtermAdr " + ref.getSearchtermId() + " has no SearchtermValue !!! We skip this SearchtermAdr.");
				continue;
			}

			if (howMuch == MappingQuantity.INITIAL_ENTITY) {
				// only take over thesaurus terms !
				if (!THESAURUS_TYPE.equals(refValue.getType())) {
					continue;
				}
			}

			IngridDocument refDoc = new IngridDocument();
			mapSearchtermValue(refValue, refDoc);
			SearchtermSns refSns = refValue.getSearchtermSns();
			mapSearchtermSns(refSns, refDoc);
			refList.add(refDoc);					
		}
		addressDoc.put(MdekKeys.SUBJECT_TERMS, refList);
		
		return addressDoc;
	}

	private IngridDocument mapT0114EnvCategorys(Set<T0114EnvCategory> refs, IngridDocument objectDoc) {
		if (refs == null) {
			return objectDoc;
		}
		ArrayList<Integer> refList = new ArrayList<Integer>(refs.size());
		for (T0114EnvCategory ref : refs) {
			refList.add(ref.getCatKey());				
		}
		objectDoc.put(MdekKeys.ENV_CATEGORIES, refList);
		
		return objectDoc;
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
		Set<T011ObjData> refs = obj.getT011ObjDatas();
		if (refs == null || refs.size() == 0) {
			return objectDoc;
		}

		IngridDocument domainDoc = new IngridDocument();
		mapT011ObjData(refs.iterator().next(), domainDoc);
		objectDoc.put(MdekKeys.TECHNICAL_DOMAIN_DATASET, domainDoc);

		mapT011ObjDataParas(obj.getT011ObjDataParas(), domainDoc);

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

		// there should only be one object in the set because of the 1:1 relation between tables 
		// get first object from iterator
		T011ObjServ ref = refs.iterator().next();

		IngridDocument domainDoc = new IngridDocument();
		mapT011ObjServ(ref, domainDoc);
		objectDoc.put(MdekKeys.TECHNICAL_DOMAIN_SERVICE, domainDoc);

		// add service versions
		mapT011ObjServVersions(ref.getT011ObjServVersions(), domainDoc);
		// add service operations
		mapT011ObjServOperations(ref.getT011ObjServOperations(), domainDoc);

		return objectDoc;
	}
	private IngridDocument mapT011ObjServ(T011ObjServ ref, IngridDocument refDoc) {
		if (ref == null) {
			return refDoc;
		}

		refDoc.put(MdekKeys.SERVICE_TYPE, ref.getTypeValue());
		refDoc.put(MdekKeys.SERVICE_TYPE_KEY, ref.getTypeKey());
		refDoc.put(MdekKeys.SYSTEM_HISTORY, ref.getHistory());
		refDoc.put(MdekKeys.SYSTEM_ENVIRONMENT, ref.getEnvironment());
		refDoc.put(MdekKeys.DATABASE_OF_SYSTEM, ref.getBase());
		refDoc.put(MdekKeys.DESCRIPTION_OF_TECH_DOMAIN, ref.getDescription());

		return refDoc;
	}
	private IngridDocument mapT011ObjServVersions(Set<T011ObjServVersion> refs, IngridDocument inDoc) {
		if (refs == null) {
			return inDoc;
		}
		ArrayList<String> refList = new ArrayList<String>(refs.size());
		for (T011ObjServVersion ref : refs) {
			refList.add(ref.getServVersion());				
		}
		inDoc.put(MdekKeys.SERVICE_VERSION_LIST, refList);
		
		return inDoc;
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
		if (refs == null) {
			return inDoc;
		}
		ArrayList<String> refList = new ArrayList<String>(refs.size());
		for (T011ObjServOpPlatform ref : refs) {
			refList.add(ref.getPlatform());				
		}
		inDoc.put(MdekKeys.PLATFORM_LIST, refList);
		
		return inDoc;
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
		refDoc.put(MdekKeys.CLASS, parentAddress.getAdrType());
		resultDoc.put(MdekKeys.PARENT_INFO, refDoc);
		return resultDoc;
	}
	
	public IngridDocument mapT03Catalog(T03Catalogue cat, IngridDocument resultDoc) {
		resultDoc.put(MdekKeys.UUID, cat.getCatUuid());
		resultDoc.put(MdekKeys.CATALOG_NAME, cat.getCatName());
		resultDoc.put(MdekKeys.PARTNER_NAME, cat.getPartnerName());
		resultDoc.put(MdekKeys.PROVIDER_NAME, cat.getProviderName());
		resultDoc.put(MdekKeys.COUNTRY, cat.getCountryCode());
		resultDoc.put(MdekKeys.LANGUAGE, cat.getLanguageCode());
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
		
		ArrayList<IngridDocument> entryList = new ArrayList<IngridDocument>(list.size());
		for (SysList entry : list) {
			IngridDocument entryDoc = new IngridDocument();
			entryDoc.put(MdekKeys.ENTRY_ID, entry.getEntryId());
			entryDoc.put(MdekKeys.ENTRY_NAME, entry.getName());
			entryDoc.put(MdekKeys.IS_DEFAULT, entry.getIsDefault());
			entryList.add(entryDoc);
		}
		listDoc.put(MdekKeys.LST_ENTRY_LIST, entryList);

		return listDoc;
	}
}
