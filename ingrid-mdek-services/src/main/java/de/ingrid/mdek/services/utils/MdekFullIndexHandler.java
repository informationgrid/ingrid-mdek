/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2016 wemove digital solutions GmbH
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

import java.util.Set;

import org.apache.log4j.Logger;

import de.ingrid.mdek.EnumUtil;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekUtils.AddressType;
import de.ingrid.mdek.MdekUtils.MdekSysList;
import de.ingrid.mdek.MdekUtils.ObjectType;
import de.ingrid.mdek.MdekUtils.SearchtermType;
import de.ingrid.mdek.MdekUtils.SpatialReferenceType;
import de.ingrid.mdek.services.catalog.MdekCatalogService;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.IEntity;
import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.dao.hibernate.IFullIndexAccess;
import de.ingrid.mdek.services.persistence.db.model.AdditionalFieldData;
import de.ingrid.mdek.services.persistence.db.model.AddressComment;
import de.ingrid.mdek.services.persistence.db.model.AddressNode;
import de.ingrid.mdek.services.persistence.db.model.FullIndexAddr;
import de.ingrid.mdek.services.persistence.db.model.FullIndexObj;
import de.ingrid.mdek.services.persistence.db.model.ObjectAccess;
import de.ingrid.mdek.services.persistence.db.model.ObjectComment;
import de.ingrid.mdek.services.persistence.db.model.ObjectConformity;
import de.ingrid.mdek.services.persistence.db.model.ObjectDataQuality;
import de.ingrid.mdek.services.persistence.db.model.ObjectFormatInspire;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;
import de.ingrid.mdek.services.persistence.db.model.ObjectOpenDataCategory;
import de.ingrid.mdek.services.persistence.db.model.ObjectTypesCatalogue;
import de.ingrid.mdek.services.persistence.db.model.ObjectUse;
import de.ingrid.mdek.services.persistence.db.model.ObjectUseConstraint;
import de.ingrid.mdek.services.persistence.db.model.SearchtermAdr;
import de.ingrid.mdek.services.persistence.db.model.SearchtermObj;
import de.ingrid.mdek.services.persistence.db.model.SearchtermValue;
import de.ingrid.mdek.services.persistence.db.model.SpatialRefValue;
import de.ingrid.mdek.services.persistence.db.model.SpatialReference;
import de.ingrid.mdek.services.persistence.db.model.SpatialSystem;
import de.ingrid.mdek.services.persistence.db.model.T0110AvailFormat;
import de.ingrid.mdek.services.persistence.db.model.T0112MediaOption;
import de.ingrid.mdek.services.persistence.db.model.T011ObjData;
import de.ingrid.mdek.services.persistence.db.model.T011ObjDataPara;
import de.ingrid.mdek.services.persistence.db.model.T011ObjGeo;
import de.ingrid.mdek.services.persistence.db.model.T011ObjGeoSymc;
import de.ingrid.mdek.services.persistence.db.model.T011ObjLiterature;
import de.ingrid.mdek.services.persistence.db.model.T011ObjProject;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServ;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServOpConnpoint;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServOpDepends;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServOpPara;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServOpPlatform;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServOperation;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServType;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServUrl;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServVersion;
import de.ingrid.mdek.services.persistence.db.model.T014InfoImpart;
import de.ingrid.mdek.services.persistence.db.model.T015Legist;
import de.ingrid.mdek.services.persistence.db.model.T017UrlRef;
import de.ingrid.mdek.services.persistence.db.model.T01Object;
import de.ingrid.mdek.services.persistence.db.model.T021Communication;
import de.ingrid.mdek.services.persistence.db.model.T02Address;


/**
 * Handles Update of Index.
 */
public class MdekFullIndexHandler implements IFullIndexAccess {

	private static final Logger LOG = Logger.getLogger(MdekFullIndexHandler.class);
	
	protected MdekCatalogService catalogService;

	private IGenericDao<IEntity> daoFullIndexAddr;
	private IGenericDao<IEntity> daoFullIndexObj;

