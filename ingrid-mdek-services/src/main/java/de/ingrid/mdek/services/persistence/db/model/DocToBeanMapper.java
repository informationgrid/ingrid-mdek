package de.ingrid.mdek.services.persistence.db.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.utils.IngridDocument;

/**
 * Singleton encapsulating methods for mapping ingrid documents to hibernate beans.
 * 
 * @author Martin
 */
public class DocToBeanMapper implements IMapper {

	private static final Logger LOG = Logger.getLogger(DocToBeanMapper.class);

	private static DocToBeanMapper myInstance;

	/** Get The Singleton */
	public static synchronized DocToBeanMapper getInstance() {
		if (myInstance == null) {
	        myInstance = new DocToBeanMapper();
	      }
		return myInstance;
	}

	private DocToBeanMapper() {}

	/**
	 * Transfer data of passed doc to passed bean according to mapping type.
	 * @param oDoc the doc containing data to transfer
	 * @param o the bean to transfer data to, pass new Bean or null if new one
	 * @param type how much data to transfer
	 * @return the passed bean containing all mapped data
	 */
	public T01Object mapT01Object(IngridDocument oDoc, T01Object o, MappingQuantity type) {
		if (o == null) {
			o = new T01Object();
		}

		o.setId((Long) oDoc.get(MdekKeys.ID));
		o.setObjUuid((String) oDoc.get(MdekKeys.UUID));
		o.setObjClass((Integer) oDoc.get(MdekKeys.CLASS));
		o.setObjName((String) oDoc.get(MdekKeys.TITLE));

		if (type == MappingQuantity.DETAIL_ENTITY) {
			o.setObjDescr((String) oDoc.get(MdekKeys.ABSTRACT));
			
			// update related ObjAdrs
			List<IngridDocument> aDocs = (List) oDoc.get(MdekKeys.ADR_ENTITIES);
			Set<T012ObjAdr> oAs = o.getT012ObjAdrs();
			ArrayList<T012ObjAdr> oAs_unprocessed = new ArrayList<T012ObjAdr>(oAs);
			int line = 1;
			for (IngridDocument aDoc : aDocs) {
				String aUuid = (String) aDoc.get(MdekKeys.UUID);
				boolean found = false;
				for (T012ObjAdr oA : oAs) {
					if (oA.getAdrUuid().equals(aUuid)) {
						mapT012ObjAdr(oDoc, aDoc, oA, line);
						oAs_unprocessed.remove(oA);
						found = true;
						break;
					}
				}
				if (!found) {
					// add new one
					T012ObjAdr oA = mapT012ObjAdr(oDoc, aDoc, new T012ObjAdr(), line);
					oAs.add(oA);
				}
				line++;
			}
			// remove the ones not processed, will be deleted !
			for (T012ObjAdr oA : oAs_unprocessed) {
				oAs.remove(oA);
			}
		}			
			
		return o;
	}

	/**
	 * Transfer data of passed docs to passed bean.
	 * @param oDoc the doc containing object data
	 * @param aDoc the doc containing address data
	 * @param oA the bean to transfer data to, pass new Bean or null if new one
	 * @param line additional data for bean
	 * @return the passed bean containing all mapped data
	 */
	private T012ObjAdr mapT012ObjAdr(IngridDocument oDoc,
		IngridDocument aDoc,
		T012ObjAdr oA, 
		int line) 
	{
		if (oA == null) {
			oA = new T012ObjAdr();
		}
		oA.setObjId((Long) oDoc.get(MdekKeys.ID));
		oA.setAdrUuid((String) aDoc.get(MdekKeys.UUID));
		oA.setType((Integer) aDoc.get(MdekKeys.TYPE_OF_RELATION));
		oA.setLine(line);

		// TODO: mapT012ObjAdr -> fehlende Attribute !

		return oA;
	}
}
