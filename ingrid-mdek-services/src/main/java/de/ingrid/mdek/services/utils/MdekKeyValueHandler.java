/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2017 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.mdek.services.utils;

import java.util.Map;

import org.apache.log4j.Logger;

import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekUtils.MdekSysList;
import de.ingrid.mdek.MdekUtils.ObjectType;
import de.ingrid.mdek.MdekUtils.SearchtermType;
import de.ingrid.mdek.services.catalog.MdekCatalogService;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.IEntity;
import de.ingrid.mdek.services.persistence.db.model.AdditionalFieldData;
import de.ingrid.mdek.services.persistence.db.model.ObjectAccess;
import de.ingrid.mdek.services.persistence.db.model.ObjectConformity;
import de.ingrid.mdek.services.persistence.db.model.ObjectDataQuality;
import de.ingrid.mdek.services.persistence.db.model.ObjectFormatInspire;
import de.ingrid.mdek.services.persistence.db.model.ObjectOpenDataCategory;
import de.ingrid.mdek.services.persistence.db.model.ObjectReference;
import de.ingrid.mdek.services.persistence.db.model.ObjectTypesCatalogue;
import de.ingrid.mdek.services.persistence.db.model.ObjectUseConstraint;
import de.ingrid.mdek.services.persistence.db.model.SearchtermValue;
import de.ingrid.mdek.services.persistence.db.model.SpatialRefValue;
import de.ingrid.mdek.services.persistence.db.model.SpatialSystem;
import de.ingrid.mdek.services.persistence.db.model.T0110AvailFormat;
import de.ingrid.mdek.services.persistence.db.model.T011ObjGeoSymc;
import de.ingrid.mdek.services.persistence.db.model.T011ObjLiterature;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServ;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServOpPlatform;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServOperation;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServType;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServVersion;
import de.ingrid.mdek.services.persistence.db.model.T012ObjAdr;
import de.ingrid.mdek.services.persistence.db.model.T014InfoImpart;
import de.ingrid.mdek.services.persistence.db.model.T015Legist;
import de.ingrid.mdek.services.persistence.db.model.T017UrlRef;
import de.ingrid.mdek.services.persistence.db.model.T01Object;
import de.ingrid.mdek.services.persistence.db.model.T021Communication;
import de.ingrid.mdek.services.persistence.db.model.T02Address;
import de.ingrid.mdek.services.persistence.db.model.T03Catalogue;


/**
 * Encapsulates validation and mapping of key/value pairs in beans. -> syslists !
 */
public class MdekKeyValueHandler {

	private static final Logger LOG = Logger.getLogger(MdekKeyValueHandler.class);

	private static MdekKeyValueHandler myInstance;

	private MdekCatalogService catalogService;

	// DO NOT FORGET TO KEEP THIS ONE UP TO DATE !!!
	// !!! DO NOT FORGET TO ASSURE ACCORDING DAO CAN BE FETCHED VIA DaoFactory.getDao(Class) !!!!
	private static Class[] keyValueClasses = new Class[] {
		T011ObjServ.class,
		T011ObjServOperation.class,
		T011ObjServVersion.class,
		T011ObjGeoSymc.class,
		T017UrlRef.class,
		T015Legist.class,
		T014InfoImpart.class,
		T0110AvailFormat.class,
		T011ObjLiterature.class,
		T021Communication.class,
		SpatialRefValue.class,
		T02Address.class,
		ObjectReference.class,
		T012ObjAdr.class,
		ObjectConformity.class,
		ObjectAccess.class,
		T011ObjServType.class,
		SearchtermValue.class,
		T03Catalogue.class,
		T01Object.class,
		ObjectDataQuality.class,
		ObjectFormatInspire.class,
		AdditionalFieldData.class,
		SpatialSystem.class,
		ObjectTypesCatalogue.class,
		T011ObjServOpPlatform.class,
		ObjectOpenDataCategory.class,
		ObjectUseConstraint.class,
	};

	/** Get The Singleton */
	public static synchronized MdekKeyValueHandler getInstance(DaoFactory daoFactory) {
		if (myInstance == null) {
	        myInstance = new MdekKeyValueHandler(daoFactory);
		}
		return myInstance;
	}

	private MdekKeyValueHandler(DaoFactory daoFactory) {
		catalogService = MdekCatalogService.getInstance(daoFactory);
	}

	
	/** Get all entity classes containing key/value pairs */
	public Class[] getEntityClassesContainingKeyValue() {
		return keyValueClasses;
	}