	private static MdekFullIndexHandler myInstance;

	/** Get The Singleton */
	public static synchronized MdekFullIndexHandler getInstance(DaoFactory daoFactory) {
		if (myInstance == null) {
	        myInstance = new MdekFullIndexHandler(daoFactory);
	      }
		return myInstance;
	}

	private MdekFullIndexHandler(DaoFactory daoFactory) {
		catalogService = MdekCatalogService.getInstance(daoFactory);

		daoFullIndexAddr = daoFactory.getDao(FullIndexAddr.class);
		daoFullIndexObj = daoFactory.getDao(FullIndexObj.class);
	}

	/** Updates data of given address in index. */
	public void updateAddressIndex(AddressNode aNode) {
		// CHECK WHETHER ADDRESS IS HIDDEN USER ADDRESS !
		// WE DO NOT INDEX USERS !
		if (AddressType.getIGEUserParentUuid().equals(aNode.getFkAddrUuid())) {
			LOG.info("IGE USER address " + aNode.getAddrUuid() + " passed to Address Index !!! Newly created ? We do NOT add to index !");
			return;
		}

		// we write data of working version into index !!!
		T02Address a = aNode.getT02AddressWork();
		if (a == null) {
			// this should never happen, so log this!
			LOG.error("Address for building index is null. Writing empty index !!!");
		}

		// update FULL data

		// template for accessing data in index
		FullIndexAddr template = new FullIndexAddr();
		template.setAddrNodeId(aNode.getId());
		template.setIdxName(IDX_NAME_FULLTEXT);
		FullIndexAddr idxEntry = (FullIndexAddr) daoFullIndexAddr.findUniqueByExample(template);		
		if (idxEntry == null) {
			idxEntry = template;
		}
		String data = getFullData(a);
		// end with final separator !!!
		idxEntry.setIdxValue(data + IDX_SEPARATOR);
		daoFullIndexAddr.makePersistent(idxEntry);

		// update PARTIAL data

		template = new FullIndexAddr();
		template.setAddrNodeId(aNode.getId());
		template.setIdxName(IDX_NAME_PARTIAL);
		idxEntry = (FullIndexAddr) daoFullIndexAddr.findUniqueByExample(template);		
		if (idxEntry == null) {
			idxEntry = template;
		}
		data = getPartialData(a);
		// end with final separator !!!
		idxEntry.setIdxValue(data + IDX_SEPARATOR);
		daoFullIndexAddr.makePersistent(idxEntry);
	}

	/** Updates data of given object in index. */
	public void updateObjectIndex(ObjectNode oNode) {
		// we write data of working version into index !!!
		T01Object o = oNode.getT01ObjectWork();
		if (o == null) {
			// this should never happen, so log this!
			LOG.error("Object for building index is null. Writing empty index !!!");
		}

		// update FULL data
		updateObjectIndexEntry(oNode.getId(), IDX_NAME_FULLTEXT, getFullData(o));

		// update THESAURUS data
        updateObjectIndexEntry(oNode.getId(), IDX_NAME_THESAURUS, getThesaurusData(o));

		// update GEO THESAURUS data
        updateObjectIndexEntry(oNode.getId(), IDX_NAME_GEOTHESAURUS, getGeothesaurusData(o));
	}
	
    /** Writes node data to index of given type */
    private void updateObjectIndexEntry(Long nodeId, String whichIndex, String nodeData) {
        FullIndexObj template = new FullIndexObj();
        template.setObjNodeId(nodeId);
        template.setIdxName(whichIndex);
        
        FullIndexObj idxEntry = (FullIndexObj) daoFullIndexObj.findUniqueByExample(template);
        if (idxEntry == null) {
            // create new entry synchronized cause had problems with HH instance with multiple entries for same node !
            createObjectIndexEntry(template, nodeData);
        } else {
            // end with final separator !!!
            idxEntry.setIdxValue(nodeData + IDX_SEPARATOR);
            daoFullIndexObj.makePersistent(idxEntry);            
        }
    }

