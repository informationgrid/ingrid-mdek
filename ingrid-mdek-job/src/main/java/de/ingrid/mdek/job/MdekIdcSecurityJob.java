/*
 * **************************************************-
 * ingrid-mdek-job
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.mdek.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekKeysSecurity;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekUtils.IdcEntityVersion;
import de.ingrid.mdek.MdekUtils.WorkState;
import de.ingrid.mdek.MdekUtilsSecurity.IdcRole;
import de.ingrid.mdek.services.catalog.MdekAddressService;
import de.ingrid.mdek.services.catalog.MdekCatalogService;
import de.ingrid.mdek.services.catalog.MdekObjectService;
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
import de.ingrid.mdek.services.persistence.db.model.IdcUserPermission;
import de.ingrid.mdek.services.persistence.db.model.Permission;
import de.ingrid.mdek.services.persistence.db.model.PermissionAddr;
import de.ingrid.mdek.services.persistence.db.model.PermissionObj;
import de.ingrid.mdek.services.persistence.db.model.T02Address;
import de.ingrid.mdek.services.security.IPermissionService;
import de.ingrid.mdek.services.security.PermissionFactory;
import de.ingrid.mdek.services.utils.MdekIdcUserHandler;
import de.ingrid.mdek.services.utils.MdekPermissionHandler;
import de.ingrid.utils.IngridDocument;

/**
 * Encapsulates all Job functionality concerning SECURITY / USER MANAGEMENT.
 
 */
@Service
public class MdekIdcSecurityJob extends MdekIdcJob {

	/** service encapsulating security functionality */
	private IPermissionService permService;
	private MdekPermissionHandler permHandler;
	private MdekIdcUserHandler userHandler;
	private MdekAddressService addressService;
	private MdekObjectService objectService;
	private MdekCatalogService catalogService;

	private IIdcGroupDao daoIdcGroup;
	private IIdcUserDao daoIdcUser;
	private IAddressNodeDao daoAddressNode;
	private IObjectNodeDao daoObjectNode;

	/** generic dao: ENTITY UNSPECIFIC for transaction ops ... */
	private IGenericDao<IEntity> dao;

	protected BeanToDocMapperSecurity beanToDocMapperSecurity;
	protected DocToBeanMapperSecurity docToBeanMapperSecurity;

