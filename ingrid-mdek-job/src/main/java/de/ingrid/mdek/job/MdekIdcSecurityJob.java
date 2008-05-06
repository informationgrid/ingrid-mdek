package de.ingrid.mdek.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekKeysSecurity;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.MdekUtils.WorkState;
import de.ingrid.mdek.job.tools.MdekPermissionHandler;
import de.ingrid.mdek.services.log.ILogService;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.IEntity;
import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.dao.IAddressNodeDao;
import de.ingrid.mdek.services.persistence.db.dao.IIdcGroupDao;
import de.ingrid.mdek.services.persistence.db.dao.IIdcUserDao;
import de.ingrid.mdek.services.persistence.db.dao.IObjectNodeDao;
import de.ingrid.mdek.services.persistence.db.mapper.BeanToDocMapperSecurity;
import de.ingrid.mdek.services.persistence.db.mapper.DocToBeanMapperSecurity;
import de.ingrid.mdek.services.persistence.db.mapper.IMapper.MappingQuantity;
import de.ingrid.mdek.services.persistence.db.model.AddressNode;
import de.ingrid.mdek.services.persistence.db.model.IdcGroup;
import de.ingrid.mdek.services.persistence.db.model.IdcUser;
import de.ingrid.mdek.services.persistence.db.model.Permission;
import de.ingrid.mdek.services.persistence.db.model.PermissionAddr;
import de.ingrid.mdek.services.persistence.db.model.PermissionObj;
import de.ingrid.mdek.services.persistence.db.model.T02Address;
import de.ingrid.mdek.services.security.IPermissionService;
import de.ingrid.mdek.services.security.PermissionFactory;
import de.ingrid.utils.IngridDocument;

/**
 * Encapsulates all Job functionality concerning SECURITY / USER MANAGEMENT.
 
 */
public class MdekIdcSecurityJob extends MdekIdcJob {

	/** service encapsulating security functionality */
	private IPermissionService permService;
	private MdekPermissionHandler permHandler;

	private IIdcGroupDao daoIdcGroup;
	private IIdcUserDao daoIdcUser;
	private IAddressNodeDao daoAddressNode;
	private IObjectNodeDao daoObjectNode;

	/** generic dao: ENTITY UNSPECIFIC for transaction ops ... */
	private IGenericDao<IEntity> dao;

	protected BeanToDocMapperSecurity beanToDocMapperSecurity;
	protected DocToBeanMapperSecurity docToBeanMapperSecurity;

	public MdekIdcSecurityJob(ILogService logService,
			DaoFactory daoFactory,
			IPermissionService permissionService) {
		super(logService.getLogger(MdekIdcSecurityJob.class), daoFactory);
		
		this.permService = permissionService;
		permHandler = MdekPermissionHandler.getInstance(permissionService);
		
		dao = daoFactory.getDao(IEntity.class);

		daoIdcGroup = daoFactory.getIdcGroupDao();
		daoIdcUser = daoFactory.getIdcUserDao();
		daoAddressNode = daoFactory.getAddressNodeDao();
		daoObjectNode = daoFactory.getObjectNodeDao();

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

			// perform checks, throws exception if not ok !
			checkPermissionsOfGroup(newGrp);

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
			
			// transfer new data AND MAKE PERSISTENT, so oncoming checks have newest data !
			docToBeanMapperSecurity.mapIdcGroup(gDocIn, grp, MappingQuantity.DETAIL_ENTITY);
			daoIdcGroup.makePersistent(grp);

			// perform checks, throws exception if not ok !

			// check for nested permissions
			checkPermissionsOfGroup(grp);
			// check for users editing entities with removed permissions
			checkUsersOfGroup(grp);

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
	
	public IngridDocument deleteGroup(IngridDocument docIn) {
		String userId = getCurrentUserUuid(docIn);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JOB_DESCR_DELETE, 0, 1, false));

			daoIdcGroup.beginTransaction();