    /** Create new index entry SYNCHRONIZED !
     * Cause had problems with HH instance with multiple entries for same node ! */
    private synchronized void createObjectIndexEntry(FullIndexObj template, String nodeData) {
        // First SELECT AGAIN to guarantee entry not there !!!
        FullIndexObj idxEntry = (FullIndexObj) daoFullIndexObj.findUniqueByExample(template);       
        if (idxEntry == null) {
            idxEntry = template;
        }
        // end with final separator !!!
        idxEntry.setIdxValue(nodeData + IDX_SEPARATOR);
        daoFullIndexObj.makePersistent(idxEntry);
    }

	/** Get full data of given address for updating full text index. */
	private String getFullData(T02Address a) {
		StringBuffer data = new StringBuffer();

		if (a == null) {
			return data.toString();
		}
		
		// AddressComment
		Set<AddressComment> comments = a.getAddressComments();
		for (AddressComment comment : comments) {
			extendFullData(data, comment.getComment());
		}
		// SearchtermAdr
		data.append(getSearchtermData(a));
		// T021Communication
		Set<T021Communication> comms = a.getT021Communications();
		for (T021Communication comm : comms) {
			extendFullData(data, comm.getCommValue());
			extendFullData(data, comm.getDescr());
		}
		// T02Address
		extendFullData(data, a.getAdrUuid());
		extendFullData(data, a.getOrgAdrId());
		extendFullData(data, a.getInstitution());
		extendFullData(data, a.getLastname());
		extendFullData(data, a.getFirstname());
		extendFullDataWithSysList(data, MdekSysList.ADDRESS_VALUE, a.getAddressKey(), a.getAddressValue());
		extendFullDataWithSysList(data, MdekSysList.ADDRESS_TITLE, a.getTitleKey(), a.getTitleValue());
		extendFullData(data, a.getStreet());
		extendFullData(data, a.getPostcode());
		extendFullData(data, a.getPostbox());
		extendFullData(data, a.getPostboxPc());
		extendFullData(data, a.getCity());
		extendFullData(data, a.getJob());
		extendFullDataWithSysList(data, MdekSysList.COUNTRY, a.getCountryKey(), a.getCountryValue());
		extendFullDataWithSysList(data, MdekSysList.ADMINISTRATIVE_AREA, a.getAdministrativeAreaKey(), a.getAdministrativeAreaValue());
		extendFullData(data, a.getHoursOfService());

		return data.toString();
	}

	/** Get searchterm/thesaurus data of given address for updating index. */
	private String getSearchtermData(T02Address a) {
		StringBuffer data = new StringBuffer();

		// SearchtermAdr
		Set<SearchtermAdr> terms = a.getSearchtermAdrs();
		for (SearchtermAdr term : terms) {
			SearchtermValue termValue = term.getSearchtermValue();
			SearchtermType termType = EnumUtil.mapDatabaseToEnumConst(SearchtermType.class, termValue.getType());
			extendFullData(data, termValue.getTerm());
			if (SearchtermType.isThesaurusType(termType)) {
				extendFullData(data, termValue.getSearchtermSns().getSnsId());
				extendFullData(data, termValue.getSearchtermSns().getGemetId());
				extendFullData(data, termValue.getAlternateTerm());
			}
		}

		return data.toString();
	}	
	
	/** Get partial data of given address for updating full text index. */
	private String getPartialData(T02Address a) {
		StringBuffer data = new StringBuffer();

		if (a == null) {
			return data.toString();
		}
		
		// SearchtermAdr
		data.append(getSearchtermData(a));
		// T02Address
		extendFullData(data, a.getInstitution());
		extendFullData(data, a.getLastname());
		extendFullData(data, a.getFirstname());

		return data.toString();
	}	

