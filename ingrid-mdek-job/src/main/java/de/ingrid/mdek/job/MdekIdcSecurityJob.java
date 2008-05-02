package de.ingrid.mdek.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekKeysSecurity;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.job.tools.MdekPermissionHandler;
import de.ingrid.mdek.services.log.ILogService;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.IEntity;
import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.dao.IAddressNodeDao;
import de.ingrid.mdek.services.persistence.db.dao.IIdcGroupDao;
import de.ingrid.mdek.services.persistence.db.dao.IIdcUserDao;
import de.ingrid.mdek.services.persistence.db.mapper.BeanToDocMapperSecurity;
import de.ingrid.mdek.services.persistence.db.mapper.DocToBeanMapperSecurity;
import de.ingrid.mdek.services.persistence.db.mapper.IMapper.MappingQuantity;
import de.ingrid.mdek.services.persistence.db.model.AddressNode;
import de.ingrid.mdek.services.persistence.db.model.IdcGroup;
import de.ingrid.mdek.services.persistence.db.model.IdcUser;
import de.ingrid.mdek.services.persistence.db.model.Permission;
import de.ingrid.mdek.services.persistence.db.model.T02Address;
import de.ingrid.mdek.services.security.IPermissionService;
import de.ingrid.utils.IngridDocument;

/**
 * Encapsulates all Job functionality concerning SECURITY / USER MANAGEMENT.
 
 */
public class MdekIdcSecurityJob extends MdekIdcJob {

	/** service encapsulating security functionality */
	private IPermissionService permissionService;
	private MdekPermissionHandler permissionHandler;

	private IIdcGroupDao daoIdcGroup;
	private IIdcUserDao daoIdcUser;
	private IAddressNodeDao daoAddressNode;

	/** generic dao: ENTITY UNSPECIFIC for transaction ops ... */
	private IGenericDao<IEntity> dao;

	protected BeanToDocMapperSecurity beanToDocMapperSecurity;
	protected DocToBeanMapperSecurity docToBeanMapperSecurity;

	public MdekIdcSecurityJob(ILogService logService,
			DaoFactory daoFactory,
			IPermissionService permissionService) {
		super(logService.getLogger(MdekIdcSecurityJob.class), daoFactory);
		
		this.permissionService = permissionService;
		permissionHandler = MdekPermissionHandler.getInstance(permissionService);
		
		dao = daoFactory.getDao(IEntity.class);

		daoIdcGroup = daoFactory.getIdcGroupDao();
		daoIdcUser = daoFactory.getIdcUserDao();
		daoAddressNode = daoFactory.getAddressNodeDao();

		beanToDocMapperSecurity = BeanToDocMapperSecurity.getInstance(daoFactory, permissionService);
		docToBeanMapperSecurity = DocToBeanMapperSecurity.getInstance(daoFactory, permissionService);
	}

	public IngridDocument getGroups(IngridDocument params) {
		try {
			daoIdcGroup.beginTransaction();

			List<IdcGroup> groups = daoIdcGroup.getGroups();

			ArrayList<IngridDocument> resultList = new ArrayList<IngridDocument>(groups.size());
			for (IdcGroup group : groups) {
				IngridDocument groupDoc = new IngridDocument();
				beanToDocMapperSecurity.mapIdcGroup(group, groupDoc, MappingQuantity.BASIC_ENTITY);
				resultList.add(groupDoc);
			}

			IngridDocument result = new IngridDocument();
			result.put(MdekKeysSecurity.GROUPS, resultList);

			daoIdcGroup.commitTransaction();
			return result;

		} catch (RuntimeException e) {
			daoIdcGroup.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
		    throw handledExc;
		}
	}

	public IngridDocument getGroupDetails(IngridDocument params) {
		try {
			daoIdcGroup.beginTransaction();

			String name = params.getString(MdekKeys.NAME);
			IngridDocument result = getGroupDetails(name);
			
			daoIdcGroup.commitTransaction();
			return result;
			
		} catch (RuntimeException e) {
			daoIdcGroup.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
		    throw handledExc;
		}
	}