			Long grpId = (Long) docIn.get(MdekKeysSecurity.IDC_GROUP_ID);
			IdcGroup group = daoIdcGroup.getById(grpId);
			if (group == null) {
				throw new MdekException(new MdekError(MdekErrorType.ENTITY_NOT_FOUND));
			}
// GROUP CAN BE DELETED WITH USERS AND PERMISSIONS ! further checks, see below !
/*
			// check for attached permissions
			if (grp.getPermissionAddrs().size() > 0 || grp.getPermissionObjs().size() > 0) {
				throw new MdekException(new MdekError(MdekErrorType.GROUP_HAS_PERMISSIONS));
			}
			// check for attached users
			List<IdcUser> connectedUsers = daoIdcUser.getIdcUsersByGroupId(grp.getId());
			if (connectedUsers.size() > 0) {
				throw new MdekException(new MdekError(MdekErrorType.GROUP_HAS_USERS));
			}
*/
			// first remove ALL permissions AND MAKE PERSISTENT, so oncoming checks work (checks read from database) !
			IngridDocument noPermissionsDoc = new IngridDocument();
			docToBeanMapperSecurity.updateIdcUserPermissions(noPermissionsDoc, group);
			docToBeanMapperSecurity.updatePermissionAddrs(noPermissionsDoc, group);
			docToBeanMapperSecurity.updatePermissionObjs(noPermissionsDoc, group);
			daoIdcGroup.makePersistent(group);

			// check for users editing entities and still needing permissions, throws exception if so !
			checkUsersOfGroup(group);
			
			// ok, we update ex group users (remove from group, ...) and return them
			List<IdcUser> users = daoIdcUser.getIdcUsersByGroupId(group.getId());
			clearGroupOnUsers(users, userId);
			IngridDocument result = new IngridDocument();
			beanToDocMapperSecurity.mapIdcUserList(users, result, true);

			// all ok, now we delete and COMMIT !
			daoIdcGroup.makeTransient(group);
			daoIdcGroup.commitTransaction();
			
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