	/** Get full data of given object for updating full text index. */
	private String getFullData(T01Object o) {
		StringBuffer data = new StringBuffer();
		
		if (o == null) {
			return data.toString();
		}
		
		// ObjectComment
		Set<ObjectComment> comments = o.getObjectComments();
		for (ObjectComment comment : comments) {
			extendFullData(data, comment.getComment());
		}
		// SearchtermObj
		Set<SearchtermObj> terms = o.getSearchtermObjs();
		for (SearchtermObj term : terms) {
			SearchtermValue termValue = term.getSearchtermValue();
			SearchtermType termType = EnumUtil.mapDatabaseToEnumConst(SearchtermType.class, termValue.getType());
			extendFullData(data, termValue.getTerm());
			if (SearchtermType.isThesaurusType(termType)) {
				extendFullData(data, termValue.getSearchtermSns().getSnsId());
				extendFullData(data, termValue.getSearchtermSns().getGemetId());
				extendFullData(data, termValue.getAlternateTerm());
			}
		}
		// SpatialReference
		Set<SpatialReference> spatRefs = o.getSpatialReferences();
		for (SpatialReference spatRef : spatRefs) {
			SpatialRefValue spatRefValue = spatRef.getSpatialRefValue();
			extendFullData(data, spatRefValue.getNativekey());
			SpatialReferenceType spatRefType = EnumUtil.mapDatabaseToEnumConst(SpatialReferenceType.class, spatRefValue.getType());
			if (spatRefType == SpatialReferenceType.FREI) {
				extendFullDataWithSysList(data, MdekSysList.SPATIAL_REF_VALUE,
						spatRefValue.getNameKey(), spatRefValue.getNameValue());
			} else if (spatRefType == SpatialReferenceType.GEO_THESAURUS) {
				extendFullDataWithSysList(data, MdekSysList.SPATIAL_REF_VALUE,
						spatRefValue.getNameKey(), spatRefValue.getNameValue());
				extendFullData(data, spatRefValue.getSpatialRefSns().getSnsId());
				extendFullData(data, spatRefValue.getTopicType());
			}
		}
		// T0110AvailFormat
		Set<T0110AvailFormat> formats = o.getT0110AvailFormats();
		for (T0110AvailFormat format : formats) {
			extendFullDataWithSysList(data, MdekSysList.AVAIL_FORMAT,
					format.getFormatKey(), format.getFormatValue());
			extendFullData(data, format.getVer());
			extendFullData(data, format.getFileDecompressionTechnique());
			extendFullData(data, format.getSpecification());
		}
		// T0112MediaOption
		Set<T0112MediaOption> medOpts = o.getT0112MediaOptions();
		for (T0112MediaOption medOpt : medOpts) {
			extendFullData(data, medOpt.getMediumNote());
			extendFullDataWithSysList(data, MdekSysList.MEDIA_OPTION_MEDIUM,
					medOpt.getMediumName(), null);
		}
		// T011ObjData
		Set<T011ObjData> oDatas = o.getT011ObjDatas();
		for (T011ObjData oData : oDatas) {
			extendFullData(data, oData.getBase());
			extendFullData(data, oData.getDescription());
		}
		// T011ObjDataPara
		Set<T011ObjDataPara> oDataParas = o.getT011ObjDataParas();
		for (T011ObjDataPara oDataPara : oDataParas) {
			extendFullData(data, oDataPara.getParameter());
			extendFullData(data, oDataPara.getUnit());
		}
		Set<ObjectTypesCatalogue> oTypesCats = o.getObjectTypesCatalogues();
		for (ObjectTypesCatalogue oTypesCat : oTypesCats) {
			extendFullData(data, MdekUtils.timestampToDisplayDate(oTypesCat.getTypeDate()));
			extendFullData(data, oTypesCat.getTypeVersion());
			extendFullDataWithSysList(data, MdekSysList.OBJ_TYPES_CATALOGUE,
					oTypesCat.getTitleKey(), oTypesCat.getTitleValue());
		}
		// T011ObjGeo
		Set<T011ObjGeo> oGeos = o.getT011ObjGeos();
		for (T011ObjGeo oGeo : oGeos) {
			extendFullData(data, oGeo.getSpecialBase());
			extendFullData(data, oGeo.getDataBase());
			extendFullData(data, oGeo.getMethod());
			Set<T011ObjGeoSymc> oGeoSymcs = oGeo.getT011ObjGeoSymcs();
			for (T011ObjGeoSymc oGeoSymc : oGeoSymcs) {
				extendFullData(data, MdekUtils.timestampToDisplayDate(oGeoSymc.getSymbolDate()));
				extendFullData(data, oGeoSymc.getEdition());
				extendFullDataWithSysList(data, MdekSysList.OBJ_GEO_SYMC,
						oGeoSymc.getSymbolCatKey(), oGeoSymc.getSymbolCatValue());
			}
		}
		// T011ObjLiterature
		Set<T011ObjLiterature> oLits = o.getT011ObjLiteratures();
		for (T011ObjLiterature oLit : oLits) {
			extendFullData(data, oLit.getAuthor());			
			extendFullData(data, oLit.getPublisher());			
			extendFullData(data, oLit.getPublishIn());			
			extendFullData(data, oLit.getVolume());			
			extendFullData(data, oLit.getSides());			
			extendFullData(data, oLit.getPublishYear());			
			extendFullData(data, oLit.getPublishLoc());			
			extendFullData(data, oLit.getLoc());			
			extendFullData(data, oLit.getDocInfo());			
			extendFullData(data, oLit.getBase());			
			extendFullData(data, oLit.getIsbn());			
			extendFullData(data, oLit.getPublishing());			
			extendFullData(data, oLit.getDescription());			
			extendFullDataWithSysList(data, MdekSysList.OBJ_LITERATURE_TYPE,
					oLit.getTypeKey(), oLit.getTypeValue());
		}
		// T011ObjProject
		Set<T011ObjProject> oProjs = o.getT011ObjProjects();
		for (T011ObjProject oProj : oProjs) {
			extendFullData(data, oProj.getLeader());
			extendFullData(data, oProj.getMember());
			extendFullData(data, oProj.getDescription());
		}
		// T011ObjServ
		Set<T011ObjServ> oServs = o.getT011ObjServs();
		for (T011ObjServ oServ : oServs) {
			extendFullData(data, oServ.getHistory());
			extendFullData(data, oServ.getEnvironment());
			extendFullData(data, oServ.getBase());
			extendFullData(data, oServ.getDescription());
			extendFullData(data, oServ.getCouplingType());
			if (MdekUtils.YES.equals(oServ.getHasAtomDownload())) {
				extendFullData(data, IDX_VALUE_HAS_ATOM_DOWNLOAD);
			}

			Integer oServTypeKey = oServ.getTypeKey();

			// ServType syslist is dependent from class of object !
			// default is class 3 = "Geodatendienst"
			MdekSysList sysListServType = MdekSysList.OBJ_SERV_TYPE;
			// change syslist if class 6 = "Informationssystem/Dienst/Anwendung"
			if (ObjectType.INFOSYSTEM_DIENST.getDbValue().equals(o.getObjClass())) {
				sysListServType = MdekSysList.OBJ_SERV_TYPE_CLASS_6;
			}
			extendFullDataWithSysList(data, sysListServType,
				oServTypeKey, oServ.getTypeValue());

			Set<T011ObjServType> oServClassifications = oServ.getT011ObjServTypes();
			for (T011ObjServType oServClassification : oServClassifications) {
				extendFullDataWithSysList(data, MdekSysList.OBJ_SERV_TYPE2,
					oServClassification.getServTypeKey(), oServClassification.getServTypeValue());				
			}
			Set<T011ObjServOperation> oServOps = oServ.getT011ObjServOperations();
			for (T011ObjServOperation oServOp : oServOps) {
				if (MdekUtils.OBJ_SERV_TYPE_WMS.equals(oServTypeKey)) {
					extendFullDataWithSysList(data, MdekSysList.OBJ_SERV_OPERATION_WMS,
							oServOp.getNameKey(), oServOp.getNameValue());				
				} else if (MdekUtils.OBJ_SERV_TYPE_WFS.equals(oServTypeKey)) {
					extendFullDataWithSysList(data, MdekSysList.OBJ_SERV_OPERATION_WFS,
							oServOp.getNameKey(), oServOp.getNameValue());				
				} else if (MdekUtils.OBJ_SERV_TYPE_CSW.equals(oServTypeKey)) {
					extendFullDataWithSysList(data, MdekSysList.OBJ_SERV_OPERATION_CSW,
							oServOp.getNameKey(), oServOp.getNameValue());				
				} else if (MdekUtils.OBJ_SERV_TYPE_WCTS.equals(oServTypeKey)) {
					extendFullDataWithSysList(data, MdekSysList.OBJ_SERV_OPERATION_WCTS,
							oServOp.getNameKey(), oServOp.getNameValue());				
				} else {
					extendFullData(data, oServOp.getNameValue());					
				}
				extendFullData(data, oServOp.getDescr());
				extendFullData(data, oServOp.getInvocationName());
				Set<T011ObjServOpConnpoint> oServOpConnpts = oServOp.getT011ObjServOpConnpoints();
				for (T011ObjServOpConnpoint oServOpConnpt : oServOpConnpts) {
					extendFullData(data, oServOpConnpt.getConnectPoint());
				}
				Set<T011ObjServOpDepends> oServOpDeps = oServOp.getT011ObjServOpDependss();
				for (T011ObjServOpDepends oServOpDep : oServOpDeps) {
					extendFullData(data, oServOpDep.getDependsOn());
				}
				Set<T011ObjServOpPara> oServOpParas = oServOp.getT011ObjServOpParas();
				for (T011ObjServOpPara oServOpPara : oServOpParas) {
					extendFullData(data, oServOpPara.getName());
					extendFullData(data, oServOpPara.getDirection());
					extendFullData(data, oServOpPara.getDescr());
				}
				Set<T011ObjServOpPlatform> oServOpPlatfs = oServOp.getT011ObjServOpPlatforms();
				for (T011ObjServOpPlatform oServOpPlatf : oServOpPlatfs) {
					extendFullData(data, oServOpPlatf.getPlatformValue());
				}
			}
			Set<T011ObjServVersion> oServVersions = oServ.getT011ObjServVersions();
			for (T011ObjServVersion oServVersion : oServVersions) {
				extendFullData(data, oServVersion.getVersionValue());
			}
			Set<T011ObjServUrl> oServUrls = oServ.getT011ObjServUrls();
			for (T011ObjServUrl oServUrl : oServUrls) {
				extendFullData(data, oServUrl.getName());
				extendFullData(data, oServUrl.getUrl());
				extendFullData(data, oServUrl.getDescription());
			}
		}
		// T014InfoImpart
		Set<T014InfoImpart> infoImps = o.getT014InfoImparts();
		for (T014InfoImpart infoImp : infoImps) {
			extendFullDataWithSysList(data, MdekSysList.INFO_IMPART,
					infoImp.getImpartKey(), infoImp.getImpartValue());				
		}
		// T015Legist
		Set<T015Legist> oLegists = o.getT015Legists();
		for (T015Legist oLegist : oLegists) {
			extendFullDataWithSysList(data, MdekSysList.LEGIST,
					oLegist.getLegistKey(), oLegist.getLegistValue());				
		}
		// T017UrlRef
		Set<T017UrlRef> oUrlRefs = o.getT017UrlRefs();
		for (T017UrlRef oUrlRef : oUrlRefs) {
			extendFullData(data, oUrlRef.getUrlLink());
			extendFullData(data, oUrlRef.getContent());
			extendFullData(data, oUrlRef.getDescr());
			extendFullDataWithSysList(data, MdekSysList.URL_REF_SPECIAL,
					oUrlRef.getSpecialRef(), oUrlRef.getSpecialName());				
			extendFullDataWithSysList(data, MdekSysList.URL_REF_DATATYPE,
					oUrlRef.getDatatypeKey(), oUrlRef.getDatatypeValue());				
		}
		// ObjectConformity
		Set<ObjectConformity> objConforms = o.getObjectConformitys();
		for (ObjectConformity objConform : objConforms) {
			extendFullDataWithSysList(data, MdekSysList.OBJ_CONFORMITY_SPECIFICATION,
					objConform.getSpecificationKey(), objConform.getSpecificationValue());				
			extendFullDataWithSysList(data, MdekSysList.OBJ_CONFORMITY_DEGREE,
					objConform.getDegreeKey(), objConform.getDegreeValue());				
		}
		// ObjectAccess
		Set<ObjectAccess> objAccesses = o.getObjectAccesss();
		for (ObjectAccess objAccess : objAccesses) {
			extendFullDataWithSysList(data, MdekSysList.OBJ_ACCESS,
					objAccess.getRestrictionKey(), objAccess.getRestrictionValue());				
		}
		// ObjectUse
		Set<ObjectUse> objUses = o.getObjectUses();
		for (ObjectUse objUse : objUses) {
            extendFullData(data, objUse.getTermsOfUseValue());
		}
        // ObjectUseConstraint
        Set<ObjectUseConstraint> objUseConstraints = o.getObjectUseConstraints();
        for (ObjectUseConstraint objUseConstraint : objUseConstraints) {
            extendFullDataWithSysList(data, MdekSysList.OBJ_USE_LICENCE,
                    objUseConstraint.getLicenseKey(), objUseConstraint.getLicenseValue());                
        }
		// ObjectOpenDataCategory
		Set<ObjectOpenDataCategory> objOpenDataCats = o.getObjectOpenDataCategorys();
		for (ObjectOpenDataCategory objOpenDataCat : objOpenDataCats) {
			extendFullDataWithSysList(data, MdekSysList.OBJ_OPEN_DATA_CATEGORY,
					objOpenDataCat.getCategoryKey(), objOpenDataCat.getCategoryValue());				
		}
		// Data Quality
		Set<ObjectDataQuality> objDQs = o.getObjectDataQualitys();
		for (ObjectDataQuality objDQ : objDQs) {
			extendFullData(data, objDQ.getNameOfMeasureValue());
			extendFullData(data, objDQ.getResultValue());
			extendFullData(data, objDQ.getMeasureDescription());
		}
		// ObjectFormatInspire
		Set<ObjectFormatInspire> objFormatInspires = o.getObjectFormatInspires();
		for (ObjectFormatInspire objFormatInspire : objFormatInspires) {
			extendFullDataWithSysList(data, MdekSysList.OBJ_FORMAT_INSPIRE,
					objFormatInspire.getFormatKey(), objFormatInspire.getFormatValue());				
		}
		// SpatialSystem
		Set<SpatialSystem> spatialSystems = o.getSpatialSystems();
		for (SpatialSystem spatialSystem : spatialSystems) {
			extendFullDataWithSysList(data, MdekSysList.OBJ_GEO_REFERENCESYSTEM,
					spatialSystem.getReferencesystemKey(), spatialSystem.getReferencesystemValue());
		}
		// AdditionalFieldData
		extendFullData(data, o.getAdditionalFieldDatas());

		// T01Object
		extendFullData(data, o.getObjUuid());
		extendFullData(data, o.getObjName());
		extendFullData(data, o.getOrgObjId());
		extendFullData(data, o.getObjDescr());
		extendFullData(data, o.getInfoNote());
		extendFullData(data, o.getLocDescr());
		extendFullData(data, o.getTimeDescr());
		extendFullData(data, o.getDatasetAlternateName());
		extendFullData(data, o.getDatasetUsage());
		extendFullData(data, o.getMetadataStandardName());
		extendFullData(data, o.getMetadataStandardVersion());
		extendFullData(data, o.getOrderingInstructions());
		extendFullData(data, o.getVerticalExtentVdatumValue());
		if (MdekUtils.YES.equals(o.getIsInspireRelevant())) {
			extendFullData(data, IDX_VALUE_IS_INSPIRE_RELEVANT);
		}
		if (MdekUtils.YES.equals(o.getIsOpenData())) {
			extendFullData(data, IDX_VALUE_IS_OPEN_DATA);
		}
		if (MdekUtils.YES.equals(o.getIsAdvCompatible())) {
		    extendFullData(data, IDX_VALUE_IS_ADV_COMPATIBLE);
		}

		return data.toString();
	}

