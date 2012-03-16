package de.ingrid.mdek.services.persistence.db.mapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import de.ingrid.mdek.EnumUtil;
import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekUtils.IdcEntityType;
import de.ingrid.mdek.MdekUtils.ObjectType;
import de.ingrid.mdek.MdekUtils.SearchtermType;
import de.ingrid.mdek.MdekUtils.SpatialReferenceType;
import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.IEntity;
import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.dao.ISearchtermSnsDao;
import de.ingrid.mdek.services.persistence.db.dao.ISearchtermValueDao;
import de.ingrid.mdek.services.persistence.db.dao.ISpatialRefSnsDao;
import de.ingrid.mdek.services.persistence.db.dao.ISpatialRefValueDao;
import de.ingrid.mdek.services.persistence.db.model.AdditionalFieldData;
import de.ingrid.mdek.services.persistence.db.model.AddressComment;
import de.ingrid.mdek.services.persistence.db.model.AddressMetadata;
import de.ingrid.mdek.services.persistence.db.model.AddressNode;
import de.ingrid.mdek.services.persistence.db.model.ObjectAccess;
import de.ingrid.mdek.services.persistence.db.model.ObjectComment;
import de.ingrid.mdek.services.persistence.db.model.ObjectConformity;
import de.ingrid.mdek.services.persistence.db.model.ObjectDataQuality;
import de.ingrid.mdek.services.persistence.db.model.ObjectFormatInspire;
import de.ingrid.mdek.services.persistence.db.model.ObjectMetadata;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;
import de.ingrid.mdek.services.persistence.db.model.ObjectReference;
import de.ingrid.mdek.services.persistence.db.model.ObjectTypesCatalogue;
import de.ingrid.mdek.services.persistence.db.model.ObjectUse;
import de.ingrid.mdek.services.persistence.db.model.SearchtermAdr;
import de.ingrid.mdek.services.persistence.db.model.SearchtermObj;
import de.ingrid.mdek.services.persistence.db.model.SearchtermSns;
import de.ingrid.mdek.services.persistence.db.model.SearchtermValue;
import de.ingrid.mdek.services.persistence.db.model.SpatialRefSns;
import de.ingrid.mdek.services.persistence.db.model.SpatialRefValue;
import de.ingrid.mdek.services.persistence.db.model.SpatialReference;
import de.ingrid.mdek.services.persistence.db.model.SpatialSystem;
import de.ingrid.mdek.services.persistence.db.model.SysGenericKey;
import de.ingrid.mdek.services.persistence.db.model.SysList;
import de.ingrid.mdek.services.persistence.db.model.T0110AvailFormat;
import de.ingrid.mdek.services.persistence.db.model.T0112MediaOption;
import de.ingrid.mdek.services.persistence.db.model.T0113DatasetReference;
import de.ingrid.mdek.services.persistence.db.model.T0114EnvTopic;
import de.ingrid.mdek.services.persistence.db.model.T011ObjData;
import de.ingrid.mdek.services.persistence.db.model.T011ObjDataPara;
import de.ingrid.mdek.services.persistence.db.model.T011ObjGeo;
import de.ingrid.mdek.services.persistence.db.model.T011ObjGeoScale;
import de.ingrid.mdek.services.persistence.db.model.T011ObjGeoSpatialRep;
import de.ingrid.mdek.services.persistence.db.model.T011ObjGeoSupplinfo;
import de.ingrid.mdek.services.persistence.db.model.T011ObjGeoSymc;
import de.ingrid.mdek.services.persistence.db.model.T011ObjGeoVector;
import de.ingrid.mdek.services.persistence.db.model.T011ObjLiterature;
import de.ingrid.mdek.services.persistence.db.model.T011ObjProject;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServ;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServOpConnpoint;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServOpDepends;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServOpPara;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServOpPlatform;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServOperation;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServScale;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServType;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServUrl;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServVersion;
import de.ingrid.mdek.services.persistence.db.model.T011ObjTopicCat;
import de.ingrid.mdek.services.persistence.db.model.T012ObjAdr;
import de.ingrid.mdek.services.persistence.db.model.T014InfoImpart;
import de.ingrid.mdek.services.persistence.db.model.T015Legist;
import de.ingrid.mdek.services.persistence.db.model.T017UrlRef;
import de.ingrid.mdek.services.persistence.db.model.T01Object;
import de.ingrid.mdek.services.persistence.db.model.T021Communication;
import de.ingrid.mdek.services.persistence.db.model.T02Address;
import de.ingrid.mdek.services.persistence.db.model.T03Catalogue;
import de.ingrid.mdek.services.utils.MdekKeyValueHandler;
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
	private ISearchtermSnsDao daoSearchtermSns;
	private ISearchtermValueDao daoSearchtermValue;

	/** Generic dao for class unspecific operations !!! */
	private IGenericDao<IEntity> dao;

	private MdekKeyValueHandler keyValueService;

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
		daoSearchtermSns = daoFactory.getSearchtermSnsDao();
		daoSearchtermValue = daoFactory.getSearchtermValueDao();

		dao = daoFactory.getDao(IEntity.class);

		keyValueService = MdekKeyValueHandler.getInstance(daoFactory);
	}
	
	public T03Catalogue mapT03Catalog(IngridDocument inDoc, T03Catalogue cat) {
		cat.setCatUuid(inDoc.getString(MdekKeys.UUID));
		cat.setCatName(inDoc.getString(MdekKeys.CATALOG_NAME));
		cat.setCatNamespace(inDoc.getString(MdekKeys.CATALOG_NAMESPACE));
		cat.setPartnerName(inDoc.getString(MdekKeys.PARTNER_NAME));
		cat.setProviderName(inDoc.getString(MdekKeys.PROVIDER_NAME));
		cat.setCountryKey((Integer)inDoc.get(MdekKeys.COUNTRY_CODE));
		cat.setCountryValue(inDoc.getString(MdekKeys.COUNTRY_NAME));
		cat.setLanguageKey((Integer)inDoc.get(MdekKeys.LANGUAGE_CODE));
		cat.setLanguageValue(inDoc.getString(MdekKeys.LANGUAGE_NAME));
		cat.setWorkflowControl(inDoc.getString(MdekKeys.WORKFLOW_CONTROL));
		cat.setExpiryDuration((Integer) inDoc.get(MdekKeys.EXPIRY_DURATION));
		String creationDate = (String) inDoc.get(MdekKeys.DATE_OF_CREATION);
		if (creationDate != null) {
			cat.setCreateTime(creationDate);				
		}
		cat.setModTime((String) inDoc.get(MdekKeys.DATE_OF_LAST_MODIFICATION));
		cat.setModUuid(extractModUserUuid(inDoc));

		updateSpatialRefValueOfCatalogue(inDoc, cat);

		keyValueService.processKeyValue(cat);

		return cat;
	}
	
	/**
     * Update syslist with new data and persist.
     * @param docIn map containing new syslist data
     * @param sysList current entries of list from DB ordered by entities ascending
     */
    public void updateSysList(IngridDocument docIn, List<SysList> sysListEntries) {

        Integer inLstId = (Integer) docIn.get(MdekKeys.LST_ID);
        Boolean tmpMaintainable = (Boolean) docIn.get(MdekKeys.LST_MAINTAINABLE);
        int inMaintainable = tmpMaintainable ? MdekUtils.YES_INTEGER : MdekUtils.NO_INTEGER;
        Integer tmpDefaultEntryIndex = (Integer) docIn.get(MdekKeys.LST_DEFAULT_ENTRY_INDEX);
        int inDefaultEntryIndex = (tmpDefaultEntryIndex == null) ? -1 : tmpDefaultEntryIndex;
        Integer[] inEntryIds = (Integer[]) docIn.get(MdekKeys.LST_ENTRY_IDS);
        String[] inNames_de = (String[]) docIn.get(MdekKeys.LST_ENTRY_NAMES_DE);
        String[] inNames_en = (String[]) docIn.get(MdekKeys.LST_ENTRY_NAMES_EN);
        boolean hasEnglishEntries = false;
        if (inNames_en != null) {
            for (String inName_en : inNames_en) {
                if (inName_en != null && inName_en.trim().length() > 0) {
                    hasEnglishEntries = true;
                    break;
                }
            }
        }

        // determine max entry id of given syslist in database. If new syslist then start at "0".
        // will be increased for new entries.
        // NOTICE: entries may be ordered by line, so we have to iterate all entries !
        int maxId = 0;
        for (SysList entry : sysListEntries) {
        	if (entry.getEntryId() > maxId) {
        		maxId = entry.getEntryId();
        	}
        }

        // here the ones to delete will remain
        ArrayList<SysList> entriesUnprocessed = new ArrayList<SysList>(sysListEntries);

        // process passed data, update syslist entries, add new ones, delete removed ones ...
        for (int i=0; i < inEntryIds.length; i++) {
            Integer inEntryId = inEntryIds[i];

            // process all languages one by one
            for (String langId : MdekUtils.LANGUAGES_SHORTCUTS) {
                String[] inNames;

                if (langId.equals(MdekUtils.LANGUAGE_SHORTCUT_DE)) {
                    inNames = inNames_de;
                } else if (langId.equals(MdekUtils.LANGUAGE_SHORTCUT_EN)) {
                    if (!hasEnglishEntries) {
                        // skip english
                        continue;
                    }
                    inNames = inNames_en;                   
                } else {
                    // UNKNOWN LANGUAGE ! skip it
                    continue;
                }

                SysList foundEntry = null;
                if (inEntryId != null) {
                    for (SysList entry : sysListEntries) {
                        if (inLstId.equals(entry.getLstId()) &&
                                inEntryId.equals(entry.getEntryId()) &&
                                langId.equals(entry.getLangId())) {
                            foundEntry = entry;
                            entriesUnprocessed.remove(foundEntry);
                            break;
                        }
                    }               
                } else {
                    // new entry id is one above former max entry id !
                    maxId++;
                    inEntryId = maxId;
                }
                if (foundEntry == null) {
                    // add new one
                    foundEntry = new SysList();
                    foundEntry.setLstId(inLstId);
                    foundEntry.setEntryId(inEntryId);
                    foundEntry.setLangId(langId);
                    // TODO: order of syslist (line attribute) not stored !
                    foundEntry.setLine(0);
                    sysListEntries.add(foundEntry);
                }
                String isDefault = (i == inDefaultEntryIndex) ? MdekUtils.YES : MdekUtils.NO;
                foundEntry.setIsDefault(isDefault);
                String inName = (inNames[i] == null) ? "" : inNames[i];
                foundEntry.setName(inName);
                foundEntry.setMaintainable(inMaintainable);
                dao.makePersistent(foundEntry);
            }
        }
        // remove the ones not processed
        for (SysList entryUnprocessed : entriesUnprocessed) {
            sysListEntries.remove(entryUnprocessed);
            dao.makeTransient(entryUnprocessed);
        }       
    }

	/**
	 * Update a syslist with all available languages and persist.
	 * @param docIn map containing new syslist data
	 * @param sysList current entries of list from DB ordered by entities ascending
	 */
	public void updateSysListAllLang(IngridDocument docIn, List<SysList> sysListEntries) {

		Integer inLstId = (Integer) docIn.get(MdekKeys.LST_ID);
		Boolean tmpMaintainable = (Boolean) docIn.get(MdekKeys.LST_MAINTAINABLE);
		int inMaintainable = tmpMaintainable ? MdekUtils.YES_INTEGER : MdekUtils.NO_INTEGER;
		Integer tmpDefaultEntryId = (Integer) docIn.get(MdekKeys.LST_DEFAULT_ENTRY_ID);
		int inDefaultEntryId = (tmpDefaultEntryId == null) ? -1 : tmpDefaultEntryId;

        // determine max entry id of given syslist in database. If new syslist then start at "0".
        // will be increased for new entries.
        // NOTICE: entries may be ordered by line, so we have to iterate all entries !
        int maxId = 0;
        for (SysList entry : sysListEntries) {
        	if (entry.getEntryId() > maxId) {
        		maxId = entry.getEntryId();
        	}
        }

		// here the ones to delete will remain
		ArrayList<SysList> entriesUnprocessed = new ArrayList<SysList>(sysListEntries);

		IngridDocument[] entries = (IngridDocument[]) docIn.get(MdekKeys.LST_ENTRIES);
		// process passed data, update syslist entries, add new ones, delete removed ones ...
		for (int i=0; i < entries.length; i++) {
			Integer inEntryId = entries[i].getInt(MdekKeys.LST_ENTRY_ID);

			// process all languages one by one
			IngridDocument localNames = (IngridDocument) entries[i].get(MdekKeys.LST_LOCALISED_ENTRY_NAME_MAP);
			for (String langId : ((Map<String,String>)localNames).keySet()) {
			    String inName = localNames.getString(langId);

				SysList foundEntry = null;
				if (inEntryId != null) {
					for (SysList entry : sysListEntries) {
						if (inLstId.equals(entry.getLstId()) &&
								inEntryId.equals(entry.getEntryId()) &&
								langId.equals(entry.getLangId())) {
							foundEntry = entry;
							entriesUnprocessed.remove(foundEntry);
							break;
						}
					}				
				} else {
					// new entry id is one above former max entry id !
					maxId++;
					inEntryId = maxId;
				}
				if (foundEntry == null) {
					// add new one
					foundEntry = new SysList();
					foundEntry.setLstId(inLstId);
					foundEntry.setEntryId(inEntryId);
					foundEntry.setLangId(langId);
					// TODO: order of syslist (line attribute) not stored !
					foundEntry.setLine(0);
					sysListEntries.add(foundEntry);
				}
				String isDefault = (inEntryId == inDefaultEntryId) ? MdekUtils.YES : MdekUtils.NO;
				foundEntry.setIsDefault(isDefault);
				//String inName = (inName == null) ? "" : inName;
				foundEntry.setName(inName);
				foundEntry.setDescription(entries[i].getString(MdekKeys.LST_ENTRY_DESCRIPTION));
				foundEntry.setMaintainable(inMaintainable);
				dao.makePersistent(foundEntry);
			}
		}
		// remove the ones not processed
		for (SysList entryUnprocessed : entriesUnprocessed) {
			sysListEntries.remove(entryUnprocessed);
			dao.makeTransient(entryUnprocessed);
		}		
	}
	
	public void updateSysGenericKeys(IngridDocument inDoc, List<SysGenericKey> sysKeys) {
		String[] keyNames = (String[]) inDoc.get(MdekKeys.SYS_GENERIC_KEY_NAMES);
		String[] keyValues = (String[]) inDoc.get(MdekKeys.SYS_GENERIC_KEY_VALUES);

		// Currently we don't DELETE SYS GENERIC KEYS !
		for (int i=0; i<keyNames.length; i++) {
			String keyName = keyNames[i];
			SysGenericKey foundSysKey = null;
			for (SysGenericKey sysKey : sysKeys) {
				if (sysKey.getKeyName().equals(keyName)) {
					foundSysKey = sysKey;
					break;
				}
			}
			if (foundSysKey == null) {
				// add new one
				foundSysKey = new SysGenericKey();
				foundSysKey.setKeyName(keyName);
				sysKeys.add(foundSysKey);
			}
			foundSysKey.setValueString(keyValues[i]);
			dao.makePersistent(foundSysKey);
		}
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

	public AddressNode mapAddressNode(IngridDocument docIn, AddressNode nodeIn) {
		nodeIn.setAddrUuid((String) docIn.get(MdekKeys.UUID));
		if (docIn.containsKey(MdekKeys.PARENT_UUID)) {
			// NOTICE: parent may be null, then root node !
			String parentUuid = (String) docIn.get(MdekKeys.PARENT_UUID);
			nodeIn.setFkAddrUuid(parentUuid);
		}

		return nodeIn;
	}

	/**
	 * Transfer data of passed doc to passed bean according to mapping type.
	 */
	public T01Object mapT01Object(IngridDocument oDocIn, T01Object oIn, MappingQuantity howMuch) {

		oIn.setObjUuid((String) oDocIn.get(MdekKeys.UUID));
		oIn.setObjClass((Integer) oDocIn.get(MdekKeys.CLASS));
		oIn.setObjName((String) oDocIn.get(MdekKeys.TITLE));
		oIn.setWorkState((String) oDocIn.get(MdekKeys.WORK_STATE));
		oIn.setModTime((String) oDocIn.get(MdekKeys.DATE_OF_LAST_MODIFICATION));

		// stuff only set if NEW object !
		String creationDate = (String) oDocIn.get(MdekKeys.DATE_OF_CREATION);
		if (creationDate != null) {
			oIn.setCreateTime(creationDate);				
		}
		Long catId = (Long) oDocIn.get(MdekKeys.CATALOGUE_IDENTIFIER);
		if (catId != null) {
			oIn.setCatId(catId);				
		}

		if (howMuch == MappingQuantity.DETAIL_ENTITY ||
				howMuch == MappingQuantity.COPY_ENTITY)
		{
			oIn.setDatasetAlternateName((String) oDocIn.get(MdekKeys.DATASET_ALTERNATE_NAME));
			oIn.setObjDescr((String) oDocIn.get(MdekKeys.ABSTRACT));

			oIn.setVerticalExtentMinimum((Double) oDocIn.get(MdekKeys.VERTICAL_EXTENT_MINIMUM));
			oIn.setVerticalExtentMaximum((Double) oDocIn.get(MdekKeys.VERTICAL_EXTENT_MAXIMUM));
			oIn.setVerticalExtentUnit((Integer) oDocIn.get(MdekKeys.VERTICAL_EXTENT_UNIT));
			oIn.setVerticalExtentVdatumKey((Integer) oDocIn.get(MdekKeys.VERTICAL_EXTENT_VDATUM_KEY));
			oIn.setVerticalExtentVdatumValue(oDocIn.getString(MdekKeys.VERTICAL_EXTENT_VDATUM_VALUE));
			oIn.setLocDescr((String) oDocIn.get(MdekKeys.DESCRIPTION_OF_SPATIAL_DOMAIN));

			oIn.setTimeType((String) oDocIn.get(MdekKeys.TIME_TYPE));
			oIn.setTimeFrom((String) oDocIn.get(MdekKeys.BEGINNING_DATE));
			oIn.setTimeTo((String) oDocIn.get(MdekKeys.ENDING_DATE));
			oIn.setTimeStatus((Integer) oDocIn.get(MdekKeys.TIME_STATUS));
			oIn.setTimePeriod((Integer) oDocIn.get(MdekKeys.TIME_PERIOD));
			oIn.setTimeInterval((String) oDocIn.get(MdekKeys.TIME_STEP));
			oIn.setTimeAlle((String) oDocIn.get(MdekKeys.TIME_SCALE));
			oIn.setTimeDescr((String) oDocIn.get(MdekKeys.DESCRIPTION_OF_TEMPORAL_DOMAIN));
			
			oIn.setMetadataLanguageKey((Integer) oDocIn.get(MdekKeys.METADATA_LANGUAGE_CODE));
			oIn.setMetadataLanguageValue(oDocIn.getString(MdekKeys.METADATA_LANGUAGE_NAME));
			oIn.setDataLanguageKey((Integer) oDocIn.get(MdekKeys.DATA_LANGUAGE_CODE));
			oIn.setDataLanguageValue(oDocIn.getString(MdekKeys.DATA_LANGUAGE_NAME));
			oIn.setPublishId((Integer) oDocIn.get(MdekKeys.PUBLICATION_CONDITION));
			oIn.setInfoNote((String) oDocIn.get(MdekKeys.DATASET_INTENTIONS));
			oIn.setDatasetUsage((String) oDocIn.get(MdekKeys.DATASET_USAGE));
			oIn.setDatasetCharacterSet((Integer) oDocIn.get(MdekKeys.DATASET_CHARACTER_SET));

			oIn.setOrderingInstructions((String) oDocIn.get(MdekKeys.ORDERING_INSTRUCTIONS));
			oIn.setIsCatalogData(oDocIn.getString(MdekKeys.IS_CATALOG_DATA));
			oIn.setIsInspireRelevant(oDocIn.getString(MdekKeys.IS_INSPIRE_RELEVANT));

			oIn.setModUuid(extractModUserUuid(oDocIn));
			oIn.setResponsibleUuid(extractResponsibleUserUuid(oDocIn));

			// update associations
			updateObjectReferences((List<IngridDocument>) oDocIn.get(MdekKeys.OBJ_REFERENCES_TO), oIn);
			updateT012ObjAdrs(oDocIn, oIn, howMuch);
			updateSpatialReferences(oDocIn, oIn);
			updateSearchtermObjs(oDocIn, oIn);
			updateT017UrlRefs(oDocIn, oIn);
			updateT0113DatasetReferences(oDocIn, oIn);
			updateT014InfoImparts(oDocIn, oIn);
			updateT015Legists(oDocIn, oIn);
			updateT0110AvailFormats(oDocIn, oIn);
			updateT0112MediaOptions(oDocIn, oIn);
			updateT0114EnvTopics(oDocIn, oIn);
			updateT011ObjTopicCats(oDocIn, oIn);

			// technical domain map (class 1)
			updateT011ObjGeo(oDocIn, oIn);
			// technical domain document (class 2)
			updateT011ObjLiterature(oDocIn, oIn);
			// NOTICE: T011ObjServ is used for the object classes "Geodatendienst" (class 3) AND
			// "Informationssystem/Dienst/Anwendung" (class 6) with DIFFERENT content !
			// we don't distinguish here and map all stuff available !
			updateT011ObjServ(oDocIn, oIn);
			// technical domain project (class 4)
			updateT011ObjProject(oDocIn, oIn);
			// technical domain dataset (class 5)
			updateT011ObjData(oDocIn, oIn);

			// connected to T01Object and NOT technical domains ! so call independent from technical domains !
			updateT011ObjDataParas(oDocIn, oIn);
			updateObjectTypesCatalogues(oDocIn, oIn);

			// additional fields
			updateAdditionalFieldDatas(oDocIn, oIn);

			// comments
			updateObjectComments(oDocIn, oIn);
			updateObjectConformitys(oDocIn, oIn);
			updateObjectAccesses(oDocIn, oIn);
			updateObjectUses(oDocIn, oIn);
			updateObjectDataQualitys(oDocIn, oIn);
			updateObjectFormatInspires(oDocIn, oIn);
			updateSpatialSystems(oDocIn, oIn);
			updateObjectMetadata(oDocIn, oIn);
		}

		if (howMuch == MappingQuantity.COPY_ENTITY) {
			// update only if set (so object keeps former values) ! NOT PASSED FROM CLIENT !!!
			// BUT E.G. PASSED WHEN IMPORTING !!!
			if (oDocIn.containsKey(MdekKeys.ORIGINAL_CONTROL_IDENTIFIER)) {
				oIn.setOrgObjId((String) oDocIn.get(MdekKeys.ORIGINAL_CONTROL_IDENTIFIER));
			}
			if (oDocIn.containsKey(MdekKeys.METADATA_CHARACTER_SET)) {
				oIn.setMetadataCharacterSet((Integer) oDocIn.get(MdekKeys.METADATA_CHARACTER_SET));
			}
			if (oDocIn.containsKey(MdekKeys.METADATA_STANDARD_NAME)) {
				oIn.setMetadataStandardName((String) oDocIn.get(MdekKeys.METADATA_STANDARD_NAME));
			}
			if (oDocIn.containsKey(MdekKeys.METADATA_STANDARD_VERSION)) {
				oIn.setMetadataStandardVersion((String) oDocIn.get(MdekKeys.METADATA_STANDARD_VERSION));			
			}
		}

		keyValueService.processKeyValue(oIn);

		return oIn;
	}

	public T02Address mapT02Address(IngridDocument aDocIn, T02Address aIn, MappingQuantity howMuch) {

		aIn.setAdrUuid(aDocIn.getString(MdekKeys.UUID));
		aIn.setAdrType((Integer) aDocIn.get(MdekKeys.CLASS));
		aIn.setInstitution(aDocIn.getString(MdekKeys.ORGANISATION));
		aIn.setLastname(aDocIn.getString(MdekKeys.NAME));
		aIn.setFirstname(aDocIn.getString(MdekKeys.GIVEN_NAME));
		aIn.setTitleValue(aDocIn.getString(MdekKeys.TITLE_OR_FUNCTION));
		aIn.setTitleKey((Integer)aDocIn.get(MdekKeys.TITLE_OR_FUNCTION_KEY));
		aIn.setWorkState((String) aDocIn.get(MdekKeys.WORK_STATE));
		String creationDate = (String) aDocIn.get(MdekKeys.DATE_OF_CREATION);
		if (creationDate != null) {
			aIn.setCreateTime(creationDate);				
		}
		aIn.setModTime((String) aDocIn.get(MdekKeys.DATE_OF_LAST_MODIFICATION));
		aIn.setHideAddress((String) aDocIn.get(MdekKeys.HIDE_ADDRESS));

		if (howMuch == MappingQuantity.DETAIL_ENTITY ||
				howMuch == MappingQuantity.COPY_ENTITY)
		{
			aIn.setStreet(aDocIn.getString(MdekKeys.STREET));
			aIn.setCountryKey((Integer)aDocIn.get(MdekKeys.COUNTRY_CODE));
			aIn.setCountryValue(aDocIn.getString(MdekKeys.COUNTRY_NAME));
			aIn.setPostcode(aDocIn.getString(MdekKeys.POSTAL_CODE));
			aIn.setCity(aDocIn.getString(MdekKeys.CITY));
			aIn.setPostboxPc(aDocIn.getString(MdekKeys.POST_BOX_POSTAL_CODE));
			aIn.setPostbox(aDocIn.getString(MdekKeys.POST_BOX));

			aIn.setJob(aDocIn.getString(MdekKeys.FUNCTION));
			aIn.setAddressValue(aDocIn.getString(MdekKeys.NAME_FORM));
			aIn.setAddressKey((Integer)aDocIn.get(MdekKeys.NAME_FORM_KEY));
			aIn.setDescr(aDocIn.getString(MdekKeys.ADDRESS_DESCRIPTION));

			aIn.setModUuid(extractModUserUuid(aDocIn));
			aIn.setResponsibleUuid(extractResponsibleUserUuid(aDocIn));

			// update associations
			updateT021Communications(aDocIn, aIn);
			updateSearchtermAdrs(aDocIn, aIn);
			updateAddressComments(aDocIn, aIn);
			updateAddressMetadata(aDocIn, aIn);
		}

		if (howMuch == MappingQuantity.COPY_ENTITY) {
			// update only if set (so object keeps former values) ! NOT PASSED FROM CLIENT !!!
			// BUT E.G. PASSED WHEN IMPORTING !!!
			if (aDocIn.containsKey(MdekKeys.ORIGINAL_ADDRESS_IDENTIFIER)) {
				aIn.setOrgAdrId(aDocIn.getString(MdekKeys.ORIGINAL_ADDRESS_IDENTIFIER));
			}

		}

		keyValueService.processKeyValue(aIn);

		return aIn;
	}

	public String extractModUserUuid(IngridDocument inDoc) {
		return extractUserUuid(inDoc, MdekKeys.MOD_USER);
	}

	public String extractResponsibleUserUuid(IngridDocument inDoc) {
		return extractUserUuid(inDoc, MdekKeys.RESPONSIBLE_USER);
	}

	private String extractUserUuid(IngridDocument inDoc, String userKeyInMap) {
		String userUuid = null;
		
		IngridDocument userDoc = (IngridDocument) inDoc.get(userKeyInMap);
		if (userDoc != null) {
			userUuid = userDoc.getString(MdekKeys.UUID);
		}
		
		return userUuid;
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
		keyValueService.processKeyValue(oRef);

		return oRef;
	}

	public void updateObjectReferences(List<IngridDocument> oDocsTo, T01Object oIn) {
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
			dao.makeTransient(oR);
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

		keyValueService.processKeyValue(oA);

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
			String inUuidTo = (String) aDocTo.get(MdekKeys.UUID);
			Integer inRelType = (Integer) aDocTo.get(MdekKeys.RELATION_TYPE_ID);
			boolean found = false;
			for (T012ObjAdr oA : oAs) {
				if (oA.getAdrUuid().equals(inUuidTo) &&
					oA.getType().equals(inRelType))
				{
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
			dao.makeTransient(oA);
		}		
	}

	/** Just map spatial ref doc to bean for better handling of spatial references ! NO DATABASE BEAN !!! */
	public SpatialRefValue mapHelperSpatialRefValue(IngridDocument refDoc, SpatialRefValue refValue) {
		if (refDoc == null) {
			return null;
		}

		SpatialRefSns refSns = refValue.getSpatialRefSns();
		if (refSns == null) {
			refSns = new SpatialRefSns();
		}
		mapSpatialRefSns(refDoc, refSns);
		mapSpatialRefValue(refSns, refDoc, refValue);

		return refValue;
	}

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
	public SpatialRefValue mapSpatialRefValue(SpatialRefSns spRefSns,
		IngridDocument locDoc,
		SpatialRefValue spRefValue) 
	{
		spRefValue.setNameValue(locDoc.getString(MdekKeys.LOCATION_NAME));
		spRefValue.setNameKey((Integer)locDoc.get(MdekKeys.LOCATION_NAME_KEY));
		spRefValue.setType((String) locDoc.get(MdekKeys.LOCATION_TYPE));
		spRefValue.setNativekey((String) locDoc.get(MdekKeys.LOCATION_CODE));
		spRefValue.setX1((Double) locDoc.get(MdekKeys.WEST_BOUNDING_COORDINATE));
		spRefValue.setY1((Double) locDoc.get(MdekKeys.SOUTH_BOUNDING_COORDINATE));
		spRefValue.setX2((Double) locDoc.get(MdekKeys.EAST_BOUNDING_COORDINATE));
		spRefValue.setY2((Double) locDoc.get(MdekKeys.NORTH_BOUNDING_COORDINATE));
		spRefValue.setTopicType((String) locDoc.get(MdekKeys.SNS_TOPIC_TYPE));
		
		Long spRefSnsId = null;
		if (spRefSns != null) {
			spRefSnsId = spRefSns.getId();			
		}
		spRefValue.setSpatialRefSns(spRefSns);			
		spRefValue.setSpatialRefSnsId(spRefSnsId);
		keyValueService.processKeyValue(spRefValue);

		return spRefValue;
	}
	private SpatialRefSns mapSpatialRefSns(IngridDocument refDoc, SpatialRefSns refValue) {
		refValue.setSnsId(refDoc.getString(MdekKeys.LOCATION_SNS_ID));
		// NO mapping of expired. Will not be updated via doc (set from job !)
//		refValue.setExpiredAt(refDoc.getString(MdekKeys.LOCATION_EXPIRED_AT));			

		return refValue;
	}
	private void updateSpatialReferences(IngridDocument oDocIn, T01Object oIn) {
		List<IngridDocument> locList = (List) oDocIn.get(MdekKeys.LOCATIONS);
		if (locList == null) {
			locList = new ArrayList<IngridDocument>(0);
		}

		Set<SpatialReference> spatialRefs = oIn.getSpatialReferences();
		ArrayList<SpatialReference> spatialRefs_unprocessed = new ArrayList<SpatialReference>(spatialRefs);

		int line = 1;
		// remember ids of all added spatial refs to avoid delete of added one(s) 
		Set<Long> idsSpatialRefValsAdded = new HashSet<Long>();
		for (IngridDocument loc : locList) {
			boolean found = false;
			for (SpatialReference spRef : spatialRefs) {
				SpatialRefValue spRefValue = spRef.getSpatialRefValue();
				if (spRefValue != null) {
					found = updateSpatialRefValueViaDoc(loc, spRefValue);
					if (found) {
						// update line
						spRef.setLine(line);
						spatialRefs_unprocessed.remove(spRef);
						idsSpatialRefValsAdded.add(spRefValue.getId());
						break;
					}
				}
			}
			if (!found) {
				SpatialRefValue spRefValue =
					loadOrCreateSpatialRefValueViaDoc(loc, oIn.getId(), false);

				// then create SpatialReference
				SpatialReference spRef = new SpatialReference();
				mapSpatialReference(oIn, spRefValue, spRef, line);
				spatialRefs.add(spRef);
				idsSpatialRefValsAdded.add(spRefValue.getId());
			}
			line++;
		}
		// remove the ones not processed, will be deleted by hibernate (delete-orphan set in parent)
		for (SpatialReference spRef : spatialRefs_unprocessed) {
			spatialRefs.remove(spRef);
			SpatialRefValue spRefValue = spRef.getSpatialRefValue();

			// delete reference. delete-orphan doesn't work !!!?????
			dao.makeTransient(spRef);
			
			// also delete spRefValue if FREE spatial ref ! Every Object has its own FREE refs
			if (SpatialReferenceType.FREI.getDbValue().equals(spRefValue.getType()) &&
					!idsSpatialRefValsAdded.contains(spRefValue.getId())) {
				dao.makeTransient(spRefValue);				
			}
		}		
	}

	/** Update SpatialRefValue of catalog. NOTICE: CATALOG SPATIAL REF IS ALWAYS FROM GEOTHESAURUS !!! */
	private void updateSpatialRefValueOfCatalogue(IngridDocument cDocIn, T03Catalogue cIn) {
		IngridDocument locDoc = (IngridDocument) cDocIn.get(MdekKeys.CATALOG_LOCATION);

		String locSnsId = locDoc.getString(MdekKeys.LOCATION_SNS_ID);
		if (locSnsId == null) {
			throw new MdekException(new MdekError(MdekErrorType.CATALOG_DATA_MISSING));
		}

		SpatialRefValue spRefValue = cIn.getSpatialRefValue();

		boolean updated = false;
		if (spRefValue != null) {
			updated = updateSpatialRefValueViaDoc(locDoc, spRefValue);
		}
		if (!updated) {
			// pass NULL as object reference, so it always will be created if FREE spatial ref
			spRefValue = loadOrCreateSpatialRefValueViaDoc(locDoc, null, false);

			cIn.setSpatialRefId(spRefValue.getId());
			cIn.setSpatialRefValue(spRefValue);
		}
	}

	/** Checks whether passed location doc represents passed SpatialRefValue and updates
	 * data in entity if so.
	 * @param locationDoc data describing SpatialRefValue
	 * @param spRefValue SpatialRefValue entity
	 * @return true=spRefValue entity was updated<br>
	 * false=spRefValue entity is different from locationDoc and was NOT updated
	 */
	private boolean updateSpatialRefValueViaDoc(IngridDocument locationDoc, SpatialRefValue spRefValue) {
		// Further checks: NPE was thrown, see https://dev.wemove.com/jira/browse/INGRID-1911
		if (locationDoc == null || spRefValue == null) {
			return false;
		}

		SpatialRefSns spRefSns = spRefValue.getSpatialRefSns();

		// to be more robust compare data dependent from type !!!!!!!
		// NOTICE: this compare has to match comparison in daoSpatialRefValue.loadOrCreate(...) !!!
		boolean doUpdate = false;
		if (MdekUtils.isEqual(locationDoc.getString(MdekKeys.LOCATION_TYPE), spRefValue.getType())) {
			// type matches !
			if (MdekUtils.SpatialReferenceType.FREI.getDbValue().equals(spRefValue.getType())) {
				// FREE Spatial Ref
				Integer inNameKey = (Integer) locationDoc.get(MdekKeys.LOCATION_NAME_KEY);
				if (MdekUtils.isEqual(inNameKey, spRefValue.getNameKey())) {
					// same name key (is never null)
					if (inNameKey.equals(-1)) {
						// freier eintrag, check name, name is criteria !
						if (MdekUtils.isEqual(locationDoc.getString(MdekKeys.LOCATION_NAME), spRefValue.getNameValue())) {
							doUpdate = true;
						}
					} else {
						// combo entry, we do NOT check name, key is criteria !
						doUpdate = true;
					}
				}

			} else if (MdekUtils.SpatialReferenceType.GEO_THESAURUS.getDbValue().equals(spRefValue.getType())) {
				// SNS Spatial Ref
				if (spRefSns != null && 
					MdekUtils.isEqual(locationDoc.getString(MdekKeys.LOCATION_SNS_ID), spRefSns.getSnsId())) {
					doUpdate = true;
				}
			}
		}
		
		if (doUpdate) {
			mapSpatialRefValue(spRefSns, locationDoc, spRefValue);
			return true;
		}

		return false;
	}

	/** Load/Create SpatialRefValue entity according to the passed location document.
	 * @param locationDoc data describing SpatialRefValue
	 * @param objectId SpatialRef is connected to this object, PASS NULL IF CONNECTION DOESN'T MATTER
	 * @param persistAllData normally result contains complete data of doc but NOT fully persisted yet.
	 * 		pass true if all data should be persisted here (not necessary if cascaded save is called afterwards).
	 * @return persistent SpatialRefValue (with Id) BUT maybe not fully persisted data (dependent from
	 * 		passed persistAllData)
	 */
	public SpatialRefValue loadOrCreateSpatialRefValueViaDoc(IngridDocument locationDoc, Long objectId,
			boolean persistAllData) {
		// first load/create SpatialRefSns
		String locSnsId = (String) locationDoc.get(MdekKeys.LOCATION_SNS_ID);
		SpatialRefSns spRefSns = null;
		if (locSnsId != null) {
			spRefSns = daoSpatialRefSns.loadOrCreate(locSnsId);
		}

		String locNameValue = locationDoc.getString(MdekKeys.LOCATION_NAME);
		Integer locNameKey = (Integer) locationDoc.get(MdekKeys.LOCATION_NAME_KEY);
		String locType = locationDoc.getString(MdekKeys.LOCATION_TYPE);

		// then load/create SpatialRefValue
		// NOTICE: Freie Raumbezuege (SpatialRefValue) werden IMMER neu angelegt, wenn die Objektbeziehung nicht vorhanden ist.
		// Selbst wenn der identische Freie Raumbezug vorhanden ist. Beim Loeschen des Objektes wird nur die Referenz (SpatialReference)
		// geloescht (cascade nicht moeglich, da hier auch Thesaurusbegriffe drin stehen, die erhalten bleiben sollen ! bei denen wird
		// der vorhandene Thesaurus Begriff genommen, wenn schon da; dies ist bei Freien nicht moeglich, da die ja objektspezifisch
		// geaendert werden koennen -> vom Frontend kommen immer die akt. Freien, die Prüfung ob Freier schon da erfolgt dann über fast
		// alle Attribute, bis auf BBox Koordinaten, wenn gefunden, werden also die BBox Koords aktualisiert). -> Aufraeum Job noetig !
		// NEIN, nicht mehr noetig, da Freie Raumbezuege jetzt geloescht werden, wenn Ihre Referenz zum Objekt gelöscht wird, s.updateSpatialReferences(...)
		SpatialRefValue spRefValue =
			daoSpatialRefValue.loadOrCreate(locType, locNameValue, locNameKey, spRefSns, objectId);
		mapSpatialRefValue(spRefSns, locationDoc, spRefValue);
		
		if (persistAllData) {
			dao.makePersistent(spRefValue);
		}

		return spRefValue;
	}

	public void updateObjectComments(IngridDocument oDocIn, T01Object oIn) {
		Set<ObjectComment> refs = oIn.getObjectComments();
		ArrayList<ObjectComment> refs_unprocessed = new ArrayList<ObjectComment>(refs);
		// remove all !
		for (ObjectComment ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			dao.makeTransient(ref);			
		}		
		
		List<IngridDocument> refDocs = (List) oDocIn.get(MdekKeys.COMMENT_LIST);
		if (refDocs != null) {
			// and add all new ones !
			String now = MdekUtils.dateToTimestamp(new Date());
			int line = 1;
			for (IngridDocument refDoc : refDocs) {
				ObjectComment ref = new ObjectComment();
				ref.setObjId(oIn.getId());
				ref.setComment(refDoc.getString(MdekKeys.COMMENT));
				String createTime = refDoc.getString(MdekKeys.CREATE_TIME);
				if (createTime == null) {
					createTime = now;
				}
				ref.setCreateTime(createTime);
				ref.setCreateUuid(extractUserUuid(refDoc, MdekKeys.CREATE_USER));
				ref.setLine(line);

				refs.add(ref);
				line++;
			}
		}
	}
	
	public void updateAddressComments(IngridDocument aDocIn, T02Address aIn) {
		Set<AddressComment> refs = aIn.getAddressComments();
		ArrayList<AddressComment> refs_unprocessed = new ArrayList<AddressComment>(refs);
		// remove all !
		for (AddressComment ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			dao.makeTransient(ref);			
		}		
		
		List<IngridDocument> refDocs = (List) aDocIn.get(MdekKeys.COMMENT_LIST);
		if (refDocs != null) {
			// and add all new ones !
			String now = MdekUtils.dateToTimestamp(new Date());
			int line = 1;
			for (IngridDocument refDoc : refDocs) {
				AddressComment ref = new AddressComment();
				ref.setAddrId(aIn.getId());
				ref.setComment(refDoc.getString(MdekKeys.COMMENT));
				String createTime = refDoc.getString(MdekKeys.CREATE_TIME);
				if (createTime == null) {
					createTime = now;
				}
				ref.setCreateTime(createTime);
				ref.setCreateUuid(extractUserUuid(refDoc, MdekKeys.CREATE_USER));
				ref.setLine(line);

				refs.add(ref);
				line++;
			}
		}
	}

	private void updateAddressMetadata(IngridDocument aDocIn, T02Address aIn) {
		AddressMetadata ref = aIn.getAddressMetadata();
		if (ref == null) {
			// initialize new Metadata (NOT DONE BY HIBERBNATE WITH DEFAULT VALUES FROM DATABASE WHEN PERSISTED :( )
			ref = new AddressMetadata();
			ref.setMarkDeleted(MdekUtils.NO);
			ref.setExpiryState(MdekUtils.ExpiryState.INITIAL.getDbValue());
			// save the address to get ID from database (not handled by hibernate ?)
			dao.makePersistent(ref);
			aIn.setAddrMetadataId(ref.getId());
			aIn.setAddressMetadata(ref);
		}

		// update only if set (so address keeps former values) !
		if (aDocIn.containsKey(MdekKeys.LASTEXPORT_TIME)) {
			ref.setLastexportTime(aDocIn.getString(MdekKeys.LASTEXPORT_TIME));			
		}
		if (aDocIn.containsKey(MdekKeys.EXPIRY_STATE)) {
			ref.setExpiryState((Integer) aDocIn.get(MdekKeys.EXPIRY_STATE));
		}
		if (aDocIn.containsKey(MdekKeys.MARK_DELETED)) {
			ref.setMarkDeleted(aDocIn.getString(MdekKeys.MARK_DELETED));
		}
		if (aDocIn.containsKey(MdekKeys.ASSIGNER_UUID)) {
			ref.setAssignerUuid(aDocIn.getString(MdekKeys.ASSIGNER_UUID));
		}
		if (aDocIn.containsKey(MdekKeys.ASSIGN_TIME)) {
			ref.setAssignTime(aDocIn.getString(MdekKeys.ASSIGN_TIME));
		}
		if (aDocIn.containsKey(MdekKeys.REASSIGNER_UUID)) {
			ref.setReassignerUuid(aDocIn.getString(MdekKeys.REASSIGNER_UUID));
		}
		if (aDocIn.containsKey(MdekKeys.REASSIGN_TIME)) {
			ref.setReassignTime(aDocIn.getString(MdekKeys.REASSIGN_TIME));
		}

	}

	private void updateT021Communications(IngridDocument aDocIn, T02Address aIn) {
		Set<T021Communication> refs = aIn.getT021Communications();
		ArrayList<T021Communication> refs_unprocessed = new ArrayList<T021Communication>(refs);
		// remove all !
		for (T021Communication ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			dao.makeTransient(ref);			
		}

		// and add new ones !
		List<IngridDocument> refDocs = (List) aDocIn.get(MdekKeys.COMMUNICATION);
		if (refDocs == null) {
			refDocs = new ArrayList<IngridDocument>(0);
		}
		// and add all new ones !
		int line = 1;
		for (IngridDocument refDoc : refDocs) {
			T021Communication ref = mapT021Communication(aIn, refDoc, new T021Communication(), line);
			refs.add(ref);
			line++;
		}
	}
	private T021Communication mapT021Communication(T02Address aFrom,
		IngridDocument refDoc,
		T021Communication ref,
		int line)
	{
		ref.setAdrId(aFrom.getId());
		ref.setCommtypeValue(refDoc.getString(MdekKeys.COMMUNICATION_MEDIUM));
		ref.setCommtypeKey((Integer)refDoc.get(MdekKeys.COMMUNICATION_MEDIUM_KEY));
		ref.setCommValue(refDoc.getString(MdekKeys.COMMUNICATION_VALUE));
		ref.setDescr(refDoc.getString(MdekKeys.COMMUNICATION_DESCRIPTION));
		ref.setLine(line);
		keyValueService.processKeyValue(ref);

		return ref;
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
		urlRef.setDescr((String) urlDoc.get(MdekKeys.LINKAGE_DESCRIPTION));
		urlRef.setContent((String) urlDoc.get(MdekKeys.LINKAGE_NAME));
		urlRef.setUrlType((Integer) urlDoc.get(MdekKeys.LINKAGE_URL_TYPE));		
		urlRef.setLine(line);
		keyValueService.processKeyValue(urlRef);

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
			dao.makeTransient(ref);			
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
			dao.makeTransient(ref);			
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
			IngridDocument refDoc,
			T014InfoImpart ref, 
			int line) 
	{
		ref.setObjId(oFrom.getId());
		ref.setImpartValue(refDoc.getString(MdekKeys.EXPORT_CRITERION_VALUE));
		ref.setImpartKey((Integer)refDoc.get(MdekKeys.EXPORT_CRITERION_KEY));
		ref.setLine(line);
		keyValueService.processKeyValue(ref);

		return ref;
	}
	private void updateT014InfoImparts(IngridDocument oDocIn, T01Object oIn) {
		List<IngridDocument> refDocs = (List) oDocIn.get(MdekKeys.EXPORT_CRITERIA);
		if (refDocs == null) {
			refDocs = new ArrayList<IngridDocument>(0);
		}
		Set<T014InfoImpart> refs = oIn.getT014InfoImparts();
		ArrayList<T014InfoImpart> refs_unprocessed = new ArrayList<T014InfoImpart>(refs);
		// remove all !
		for (T014InfoImpart ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			dao.makeTransient(ref);			
		}		
		// and add all new ones !
		int line = 1;
		for (IngridDocument refDoc : refDocs) {
			// add all as new ones
			T014InfoImpart ref = mapT014InfoImpart(oIn, refDoc, new T014InfoImpart(), line);
			refs.add(ref);
			line++;
		}
	}

	private void updateT011ObjGeo(IngridDocument oDocIn, T01Object oIn) {
		Set<T011ObjGeo> refs = oIn.getT011ObjGeos();
		ArrayList<T011ObjGeo> refs_unprocessed = new ArrayList<T011ObjGeo>(refs);
		// remove all !
		for (T011ObjGeo ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			dao.makeTransient(ref);			
		}
		
		IngridDocument refDoc = (IngridDocument)oDocIn.get(MdekKeys.TECHNICAL_DOMAIN_MAP);
		if (refDoc != null) {
			// and the new one, should be only one, because of the 1:1 relation of tables 
			T011ObjGeo ref = new T011ObjGeo();
			ref.setObjId(oIn.getId());
			ref.setSpecialBase(refDoc.getString(MdekKeys.TECHNICAL_BASE));
			ref.setDataBase(refDoc.getString(MdekKeys.DATA));
			ref.setMethod(refDoc.getString(MdekKeys.METHOD_OF_PRODUCTION));
			ref.setRecExact((Double)refDoc.get(MdekKeys.RESOLUTION));
			ref.setRecGrade((Double)refDoc.get(MdekKeys.DEGREE_OF_RECORD));
			ref.setHierarchyLevel((Integer)refDoc.get(MdekKeys.HIERARCHY_LEVEL));
			ref.setVectorTopologyLevel((Integer)refDoc.get(MdekKeys.VECTOR_TOPOLOGY_LEVEL));
			ref.setPosAccuracyVertical((Double)refDoc.get(MdekKeys.POS_ACCURACY_VERTICAL));
			ref.setKeycInclWDataset((Integer)refDoc.get(MdekKeys.KEYC_INCL_W_DATASET));
			ref.setDatasourceUuid(refDoc.getString(MdekKeys.DATASOURCE_UUID));

			// save the object and get ID from database (cascading insert do not work??)
			dao.makePersistent(ref);
			
			// map 1:N relations
			updateT011ObjGeoScales(refDoc, ref);
			updateT011ObjGeoSymcs(refDoc, ref);
			updateT011ObjGeoSupplinfos(refDoc, ref);
			updateT011ObjGeoVectors(refDoc, ref);
			updateT011ObjGeoSpatialReps(refDoc, ref);
			
			refs.add(ref);
		}
	}
	
	private void updateT011ObjGeoScales(IngridDocument docIn, T011ObjGeo in) {
		Set<T011ObjGeoScale> refs = in.getT011ObjGeoScales();
		ArrayList<T011ObjGeoScale> refs_unprocessed = new ArrayList<T011ObjGeoScale>(refs);
		// remove all !
		for (T011ObjGeoScale ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			dao.makeTransient(ref);			
		}		
		
		List<IngridDocument> refDocs = (List<IngridDocument>)docIn.get(MdekKeys.PUBLICATION_SCALE_LIST);
		if (refDocs != null) {
			// and add all new ones !
			int line = 1;
			for (IngridDocument refDoc : refDocs) {
				// add all as new ones
				T011ObjGeoScale ref = new T011ObjGeoScale();
				ref.setObjGeoId(in.getId());
				ref.setScale((Integer)refDoc.get(MdekKeys.SCALE));
				ref.setResolutionGround((Double)refDoc.get(MdekKeys.RESOLUTION_GROUND));
				ref.setResolutionScan((Double)refDoc.get(MdekKeys.RESOLUTION_SCAN));
				ref.setLine(line);
				refs.add(ref);
				line++;
			}
		}
	}	

	private void updateT011ObjGeoSymcs(IngridDocument docIn, T011ObjGeo in) {
		Set<T011ObjGeoSymc> refs = in.getT011ObjGeoSymcs();
		ArrayList<T011ObjGeoSymc> refs_unprocessed = new ArrayList<T011ObjGeoSymc>(refs);
		// remove all !
		for (T011ObjGeoSymc ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			dao.makeTransient(ref);			
		}		
		
		List<IngridDocument> refDocs = (List<IngridDocument>)docIn.get(MdekKeys.SYMBOL_CATALOG_LIST);
		if (refDocs != null) {
			// and add all new ones !
			int line = 1;
			for (IngridDocument refDoc : refDocs) {
				// add all as new ones
				T011ObjGeoSymc ref = new T011ObjGeoSymc();
				ref.setObjGeoId(in.getId());
				ref.setSymbolCatValue(refDoc.getString(MdekKeys.SYMBOL_CAT));
				ref.setSymbolCatKey((Integer)refDoc.get(MdekKeys.SYMBOL_CAT_KEY));
				ref.setSymbolDate(refDoc.getString(MdekKeys.SYMBOL_DATE));
				ref.setEdition(refDoc.getString(MdekKeys.SYMBOL_EDITION));
				ref.setLine(line);
				keyValueService.processKeyValue(ref);
				refs.add(ref);
				line++;
			}
		}
	}
	
	private void updateT011ObjGeoSupplinfos(IngridDocument docIn, T011ObjGeo in) {
		Set<T011ObjGeoSupplinfo> refs = in.getT011ObjGeoSupplinfos();
		ArrayList<T011ObjGeoSupplinfo> refs_unprocessed = new ArrayList<T011ObjGeoSupplinfo>(refs);
		// remove all !
		for (T011ObjGeoSupplinfo ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			dao.makeTransient(ref);			
		}		
		
		List<String> refStrs = (List<String>)docIn.get(MdekKeys.FEATURE_TYPE_LIST);
		if (refStrs != null) {
			// and add all new ones !
			int line = 1;
			for (String refStr : refStrs) {
				// add all as new ones
				T011ObjGeoSupplinfo ref = new T011ObjGeoSupplinfo();
				ref.setObjGeoId(in.getId());
				ref.setFeatureType(refStr);
				ref.setLine(line);
				refs.add(ref);
				line++;
			}
		}
	}	

	private void updateT011ObjGeoVectors(IngridDocument docIn, T011ObjGeo in) {
		Set<T011ObjGeoVector> refs = in.getT011ObjGeoVectors();
		ArrayList<T011ObjGeoVector> refs_unprocessed = new ArrayList<T011ObjGeoVector>(refs);
		// remove all !
		for (T011ObjGeoVector ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			dao.makeTransient(ref);			
		}		
		
		List<IngridDocument> refDocs = (List<IngridDocument>)docIn.get(MdekKeys.GEO_VECTOR_LIST);
		if (refDocs != null) {
			// and add all new ones !
			int line = 1;
			for (IngridDocument refDoc : refDocs) {
				// add all as new ones
				T011ObjGeoVector ref = new T011ObjGeoVector();
				ref.setObjGeoId(in.getId());
				ref.setGeometricObjectType((Integer)refDoc.get(MdekKeys.GEOMETRIC_OBJECT_TYPE));
				ref.setGeometricObjectCount((Integer)refDoc.get(MdekKeys.GEOMETRIC_OBJECT_COUNT));
				ref.setLine(line);
				refs.add(ref);
				line++;
			}
		}
	}	

	private void updateT011ObjGeoSpatialReps(IngridDocument docIn, T011ObjGeo in) {
		Set<T011ObjGeoSpatialRep> refs = in.getT011ObjGeoSpatialReps();
		ArrayList<T011ObjGeoSpatialRep> refs_unprocessed = new ArrayList<T011ObjGeoSpatialRep>(refs);
		// remove all !
		for (T011ObjGeoSpatialRep ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			dao.makeTransient(ref);			
		}		
		
		List<Integer> refInts = (List<Integer>)docIn.get(MdekKeys.SPATIAL_REPRESENTATION_TYPE_LIST);
		if (refInts != null) {
			// and add all new ones !
			int line = 1;
			for (Integer refInt : refInts) {
				// add all as new ones
				T011ObjGeoSpatialRep ref = new T011ObjGeoSpatialRep();
				ref.setObjGeoId(in.getId());
				ref.setType(refInt);
				ref.setLine(line);
				refs.add(ref);
				line++;
			}
		}
	}	
	

	private void updateT011ObjLiterature(IngridDocument oDocIn, T01Object oIn) {
		Set<T011ObjLiterature> refs = oIn.getT011ObjLiteratures();
		ArrayList<T011ObjLiterature> refs_unprocessed = new ArrayList<T011ObjLiterature>(refs);
		// remove all !
		for (T011ObjLiterature ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			dao.makeTransient(ref);			
		}
		
		IngridDocument refDoc = (IngridDocument)oDocIn.get(MdekKeys.TECHNICAL_DOMAIN_DOCUMENT);
		if (refDoc != null) {
			// and the new one, should be only one, because of the 1:1 relation of tables 
			T011ObjLiterature ref = new T011ObjLiterature();
			ref.setObjId(oIn.getId());
			ref.setAuthor(refDoc.getString(MdekKeys.AUTHOR));
			ref.setBase(refDoc.getString(MdekKeys.SOURCE));
			ref.setDescription(refDoc.getString(MdekKeys.DESCRIPTION_OF_TECH_DOMAIN));
			ref.setDocInfo(refDoc.getString(MdekKeys.ADDITIONAL_BIBLIOGRAPHIC_INFO));
			ref.setIsbn(refDoc.getString(MdekKeys.ISBN));
			ref.setLoc(refDoc.getString(MdekKeys.LOCATION));
			ref.setPublisher(refDoc.getString(MdekKeys.EDITOR));
			ref.setPublishIn(refDoc.getString(MdekKeys.PUBLISHED_IN));
			ref.setPublishing(refDoc.getString(MdekKeys.PUBLISHER));
			ref.setPublishLoc(refDoc.getString(MdekKeys.PUBLISHING_PLACE));
			ref.setPublishYear(refDoc.getString(MdekKeys.YEAR));
			ref.setSides(refDoc.getString(MdekKeys.PAGES));
			ref.setTypeValue(refDoc.getString(MdekKeys.TYPE_OF_DOCUMENT));
			ref.setTypeKey((Integer)refDoc.get(MdekKeys.TYPE_OF_DOCUMENT_KEY));
			ref.setVolume(refDoc.getString(MdekKeys.VOLUME));
			keyValueService.processKeyValue(ref);
			refs.add(ref);
		}
		
	}	
	
	private T015Legist mapT015Legist(T01Object oFrom,
			IngridDocument refDoc,
			T015Legist ref, 
			int line)
	{
		ref.setObjId(oFrom.getId());
		ref.setLegistValue(refDoc.getString(MdekKeys.LEGISLATION_VALUE));
		ref.setLegistKey((Integer)refDoc.get(MdekKeys.LEGISLATION_KEY));
		ref.setLine(line);
		keyValueService.processKeyValue(ref);

		return ref;
	}
	private void updateT015Legists(IngridDocument oDocIn, T01Object oIn) {
		List<IngridDocument> refDocs = (List) oDocIn.get(MdekKeys.LEGISLATIONS);
		if (refDocs == null) {
			refDocs = new ArrayList<IngridDocument>(0);
		}
		Set<T015Legist> refs = oIn.getT015Legists();
		ArrayList<T015Legist> refs_unprocessed = new ArrayList<T015Legist>(refs);
		// remove all !
		for (T015Legist ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			dao.makeTransient(ref);			
		}		
		// and add all new ones !
		int line = 1;
		for (IngridDocument refDoc : refDocs) {
			// add all as new ones
			T015Legist ref = mapT015Legist(oIn, refDoc, new T015Legist(), line);
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
		ref.setFormatValue((String) refDoc.get(MdekKeys.FORMAT_NAME));
		ref.setFormatKey((Integer) refDoc.get(MdekKeys.FORMAT_NAME_KEY));
		ref.setVer((String) refDoc.get(MdekKeys.FORMAT_VERSION));
		ref.setSpecification((String) refDoc.get(MdekKeys.FORMAT_SPECIFICATION));
		ref.setFileDecompressionTechnique((String) refDoc.get(MdekKeys.FORMAT_FILE_DECOMPRESSION_TECHNIQUE));
		ref.setLine(line);
		keyValueService.processKeyValue(ref);

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
			dao.makeTransient(ref);			
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
			dao.makeTransient(ref);			
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

	private SearchtermObj mapSearchtermObj(T01Object oFrom,
		SearchtermValue refValue,
		SearchtermObj ref,
		int line) 
	{
		ref.setObjId(oFrom.getId());
		ref.setSearchtermValue(refValue);			
		ref.setSearchtermId(refValue.getId());
		ref.setLine(line);

		return ref;
	}
	private SearchtermAdr mapSearchtermAdr(T02Address aFrom,
		SearchtermValue refValue,
		SearchtermAdr ref,
		int line)
	{
		ref.setAdrId(aFrom.getId());
		ref.setSearchtermValue(refValue);			
		ref.setSearchtermId(refValue.getId());
		ref.setLine(line);

		return ref;
	}

	/** Just map searchterm doc to bean for better handling of searchterms ! NO DATABASE BEAN !!! */
	public SearchtermValue mapHelperSearchtermValue(IngridDocument refDoc,
			SearchtermValue refValue)
	{
		if (refDoc == null) {
			return null;
		}

		SearchtermSns refSns = refValue.getSearchtermSns();
		if (refSns == null) {
			refSns = new SearchtermSns();
		}
		mapSearchtermSns(refDoc, refSns);
		mapSearchtermValue(refSns, refDoc, refValue);

		return refValue;
	}

	public SearchtermValue mapSearchtermValue(SearchtermSns refSns,
		IngridDocument refDoc,
		SearchtermValue refValue) 
	{
		refValue.setTerm(refDoc.getString(MdekKeys.TERM_NAME));
		refValue.setAlternateTerm(refDoc.getString(MdekKeys.TERM_ALTERNATE_NAME));
		refValue.setType(refDoc.getString(MdekKeys.TERM_TYPE));
		refValue.setEntryId((Integer) refDoc.get(MdekKeys.TERM_ENTRY_ID));
		keyValueService.processKeyValue(refValue);

		Long refSnsId = null;
		if (refSns != null) {
			refSnsId = refSns.getId();			
		}
		refValue.setSearchtermSns(refSns);			
		refValue.setSearchtermSnsId(refSnsId);

		return refValue;
	}
	public SearchtermSns mapSearchtermSns(IngridDocument refDoc,
			SearchtermSns refValue) 
	{
		refValue.setSnsId(refDoc.getString(MdekKeys.TERM_SNS_ID));			
		refValue.setGemetId(refDoc.getString(MdekKeys.TERM_GEMET_ID));			

		return refValue;
	}
	private void updateSearchtermObjs(IngridDocument oDocIn, T01Object oIn) {
		updateSearchterms(IdcEntityType.OBJECT, oDocIn, oIn);
	}
	private void updateSearchtermAdrs(IngridDocument aDocIn, T02Address aIn) {
		updateSearchterms(IdcEntityType.ADDRESS, aDocIn, aIn);		
	}
	private void updateSearchterms(IdcEntityType entityType, IngridDocument docIn, IEntity entityIn) {
		List<IngridDocument> inTermDocs = (List) docIn.get(MdekKeys.SUBJECT_TERMS);
		if (inTermDocs == null) {
			inTermDocs = new ArrayList<IngridDocument>(0);
		}
		List<IngridDocument> inTermInspireDocs = (List) docIn.get(MdekKeys.SUBJECT_TERMS_INSPIRE);
		if (inTermInspireDocs == null) {
			inTermInspireDocs = new ArrayList<IngridDocument>(0);
		}
		// combine all terms for better processing !
		inTermDocs.addAll(inTermInspireDocs);

		Set<IEntity> termEntityRefs;
		if (entityType == IdcEntityType.OBJECT) {
			termEntityRefs = ((T01Object)entityIn).getSearchtermObjs();			
		} else {
			termEntityRefs = ((T02Address)entityIn).getSearchtermAdrs();			
		}
		ArrayList<IEntity> termEntityRefs_unprocessed = new ArrayList<IEntity>(termEntityRefs);

		int line = 1;
		for (IngridDocument inTermDoc : inTermDocs) {
			// check on already existing searchterm, compare searchterm values !
			boolean found = false;
			for (IEntity termEntityRef : termEntityRefs) {
				SearchtermValue termValue;
				if (entityType == IdcEntityType.OBJECT) {
					termValue = ((SearchtermObj)termEntityRef).getSearchtermValue();
				} else {
					termValue = ((SearchtermAdr)termEntityRef).getSearchtermValue();
				}
				if (termValue != null) {
					found = updateSearchtermValueViaDoc(inTermDoc, termValue);
					if (found) {
						// update line
						if (entityType == IdcEntityType.OBJECT) {
							((SearchtermObj)termEntityRef).setLine(line);
						} else {
							((SearchtermAdr)termEntityRef).setLine(line);
						}
						termEntityRefs_unprocessed.remove(termEntityRef);
						found = true;
						break;
					}
				}
			}
			if (!found) {
				// add new one

				// first load/create SearchtermValue
				SearchtermValue termValue = loadOrCreateSearchtermValueViaDoc(
						inTermDoc, (Long)entityIn.getId(), entityType, false);

				// then create connection to entity
				IEntity termEntityRef;
				if (entityType == IdcEntityType.OBJECT) {
					termEntityRef = new SearchtermObj();
					mapSearchtermObj((T01Object)entityIn, termValue, (SearchtermObj)termEntityRef, line);
				} else {
					termEntityRef = new SearchtermAdr();
					mapSearchtermAdr((T02Address)entityIn, termValue, (SearchtermAdr)termEntityRef, line);
				}
				termEntityRefs.add(termEntityRef);
			}
			line++;
		}
		// remove the ones not processed, will be deleted by hibernate (delete-orphan set in parent)
		for (IEntity ref : termEntityRefs_unprocessed) {
			termEntityRefs.remove(ref);
/*
			SearchtermValue termValue;
			if (entityType == IdcEntityType.OBJECT) {
				termValue = ((SearchtermObj)ref).getSearchtermValue();
			} else {
				termValue = ((SearchtermAdr)ref).getSearchtermValue();
			}
*/
			// delete reference. delete-orphan doesn't work !!!?????
			dao.makeTransient(ref);
			
			// also delete termValue if FREE term ! Every Entity has its own FREE terms !
			// NO !!!!! Initial Migration refers to SAME FREE termValue MULTIPLE TIMES FROM DIFFERENT OBJECTS !!!
			// SO DO NOT DELETE termValue to avoid MISSING termValues when loading other Objects !!!
/*
			if (SearchtermType.FREI.getDbValue().equals(termValue.getType())) {
				dao.makeTransient(termValue);				
			}
*/
		}		
	}

	/** Checks whether passed term doc represents passed SearchtermValue and updates
	 * data in entity if so.
	 * @param inTermDoc data describing SearchtermValue
	 * @param termValue SearchtermValue entity
	 * @return true=termValue entity was updated<br>
	 * false=termValue entity is different from inTermDoc and was NOT updated
	 */
	private boolean updateSearchtermValueViaDoc(IngridDocument inTermDoc, SearchtermValue termValue) {
		SearchtermSns termSns = termValue.getSearchtermSns();

		// to be more robust compare data dependent from type !!!!!!!
		// NOTICE: this compare has to match comparison in daoSearchtermValue.loadOrCreate(...) !!!
		boolean doUpdate = false;
		if (MdekUtils.isEqual(inTermDoc.getString(MdekKeys.TERM_TYPE), termValue.getType())) {
			// type matches !
			SearchtermType termValType = EnumUtil.mapDatabaseToEnumConst(SearchtermType.class, termValue.getType());

			if (SearchtermType.FREI == termValType) {
				// FREE Searchterm
				if (MdekUtils.isEqual(inTermDoc.getString(MdekKeys.TERM_NAME), termValue.getTerm())) {
					doUpdate = true;
				}
				
			} else if (SearchtermType.INSPIRE == termValType) {
				// INSPIRE Term
				if (MdekUtils.isEqual((Integer)inTermDoc.get(MdekKeys.TERM_ENTRY_ID), termValue.getEntryId())) {
					doUpdate = true;
				}

			} else if (SearchtermType.isThesaurusType(termValType)) {
				// SNS Term
				if (MdekUtils.isEqual(inTermDoc.getString(MdekKeys.TERM_SNS_ID), termSns.getSnsId())) {
					doUpdate = true;
				}
			}
		}
		
		if (doUpdate) {
			mapSearchtermValue(termSns, inTermDoc, termValue);
			return true;
		}

		return false;
	}

	/** Load/Create SearchtermValue according to the passed term document.
	 * @param inTermDoc data describing SearchtermValue
	 * @param entityId SearchtermValue is connected to this entity, PASS NULL IF CONNECTION DOESN'T MATTER
	 * @param entityType type of entity the term is connected to, PASS NULL IF CONNECTION DOESN'T MATTER
	 * @param persistAllData normally result contains complete data of doc but NOT fully persisted yet.
	 * 		pass true if all data should be persisted here (not necessary if cascaded save is called afterwards).
	 * @return persistent SearchtermValue (with Id) BUT maybe not fully persisted data (dependent from
	 * 		passed persistAllData)
	 */
	public SearchtermValue loadOrCreateSearchtermValueViaDoc(IngridDocument inTermDoc,
			Long entityId, IdcEntityType entityType,
			boolean persistAllData) {
		// first load/create SearchtermSns
		String termSnsId = inTermDoc.getString(MdekKeys.TERM_SNS_ID);
		String termGemetId = inTermDoc.getString(MdekKeys.TERM_GEMET_ID);
		SearchtermSns termSns = null;
		if (termSnsId != null) {
			termSns = daoSearchtermSns.loadOrCreate(termSnsId, termGemetId);
		}

		// then load/create SearchtermValue
		// NOTICE: Freie Schlagwörter (SearchtermValue) werden IMMER neu angelegt, wenn die Objektbeziehung nicht vorhanden ist.
		// gleiches Verhalten wie bei FREIEN RAUMBEZUEGEN, s.o.
		// ABER: ist eigentlich nicht nötig, da ursprüngliche Migration gleiche freie Suchbegriffe NUR EINMAL ANGELEGT HAT
		// und diese mit mehrerern Objekten/Adressen verknüft hat !!!!!!!!! Diese dürfen also NICHT geloescht werden !
		// Gleiche Freie Raumbezuege koennten also auch nur EINMAL existieren !!!
		SearchtermValue termValue = daoSearchtermValue.loadOrCreate(
				inTermDoc.getString(MdekKeys.TERM_TYPE),
				inTermDoc.getString(MdekKeys.TERM_NAME),
				inTermDoc.getString(MdekKeys.TERM_ALTERNATE_NAME),
				(Integer) inTermDoc.get(MdekKeys.TERM_ENTRY_ID),
				termSns,
				entityId, entityType);
		mapSearchtermValue(termSns, inTermDoc, termValue);
		
		if (persistAllData) {
			dao.makePersistent(termValue);
		}

		return termValue;
	}

	private T0114EnvTopic mapT0114EnvTopic(T01Object oFrom,
			Integer refValue,
			T0114EnvTopic ref,
			int line)
	{
		ref.setObjId(oFrom.getId());
		ref.setTopicKey(refValue);
		ref.setLine(line);

		return ref;
	}
	private void updateT0114EnvTopics(IngridDocument oDocIn, T01Object oIn) {
		List<Integer> refValues = (List) oDocIn.get(MdekKeys.ENV_TOPICS);
		if (refValues == null) {
			refValues = new ArrayList<Integer>(0);
		}
		Set<T0114EnvTopic> refs = oIn.getT0114EnvTopics();
		ArrayList<T0114EnvTopic> refs_unprocessed = new ArrayList<T0114EnvTopic>(refs);
		// remove all !
		for (T0114EnvTopic ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			dao.makeTransient(ref);			
		}		
		// and add all new ones !
		int line = 1;
		for (Integer refValue : refValues) {
			T0114EnvTopic ref = mapT0114EnvTopic(oIn, refValue, new T0114EnvTopic(), line);
			refs.add(ref);
			line++;
		}
	}

	private T011ObjTopicCat mapT011ObjTopicCat(T01Object oFrom,
			Integer category,
			T011ObjTopicCat ref,
			int line)
	{
		ref.setObjId(oFrom.getId());
		ref.setTopicCategory(category);
		ref.setLine(line);

		return ref;
	}
	private void updateT011ObjTopicCats(IngridDocument oDocIn, T01Object oIn) {
		List<Integer> refCats = (List) oDocIn.get(MdekKeys.TOPIC_CATEGORIES);
		if (refCats == null) {
			refCats = new ArrayList<Integer>(0);
		}
		Set<T011ObjTopicCat> refs = oIn.getT011ObjTopicCats();
		ArrayList<T011ObjTopicCat> refs_unprocessed = new ArrayList<T011ObjTopicCat>(refs);
		// remove all !
		for (T011ObjTopicCat ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			dao.makeTransient(ref);			
		}		
		// and add all new ones !
		int line = 1;
		for (Integer refCat : refCats) {
			T011ObjTopicCat ref = mapT011ObjTopicCat(oIn, refCat, new T011ObjTopicCat(), line);
			refs.add(ref);
			line++;
		}
	}

	private T011ObjData mapT011ObjData(T01Object oFrom,
			IngridDocument refDoc,
			T011ObjData ref) 
	{
		ref.setObjId(oFrom.getId());
		ref.setBase(refDoc.getString(MdekKeys.METHOD));
		ref.setDescription(refDoc.getString(MdekKeys.DESCRIPTION_OF_TECH_DOMAIN));

		return ref;
	}
	private void updateT011ObjData(IngridDocument oDocIn, T01Object oIn) {
		Set<T011ObjData> refs = oIn.getT011ObjDatas();
		ArrayList<T011ObjData> refs_unprocessed = new ArrayList<T011ObjData>(refs);
		// remove all !
		for (T011ObjData ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			dao.makeTransient(ref);			
		}		
		// and add new one !
		IngridDocument domainDoc = (IngridDocument) oDocIn.get(MdekKeys.TECHNICAL_DOMAIN_DATASET);
		if (domainDoc != null) {
			T011ObjData ref = mapT011ObjData(oIn, domainDoc, new T011ObjData());
			refs.add(ref);
		}
	}

	private T011ObjDataPara mapT011ObjDataPara(T01Object oFrom,
			IngridDocument refDoc,
			T011ObjDataPara ref,
			int line)
	{
		ref.setObjId(oFrom.getId());
		ref.setParameter(refDoc.getString(MdekKeys.PARAMETER));
		ref.setUnit(refDoc.getString(MdekKeys.SUPPLEMENTARY_INFORMATION));
		ref.setLine(line);

		return ref;
	}
	private void updateT011ObjDataParas(IngridDocument oDocIn, T01Object oIn) {
		Set<T011ObjDataPara> refs = oIn.getT011ObjDataParas();
		ArrayList<T011ObjDataPara> refs_unprocessed = new ArrayList<T011ObjDataPara>(refs);
		// remove all !
		for (T011ObjDataPara ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			dao.makeTransient(ref);			
		}

		// and add new ones !
		IngridDocument domainDoc = (IngridDocument) oDocIn.get(MdekKeys.TECHNICAL_DOMAIN_DATASET);
		if (domainDoc != null) {
			List<IngridDocument> refDocs = (List) domainDoc.get(MdekKeys.PARAMETERS);
			if (refDocs == null) {
				refDocs = new ArrayList<IngridDocument>(0);
			}
			// and add all new ones !
			int line = 1;
			for (IngridDocument refDoc : refDocs) {
				T011ObjDataPara ref = mapT011ObjDataPara(oIn, refDoc, new T011ObjDataPara(), line);
				refs.add(ref);
				line++;
			}
		}
	}

	private ObjectTypesCatalogue mapObjectTypesCatalogue(T01Object oFrom,
			IngridDocument refDoc,
			ObjectTypesCatalogue ref,
			int line) {

		ref.setObjId(oFrom.getId());
		ref.setTitleKey((Integer)refDoc.get(MdekKeys.SUBJECT_CAT_KEY));
		ref.setTitleValue(refDoc.getString(MdekKeys.SUBJECT_CAT));
		ref.setTypeDate(refDoc.getString(MdekKeys.KEY_DATE));
		ref.setTypeVersion(refDoc.getString(MdekKeys.EDITION));
		ref.setLine(line);

		keyValueService.processKeyValue(ref);

		return ref;
	}
	private void updateObjectTypesCatalogues(IngridDocument docIn, T01Object oIn) {
		Set<ObjectTypesCatalogue> refs = oIn.getObjectTypesCatalogues();
		ArrayList<ObjectTypesCatalogue> refs_unprocessed = new ArrayList<ObjectTypesCatalogue>(refs);
		// remove all !
		for (ObjectTypesCatalogue ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			dao.makeTransient(ref);			
		}		

		// Used for data of multiple technical domains ! We map all possible domains (so our MdekExamples work :)
		List<IngridDocument> domainDocs = new ArrayList<IngridDocument>();
		domainDocs.add((IngridDocument) docIn.get(MdekKeys.TECHNICAL_DOMAIN_MAP));
		domainDocs.add((IngridDocument) docIn.get(MdekKeys.TECHNICAL_DOMAIN_DATASET));
		int line = 1;
		for (IngridDocument domainDoc : domainDocs) {
			if (domainDoc != null) {
				List<IngridDocument> refDocs = (List<IngridDocument>)domainDoc.get(MdekKeys.KEY_CATALOG_LIST);
				if (refDocs != null) {
					// and add all new ones !
					for (IngridDocument refDoc : refDocs) {
						ObjectTypesCatalogue ref = mapObjectTypesCatalogue(oIn, refDoc, new ObjectTypesCatalogue(), line);
						refs.add(ref);
						line++;
					}
				}
			}
		}
	}

	private T011ObjProject mapT011ObjProject(T01Object oFrom,
			IngridDocument refDoc,
			T011ObjProject ref) 
	{
		ref.setObjId(oFrom.getId());
		ref.setLeader(refDoc.getString(MdekKeys.LEADER_DESCRIPTION));
		ref.setMember(refDoc.getString(MdekKeys.MEMBER_DESCRIPTION));
		ref.setDescription(refDoc.getString(MdekKeys.DESCRIPTION_OF_TECH_DOMAIN));

		return ref;
	}
	private void updateT011ObjProject(IngridDocument oDocIn, T01Object oIn) {
		Set<T011ObjProject> refs = oIn.getT011ObjProjects();
		ArrayList<T011ObjProject> refs_unprocessed = new ArrayList<T011ObjProject>(refs);
		// remove all !
		for (T011ObjProject ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			dao.makeTransient(ref);			
		}		
		// and add new one !
		IngridDocument domainDoc = (IngridDocument) oDocIn.get(MdekKeys.TECHNICAL_DOMAIN_PROJECT);
		if (domainDoc != null) {
			T011ObjProject ref = mapT011ObjProject(oIn, domainDoc, new T011ObjProject());
			refs.add(ref);
		}
	}

	private void updateT011ObjServ(IngridDocument oDocIn, T01Object oIn) {
		Set<T011ObjServ> refs = oIn.getT011ObjServs();

		// NOTICE: This container is used for the object classes "Geodatendienst" AND
		// "Informationssystem/Dienst/Anwendung" with DIFFERENT content !
		// BUT WE ALWAYS MAP EVERYTHING ASSUMING EVERY CLASS HAS ITS RIGHT CONTENT !

		ArrayList<T011ObjServ> refs_unprocessed = new ArrayList<T011ObjServ>(refs);
		// remove all !
		for (T011ObjServ ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			dao.makeTransient(ref);			
		}		
		// and add new one !
		IngridDocument domainDoc = (IngridDocument) oDocIn.get(MdekKeys.TECHNICAL_DOMAIN_SERVICE);
		if (domainDoc != null) {
			T011ObjServ ref = mapT011ObjServ(oIn, domainDoc, new T011ObjServ());

			// save the object and get ID from database (cascading insert do not work??)
			dao.makePersistent(ref);

			// map 1:N relations

			updateT011ObjServVersions(domainDoc, ref);
			// following stuff should only be present in class "Geodatendienst" !
			updateT011ObjServOperations(domainDoc, ref);
			updateT011ObjServTypes(domainDoc, ref);
			updateT011ObjServScales(domainDoc, ref);
			// following stuff should only be present in class "Informationssystem/Dienst/Anwendung" !
			// add URLs
			updateT011ObjServUrls(domainDoc, ref);

			refs.add(ref);
		}
	}
	private T011ObjServ mapT011ObjServ(T01Object oFrom,
			IngridDocument refDoc,
			T011ObjServ ref) 
	{
		ref.setObjId(oFrom.getId());
		ref.setTypeValue(refDoc.getString(MdekKeys.SERVICE_TYPE));
		ref.setTypeKey((Integer)refDoc.get(MdekKeys.SERVICE_TYPE_KEY));
		ref.setHistory(refDoc.getString(MdekKeys.SYSTEM_HISTORY));
		ref.setEnvironment(refDoc.getString(MdekKeys.SYSTEM_ENVIRONMENT));
		ref.setBase(refDoc.getString(MdekKeys.DATABASE_OF_SYSTEM));
		ref.setDescription(refDoc.getString(MdekKeys.DESCRIPTION_OF_TECH_DOMAIN));
		ref.setHasAccessConstraint(refDoc.getString(MdekKeys.HAS_ACCESS_CONSTRAINT));
		keyValueService.processKeyValueT011ObjServ(ref, oFrom);

		return ref;
	}
	private void updateT011ObjServVersions(IngridDocument oDocIn, T011ObjServ oIn) {
		List<String> versions = (List) oDocIn.get(MdekKeys.SERVICE_VERSION_LIST);
		if (versions == null) {
			versions = new ArrayList<String>(0);
		}
		Set<T011ObjServVersion> refs = oIn.getT011ObjServVersions();
		ArrayList<T011ObjServVersion> refs_unprocessed = new ArrayList<T011ObjServVersion>(refs);
		// remove all !
		for (T011ObjServVersion ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			dao.makeTransient(ref);			
		}		
		// and add all new ones !
		int line = 1;
		for (String version : versions) {
			T011ObjServVersion ref = mapT011ObjServVersion(oIn, version, new T011ObjServVersion(), line);
			refs.add(ref);
			line++;
		}
	}
	private T011ObjServVersion mapT011ObjServVersion(T011ObjServ oFrom,
			String version,
			T011ObjServVersion ref,
			int line)
	{
		ref.setObjServId(oFrom.getId());
		ref.setServVersion(version);
		ref.setLine(line);

		return ref;
	}
	private void updateT011ObjServOperations(IngridDocument oDocIn, T011ObjServ oIn) {
		Set<T011ObjServOperation> refs = oIn.getT011ObjServOperations();
		ArrayList<T011ObjServOperation> refs_unprocessed = new ArrayList<T011ObjServOperation>(refs);
		// remove all !
		for (T011ObjServOperation ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			dao.makeTransient(ref);			
		}

		// and add new ones !
		List<IngridDocument> refDocs = (List) oDocIn.get(MdekKeys.SERVICE_OPERATION_LIST);
		if (refDocs == null) {
			refDocs = new ArrayList<IngridDocument>(0);
		}
		// and add all new ones !
		int line = 1;
		for (IngridDocument refDoc : refDocs) {
			T011ObjServOperation ref = mapT011ObjServOperation(oIn, refDoc, new T011ObjServOperation(), line);
			
			// save the object and get ID from database (cascading insert do not work??)
			dao.makePersistent(ref);

			// map 1:N relations
			updateT011ObjServOpPlatforms(refDoc, ref);
			updateT011ObjServOpDependss(refDoc, ref);
			updateT011ObjServOpConnpoints(refDoc, ref);
			updateT011ObjServOpParas(refDoc, ref);

			refs.add(ref);
			line++;
		}
	}
	private T011ObjServOperation mapT011ObjServOperation(T011ObjServ oFrom,
			IngridDocument refDoc,
			T011ObjServOperation ref,
			int line)
	{
		ref.setObjServId(oFrom.getId());
		ref.setNameValue(refDoc.getString(MdekKeys.SERVICE_OPERATION_NAME));
		ref.setNameKey((Integer)refDoc.get(MdekKeys.SERVICE_OPERATION_NAME_KEY));
		ref.setDescr(refDoc.getString(MdekKeys.SERVICE_OPERATION_DESCRIPTION));
		ref.setInvocationName(refDoc.getString(MdekKeys.INVOCATION_NAME));
		ref.setLine(line);
		keyValueService.processKeyValueT011ObjServOperation(ref, oFrom);

		return ref;
	}
	private void updateT011ObjServOpPlatforms(IngridDocument oDocIn, T011ObjServOperation oIn) {
		Set<T011ObjServOpPlatform> refs = oIn.getT011ObjServOpPlatforms();
		ArrayList<T011ObjServOpPlatform> refs_unprocessed = new ArrayList<T011ObjServOpPlatform>(refs);
		// remove all !
		for (T011ObjServOpPlatform ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			dao.makeTransient(ref);			
		}

		// and add all new ones !
		List<IngridDocument> refDocs = (List) oDocIn.get(MdekKeys.PLATFORM_LIST);
		if (refDocs == null) {
			refDocs = new ArrayList<IngridDocument>(0);
		}
		int line = 1;
		for (IngridDocument refDoc : refDocs) {
			T011ObjServOpPlatform ref = mapT011ObjServOpPlatform(oIn, refDoc, new T011ObjServOpPlatform(), line);
			refs.add(ref);
			line++;
		}
	}
	private T011ObjServOpPlatform mapT011ObjServOpPlatform(T011ObjServOperation oFrom,
			IngridDocument refDoc,
			T011ObjServOpPlatform ref,
			int line)
	{
		ref.setObjServOpId(oFrom.getId());
		ref.setPlatformKey((Integer)refDoc.get(MdekKeys.PLATFORM_KEY));
		ref.setPlatformValue(refDoc.getString(MdekKeys.PLATFORM_VALUE));
		ref.setLine(line);
		keyValueService.processKeyValue(ref);

		return ref;
	}
	private void updateT011ObjServOpDependss(IngridDocument oDocIn, T011ObjServOperation oIn) {
		List<String> dependsOns = (List) oDocIn.get(MdekKeys.DEPENDS_ON_LIST);
		if (dependsOns == null) {
			dependsOns = new ArrayList<String>(0);
		}
		Set<T011ObjServOpDepends> refs = oIn.getT011ObjServOpDependss();
		ArrayList<T011ObjServOpDepends> refs_unprocessed = new ArrayList<T011ObjServOpDepends>(refs);
		// remove all !
		for (T011ObjServOpDepends ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			dao.makeTransient(ref);			
		}		
		// and add all new ones !
		int line = 1;
		for (String dependsOn : dependsOns) {
			T011ObjServOpDepends ref = mapT011ObjServOpDepends(oIn, dependsOn, new T011ObjServOpDepends(), line);
			refs.add(ref);
			line++;
		}
	}
	private T011ObjServOpDepends mapT011ObjServOpDepends(T011ObjServOperation oFrom,
			String dependsOn,
			T011ObjServOpDepends ref,
			int line)
	{
		ref.setObjServOpId(oFrom.getId());
		ref.setDependsOn(dependsOn);
		ref.setLine(line);

		return ref;
	}
	private void updateT011ObjServOpConnpoints(IngridDocument oDocIn, T011ObjServOperation oIn) {
		List<String> connectPoints = (List) oDocIn.get(MdekKeys.CONNECT_POINT_LIST);
		if (connectPoints == null) {
			connectPoints = new ArrayList<String>(0);
		}
		Set<T011ObjServOpConnpoint> refs = oIn.getT011ObjServOpConnpoints();
		ArrayList<T011ObjServOpConnpoint> refs_unprocessed = new ArrayList<T011ObjServOpConnpoint>(refs);
		// remove all !
		for (T011ObjServOpConnpoint ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			dao.makeTransient(ref);			
		}		
		// and add all new ones !
		int line = 1;
		for (String connectPoint : connectPoints) {
			T011ObjServOpConnpoint ref = mapT011ObjServOpConnpoint(oIn, connectPoint, new T011ObjServOpConnpoint(), line);
			refs.add(ref);
			line++;
		}
	}
	private T011ObjServOpConnpoint mapT011ObjServOpConnpoint(T011ObjServOperation oFrom,
			String connectPoint,
			T011ObjServOpConnpoint ref,
			int line)
	{
		ref.setObjServOpId(oFrom.getId());
		ref.setConnectPoint(connectPoint);
		ref.setLine(line);

		return ref;
	}
	private void updateT011ObjServOpParas(IngridDocument oDocIn, T011ObjServOperation oIn) {
		Set<T011ObjServOpPara> refs = oIn.getT011ObjServOpParas();
		ArrayList<T011ObjServOpPara> refs_unprocessed = new ArrayList<T011ObjServOpPara>(refs);
		// remove all !
		for (T011ObjServOpPara ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			dao.makeTransient(ref);			
		}

		// and add new ones !
		List<IngridDocument> refDocs = (List) oDocIn.get(MdekKeys.PARAMETER_LIST);
		if (refDocs == null) {
			refDocs = new ArrayList<IngridDocument>(0);
		}
		// and add all new ones !
		int line = 1;
		for (IngridDocument refDoc : refDocs) {
			T011ObjServOpPara ref = mapT011ObjServOpPara(oIn, refDoc, new T011ObjServOpPara(), line);
			refs.add(ref);
			line++;
		}
	}
	private T011ObjServOpPara mapT011ObjServOpPara(T011ObjServOperation oFrom,
			IngridDocument refDoc,
			T011ObjServOpPara ref,
			int line)
	{
		ref.setObjServOpId(oFrom.getId());
		ref.setName(refDoc.getString(MdekKeys.PARAMETER_NAME));
		ref.setDirection(refDoc.getString(MdekKeys.DIRECTION));
		ref.setDescr(refDoc.getString(MdekKeys.DESCRIPTION));
		ref.setOptional((Integer) refDoc.get(MdekKeys.OPTIONALITY));
		ref.setRepeatability((Integer) refDoc.get(MdekKeys.REPEATABILITY));
		ref.setLine(line);

		return ref;
	}
	private void updateT011ObjServTypes(IngridDocument oDocIn, T011ObjServ oIn) {
		List<IngridDocument> refDocs = (List) oDocIn.get(MdekKeys.SERVICE_TYPE2_LIST);
		if (refDocs == null) {
			refDocs = new ArrayList<IngridDocument>(0);
		}
		Set<T011ObjServType> refs = oIn.getT011ObjServTypes();
		ArrayList<T011ObjServType> refs_unprocessed = new ArrayList<T011ObjServType>(refs);
		// remove all !
		for (T011ObjServType ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			dao.makeTransient(ref);			
		}		
		// and add all new ones !
		int line = 1;
		for (IngridDocument refDoc : refDocs) {
			// add all as new ones
			T011ObjServType ref = mapT011ObjServType(oIn, refDoc, new T011ObjServType(), line);
			refs.add(ref);
			line++;
		}
	}
	private T011ObjServType mapT011ObjServType(T011ObjServ oFrom,
			IngridDocument refDoc,
			T011ObjServType ref, 
			int line)
	{
		ref.setObjServId(oFrom.getId());
		ref.setServTypeKey((Integer)refDoc.get(MdekKeys.SERVICE_TYPE2_KEY));
		ref.setServTypeValue(refDoc.getString(MdekKeys.SERVICE_TYPE2_VALUE));
		ref.setLine(line);
		keyValueService.processKeyValue(ref);

		return ref;
	}
	private void updateT011ObjServScales(IngridDocument oDocIn, T011ObjServ oIn) {
		List<IngridDocument> refDocs = (List) oDocIn.get(MdekKeys.PUBLICATION_SCALE_LIST);
		if (refDocs == null) {
			refDocs = new ArrayList<IngridDocument>(0);
		}
		Set<T011ObjServScale> refs = oIn.getT011ObjServScales();
		ArrayList<T011ObjServScale> refs_unprocessed = new ArrayList<T011ObjServScale>(refs);
		// remove all !
		for (T011ObjServScale ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			dao.makeTransient(ref);			
		}		
		// and add all new ones !
		int line = 1;
		for (IngridDocument refDoc : refDocs) {
			// add all as new ones
			T011ObjServScale ref = mapT011ObjServScale(oIn, refDoc, new T011ObjServScale(), line);
			refs.add(ref);
			line++;
		}
	}
	private T011ObjServScale mapT011ObjServScale(T011ObjServ oFrom,
			IngridDocument refDoc,
			T011ObjServScale ref, 
			int line)
	{
		ref.setObjServId(oFrom.getId());
		ref.setScale((Integer)refDoc.get(MdekKeys.SCALE));
		ref.setResolutionGround((Double)refDoc.get(MdekKeys.RESOLUTION_GROUND));
		ref.setResolutionScan((Double)refDoc.get(MdekKeys.RESOLUTION_SCAN));
		ref.setLine(line);

		return ref;
	}
	private void updateT011ObjServUrls(IngridDocument oDocIn, T011ObjServ oIn) {
		List<IngridDocument> refDocs = (List) oDocIn.get(MdekKeys.URL_LIST);
		if (refDocs == null) {
			refDocs = new ArrayList<IngridDocument>(0);
		}
		Set<T011ObjServUrl> refs = oIn.getT011ObjServUrls();
		ArrayList<T011ObjServUrl> refs_unprocessed = new ArrayList<T011ObjServUrl>(refs);
		// remove all !
		for (T011ObjServUrl ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			dao.makeTransient(ref);			
		}		
		// and add all new ones !
		int line = 1;
		for (IngridDocument refDoc : refDocs) {
			// add all as new ones
			T011ObjServUrl ref = mapT011ObjServUrl(oIn, refDoc, new T011ObjServUrl(), line);
			refs.add(ref);
			line++;
		}
	}
	private T011ObjServUrl mapT011ObjServUrl(T011ObjServ oFrom,
			IngridDocument refDoc,
			T011ObjServUrl ref, 
			int line)
	{
		ref.setObjServId(oFrom.getId());
		ref.setName(refDoc.getString(MdekKeys.NAME));
		ref.setUrl(refDoc.getString(MdekKeys.URL));
		ref.setDescription(refDoc.getString(MdekKeys.DESCRIPTION));
		ref.setLine(line);

		return ref;
	}

	private AdditionalFieldData mapAdditionalFieldData(T01Object oFrom,
			IngridDocument doc,
			AdditionalFieldData bean,
			int line,
			Long parentBeanId)
	{
		bean.setFieldKey(doc.getString(MdekKeys.ADDITIONAL_FIELD_KEY));
		if (oFrom != null) {
			bean.setObjId(oFrom.getId());			
		}
		if (doc.get(MdekKeys.ADDITIONAL_FIELD_DATA) != null) {
			bean.setData(doc.getString(MdekKeys.ADDITIONAL_FIELD_DATA));			
		}
		if (doc.get(MdekKeys.ADDITIONAL_FIELD_LIST_ITEM_ID) != null) {
			bean.setListItemId(doc.getString(MdekKeys.ADDITIONAL_FIELD_LIST_ITEM_ID));			
		}
		bean.setParentFieldId(parentBeanId);			
		bean.setSort(line);

		keyValueService.processKeyValue(bean);

		List<List<IngridDocument>> rows = (List<List<IngridDocument>>) doc.get(MdekKeys.ADDITIONAL_FIELD_ROWS);
		if (rows != null && rows.size() > 0) {
			// guarantee, that this bean, including the rows, has an id to be set in "subbeans"
			if (bean.getId() == null) {
				// no id, we have to store, to get an id !
				dao.makePersistent(bean);
			}
			updateAdditionalFieldDatas(bean,
				(List<List<IngridDocument>>) doc.get(MdekKeys.ADDITIONAL_FIELD_ROWS));
		}

		return bean;
	}
	private void updateAdditionalFieldDatas(IngridDocument oDocIn, T01Object oIn) {
		List<IngridDocument> docs = (List) oDocIn.get(MdekKeys.ADDITIONAL_FIELDS);
		if (docs == null) {
			docs = new ArrayList<IngridDocument>(0);
		}

		Set<AdditionalFieldData> beans = oIn.getAdditionalFieldDatas();
		ArrayList<AdditionalFieldData> beansUnprocessed = new ArrayList<AdditionalFieldData>(beans);

		int sort = 0;
		for (IngridDocument doc : docs) {
			boolean found = false;
			// always sort = 0 ! No multiple lines in first level !
//			sort++;
			String docFieldKey = doc.getString(MdekKeys.ADDITIONAL_FIELD_KEY);
			for (AdditionalFieldData bean : beans) {
				if (bean.getFieldKey().equals(docFieldKey)) {
					found = true;
					mapAdditionalFieldData(oIn, doc, bean, sort, null);
					beansUnprocessed.remove(bean);
					break;
				}
			}
			if (!found) {
				AdditionalFieldData bean = new AdditionalFieldData();
				mapAdditionalFieldData(oIn, doc, bean, sort, null);
				beans.add(bean);
			}
		}
		// remove the ones not processed, will be deleted by hibernate (delete-orphan set in parent)
		for (AdditionalFieldData bean : beansUnprocessed) {
			beans.remove(bean);
			// delete-orphan doesn't work !!!?????
			dao.makeTransient(bean);
		}		
	}
	private void updateAdditionalFieldDatas(AdditionalFieldData inBean, List<List<IngridDocument>> rowsList) {
		Set<AdditionalFieldData> beans = inBean.getAdditionalFieldDatas();
		ArrayList<AdditionalFieldData> beansUnprocessed = new ArrayList<AdditionalFieldData>(beans);

		Integer rowNumber = 0;
		for (List<IngridDocument> colList : rowsList) {
			rowNumber++;
			for (IngridDocument doc : colList) {
				boolean found = false;
				String docFieldKey = doc.getString(MdekKeys.ADDITIONAL_FIELD_KEY);
				for (AdditionalFieldData bean : beans) {
					if (bean.getFieldKey().equals(docFieldKey) &&
							rowNumber.equals(bean.getSort())) {
						found = true;
						mapAdditionalFieldData(null, doc, bean, rowNumber, inBean.getId());
						beansUnprocessed.remove(bean);
						break;
					}
				}
				if (!found) {
					AdditionalFieldData bean = new AdditionalFieldData();
					mapAdditionalFieldData(null, doc, bean, rowNumber, inBean.getId());
					beans.add(bean);
				}
			}
		}
		// remove the ones not processed, will be deleted by hibernate (delete-orphan set in parent)
		for (AdditionalFieldData bean : beansUnprocessed) {
			beans.remove(bean);
			// delete-orphan doesn't work !!!?????
			dao.makeTransient(bean);
		}		
	}

	private ObjectConformity mapObjectConformity(T01Object oFrom,
			IngridDocument refDoc,
			ObjectConformity ref, 
			int line)
	{
		ref.setObjId(oFrom.getId());
		ref.setSpecificationKey((Integer)refDoc.get(MdekKeys.CONFORMITY_SPECIFICATION_KEY));
		ref.setSpecificationValue(refDoc.getString(MdekKeys.CONFORMITY_SPECIFICATION_VALUE));
		ref.setDegreeKey((Integer)refDoc.get(MdekKeys.CONFORMITY_DEGREE_KEY));
		ref.setDegreeValue(refDoc.getString(MdekKeys.CONFORMITY_DEGREE_VALUE));
		ref.setLine(line);
		keyValueService.processKeyValue(ref);

		return ref;
	}
	private List<IngridDocument> createObjectConformityList(int specificationKey, int degreeKey) {
		List<IngridDocument> cDocList = new ArrayList<IngridDocument>();
		IngridDocument cDoc = new IngridDocument();
		cDoc.put(MdekKeys.CONFORMITY_SPECIFICATION_KEY, specificationKey);
		cDoc.put(MdekKeys.CONFORMITY_DEGREE_KEY, degreeKey);
		cDocList.add(cDoc);
		
		return cDocList;
	}	
	private void updateObjectConformitys(IngridDocument oDocIn, T01Object oIn) {
		List<IngridDocument> refDocs = (List) oDocIn.get(MdekKeys.CONFORMITY_LIST);
		// NOTICE: objects conformities are only editable in special object types (classes) and have default
		// values in other types. We guarantee default values, when necessary ! object-classes can be switched
		// so conformity might be wrong, remaining from former class !
		ObjectType oType = EnumUtil.mapDatabaseToEnumConst(ObjectType.class, oIn.getObjClass());
		if (oType == ObjectType.GEO_INFORMATION || 
				oType == ObjectType.GEO_DIENST ||
				oType == ObjectType.INFOSYSTEM_DIENST) {
			// set default if not set, else keep set values. DISPLAYED in frontend.
			if (refDocs == null) {
				refDocs = createObjectConformityList(MdekUtils.OBJ_CONFORMITY_SPECIFICATION_INSPIRE_KEY, MdekUtils.OBJ_CONFORMITY_NOT_EVALUATED);
			}			
		} else {
			// check whether correct default is set ! if not, set it. NOT displayed in frontend !
			if (refDocs == null ||
					refDocs.size() != 1 ||
					!MdekUtils.OBJ_CONFORMITY_SPECIFICATION_INSPIRE_KEY.equals(refDocs.get(0).get(MdekKeys.CONFORMITY_SPECIFICATION_KEY)) ||
					!MdekUtils.OBJ_CONFORMITY_NOT_EVALUATED.equals(refDocs.get(0).get(MdekKeys.CONFORMITY_DEGREE_KEY))) {
				refDocs = createObjectConformityList(MdekUtils.OBJ_CONFORMITY_SPECIFICATION_INSPIRE_KEY, MdekUtils.OBJ_CONFORMITY_NOT_EVALUATED);				
			}
		}

		Set<ObjectConformity> refs = oIn.getObjectConformitys();
		ArrayList<ObjectConformity> refs_unprocessed = new ArrayList<ObjectConformity>(refs);
		// remove all !
		for (ObjectConformity ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			dao.makeTransient(ref);			
		}		
		// and add all new ones !
		int line = 1;
		for (IngridDocument refDoc : refDocs) {
			// add all as new ones
			ObjectConformity ref = mapObjectConformity(oIn, refDoc, new ObjectConformity(), line);
			refs.add(ref);
			line++;
		}
	}

	private ObjectAccess mapObjectAccess(T01Object oFrom,
			IngridDocument refDoc,
			ObjectAccess ref, 
			int line)
	{
		ref.setObjId(oFrom.getId());
		ref.setRestrictionKey((Integer)refDoc.get(MdekKeys.ACCESS_RESTRICTION_KEY));
		ref.setRestrictionValue(refDoc.getString(MdekKeys.ACCESS_RESTRICTION_VALUE));
		ref.setLine(line);
		keyValueService.processKeyValue(ref);

		return ref;
	}
	private void updateObjectAccesses(IngridDocument oDocIn, T01Object oIn) {
		List<IngridDocument> refDocs = (List) oDocIn.get(MdekKeys.ACCESS_LIST);
		if (refDocs == null) {
			refDocs = new ArrayList<IngridDocument>(0);
		}
		Set<ObjectAccess> refs = oIn.getObjectAccesss();
		ArrayList<ObjectAccess> refs_unprocessed = new ArrayList<ObjectAccess>(refs);
		// remove all !
		for (ObjectAccess ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			dao.makeTransient(ref);			
		}		
		// and add all new ones !
		int line = 1;
		for (IngridDocument refDoc : refDocs) {
			// add all as new ones
			ObjectAccess ref = mapObjectAccess(oIn, refDoc, new ObjectAccess(), line);
			refs.add(ref);
			line++;
		}
	}

	private ObjectUse mapObjectUse(T01Object oFrom,
			IngridDocument refDoc,
			ObjectUse ref, 
			int line)
	{
		ref.setObjId(oFrom.getId());
		ref.setTermsOfUseKey((Integer)refDoc.get(MdekKeys.USE_TERMS_OF_USE_KEY));
		ref.setTermsOfUseValue(refDoc.getString(MdekKeys.USE_TERMS_OF_USE_VALUE));
		ref.setLine(line);
		keyValueService.processKeyValue(ref);

		return ref;
	}
	private void updateObjectUses(IngridDocument oDocIn, T01Object oIn) {
		List<IngridDocument> refDocs = (List) oDocIn.get(MdekKeys.USE_LIST);
		if (refDocs == null) {
			refDocs = new ArrayList<IngridDocument>(0);
		}
		Set<ObjectUse> refs = oIn.getObjectUses();
		ArrayList<ObjectUse> refs_unprocessed = new ArrayList<ObjectUse>(refs);
		// remove all !
		for (ObjectUse ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			dao.makeTransient(ref);			
		}		
		// and add all new ones !
		int line = 1;
		for (IngridDocument refDoc : refDocs) {
			// add all as new ones
			ObjectUse ref = mapObjectUse(oIn, refDoc, new ObjectUse(), line);
			refs.add(ref);
			line++;
		}
	}

	private ObjectDataQuality mapObjectDataQuality(T01Object oFrom,
			IngridDocument refDoc,
			ObjectDataQuality ref,
			int line)
	{
		ref.setObjId(oFrom.getId());
		ref.setDqElementId((Integer)refDoc.get(MdekKeys.DQ_ELEMENT_ID));
		ref.setNameOfMeasureKey((Integer)refDoc.get(MdekKeys.NAME_OF_MEASURE_KEY));
		ref.setNameOfMeasureValue(refDoc.getString(MdekKeys.NAME_OF_MEASURE_VALUE));
		ref.setResultValue(refDoc.getString(MdekKeys.RESULT_VALUE));
		ref.setMeasureDescription(refDoc.getString(MdekKeys.MEASURE_DESCRIPTION));
		ref.setLine(line);

		keyValueService.processKeyValue(ref);

		return ref;
	}
	private void updateObjectDataQualitys(IngridDocument oDocIn, T01Object oIn) {
		List<IngridDocument> refDocs = (List) oDocIn.get(MdekKeys.DATA_QUALITY_LIST);
		if (refDocs == null) {
			refDocs = new ArrayList<IngridDocument>(0);
		}
		Set<ObjectDataQuality> refs = oIn.getObjectDataQualitys();
		ArrayList<ObjectDataQuality> refs_unprocessed = new ArrayList<ObjectDataQuality>(refs);
		// remove all !
		for (ObjectDataQuality ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			dao.makeTransient(ref);			
		}		
		// and add all new ones !
		int line = 1;
		for (IngridDocument refDoc : refDocs) {
			// add all as new ones
			ObjectDataQuality ref = mapObjectDataQuality(oIn, refDoc, new ObjectDataQuality(), line);
			refs.add(ref);
			line++;
		}
	}

	private ObjectFormatInspire mapObjectFormatInspire(T01Object oFrom,
			IngridDocument refDoc,
			ObjectFormatInspire ref, 
			int line)
	{
		ref.setObjId(oFrom.getId());
		ref.setFormatKey((Integer)refDoc.get(MdekKeys.FORMAT_KEY));
		ref.setFormatValue(refDoc.getString(MdekKeys.FORMAT_VALUE));
		ref.setLine(line);
		keyValueService.processKeyValue(ref);

		return ref;
	}
	private void updateObjectFormatInspires(IngridDocument oDocIn, T01Object oIn) {
		List<IngridDocument> refDocs = (List) oDocIn.get(MdekKeys.FORMAT_INSPIRE_LIST);
		if (refDocs == null) {
			refDocs = new ArrayList<IngridDocument>(0);
		}
		Set<ObjectFormatInspire> refs = oIn.getObjectFormatInspires();
		ArrayList<ObjectFormatInspire> refs_unprocessed = new ArrayList<ObjectFormatInspire>(refs);
		// remove all !
		for (ObjectFormatInspire ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			dao.makeTransient(ref);			
		}		
		// and add all new ones !
		int line = 1;
		for (IngridDocument refDoc : refDocs) {
			// add all as new ones
			ObjectFormatInspire ref = mapObjectFormatInspire(oIn, refDoc, new ObjectFormatInspire(), line);
			refs.add(ref);
			line++;
		}
	}

	private SpatialSystem mapSpatialSystem(T01Object oFrom,
			IngridDocument refDoc,
			SpatialSystem ref, 
			int line)
	{
		ref.setObjId(oFrom.getId());
		ref.setReferencesystemKey((Integer)refDoc.get(MdekKeys.REFERENCESYSTEM_ID));
		ref.setReferencesystemValue(refDoc.getString(MdekKeys.COORDINATE_SYSTEM));
		ref.setLine(line);
		keyValueService.processKeyValue(ref);

		return ref;
	}
	private void updateSpatialSystems(IngridDocument oDocIn, T01Object oIn) {
		List<IngridDocument> refDocs = (List) oDocIn.get(MdekKeys.SPATIAL_SYSTEM_LIST);
		if (refDocs == null) {
			refDocs = new ArrayList<IngridDocument>(0);
		}
		Set<SpatialSystem> refs = oIn.getSpatialSystems();
		ArrayList<SpatialSystem> refs_unprocessed = new ArrayList<SpatialSystem>(refs);
		// remove all !
		for (SpatialSystem ref : refs_unprocessed) {
			refs.remove(ref);
			// delete-orphan doesn't work !!!?????
			dao.makeTransient(ref);			
		}		
		// and add all new ones !
		int line = 1;
		for (IngridDocument refDoc : refDocs) {
			// add all as new ones
			SpatialSystem ref = mapSpatialSystem(oIn, refDoc, new SpatialSystem(), line);
			refs.add(ref);
			line++;
		}
	}

	private void updateObjectMetadata(IngridDocument oDocIn, T01Object oIn) {
		ObjectMetadata ref = oIn.getObjectMetadata();
		if (ref == null) {
			// initialize new Metadata (NOT DONE BY HIBERBNATE WITH DEFAULT VALUES FROM DATABASE WHEN PERSISTED :( )
			ref = new ObjectMetadata();
			ref.setMarkDeleted(MdekUtils.NO);
			ref.setExpiryState(MdekUtils.ExpiryState.INITIAL.getDbValue());
			// save the object to get ID from database (not handled by hibernate ?)
			dao.makePersistent(ref);			
			oIn.setObjMetadataId(ref.getId());
			oIn.setObjectMetadata(ref);
		}

		// update only if set (so object keeps former values) !
		if (oDocIn.containsKey(MdekKeys.LASTEXPORT_TIME)) {
			ref.setLastexportTime(oDocIn.getString(MdekKeys.LASTEXPORT_TIME));			
		}
		if (oDocIn.containsKey(MdekKeys.EXPIRY_STATE)) {
			ref.setExpiryState((Integer) oDocIn.get(MdekKeys.EXPIRY_STATE));
		}
		if (oDocIn.containsKey(MdekKeys.MARK_DELETED)) {
			ref.setMarkDeleted(oDocIn.getString(MdekKeys.MARK_DELETED));
		}
		if (oDocIn.containsKey(MdekKeys.ASSIGNER_UUID)) {
			ref.setAssignerUuid(oDocIn.getString(MdekKeys.ASSIGNER_UUID));
		}
		if (oDocIn.containsKey(MdekKeys.ASSIGN_TIME)) {
			ref.setAssignTime(oDocIn.getString(MdekKeys.ASSIGN_TIME));
		}
		if (oDocIn.containsKey(MdekKeys.REASSIGNER_UUID)) {
			ref.setReassignerUuid(oDocIn.getString(MdekKeys.REASSIGNER_UUID));
		}
		if (oDocIn.containsKey(MdekKeys.REASSIGN_TIME)) {
			ref.setReassignTime(oDocIn.getString(MdekKeys.REASSIGN_TIME));
		}
	}
}