	/** evaluate keys and set correct syslist values in bean according to bean type. */
	public IEntity processKeyValue(IEntity bean) {
		Class clazz = bean.getClass();
		
		// NOTICE: bean may be proxy class generated by hibernate (=subclass of orig class)
		
		// NOTICE: Also adapt MdekCatalogService.rebuildEntitiesSyslistData() to call special methods here !
		if (T011ObjServ.class.isAssignableFrom(clazz)) {
			throw new IllegalArgumentException("Unsupported class: " + clazz.getName() +
				" -> Process with separate method 'processKeyValueT011ObjServ(...)' !!!");
		} else if (T011ObjServOperation.class.isAssignableFrom(clazz)) {
			throw new IllegalArgumentException("Unsupported class: " + clazz.getName() +
				" -> Process with separate method 'processKeyValueT011ObjServOperation(...)' !!!");
        } else if (T011ObjServVersion.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException("Unsupported class: " + clazz.getName() +
                " -> Process with separate method 'processKeyValueT011ObjServVersion(...)' !!!");
		} else if (T011ObjGeoSymc.class.isAssignableFrom(clazz)) {
			processKeyValueT011ObjGeoSymc((T011ObjGeoSymc) bean);
		} else if (T017UrlRef.class.isAssignableFrom(clazz)) {
			processKeyValueT017UrlRef((T017UrlRef) bean);
		} else if (T015Legist.class.isAssignableFrom(clazz)) {
			processKeyValueT015Legist((T015Legist) bean);
		} else if (T014InfoImpart.class.isAssignableFrom(clazz)) {
			processKeyValueT014InfoImpart((T014InfoImpart) bean);
		} else if (T0110AvailFormat.class.isAssignableFrom(clazz)) {
			processKeyValueT0110AvailFormat((T0110AvailFormat) bean);
		} else if (T011ObjLiterature.class.isAssignableFrom(clazz)) {
			processKeyValueT011ObjLiterature((T011ObjLiterature) bean);
		} else if (T021Communication.class.isAssignableFrom(clazz)) {
			processKeyValueT021Communication((T021Communication) bean);
		} else if (SpatialRefValue.class.isAssignableFrom(clazz)) {
			processKeyValueSpatialRefValue((SpatialRefValue) bean);
		} else if (T02Address.class.isAssignableFrom(clazz)) {
			processKeyValueT02Address((T02Address) bean);
		} else if (ObjectReference.class.isAssignableFrom(clazz)) {
			processKeyValueObjectReference((ObjectReference) bean);
		} else if (T012ObjAdr.class.isAssignableFrom(clazz)) {
			processKeyValueT012ObjAdr((T012ObjAdr) bean);
		} else if (ObjectConformity.class.isAssignableFrom(clazz)) {
			processKeyValueObjectConformity((ObjectConformity) bean);
		} else if (ObjectAccess.class.isAssignableFrom(clazz)) {
			processKeyValueObjectAccess((ObjectAccess) bean);
		} else if (T011ObjServType.class.isAssignableFrom(clazz)) {
			processKeyValueT011ObjServType((T011ObjServType) bean);
		} else if (SearchtermValue.class.isAssignableFrom(clazz)) {
			processKeyValueSearchtermValue((SearchtermValue) bean);
		} else if (T03Catalogue.class.isAssignableFrom(clazz)) {
			processKeyValueT03Catalogue((T03Catalogue) bean);
		} else if (T01Object.class.isAssignableFrom(clazz)) {
			processKeyValueT01Object((T01Object) bean);
		} else if (ObjectDataQuality.class.isAssignableFrom(clazz)) {
			processKeyValueObjectDataQuality((ObjectDataQuality) bean);
		} else if (ObjectFormatInspire.class.isAssignableFrom(clazz)) {
			processKeyValueObjectFormatInspire((ObjectFormatInspire) bean);
		} else if (AdditionalFieldData.class.isAssignableFrom(clazz)) {
			processKeyValueAdditionalFieldData((AdditionalFieldData) bean);
		} else if (SpatialSystem.class.isAssignableFrom(clazz)) {
			processKeyValueSpatialSystem((SpatialSystem) bean);
		} else if (ObjectTypesCatalogue.class.isAssignableFrom(clazz)) {
			processKeyValueObjectTypesCatalogue((ObjectTypesCatalogue) bean);
		} else if (T011ObjServOpPlatform.class.isAssignableFrom(clazz)) {
			processKeyValueT011ObjServOpPlatform((T011ObjServOpPlatform) bean);
		} else if (ObjectOpenDataCategory.class.isAssignableFrom(clazz)) {
			processKeyValueObjectOpenDataCategory((ObjectOpenDataCategory) bean);
        } else if (ObjectUseConstraint.class.isAssignableFrom(clazz)) {
            processKeyValueObjectUseConstraint((ObjectUseConstraint) bean);
		// NOTICE: ALSO ADD NEW CLASSES TO ARRAY keyValueClasses ABOVE !!!!
		// !!! DO NOT FORGET TO ASSURE ACCORDING DAO CAN BE FETCHED VIA DaoFactory.getDao(Class) !!!!

		} else {
			throw new IllegalArgumentException("Unsupported class: " + clazz.getName());
		}

		return bean;
	}
	
