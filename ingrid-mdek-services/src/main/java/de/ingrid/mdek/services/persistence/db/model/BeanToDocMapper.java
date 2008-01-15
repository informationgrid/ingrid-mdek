package de.ingrid.mdek.services.persistence.db.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.utils.IngridDocument;

/**
 * Singleton encapsulating methods for mapping hibernate beans to ingrid documents.
 * 
 * @author Martin
 */
public class BeanToDocMapper implements IMapper {

	private static final Logger LOG = Logger.getLogger(BeanToDocMapper.class);

	private static BeanToDocMapper myInstance;

	/** Get The Singleton */
	public static synchronized BeanToDocMapper getInstance() {
		if (myInstance == null) {
	        myInstance = new BeanToDocMapper();
	      }
		return myInstance;
	}

	private BeanToDocMapper() {}

	/**
	 * Transfer structural info ("hasChild") to passed doc.
	 * @return doc containing additional data.
	 */
	public IngridDocument mapObjectNode(ObjectNode oNIn, IngridDocument objectDoc) {
		if (oNIn == null) {
			return objectDoc;
		}

    	boolean hasChild = false;
		if (oNIn.getObjectNodeChildren().size() > 0) {
        	hasChild = true;
		}
		objectDoc.putBoolean(MdekKeys.HAS_CHILD, hasChild);

		return objectDoc;
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
		
		// just to track ID in test suite !
		objectDoc.put(MdekKeys.ID, o.getId());
		objectDoc.put(MdekKeys.UUID, o.getObjUuid());
		objectDoc.put(MdekKeys.CLASS, o.getObjClass());
		objectDoc.put(MdekKeys.TITLE, o.getObjName());
		objectDoc.put(MdekKeys.WORK_STATE, o.getWorkState());
		
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

			// map related objects (Querverweise)
			Set<ObjectReference> oRefs = o.getObjectReferences();
			mapObjectReferences(oRefs, objectDoc, howMuch);

			// map related addresses
			Set<T012ObjAdr> oAs = o.getT012ObjAdrs();
			mapT012ObjAdrs(oAs, objectDoc, howMuch);

			// map related spatial references
			Set<SpatialReference> spatRefs = o.getSpatialReferences();
			mapSpatialReferences(spatRefs, objectDoc);

			// map related url references
			Set<T017UrlRef> urlRefs = o.getT017UrlRefs();
			mapT017UrlRefs(urlRefs, objectDoc);
		}

		if (howMuch == MappingQuantity.COPY_ENTITY) {
			objectDoc.put(MdekKeys.ORIGINAL_CONTROL_IDENTIFIER, o.getOrgObjId());
			objectDoc.put(MdekKeys.NO_OF_PARENTS, o.getRoot());
			objectDoc.put(MdekKeys.CATALOGUE_IDENTIFIER, o.getCatId());
			objectDoc.put(MdekKeys.DATASET_CHARACTER_SET, o.getDatasetCharacterSet());
			objectDoc.put(MdekKeys.METADATA_CHARACTER_SET, o.getMetadataCharacterSet());
			objectDoc.put(MdekKeys.METADATA_STANDARD_NAME, o.getMetadataStandardName());
			objectDoc.put(MdekKeys.METADATA_STANDARD_VERSION, o.getMetadataStandardVersion());
			objectDoc.put(MdekKeys.LASTEXPORT_TIME, o.getLastexportTime());
			objectDoc.put(MdekKeys.EXPIRY_TIME, o.getExpiryTime());
			objectDoc.put(MdekKeys.WORK_VERSION, o.getWorkVersion());
			objectDoc.put(MdekKeys.MARK_DELETED, o.getMarkDeleted());
			objectDoc.put(MdekKeys.MOD_UUID, o.getModUuid());
			objectDoc.put(MdekKeys.RESPONSIBLE_UUID, o.getResponsibleUuid());
		}

