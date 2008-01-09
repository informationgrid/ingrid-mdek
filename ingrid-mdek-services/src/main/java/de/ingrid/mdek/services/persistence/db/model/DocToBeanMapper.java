package de.ingrid.mdek.services.persistence.db.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
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
	public static synchronized DocToBeanMapper getInstance(DaoFactory daoFactory) {
		if (myInstance == null) {
	        myInstance = new DocToBeanMapper(daoFactory);
	      }
		return myInstance;
	}

	private DocToBeanMapper(DaoFactory daoFactory) {
//		daoT012ObjObj = daoFactory.getT012ObjObjDao();
	}

	/**
	 * Transfer data of passed doc to passed bean according to mapping type.
	 * @param oDocIn the doc containing data to transfer
	 * @param oIn the bean to transfer data to !
	 * @param type how much data to transfer
	 * @return the passed bean containing all mapped data
	 */
	public T01Object mapT01Object(IngridDocument oDocIn, T01Object oIn, MappingQuantity type) {
		oIn.setObjUuid((String) oDocIn.get(MdekKeys.UUID));
		oIn.setObjClass((Integer) oDocIn.get(MdekKeys.CLASS));
		oIn.setObjName((String) oDocIn.get(MdekKeys.TITLE));
		oIn.setWorkState((String) oDocIn.get(MdekKeys.WORK_STATE));
		String creationDate = (String) oDocIn.get(MdekKeys.DATE_OF_CREATION);
		if (creationDate != null) {
			oIn.setCreateTime(creationDate);				
		}
		oIn.setModTime((String) oDocIn.get(MdekKeys.DATE_OF_LAST_MODIFICATION));

		if (type == MappingQuantity.DETAIL_ENTITY) {
			oIn.setDatasetAlternateName((String) oDocIn.get(MdekKeys.DATASET_ALTERNATE_NAME));
			oIn.setObjDescr((String) oDocIn.get(MdekKeys.ABSTRACT));

			oIn.setVerticalExtentMinimum((Double) oDocIn.get(MdekKeys.VERTICAL_EXTENT_MINIMUM));
			oIn.setVerticalExtentMaximum((Double) oDocIn.get(MdekKeys.VERTICAL_EXTENT_MAXIMUM));
			oIn.setVerticalExtentUnit((Integer) oDocIn.get(MdekKeys.VERTICAL_EXTENT_UNIT));
			oIn.setVerticalExtentVdatum((Integer) oDocIn.get(MdekKeys.VERTICAL_EXTENT_VDATUM));
			oIn.setLocDescr((String) oDocIn.get(MdekKeys.DESCRIPTION_OF_SPATIAL_DOMAIN));

			oIn.setTimeType((String) oDocIn.get(MdekKeys.TIME_TYPE));
			oIn.setTimeFrom((String) oDocIn.get(MdekKeys.BEGINNING_DATE));
			oIn.setTimeTo((String) oDocIn.get(MdekKeys.ENDING_DATE));
			oIn.setTimeStatus((Integer) oDocIn.get(MdekKeys.TIME_STATUS));
			oIn.setTimePeriod((Integer) oDocIn.get(MdekKeys.TIME_PERIOD));
			oIn.setTimeInterval((String) oDocIn.get(MdekKeys.TIME_STEP));
			oIn.setTimeAlle((String) oDocIn.get(MdekKeys.TIME_SCALE));
			oIn.setTimeDescr((String) oDocIn.get(MdekKeys.DESCRIPTION_OF_TEMPORAL_DOMAIN));
			
			oIn.setMetadataLanguageCode((String) oDocIn.get(MdekKeys.METADATA_LANGUAGE));
			oIn.setDataLanguageCode((String) oDocIn.get(MdekKeys.DATA_LANGUAGE));
			oIn.setPublishId((Integer) oDocIn.get(MdekKeys.PUBLICATION_CONDITION));
			oIn.setInfoNote((String) oDocIn.get(MdekKeys.DATASET_INTENSIONS));
			oIn.setDatasetUsage((String) oDocIn.get(MdekKeys.DATASET_USAGE));

			oIn.setOrderingInstructions((String) oDocIn.get(MdekKeys.ORDERING_INSTRUCTIONS));
			oIn.setAvailAccessNote((String) oDocIn.get(MdekKeys.USE_CONSTRAINTS));
			oIn.setFees((String) oDocIn.get(MdekKeys.FEES));
			

			// update related object references (Querverweise)
			updateObjectReferences(oDocIn, oIn);

			// update related ObjAdrs
			updateT012ObjAdrs(oDocIn, oIn);
		}			

		// TODO: Extent MappingQuantity.FULL_ENTITY for copy

		return oIn;
	}

	/**
	 * Transfer data of passed doc to passed bean.
	 * @param oFromId id of from object
	 * @param oToDoc the to doc containing to object data
	 * @param oO the bean to transfer data to, pass new Bean or null if new one
	 * @param line additional line data for bean
	 * @return the passed bean containing all mapped data
	 */
	public ObjectReference mapObjectReference(long oFromId,
		IngridDocument oToDoc,
		ObjectReference oRef, 
		int line) 
	{
		oRef.setObjFromId(oFromId);
		oRef.setObjToUuid((String) oToDoc.get(MdekKeys.UUID));
		oRef.setDescr((String) oToDoc.get(MdekKeys.RELATION_DESCRIPTION));
		oRef.setLine(line);
		oRef.setSpecialName((String) oToDoc.get(MdekKeys.RELATION_TYPE_NAME));
		oRef.setSpecialRef((Integer) oToDoc.get(MdekKeys.RELATION_TYPE_REF));

		// TODO: Extent MappingQuantity.FULL_ENTITY for copy

		return oRef;
	}

	/**
	 * Transfer data of passed doc to passed bean.
	 * @param oFromId id of from object
	 * @param aToDoc the to doc containing to address data
	 * @param oA the bean to transfer data to !
	 * @param line additional line data for bean
	 * @return the passed bean containing all mapped data
	 */
	private T012ObjAdr mapT012ObjAdr(long oFromId,
		IngridDocument aToDoc,
		T012ObjAdr oA, 
		int line) 
	{
		oA.setObjId(oFromId);
		oA.setAdrUuid((String) aToDoc.get(MdekKeys.UUID));
		oA.setType((Integer) aToDoc.get(MdekKeys.RELATION_TYPE_ID));
		oA.setSpecialName((String) aToDoc.get(MdekKeys.RELATION_TYPE_NAME));
		oA.setSpecialRef((Integer) aToDoc.get(MdekKeys.RELATION_TYPE_REF));
		oA.setLine(line);

		// TODO: Extent MappingQuantity.FULL_ENTITY for copy

		return oA;
	}

	private void updateObjectReferences(IngridDocument oDocIn, T01Object oIn) {
		List<IngridDocument> oDocsTo = (List) oDocIn.get(MdekKeys.OBJ_REFERENCES_TO);
		if (oDocsTo == null) {
			oDocsTo = new ArrayList<IngridDocument>(0);
		}
		Set<ObjectReference> oRefs = oIn.getObjectReferences();
		ArrayList<ObjectReference> oRefs_unprocessed = new ArrayList<ObjectReference>(oRefs);
		int line = 1;
		for (IngridDocument oDocTo : oDocsTo) {
			String oToUuid = (String) oDocTo.get(MdekKeys.UUID);
			boolean found = false;
			for (ObjectReference oRef : oRefs) {
				if (oRef.getObjToUuid().equals(oToUuid)) {
					mapObjectReference(oIn.getId(), oDocTo, oRef, line);
					oRefs_unprocessed.remove(oRef);
					found = true;
					break;
				}
			}
			if (!found) {
				// add new one
				ObjectReference oRef = mapObjectReference(oIn.getId(), oDocTo, new ObjectReference(), line);
				oRefs.add(oRef);
			}
			line++;
		}
		// remove the ones not processed, will be deleted by hibernate (delete-orphan set in parent)
		for (ObjectReference oR : oRefs_unprocessed) {
			oRefs.remove(oR);
		}		
	}

	private void updateT012ObjAdrs(IngridDocument oDocIn, T01Object oIn) {
		List<IngridDocument> aDocsTo = (List) oDocIn.get(MdekKeys.ADR_REFERENCES_TO);
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
					mapT012ObjAdr(oIn.getId(), aDocTo, oA, line);
					oAs_unprocessed.remove(oA);
					found = true;
					break;
				}
			}
			if (!found) {
				// add new one
				T012ObjAdr oA = mapT012ObjAdr(oIn.getId(), aDocTo, new T012ObjAdr(), line);
				oAs.add(oA);
			}
			line++;
		}
		// remove the ones not processed, will be deleted by hibernate (delete-orphan set in parent)
		for (T012ObjAdr oA : oAs_unprocessed) {
			oAs.remove(oA);
		}		
	}
}
