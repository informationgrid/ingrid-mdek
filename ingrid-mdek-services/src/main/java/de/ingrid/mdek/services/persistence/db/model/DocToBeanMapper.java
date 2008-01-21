package de.ingrid.mdek.services.persistence.db.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.IEntity;
import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.dao.ISpatialRefSnsDao;
import de.ingrid.mdek.services.persistence.db.dao.ISpatialRefValueDao;
import de.ingrid.utils.IngridDocument;

/**
 * Singleton encapsulating methods for mapping ingrid documents to hibernate beans.
 * 
 * @author Martin
 */
public class DocToBeanMapper implements IMapper {

	private static final Logger LOG = Logger.getLogger(DocToBeanMapper.class);

	private static DocToBeanMapper myInstance;
	
	private ISpatialRefSnsDao daoSpatialRefSns;
	private ISpatialRefValueDao daoSpatialRefValue;
	private IGenericDao<IEntity> daoSpatialReference;
	private IGenericDao<IEntity> daoT012ObjAdr;
	private IGenericDao<IEntity> daoObjectReference;
	private IGenericDao<IEntity> daoT017UrlRef;
	private IGenericDao<IEntity> daoT0113DatasetReference;
	private IGenericDao<IEntity> daoT014InfoImpart;
	private IGenericDao<IEntity> daoT015Legist;
	private IGenericDao<IEntity> daoT0110AvailFormat;
	private IGenericDao<IEntity> daoT0112MediaOption;

	/** Get The Singleton */
	public static synchronized DocToBeanMapper getInstance(DaoFactory daoFactory) {
		if (myInstance == null) {
	        myInstance = new DocToBeanMapper(daoFactory);
	      }
		return myInstance;
	}

	private DocToBeanMapper(DaoFactory daoFactory) {
		daoSpatialRefSns = daoFactory.getSpatialRefSnsDao();
		daoSpatialRefValue = daoFactory.getSpatialRefValueDao();
		daoSpatialReference = daoFactory.getDao(SpatialReference.class);
		daoT012ObjAdr = daoFactory.getDao(T012ObjAdr.class);
		daoObjectReference = daoFactory.getDao(ObjectReference.class);
		daoT017UrlRef = daoFactory.getDao(T017UrlRef.class);
		daoT0113DatasetReference = daoFactory.getDao(T0113DatasetReference.class);
		daoT014InfoImpart = daoFactory.getDao(T014InfoImpart.class);
		daoT015Legist = daoFactory.getDao(T015Legist.class);
		daoT0110AvailFormat = daoFactory.getDao(T0110AvailFormat.class);
		daoT0112MediaOption = daoFactory.getDao(T0112MediaOption.class);
	}

	/**
	 * Transfer data of passed doc to passed bean according to mapping type.
	 */
	public ObjectNode mapObjectNode(IngridDocument oDocIn, ObjectNode oNodeIn) {
		oNodeIn.setObjUuid((String) oDocIn.get(MdekKeys.UUID));
		if (oDocIn.containsKey(MdekKeys.PARENT_UUID)) {
			// NOTICE: parent may be null, then root node !
			String parentUuid = (String) oDocIn.get(MdekKeys.PARENT_UUID);
			oNodeIn.setFkObjUuid(parentUuid);
		}

		return oNodeIn;
	}