	private IngridDocument getGroupDetails(String name) {
		IdcGroup group = daoIdcGroup.getGroupDetails(name);
		if (group == null) {
			throw new MdekException(new MdekError(MdekErrorType.ENTITY_NOT_FOUND));
		}

		IngridDocument resultDoc = new IngridDocument();
		beanToDocMapperSecurity.mapIdcGroup(group, resultDoc, MappingQuantity.DETAIL_ENTITY);
		
		return resultDoc;
	}

	public IngridDocument createGroup(IngridDocument gDocIn) {
		String userId = getCurrentUserUuid(gDocIn);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JOB_DESCR_STORE, 0, 1, false));

			daoIdcGroup.beginTransaction();
			String currentTime = MdekUtils.dateToTimestamp(new Date());

			String name = gDocIn.getString(MdekKeys.NAME);
			Boolean refetchAfterStore = (Boolean) gDocIn.get(MdekKeys.REQUESTINFO_REFETCH_ENTITY);

			// set common data to transfer !
			gDocIn.put(MdekKeys.DATE_OF_CREATION, currentTime);
			gDocIn.put(MdekKeys.DATE_OF_LAST_MODIFICATION, currentTime);
			beanToDocMapper.mapModUser(userId, gDocIn, MappingQuantity.INITIAL_ENTITY);
			
			// exception if group already exists
			if (daoIdcGroup.loadByName(name) != null) {
				throw new MdekException(new MdekError(MdekErrorType.ENTITY_ALREADY_EXISTS));
			}
			
			IdcGroup newGrp = docToBeanMapperSecurity.mapIdcGroup(gDocIn, new IdcGroup(), MappingQuantity.DETAIL_ENTITY);
			 // save it, generates id
			daoIdcGroup.makePersistent(newGrp);
			
			// COMMIT BEFORE REFETCHING !!! otherwise we get old data ???
			daoIdcGroup.commitTransaction();

			// return basic data
			IngridDocument result = new IngridDocument();
			result.put(MdekKeysSecurity.IDC_GROUP_ID, newGrp.getId());

			if (refetchAfterStore) {
				daoIdcGroup.beginTransaction();
				result = getGroupDetails(name);
				daoIdcGroup.commitTransaction();
			}
			
			return result;

		} catch (RuntimeException e) {
			daoIdcGroup.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
			removeRunningJob = errorHandler.shouldRemoveRunningJob(handledExc);
		    throw handledExc;
		} finally {
			if (removeRunningJob) {
				removeRunningJob(userId);				
			}
		}
	}

	public IngridDocument storeGroup(IngridDocument gDocIn) {
		String userId = getCurrentUserUuid(gDocIn);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JOB_DESCR_STORE, 0, 1, false));

			daoIdcGroup.beginTransaction();
			String currentTime = MdekUtils.dateToTimestamp(new Date());

			Long grpId = (Long) gDocIn.get(MdekKeysSecurity.IDC_GROUP_ID);
			Boolean refetchAfterStore = (Boolean) gDocIn.get(MdekKeys.REQUESTINFO_REFETCH_ENTITY);

			// set common data to transfer !
			gDocIn.put(MdekKeys.DATE_OF_LAST_MODIFICATION, currentTime);
			beanToDocMapper.mapModUser(userId, gDocIn, MappingQuantity.INITIAL_ENTITY);
			
			// exception if group not existing
			IdcGroup grp = daoIdcGroup.getGroupDetails(grpId);
			if (grp == null) {
				throw new MdekException(new MdekError(MdekErrorType.ENTITY_NOT_FOUND));
			}
			
			docToBeanMapperSecurity.mapIdcGroup(gDocIn, grp, MappingQuantity.DETAIL_ENTITY);
			daoIdcGroup.makePersistent(grp);
			
			// COMMIT BEFORE REFETCHING !!! otherwise we get old data ???
			daoIdcGroup.commitTransaction();

			// return basic data
			IngridDocument result = new IngridDocument();
			result.put(MdekKeysSecurity.IDC_GROUP_ID, grp.getId());

			if (refetchAfterStore) {
				daoIdcGroup.beginTransaction();
				result = getGroupDetails(grp.getName());
				daoIdcGroup.commitTransaction();
			}
			
			return result;

		} catch (RuntimeException e) {
			daoIdcGroup.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
			removeRunningJob = errorHandler.shouldRemoveRunningJob(handledExc);
		    throw handledExc;
		} finally {
			if (removeRunningJob) {
				removeRunningJob(userId);				
			}
		}
	}
	
	public void deleteGroup(IngridDocument docIn) {
		String userId = getCurrentUserUuid(docIn);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JOB_DESCR_DELETE, 0, 1, false));

			daoIdcGroup.beginTransaction();
			Long grpId = (Long) docIn.get(MdekKeysSecurity.IDC_GROUP_ID);
			
			// exception if group not existing
			IdcGroup group = daoIdcGroup.getById(grpId);
			if (group == null) {
				throw new MdekException(new MdekError(MdekErrorType.ENTITY_NOT_FOUND));
			}
			// check for attached permissions
			if (group.getPermissionAddrs().size() > 0 || group.getPermissionObjs().size() > 0) {
				throw new MdekException(new MdekError(MdekErrorType.GROUP_HAS_PERMISSIONS));
			}
			// check for attached users
			List<IdcUser> connectedUsers = daoIdcUser.getIdcUsersByGroupId(group.getId());
			if (connectedUsers.size() > 0) {
				throw new MdekException(new MdekError(MdekErrorType.GROUP_HAS_USERS));
			}
			
			daoIdcGroup.makeTransient(group);
			daoIdcGroup.commitTransaction();

		} catch (RuntimeException e) {
			daoIdcGroup.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
			removeRunningJob = errorHandler.shouldRemoveRunningJob(handledExc);
		    throw handledExc;
		} finally {
			if (removeRunningJob) {
				removeRunningJob(userId);				
			}
		}
	}	

	public IngridDocument getAddressPermissions(IngridDocument params) {
		try {
			dao.beginTransaction();

			String addrUuid = params.getString(MdekKeys.UUID);
			String userAddrUuid = getCurrentUserUuid(params);

			List<Permission> perms = permissionHandler.getPermissionsForAddress(addrUuid, userAddrUuid);

			IngridDocument resultDoc = new IngridDocument();
			beanToDocMapperSecurity.mapPermissionList(perms, resultDoc);

			dao.commitTransaction();
			return resultDoc;
			
		} catch (RuntimeException e) {
			dao.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
		    throw handledExc;
		}
	}

	public IngridDocument getObjectPermissions(IngridDocument params) {
		try {
			dao.beginTransaction();

			String objUuid = params.getString(MdekKeys.UUID);
			String userAddrUuid = getCurrentUserUuid(params);

			List<Permission> perms = permissionHandler.getPermissionsForObject(objUuid, userAddrUuid);

			IngridDocument resultDoc = new IngridDocument();
			beanToDocMapperSecurity.mapPermissionList(perms, resultDoc);

			dao.commitTransaction();
			return resultDoc;
			
		} catch (RuntimeException e) {
			dao.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
		    throw handledExc;
		}
	}

	public IngridDocument getUserPermissions(IngridDocument params) {
		try {
			dao.beginTransaction();

			String userAddrUuid = getCurrentUserUuid(params);

			List<Permission> perms = permissionHandler.getUserPermissions(userAddrUuid);

			IngridDocument resultDoc = new IngridDocument();
			beanToDocMapperSecurity.mapPermissionList(perms, resultDoc);

			dao.commitTransaction();
			return resultDoc;
			
		} catch (RuntimeException e) {
			dao.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
		    throw handledExc;
		}
	}

	public IngridDocument getUserDetails(IngridDocument params) {
		try {
			daoIdcUser.beginTransaction();

			String addrUuid = params.getString(MdekKeysSecurity.IDC_USER_ADDR_UUID);
			IngridDocument result = getUserDetails(addrUuid);
			
			daoIdcUser.commitTransaction();
			return result;
			
		} catch (RuntimeException e) {
			daoIdcUser.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
		    throw handledExc;
		}
	}
	
	
	private IngridDocument getUserDetails(String addrUuid) {
		IdcUser user = daoIdcUser.getIdcUserByAddrUuid(addrUuid);
		if (user == null) {
			throw new MdekException(new MdekError(MdekErrorType.ENTITY_NOT_FOUND));
		}
		
		AddressNode addressNode = daoAddressNode.loadByUuid(addrUuid);
		T02Address address = null;
		if (addressNode == null) {
			throw new MdekException(new MdekError(MdekErrorType.ENTITY_NOT_FOUND));
		} else {
			address = addressNode.getT02AddressWork();
		}

		IngridDocument resultDoc = new IngridDocument();
		beanToDocMapperSecurity.mapIdcUser(user, resultDoc, MappingQuantity.DETAIL_ENTITY);
		
		return resultDoc;
	}
	
	public IngridDocument createUser(IngridDocument uDocIn) {
		String userId = getCurrentUserUuid(uDocIn);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JOB_DESCR_STORE, 0, 1, false));

			daoIdcUser.beginTransaction();
			String currentTime = MdekUtils.dateToTimestamp(new Date());

			String addrUuid = uDocIn.getString(MdekKeysSecurity.IDC_USER_ADDR_UUID);
			Boolean refetchAfterStore = (Boolean) uDocIn.get(MdekKeys.REQUESTINFO_REFETCH_ENTITY);

			// set common data to transfer !
			uDocIn.put(MdekKeys.DATE_OF_CREATION, currentTime);
			uDocIn.put(MdekKeys.DATE_OF_LAST_MODIFICATION, currentTime);
			beanToDocMapper.mapModUser(userId, uDocIn, MappingQuantity.INITIAL_ENTITY);
			
			// exception if user already exists
			if (daoIdcUser.getIdcUserByAddrUuid(addrUuid) != null) {
				throw new MdekException(new MdekError(MdekErrorType.ENTITY_ALREADY_EXISTS));
			}
			
			IdcUser newUser = docToBeanMapperSecurity.mapIdcUser(uDocIn, new IdcUser());
			 // save it, generates id
			// TODO: check whether first store with BASIC DATA to genrate id for detailed mapping
			daoIdcUser.makePersistent(newUser);
			
			// COMMIT BEFORE REFETCHING !!! otherwise we get old data ???
			daoIdcUser.commitTransaction();

			// return basic data
			IngridDocument result = new IngridDocument();
			result.put(MdekKeysSecurity.IDC_USER_ID, newUser.getId());

			if (refetchAfterStore) {
				daoIdcUser.beginTransaction();
				result = getUserDetails(addrUuid);
				daoIdcUser.commitTransaction();
			}
			
			return result;

		} catch (RuntimeException e) {
			daoIdcUser.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
			removeRunningJob = errorHandler.shouldRemoveRunningJob(handledExc);
		    throw handledExc;
		} finally {
			if (removeRunningJob) {
				removeRunningJob(userId);				
			}
		}		
	}

	public IngridDocument storeUser(IngridDocument uDocIn) {
		String userId = getCurrentUserUuid(uDocIn);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JOB_DESCR_STORE, 0, 1, false));

			daoIdcUser.beginTransaction();
			String currentTime = MdekUtils.dateToTimestamp(new Date());

			Long usrId = (Long) uDocIn.get(MdekKeysSecurity.IDC_USER_ID);
			Boolean refetchAfterStore = (Boolean) uDocIn.get(MdekKeys.REQUESTINFO_REFETCH_ENTITY);

			// set common data to transfer !
			uDocIn.put(MdekKeys.DATE_OF_LAST_MODIFICATION, currentTime);
			beanToDocMapper.mapModUser(userId, uDocIn, MappingQuantity.INITIAL_ENTITY);
			
			// exception if group not existing
			IdcUser user = daoIdcUser.getById(usrId);
			if (user == null) {
				throw new MdekException(new MdekError(MdekErrorType.ENTITY_NOT_FOUND));
			}
			
			docToBeanMapperSecurity.mapIdcUser(uDocIn, user);
			daoIdcUser.makePersistent(user);
			
			// COMMIT BEFORE REFETCHING !!! otherwise we get old data ???
			daoIdcUser.commitTransaction();

			// return basic data
			IngridDocument result = new IngridDocument();
			result.put(MdekKeysSecurity.IDC_USER_ID, user.getId());

			if (refetchAfterStore) {
				daoIdcUser.beginTransaction();
				result = getUserDetails(user.getAddrUuid());
				daoIdcUser.commitTransaction();
			}
			
			return result;

		} catch (RuntimeException e) {
			daoIdcUser.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
			removeRunningJob = errorHandler.shouldRemoveRunningJob(handledExc);
		    throw handledExc;
		} finally {
			if (removeRunningJob) {
				removeRunningJob(userId);				
			}
		}
	}

	public void deleteUser(IngridDocument uDocIn) {
		String userId = getCurrentUserUuid(uDocIn);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JOB_DESCR_DELETE, 0, 1, false));

			daoIdcUser.beginTransaction();
			Long usrId = (Long) uDocIn.get(MdekKeysSecurity.IDC_USER_ID);
			
			// exception if group not existing
			IdcUser user = daoIdcUser.getById(usrId);
			if (user == null) {
				throw new MdekException(new MdekError(MdekErrorType.ENTITY_NOT_FOUND));
			}
			daoIdcUser.makeTransient(user);
			daoIdcUser.commitTransaction();

		} catch (RuntimeException e) {
			daoIdcUser.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
			removeRunningJob = errorHandler.shouldRemoveRunningJob(handledExc);
		    throw handledExc;
		} finally {
			if (removeRunningJob) {
				removeRunningJob(userId);				
			}
		}
	}	
	
	public IngridDocument getCatalogAdmin(IngridDocument uDocIn) {
		try {
			daoIdcUser.beginTransaction();

			IdcUser user = permissionService.getCatalogAdmin();
			if (user == null) {
				throw new MdekException(new MdekError(MdekErrorType.ENTITY_NOT_FOUND));
			}
			
			// map before committing, uses lazy loading
			IngridDocument result = new IngridDocument();
			beanToDocMapperSecurity.mapIdcUser(user, result, MappingQuantity.DETAIL_ENTITY);
			
			daoIdcUser.commitTransaction();

			return result;

		} catch (RuntimeException e) {
			daoIdcUser.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
		    throw handledExc;
		}
	}	

	public IngridDocument getSubUsers(IngridDocument params) {
		try {
			daoIdcUser.beginTransaction();
			Long usrId = (Long) params.get(MdekKeysSecurity.IDC_USER_ID);

			List<IdcUser> subUsers = daoIdcUser.getSubUsers(usrId);

			ArrayList<IngridDocument> resultList = new ArrayList<IngridDocument>(subUsers.size());
			for (IdcUser subUser : subUsers) {
				IngridDocument uDoc = new IngridDocument();
				beanToDocMapperSecurity.mapIdcUser(subUser, uDoc, MappingQuantity.TREE_ENTITY);
				resultList.add(uDoc);
			}

			IngridDocument result = new IngridDocument();
			result.put(MdekKeysSecurity.IDC_USERS, resultList);

			daoIdcUser.commitTransaction();
			return result;

		} catch (RuntimeException e) {
			daoIdcUser.rollbackTransaction();
			RuntimeException handledExc = errorHandler.handleException(e);
		    throw handledExc;
		}
	}

}
