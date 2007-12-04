package de.ingrid.mdek.services.persistence.db.model;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.utils.IngridDocument;

/**
 * Singleton encapsulating methods for mapping hibernate beans to ingrid documents.
 * 
 * @author Martin
 */
public class BeanToDocMapper {

	private static BeanToDocMapper myInstance;
	
	public enum MappingQuantity {	
		MINIMUM(0),
		BASIC(3),
		AVERAGE(5),
		MAXIMUM(10);

		public int value;

		MappingQuantity(int value) {
			this.value = value;
		}
		protected int value() {
			return value;
		}
	}

	public static synchronized BeanToDocMapper getInstance() {
		if (myInstance == null) {
	        myInstance = new BeanToDocMapper();
	      }
		return myInstance;
	}

	private BeanToDocMapper() {}

	public IngridDocument mapT01Object(T01Object o) {
		return mapT01Object(o, MappingQuantity.BASIC);
	}
	public IngridDocument mapT01Object(T01Object o, MappingQuantity howMuch) {
		IngridDocument doc = new IngridDocument();

		doc.put(MdekKeys.UUID, o.getId());

		if (howMuch.value() >= MappingQuantity.BASIC.value()) {
			doc.put(MdekKeys.CLASS, o.getObjClass());
			doc.put(MdekKeys.TITLE, o.getObjName());
		}
		if (howMuch.value() >= MappingQuantity.MAXIMUM.value()) {
			doc.put(MdekKeys.ABSTRACT, o.getObjDescr());
		}
		
		return doc;
	}

	public IngridDocument mapT02Address(T02Address a) {
		return mapT02Address(a, MappingQuantity.BASIC);
	}
	public IngridDocument mapT02Address(T02Address a, MappingQuantity howMuch) {
		IngridDocument doc = new IngridDocument();

		doc.put(MdekKeys.UUID, a.getId());
		if (howMuch.value() >= MappingQuantity.BASIC.value()) {
			doc.put(MdekKeys.CLASS, a.getTyp());
			doc.put(MdekKeys.ORGANISATION, a.getInstitution());
			doc.put(MdekKeys.NAME, a.getLastname());
			doc.put(MdekKeys.GIVEN_NAME, a.getFirstname());
			doc.put(MdekKeys.TITLE_OR_FUNCTION, a.getTitle());
		}
		if (howMuch.value() >= MappingQuantity.AVERAGE.value()) {
			doc.put(MdekKeys.STREET, a.getStreet());
			doc.put(MdekKeys.POSTAL_CODE_OF_COUNTRY, a.getStateId());
			doc.put(MdekKeys.CITY, a.getCity());
			doc.put(MdekKeys.POST_BOX_POSTAL_CODE, a.getPostboxPc());
			doc.put(MdekKeys.POST_BOX, a.getPostbox());
			doc.put(MdekKeys.FUNCTION, a.getJob());
		}
		if (howMuch.value() >= MappingQuantity.MAXIMUM.value()) {
			doc.put(MdekKeys.NAME_FORM, a.getAddress());
			doc.put(MdekKeys.ADDRESS_DESCRIPTION, a.getDescr());			
		}
		
		return doc;
	}
}
