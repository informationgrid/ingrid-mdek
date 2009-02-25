/*
 * Created on 10.07.2007
 */
package de.ingrid.mdek.services.persistence.db;

import org.hibernate.SessionFactory;

import de.ingrid.mdek.services.persistence.db.dao.IAddressNodeDao;
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
import de.ingrid.mdek.services.persistence.db.dao.ISysGuiDao;
import de.ingrid.mdek.services.persistence.db.dao.ISysJobInfoDao;
import de.ingrid.mdek.services.persistence.db.dao.ISysListDao;
import de.ingrid.mdek.services.persistence.db.dao.IT01ObjectDao;
import de.ingrid.mdek.services.persistence.db.dao.IT02AddressDao;
import de.ingrid.mdek.services.persistence.db.dao.IT08AttrTypeDao;
import de.ingrid.mdek.services.persistence.db.dao.hibernate.AddressNodeDaoHibernate;
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
import de.ingrid.mdek.services.persistence.db.dao.hibernate.SysGuiDaoHibernate;
import de.ingrid.mdek.services.persistence.db.dao.hibernate.SysJobInfoDaoHibernate;
import de.ingrid.mdek.services.persistence.db.dao.hibernate.SysListDaoHibernate;
import de.ingrid.mdek.services.persistence.db.dao.hibernate.T01ObjectDaoHibernate;
import de.ingrid.mdek.services.persistence.db.dao.hibernate.T02AddressDaoHibernate;
import de.ingrid.mdek.services.persistence.db.dao.hibernate.T08AttrTypeDaoHibernate;
import de.ingrid.mdek.services.persistence.db.model.AddressComment;
import de.ingrid.mdek.services.persistence.db.model.AddressMetadata;
import de.ingrid.mdek.services.persistence.db.model.FullIndexAddr;
import de.ingrid.mdek.services.persistence.db.model.FullIndexObj;
import de.ingrid.mdek.services.persistence.db.model.IdcGroup;
import de.ingrid.mdek.services.persistence.db.model.IdcUserPermission;
import de.ingrid.mdek.services.persistence.db.model.ObjectComment;
import de.ingrid.mdek.services.persistence.db.model.ObjectMetadata;
import de.ingrid.mdek.services.persistence.db.model.ObjectReference;
import de.ingrid.mdek.services.persistence.db.model.Permission;
import de.ingrid.mdek.services.persistence.db.model.PermissionAddr;
import de.ingrid.mdek.services.persistence.db.model.PermissionObj;
import de.ingrid.mdek.services.persistence.db.model.SearchtermAdr;
import de.ingrid.mdek.services.persistence.db.model.SearchtermObj;
import de.ingrid.mdek.services.persistence.db.model.SpatialReference;
import de.ingrid.mdek.services.persistence.db.model.T0110AvailFormat;
import de.ingrid.mdek.services.persistence.db.model.T0112MediaOption;
import de.ingrid.mdek.services.persistence.db.model.T0113DatasetReference;
import de.ingrid.mdek.services.persistence.db.model.T0114EnvCategory;
import de.ingrid.mdek.services.persistence.db.model.T0114EnvTopic;
import de.ingrid.mdek.services.persistence.db.model.T011ObjData;
import de.ingrid.mdek.services.persistence.db.model.T011ObjDataPara;
import de.ingrid.mdek.services.persistence.db.model.T011ObjGeo;
import de.ingrid.mdek.services.persistence.db.model.T011ObjGeoKeyc;
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
import de.ingrid.mdek.services.persistence.db.model.T011ObjServVersion;
import de.ingrid.mdek.services.persistence.db.model.T011ObjTopicCat;
import de.ingrid.mdek.services.persistence.db.model.T012ObjAdr;
import de.ingrid.mdek.services.persistence.db.model.T014InfoImpart;
import de.ingrid.mdek.services.persistence.db.model.T015Legist;
import de.ingrid.mdek.services.persistence.db.model.T017UrlRef;
import de.ingrid.mdek.services.persistence.db.model.T021Communication;
import de.ingrid.mdek.services.persistence.db.model.T03Catalogue;

public class DaoFactory implements IDaoFactory {

    private final SessionFactory _sessionFactory;

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

    public ISysGuiDao getSysGuiDao() {
        return new SysGuiDaoHibernate(_sessionFactory);
    }

    public ISysGenericKeyDao getSysGenericKeyDao() {
        return new SysGenericKeyDaoHibernate(_sessionFactory);
    }

    public IT08AttrTypeDao getT08AttrTypeDao() {
        return new T08AttrTypeDaoHibernate(_sessionFactory);
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
    
    public IGenericDao<IEntity> getDao(Class clazz) {
		IGenericDao dao = null;

		if (clazz.isAssignableFrom(IEntity.class)) {
			// Generic dao for class unspecific operations !!!
			dao = new GenericHibernateDao<SpatialReference>(_sessionFactory, IEntity.class);			
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
		} else if (clazz.isAssignableFrom(T0114EnvCategory.class)) {
			dao = new GenericHibernateDao<T0114EnvCategory>(_sessionFactory, T0114EnvCategory.class);
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
		} else if (clazz.isAssignableFrom(T011ObjGeoKeyc.class)) {
			dao = new GenericHibernateDao<T011ObjGeoKeyc>(_sessionFactory, T011ObjGeoKeyc.class);
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
			dao = new GenericHibernateDao<IdcGroup>(_sessionFactory, IdcGroup.class);
		} else if (clazz.isAssignableFrom(Permission.class)) {
			dao = new GenericHibernateDao<Permission>(_sessionFactory, Permission.class);
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
		} else {
			throw new IllegalArgumentException("Unsupported class: " + clazz.getName());
		}

        return dao;
    }

}
