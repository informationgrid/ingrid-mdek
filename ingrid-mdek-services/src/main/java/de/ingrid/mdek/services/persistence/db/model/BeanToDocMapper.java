package de.ingrid.mdek.services.persistence.db.model;

import java.util.ArrayList;
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
	 * Transfer structural info (parent/child) of passed bean to passed doc.
	 * @return doc containing additional data.
	 */
	public IngridDocument mapObjectNode(ObjectNode oNIn, IngridDocument objectDoc,
			MappingQuantity howMuch) {
		if (oNIn == null) {
			return null;
		}

    	boolean hasChild = false;
		if (oNIn.getObjectNodeChildren().size() > 0) {
        	hasChild = true;
		}
		objectDoc.putBoolean(MdekKeys.HAS_CHILD, hasChild);

		// TODO: Extent MappingQuantity.FULL_ENTITY for copy

		return objectDoc;
	}

	/**
	 * Transfer object relation data of passed bean to passed doc.
	 * @return doc containing additional data.
	 */
	public IngridDocument mapObjectReference(ObjectReference oR, IngridDocument objectDoc,
			MappingQuantity howMuch) {
		if (oR == null) {
			return null;
		}

		objectDoc.put(MdekKeys.RELATION_DESCRIPTION, oR.getDescr());
		objectDoc.put(MdekKeys.RELATION_TYPE_NAME, oR.getSpecialName());

		if (howMuch == MappingQuantity.FULL_ENTITY) {
			objectDoc.put(MdekKeys.RELATION_TYPE_REF, oR.getSpecialRef());
			
			// TODO: Extent MappingQuantity.FULL_ENTITY for copy
		}

		return objectDoc;
	}

	/**
	 * Transfer object data of passed bean to passed doc.
	 * Also includes related addresses etc. dependent from MappingQuantity.
	 * @return doc containing additional data.
	 */
	public IngridDocument mapT01Object(T01Object o, IngridDocument objectDoc,
			MappingQuantity howMuch) {
		if (o == null) {
			return null;
		}
		
		objectDoc.put(MdekKeys.UUID, o.getObjUuid());
		objectDoc.put(MdekKeys.CLASS, o.getObjClass());
		objectDoc.put(MdekKeys.TITLE, o.getObjName());
		objectDoc.put(MdekKeys.WORK_STATE, o.getWorkState());
		
		if (howMuch == MappingQuantity.DETAIL_ENTITY) {
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
			
			// get related addresses
			Set<T012ObjAdr> oAs = o.getT012ObjAdrs();
			ArrayList<IngridDocument> adrsList = new ArrayList<IngridDocument>(oAs.size());
			for (T012ObjAdr oA : oAs) {
				IngridDocument aDoc = new IngridDocument();
				mapT012ObjAdr(oA, aDoc, MappingQuantity.TABLE_ENTITY);
				T02Address a = oA.getAddressNode().getT02AddressWork();
				mapT02Address(a, aDoc, MappingQuantity.TABLE_ENTITY);
				adrsList.add(aDoc);
			}
			objectDoc.put(MdekKeys.ADR_ENTITIES, adrsList);

			// get related objects (Querverweise)
			Set<ObjectReference> oRefs = o.getObjectReferences();
			ArrayList<IngridDocument> objsList = new ArrayList<IngridDocument>(oRefs.size());
			for (ObjectReference oRef : oRefs) {
				IngridDocument oToDoc = new IngridDocument();
				mapObjectReference(oRef, oToDoc, MappingQuantity.TABLE_ENTITY);
				T01Object oTo = oRef.getObjectNode().getT01ObjectWork();
				mapT01Object(oTo, oToDoc, MappingQuantity.TABLE_ENTITY);
				objsList.add(oToDoc);
			}
			objectDoc.put(MdekKeys.OBJ_ENTITIES, objsList);
		}

		// TODO: Extent MappingQuantity.FULL_ENTITY for copy

		return objectDoc;
	}

	/**
	 * Transfer relation data of passed bean to passed doc.
	 * @return doc containing additional data.
	 */
	public IngridDocument mapT012ObjAdr(T012ObjAdr oA, IngridDocument adressDoc,
			MappingQuantity howMuch) {
		if (oA == null) {
			return null;
		}

		adressDoc.put(MdekKeys.RELATION_TYPE_ID, oA.getType());
		adressDoc.put(MdekKeys.RELATION_TYPE_NAME, oA.getSpecialName());

		if (howMuch == MappingQuantity.FULL_ENTITY) {
			adressDoc.put(MdekKeys.RELATION_TYPE_REF, oA.getSpecialRef());

			// TODO: Extent MappingQuantity.FULL_ENTITY for copy
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
			return null;
		}

		adressDoc.put(MdekKeys.UUID, a.getAdrUuid());
		adressDoc.put(MdekKeys.CLASS, a.getAdrType());
		adressDoc.put(MdekKeys.ORGANISATION, a.getInstitution());
		adressDoc.put(MdekKeys.NAME, a.getLastname());
		adressDoc.put(MdekKeys.GIVEN_NAME, a.getFirstname());
		adressDoc.put(MdekKeys.TITLE_OR_FUNCTION, a.getTitle());

		if (howMuch == MappingQuantity.TABLE_ENTITY ||
			howMuch == MappingQuantity.DETAIL_ENTITY)
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
				docList.add(mapT021Communication(c, commDoc, MappingQuantity.TABLE_ENTITY));
			}
			adressDoc.put(MdekKeys.COMMUNICATION, docList);				
		}

		if (howMuch == MappingQuantity.DETAIL_ENTITY) {
			adressDoc.put(MdekKeys.FUNCTION, a.getJob());			
			adressDoc.put(MdekKeys.NAME_FORM, a.getAddress());
			adressDoc.put(MdekKeys.ADDRESS_DESCRIPTION, a.getDescr());			
		}

		// TODO: Extent MappingQuantity.FULL_ENTITY for copy

		return adressDoc;
	}

	/**
	 * Transfer communication data of passed bean to passed doc.
	 * @return doc containing additional data.
	 */
	public IngridDocument mapT021Communication(T021Communication c, IngridDocument commDoc,
			MappingQuantity howMuch) {
		if (c == null) {
			return null;
		}

		commDoc.put(MdekKeys.ID, c.getId());
		commDoc.put(MdekKeys.COMMUNICATION_MEDIUM, c.getCommType());
		commDoc.put(MdekKeys.COMMUNICATION_VALUE, c.getCommValue());

		if (howMuch == MappingQuantity.DETAIL_ENTITY) {
			commDoc.put(MdekKeys.COMMUNICATION_DESCRIPTION, c.getDescr());
		}

		// TODO: Extent MappingQuantity.FULL_ENTITY for copy

		return commDoc;
	}
}
