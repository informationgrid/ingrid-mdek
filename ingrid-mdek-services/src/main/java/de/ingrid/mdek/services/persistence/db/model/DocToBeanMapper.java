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
	 * @param oDocIn the doc containing data to transfer
	 * @param oIn the bean to transfer data to, pass new Bean or null if new one
	 * @param type how much data to transfer
	 * @return the passed bean containing all mapped data
	 */
	public T01Object mapT01Object(IngridDocument oDocIn, T01Object oIn, MappingQuantity type) {
		if (oIn == null) {
			oIn = new T01Object();
		}

		oIn.setId((Long) oDocIn.get(MdekKeys.ID));
		oIn.setObjUuid((String) oDocIn.get(MdekKeys.UUID));
		oIn.setObjClass((Integer) oDocIn.get(MdekKeys.CLASS));
		oIn.setObjName((String) oDocIn.get(MdekKeys.TITLE));

		if (type == MappingQuantity.DETAIL_ENTITY) {
			oIn.setObjDescr((String) oDocIn.get(MdekKeys.ABSTRACT));
			String creationDate = (String) oDocIn.get(MdekKeys.DATE_OF_CREATION);
			if (creationDate != null) {
				oIn.setCreateTime(creationDate);				
			}
			oIn.setModTime((String) oDocIn.get(MdekKeys.DATE_OF_LAST_MODIFICATION));
			
			// update related ObjAdrs
			updateT012ObjAdrs(oDocIn, oIn);

			// update related ObjObjs (Querverweise !!!)
			updateT012ObjObjs(oDocIn, oIn);
		}			
			
		return oIn;
	}

	/**
	 * Transfer data of passed docs to passed bean.
	 * @param oFrom the from object bean
	 * @param oDocTo the to doc containing to object data
	 * @param oO the bean to transfer data to, pass new Bean or null if new one
	 * @param line additional line data for bean
	 * @return the passed bean containing all mapped data
	 */
	public T012ObjObj mapT012ObjObj(T01Object oFrom,
		IngridDocument oDocTo,
		T012ObjObj oO, 
		int line) 
	{
		if (oO == null) {
			oO = new T012ObjObj();
		}
		// set parent bean and id !
		oO.setFromT01Object(oFrom);
		oO.setObjectFromUuid((String) oFrom.getObjUuid());
		oO.setObjectToUuid((String) oDocTo.get(MdekKeys.UUID));
		oO.setType((Integer) oDocTo.get(MdekKeys.RELATION_TYPE));
		oO.setDescr((String) oDocTo.get(MdekKeys.RELATION_DESCRIPTION));
		oO.setLine(line);

		// TODO: mapT012ObjObj -> fehlende Attribute !

		return oO;
	}

	/**
	 * Transfer data of passed docs to passed bean.
	 * @param oFrom  the from object bean
	 * @param aDocTo the to doc containing to address data
	 * @param oA the bean to transfer data to, pass new Bean or null if new one
	 * @param line additional line data for bean
	 * @return the passed bean containing all mapped data
	 */
	private T012ObjAdr mapT012ObjAdr(T01Object oFrom,
		IngridDocument aDocTo,
		T012ObjAdr oA, 
		int line) 
	{
		if (oA == null) {
			oA = new T012ObjAdr();
		}
		// set parent bean and id !
		oA.setT01Object(oFrom);
		oA.setObjId(oFrom.getId());
		oA.setAdrUuid((String) aDocTo.get(MdekKeys.UUID));
		oA.setType((Integer) aDocTo.get(MdekKeys.RELATION_TYPE));
		oA.setLine(line);

		// TODO: mapT012ObjAdr -> fehlende Attribute !

		return oA;
	}

	private void updateT012ObjAdrs(IngridDocument oDocIn, T01Object oIn) {
		List<IngridDocument> aDocsTo = (List) oDocIn.get(MdekKeys.ADR_ENTITIES);
		if (aDocsTo == null) {
			aDocsTo = new ArrayList<IngridDocument>(0);
		}
		Set<T012ObjAdr> oAs = oIn.getT012ObjAdrs();
		ArrayList<T012ObjAdr> oAs_unprocessed = new ArrayList<T012ObjAdr>(oAs);
		int line = 1;
		for (IngridDocument aDocTo : aDocsTo) {
			String aUuidTo = (String) aDocTo.get(MdekKeys.UUID);
			boolean found = false;
			for (T012ObjAdr oA : oAs) {
				if (oA.getAdrUuid().equals(aUuidTo)) {
					mapT012ObjAdr(oIn, aDocTo, oA, line);
					oAs_unprocessed.remove(oA);
					found = true;
					break;
				}
			}
			if (!found) {
				// add new one
				T012ObjAdr oA = mapT012ObjAdr(oIn, aDocTo, new T012ObjAdr(), line);
				oAs.add(oA);
			}
			line++;
		}
		// remove the ones not processed, will be deleted !
		for (T012ObjAdr oA : oAs_unprocessed) {
			oAs.remove(oA);
		}		
	}

	private void updateT012ObjObjs(IngridDocument oDocIn, T01Object oIn) {
		List<IngridDocument> oDocsTo = (List) oDocIn.get(MdekKeys.OBJ_ENTITIES);
		if (oDocsTo == null) {
			oDocsTo = new ArrayList<IngridDocument>(0);
		}
		Set<T012ObjObj> oOs = oIn.getT012ObjObjs();
		ArrayList<T012ObjObj> oOs_unprocessed = new ArrayList<T012ObjObj>(oOs);
		int line = 1;
		for (IngridDocument oDocTo : oDocsTo) {
			String oToUuid = (String) oDocTo.get(MdekKeys.UUID);
			boolean found = false;
			for (T012ObjObj oO : oOs) {
				if (oO.getType() != T012ObjObjRelationType.QUERVERWEIS.getDbValue().intValue()) {
					LOG.error("Passed associated object not of Type \"Querverweis\" ! We ignore object ");
					oOs_unprocessed.remove(oO);
					continue;
				}
				if (oO.getObjectToUuid().equals(oToUuid)) {
					mapT012ObjObj(oIn, oDocTo, oO, line);
					oOs_unprocessed.remove(oO);
					found = true;
					break;
				}
			}
			if (!found) {
				// add new one
				T012ObjObj oO = mapT012ObjObj(oIn, oDocTo, new T012ObjObj(), line);
				oOs.add(oO);
			}
			line++;
		}
		// remove the ones not processed, will be deleted !
		for (T012ObjObj oO : oOs_unprocessed) {
			oOs.remove(oO);
		}		
	}
}
