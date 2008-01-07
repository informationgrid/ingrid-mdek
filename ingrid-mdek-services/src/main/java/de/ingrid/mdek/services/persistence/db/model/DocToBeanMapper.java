package de.ingrid.mdek.services.persistence.db.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.dao.IT012ObjObjDao;
import de.ingrid.utils.IngridDocument;

/**
 * Singleton encapsulating methods for mapping ingrid documents to hibernate beans.
 * 
 * @author Martin
 */
public class DocToBeanMapper implements IMapper {

	private static final Logger LOG = Logger.getLogger(DocToBeanMapper.class);

	private static DocToBeanMapper myInstance;

	private IT012ObjObjDao daoT012ObjObj;

	/** Get The Singleton */
	public static synchronized DocToBeanMapper getInstance(DaoFactory daoFactory) {
		if (myInstance == null) {
	        myInstance = new DocToBeanMapper(daoFactory);
	      }
		return myInstance;
	}

	private DocToBeanMapper(DaoFactory daoFactory) {
		daoT012ObjObj = daoFactory.getT012ObjObjDao();
	}

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
		oIn.setWorkState((String) oDocIn.get(MdekKeys.WORK_STATE));

		if (type == MappingQuantity.DETAIL_ENTITY) {
			oIn.setDatasetAlternateName((String) oDocIn.get(MdekKeys.DATASET_ALTERNATE_NAME));
			oIn.setObjDescr((String) oDocIn.get(MdekKeys.ABSTRACT));
			String creationDate = (String) oDocIn.get(MdekKeys.DATE_OF_CREATION);
			if (creationDate != null) {
				oIn.setCreateTime(creationDate);				
			}
			oIn.setModTime((String) oDocIn.get(MdekKeys.DATE_OF_LAST_MODIFICATION));
			oIn.setVerticalExtentMinimum((Double) oDocIn.get(MdekKeys.VERTICAL_EXTENT_MINIMUM));
			oIn.setVerticalExtentMaximum((Double) oDocIn.get(MdekKeys.VERTICAL_EXTENT_MAXIMUM));
			oIn.setVerticalExtentUnit((Integer) oDocIn.get(MdekKeys.VERTICAL_EXTENT_UNIT));
			oIn.setVerticalExtentVdatum((Integer) oDocIn.get(MdekKeys.VERTICAL_EXTENT_VDATUM));
			oIn.setLocDescr((String) oDocIn.get(MdekKeys.DESCRIPTION_OF_SPATIAL_DOMAIN));


			// update related ObjAdrs
			updateT012ObjAdrs(oDocIn, oIn);

			// update related ObjObjs (Querverweise !!!)
			updateT012ObjObjs(oDocIn, oIn);
		}			
			
		return oIn;
	}

	/**
	 * Transfer data of passed docs to passed bean.
	 * @param oFromUuid uuid of from object
	 * @param oToDoc the to doc containing to object data
	 * @param oO the bean to transfer data to, pass new Bean or null if new one
	 * @param line additional line data for bean
	 * @return the passed bean containing all mapped data
	 */
	public T012ObjObj mapT012ObjObj(String oFromUuid,
		IngridDocument oToDoc,
		T012ObjObj oO, 
		int line) 
	{
		if (oO == null) {
			oO = new T012ObjObj();
		}
		// set parent bean and id !
		oO.setObjectFromUuid(oFromUuid);
		oO.setObjectToUuid((String) oToDoc.get(MdekKeys.UUID));
		oO.setType((Integer) oToDoc.get(MdekKeys.RELATION_TYPE));
		oO.setDescr((String) oToDoc.get(MdekKeys.RELATION_DESCRIPTION));
		oO.setLine(line);

		// TODO: mapT012ObjObj -> fehlende Attribute !

		return oO;
	}

	/**
	 * Transfer data of passed docs to passed bean.
	 * @param oFromId id of from object
	 * @param aToDoc the to doc containing to address data
	 * @param oA the bean to transfer data to, pass new Bean or null if new one
	 * @param line additional line data for bean
	 * @return the passed bean containing all mapped data
	 */
	private T012ObjAdr mapT012ObjAdr(long oFromId,
		IngridDocument aToDoc,
		T012ObjAdr oA, 
		int line) 
	{
		if (oA == null) {
			oA = new T012ObjAdr();
		}
		// set parent bean and id !
		oA.setObjId(oFromId);
		oA.setAdrUuid((String) aToDoc.get(MdekKeys.UUID));
		oA.setType((Integer) aToDoc.get(MdekKeys.RELATION_TYPE));
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
					mapT012ObjAdr(oDocIn.getLong(MdekKeys.ID), aDocTo, oA, line);
					oAs_unprocessed.remove(oA);
					found = true;
					break;
				}
			}
			if (!found) {
				// add new one
				T012ObjAdr oA = mapT012ObjAdr(oDocIn.getLong(MdekKeys.ID), aDocTo, new T012ObjAdr(), line);
				oAs.add(oA);
			}
			line++;
		}
		// remove the ones not processed, will be deleted by hibernate (delete-orphan set in parent)
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
					mapT012ObjObj((String)oDocIn.get(MdekKeys.UUID), oDocTo, oO, line);
					oOs_unprocessed.remove(oO);
					found = true;
					break;
				}
			}
			if (!found) {
				// add new one
				T012ObjObj oO = mapT012ObjObj((String)oDocIn.get(MdekKeys.UUID), oDocTo, new T012ObjObj(), line);
				oOs.add(oO);
			}
			line++;
		}
		// remove the ones not processed AND DELETE !!! (no delete-orphan due to different Struktur-/Querverweise handling)
		for (T012ObjObj oO : oOs_unprocessed) {
			if (oO.getType() == T012ObjObjRelationType.QUERVERWEIS.getDbValue()) {
				oOs.remove(oO);
				daoT012ObjObj.makeTransient(oO);				
			}
		}		
	}
}