	/**
	 * Transfer data of passed doc to passed bean according to mapping type.
	 */
	public T01Object mapT01Object(IngridDocument oDocIn, T01Object oIn, MappingQuantity howMuch) {

		oIn.setObjUuid((String) oDocIn.get(MdekKeys.UUID));
		oIn.setObjClass((Integer) oDocIn.get(MdekKeys.CLASS));
		oIn.setObjName((String) oDocIn.get(MdekKeys.TITLE));
		oIn.setWorkState((String) oDocIn.get(MdekKeys.WORK_STATE));
		String creationDate = (String) oDocIn.get(MdekKeys.DATE_OF_CREATION);
		if (creationDate != null) {
			oIn.setCreateTime(creationDate);				
		}
		oIn.setModTime((String) oDocIn.get(MdekKeys.DATE_OF_LAST_MODIFICATION));

		if (howMuch == MappingQuantity.DETAIL_ENTITY ||
				howMuch == MappingQuantity.COPY_ENTITY)
		{
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

			// update associations
			updateObjectReferences(oDocIn, oIn);
			updateT012ObjAdrs(oDocIn, oIn, howMuch);
			updateSpatialReferences(oDocIn, oIn);
			updateT017UrlRefs(oDocIn, oIn);
			updateT0113DatasetReferences(oDocIn, oIn);
			updateT014InfoImparts(oDocIn, oIn);
			updateT015Legists(oDocIn, oIn);
			updateT0110AvailFormats(oDocIn, oIn);
			updateT0112MediaOptions(oDocIn, oIn);
		}			

		if (howMuch == MappingQuantity.COPY_ENTITY) {
			oIn.setOrgObjId((String) oDocIn.get(MdekKeys.ORIGINAL_CONTROL_IDENTIFIER));
			oIn.setRoot((Integer) oDocIn.get(MdekKeys.NO_OF_PARENTS));
			oIn.setCatId((Long) oDocIn.get(MdekKeys.CATALOGUE_IDENTIFIER));
			oIn.setDatasetCharacterSet((Integer) oDocIn.get(MdekKeys.DATASET_CHARACTER_SET));
			oIn.setMetadataCharacterSet((Integer) oDocIn.get(MdekKeys.METADATA_CHARACTER_SET));
			oIn.setMetadataStandardName((String) oDocIn.get(MdekKeys.METADATA_STANDARD_NAME));
			oIn.setMetadataStandardVersion((String) oDocIn.get(MdekKeys.METADATA_STANDARD_VERSION));
			oIn.setLastexportTime((String) oDocIn.get(MdekKeys.LASTEXPORT_TIME));
			oIn.setExpiryTime((String) oDocIn.get(MdekKeys.EXPIRY_TIME));
			oIn.setWorkVersion((Integer) oDocIn.get(MdekKeys.WORK_VERSION));
			oIn.setMarkDeleted((String) oDocIn.get(MdekKeys.MARK_DELETED));
			oIn.setModUuid((String) oDocIn.get(MdekKeys.MOD_UUID));
			oIn.setResponsibleUuid((String) oDocIn.get(MdekKeys.RESPONSIBLE_UUID));
		}

		return oIn;
	}

