package de.ingrid.mdek.services.persistence.db.model;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Level;
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

	/** Specials to include when object is mapped */
	public enum MappingSpecials {
		ADD_CHILD_INFO
	}
	
	private static final MappingQuantity DEFAULT_QUANTITY = MappingQuantity.BASIC;
	private static final MappingSpecials[] DEFAULT_SPECIALS = new MappingSpecials[]{};

	private static BeanToDocMapper myInstance;

	/** Get The Singleton */
	public static synchronized BeanToDocMapper getInstance() {
		if (myInstance == null) {
	        myInstance = new BeanToDocMapper();
	      }
		return myInstance;
	}

	private BeanToDocMapper() {}

	/** Generic method. Mapping method determined by reflection and has to exist ("map" + o.getClass()) ! 
	 * Map basic data and add basic specials */
	public IngridDocument map(Object o) {
		IngridDocument ret = new IngridDocument();
		try {
			Method m = getMappingMethod(o);
			ret = (IngridDocument) m.invoke(this, new Object[] { o, DEFAULT_QUANTITY, DEFAULT_SPECIALS });
		} catch (Throwable e) {
			if (LOG.isEnabledFor(Level.WARN)) {
				LOG.warn("invoking mapping method (o) failed for object " + o, e);
			}
		}
		
		return ret;
	}

	/** Generic method. Mapping method determined by reflection and has to exist ("map" + o.getClass()) !
	 * Map basic data and add given specials */
	public IngridDocument map(Object o, MappingSpecials[] specials) {
		IngridDocument ret = new IngridDocument();
		try {
			Method m = getMappingMethod(o);
			ret = (IngridDocument) m.invoke(this, new Object[] { o, DEFAULT_QUANTITY, specials });
		} catch (Throwable e) {
			if (LOG.isEnabledFor(Level.WARN)) {
				LOG.warn("invoking mapping method (o, specials) failed for object " + o, e);
			}
		}
		
		return ret;
	}

	/** Generic method. Mapping method determined by reflection and has to exist ("map" + o.getClass()) !
	 * Map data according to given quantity and add basic specials */
	public IngridDocument map(Object o, MappingQuantity howMuch) {
		IngridDocument ret = new IngridDocument();
		try {
			Method m = getMappingMethod(o);
			ret = (IngridDocument) m.invoke(this, new Object[] { o, howMuch, DEFAULT_SPECIALS });
		} catch (Throwable e) {
			if (LOG.isEnabledFor(Level.WARN)) {
				LOG.warn("invoking mapping method (o, quantity) failed for object " + o, e);
			}
		}
		
		return ret;
	}

	/** Map data according to given quantity and add given specials */
	public IngridDocument mapT01Object(Object obj, MappingQuantity howMuch, MappingSpecials[] specials) {
		IngridDocument doc = new IngridDocument();

		T01Object o = (T01Object) obj;
		doc.put(MdekKeys.UUID, o.getId());

		if (howMuch.value() >= MappingQuantity.BASIC.value()) {
			doc.put(MdekKeys.CLASS, o.getObjClass());
			doc.put(MdekKeys.TITLE, o.getObjName());
		}
		if (howMuch.value() >= MappingQuantity.MAXIMUM.value()) {
			doc.put(MdekKeys.ABSTRACT, o.getObjDescr());
		}

        List<MappingSpecials> specList = Arrays.asList(specials);
        if (specList.contains(MappingSpecials.ADD_CHILD_INFO)) {
    		// NOTICE: May cause another select !
        	boolean hasChild = false;
    		if (o.getT012ObjObjs().size() > 0) {
            	hasChild = true;
    		}
    		doc.putBoolean(MdekKeys.HAS_CHILD, hasChild);
        }

		return doc;
	}

	/** Map data according to given quantity and add given specials */
	public IngridDocument mapT02Address(Object obj, MappingQuantity howMuch, MappingSpecials[] specials) {
		IngridDocument doc = new IngridDocument();

		T02Address a = (T02Address) obj;
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
		
        List<MappingSpecials> specList = Arrays.asList(specials);
        if (specList.contains(MappingSpecials.ADD_CHILD_INFO)) {
    		// NOTICE: May cause another select !
        	boolean hasChild = false;
    		if (a.getT022AdrAdrs().size() > 0) {
            	hasChild = true;
    		}
    		doc.putBoolean(MdekKeys.HAS_CHILD, hasChild);
        }

		return doc;
	}

	private Method getMappingMethod(Object o) {
		Method ret = null;
		String fullClassName = o.getClass().toString();
		String methodName = "map" + fullClassName.substring(fullClassName.lastIndexOf(".")+1);
		Method[] methods = this.getClass().getMethods();
		for (Method m : methods) {
			if (methodName.equalsIgnoreCase(m.getName())) {
				ret = m;
				break;
			}
		}
		return ret;
	}
}