	/** Set correct syslist values in servOp according to serv Type (determines syslist) and entry key in servOp. */
	public IEntity processKeyValueT011ObjServOperation(T011ObjServOperation servOp, T011ObjServ serv) {
		Integer servOpKey = servOp.getNameKey();
		if (servOpKey != null && servOpKey > -1) {
			Integer servTypeKey = serv.getTypeKey();
			if (servTypeKey != null) {
				Map<Integer, String> keyNameMap = null;
				if (servTypeKey.equals(MdekUtils.OBJ_SERV_TYPE_WMS)) {
					keyNameMap = catalogService.getSysListKeyNameMap(
						MdekSysList.OBJ_SERV_OPERATION_WMS.getDbValue(),
						catalogService.getCatalogLanguage());
				} else if (servTypeKey.equals(MdekUtils.OBJ_SERV_TYPE_WFS)) {
					keyNameMap = catalogService.getSysListKeyNameMap(
							MdekSysList.OBJ_SERV_OPERATION_WFS.getDbValue(),
							catalogService.getCatalogLanguage());
				} else if (servTypeKey.equals(MdekUtils.OBJ_SERV_TYPE_CSW)) {
					keyNameMap = catalogService.getSysListKeyNameMap(
							MdekSysList.OBJ_SERV_OPERATION_CSW.getDbValue(),
							catalogService.getCatalogLanguage());
				} else if (servTypeKey.equals(MdekUtils.OBJ_SERV_TYPE_WCTS)) {
					keyNameMap = catalogService.getSysListKeyNameMap(
							MdekSysList.OBJ_SERV_OPERATION_WCTS.getDbValue(),
							catalogService.getCatalogLanguage());
				}

				if (keyNameMap != null) {
					servOp.setNameValue(keyNameMap.get(servOpKey));
				}
			}
		}

		return servOp;
	}

    /** Set correct syslist values in servVersion according to serv Type (determines syslist) and entry key in servVersion. */
    public IEntity processKeyValueT011ObjServVersion(T011ObjServVersion servVersion, T011ObjServ serv) {
        Integer servVersionKey = servVersion.getVersionKey();
        if (servVersionKey != null && servVersionKey > -1) {
            Integer servTypeKey = serv.getTypeKey();
            if (servTypeKey != null) {
                Map<Integer, String> keyNameMap = null;
                if (servTypeKey.equals(MdekUtils.OBJ_SERV_TYPE_WMS)) {
                    keyNameMap = catalogService.getSysListKeyNameMap(
                        MdekSysList.OBJ_SERV_VERSION_WMS.getDbValue(),
                        catalogService.getCatalogLanguage());
                } else if (servTypeKey.equals(MdekUtils.OBJ_SERV_TYPE_WFS)) {
                    keyNameMap = catalogService.getSysListKeyNameMap(
                            MdekSysList.OBJ_SERV_VERSION_WFS.getDbValue(),
                            catalogService.getCatalogLanguage());
                } else if (servTypeKey.equals(MdekUtils.OBJ_SERV_TYPE_CSW)) {
                    keyNameMap = catalogService.getSysListKeyNameMap(
                            MdekSysList.OBJ_SERV_VERSION_CSW.getDbValue(),
                            catalogService.getCatalogLanguage());
                } else if (servTypeKey.equals(MdekUtils.OBJ_SERV_TYPE_WCTS)) {
                    keyNameMap = catalogService.getSysListKeyNameMap(
                            MdekSysList.OBJ_SERV_VERSION_WCTS.getDbValue(),
                            catalogService.getCatalogLanguage());
                }

                if (keyNameMap != null) {
                    servVersion.setVersionValue(keyNameMap.get(servVersionKey));
                }
            }
        }

        return servVersion;
    }

