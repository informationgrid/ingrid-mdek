/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2021 wemove digital solutions GmbH
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
package de.ingrid.mdek.services.persistence.db.mapper;

import java.util.ArrayList;
import java.util.Date;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekKeysSecurity;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekUtilsSecurity;
import de.ingrid.mdek.services.persistence.db.mapper.IMapper.MappingQuantity;
import de.ingrid.mdek.services.persistence.db.model.IdcGroup;
import de.ingrid.mdek.services.persistence.db.model.IdcUser;
import de.ingrid.mdek.services.persistence.db.model.IdcUserGroup;
import de.ingrid.utils.IngridDocument;

public class DocToBeanMapperSecurityTest {

	@Test
	public void testMapIdcGroup() {
		IngridDocument inDoc = new IngridDocument();
		String currentTime = MdekUtils.dateToTimestamp(new Date());
		inDoc.put(MdekKeys.NAME, "obj-name");
		inDoc.put(MdekKeys.DATE_OF_CREATION, currentTime);
		inDoc.put(MdekKeys.DATE_OF_LAST_MODIFICATION, currentTime);
		// TODO: How to get Dao Factory ?
		BeanToDocMapper.getInstance(null).mapModUser("creating-user-id", inDoc, MappingQuantity.INITIAL_ENTITY);

		IdcGroup group = new IdcGroup();
		// TODO: How to get Dao Factory ?
		DocToBeanMapperSecurity.getInstance(null, null).mapIdcGroup(inDoc, group, MappingQuantity.BASIC_ENTITY);
		assertEquals(group.getName(), "obj-name");
		assertEquals(group.getCreateTime(), currentTime);
		assertEquals(group.getModTime(), currentTime);
		assertEquals(group.getModUuid(), "creating-user-id");
	}

	@Test
	public void testMapIdcUser() {
		IngridDocument inDoc = new IngridDocument();
		String currentTime = MdekUtils.dateToTimestamp(new Date());
		inDoc.put(MdekKeysSecurity.IDC_USER_ADDR_UUID, "idc-user-addr-uuid");
		inDoc.put(MdekKeysSecurity.IDC_GROUP_IDS, new Long[]{ 123445453L });
		inDoc.put(MdekKeysSecurity.IDC_ROLE, new Integer(MdekUtilsSecurity.IdcRole.CATALOG_ADMINISTRATOR.getDbValue()));
		inDoc.put(MdekKeysSecurity.PARENT_IDC_USER_ID, new Long(111111111));
		inDoc.put(MdekKeys.DATE_OF_CREATION, currentTime);
		inDoc.put(MdekKeys.DATE_OF_LAST_MODIFICATION, currentTime);
		// TODO: How to get Dao Factory ?
		BeanToDocMapper.getInstance(null).mapModUser("creating-user-id", inDoc, MappingQuantity.INITIAL_ENTITY);

		IdcUser user = new IdcUser();
		// TODO: How to get Dao Factory ?
		DocToBeanMapperSecurity.getInstance(null, null).mapIdcUser(inDoc, user);
		assertEquals(user.getAddrUuid(), "idc-user-addr-uuid");
		ArrayList<IdcUserGroup> userGroups = new ArrayList<IdcUserGroup>(user.getIdcUserGroups());
		assertEquals(userGroups.size(), 1);
		assertThat( userGroups.get(0).getIdcGroupId(), is(123445453L));
		assertThat( user.getIdcRole().intValue(), is(MdekUtilsSecurity.IdcRole.CATALOG_ADMINISTRATOR.getDbValue()));
		assertEquals(user.getParentId().longValue(), 111111111);
		assertEquals(user.getCreateTime(), currentTime);
		assertEquals(user.getModTime(), currentTime);
		assertEquals(user.getModUuid(), "creating-user-id");

		inDoc.remove(MdekKeysSecurity.PARENT_IDC_USER_ID);
		try {
			DocToBeanMapperSecurity.getInstance(null, null).mapIdcUser(inDoc, user);
		} catch (RuntimeException e) {
			fail("Parent ID = null is allowed for role catalog administrator");
		}

		inDoc.put(MdekKeysSecurity.IDC_ROLE, new Integer(MdekUtilsSecurity.IdcRole.METADATA_ADMINISTRATOR.getDbValue()));
		try {
			DocToBeanMapperSecurity.getInstance(null, null).mapIdcUser(inDoc, user);
			fail("Parent ID = null is NOT allowed for role meta data administrator");
		} catch (RuntimeException e) {
		}
	}

}
