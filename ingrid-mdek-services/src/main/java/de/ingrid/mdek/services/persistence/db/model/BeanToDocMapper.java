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

	public IngridDocument mapObjectNode(ObjectNode oNIn, MappingQuantity type) {
		if (oNIn == null) {
			return null;
		}

		T01Object o = oNIn.getT01ObjectWork();
		
		IngridDocument doc = new IngridDocument();
		doc.put(MdekKeys.ID, o.getId());
		doc.put(MdekKeys.UUID, o.getObjUuid());
		doc.put(MdekKeys.CLASS, o.getObjClass());
		doc.put(MdekKeys.TITLE, o.getObjName());
		doc.put(MdekKeys.WORK_STATE, o.getWorkState());
		
		if (type == MappingQuantity.DETAIL_ENTITY) {
			doc.put(MdekKeys.DATASET_ALTERNATE_NAME, o.getDatasetAlternateName());
			doc.put(MdekKeys.ABSTRACT, o.getObjDescr());
			doc.put(MdekKeys.DATE_OF_CREATION, o.getCreateTime());
			doc.put(MdekKeys.DATE_OF_LAST_MODIFICATION, o.getModTime());

			doc.put(MdekKeys.VERTICAL_EXTENT_MINIMUM, o.getVerticalExtentMinimum());
			doc.put(MdekKeys.VERTICAL_EXTENT_MAXIMUM, o.getVerticalExtentMaximum());
			doc.put(MdekKeys.VERTICAL_EXTENT_UNIT, o.getVerticalExtentUnit());
			doc.put(MdekKeys.VERTICAL_EXTENT_VDATUM, o.getVerticalExtentVdatum());
			doc.put(MdekKeys.DESCRIPTION_OF_SPATIAL_DOMAIN, o.getLocDescr());

			doc.put(MdekKeys.TIME_TYPE, o.getTimeType());
			doc.put(MdekKeys.BEGINNING_DATE, o.getTimeFrom());
			doc.put(MdekKeys.ENDING_DATE, o.getTimeTo());
			doc.put(MdekKeys.TIME_STATUS, o.getTimeStatus());
			doc.put(MdekKeys.TIME_PERIOD, o.getTimePeriod());
			doc.put(MdekKeys.TIME_STEP, o.getTimeInterval());
			doc.put(MdekKeys.TIME_SCALE, o.getTimeAlle());
			doc.put(MdekKeys.DESCRIPTION_OF_TEMPORAL_DOMAIN, o.getTimeDescr());
			
			// get related addresses
			Set<T012ObjAdr> oAs = o.getT012ObjAdrs();
			ArrayList<IngridDocument> adrsList = new ArrayList<IngridDocument>(oAs.size());
			for (T012ObjAdr oA : oAs) {
				T02Address a = oA.getAddressNode().getT02AddressWork();
				IngridDocument aDoc = mapT02Address(a, MappingQuantity.TABLE_ENTITY);
				aDoc.put(MdekKeys.RELATION_TYPE, oA.getType());
				adrsList.add(aDoc);
			}
			doc.put(MdekKeys.ADR_ENTITIES, adrsList);

			// get related objects (Querverweise)
			Set<ObjectReference> oRs = o.getObjectReferences();
			ArrayList<IngridDocument> objsList = new ArrayList<IngridDocument>(oRs.size());
			for (ObjectReference oR : oRs) {
				ObjectNode oN = oR.getObjectNode();
				IngridDocument oToDoc = mapObjectNode(oN, MappingQuantity.TABLE_ENTITY);
				oToDoc.put(MdekKeys.RELATION_DESCRIPTION, oR.getDescr());
				objsList.add(oToDoc);
			}
			doc.put(MdekKeys.OBJ_ENTITIES, objsList);

		}

		if (type == MappingQuantity.TOP_ENTITY ||
			type == MappingQuantity.SUB_ENTITY)
		{
        	boolean hasChild = false;
    		if (oNIn.getObjectNodeChildren().size() > 0) {
            	hasChild = true;
    		}
    		doc.putBoolean(MdekKeys.HAS_CHILD, hasChild);
        }

		return doc;
	}

	public IngridDocument mapT02Address(T02Address a, MappingQuantity type) {
		if (a == null) {
			return null;
		}

		IngridDocument doc = new IngridDocument();
		doc.put(MdekKeys.ID, a.getId());
		doc.put(MdekKeys.UUID, a.getAdrUuid());
		doc.put(MdekKeys.CLASS, a.getAdrType());
		doc.put(MdekKeys.ORGANISATION, a.getInstitution());
		doc.put(MdekKeys.NAME, a.getLastname());
		doc.put(MdekKeys.GIVEN_NAME, a.getFirstname());
		doc.put(MdekKeys.TITLE_OR_FUNCTION, a.getTitle());

		if (type == MappingQuantity.TABLE_ENTITY ||
			type == MappingQuantity.DETAIL_ENTITY)
		{
			doc.put(MdekKeys.STREET, a.getStreet());
			doc.put(MdekKeys.POSTAL_CODE_OF_COUNTRY, a.getCountryCode());
			doc.put(MdekKeys.CITY, a.getCity());
			doc.put(MdekKeys.POST_BOX_POSTAL_CODE, a.getPostboxPc());
			doc.put(MdekKeys.POST_BOX, a.getPostbox());

			// add communication data (emails etc.) 
			Set<T021Communication> comms = a.getT021Communications();
			ArrayList<IngridDocument> docList = new ArrayList<IngridDocument>(comms.size());
			for (T021Communication c : comms) {
				docList.add(mapT021Communication(c, MappingQuantity.TABLE_ENTITY));
			}
			doc.put(MdekKeys.COMMUNICATION, docList);				
		}

		if (type == MappingQuantity.DETAIL_ENTITY) {
			doc.put(MdekKeys.FUNCTION, a.getJob());			
			doc.put(MdekKeys.NAME_FORM, a.getAddress());
			doc.put(MdekKeys.ADDRESS_DESCRIPTION, a.getDescr());			
		}
/*
		// add flag indicating having children 
		if (type == MappingQuantity.TOP_ENTITY ||
			type == MappingQuantity.SUB_ENTITY)
		{
        	boolean hasChild = false;
    		if (a.getT022AdrAdrs().size() > 0) {
            	hasChild = true;
    		}
    		doc.putBoolean(MdekKeys.HAS_CHILD, hasChild);
		}
*/
		return doc;
	}

	public IngridDocument mapT021Communication(T021Communication c, MappingQuantity type) {
		if (c == null) {
			return null;
		}

		IngridDocument doc = new IngridDocument();
		doc.put(MdekKeys.ID, c.getId());
		doc.put(MdekKeys.COMMUNICATION_MEDIUM, c.getCommType());
		doc.put(MdekKeys.COMMUNICATION_VALUE, c.getCommValue());

		if (type == MappingQuantity.DETAIL_ENTITY) {
			doc.put(MdekKeys.COMMUNICATION_DESCRIPTION, c.getDescr());
		}
		
		return doc;
	}
}
