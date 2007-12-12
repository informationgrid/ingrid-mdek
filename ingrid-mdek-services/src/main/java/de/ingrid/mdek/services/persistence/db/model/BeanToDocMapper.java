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
	public enum MappingType {
		TOP_ENTITY,
		SUB_ENTITY,
		BASIC_ENTITY,
		DETAIL_ENTITY
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

	/** Map data according to given quantity and add given specials */
	public IngridDocument mapT01Object(T01Object o, MappingType type) {
		IngridDocument doc = new IngridDocument();

		doc.put(MdekKeys.UUID, o.getId());
		doc.put(MdekKeys.CLASS, o.getObjClass());
		doc.put(MdekKeys.TITLE, o.getObjName());
		
		if (type == MappingType.DETAIL_ENTITY) {
			doc.put(MdekKeys.ABSTRACT, o.getObjDescr());
			
			// get related addresses
			Set<T02Address> adrs = o.getT012ObjAdrs();
			ArrayList<IngridDocument> adrsList = new ArrayList<IngridDocument>(adrs.size());
			for (T02Address adr : adrs) {
				adrsList.add(mapT02Address(adr, MappingType.BASIC_ENTITY));
			}
			doc.put(MdekKeys.ADR_ENTITIES, adrsList);

		}

		if (type == MappingType.TOP_ENTITY ||
			type == MappingType.SUB_ENTITY)
		{
        	boolean hasChild = false;
    		if (o.getT012ObjObjs().size() > 0) {
            	hasChild = true;
    		}
    		doc.putBoolean(MdekKeys.HAS_CHILD, hasChild);
        }

		return doc;
	}

	/** Map data according to given quantity and add given specials */
	public IngridDocument mapT02Address(T02Address a, MappingType type) {
		IngridDocument doc = new IngridDocument();

		doc.put(MdekKeys.UUID, a.getId());
		doc.put(MdekKeys.CLASS, a.getTyp());
		doc.put(MdekKeys.ORGANISATION, a.getInstitution());
		doc.put(MdekKeys.NAME, a.getLastname());
		doc.put(MdekKeys.GIVEN_NAME, a.getFirstname());
		doc.put(MdekKeys.TITLE_OR_FUNCTION, a.getTitle());

		if (type == MappingType.DETAIL_ENTITY) {
			doc.put(MdekKeys.STREET, a.getStreet());
			doc.put(MdekKeys.POSTAL_CODE_OF_COUNTRY, a.getStateId());
			doc.put(MdekKeys.CITY, a.getCity());
			doc.put(MdekKeys.POST_BOX_POSTAL_CODE, a.getPostboxPc());
			doc.put(MdekKeys.POST_BOX, a.getPostbox());
			doc.put(MdekKeys.FUNCTION, a.getJob());			
			doc.put(MdekKeys.NAME_FORM, a.getAddress());
			doc.put(MdekKeys.ADDRESS_DESCRIPTION, a.getDescr());			
		}
		
		if (type == MappingType.TOP_ENTITY ||
				type == MappingType.SUB_ENTITY)
		{
        	boolean hasChild = false;
    		if (a.getT022AdrAdrs().size() > 0) {
            	hasChild = true;
    		}
    		doc.putBoolean(MdekKeys.HAS_CHILD, hasChild);
		}

		return doc;
	}
}
