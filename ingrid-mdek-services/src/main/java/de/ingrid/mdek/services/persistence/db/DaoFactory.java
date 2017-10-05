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
/*
 * Created on 10.07.2007
 */
package de.ingrid.mdek.services.persistence.db;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.mdek.services.persistence.db.dao.IAddressNodeDao;
import de.ingrid.mdek.services.persistence.db.dao.IConsistencyCheckerDao;
import de.ingrid.mdek.services.persistence.db.dao.IHQLDao;
import de.ingrid.mdek.services.persistence.db.dao.IIdcGroupDao;
import de.ingrid.mdek.services.persistence.db.dao.IIdcUserDao;
import de.ingrid.mdek.services.persistence.db.dao.IObjectNodeDao;
import de.ingrid.mdek.services.persistence.db.dao.IPermissionDao;
import de.ingrid.mdek.services.persistence.db.dao.ISearchtermSnsDao;
import de.ingrid.mdek.services.persistence.db.dao.ISearchtermValueDao;
import de.ingrid.mdek.services.persistence.db.dao.ISpatialRefSnsDao;
import de.ingrid.mdek.services.persistence.db.dao.ISpatialRefValueDao;
import de.ingrid.mdek.services.persistence.db.dao.ISysGenericKeyDao;
import de.ingrid.mdek.services.persistence.db.dao.ISysJobInfoDao;
import de.ingrid.mdek.services.persistence.db.dao.ISysListDao;
import de.ingrid.mdek.services.persistence.db.dao.IT01ObjectDao;
import de.ingrid.mdek.services.persistence.db.dao.IT02AddressDao;
import de.ingrid.mdek.services.persistence.db.dao.hibernate.AddressNodeDaoHibernate;
import de.ingrid.mdek.services.persistence.db.dao.hibernate.ConsistencyCheckerDaoHibernate;
import de.ingrid.mdek.services.persistence.db.dao.hibernate.HQLDaoHibernate;
import de.ingrid.mdek.services.persistence.db.dao.hibernate.IdcGroupDaoHibernate;
import de.ingrid.mdek.services.persistence.db.dao.hibernate.IdcUserDaoHibernate;
import de.ingrid.mdek.services.persistence.db.dao.hibernate.ObjectNodeDaoHibernate;
import de.ingrid.mdek.services.persistence.db.dao.hibernate.PermissionDaoHibernate;
import de.ingrid.mdek.services.persistence.db.dao.hibernate.SearchtermSnsDaoHibernate;
import de.ingrid.mdek.services.persistence.db.dao.hibernate.SearchtermValueDaoHibernate;
import de.ingrid.mdek.services.persistence.db.dao.hibernate.SpatialRefSnsDaoHibernate;
import de.ingrid.mdek.services.persistence.db.dao.hibernate.SpatialRefValueDaoHibernate;
import de.ingrid.mdek.services.persistence.db.dao.hibernate.SysGenericKeyDaoHibernate;
import de.ingrid.mdek.services.persistence.db.dao.hibernate.SysJobInfoDaoHibernate;
import de.ingrid.mdek.services.persistence.db.dao.hibernate.SysListDaoHibernate;
import de.ingrid.mdek.services.persistence.db.dao.hibernate.T01ObjectDaoHibernate;
import de.ingrid.mdek.services.persistence.db.dao.hibernate.T02AddressDaoHibernate;
import de.ingrid.mdek.services.persistence.db.model.AdditionalFieldData;
import de.ingrid.mdek.services.persistence.db.model.AddressComment;
import de.ingrid.mdek.services.persistence.db.model.AddressMetadata;
import de.ingrid.mdek.services.persistence.db.model.FullIndexAddr;
import de.ingrid.mdek.services.persistence.db.model.FullIndexObj;
import de.ingrid.mdek.services.persistence.db.model.IdcGroup;
import de.ingrid.mdek.services.persistence.db.model.IdcUserPermission;
import de.ingrid.mdek.services.persistence.db.model.ObjectAccess;
import de.ingrid.mdek.services.persistence.db.model.ObjectAdvProductGroup;
import de.ingrid.mdek.services.persistence.db.model.ObjectComment;
import de.ingrid.mdek.services.persistence.db.model.ObjectConformity;
import de.ingrid.mdek.services.persistence.db.model.ObjectDataLanguage;
import de.ingrid.mdek.services.persistence.db.model.ObjectDataQuality;
import de.ingrid.mdek.services.persistence.db.model.ObjectFormatInspire;
import de.ingrid.mdek.services.persistence.db.model.ObjectMetadata;
import de.ingrid.mdek.services.persistence.db.model.ObjectOpenDataCategory;
import de.ingrid.mdek.services.persistence.db.model.ObjectReference;
import de.ingrid.mdek.services.persistence.db.model.ObjectTypesCatalogue;
import de.ingrid.mdek.services.persistence.db.model.ObjectUse;
import de.ingrid.mdek.services.persistence.db.model.ObjectUseConstraint;
import de.ingrid.mdek.services.persistence.db.model.Permission;
import de.ingrid.mdek.services.persistence.db.model.PermissionAddr;
import de.ingrid.mdek.services.persistence.db.model.PermissionObj;
import de.ingrid.mdek.services.persistence.db.model.SearchtermAdr;
import de.ingrid.mdek.services.persistence.db.model.SearchtermObj;
import de.ingrid.mdek.services.persistence.db.model.SearchtermValue;
import de.ingrid.mdek.services.persistence.db.model.SpatialRefValue;
import de.ingrid.mdek.services.persistence.db.model.SpatialReference;
import de.ingrid.mdek.services.persistence.db.model.SpatialSystem;
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
import de.ingrid.mdek.services.persistence.db.model.T011ObjServType;
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

