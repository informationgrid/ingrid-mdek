package de.ingrid.mdek.services.persistence.db.model;

import de.ingrid.mdek.job.MdekKeys;
import de.ingrid.utils.IngridDocument;

/**
 * Singleton encapsulating methods for mapping hibernate beans to ingrid documents.
 * 
 * @author Martin
 */
public class BeanToDocMapper {

	private static BeanToDocMapper myInstance;

	public static synchronized BeanToDocMapper getInstance() {
		if (myInstance == null) {
	        myInstance = new BeanToDocMapper();
	      }
		return myInstance;
	}

	public IngridDocument mapT01Object(T01Object o) {
		IngridDocument doc = new IngridDocument();

		doc.put(MdekKeys.ENTITY_UUID, o.getId());
		doc.put(MdekKeys.ENTITY_NAME, o.getObjName());
//		doc.put(MdekKeys.ENTITY_DESCRIPTION, o.getObjDescr());
		
		return doc;
	}

	public IngridDocument mapT02Address(T02Address a) {
		IngridDocument doc = new IngridDocument();

		doc.put(MdekKeys.ENTITY_UUID, a.getId());
		
		String name = a.getInstitution();
		if (name == null) {
			name = a.getFirstname() + " " + a.getLastname();
		}
		doc.put(MdekKeys.ENTITY_NAME, name);
		doc.put(MdekKeys.ENTITY_DESCRIPTION, a.getDescr());
		
		return doc;
	}
}