	/** Set correct syslist values in objServ according to object class (determines syslist) and entry key in objServ. */
	public IEntity processKeyValueT011ObjServ(T011ObjServ objServ, T01Object obj) {
		Integer entryKey = objServ.getTypeKey();
		if (entryKey != null && entryKey > -1) {
			// ServType syslist is dependent from class of object !
			// default is class 3 = "Geodatendienst"
			Integer syslistId = MdekSysList.OBJ_SERV_TYPE.getDbValue();		
			// change syslist if class 6 = "Informationssystem/Dienst/Anwendung"
			if (ObjectType.INFOSYSTEM_DIENST.getDbValue().equals(obj.getObjClass())) {
				syslistId = MdekSysList.OBJ_SERV_TYPE_CLASS_6.getDbValue();
			}

			Map<Integer, String> keyNameMap = catalogService.getSysListKeyNameMap(
				syslistId,
				catalogService.getCatalogLanguage());

			objServ.setTypeValue(keyNameMap.get(entryKey));
		}
		
		return objServ;
	}

	private IEntity processKeyValueT011ObjGeoSymc(T011ObjGeoSymc bean) {
		Integer entryKey = bean.getSymbolCatKey();
		if (entryKey != null && entryKey > -1) {
			Map<Integer, String> keyNameMap = catalogService.getSysListKeyNameMap(
				MdekSysList.OBJ_GEO_SYMC.getDbValue(),
				catalogService.getCatalogLanguage());

			if (keyNameMap.get(entryKey) != null) {
				// entry found in syslist, set name !
				bean.setSymbolCatValue(keyNameMap.get(entryKey));
			} else {
				// entry NOT found in syslist ! transform to free entry cause may be changed in IGE outside codelist repo !
				// see INGRID33-29
				logTransformToFreeEntry(MdekSysList.OBJ_GEO_SYMC, entryKey, bean.getSymbolCatValue());
				bean.setSymbolCatKey(-1);
			}
		}
		
		return bean;
	}

	private IEntity processKeyValueT017UrlRef(T017UrlRef bean) {
		Integer entryKey = bean.getSpecialRef();
		if (entryKey != null && entryKey > -1) {
			Map<Integer, String> keyNameMap = catalogService.getSysListKeyNameMap(
				MdekSysList.URL_REF_SPECIAL.getDbValue(),
				catalogService.getCatalogLanguage());

			bean.setSpecialName(keyNameMap.get(entryKey));
		}
		
		entryKey = bean.getDatatypeKey();
		if (entryKey != null && entryKey > -1) {
			Map<Integer, String> keyNameMap = catalogService.getSysListKeyNameMap(
				MdekSysList.URL_REF_DATATYPE.getDbValue(),
				catalogService.getCatalogLanguage());

			bean.setDatatypeValue(keyNameMap.get(entryKey));
		}
		
		return bean;
	}

	private IEntity processKeyValueT015Legist(T015Legist bean) {
		Integer entryKey = bean.getLegistKey();
		if (entryKey != null && entryKey > -1) {
			Map<Integer, String> keyNameMap = catalogService.getSysListKeyNameMap(
				MdekSysList.LEGIST.getDbValue(),
				catalogService.getCatalogLanguage());

			if (keyNameMap.get(entryKey) != null) {
				// entry found in syslist, set name !
				bean.setLegistValue(keyNameMap.get(entryKey));
			} else {
				// entry NOT found in syslist ! transform to free entry cause may be changed in IGE outside codelist repo !
				// see INGRID33-29
				logTransformToFreeEntry(MdekSysList.LEGIST, entryKey, bean.getLegistValue());
				bean.setLegistKey(-1);
			}
		}
		
		return bean;
	}

	private IEntity processKeyValueT014InfoImpart(T014InfoImpart bean) {
		Integer entryKey = bean.getImpartKey();
		if (entryKey != null && entryKey > -1) {
			Map<Integer, String> keyNameMap = catalogService.getSysListKeyNameMap(
				MdekSysList.INFO_IMPART.getDbValue(),
				catalogService.getCatalogLanguage());

			if (keyNameMap.get(entryKey) != null) {
				// entry found in syslist, set name !
				bean.setImpartValue(keyNameMap.get(entryKey));
			} else {
				// entry NOT found in syslist ! transform to free entry cause may be changed in IGE outside codelist repo !
				// see INGRID33-29
				logTransformToFreeEntry(MdekSysList.INFO_IMPART, entryKey, bean.getImpartValue());
				bean.setImpartKey(-1);
			}
		}
		
		return bean;
	}

	private IEntity processKeyValueT0110AvailFormat(T0110AvailFormat bean) {
		Integer entryKey = bean.getFormatKey();
		if (entryKey != null && entryKey > -1) {
			Map<Integer, String> keyNameMap = catalogService.getSysListKeyNameMap(
				MdekSysList.AVAIL_FORMAT.getDbValue(),
				catalogService.getCatalogLanguage());

			bean.setFormatValue(keyNameMap.get(entryKey));
		}
		
		return bean;
	}

