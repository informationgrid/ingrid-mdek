package de.ingrid.mdek.services.persistence.db.mapper;

import java.util.ArrayList;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

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
		Assert.assertEquals(group.getName(), "obj-name");
		Assert.assertEquals(group.getCreateTime(), currentTime);
		Assert.assertEquals(group.getModTime(), currentTime);
		Assert.assertEquals(group.getModUuid(), "creating-user-id");
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
		Assert.assertEquals(user.getAddrUuid(), "idc-user-addr-uuid");
		ArrayList<IdcUserGroup> userGroups = new ArrayList<IdcUserGroup>(user.getIdcUserGroups());
		Assert.assertEquals(userGroups.size(), 1);
		Assert.assertEquals(userGroups.get(0).getIdcGroupId(), 123445453L);
		Assert.assertEquals(user.getIdcRole().intValue(), MdekUtilsSecurity.IdcRole.CATALOG_ADMINISTRATOR.getDbValue());
		Assert.assertEquals(user.getParentId().longValue(), 111111111);
		Assert.assertEquals(user.getCreateTime(), currentTime);
		Assert.assertEquals(user.getModTime(), currentTime);
		Assert.assertEquals(user.getModUuid(), "creating-user-id");

		inDoc.remove(MdekKeysSecurity.PARENT_IDC_USER_ID);
		try {
			DocToBeanMapperSecurity.getInstance(null, null).mapIdcUser(inDoc, user);
		} catch (RuntimeException e) {
			Assert.fail("Parent ID = null is allowed for role catalog administrator");
		}

		inDoc.put(MdekKeysSecurity.IDC_ROLE, new Integer(MdekUtilsSecurity.IdcRole.METADATA_ADMINISTRATOR.getDbValue()));
		try {
			DocToBeanMapperSecurity.getInstance(null, null).mapIdcUser(inDoc, user);
			Assert.fail("Parent ID = null is NOT allowed for role meta data administrator");
		} catch (RuntimeException e) {
		}
	}

}