	/** Get thesaurus data of given object for updating thesaurus index. */
	private String getThesaurusData(T01Object o) {
		StringBuffer data = new StringBuffer();

		// SearchtermObj
		Set<SearchtermObj> terms = o.getSearchtermObjs();
		for (SearchtermObj term : terms) {
			SearchtermValue termValue = term.getSearchtermValue();
			SearchtermType termType = EnumUtil.mapDatabaseToEnumConst(SearchtermType.class, termValue.getType());
			if (termType == SearchtermType.UMTHES || termType == SearchtermType.GEMET) {
				extendFullData(data, termValue.getSearchtermSns().getSnsId());
				extendFullData(data, termValue.getSearchtermSns().getGemetId());
			}
		}

		return data.toString();
	}	
	
	/** Get geothesaurus data of given object for updating geothesaurus index. */
	private String getGeothesaurusData(T01Object o) {
		StringBuffer data = new StringBuffer();

		Set<SpatialReference> spatRefs = o.getSpatialReferences();
		for (SpatialReference spatRef : spatRefs) {
			SpatialRefValue spatRefValue = spatRef.getSpatialRefValue();
			SpatialReferenceType spatRefType = EnumUtil.mapDatabaseToEnumConst(SpatialReferenceType.class, spatRefValue.getType());
			if (spatRefType == SpatialReferenceType.GEO_THESAURUS) {
				extendFullData(data, spatRefValue.getSpatialRefSns().getSnsId());
			}
		}

		return data.toString();
	}	