	private IEntity processKeyValueT011ObjLiterature(T011ObjLiterature bean) {
		Integer entryKey = bean.getTypeKey();
		if (entryKey != null && entryKey > -1) {
			Map<Integer, String> keyNameMap = catalogService.getSysListKeyNameMap(
				MdekSysList.OBJ_LITERATURE_TYPE.getDbValue(),
				catalogService.getCatalogLanguage());

			bean.setTypeValue(keyNameMap.get(entryKey));
		}
		
		return bean;
	}

	private IEntity processKeyValueT021Communication(T021Communication bean) {
		Integer entryKey = bean.getCommtypeKey();
		if (entryKey != null && entryKey > -1) {
			Map<Integer, String> keyNameMap = catalogService.getSysListKeyNameMap(
				MdekSysList.COMM_TYPE.getDbValue(),
				catalogService.getCatalogLanguage());

			bean.setCommtypeValue(keyNameMap.get(entryKey));
		}
		
		return bean;
	}

	private IEntity processKeyValueSpatialRefValue(SpatialRefValue bean) {
		Integer entryKey = bean.getNameKey();
		if (entryKey != null && entryKey > -1) {
			Map<Integer, String> keyNameMap = catalogService.getSysListKeyNameMap(
				MdekSysList.SPATIAL_REF_VALUE.getDbValue(),
				catalogService.getCatalogLanguage());

			if (keyNameMap.get(entryKey) != null) {
				// entry found in syslist, set name !
				bean.setNameValue(keyNameMap.get(entryKey));
			} else {
				// entry NOT found in syslist ! transform to free entry cause may be changed in IGE outside codelist repo !
				// see INGRID33-29
				logTransformToFreeEntry(MdekSysList.SPATIAL_REF_VALUE, entryKey, bean.getNameValue());
				bean.setNameKey(-1);
			}
		}
		
		return bean;
	}

	private IEntity processKeyValueT02Address(T02Address bean) {
		Integer entryKey = bean.getAddressKey();
		if (entryKey != null && entryKey > -1) {
			Map<Integer, String> keyNameMap = catalogService.getSysListKeyNameMap(
				MdekSysList.ADDRESS_VALUE.getDbValue(),
				catalogService.getCatalogLanguage());
			bean.setAddressValue(keyNameMap.get(entryKey));
		}
		
		entryKey = bean.getTitleKey();
		if (entryKey != null && entryKey > -1) {
			Map<Integer, String> keyNameMap = catalogService.getSysListKeyNameMap(
				MdekSysList.ADDRESS_TITLE.getDbValue(),
				catalogService.getCatalogLanguage());
			bean.setTitleValue(keyNameMap.get(entryKey));
		}
		
		entryKey = bean.getCountryKey();
		if (entryKey != null && entryKey > -1) {
			Map<Integer, String> keyNameMap = catalogService.getSysListKeyNameMap(
				MdekSysList.COUNTRY.getDbValue(),
				catalogService.getCatalogLanguage());
			bean.setCountryValue(keyNameMap.get(entryKey));
		}
		
		return bean;
	}

	private IEntity processKeyValueObjectReference(ObjectReference bean) {
		Integer entryKey = bean.getSpecialRef();
		if (entryKey != null && entryKey > -1) {
			Map<Integer, String> keyNameMap = catalogService.getSysListKeyNameMap(
				MdekSysList.OBJ_REFERENCE.getDbValue(),
				catalogService.getCatalogLanguage());

			bean.setSpecialName(keyNameMap.get(entryKey));
		}
		
		return bean;
	}

	private IEntity processKeyValueT012ObjAdr(T012ObjAdr bean) {
		Integer sysListKey = bean.getSpecialRef();
		Integer entryKey = bean.getType();

		if (sysListKey != null && sysListKey > -1 &&
				entryKey != null && entryKey > -1)
		{
			Map<Integer, String> keyNameMap = null;
			if (sysListKey.equals(MdekSysList.OBJ_ADR_TYPE.getDbValue())) {
				keyNameMap = catalogService.getSysListKeyNameMap(
						MdekSysList.OBJ_ADR_TYPE.getDbValue(),
						catalogService.getCatalogLanguage());
				
			} else if (sysListKey.equals(MdekSysList.OBJ_ADR_TYPE_SPECIAL.getDbValue())) {
				keyNameMap = catalogService.getSysListKeyNameMap(
						MdekSysList.OBJ_ADR_TYPE_SPECIAL.getDbValue(),
						catalogService.getCatalogLanguage());				
			}

			if (keyNameMap != null) {
				bean.setSpecialName(keyNameMap.get(entryKey));
			}
		}
		
		return bean;
	}

