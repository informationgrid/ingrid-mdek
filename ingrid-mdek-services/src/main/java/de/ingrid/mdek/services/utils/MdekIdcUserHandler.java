/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2023 wemove digital solutions GmbH
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

import java.util.ArrayList;
import java.util.List;

import de.ingrid.mdek.EnumUtil;
import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.MdekUtilsSecurity.IdcRole;
import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.dao.IIdcUserDao;
import de.ingrid.mdek.services.persistence.db.model.IdcUser;


/**
 * Handles IdcUser stuff.
 */
public class MdekIdcUserHandler {

	private static MdekIdcUserHandler myInstance;

	protected IIdcUserDao daoIdcUser;

	/** Get The Singleton */
	public static synchronized MdekIdcUserHandler getInstance(DaoFactory daoFactory) {
		if (myInstance == null) {
	        myInstance = new MdekIdcUserHandler(daoFactory);
	      }
		return myInstance;
	}

	private MdekIdcUserHandler(DaoFactory daoFactory) {
		daoIdcUser = daoFactory.getIdcUserDao();
	}

	/**
	 * Get the Calling IdcUser by its addrUuid (passed in request). 
	 * @param currentUserUuid addrUuid of calling user passed in request doc.
	 * @return
	 * @throws MdekException idc user not found (MdekErrorType.CALLING_USER_NOT_FOUND). 
	 */
	public IdcUser getCurrentUser(String currentUserUuid) {
		IdcUser currentUser = daoIdcUser.getIdcUserByAddrUuid(currentUserUuid);
		if (currentUser == null) {
			throw new MdekException(new MdekError(MdekErrorType.CALLING_USER_NOT_FOUND));
		}

		return currentUser;
	}

	/**
	 * Get the IdcUser by its ID. 
	 * @throws MdekException idc user not found (MdekErrorType.ENTITY_NOT_FOUND). 
	 */
	public IdcUser getUserById(Long idcUserId) {
		IdcUser user = daoIdcUser.getById(idcUserId);
		if (user == null) {
			throw new MdekException(new MdekError(MdekErrorType.ENTITY_NOT_FOUND));
		}

		return user;
	}

	/**
	 * Get IdcUser by its addrUuid. 
	 * @param addrUuid addrUuid of user
	 * @return IdcUser or null if user not found
	 */
	public IdcUser getUserByAddrUuid(String addrUuid) {
		return daoIdcUser.getIdcUserByAddrUuid(addrUuid);
	}

	/** Is there a user with the given address uuid ?
	 * @param userAddrUuid address uuid of user
	 * @return true=user exists, false=no user with given addressuuid
	 */
	public boolean userExists(String userAddrUuid) {
		if (getUserByAddrUuid(userAddrUuid) != null) {
			return true;
		}
		return false;
	}

	/**
	 * Returns whether role1 is above role2, e.g. Cat-Admin above MD-Admin = true.
	 */
	public boolean isRole1AboveRole2(Integer role1, Integer role2) {
		IdcRole r1 = EnumUtil.mapDatabaseToEnumConst(IdcRole.class, role1);
		if (r1 == null) {
			return false;
		}
		IdcRole r2 = EnumUtil.mapDatabaseToEnumConst(IdcRole.class, role2);

		return r1.isAbove(r2);
	}

	/**
	 * Returns whether userId1 is parent or equals userId2.
	 * @throws MdekException idc user not found (MdekErrorType.ENTITY_NOT_FOUND). 
	 */
	public boolean isUser1AboveOrEqualUser2(Long userId1, Long userId2) {
		if (userId1 == null || userId2 == null) {
			return false;
		}
		if (!getUserPath(userId2).contains(userId1)) {
			return false;
		}

		return true;
	}


	/**
	 * Returns a list of user ids starting at catalog admin and leading to passed user id.
	 * @throws MdekException idc user not found (MdekErrorType.ENTITY_NOT_FOUND). 
	 */
	public List<Long> getUserPath(Long userId) {
		ArrayList<Long> idList = new ArrayList<Long>();
		while(userId != null) {
			IdcUser u = getUserById(userId);
			idList.add(0, userId);
			userId = u.getParentId();
		}

		return idList;
	}
}
