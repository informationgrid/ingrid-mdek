package de.ingrid.mdek.services.persistence.db.mapper;

import org.apache.log4j.Logger;

import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekKeysSecurity;
import de.ingrid.mdek.MdekUtilsSecurity;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.model.IdcGroup;
import de.ingrid.mdek.services.persistence.db.model.IdcUser;
import de.ingrid.utils.IngridDocument;

/**
 * Singleton encapsulating methods for mapping ingrid documents to hibernate beans
 * concerning SECURITY / USER MANAGEMENT.
 */
public class DocToBeanMapperSecurity implements IMapper {

	private static final Logger LOG = Logger.getLogger(DocToBeanMapperSecurity.class);

	private static DocToBeanMapperSecurity myInstance;
	
	/** Get The Singleton */
	public static synchronized DocToBeanMapperSecurity getInstance(DaoFactory daoFactory) {
		if (myInstance == null) {
	        myInstance = new DocToBeanMapperSecurity(daoFactory);
	      }
		return myInstance;
	}

	private DocToBeanMapperSecurity(DaoFactory daoFactory) {
	}

	/**
	 * Transfer data of passed doc to passed bean according to mapping quantity.
	 */
	public IdcGroup mapIdcGroup(IngridDocument docIn, IdcGroup grpIn, MappingQuantity howMuch) {

		grpIn.setName(docIn.getString(MdekKeysSecurity.NAME));
		String creationDate = docIn.getString(MdekKeysSecurity.DATE_OF_CREATION);
		if (creationDate != null) {
			grpIn.setCreateTime(creationDate);				
		}
		grpIn.setModTime(docIn.getString(MdekKeysSecurity.DATE_OF_LAST_MODIFICATION));
		grpIn.setModUuid(docIn.getString(MdekKeysSecurity.MOD_UUID));

		if (howMuch == MappingQuantity.DETAIL_ENTITY ||
				howMuch == MappingQuantity.COPY_ENTITY)
		{
			// update associations
			// TODO: update associations in IdcGroup
//			updatePermissionAddrs(docIn, grpIn);
//			updatePermissionObjs(docIn, grpIn);
		}

		return grpIn;
	}

	/**
	 * Transfer data of passed doc to passed bean according to mapping quantity.
	 */
	public IdcUser mapIdcUser(IngridDocument docIn, IdcUser userIn) {

		userIn.setAddrUuid(docIn.getString(MdekKeysSecurity.IDC_USER_ADDR_UUID));
		userIn.setIdcGroupId((Long)docIn.get(MdekKeysSecurity.IDC_GROUP_ID));
		userIn.setIdcRole((Integer)docIn.get(MdekKeysSecurity.IDC_ROLE));
		Long parentId = (Long)docIn.get(MdekKeysSecurity.PARENT_IDC_USER_ID); 
		if (parentId == null && userIn.getIdcRole().intValue() != MdekUtilsSecurity.IdcRole.CATALOG_ADMINISTRATOR.getDbValue()) {
			throw new MdekException(new MdekError(MdekErrorType.USER_HAS_NO_VALID_PARENT));
		}
		userIn.setParentId(parentId);
		String creationDate = docIn.getString(MdekKeysSecurity.DATE_OF_CREATION);
		if (creationDate != null) {
			userIn.setCreateTime(creationDate);				
		}
		userIn.setModTime(docIn.getString(MdekKeysSecurity.DATE_OF_LAST_MODIFICATION));
		userIn.setModUuid(docIn.getString(MdekKeysSecurity.MOD_UUID));

		return userIn;
	}

}