	private IEntity processKeyValueObjectConformity(ObjectConformity bean) {
		Integer entryKey = bean.getDegreeKey();
		if (entryKey != null && entryKey > -1) {
			Map<Integer, String> keyNameMap = catalogService.getSysListKeyNameMap(
				MdekSysList.OBJ_CONFORMITY_DEGREE.getDbValue(),
				catalogService.getCatalogLanguage());

			bean.setDegreeValue(keyNameMap.get(entryKey));
		}
		
		entryKey = bean.getSpecificationKey();
		if (entryKey != null && entryKey > -1) {
			Map<Integer, String> keyNameMap = catalogService.getSysListKeyNameMap(
				MdekSysList.OBJ_CONFORMITY_SPECIFICATION.getDbValue(),
				catalogService.getCatalogLanguage());

			bean.setSpecificationValue(keyNameMap.get(entryKey));
		}
		
		return bean;
	}

	private IEntity processKeyValueObjectAccess(ObjectAccess bean) {
		Integer entryKey = bean.getRestrictionKey();
		if (entryKey != null && entryKey > -1) {
			Map<Integer, String> keyNameMap = catalogService.getSysListKeyNameMap(
				MdekSysList.OBJ_ACCESS.getDbValue(),
				catalogService.getCatalogLanguage());

			if (keyNameMap.get(entryKey) != null) {
				// entry found in syslist, set name !
				bean.setRestrictionValue(keyNameMap.get(entryKey));
			} else {
				// entry NOT found in syslist ! transform to free entry cause may be changed in IGE outside codelist repo !
				// see INGRID33-29
				logTransformToFreeEntry(MdekSysList.OBJ_ACCESS, entryKey, bean.getRestrictionValue());
				bean.setRestrictionKey(-1);
			}
		}

		return bean;
	}

	private IEntity processKeyValueT011ObjServType(T011ObjServType bean) {
		Integer entryKey = bean.getServTypeKey();
		if (entryKey != null && entryKey > -1) {
			Map<Integer, String> keyNameMap = catalogService.getSysListKeyNameMap(
				MdekSysList.OBJ_SERV_TYPE2.getDbValue(),
				catalogService.getCatalogLanguage());

			bean.setServTypeValue(keyNameMap.get(entryKey));
		}

		return bean;
	}

	private IEntity processKeyValueSearchtermValue(SearchtermValue bean) {
		Integer entryKey = bean.getEntryId();
		if (entryKey != null && entryKey > -1) {
			if (SearchtermType.INSPIRE.getDbValue().equals(bean.getType())) {
				Map<Integer, String> keyNameMap = catalogService.getSysListKeyNameMap(
						MdekSysList.INSPIRE_SEARCHTERM.getDbValue(),
						catalogService.getCatalogLanguage());
				bean.setTerm(keyNameMap.get(entryKey));
			}
		}

		return bean;
	}

	private IEntity processKeyValueT03Catalogue(T03Catalogue bean) {
		Integer entryKey = bean.getCountryKey();
		if (entryKey != null && entryKey > -1) {
			Map<Integer, String> keyNameMap = catalogService.getSysListKeyNameMap(
				MdekSysList.COUNTRY.getDbValue(),
				catalogService.getCatalogLanguage());
			bean.setCountryValue(keyNameMap.get(entryKey));
		}
		
		entryKey = bean.getLanguageKey();
		if (entryKey != null && entryKey > -1) {
			Map<Integer, String> keyNameMap = catalogService.getSysListKeyNameMap(
				MdekSysList.LANGUAGE.getDbValue(),
				catalogService.getCatalogLanguage());
			bean.setLanguageValue(keyNameMap.get(entryKey));
		}
		
		return bean;
	}

	private IEntity processKeyValueT01Object(T01Object bean) {
		Integer entryKey = bean.getDataLanguageKey();
		if (entryKey != null && entryKey > -1) {
			Map<Integer, String> keyNameMap = catalogService.getSysListKeyNameMap(
				MdekSysList.LANGUAGE.getDbValue(),
				catalogService.getCatalogLanguage());
			bean.setDataLanguageValue(keyNameMap.get(entryKey));
		}
		
		entryKey = bean.getMetadataLanguageKey();
		if (entryKey != null && entryKey > -1) {
			Map<Integer, String> keyNameMap = catalogService.getSysListKeyNameMap(
				MdekSysList.LANGUAGE.getDbValue(),
				catalogService.getCatalogLanguage());
			bean.setMetadataLanguageValue(keyNameMap.get(entryKey));
		}
		
		entryKey = bean.getVerticalExtentVdatumKey();
		if (entryKey != null && entryKey > -1) {
			Map<Integer, String> keyNameMap = catalogService.getSysListKeyNameMap(
				MdekSysList.VERTICAL_EXTENT_VDATUM.getDbValue(),
				catalogService.getCatalogLanguage());
			bean.setVerticalExtentVdatumValue(keyNameMap.get(entryKey));
		}
		
		return bean;
	}