	public IngridDocument getAddressPermissions(IngridDocument params) {
		try {
			dao.beginTransaction();

			String addrUuid = params.getString(MdekKeys.UUID);
			String userAddrUuid = getCurrentUserUuid(params);

			List<Permission> perms = permHandler.getPermissionsForAddress(addrUuid, userAddrUuid);

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

			List<Permission> perms = permHandler.getPermissionsForObject(objUuid, userAddrUuid);

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

			List<Permission> perms = permHandler.getUserPermissions(userAddrUuid);

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

			IdcUser user = permService.getCatalogAdmin();
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

	/**
	 * Validate whether the assigned permissions of the given group are ok or whether there are
	 * conflicts (e.g. "write" underneath "write-tree"). THROWS EXCEPTION IF A CONFLICT OCCURS
	 * WITH DETAILED DATA ABOUT CONFLICT.
	 * @param grp group to validate
	 */
	private void checkPermissionsOfGroup(IdcGroup grp) {
		checkObjectPermissionsOfGroup(grp);
		checkAddressPermissionsOfGroup(grp);
	}

	/**
	 * Validate whether the assigned OBJECT permissions of the given group are ok or whether there are
	 * conflicts (e.g. "write" underneath "write-tree"). THROWS EXCEPTION IF A CONFLICT OCCURS
	 * WITH DETAILED DATA ABOUT CONFLICT.
	 * @param grp group to validate
	 */
	private void checkObjectPermissionsOfGroup(IdcGroup grp) {
		Set<PermissionObj> pos = grp.getPermissionObjs();

		// permission templates for checking
		Permission pTemplateSingle = PermissionFactory.getPermissionTemplateSingle();
		Permission pTemplateTree = PermissionFactory.getPermissionTemplateTree();

		// separate uuids by permission types
		ArrayList<String> uuidsSingle = new ArrayList<String>();
		ArrayList<String> uuidsTree = new ArrayList<String>();
		for (PermissionObj po : pos) {
			Permission p = po.getPermission();
			String oUuid = po.getUuid();

			// check whether "double" permissions !
			if (uuidsSingle.contains(oUuid) || uuidsTree.contains(oUuid)) {
				IngridDocument errInfo = setupErrorInfoObj(new IngridDocument(), oUuid);
				throw new MdekException(new MdekError(MdekErrorType.MULTIPLE_PERMISSIONS_ON_OBJECT, errInfo));
			}

			// separate uuids by permission type
			if (permService.isEqualPermissions(p, pTemplateSingle)) {
				uuidsSingle.add(oUuid);
			} else if (permService.isEqualPermissions(p, pTemplateTree)) {
				uuidsTree.add(oUuid);
			}
		}
		
		// check nested tree permissions
		for (int i=0; i < uuidsTree.size(); i++) {
			String uuid1 = uuidsTree.get(i);
			List<String> path1 = daoObjectNode.getObjectPath(uuid1);
			for (int j=0; j < uuidsTree.size(); j++) {
				if (j==i) {
					continue;
				}
				String uuid2 = uuidsTree.get(j);
				List<String> path2 = daoObjectNode.getObjectPath(uuid2);
				// order of uuids determines parent/child !
				if (path1.contains(uuid2)) {
					IngridDocument errInfo = setupErrorInfoObj(new IngridDocument(), uuid2);
					errInfo = setupErrorInfoObj(errInfo, uuid1);
					throw new MdekException(new MdekError(MdekErrorType.TREE_BELOW_TREE_OBJECT_PERMISSION, errInfo));					
				} else if  (path2.contains(uuid1)) {
					IngridDocument errInfo = setupErrorInfoObj(new IngridDocument(), uuid1);
					errInfo = setupErrorInfoObj(errInfo, uuid2);
					throw new MdekException(new MdekError(MdekErrorType.TREE_BELOW_TREE_OBJECT_PERMISSION, errInfo));					
					
				}
			}
		}

		// check single permission beneath tree permission
		for (int i=0; i < uuidsSingle.size(); i++) {
			String uuidSingle = uuidsSingle.get(i);
			List<String> pathSingle = daoObjectNode.getObjectPath(uuidSingle);
			for (int j=0; j < uuidsTree.size(); j++) {
				String uuidTree = uuidsTree.get(j);
				// order of uuids determines parent/child !
				if (pathSingle.contains(uuidTree)) {
					IngridDocument errInfo = setupErrorInfoObj(new IngridDocument(), uuidTree);
					errInfo = setupErrorInfoObj(errInfo, uuidSingle);
					throw new MdekException(new MdekError(MdekErrorType.SINGLE_BELOW_TREE_OBJECT_PERMISSION, errInfo));					
				}
			}
		}
	}

	/**
	 * Validate whether the assigned ADDRESS permissions of the given group are ok or whether there are
	 * conflicts (e.g. "write" underneath "write-tree"). THROWS EXCEPTION IF A CONFLICT OCCURS
	 * WITH DETAILED DATA ABOUT CONFLICT.
	 * @param grp group to validate
	 */
	private void checkAddressPermissionsOfGroup(IdcGroup grp) {
		Set<PermissionAddr> pas = grp.getPermissionAddrs();

		// permission templates for checking
		Permission pTemplateSingle = PermissionFactory.getPermissionTemplateSingle();
		Permission pTemplateTree = PermissionFactory.getPermissionTemplateTree();

		// separate uuids by permission types
		ArrayList<String> uuidsSingle = new ArrayList<String>();
		ArrayList<String> uuidsTree = new ArrayList<String>();
		for (PermissionAddr pa : pas) {
			Permission p = pa.getPermission();
			String aUuid = pa.getUuid();

			// check whether "double" permissions !
			if (uuidsSingle.contains(aUuid) || uuidsTree.contains(aUuid)) {
				IngridDocument errInfo = setupErrorInfoAddr(new IngridDocument(), aUuid);
				throw new MdekException(new MdekError(MdekErrorType.MULTIPLE_PERMISSIONS_ON_ADDRESS, errInfo));
			}

			// separate uuids by permission type
			if (permService.isEqualPermissions(p, pTemplateSingle)) {
				uuidsSingle.add(aUuid);
			} else if (permService.isEqualPermissions(p, pTemplateTree)) {
				uuidsTree.add(aUuid);
			}
		}
		
		// check nested tree permissions
		for (int i=0; i < uuidsTree.size(); i++) {
			String uuid1 = uuidsTree.get(i);
			List<String> path1 = daoAddressNode.getAddressPath(uuid1);
			for (int j=0; j < uuidsTree.size(); j++) {
				if (j==i) {
					continue;
				}
				String uuid2 = uuidsTree.get(j);
				List<String> path2 = daoAddressNode.getAddressPath(uuid2);
				// order of uuids determines parent/child !
				if (path1.contains(uuid2)) {
					IngridDocument errInfo = setupErrorInfoAddr(new IngridDocument(), uuid2);
					errInfo = setupErrorInfoAddr(errInfo, uuid1);
					throw new MdekException(new MdekError(MdekErrorType.TREE_BELOW_TREE_ADDRESS_PERMISSION, errInfo));					
				} else if  (path2.contains(uuid1)) {
					IngridDocument errInfo = setupErrorInfoAddr(new IngridDocument(), uuid1);
					errInfo = setupErrorInfoAddr(errInfo, uuid2);
					throw new MdekException(new MdekError(MdekErrorType.TREE_BELOW_TREE_ADDRESS_PERMISSION, errInfo));					
					
				}
			}
		}

		// check single permission beneath tree permission
		for (int i=0; i < uuidsSingle.size(); i++) {
			String uuidSingle = uuidsSingle.get(i);
			List<String> pathSingle = daoAddressNode.getAddressPath(uuidSingle);
			for (int j=0; j < uuidsTree.size(); j++) {
				String uuidTree = uuidsTree.get(j);
				// order of uuids determines parent/child !
				if (pathSingle.contains(uuidTree)) {
					IngridDocument errInfo = setupErrorInfoAddr(new IngridDocument(), uuidTree);
					errInfo = setupErrorInfoAddr(errInfo, uuidSingle);
					throw new MdekException(new MdekError(MdekErrorType.SINGLE_BELOW_TREE_ADDRESS_PERMISSION, errInfo));					
				}
			}
		}
	}

	private IngridDocument setupErrorInfoObj(IngridDocument errInfo, String objUuid) {
		if (errInfo == null) {
			errInfo = new IngridDocument();			
		}
		IngridDocument oDoc = beanToDocMapper.mapT01Object(daoObjectNode.loadByUuid(objUuid).getT01ObjectWork(),
					new IngridDocument(), MappingQuantity.BASIC_ENTITY);
		List<IngridDocument> oList = (List<IngridDocument>) errInfo.get(MdekKeys.OBJ_ENTITIES);
		if (oList == null) {
			oList = new ArrayList<IngridDocument>();
			errInfo.put(MdekKeys.OBJ_ENTITIES, oList);
		}
		oList.add(oDoc);
		
		return errInfo;
	}
	private IngridDocument setupErrorInfoAddr(IngridDocument errInfo, String addrUuid) {
		if (errInfo == null) {
			errInfo = new IngridDocument();			
		}
		IngridDocument aDoc = beanToDocMapper.mapT02Address(daoAddressNode.loadByUuid(addrUuid).getT02AddressWork(),
					new IngridDocument(), MappingQuantity.BASIC_ENTITY);
		List<IngridDocument> aList = (List<IngridDocument>) errInfo.get(MdekKeys.ADR_ENTITIES);
		if (aList == null) {
			aList = new ArrayList<IngridDocument>();
			errInfo.put(MdekKeys.ADR_ENTITIES, aList);
		}
		aList.add(aDoc);
		
		return errInfo;
	}

	private IngridDocument setupErrorInfoUser(IngridDocument errInfo, String userUuid) {
		if (errInfo == null) {
			errInfo = new IngridDocument();			
		}
		IngridDocument aDoc = beanToDocMapper.mapT02Address(daoAddressNode.loadByUuid(userUuid).getT02AddressWork(),
					new IngridDocument(), MappingQuantity.BASIC_ENTITY);
		errInfo.put(MdekKeys.MOD_USER, aDoc);
		
		return errInfo;
	}

	/**
	 * Check whether there are group users with entities they work on and without write permissions
	 * for the entity (because of removed permissions). THROWS EXCEPTION IF NO PERMISSION ANYMORE.
	 * @param grp group to validate
	 */
	private void checkUsersOfGroup(IdcGroup grp) {
		checkMissingUserPermissionsOnObjects(grp);
		checkMissingUserPermissionsOnAddresses(grp);
	}

	/**
	 * Check whether there are group users with OBJECTS they work on and without write permissions
	 * for these OBJECTS (because of removed permissions). THROWS EXCEPTION IF NO PERMISSION ANYMORE.
	 * @param grp group to validate
	 */
	private void checkMissingUserPermissionsOnObjects(IdcGroup grp) {
		List<Map> objUserMaps = 
			daoIdcGroup.getGroupUsersWithObjectsInGivenState(grp.getName(), WorkState.IN_BEARBEITUNG);
		for (Map objUserMap : objUserMaps) {
			String userUuid = (String) objUserMap.get(daoIdcGroup.KEY_USER_UUID);
			String objUuid = (String) objUserMap.get(daoIdcGroup.KEY_ENTITY_UUID);
			
			if (!permHandler.hasWritePermissionForObject(objUuid, userUuid)) {
				IngridDocument errInfo = setupErrorInfoUser(new IngridDocument(), userUuid);
				errInfo = setupErrorInfoObj(errInfo, objUuid);
				throw new MdekException(new MdekError(MdekErrorType.USER_OBJECT_PERMISSION_MISSING, errInfo));
			}
		}
	}

	/**
	 * Check whether there are group users with ADDRESSES they work on and without write permissions
	 * for these ADDRESSES (because of removed permissions). THROWS EXCEPTION IF NO PERMISSION ANYMORE.
	 * @param grp group to validate
	 */
	private void checkMissingUserPermissionsOnAddresses(IdcGroup grp) {
		List<Map> addrUserMaps = 
			daoIdcGroup.getGroupUsersWithAddressesInGivenState(grp.getName(), WorkState.IN_BEARBEITUNG);
		for (Map addrUserMap : addrUserMaps) {
			String userUuid = (String) addrUserMap.get(daoIdcGroup.KEY_USER_UUID);
			String addrUuid = (String) addrUserMap.get(daoIdcGroup.KEY_ENTITY_UUID);
			
			if (!permHandler.hasWritePermissionForAddress(addrUuid, userUuid)) {
				IngridDocument errInfo = setupErrorInfoUser(new IngridDocument(), userUuid);
				errInfo = setupErrorInfoAddr(errInfo, addrUuid);
				throw new MdekException(new MdekError(MdekErrorType.USER_ADDRESS_PERMISSION_MISSING, errInfo));
			}
		}
	}

	/**
	 * Clear all group data on given users. NOTICE: already persists users !
	 */
	private void clearGroupOnUsers(List<IdcUser> users, String modUuid) {
		String currentTime = MdekUtils.dateToTimestamp(new Date());
		for (IdcUser user : users) {
			user.setIdcGroupId(null);
			user.setIdcGroup(null);
			user.setModTime(currentTime);
			user.setModUuid(modUuid);
			daoIdcUser.makePersistent(user);
		}
	}
}
