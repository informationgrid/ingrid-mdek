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
public class BeanToDocMapper {

	private static final Logger LOG = Logger.getLogger(BeanToDocMapper.class);

	/** How much to map of bean properties */
	public enum MappingQuantity {
		BASIC_ENTITY, // client: minimum data of bean needed
		TOP_ENTITY, // client: bean displayed in tree as topnode
		SUB_ENTITY, // client: bean displayed in tree as subnode
		TABLE_ENTITY, // client: bean displayed in table
		DETAIL_ENTITY // client: bean edit/save
	}

	private static BeanToDocMapper myInstance;

	/** Get The Singleton */
	public static synchronized BeanToDocMapper getInstance() {
		if (myInstance == null) {
	        myInstance = new BeanToDocMapper();
	      }
		return myInstance;
	}

	private BeanToDocMapper() {}

	public IngridDocument mapT01Object(T01Object o, MappingQuantity type) {
		IngridDocument doc = new IngridDocument();
		if (o == null) {
			return doc;
		}

		doc.put(MdekKeys.ID, o.getId());
		doc.put(MdekKeys.UUID, o.getObjUuid());
		doc.put(MdekKeys.CLASS, o.getObjClass());
		doc.put(MdekKeys.TITLE, o.getObjName());
		
		if (type == MappingQuantity.DETAIL_ENTITY) {
			doc.put(MdekKeys.ABSTRACT, o.getObjDescr());
			
			// get related addresses
			Set<T012ObjAdr> oAs = o.getT012ObjAdrs();
			ArrayList<IngridDocument> adrsList = new ArrayList<IngridDocument>(oAs.size());
			for (T012ObjAdr oA : oAs) {
				T02Address a = oA.getT02Address();
				IngridDocument aDoc = mapT02Address(a, MappingQuantity.TABLE_ENTITY);
				aDoc.put(MdekKeys.TYPE_OF_RELATION, oA.getType());
				adrsList.add(aDoc);
			}
			doc.put(MdekKeys.ADR_ENTITIES, adrsList);
		}

		if (type == MappingQuantity.TOP_ENTITY ||
			type == MappingQuantity.SUB_ENTITY)
		{
        	boolean hasChild = false;
    		if (o.getT012ObjObjs().size() > 0) {
            	hasChild = true;
    		}
    		doc.putBoolean(MdekKeys.HAS_CHILD, hasChild);
        }

		return doc;
	}

	public IngridDocument mapT02Address(T02Address a, MappingQuantity type) {
		IngridDocument doc = new IngridDocument();
		if (a == null) {
			return doc;
		}

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
		IngridDocument doc = new IngridDocument();
		if (c == null) {
			return doc;
		}

		doc.put(MdekKeys.ID, c.getId());
		doc.put(MdekKeys.COMMUNICATION_MEDIUM, c.getCommType());
		doc.put(MdekKeys.COMMUNICATION_VALUE, c.getCommValue());

		if (type == MappingQuantity.DETAIL_ENTITY) {
			doc.put(MdekKeys.COMMUNICATION_DESCRIPTION, c.getDescr());
		}
		
		return doc;
	}
}