@Service
public class DaoFactory implements IDaoFactory {

    private final SessionFactory _sessionFactory;

    @Autowired
    DaoFactory(SessionFactory sessionFactory) {
        _sessionFactory = sessionFactory;
    }

    public IHQLDao getHQLDao() {
        return new HQLDaoHibernate(_sessionFactory);
    }

    public IObjectNodeDao getObjectNodeDao() {
        return new ObjectNodeDaoHibernate(_sessionFactory);
    }

    public IAddressNodeDao getAddressNodeDao() {
        return new AddressNodeDaoHibernate(_sessionFactory);
    }

    public IT01ObjectDao getT01ObjectDao() {
        return new T01ObjectDaoHibernate(_sessionFactory);
    }

    public IT02AddressDao getT02AddressDao() {
        return new T02AddressDaoHibernate(_sessionFactory);
    }

    public ISpatialRefValueDao getSpatialRefValueDao() {
        return new SpatialRefValueDaoHibernate(_sessionFactory);
    }

    public ISpatialRefSnsDao getSpatialRefSnsDao() {
        return new SpatialRefSnsDaoHibernate(_sessionFactory);
    }

    public ISearchtermValueDao getSearchtermValueDao() {
        return new SearchtermValueDaoHibernate(_sessionFactory);
    }

    public ISearchtermSnsDao getSearchtermSnsDao() {
        return new SearchtermSnsDaoHibernate(_sessionFactory);
    }

    public ISysListDao getSysListDao() {
        return new SysListDaoHibernate(_sessionFactory);
    }

    public ISysGenericKeyDao getSysGenericKeyDao() {
        return new SysGenericKeyDaoHibernate(_sessionFactory);
    }

    public ISysJobInfoDao getSysJobInfoDao() {
        return new SysJobInfoDaoHibernate(_sessionFactory);
    }

    public IPermissionDao getPermissionDao() {
        return new PermissionDaoHibernate(_sessionFactory);
    }

    public IIdcUserDao getIdcUserDao() {
        return new IdcUserDaoHibernate(_sessionFactory);
    }

    public IIdcGroupDao getIdcGroupDao() {
        return new IdcGroupDaoHibernate(_sessionFactory);
    }
    