	@Autowired
	public MdekIdcSecurityJob(ILogService logService,
			DaoFactory daoFactory,
			IPermissionService permissionService) {
		super(logService.getLogger(MdekIdcSecurityJob.class), daoFactory);
		
		this.permService = permissionService;
		permHandler = MdekPermissionHandler.getInstance(permissionService, daoFactory);
		userHandler = MdekIdcUserHandler.getInstance(daoFactory);
		addressService = MdekAddressService.getInstance(daoFactory, permissionService);
		objectService = MdekObjectService.getInstance(daoFactory, permissionService);
		catalogService = MdekCatalogService.getInstance(daoFactory);
		
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
			dao.disableAutoFlush();

			Boolean includeCatAdminGroup = (Boolean) params.get(MdekKeysSecurity.REQUESTINFO_INCLUDE_CATADMIN_GROUP);

			List<IdcGroup> groups = daoIdcGroup.getGroups();
			
			// fetch group id of catAdmin for comparison
			Long catAdminGroupId = null;
			if (!includeCatAdminGroup) {
				catAdminGroupId = permService.getCatalogAdminGroup().getId();
			}

			ArrayList<IngridDocument> resultList = new ArrayList<IngridDocument>(groups.size());
			for (IdcGroup group : groups) {
				// skip group of catAdmin ?
				if (!includeCatAdminGroup) {
					if (group.getId().equals(catAdminGroupId)) {
						continue;
					}
				}

				IngridDocument groupDoc = new IngridDocument();
				beanToDocMapperSecurity.mapIdcGroup(group, groupDoc, MappingQuantity.BASIC_ENTITY);
				resultList.add(groupDoc);
			}

			IngridDocument result = new IngridDocument();
			result.put(MdekKeysSecurity.GROUPS, resultList);

			daoIdcGroup.commitTransaction();
			return result;

		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
		    throw handledExc;
		}
	}

	public IngridDocument getGroupDetails(IngridDocument params) {
		try {
			daoIdcGroup.beginTransaction();
			dao.disableAutoFlush();

			String name = params.getString(MdekKeys.NAME);
			IngridDocument result = getGroupDetails(name);
			
			daoIdcGroup.commitTransaction();
			return result;
			
		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
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

	public IngridDocument getUsersOfGroup(IngridDocument params) {
		try {
			daoIdcUser.beginTransaction();
			dao.disableAutoFlush();

			String name = params.getString(MdekKeys.NAME);
			
			List<IdcUser> users = daoIdcUser.getIdcUsersByGroupName(name);

			ArrayList<IngridDocument> resultList = new ArrayList<IngridDocument>(users.size());
			for (IdcUser user : users) {
				IngridDocument uDoc = new IngridDocument();
				beanToDocMapperSecurity.mapIdcUser(user, uDoc, MappingQuantity.TREE_ENTITY);				
				resultList.add(uDoc);
			}

			IngridDocument resultDoc = new IngridDocument();
			resultDoc.put(MdekKeysSecurity.IDC_USERS, resultList);
			
			daoIdcUser.commitTransaction();
			return resultDoc;
			
		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
		    throw handledExc;
		}
	}

	public IngridDocument createGroup(IngridDocument gDocIn) {
		String userId = getCurrentUserUuid(gDocIn);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JobType.STORE, 0, 1, false));

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
			checkPermissionStructureOfGroup(newGrp);

			 // save it, generates id
			daoIdcGroup.makePersistent(newGrp);
			
			// COMMIT BEFORE REFETCHING !!! otherwise we get old data ???
			daoIdcGroup.commitTransaction();

			// return basic data
			IngridDocument result = new IngridDocument();
			result.put(MdekKeysSecurity.ID, newGrp.getId());

			if (refetchAfterStore) {
				daoIdcGroup.beginTransaction();
				result = getGroupDetails(name);
				daoIdcGroup.commitTransaction();
			}
			
			return result;

		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
			removeRunningJob = errorHandler.shouldRemoveRunningJob(handledExc);
		    throw handledExc;
		} finally {
			if (removeRunningJob) {
				removeRunningJob(userId);				
			}
		}
	}

	public IngridDocument storeGroup(IngridDocument gDocIn) {
		String userUuid = getCurrentUserUuid(gDocIn);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userUuid, createRunningJobDescription(JobType.STORE, 0, 1, false));

			daoIdcGroup.beginTransaction();

			String currentTime = MdekUtils.dateToTimestamp(new Date());
			Long grpId = (Long) gDocIn.get(MdekKeysSecurity.ID);
			Boolean refetchAfterStore = (Boolean) gDocIn.get(MdekKeys.REQUESTINFO_REFETCH_ENTITY);

			// set common data to transfer !
			gDocIn.put(MdekKeys.DATE_OF_LAST_MODIFICATION, currentTime);
			beanToDocMapper.mapModUser(userUuid, gDocIn, MappingQuantity.INITIAL_ENTITY);
			
			// exception if group not existing
			IdcGroup grp = daoIdcGroup.getGroupDetails(grpId);
			if (grp == null) {
				throw new MdekException(new MdekError(MdekErrorType.ENTITY_NOT_FOUND));
			}

			// perform checks, concerning removed data !

			// check removed/added object/address/user permissions
			checkRemovedPermissions(grp, gDocIn, userUuid);
			checkAddedPermissions(grp, gDocIn, userUuid);
			
			// transfer new data AND MAKE PERSISTENT, so oncoming checks have newest data !
			docToBeanMapperSecurity.mapIdcGroup(gDocIn, grp, MappingQuantity.DETAIL_ENTITY);
			daoIdcGroup.makePersistent(grp);

			// perform checks, throws exception if not ok !

			// check for nested permissions
			checkPermissionStructureOfGroup(grp);
			// check for users editing entities or responsibles with removed permissions
			checkMissingPermissionOfGroup(grp);

			// COMMIT BEFORE REFETCHING !!! otherwise we get old data ???
			daoIdcGroup.commitTransaction();

			// return basic data
			IngridDocument result = new IngridDocument();
			result.put(MdekKeysSecurity.ID, grp.getId());

			if (refetchAfterStore) {
				daoIdcGroup.beginTransaction();
				result = getGroupDetails(grp.getName());
				daoIdcGroup.commitTransaction();
			}
			
			return result;

		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
			removeRunningJob = errorHandler.shouldRemoveRunningJob(handledExc);
		    throw handledExc;
		} finally {
			if (removeRunningJob) {
				removeRunningJob(userUuid);				
			}
		}
	}
	
	public IngridDocument deleteGroup(IngridDocument docIn) {
		String userId = getCurrentUserUuid(docIn);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JobType.DELETE, 0, 1, false));

			daoIdcGroup.beginTransaction();

			Boolean forceDeleteGroupWhenUsers = (Boolean) docIn.get(MdekKeysSecurity.REQUESTINFO_FORCE_DELETE_GROUP_WHEN_USERS);
			Long grpId = (Long) docIn.get(MdekKeysSecurity.ID);
			IdcGroup group = daoIdcGroup.getById(grpId);
			
			// first checks
			if (group == null) {
				throw new MdekException(new MdekError(MdekErrorType.ENTITY_NOT_FOUND));
			}
			List<IdcUser> groupUsers = daoIdcUser.getIdcUsersByGroupId(group.getId());
			if (!forceDeleteGroupWhenUsers) {
				if (groupUsers.size() > 0) {
					// attach error info
					IngridDocument errInfo = beanToDocMapperSecurity.mapIdcUserList(groupUsers, new IngridDocument(), false);;
					throw new MdekException(new MdekError(MdekErrorType.GROUP_HAS_USERS, errInfo));
				}
			}

			// remove ALL permissions AND MAKE PERSISTENT, so oncoming checks work (checks read from database) !
			IngridDocument noPermissionsDoc = new IngridDocument();
			docToBeanMapperSecurity.updateIdcUserPermissions(noPermissionsDoc, group);
			docToBeanMapperSecurity.updatePermissionAddrs(noPermissionsDoc, group);
			docToBeanMapperSecurity.updatePermissionObjs(noPermissionsDoc, group);
			daoIdcGroup.makePersistent(group);

			// check for users editing entities or responsibles with removed permissions
			checkMissingPermissionOfGroup(group);
			
			// ok, we update ex group users (remove from group, ...) and return them
			clearGroupOnUsers(group, groupUsers, userId);
			IngridDocument result = new IngridDocument();
			beanToDocMapperSecurity.mapIdcUserList(groupUsers, result, true);

			// all ok, now we delete and COMMIT !
			daoIdcGroup.makeTransient(group);
			daoIdcGroup.commitTransaction();
			
			return result;

		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
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
			dao.disableAutoFlush();

			String addrUuid = params.getString(MdekKeys.UUID);
			String userAddrUuid = getCurrentUserUuid(params);
			Boolean checkWorkflow = (Boolean) params.get(MdekKeysSecurity.REQUESTINFO_CHECK_WORKFLOW);

			List<Permission> perms = permHandler.getPermissionsForAddress(addrUuid, userAddrUuid, checkWorkflow);

			IngridDocument resultDoc = new IngridDocument();
			beanToDocMapperSecurity.mapPermissionList(perms, resultDoc);

			dao.commitTransaction();
			return resultDoc;
			
		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
		    throw handledExc;
		}
	}

	public IngridDocument getObjectPermissions(IngridDocument params) {
		try {
			dao.beginTransaction();
			dao.disableAutoFlush();

			String objUuid = params.getString(MdekKeys.UUID);
			String userAddrUuid = getCurrentUserUuid(params);
			Boolean checkWorkflow = (Boolean) params.get(MdekKeysSecurity.REQUESTINFO_CHECK_WORKFLOW);

			List<Permission> perms = permHandler.getPermissionsForObject(objUuid, userAddrUuid, checkWorkflow);

			IngridDocument resultDoc = new IngridDocument();
			beanToDocMapperSecurity.mapPermissionList(perms, resultDoc);

			dao.commitTransaction();
			return resultDoc;
			
		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
		    throw handledExc;
		}
	}

	public IngridDocument getUserPermissions(IngridDocument params) {
		try {
			dao.beginTransaction();
			dao.disableAutoFlush();

            String userAddrUuid = params.getString(MdekKeysSecurity.IDC_USER_ADDR_UUID);

			List<Permission> perms = permHandler.getUserPermissions(userAddrUuid);

			IngridDocument resultDoc = new IngridDocument();
			beanToDocMapperSecurity.mapPermissionList(perms, resultDoc);

			dao.commitTransaction();
			return resultDoc;
			
		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
		    throw handledExc;
		}
	}

	public IngridDocument getUserDetails(IngridDocument params) {
		try {
			daoIdcUser.beginTransaction();
			dao.disableAutoFlush();

			String addrUuid = params.getString(MdekKeysSecurity.IDC_USER_ADDR_UUID);
			IngridDocument result = getUserDetails(addrUuid);
			
			daoIdcUser.commitTransaction();
			return result;
			
		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
		    throw handledExc;
		}
	}
	
	
	private IngridDocument getUserDetails(String addrUuid) {
		IdcUser user = daoIdcUser.getIdcUserByAddrUuid(addrUuid);
		if (user == null) {
			throw new MdekException(new MdekError(MdekErrorType.ENTITY_NOT_FOUND));
		}
		
		AddressNode addressNode = addressService.loadByUuid(addrUuid, IdcEntityVersion.WORKING_VERSION);
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
			addRunningJob(userId, createRunningJobDescription(JobType.STORE, 0, 1, false));

			daoIdcUser.beginTransaction();

			String currentTime = MdekUtils.dateToTimestamp(new Date());
			// address uuid should not be passed anymore, instead full address data, see INGRID32-36
			String newUserAddrUuid = uDocIn.getString(MdekKeysSecurity.IDC_USER_ADDR_UUID);
			if (newUserAddrUuid != null) {
				throw new MdekException("Address Uuid passed to createUser ! But should not have IGC address yet !");				
			}
			Integer newUserRole = (Integer) uDocIn.get(MdekKeysSecurity.IDC_ROLE);
			Long newUserParentId = (Long) uDocIn.get(MdekKeysSecurity.PARENT_IDC_USER_ID); 
			Boolean refetchAfterStore = (Boolean) uDocIn.get(MdekKeys.REQUESTINFO_REFETCH_ENTITY);

			// set common data to transfer !
			uDocIn.put(MdekKeys.DATE_OF_CREATION, currentTime);
			uDocIn.put(MdekKeys.DATE_OF_LAST_MODIFICATION, currentTime);
			beanToDocMapper.mapModUser(userId, uDocIn, MappingQuantity.INITIAL_ENTITY);
			
			IdcUser callingUser = userHandler.getCurrentUser(userId);

			// check role of calling user above role of new user ?
			if (!userHandler.isRole1AboveRole2(callingUser.getIdcRole(), newUserRole)) {
				throw new MdekException(new MdekError(MdekErrorType.USER_HAS_WRONG_ROLE));
			}

			// check calling user is parent of new user ?
			if (!userHandler.isUser1AboveOrEqualUser2(callingUser.getId(), newUserParentId)) {
				throw new MdekException(new MdekError(MdekErrorType.USER_HIERARCHY_WRONG));
			}				

			// NEW: separation of user addresses, also create address with new address data from user administration, see INGRID32-36
			// NOTICE: address data is part of user doc !
			processUserDocWithAddressData(uDocIn);
			newUserAddrUuid = addressService.storeWorkingCopy(uDocIn, userId);
			uDocIn.put(MdekKeysSecurity.IDC_USER_ADDR_UUID, newUserAddrUuid);

			// exception if user already exists
			if (daoIdcUser.getIdcUserByAddrUuid(newUserAddrUuid) != null) {
				throw new MdekException(new MdekError(MdekErrorType.ENTITY_ALREADY_EXISTS));
			}

			// create new user and save it (to generate id !). ID NEEDED FOR MAPPING OF ASSOCIATIONS !
			IdcUser newUser = new IdcUser();
			daoIdcUser.makePersistent(newUser);
			// then map full details ! user id used for associations 
			newUser = docToBeanMapperSecurity.mapIdcUser(uDocIn, newUser);
			daoIdcUser.makePersistent(newUser);
			
			// COMMIT BEFORE REFETCHING !!! otherwise we get old data ???
			daoIdcUser.commitTransaction();

			// return basic data
			IngridDocument result = new IngridDocument();
			result.put(MdekKeysSecurity.IDC_USER_ID, newUser.getId());
			result.put(MdekKeysSecurity.IDC_USER_ADDR_UUID, newUserAddrUuid);

			if (refetchAfterStore) {
				daoIdcUser.beginTransaction();
				result = getUserDetails(newUserAddrUuid);
				daoIdcUser.commitTransaction();
			}
			
			return result;

		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
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
			addRunningJob(userId, createRunningJobDescription(JobType.STORE, 0, 1, false));

			daoIdcUser.beginTransaction();

			String currentTime = MdekUtils.dateToTimestamp(new Date());
			String newAddrUuid = uDocIn.getString(MdekKeysSecurity.IDC_USER_ADDR_UUID);
			Long inUserId = (Long) uDocIn.get(MdekKeysSecurity.IDC_USER_ID);
			Integer inUserRole = (Integer) uDocIn.get(MdekKeysSecurity.IDC_ROLE);
			Long inUserParentId = (Long) uDocIn.get(MdekKeysSecurity.PARENT_IDC_USER_ID); 
			Boolean refetchAfterStore = (Boolean) uDocIn.get(MdekKeys.REQUESTINFO_REFETCH_ENTITY);

			// set common data to transfer !
			uDocIn.put(MdekKeys.DATE_OF_LAST_MODIFICATION, currentTime);
			beanToDocMapper.mapModUser(userId, uDocIn, MappingQuantity.INITIAL_ENTITY);
			
			// get user to update. exception if user not existing
			IdcUser userToUpdate = userHandler.getUserById(inUserId);
			boolean userToUpdateIsCatalogAdmin = 
				IdcRole.CATALOG_ADMINISTRATOR.getDbValue().equals(userToUpdate.getIdcRole());
			String oldAddrUuid = userToUpdate.getAddrUuid();
			boolean userAddressChanged = !oldAddrUuid.equals(newAddrUuid);

			// check whether address uuid changed ! NOT POSSIBLE ANYMORE, no UUID input in frontend, see INGRID32-36
			if (userAddressChanged) {
				throw new MdekException("Address Uuid changed in storeUser ! But should always keep same Address !");				
			}

			// check role of updated user changed ? NOT POSSIBLE !
			if (!userToUpdate.getIdcRole().equals(inUserRole)) {
				throw new MdekException(new MdekError(MdekErrorType.USER_HAS_WRONG_ROLE));
			}

			// check parent of updated user changed ? NOT POSSIBLE !
			long oldParentId = (userToUpdate.getParentId() == null) ? 0 : userToUpdate.getParentId(); 
			long newParentId = (inUserParentId == null) ? 0 : inUserParentId; 
			if (oldParentId != newParentId) {
				throw new MdekException(new MdekError(MdekErrorType.USER_HIERARCHY_WRONG));
			}

			// get calling user. exception if user not existing
			IdcUser callingUser = userHandler.getCurrentUser(userId);
			boolean callingUserIsCatalogAdmin =
				IdcRole.CATALOG_ADMINISTRATOR.getDbValue().equals(callingUser.getIdcRole());

			// CATALOG ADMINISTRATOR CAN CHANGE GROUP AND ADDRESS OF HIMSELF !!!!!
			// all other user can't do this, is done by their parents !
			boolean catalogAdminUpdatesHimself = callingUserIsCatalogAdmin && userToUpdateIsCatalogAdmin;
			if (!catalogAdminUpdatesHimself) {
				// check role of calling user above role of user to update ?
				if (!userHandler.isRole1AboveRole2(callingUser.getIdcRole(), userToUpdate.getIdcRole())) {
					throw new MdekException(new MdekError(MdekErrorType.USER_HAS_WRONG_ROLE));
				}
				// check calling user is parent of user to update ?
				if (!userHandler.isUser1AboveOrEqualUser2(callingUser.getId(), userToUpdate.getParentId())) {
					throw new MdekException(new MdekError(MdekErrorType.USER_HIERARCHY_WRONG));
				}
			}

			// update user !
			docToBeanMapperSecurity.mapIdcUser(uDocIn, userToUpdate);
			daoIdcUser.makePersistent(userToUpdate);

			// NEW: separation of user addresses, also update address with new address data from user administration, see INGRID32-36
			// NOTICE: address data is part of user doc !
			processUserDocWithAddressData(uDocIn);
			addressService.storeWorkingCopy(uDocIn, userId);

			// address uuid changed ?
			// then update all entities, where old uuid is responsible user with new address uuid.
			if (userAddressChanged) {
				catalogService.updateResponsibleUserInEntities(oldAddrUuid, newAddrUuid);					
			}
			
			// COMMIT BEFORE REFETCHING !!! otherwise we get old data ???
			daoIdcUser.commitTransaction();

			// return basic data
			IngridDocument result = new IngridDocument();
			result.put(MdekKeysSecurity.IDC_USER_ID, userToUpdate.getId());

			if (refetchAfterStore) {
				daoIdcUser.beginTransaction();
				result = getUserDetails(userToUpdate.getAddrUuid());
				daoIdcUser.commitTransaction();
			}
			
			return result;

		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
			removeRunningJob = errorHandler.shouldRemoveRunningJob(handledExc);
		    throw handledExc;
		} finally {
			if (removeRunningJob) {
				removeRunningJob(userId);				
			}
		}
	}

	/** Prepare userDoc to be also saved as address. User address now separated
	 * from normal addresses and part of userDoc (entered in user admin gui), see INGRID32-36. 
	 */
	private IngridDocument processUserDocWithAddressData(IngridDocument userDoc) {
		// We have to supply address uuid in normal address format ! 
		userDoc.put(MdekKeys.UUID, userDoc.get(MdekKeysSecurity.IDC_USER_ADDR_UUID));
		// user address type !
		userDoc.put(MdekKeys.CLASS, MdekUtils.AddressType.getHiddenAddressTypeIGEUser());
		// user parent !
		userDoc.put(MdekKeys.PARENT_UUID, MdekUtils.AddressType.getIGEUserParentUuid());
		
		return userDoc;
	}

	public void deleteUser(IngridDocument uDocIn) {
		String userId = getCurrentUserUuid(uDocIn);
		boolean removeRunningJob = true;
		try {
			// first add basic running jobs info !
			addRunningJob(userId, createRunningJobDescription(JobType.DELETE, 0, 1, false));

			daoIdcUser.beginTransaction();
			Long inUserId = (Long) uDocIn.get(MdekKeysSecurity.IDC_USER_ID);
			
			// exception if user not existing
			IdcUser userToDelete = userHandler.getUserById(inUserId);
			boolean userToDeleteIsCatalogAdmin = 
				IdcRole.CATALOG_ADMINISTRATOR.getDbValue().equals(userToDelete.getIdcRole());

			IdcUser callingUser = userHandler.getCurrentUser(userId);

			// user is catalog admin ?
			if (userToDeleteIsCatalogAdmin) {
				throw new MdekException(new MdekError(MdekErrorType.USER_IS_CATALOG_ADMIN));
			}
			// user has subusers ?
			if (userToDelete.getIdcUsers().size() > 0) {
				throw new MdekException(new MdekError(MdekErrorType.USER_HAS_SUBUSERS));
			}
			// check role of calling user above role of user to delete ?
			if (!userHandler.isRole1AboveRole2(callingUser.getIdcRole(), userToDelete.getIdcRole())) {
				throw new MdekException(new MdekError(MdekErrorType.USER_HAS_WRONG_ROLE));
			}				
			// check calling user is parent of user to delete ?
			if (!userHandler.isUser1AboveOrEqualUser2(callingUser.getId(), userToDelete.getParentId())) {
				throw new MdekException(new MdekError(MdekErrorType.USER_HIERARCHY_WRONG));
			}

			// ok, we update all entities, where user to delete is responsible user with parent user.
			String oldResponsibleUuid = userToDelete.getAddrUuid();
			String newResponsibleUuid = userHandler.getUserById(userToDelete.getParentId()).getAddrUuid();
			catalogService.updateResponsibleUserInEntities(oldResponsibleUuid, newResponsibleUuid);

			// finally delete the user !
			daoIdcUser.makeTransient(userToDelete);

			// NEW: separation of user addresses, also delete address, see INGRID32-36
			addressService.deleteAddressFull(userToDelete.getAddrUuid(), false, userId);

			daoIdcUser.commitTransaction();

		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
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
			dao.disableAutoFlush();

			IdcUser user = permService.getCatalogAdminUser();
			if (user == null) {
				throw new MdekException(new MdekError(MdekErrorType.ENTITY_NOT_FOUND));
			}
			
			// map before committing, uses lazy loading
			IngridDocument result = new IngridDocument();
			beanToDocMapperSecurity.mapIdcUser(user, result, MappingQuantity.DETAIL_ENTITY);
			
			daoIdcUser.commitTransaction();

			return result;

		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
		    throw handledExc;
		}
	}	

	public IngridDocument getSubUsers(IngridDocument params) {
		try {
			daoIdcUser.beginTransaction();
			dao.disableAutoFlush();

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
			RuntimeException handledExc = handleException(e);
		    throw handledExc;
		}
	}

	public IngridDocument getUsersWithWritePermissionForObject(IngridDocument params) {
		try {
			daoIdcUser.beginTransaction();
			dao.disableAutoFlush();

			String objUuid = (String) params.get(MdekKeys.UUID);
			Boolean checkWorkflow = (Boolean) params.get(MdekKeysSecurity.REQUESTINFO_CHECK_WORKFLOW);
			Boolean getDetailedPermissions = (Boolean) params.get(MdekKeysSecurity.REQUESTINFO_GET_DETAILED_PERMISSIONS);

			// get all groups: search users via groups !
			List<IdcGroup> allGroups = daoIdcGroup.getGroups();
			// and search users with write access on object
			List<IdcUser> users = permHandler.getUsersWithWritePermissionForObject(objUuid, allGroups, checkWorkflow);

			ArrayList<IngridDocument> resultList = new ArrayList<IngridDocument>(users.size());
			for (IdcUser user : users) {
				IngridDocument uDoc = new IngridDocument();
				beanToDocMapperSecurity.mapIdcUser(user, uDoc, MappingQuantity.TREE_ENTITY);
				
				if (getDetailedPermissions) {
					List<Permission> perms = permHandler.getPermissionsForObject(objUuid, user.getAddrUuid(), checkWorkflow);
					List<Permission> permsUser = permHandler.getUserPermissions(user.getAddrUuid());
					perms.addAll(permsUser);
					beanToDocMapperSecurity.mapPermissionList(perms, uDoc);					
				}

				resultList.add(uDoc);
			}

			IngridDocument result = new IngridDocument();
			result.put(MdekKeysSecurity.IDC_USERS, resultList);

			daoIdcUser.commitTransaction();
			return result;

		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
		    throw handledExc;
		}
	}

	public IngridDocument getUsersWithWritePermissionForAddress(IngridDocument params) {
		try {
			daoIdcUser.beginTransaction();
			dao.disableAutoFlush();

			String addrUuid = (String) params.get(MdekKeys.UUID);
			Boolean checkWorkflow = (Boolean) params.get(MdekKeysSecurity.REQUESTINFO_CHECK_WORKFLOW);
			Boolean getDetailedPermissions = (Boolean) params.get(MdekKeysSecurity.REQUESTINFO_GET_DETAILED_PERMISSIONS);

			// get all groups: search users via groups !
			List<IdcGroup> allGroups = daoIdcGroup.getGroups();
			// and search users with write access on address
			List<IdcUser> users = permHandler.getUsersWithWritePermissionForAddress(addrUuid, allGroups, checkWorkflow);

			ArrayList<IngridDocument> resultList = new ArrayList<IngridDocument>(users.size());
			for (IdcUser user : users) {
				IngridDocument uDoc = new IngridDocument();
				beanToDocMapperSecurity.mapIdcUser(user, uDoc, MappingQuantity.TREE_ENTITY);

				if (getDetailedPermissions) {
					List<Permission> perms = permHandler.getPermissionsForAddress(addrUuid, user.getAddrUuid(), checkWorkflow);
					List<Permission> permsUser = permHandler.getUserPermissions(user.getAddrUuid());
					perms.addAll(permsUser);
					beanToDocMapperSecurity.mapPermissionList(perms, uDoc);					
				}

				resultList.add(uDoc);
			}

			IngridDocument result = new IngridDocument();
			result.put(MdekKeysSecurity.IDC_USERS, resultList);

			daoIdcUser.commitTransaction();
			return result;

		} catch (RuntimeException e) {
			RuntimeException handledExc = handleException(e);
		    throw handledExc;
		}
	}

    public IngridDocument getResponsibleUsersForNewObject(IngridDocument params) {
        try {
            daoIdcUser.beginTransaction();
            dao.disableAutoFlush();

            String objUuid = (String) params.get(MdekKeys.UUID);
    		String userUuid = getCurrentUserUuid(params);
            Boolean checkWorkflow = (Boolean) params.get(MdekKeysSecurity.REQUESTINFO_CHECK_WORKFLOW);
            Boolean getDetailedPermissions = (Boolean) params.get(MdekKeysSecurity.REQUESTINFO_GET_DETAILED_PERMISSIONS);

            // get all groups: search users via groups !
            List<IdcGroup> allGroups = daoIdcGroup.getGroups();
            // and search users with write access on object
            List<IdcUser> users =
            	permHandler.getResponsibleUsersForNewObject(objUuid, allGroups, checkWorkflow, userUuid);

            ArrayList<IngridDocument> resultList = new ArrayList<IngridDocument>(users.size());
            for (IdcUser user : users) {
                IngridDocument uDoc = new IngridDocument();
                beanToDocMapperSecurity.mapIdcUser(user, uDoc, MappingQuantity.TREE_ENTITY);
                
                if (getDetailedPermissions) {
                    List<Permission> perms = permHandler.getPermissionsForObject(objUuid, user.getAddrUuid(), checkWorkflow);
                    List<Permission> permsUser = permHandler.getUserPermissions(user.getAddrUuid());
                    perms.addAll(permsUser);
                    beanToDocMapperSecurity.mapPermissionList(perms, uDoc);                 
                }

                resultList.add(uDoc);
            }

            IngridDocument result = new IngridDocument();
            result.put(MdekKeysSecurity.IDC_USERS, resultList);

            daoIdcUser.commitTransaction();
            return result;

        } catch (RuntimeException e) {
            RuntimeException handledExc = handleException(e);
            throw handledExc;
        }
    }	
	
    /** Get all users who can be responsible user for a new address created in frontend.
     * @param params
     * @return
     */
    public IngridDocument getResponsibleUsersForNewAddress(IngridDocument params) {
        try {
            daoIdcUser.beginTransaction();
            dao.disableAutoFlush();

            String addrUuid = (String) params.get(MdekKeys.UUID);
    		String userUuid = getCurrentUserUuid(params);
            Boolean checkWorkflow = (Boolean) params.get(MdekKeysSecurity.REQUESTINFO_CHECK_WORKFLOW);
            Boolean getDetailedPermissions = (Boolean) params.get(MdekKeysSecurity.REQUESTINFO_GET_DETAILED_PERMISSIONS);

            // get all groups: search users via groups !
            List<IdcGroup> allGroups = daoIdcGroup.getGroups();
            // and search users with write access on address
            List<IdcUser> users =
            	permHandler.getResponsibleUsersForNewAddress(addrUuid, allGroups, checkWorkflow, userUuid);

            ArrayList<IngridDocument> resultList = new ArrayList<IngridDocument>(users.size());
            for (IdcUser user : users) {
                IngridDocument uDoc = new IngridDocument();
                beanToDocMapperSecurity.mapIdcUser(user, uDoc, MappingQuantity.TREE_ENTITY);

                if (getDetailedPermissions) {
                    List<Permission> perms = permHandler.getPermissionsForAddress(addrUuid, user.getAddrUuid(), checkWorkflow);
                    List<Permission> permsUser = permHandler.getUserPermissions(user.getAddrUuid());
                    perms.addAll(permsUser);
                    beanToDocMapperSecurity.mapPermissionList(perms, uDoc);                 
                }

                resultList.add(uDoc);
            }

            IngridDocument result = new IngridDocument();
            result.put(MdekKeysSecurity.IDC_USERS, resultList);

            daoIdcUser.commitTransaction();
            return result;

        } catch (RuntimeException e) {
            RuntimeException handledExc = handleException(e);
            throw handledExc;
        }
    }	
    
    /**
     * Get users with a any permission on an object.  
     * 
     * @param params
     * @return
     */
    public IngridDocument getUsersWithPermissionForObject(IngridDocument params) {
        try {
            daoIdcUser.beginTransaction();
            dao.disableAutoFlush();

            String objUuid = (String) params.get(MdekKeys.UUID);
            Boolean checkWorkflow = (Boolean) params.get(MdekKeysSecurity.REQUESTINFO_CHECK_WORKFLOW);
            Boolean getDetailedPermissions = (Boolean) params.get(MdekKeysSecurity.REQUESTINFO_GET_DETAILED_PERMISSIONS);

            // get all groups: search users via groups !
            List<IdcGroup> allGroups = daoIdcGroup.getGroups();
            // and search users with write access on object
            List<IdcUser> users = permHandler.getUsersWithPermissionForObject(objUuid, allGroups, checkWorkflow);

            ArrayList<IngridDocument> resultList = new ArrayList<IngridDocument>(users.size());
            for (IdcUser user : users) {
                IngridDocument uDoc = new IngridDocument();
                beanToDocMapperSecurity.mapIdcUser(user, uDoc, MappingQuantity.TREE_ENTITY);

                if (getDetailedPermissions) {
                    List<Permission> perms = permHandler.getPermissionsForObject(objUuid, user.getAddrUuid(), checkWorkflow);
                    List<Permission> permsUser = permHandler.getUserPermissions(user.getAddrUuid());
                    perms.addAll(permsUser);
                    beanToDocMapperSecurity.mapPermissionList(perms, uDoc);                 
                }

                resultList.add(uDoc);
            }

            IngridDocument result = new IngridDocument();
            result.put(MdekKeysSecurity.IDC_USERS, resultList);

            daoIdcUser.commitTransaction();
            return result;

        } catch (RuntimeException e) {
            RuntimeException handledExc = handleException(e);
            throw handledExc;
        }
    }
    
    /**
     * Get users with a any permission on an address.  
     * 
     * @param params
     * @return
     */
    public IngridDocument getUsersWithPermissionForAddress(IngridDocument params) {
        try {
            daoIdcUser.beginTransaction();
            dao.disableAutoFlush();

            String addrUuid = (String) params.get(MdekKeys.UUID);
            Boolean checkWorkflow = (Boolean) params.get(MdekKeysSecurity.REQUESTINFO_CHECK_WORKFLOW);
            Boolean getDetailedPermissions = (Boolean) params.get(MdekKeysSecurity.REQUESTINFO_GET_DETAILED_PERMISSIONS);

            // get all groups: search users via groups !
            List<IdcGroup> allGroups = daoIdcGroup.getGroups();
            // and search users with write access on address
            List<IdcUser> users = permHandler.getUsersWithPermissionForAddress(addrUuid, allGroups, checkWorkflow);

            ArrayList<IngridDocument> resultList = new ArrayList<IngridDocument>(users.size());
            for (IdcUser user : users) {
                IngridDocument uDoc = new IngridDocument();
                beanToDocMapperSecurity.mapIdcUser(user, uDoc, MappingQuantity.TREE_ENTITY);

                if (getDetailedPermissions) {
                    List<Permission> perms = permHandler.getPermissionsForAddress(addrUuid, user.getAddrUuid(), checkWorkflow);
                    List<Permission> permsUser = permHandler.getUserPermissions(user.getAddrUuid());
                    perms.addAll(permsUser);
                    beanToDocMapperSecurity.mapPermissionList(perms, uDoc);                 
                }

                resultList.add(uDoc);
            }

            IngridDocument result = new IngridDocument();
            result.put(MdekKeysSecurity.IDC_USERS, resultList);

            daoIdcUser.commitTransaction();
            return result;

        } catch (RuntimeException e) {
            RuntimeException handledExc = handleException(e);
            throw handledExc;
        }
    }    
	
	/**
	 * Validate whether the removed permissions of the given group are ok or whether the user
	 * is not allowed to remove an object/address/user permission. This is the case if
	 * the user doesn't have write permissions on a removed object/address or doesn't have
	 * the removed user permission himself (e.g. create root)
	 * @param oldGrp group before applying changes
	 * @param newGrpDoc new group which should be stored
	 * @param userUuid the user storing the group
	 */
	private void checkRemovedPermissions(IdcGroup oldGrp, IngridDocument newGrpDoc, String userUuid) {
		checkRemovedObjectPermissions(oldGrp, newGrpDoc, userUuid);
		checkRemovedAddressPermissions(oldGrp, newGrpDoc, userUuid);
		checkRemovedUserPermissions(oldGrp, newGrpDoc, userUuid);
	}

	/**
	 * Validate whether the removed object permissions of the given group are ok or whether
	 * the user is not allowed to remove an object permission. This is the case if the 
	 * user doesn't have write permission on a removed object.
	 * @param oldGrp group before applying changes
	 * @param newGrpDoc new group which should be stored
	 * @param userUuid the user storing the group
	 */
	private void checkRemovedObjectPermissions(IdcGroup oldGrp, IngridDocument newGrpDoc, String userUuid) {
		List<PermissionObj> removedPerms = 
			permHandler.getRemovedObjectPermissionsOfGroup(oldGrp, newGrpDoc);
		for (PermissionObj removedPerm : removedPerms) {
			String objUuid = removedPerm.getUuid();
			if (!permHandler.hasWritePermissionForObject(objUuid, userUuid, false)) {
				IngridDocument errInfo = objectService.setupErrorInfoObj(new IngridDocument(), objUuid);
				throw new MdekException(new MdekError(MdekErrorType.NO_RIGHT_TO_REMOVE_OBJECT_PERMISSION, errInfo));
			}
		}
	}

	/**
	 * Validate whether the removed address permissions of the given group are ok or whether
	 * the user is not allowed to remove an address permission. This is the case if the 
	 * user doesn't have write permission on a removed address.
	 * @param oldGrp group before applying changes
	 * @param newGrpDoc new group which should be stored
	 * @param userUuid the user storing the group
	 */
	private void checkRemovedAddressPermissions(IdcGroup oldGrp, IngridDocument newGrpDoc, String userUuid) {
		List<PermissionAddr> removedPerms = 
			permHandler.getRemovedAddressPermissionsOfGroup(oldGrp, newGrpDoc);
		for (PermissionAddr removedPerm : removedPerms) {
			String addrUuid = removedPerm.getUuid();
			if (!permHandler.hasWritePermissionForAddress(addrUuid, userUuid, false)) {
				IngridDocument errInfo = addressService.setupErrorInfoAddr(new IngridDocument(), addrUuid);
				throw new MdekException(new MdekError(MdekErrorType.NO_RIGHT_TO_REMOVE_ADDRESS_PERMISSION, errInfo));
			}
		}
	}

	/**
	 * Validate whether the removed user permissions of the given group are ok or whether
	 * the user is not allowed to remove an user permission. This is the case if the 
	 * user doesn't have the user permission himself (e.g. create root).
	 * @param oldGrp group before applying changes
	 * @param newGrpDoc new group which should be stored
	 * @param userUuid the user storing the group
	 */
	private void checkRemovedUserPermissions(IdcGroup oldGrp, IngridDocument newGrpDoc, String userUuid) {
		List<IdcUserPermission> removedPerms = 
			permHandler.getRemovedUserPermissionsOfGroup(oldGrp, newGrpDoc);
		for (IdcUserPermission removedPerm : removedPerms) {
			if (!permHandler.hasUserPermission(removedPerm.getPermission(), userUuid)) {
				throw new MdekException(new MdekError(MdekErrorType.NO_RIGHT_TO_REMOVE_USER_PERMISSION));
			}
		}
	}

	/**
	 * Validate whether the added permissions of the given group are ok or whether the user
	 * is not allowed to add an object/address/user permission. This is the case if
	 * the user doesn't have write permissions on an added object/address or doesn't have
	 * the removed user permission himself (e.g. create root)
	 * @param oldGrp group before applying changes
	 * @param newGrpDoc new group which should be stored
	 * @param userUuid the user storing the group
	 */
	private void checkAddedPermissions(IdcGroup oldGrp, IngridDocument newGrpDoc, String userUuid) {
		checkAddedObjectPermissions(oldGrp, newGrpDoc, userUuid);
		checkAddedAddressPermissions(oldGrp, newGrpDoc, userUuid);
		checkAddedUserPermissions(oldGrp, newGrpDoc, userUuid);
	}

	/**
	 * Validate whether the added object permissions of the given group are ok or whether
	 * the user is not allowed to add an object permission. This is the case if the 
	 * user doesn't have write permission on an added object.
	 * @param oldGrp group before applying changes
	 * @param newGrpDoc new group which should be stored
	 * @param userUuid the user storing the group
	 */
	private void checkAddedObjectPermissions(IdcGroup oldGrp, IngridDocument newGrpDoc, String userUuid) {
		List<PermissionObj> addedPerms = 
			permHandler.getAddedObjectPermissionsOfGroup(oldGrp, newGrpDoc);
		for (PermissionObj addedPerm : addedPerms) {
			String objUuid = addedPerm.getUuid();
			if (!permHandler.hasWritePermissionForObject(objUuid, userUuid, false)) {
				IngridDocument errInfo = objectService.setupErrorInfoObj(new IngridDocument(), objUuid);
				throw new MdekException(new MdekError(MdekErrorType.NO_RIGHT_TO_ADD_OBJECT_PERMISSION, errInfo));
			}
		}
	}

	/**
	 * Validate whether the added address permissions of the given group are ok or whether
	 * the user is not allowed to add an address permission. This is the case if the 
	 * user doesn't have write permission on an added address.
	 * @param oldGrp group before applying changes
	 * @param newGrpDoc new group which should be stored
	 * @param userUuid the user storing the group
	 */
	private void checkAddedAddressPermissions(IdcGroup oldGrp, IngridDocument newGrpDoc, String userUuid) {
		List<PermissionAddr> addedPerms = 
			permHandler.getAddedAddressPermissionsOfGroup(oldGrp, newGrpDoc);
		for (PermissionAddr addedPerm : addedPerms) {
			String addrUuid = addedPerm.getUuid();
			if (!permHandler.hasWritePermissionForAddress(addrUuid, userUuid, false)) {
				IngridDocument errInfo = addressService.setupErrorInfoAddr(new IngridDocument(), addrUuid);
				throw new MdekException(new MdekError(MdekErrorType.NO_RIGHT_TO_ADD_ADDRESS_PERMISSION, errInfo));
			}
		}
	}

	/**
	 * Validate whether the added user permissions of the given group are ok or whether
	 * the user is not allowed to add a user permission. This is the case if the 
	 * user doesn't have the user permission himself (e.g. create root).
	 * @param oldGrp group before applying changes
	 * @param newGrpDoc new group which should be stored
	 * @param userUuid the user storing the group
	 */
	private void checkAddedUserPermissions(IdcGroup oldGrp, IngridDocument newGrpDoc, String userUuid) {
		List<IdcUserPermission> addedPerms = 
			permHandler.getAddedUserPermissionsOfGroup(oldGrp, newGrpDoc);
		for (IdcUserPermission addedPerm : addedPerms) {
			if (!permHandler.hasUserPermission(addedPerm.getPermission(), userUuid)) {
				throw new MdekException(new MdekError(MdekErrorType.NO_RIGHT_TO_ADD_USER_PERMISSION));
			}
		}
	}

	/**
	 * Validate whether the assigned permissions of the given group are ok or whether there are
	 * conflicts (e.g. "write" underneath "write-tree"). THROWS EXCEPTION IF A CONFLICT OCCURS
	 * WITH DETAILED DATA ABOUT CONFLICT.
	 * @param grp group to validate
	 */
	private void checkPermissionStructureOfGroup(IdcGroup grp) {
		checkObjectPermissionStructureOfGroup(grp);
		checkAddressPermissionStructureOfGroup(grp);
	}

	/**
	 * Validate whether the assigned OBJECT permissions of the given group are ok or whether there are
	 * conflicts (e.g. "write" underneath "write-tree" or write-subnode under write-tree). THROWS EXCEPTION IF A CONFLICT OCCURS
	 * WITH DETAILED DATA ABOUT CONFLICT.
	 * @param grp group to validate
	 */
	private void checkObjectPermissionStructureOfGroup(IdcGroup grp) {
		Set<PermissionObj> pos = grp.getPermissionObjs();

		// permission templates for checking
		Permission pTemplateSingle = PermissionFactory.getPermissionTemplateSingle();
		Permission pTemplateTree = PermissionFactory.getPermissionTemplateTree();
        Permission pTemplateSubNode = PermissionFactory.getPermissionTemplateSubNode();

		// separate uuids by permission types
		ArrayList<String> uuidsSingle = new ArrayList<String>();
		ArrayList<String> uuidsTree = new ArrayList<String>();
		for (PermissionObj po : pos) {
			Permission p = po.getPermission();
			String oUuid = po.getUuid();

			// check whether "double" permissions !
			if (uuidsSingle.contains(oUuid) || uuidsTree.contains(oUuid)) {
				IngridDocument errInfo = objectService.setupErrorInfoObj(new IngridDocument(), oUuid);
				throw new MdekException(new MdekError(MdekErrorType.MULTIPLE_PERMISSIONS_ON_OBJECT, errInfo));
			}

			// separate uuids by permission type
			if (permService.isEqualPermission(p, pTemplateSingle)) {
				uuidsSingle.add(oUuid);
			} else if (permService.isEqualPermission(p, pTemplateTree)) {
				uuidsTree.add(oUuid);
			} else if (permService.isEqualPermission(p, pTemplateSubNode)) {
				uuidsSingle.add(oUuid);
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
					IngridDocument errInfo = objectService.setupErrorInfoObj(new IngridDocument(), uuid2);
					errInfo = objectService.setupErrorInfoObj(errInfo, uuid1);
					throw new MdekException(new MdekError(MdekErrorType.TREE_BELOW_TREE_OBJECT_PERMISSION, errInfo));					
				} else if  (path2.contains(uuid1)) {
					IngridDocument errInfo = objectService.setupErrorInfoObj(new IngridDocument(), uuid1);
					errInfo = objectService.setupErrorInfoObj(errInfo, uuid2);
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
					IngridDocument errInfo = objectService.setupErrorInfoObj(new IngridDocument(), uuidTree);
					errInfo = objectService.setupErrorInfoObj(errInfo, uuidSingle);
					throw new MdekException(new MdekError(MdekErrorType.SINGLE_BELOW_TREE_OBJECT_PERMISSION, errInfo));					
				}
			}
		}
	}

	/**
	 * Validate whether the assigned ADDRESS permissions of the given group are ok or whether there are
	 * conflicts (e.g. "write" underneath "write-tree" or write-subnode under write-tree). THROWS EXCEPTION IF A CONFLICT OCCURS
	 * WITH DETAILED DATA ABOUT CONFLICT.
	 * @param grp group to validate
	 */
	private void checkAddressPermissionStructureOfGroup(IdcGroup grp) {
		Set<PermissionAddr> pas = grp.getPermissionAddrs();

		// permission templates for checking
		Permission pTemplateSingle = PermissionFactory.getPermissionTemplateSingle();
		Permission pTemplateTree = PermissionFactory.getPermissionTemplateTree();
        Permission pTemplateSubNode = PermissionFactory.getPermissionTemplateSubNode();

		// separate uuids by permission types
		ArrayList<String> uuidsSingle = new ArrayList<String>();
		ArrayList<String> uuidsTree = new ArrayList<String>();
		for (PermissionAddr pa : pas) {
			Permission p = pa.getPermission();
			String aUuid = pa.getUuid();

			// check whether "double" permissions !
			if (uuidsSingle.contains(aUuid) || uuidsTree.contains(aUuid)) {
				IngridDocument errInfo = addressService.setupErrorInfoAddr(new IngridDocument(), aUuid);
				throw new MdekException(new MdekError(MdekErrorType.MULTIPLE_PERMISSIONS_ON_ADDRESS, errInfo));
			}

			// separate uuids by permission type
			if (permService.isEqualPermission(p, pTemplateSingle)) {
				uuidsSingle.add(aUuid);
			} else if (permService.isEqualPermission(p, pTemplateTree)) {
				uuidsTree.add(aUuid);
			} else if (permService.isEqualPermission(p, pTemplateSubNode)) {
				uuidsSingle.add(aUuid);
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
					IngridDocument errInfo = addressService.setupErrorInfoAddr(new IngridDocument(), uuid2);
					errInfo = addressService.setupErrorInfoAddr(errInfo, uuid1);
					throw new MdekException(new MdekError(MdekErrorType.TREE_BELOW_TREE_ADDRESS_PERMISSION, errInfo));					
				} else if  (path2.contains(uuid1)) {
					IngridDocument errInfo = addressService.setupErrorInfoAddr(new IngridDocument(), uuid1);
					errInfo = addressService.setupErrorInfoAddr(errInfo, uuid2);
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
					IngridDocument errInfo = addressService.setupErrorInfoAddr(new IngridDocument(), uuidTree);
					errInfo = addressService.setupErrorInfoAddr(errInfo, uuidSingle);
					throw new MdekException(new MdekError(MdekErrorType.SINGLE_BELOW_TREE_ADDRESS_PERMISSION, errInfo));					
				}
			}
		}
	}

	/**
	 * Check whether there are group users who need write permissions but don't have them anymore
	 * (because of removed permissions), e.g. user editing entities or responsibles.
	 * THROWS EXCEPTION IF NO PERMISSION ANYMORE.
	 * @param grp group to validate
	 */
	private void checkMissingPermissionOfGroup(IdcGroup grp) {
		checkMissingObjectPermissionOfGroupUsersEditing(grp);
		checkMissingObjectPermissionOfGroupUsersResponsible(grp);

		checkMissingAddressPermissionOfGroupUsersEditing(grp);
		checkMissingAddressPermissionOfGroupUsersResponsible(grp);
	}

	/**
	 * Check whether there are group users editing OBJECTS but don't have write permission anymore
	 * (because of removed permission).
	 * THROWS EXCEPTION IF NO PERMISSION ANYMORE.
	 * @param grp group to validate
	 */
	private void checkMissingObjectPermissionOfGroupUsersEditing(IdcGroup grp) {
		List<Map> objUserMaps = 
			daoIdcGroup.getGroupUsersWithObjectsNotInGivenState(grp.getName(), WorkState.VEROEFFENTLICHT);
		for (Map objUserMap : objUserMaps) {
			String userUuid = (String) objUserMap.get(daoIdcGroup.KEY_USER_UUID);
			String objUuid = (String) objUserMap.get(daoIdcGroup.KEY_ENTITY_UUID);
			
			if (!permHandler.hasWritePermissionForObject(objUuid, userUuid, false)) {
				IngridDocument errInfo = addressService.setupErrorInfoUserAddress(new IngridDocument(), userUuid);
				errInfo = objectService.setupErrorInfoObj(errInfo, objUuid);
				throw new MdekException(new MdekError(MdekErrorType.USER_EDITING_OBJECT_PERMISSION_MISSING, errInfo));
			}
		}
	}

	/**
	 * Check whether there are group users responsible for OBJECTS but don't have write permission anymore
	 * (because of removed permission).
	 * THROWS EXCEPTION IF NO PERMISSION ANYMORE.
	 * @param grp group to validate
	 */
	private void checkMissingObjectPermissionOfGroupUsersResponsible(IdcGroup grp) {
		List<Map> objUserMaps = 
			daoIdcGroup.getGroupUsersResponsibleForObjects(grp.getName());
		for (Map objUserMap : objUserMaps) {
			String userUuid = (String) objUserMap.get(daoIdcGroup.KEY_USER_UUID);
			String objUuid = (String) objUserMap.get(daoIdcGroup.KEY_ENTITY_UUID);
			
			if (!permHandler.hasWritePermissionForObject(objUuid, userUuid, false)) {
				IngridDocument errInfo = addressService.setupErrorInfoUserAddress(new IngridDocument(), userUuid);
				errInfo = objectService.setupErrorInfoObj(errInfo, objUuid);
				throw new MdekException(new MdekError(MdekErrorType.USER_RESPONSIBLE_FOR_OBJECT_PERMISSION_MISSING, errInfo));
			}
		}
	}

	/**
	 * Check whether there are group users editing ADDRESSES but don't have write permission anymore
	 * (because of removed permission).
	 * THROWS EXCEPTION IF NO PERMISSION ANYMORE.
	 * @param grp group to validate
	 */
	private void checkMissingAddressPermissionOfGroupUsersEditing(IdcGroup grp) {
		List<Map> addrUserMaps = 
			daoIdcGroup.getGroupUsersWithAddressesNotInGivenState(grp.getName(), WorkState.VEROEFFENTLICHT);
		for (Map addrUserMap : addrUserMaps) {
			String userUuid = (String) addrUserMap.get(daoIdcGroup.KEY_USER_UUID);
			String addrUuid = (String) addrUserMap.get(daoIdcGroup.KEY_ENTITY_UUID);
			
			if (!permHandler.hasWritePermissionForAddress(addrUuid, userUuid, false)) {
				IngridDocument errInfo = addressService.setupErrorInfoUserAddress(new IngridDocument(), userUuid);
				errInfo = addressService.setupErrorInfoAddr(errInfo, addrUuid);
				throw new MdekException(new MdekError(MdekErrorType.USER_EDITING_ADDRESS_PERMISSION_MISSING, errInfo));
			}
		}
	}

	/**
	 * Check whether there are group users responsible for ADDRESSES but don't have write permission anymore
	 * (because of removed permission).
	 * THROWS EXCEPTION IF NO PERMISSION ANYMORE.
	 * @param grp group to validate
	 */
	private void checkMissingAddressPermissionOfGroupUsersResponsible(IdcGroup grp) {
		List<Map> addrUserMaps = 
			daoIdcGroup.getGroupUsersResponsibleForAddresses(grp.getName());
		for (Map addrUserMap : addrUserMaps) {
			String userUuid = (String) addrUserMap.get(daoIdcGroup.KEY_USER_UUID);
			String addrUuid = (String) addrUserMap.get(daoIdcGroup.KEY_ENTITY_UUID);
			
			if (!permHandler.hasWritePermissionForAddress(addrUuid, userUuid, false)) {
				IngridDocument errInfo = addressService.setupErrorInfoUserAddress(new IngridDocument(), userUuid);
				errInfo = addressService.setupErrorInfoAddr(errInfo, addrUuid);
				throw new MdekException(new MdekError(MdekErrorType.USER_RESPONSIBLE_FOR_ADDRESS_PERMISSION_MISSING, errInfo));
			}
		}
	}

	/**
	 * Clear all group data on given users. NOTICE: already persists users !
	 */
	private void clearGroupOnUsers(IdcGroup grp, List<IdcUser> users, String modUuid) {
		String currentTime = MdekUtils.dateToTimestamp(new Date());
		for (IdcUser user : users) {
			user.setModTime(currentTime);
			user.setModUuid(modUuid);
			docToBeanMapperSecurity.removeIdcUserGroup(grp.getId(), user);
			daoIdcUser.makePersistent(user);
		}
	}
}