	private IEntity processKeyValueObjectDataQuality(ObjectDataQuality bean) {
		Integer dqElemId = bean.getDqElementId();
		Integer entryKey = bean.getNameOfMeasureKey();

		if (dqElemId != null && dqElemId > -1 &&
				entryKey != null && entryKey > -1)
		{
			Map<Integer, String> keyNameMap = null;
			if (dqElemId.equals(MdekSysList.DQ_109_CompletenessComission.getDqElementId())) {
				keyNameMap = catalogService.getSysListKeyNameMap(
						MdekSysList.DQ_109_CompletenessComission.getDbValue(),
						catalogService.getCatalogLanguage());				
			} else if (dqElemId.equals(MdekSysList.DQ_112_ConceptualConsistency.getDqElementId())) {
				keyNameMap = catalogService.getSysListKeyNameMap(
						MdekSysList.DQ_112_ConceptualConsistency.getDbValue(),
						catalogService.getCatalogLanguage());				
			} else if (dqElemId.equals(MdekSysList.DQ_113_DomainConsistency.getDqElementId())) {
				keyNameMap = catalogService.getSysListKeyNameMap(
						MdekSysList.DQ_113_DomainConsistency.getDbValue(),
						catalogService.getCatalogLanguage());				
			} else if (dqElemId.equals(MdekSysList.DQ_114_FormatConsistency.getDqElementId())) {
				keyNameMap = catalogService.getSysListKeyNameMap(
						MdekSysList.DQ_114_FormatConsistency.getDbValue(),
						catalogService.getCatalogLanguage());				
			} else if (dqElemId.equals(MdekSysList.DQ_126_NonQuantitativeAttributeAccuracy.getDqElementId())) {
				keyNameMap = catalogService.getSysListKeyNameMap(
						MdekSysList.DQ_126_NonQuantitativeAttributeAccuracy.getDbValue(),
						catalogService.getCatalogLanguage());				
			} else if (dqElemId.equals(MdekSysList.DQ_127_QuantitativeAttributeAccuracy.getDqElementId())) {
				keyNameMap = catalogService.getSysListKeyNameMap(
						MdekSysList.DQ_127_QuantitativeAttributeAccuracy.getDbValue(),
						catalogService.getCatalogLanguage());				
			} else if (dqElemId.equals(MdekSysList.DQ_120_TemporalConsistency.getDqElementId())) {
				keyNameMap = catalogService.getSysListKeyNameMap(
						MdekSysList.DQ_120_TemporalConsistency.getDbValue(),
						catalogService.getCatalogLanguage());				
			} else if (dqElemId.equals(MdekSysList.DQ_125_ThematicClassificationCorrectness.getDqElementId())) {
				keyNameMap = catalogService.getSysListKeyNameMap(
						MdekSysList.DQ_125_ThematicClassificationCorrectness.getDbValue(),
						catalogService.getCatalogLanguage());				
			} else if (dqElemId.equals(MdekSysList.DQ_115_TopologicalConsistency.getDqElementId())) {
				keyNameMap = catalogService.getSysListKeyNameMap(
						MdekSysList.DQ_115_TopologicalConsistency.getDbValue(),
						catalogService.getCatalogLanguage());				
			}

			if (keyNameMap != null) {
				bean.setNameOfMeasureValue(keyNameMap.get(entryKey));
			}
		}
		
		return bean;
	}

	private IEntity processKeyValueObjectFormatInspire(ObjectFormatInspire bean) {
		Integer entryKey = bean.getFormatKey();
		if (entryKey != null && entryKey > -1) {
			Map<Integer, String> keyNameMap = catalogService.getSysListKeyNameMap(
				MdekSysList.OBJ_FORMAT_INSPIRE.getDbValue(),
				catalogService.getCatalogLanguage());

			bean.setFormatValue(keyNameMap.get(entryKey));
		}

		return bean;
	}