    public IConsistencyCheckerDao getConsistencyCheckerDao() {
        return new ConsistencyCheckerDaoHibernate(_sessionFactory);
    }
    
    public IGenericDao<IEntity> getDao(Class clazz) {
		IGenericDao dao = null;

		if (clazz.isAssignableFrom(IEntity.class)) {
			// Generic dao for class unspecific operations !!!
			dao = new GenericHibernateDao<IEntity>(_sessionFactory, IEntity.class);			
		} else if (clazz.isAssignableFrom(SpatialReference.class)) {
			dao = new GenericHibernateDao<SpatialReference>(_sessionFactory, SpatialReference.class);
		} else if (clazz.isAssignableFrom(SearchtermObj.class)) {
			dao = new GenericHibernateDao<SearchtermObj>(_sessionFactory, SearchtermObj.class);
		} else if (clazz.isAssignableFrom(SearchtermAdr.class)) {
			dao = new GenericHibernateDao<SearchtermAdr>(_sessionFactory, SearchtermAdr.class);
		} else if (clazz.isAssignableFrom(T021Communication.class)) {
			dao = new GenericHibernateDao<T021Communication>(_sessionFactory, T021Communication.class);
		} else if (clazz.isAssignableFrom(T012ObjAdr.class)) {
			dao = new GenericHibernateDao<T012ObjAdr>(_sessionFactory, T012ObjAdr.class);
		} else if (clazz.isAssignableFrom(ObjectReference.class)) {
			dao = new GenericHibernateDao<ObjectReference>(_sessionFactory, ObjectReference.class);
		} else if (clazz.isAssignableFrom(T017UrlRef.class)) {
			dao = new GenericHibernateDao<T017UrlRef>(_sessionFactory, T017UrlRef.class);
		} else if (clazz.isAssignableFrom(T0113DatasetReference.class)) {
			dao = new GenericHibernateDao<T0113DatasetReference>(_sessionFactory, T0113DatasetReference.class);
		} else if (clazz.isAssignableFrom(T014InfoImpart.class)) {
			dao = new GenericHibernateDao<T014InfoImpart>(_sessionFactory, T014InfoImpart.class);
		} else if (clazz.isAssignableFrom(T011ObjGeo.class)) {
			dao = new GenericHibernateDao<T011ObjGeo>(_sessionFactory, T011ObjGeo.class);
		} else if (clazz.isAssignableFrom(T015Legist.class)) {
			dao = new GenericHibernateDao<T015Legist>(_sessionFactory, T015Legist.class);
		} else if (clazz.isAssignableFrom(T0110AvailFormat.class)) {
			dao = new GenericHibernateDao<T0110AvailFormat>(_sessionFactory, T0110AvailFormat.class);
		} else if (clazz.isAssignableFrom(T0112MediaOption.class)) {
			dao = new GenericHibernateDao<T0112MediaOption>(_sessionFactory, T0112MediaOption.class);
		} else if (clazz.isAssignableFrom(T0114EnvTopic.class)) {
			dao = new GenericHibernateDao<T0114EnvTopic>(_sessionFactory, T0114EnvTopic.class);
		} else if (clazz.isAssignableFrom(T011ObjTopicCat.class)) {
			dao = new GenericHibernateDao<T011ObjTopicCat>(_sessionFactory, T011ObjTopicCat.class);
		} else if (clazz.isAssignableFrom(T011ObjData.class)) {
			dao = new GenericHibernateDao<T011ObjData>(_sessionFactory, T011ObjData.class);
		} else if (clazz.isAssignableFrom(T011ObjDataPara.class)) {
			dao = new GenericHibernateDao<T011ObjDataPara>(_sessionFactory, T011ObjDataPara.class);
		} else if (clazz.isAssignableFrom(T011ObjProject.class)) {
			dao = new GenericHibernateDao<T011ObjProject>(_sessionFactory, T011ObjProject.class);
		} else if (clazz.isAssignableFrom(T011ObjLiterature.class)) {
			dao = new GenericHibernateDao<T011ObjLiterature>(_sessionFactory, T011ObjLiterature.class);
		} else if (clazz.isAssignableFrom(T011ObjGeoScale.class)) {
			dao = new GenericHibernateDao<T011ObjGeoScale>(_sessionFactory, T011ObjGeoScale.class);
		} else if (clazz.isAssignableFrom(T011ObjGeoSymc.class)) {
			dao = new GenericHibernateDao<T011ObjGeoSymc>(_sessionFactory, T011ObjGeoSymc.class);
		} else if (clazz.isAssignableFrom(T011ObjGeoSupplinfo.class)) {
			dao = new GenericHibernateDao<T011ObjGeoSupplinfo>(_sessionFactory, T011ObjGeoSupplinfo.class);
		} else if (clazz.isAssignableFrom(T011ObjGeoVector.class)) {
			dao = new GenericHibernateDao<T011ObjGeoVector>(_sessionFactory, T011ObjGeoVector.class);
		} else if (clazz.isAssignableFrom(T011ObjGeoSpatialRep.class)) {
			dao = new GenericHibernateDao<T011ObjGeoSpatialRep>(_sessionFactory, T011ObjGeoSpatialRep.class);
		} else if (clazz.isAssignableFrom(ObjectComment.class)) {
			dao = new GenericHibernateDao<ObjectComment>(_sessionFactory, ObjectComment.class);
		} else if (clazz.isAssignableFrom(AddressComment.class)) {
			dao = new GenericHibernateDao<AddressComment>(_sessionFactory, AddressComment.class);
		} else if (clazz.isAssignableFrom(T011ObjServ.class)) {
			dao = new GenericHibernateDao<T011ObjServ>(_sessionFactory, T011ObjServ.class);
		} else if (clazz.isAssignableFrom(T011ObjServVersion.class)) {
			dao = new GenericHibernateDao<T011ObjServVersion>(_sessionFactory, T011ObjServVersion.class);
		} else if (clazz.isAssignableFrom(T011ObjServOperation.class)) {
			dao = new GenericHibernateDao<T011ObjServOperation>(_sessionFactory, T011ObjServOperation.class);
		} else if (clazz.isAssignableFrom(T011ObjServOpPlatform.class)) {
			dao = new GenericHibernateDao<T011ObjServOpPlatform>(_sessionFactory, T011ObjServOpPlatform.class);
		} else if (clazz.isAssignableFrom(T011ObjServOpDepends.class)) {
			dao = new GenericHibernateDao<T011ObjServOpDepends>(_sessionFactory, T011ObjServOpDepends.class);
		} else if (clazz.isAssignableFrom(T011ObjServOpConnpoint.class)) {
			dao = new GenericHibernateDao<T011ObjServOpConnpoint>(_sessionFactory, T011ObjServOpConnpoint.class);
		} else if (clazz.isAssignableFrom(T011ObjServOpPara.class)) {
			dao = new GenericHibernateDao<T011ObjServOpPara>(_sessionFactory, T011ObjServOpPara.class);
		} else if (clazz.isAssignableFrom(T03Catalogue.class)) {
			dao = new GenericHibernateDao<T03Catalogue>(_sessionFactory, T03Catalogue.class);
		} else if (clazz.isAssignableFrom(IdcGroup.class)) {
			dao = getIdcGroupDao();
		} else if (clazz.isAssignableFrom(Permission.class)) {
			dao = getPermissionDao();
		} else if (clazz.isAssignableFrom(IdcUserPermission.class)) {
			dao = new GenericHibernateDao<IdcUserPermission>(_sessionFactory, IdcUserPermission.class);
		} else if (clazz.isAssignableFrom(PermissionObj.class)) {
			dao = new GenericHibernateDao<PermissionObj>(_sessionFactory, PermissionObj.class);
		} else if (clazz.isAssignableFrom(PermissionAddr.class)) {
			dao = new GenericHibernateDao<PermissionAddr>(_sessionFactory, PermissionAddr.class);
		} else if (clazz.isAssignableFrom(FullIndexAddr.class)) {
			dao = new GenericHibernateDao<FullIndexAddr>(_sessionFactory, FullIndexAddr.class);
		} else if (clazz.isAssignableFrom(FullIndexObj.class)) {
			dao = new GenericHibernateDao<FullIndexObj>(_sessionFactory, FullIndexObj.class);
		} else if (clazz.isAssignableFrom(ObjectMetadata.class)) {
			dao = new GenericHibernateDao<ObjectMetadata>(_sessionFactory, ObjectMetadata.class);
		} else if (clazz.isAssignableFrom(AddressMetadata.class)) {
			dao = new GenericHibernateDao<AddressMetadata>(_sessionFactory, AddressMetadata.class);
		} else if (clazz.isAssignableFrom(SpatialRefValue.class)) {
			dao = getSpatialRefValueDao();
		} else if (clazz.isAssignableFrom(T02Address.class)) {
			dao = getT02AddressDao();
		} else if (clazz.isAssignableFrom(ObjectConformity.class)) {
			dao = new GenericHibernateDao<ObjectConformity>(_sessionFactory, ObjectConformity.class);
		} else if (clazz.isAssignableFrom(ObjectAccess.class)) {
			dao = new GenericHibernateDao<ObjectAccess>(_sessionFactory, ObjectAccess.class);
		} else if (clazz.isAssignableFrom(T011ObjServType.class)) {
			dao = new GenericHibernateDao<T011ObjServType>(_sessionFactory, T011ObjServType.class);
		} else if (clazz.isAssignableFrom(SearchtermValue.class)) {
			dao = getSearchtermValueDao();
		} else if (clazz.isAssignableFrom(T01Object.class)) {
			dao = getT01ObjectDao();
		} else if (clazz.isAssignableFrom(ObjectDataQuality.class)) {
			dao = new GenericHibernateDao<ObjectDataQuality>(_sessionFactory, ObjectDataQuality.class);
		} else if (clazz.isAssignableFrom(ObjectFormatInspire.class)) {
			dao = new GenericHibernateDao<ObjectFormatInspire>(_sessionFactory, ObjectFormatInspire.class);
		} else if (clazz.isAssignableFrom(AdditionalFieldData.class)) {
			dao = new GenericHibernateDao<AdditionalFieldData>(_sessionFactory, AdditionalFieldData.class);
		} else if (clazz.isAssignableFrom(SpatialSystem.class)) {
			dao = new GenericHibernateDao<SpatialSystem>(_sessionFactory, SpatialSystem.class);
		} else if (clazz.isAssignableFrom(ObjectUse.class)) {
			dao = new GenericHibernateDao<ObjectUse>(_sessionFactory, ObjectUse.class);
		} else if (clazz.isAssignableFrom(ObjectTypesCatalogue.class)) {
			dao = new GenericHibernateDao<ObjectTypesCatalogue>(_sessionFactory, ObjectTypesCatalogue.class);
		} else if (clazz.isAssignableFrom(ObjectOpenDataCategory.class)) {
			dao = new GenericHibernateDao<ObjectOpenDataCategory>(_sessionFactory, ObjectOpenDataCategory.class);
        } else if (clazz.isAssignableFrom(ObjectUseConstraint.class)) {
            dao = new GenericHibernateDao<ObjectUseConstraint>(_sessionFactory, ObjectUseConstraint.class);
        } else if (clazz.isAssignableFrom(ObjectAdvProductGroup.class)) {
            dao = new GenericHibernateDao<ObjectAdvProductGroup>(_sessionFactory, ObjectAdvProductGroup.class);
        } else if (clazz.isAssignableFrom(ObjectDataLanguage.class)) {
            dao = new GenericHibernateDao<ObjectDataLanguage>(_sessionFactory, ObjectDataLanguage.class);
		} else {
			throw new IllegalArgumentException("Unsupported class: " + clazz.getName());
		}

        return dao;
    }

}