		return objectDoc;
	}

	/**
	 * Transfer object relation data of passed bean to passed doc.
	 * @return doc containing additional data.
	 */
	public IngridDocument mapObjectReference(ObjectReference oR, IngridDocument objectDoc,
			MappingQuantity howMuch) {
		if (oR == null) {
			return objectDoc;
		}

		objectDoc.put(MdekKeys.RELATION_TYPE_NAME, oR.getSpecialName());
		objectDoc.put(MdekKeys.RELATION_DESCRIPTION, oR.getDescr());

		if (howMuch == MappingQuantity.COPY_ENTITY) {
			// uuid should be already set in objectDoc
//			objectDoc.put(MdekKeys.UUID, oR.getObjToUuid());
			objectDoc.put(MdekKeys.RELATION_TYPE_REF, oR.getSpecialRef());
		}

		return objectDoc;
	}

	/**
	 * Transfer From-objectReferences (passed beans) to passed doc.
	 * @param oNodesFrom from object references
	 * @param uuidObjectTo uuid of to object
	 * @param objectDoc doc where data is added
	 * @param howMuch how much data should be added
	 * @return doc containing additional data.
	 */
	public IngridDocument mapObjectReferencesFrom(List<ObjectNode> oNodesFrom,
			String uuidObjectTo,
			IngridDocument objectDoc,
			MappingQuantity howMuch) {
		if (oNodesFrom == null) {
			return objectDoc;
		}

		ArrayList<IngridDocument> oRefFromList = new ArrayList<IngridDocument>(oNodesFrom.size());
		for (ObjectNode oN : oNodesFrom) {
			IngridDocument oFromDoc = new IngridDocument();
			T01Object oFrom = oN.getT01ObjectWork();
			mapT01Object(oFrom, oFromDoc, howMuch);
			// also map relation info
			Set<ObjectReference> oRefs = oFrom.getObjectReferences();
			for (ObjectReference oRef : oRefs) {
				if (uuidObjectTo.equals(oRef.getObjToUuid())) {
					mapObjectReference(oRef, oFromDoc, howMuch);
					break;
				}
			}
			oRefFromList.add(oFromDoc);
		}
		objectDoc.put(MdekKeys.OBJ_REFERENCES_FROM, oRefFromList);

		return objectDoc;
	}

	/**
	 * Transfer relation data of passed bean to passed doc.
	 * @return doc containing additional data.
	 */
	public IngridDocument mapT012ObjAdr(T012ObjAdr oA, IngridDocument adressDoc,
			MappingQuantity howMuch) {
		if (oA == null) {
			return adressDoc;
		}

		adressDoc.put(MdekKeys.RELATION_TYPE_ID, oA.getType());
		adressDoc.put(MdekKeys.RELATION_TYPE_NAME, oA.getSpecialName());

		if (howMuch == MappingQuantity.COPY_ENTITY) {
			// uuid should be already set in objectDoc
//			adressDoc.put(MdekKeys.UUID, oA.getAdrUuid());
			adressDoc.put(MdekKeys.RELATION_TYPE_REF, oA.getSpecialRef());
			adressDoc.put(MdekKeys.RELATION_DATE_OF_LAST_MODIFICATION, oA.getModTime());
		}

		return adressDoc;
	}

	/**
	 * Transfer address data of passed bean to passed doc.
	 * Also includes communication etc. dependent from MappingQuantity.
	 * @return doc containing additional data.
	 */
	public IngridDocument mapT02Address(T02Address a, IngridDocument adressDoc,
			MappingQuantity howMuch) {
		if (a == null) {
			return adressDoc;
		}

		adressDoc.put(MdekKeys.UUID, a.getAdrUuid());
		adressDoc.put(MdekKeys.CLASS, a.getAdrType());
		adressDoc.put(MdekKeys.ORGANISATION, a.getInstitution());
		adressDoc.put(MdekKeys.NAME, a.getLastname());
		adressDoc.put(MdekKeys.GIVEN_NAME, a.getFirstname());
		adressDoc.put(MdekKeys.TITLE_OR_FUNCTION, a.getTitle());

		if (howMuch == MappingQuantity.TABLE_ENTITY ||
			howMuch == MappingQuantity.DETAIL_ENTITY ||
			howMuch == MappingQuantity.COPY_ENTITY)
		{
			adressDoc.put(MdekKeys.STREET, a.getStreet());
			adressDoc.put(MdekKeys.POSTAL_CODE_OF_COUNTRY, a.getCountryCode());
			adressDoc.put(MdekKeys.CITY, a.getCity());
			adressDoc.put(MdekKeys.POST_BOX_POSTAL_CODE, a.getPostboxPc());
			adressDoc.put(MdekKeys.POST_BOX, a.getPostbox());

			// add communication data (emails etc.) 
			Set<T021Communication> comms = a.getT021Communications();
			ArrayList<IngridDocument> docList = new ArrayList<IngridDocument>(comms.size());
			for (T021Communication c : comms) {
				IngridDocument commDoc = new IngridDocument();
				docList.add(mapT021Communication(c, commDoc, howMuch));
			}
			adressDoc.put(MdekKeys.COMMUNICATION, docList);				
		}

		if (howMuch == MappingQuantity.DETAIL_ENTITY ||
			howMuch == MappingQuantity.COPY_ENTITY)
		{
			adressDoc.put(MdekKeys.FUNCTION, a.getJob());			
			adressDoc.put(MdekKeys.NAME_FORM, a.getAddress());
			adressDoc.put(MdekKeys.ADDRESS_DESCRIPTION, a.getDescr());			
		}

		if (howMuch == MappingQuantity.COPY_ENTITY) {
			// TODO: Extent MappingQuantity.FULL_ENTITY for copy			
		}

		return adressDoc;
	}

	/**
	 * Transfer communication data of passed bean to passed doc.
	 * @return doc containing additional data.
	 */
	public IngridDocument mapT021Communication(T021Communication c, IngridDocument commDoc,
			MappingQuantity howMuch) {
		if (c == null) {
			return commDoc;
		}

		commDoc.put(MdekKeys.COMMUNICATION_MEDIUM, c.getCommType());
		commDoc.put(MdekKeys.COMMUNICATION_VALUE, c.getCommValue());

		if (howMuch == MappingQuantity.DETAIL_ENTITY ||
			howMuch == MappingQuantity.COPY_ENTITY)
		{
			commDoc.put(MdekKeys.COMMUNICATION_DESCRIPTION, c.getDescr());
		}

		if (howMuch == MappingQuantity.COPY_ENTITY) {
			// TODO: Extent MappingQuantity.FULL_ENTITY for copy			
		}

		return commDoc;
	}

	/**
	 * Transfer data of passed bean to passed doc.
	 * @return doc containing additional data.
	 */
	public IngridDocument mapSpatialRefValue(SpatialRefValue spatRefValue, IngridDocument locDoc) {
		if (spatRefValue == null) {
			return locDoc;
		}

		locDoc.put(MdekKeys.LOCATION_NAME, spatRefValue.getName());
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
	public IngridDocument mapSpatialRefSns(SpatialRefSns spatRefSns, IngridDocument locDoc) {
		if (spatRefSns == null) {
			return locDoc;
		}

		locDoc.put(MdekKeys.LOCATION_SNS_ID, spatRefSns.getSnsId());

		return locDoc;
	}

	public IngridDocument mapT017UrlRef(T017UrlRef url, IngridDocument urlDoc) {
		if (url == null) {
			return urlDoc;
		}

		urlDoc.put(MdekKeys.LINKAGE_URL, url.getUrlLink());
		urlDoc.put(MdekKeys.LINKAGE_REFERENCE_ID, url.getSpecialRef());
		urlDoc.put(MdekKeys.LINKAGE_REFERENCE, url.getSpecialName());
		urlDoc.put(MdekKeys.LINKAGE_DATATYPE, url.getDatatype());
		urlDoc.put(MdekKeys.LINKAGE_VOLUME, url.getVolume());
		urlDoc.put(MdekKeys.LINKAGE_ICON_URL, url.getIcon());
		urlDoc.put(MdekKeys.LINKAGE_ICON_TEXT, url.getIconText());
		urlDoc.put(MdekKeys.LINKAGE_DESCRIPTION, url.getDescr());
		urlDoc.put(MdekKeys.LINKAGE_NAME, url.getContent());
		urlDoc.put(MdekKeys.LINKAGE_URL_TYPE, url.getUrlType());

		return urlDoc;
	}

	private IngridDocument mapObjectReferences(Set<ObjectReference> oRefs, IngridDocument objectDoc,
			MappingQuantity howMuch) {
		if (oRefs == null) {
			return objectDoc;
		}
		ArrayList<IngridDocument> objsList = new ArrayList<IngridDocument>(oRefs.size());
		for (ObjectReference oRef : oRefs) {
			IngridDocument oToDoc = new IngridDocument();
			mapObjectReference(oRef, oToDoc, howMuch);
			ObjectNode oNode = oRef.getObjectNode();
			if (oNode != null) {
				T01Object oTo = oNode.getT01ObjectWork();
				mapT01Object(oTo, oToDoc, howMuch);
				objsList.add(oToDoc);					
			} else {
				LOG.warn("Object " + oRef.getObjToUuid() + " has no ObjectNode !!! We skip this object reference.");
			}
		}
		objectDoc.put(MdekKeys.OBJ_REFERENCES_TO, objsList);
		
		return objectDoc;
	}

	private IngridDocument mapT012ObjAdrs(Set<T012ObjAdr> oAs, IngridDocument objectDoc,
			MappingQuantity howMuch) {
		if (oAs == null) {
			return objectDoc;
		}
		ArrayList<IngridDocument> adrsList = new ArrayList<IngridDocument>(oAs.size());
		for (T012ObjAdr oA : oAs) {
			IngridDocument aDoc = new IngridDocument();
			mapT012ObjAdr(oA, aDoc, howMuch);
			AddressNode aNode = oA.getAddressNode();
			if (aNode != null) {
				T02Address a = aNode.getT02AddressWork();
				mapT02Address(a, aDoc, howMuch);
				adrsList.add(aDoc);					
			} else {
				LOG.warn("Address " + oA.getAdrUuid() + " has no AddressNode !!! We skip this address reference.");
			}
		}
		objectDoc.put(MdekKeys.ADR_REFERENCES_TO, adrsList);
		
		return objectDoc;
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
}