	private IEntity processKeyValueAdditionalFieldData(AdditionalFieldData bean) {
		String entryKey = bean.getListItemId();
		if (entryKey != null && !entryKey.trim().isEmpty() &&
				!entryKey.trim().equals("-1")) {
			Map<String, String> keyNameMap = catalogService.getProfileFieldListKeyNameMap(
				bean.getFieldKey(),
				catalogService.getCatalogLanguage());

			String listValue = keyNameMap.get(entryKey);
			// Keep old value if selection list not found !
			if (listValue != null) {
				bean.setData(listValue);				
			}
		}

		return bean;
	}

	private IEntity processKeyValueSpatialSystem(SpatialSystem bean) {
		Integer entryKey = bean.getReferencesystemKey();
		if (entryKey != null && entryKey > -1) {
			Map<Integer, String> keyNameMap = catalogService.getSysListKeyNameMap(
				MdekSysList.OBJ_GEO_REFERENCESYSTEM.getDbValue(),
				catalogService.getCatalogLanguage());

			bean.setReferencesystemValue(keyNameMap.get(entryKey));
		}
		
		return bean;
	}

	private IEntity processKeyValueObjectTypesCatalogue(ObjectTypesCatalogue bean) {
		Integer entryKey = bean.getTitleKey();
		if (entryKey != null && entryKey > -1) {
			Map<Integer, String> keyNameMap = catalogService.getSysListKeyNameMap(
				MdekSysList.OBJ_TYPES_CATALOGUE.getDbValue(),
				catalogService.getCatalogLanguage());

			if (keyNameMap.get(entryKey) != null) {
				// entry found in syslist, set name !
				bean.setTitleValue(keyNameMap.get(entryKey));
			} else {
				// entry NOT found in syslist ! transform to free entry cause may be changed in IGE outside codelist repo !
				// see INGRID33-29
				logTransformToFreeEntry(MdekSysList.OBJ_TYPES_CATALOGUE, entryKey, bean.getTitleValue());
				bean.setTitleKey(-1);
			}
		}
		
		return bean;
	}

	private IEntity processKeyValueT011ObjServOpPlatform(T011ObjServOpPlatform bean) {
		Integer entryKey = bean.getPlatformKey();
		if (entryKey != null && entryKey > -1) {
			Map<Integer, String> keyNameMap = catalogService.getSysListKeyNameMap(
				MdekSysList.OBJ_SERV_OPERATION_PLATFORM.getDbValue(),
				catalogService.getCatalogLanguage());

			bean.setPlatformValue(keyNameMap.get(entryKey));
		}
		
		return bean;
	}

	private IEntity processKeyValueObjectOpenDataCategory(ObjectOpenDataCategory bean) {
		Integer entryKey = bean.getCategoryKey();
		if (entryKey != null && entryKey > -1) {
			Map<Integer, String> keyNameMap = catalogService.getSysListKeyNameMap(
				MdekSysList.OBJ_OPEN_DATA_CATEGORY.getDbValue(),
				catalogService.getCatalogLanguage());

			if (keyNameMap.get(entryKey) != null) {
				// entry found in syslist, set name !
				bean.setCategoryValue(keyNameMap.get(entryKey));
			} else {
				// entry NOT found in syslist ! transform to free entry cause may be changed in IGE outside codelist repo !
				// see INGRID33-29
				logTransformToFreeEntry(MdekSysList.OBJ_OPEN_DATA_CATEGORY, entryKey, bean.getCategoryValue());
				bean.setCategoryKey(-1);
			}
		}
		
		return bean;
	}

    private IEntity processKeyValueObjectUseConstraint(ObjectUseConstraint bean) {
        Integer entryKey = bean.getLicenseKey();
        if (entryKey != null && entryKey > -1) {
            Map<Integer, String> keyNameMap = catalogService.getSysListKeyNameMap(
                MdekSysList.OBJ_USE_LICENCE.getDbValue(),
                catalogService.getCatalogLanguage());

            if (keyNameMap.get(entryKey) != null) {
                // entry found in syslist, set name !
                bean.setLicenseValue(keyNameMap.get(entryKey));
            } else {
                // entry NOT found in syslist ! transform to free entry (so we keep value), ok ?
                logTransformToFreeEntry(MdekSysList.OBJ_USE_LICENCE, entryKey, bean.getLicenseValue());
                bean.setLicenseKey(-1);
            }
        }
        
        return bean;
    }

	private void logTransformToFreeEntry(MdekSysList list, Integer entryKey, String entryName) {
		LOG.warn("Syslist Entry with key " + entryKey + " NOT found in Syslist " +
				list + " (" + list.getDbValue() + ") !" +
				" We transform to FREE ENTRY key/value = -1/\"" + entryName + "\"");
	}
}