	private void extendFullData(StringBuffer fullData, Set<AdditionalFieldData> fieldDatas) {
		if (fieldDatas == null) {
			return;
		}
		for (AdditionalFieldData fieldData : fieldDatas) {
			extendFullData(fullData, fieldData.getData());
			if (fieldData.getAdditionalFieldDatas() != null) {
				extendFullData(fullData, fieldData.getAdditionalFieldDatas());
			}
		}
	}

	/** Append a value to full data. also adds pre-separator
	 * @param fullData full data where value is appended
	 * @param dataToAppend the value to append. ONLY APPENDED IF NOT NULL !
	 */
	private void extendFullData(StringBuffer fullData, String dataToAppend) {
		if (dataToAppend != null) {
			fullData.append(IDX_SEPARATOR);
			fullData.append(dataToAppend);			
		}
	}

	/** Append SysList or free entry value to full data.
	 * @param fullData full data where value is appended
	 * @param sysList which syslist (see enumeration)
	 * @param sysListEntryId the entryId from bean
	 * @param freeValue the free value from bean
	 */
	private void extendFullDataWithSysList(StringBuffer fullData,
			MdekSysList sysList, Integer sysListEntryId, String freeValue) {
		String value = getSysListOrFreeValue(sysList, sysListEntryId, freeValue);
		if (value != null) {
			extendFullData(fullData, value);			
		}
	}

	/** Analyzes given data and returns entry value or free value
	 * @param sysList which syslist (see enumeration)
	 * @param sysListEntryId the entryId from bean
	 * @param freeValue the free value from bean
	 * @return value (may be null if entry not found or free value is null ...)
	 */
	private String getSysListOrFreeValue(MdekSysList sysList, Integer sysListEntryId, String freeValue) {
		String retValue = null;

		if (sysListEntryId == null) {
			return retValue;
		}
		if (MdekSysList.FREE_ENTRY.getDbValue().equals(sysListEntryId)) {
			retValue = freeValue;
		} else {
			retValue = catalogService.getSysListEntryName(sysList.getDbValue(), sysListEntryId);
		}
		
		return retValue;
	}
}