	/**
	 * Transfer data of passed doc to passed bean.
	 * @param oFrom from object
	 * @param oToDoc the to doc containing to object data
	 * @param oO the bean to transfer data to, pass new Bean or null if new one
	 * @param line additional line data for bean
	 * @return the passed bean containing all mapped data
	 */
	private ObjectReference mapObjectReference(T01Object oFrom,
		IngridDocument oToDoc,
		ObjectReference oRef, 
		int line) 
	{
		oRef.setObjFromId(oFrom.getId());
		oRef.setObjToUuid((String) oToDoc.get(MdekKeys.UUID));
		oRef.setLine(line);
		oRef.setSpecialName((String) oToDoc.get(MdekKeys.RELATION_TYPE_NAME));
		oRef.setSpecialRef((Integer) oToDoc.get(MdekKeys.RELATION_TYPE_REF));
		oRef.setDescr((String) oToDoc.get(MdekKeys.RELATION_DESCRIPTION));

		return oRef;
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
					mapObjectReference(oIn, oDocTo, oRef, line);
					oRefs_unprocessed.remove(oRef);
					found = true;
					break;
				}
			}
			if (!found) {
				// add new one
				ObjectReference oRef = mapObjectReference(oIn, oDocTo, new ObjectReference(), line);
				oRefs.add(oRef);
			}
			line++;
		}
		// remove the ones not processed, will be deleted by hibernate (delete-orphan set in parent)
		for (ObjectReference oR : oRefs_unprocessed) {
			oRefs.remove(oR);
			// delete-orphan doesn't work !!!?????
			daoObjectReference.makeTransient(oR);
		}		
	}

	/**
	 * Transfer data of passed doc to passed bean.
	 * @param oFrom from object
	 * @param aToDoc the to doc containing to address data
	 * @param oA the bean to transfer data to !
	 * @param line additional line data for bean
	 * @param howMuch how much data to transfer
	 * @return the passed bean containing all mapped data
	 */
	private T012ObjAdr mapT012ObjAdr(T01Object oFrom,
		IngridDocument aToDoc,
		T012ObjAdr oA, 
		int line,
		MappingQuantity howMuch) 
	{
		oA.setObjId(oFrom.getId());
		oA.setT01Object(oFrom);
		oA.setAdrUuid((String) aToDoc.get(MdekKeys.UUID));
		oA.setType((Integer) aToDoc.get(MdekKeys.RELATION_TYPE_ID));
		oA.setSpecialName((String) aToDoc.get(MdekKeys.RELATION_TYPE_NAME));
		oA.setSpecialRef((Integer) aToDoc.get(MdekKeys.RELATION_TYPE_REF));
		oA.setLine(line);

		if (howMuch == MappingQuantity.COPY_ENTITY) {
			oA.setModTime((String) aToDoc.get(MdekKeys.RELATION_DATE_OF_LAST_MODIFICATION));
		} else {
			// set modification time only when creating a new object !
			if (oA.getId() == null) {
				String currentTime = MdekUtils.dateToTimestamp(new Date()); 
				oA.setModTime(currentTime);				
			}
		}

		return oA;
	}

	private void updateT012ObjAdrs(IngridDocument oDocIn, T01Object oIn, MappingQuantity howMuch) {
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
					mapT012ObjAdr(oIn, aDocTo, oA, line, howMuch);
					oAs_unprocessed.remove(oA);
					found = true;
					break;
				}
			}
			if (!found) {
				// add new one
				T012ObjAdr oA = mapT012ObjAdr(oIn, aDocTo, new T012ObjAdr(), line, howMuch);
				oAs.add(oA);
			}
			line++;
		}
		// remove the ones not processed, will be deleted by hibernate (delete-orphan set in parent)
		for (T012ObjAdr oA : oAs_unprocessed) {
			oAs.remove(oA);
			// delete-orphan doesn't work !!!?????
			daoT012ObjAdr.makeTransient(oA);
		}		
	}

	/**
	 * Transfer data to passed bean.
	 */
	private SpatialReference mapSpatialReference(T01Object oFrom,
		SpatialRefValue spRefValue,
		SpatialReference spRef,
		int line) 
	{
		spRef.setObjId(oFrom.getId());
		spRef.setSpatialRefValue(spRefValue);			
		spRef.setSpatialRefId(spRefValue.getId());
		spRef.setLine(line);

		return spRef;
	}

	/**
	 * Transfer data to passed bean.
	 */
	private SpatialRefValue mapSpatialRefValue(SpatialRefSns spRefSns,
		IngridDocument locDoc,
		SpatialRefValue spRefValue) 
	{
		spRefValue.setName((String) locDoc.get(MdekKeys.LOCATION_NAME));
		spRefValue.setType((String) locDoc.get(MdekKeys.LOCATION_TYPE));
		spRefValue.setNativekey((String) locDoc.get(MdekKeys.LOCATION_CODE));
		spRefValue.setX1((Double) locDoc.get(MdekKeys.WEST_BOUNDING_COORDINATE));
		spRefValue.setY1((Double) locDoc.get(MdekKeys.SOUTH_BOUNDING_COORDINATE));
		spRefValue.setX2((Double) locDoc.get(MdekKeys.EAST_BOUNDING_COORDINATE));
		spRefValue.setY2((Double) locDoc.get(MdekKeys.NORTH_BOUNDING_COORDINATE));

		Long spRefSnsId = null;
		if (spRefSns != null) {
			spRefSnsId = spRefSns.getId();			
		}
		spRefValue.setSpatialRefSns(spRefSns);			
		spRefValue.setSpatialRefSnsId(spRefSnsId);

		return spRefValue;
	}

	private void updateSpatialReferences(IngridDocument oDocIn, T01Object oIn) {
		List<IngridDocument> locList = (List) oDocIn.get(MdekKeys.LOCATIONS);
		if (locList == null) {
			locList = new ArrayList<IngridDocument>(0);
		}
		Set<SpatialReference> spatialRefs = oIn.getSpatialReferences();
		ArrayList<SpatialReference> spatialRefs_unprocessed = new ArrayList<SpatialReference>(spatialRefs);
		int line = 1;
		for (IngridDocument loc : locList) {
			String locName = (String) loc.get(MdekKeys.LOCATION_NAME);
			String locName_notNull = (locName == null) ? "" : locName;
			String locType = (String) loc.get(MdekKeys.LOCATION_TYPE);
			String locSnsId = (String) loc.get(MdekKeys.LOCATION_SNS_ID);
			String locSnsId_notNull = (locSnsId == null) ? "" : locSnsId;
			String locCode = (String) loc.get(MdekKeys.LOCATION_CODE);
			String locCode_notNull = (locCode == null) ? "" : locCode;
			boolean found = false;
			for (SpatialReference spRef : spatialRefs) {
				SpatialRefValue spRefValue = spRef.getSpatialRefValue();
				if (spRefValue != null) {
					SpatialRefSns spRefSns = spRefValue.getSpatialRefSns();

					String refName_notNull = (spRefValue.getName() == null) ? "" : spRefValue.getName();
					String refType = spRefValue.getType();
					String refSnsId_notNull = (spRefSns == null) ? "" : spRefSns.getSnsId();
					String refCode_notNull = (spRefValue.getNativekey() == null) ? "" : spRefValue.getNativekey();
					if (locName_notNull.equals(refName_notNull) &&
						locType.equals(refType) &&
						locSnsId_notNull.equals(refSnsId_notNull) &&
						locCode_notNull.equals(refCode_notNull))
					{
						mapSpatialRefValue(spRefSns, loc, spRefValue);
						// update line
						spRef.setLine(line);
						spatialRefs_unprocessed.remove(spRef);
						found = true;
						break;
					}					
				}
			}
			if (!found) {
				// add new one
				
				// first load/create SpatialRefSns
				SpatialRefSns spRefSns = null;
				if (locSnsId != null) {
					spRefSns = daoSpatialRefSns.loadOrCreate(locSnsId);
				}

				// then load/create SpatialRefValue
				SpatialRefValue spRefValue = daoSpatialRefValue.loadOrCreate(locType, locName, spRefSns, locCode, oIn.getId());
				mapSpatialRefValue(spRefSns, loc, spRefValue);

				// then create SpatialReference
				SpatialReference spRef = new SpatialReference();
				mapSpatialReference(oIn, spRefValue, spRef, line);
				spatialRefs.add(spRef);
			}
			line++;
		}
		// remove the ones not processed, will be deleted by hibernate (delete-orphan set in parent)
		for (SpatialReference spRef : spatialRefs_unprocessed) {
			spatialRefs.remove(spRef);
			// delete-orphan doesn't work !!!?????
			daoSpatialReference.makeTransient(spRef);
		}		
	}

	/**
	 * Transfer data to passed bean.
	 */
	private T017UrlRef mapT017UrlRef(T01Object oFrom,
		IngridDocument urlDoc,
		T017UrlRef urlRef, 
		int line) 
	{
		urlRef.setObjId(oFrom.getId());
		urlRef.setUrlLink((String) urlDoc.get(MdekKeys.LINKAGE_URL));
		urlRef.setSpecialRef((Integer) urlDoc.get(MdekKeys.LINKAGE_REFERENCE_ID));
		urlRef.setSpecialName((String) urlDoc.get(MdekKeys.LINKAGE_REFERENCE));
		urlRef.setDatatype((String) urlDoc.get(MdekKeys.LINKAGE_DATATYPE));
		urlRef.setVolume((String) urlDoc.get(MdekKeys.LINKAGE_VOLUME));
		urlRef.setIcon((String) urlDoc.get(MdekKeys.LINKAGE_ICON_URL));
		urlRef.setIconText((String) urlDoc.get(MdekKeys.LINKAGE_ICON_TEXT));
		urlRef.setDescr((String) urlDoc.get(MdekKeys.LINKAGE_DESCRIPTION));
		urlRef.setContent((String) urlDoc.get(MdekKeys.LINKAGE_NAME));
		urlRef.setUrlType((Integer) urlDoc.get(MdekKeys.LINKAGE_URL_TYPE));		
		urlRef.setLine(line);

		return urlRef;
	}

	private void updateT017UrlRefs(IngridDocument oDocIn, T01Object oIn) {
		List<IngridDocument> urlDocs = (List) oDocIn.get(MdekKeys.LINKAGES);
		if (urlDocs == null) {
			urlDocs = new ArrayList<IngridDocument>(0);
		}
		Set<T017UrlRef> urlRefs = oIn.getT017UrlRefs();
		ArrayList<T017UrlRef> refs_unprocessed = new ArrayList<T017UrlRef>(urlRefs);
		// remove all !
		for (T017UrlRef ref : refs_unprocessed) {
			urlRefs.remove(ref);
			// delete-orphan doesn't work !!!?????
			daoT017UrlRef.makeTransient(ref);			
		}		
		// and add all new ones !
		int line = 1;
		for (IngridDocument urlDoc : urlDocs) {
			// add all as new ones
			T017UrlRef urlRef = mapT017UrlRef(oIn, urlDoc, new T017UrlRef(), line);
			urlRefs.add(urlRef);
			line++;
		}
	}

	private T0113DatasetReference mapT0113DatasetReference(T01Object oFrom,
		IngridDocument refDoc,
		T0113DatasetReference ref, 
		int line) 
	{
		ref.setObjId(oFrom.getId());
		ref.setReferenceDate((String) refDoc.get(MdekKeys.DATASET_REFERENCE_DATE));
		ref.setType((Integer) refDoc.get(MdekKeys.DATASET_REFERENCE_TYPE));
		ref.setLine(line);

		return ref;
	}
	private void updateT0113DatasetReferences(IngridDocument oDocIn, T01Object oIn) {
		List<IngridDocument> refDocs = (List) oDocIn.get(MdekKeys.DATASET_REFERENCES);
		if (refDocs == null) {
			refDocs = new ArrayList<IngridDocument>(0);
		}
		Set<T0113DatasetReference> refs = oIn.getT0113DatasetReferences();
		ArrayList<T0113DatasetReference> refs_unprocessed = new ArrayList<T0113DatasetReference>(refs);
		// remove all !
		for (T0113DatasetReference ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			daoT0113DatasetReference.makeTransient(ref);			
		}		
		// and add all new ones !
		int line = 1;
		for (IngridDocument refDoc : refDocs) {
			// add all as new ones
			T0113DatasetReference ref = mapT0113DatasetReference(oIn, refDoc, new T0113DatasetReference(), line);
			refs.add(ref);
			line++;
		}
	}

	private T014InfoImpart mapT014InfoImpart(T01Object oFrom,
			String name,
			T014InfoImpart ref, 
			int line) 
	{
		ref.setObjId(oFrom.getId());
		ref.setName(name);
		ref.setLine(line);

		return ref;
	}
	private void updateT014InfoImparts(IngridDocument oDocIn, T01Object oIn) {
		List<String> refDocs = (List) oDocIn.get(MdekKeys.EXPORTS);
		if (refDocs == null) {
			refDocs = new ArrayList<String>(0);
		}
		Set<T014InfoImpart> refs = oIn.getT014InfoImparts();
		ArrayList<T014InfoImpart> refs_unprocessed = new ArrayList<T014InfoImpart>(refs);
		// remove all !
		for (T014InfoImpart ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			daoT014InfoImpart.makeTransient(ref);			
		}		
		// and add all new ones !
		int line = 1;
		for (String refName : refDocs) {
			// add all as new ones
			T014InfoImpart ref = mapT014InfoImpart(oIn, refName, new T014InfoImpart(), line);
			refs.add(ref);
			line++;
		}
	}

	private T015Legist mapT015Legist(T01Object oFrom,
			String name,
			T015Legist ref, 
			int line)
	{
		ref.setObjId(oFrom.getId());
		ref.setName(name);
		ref.setLine(line);

		return ref;
	}
	private void updateT015Legists(IngridDocument oDocIn, T01Object oIn) {
		List<String> refDocs = (List) oDocIn.get(MdekKeys.LEGISLATIONS);
		if (refDocs == null) {
			refDocs = new ArrayList<String>(0);
		}
		Set<T015Legist> refs = oIn.getT015Legists();
		ArrayList<T015Legist> refs_unprocessed = new ArrayList<T015Legist>(refs);
		// remove all !
		for (T015Legist ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			daoT015Legist.makeTransient(ref);			
		}		
		// and add all new ones !
		int line = 1;
		for (String refName : refDocs) {
			// add all as new ones
			T015Legist ref = mapT015Legist(oIn, refName, new T015Legist(), line);
			refs.add(ref);
			line++;
		}
	}

	private T0110AvailFormat mapT0110AvailFormat(T01Object oFrom,
			IngridDocument refDoc,
			T0110AvailFormat ref, 
			int line) 
	{
		ref.setObjId(oFrom.getId());
		ref.setName((String) refDoc.get(MdekKeys.FORMAT_NAME));
		ref.setVer((String) refDoc.get(MdekKeys.FORMAT_VERSION));
		ref.setSpecification((String) refDoc.get(MdekKeys.FORMAT_SPECIFICATION));
		ref.setFileDecompressionTechnique((String) refDoc.get(MdekKeys.FORMAT_FILE_DECOMPRESSION_TECHNIQUE));
		ref.setLine(line);

		return ref;
	}
	private void updateT0110AvailFormats(IngridDocument oDocIn, T01Object oIn) {
		List<IngridDocument> refDocs = (List) oDocIn.get(MdekKeys.DATA_FORMATS);
		if (refDocs == null) {
			refDocs = new ArrayList<IngridDocument>(0);
		}
		Set<T0110AvailFormat> refs = oIn.getT0110AvailFormats();
		ArrayList<T0110AvailFormat> refs_unprocessed = new ArrayList<T0110AvailFormat>(refs);
		// remove all !
		for (T0110AvailFormat ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			daoT0110AvailFormat.makeTransient(ref);			
		}		
		// and add all new ones !
		int line = 1;
		for (IngridDocument refDoc : refDocs) {
			// add all as new ones
			T0110AvailFormat ref = mapT0110AvailFormat(oIn, refDoc, new T0110AvailFormat(), line);
			refs.add(ref);
			line++;
		}
	}


	private T0112MediaOption mapT0112MediaOption(T01Object oFrom,
			IngridDocument refDoc,
			T0112MediaOption ref, 
			int line) 
	{
		ref.setObjId(oFrom.getId());
		ref.setMediumName((Integer) refDoc.get(MdekKeys.MEDIUM_NAME));
		ref.setTransferSize((Double) refDoc.get(MdekKeys.MEDIUM_TRANSFER_SIZE));
		ref.setMediumNote((String) refDoc.get(MdekKeys.MEDIUM_NOTE));
		ref.setLine(line);

		return ref;
	}
	private void updateT0112MediaOptions(IngridDocument oDocIn, T01Object oIn) {
		List<IngridDocument> refDocs = (List) oDocIn.get(MdekKeys.MEDIUM_OPTIONS);
		if (refDocs == null) {
			refDocs = new ArrayList<IngridDocument>(0);
		}
		Set<T0112MediaOption> refs = oIn.getT0112MediaOptions();
		ArrayList<T0112MediaOption> refs_unprocessed = new ArrayList<T0112MediaOption>(refs);
		// remove all !
		for (T0112MediaOption ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			daoT0112MediaOption.makeTransient(ref);			
		}		
		// and add all new ones !
		int line = 1;
		for (IngridDocument refDoc : refDocs) {
			// add all as new ones
			T0112MediaOption ref = mapT0112MediaOption(oIn, refDoc, new T0112MediaOption(), line);
			refs.add(ref);
			line++;
		}
	}
}
