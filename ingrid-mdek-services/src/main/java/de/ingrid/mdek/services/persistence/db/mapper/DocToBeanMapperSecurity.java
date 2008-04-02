package de.ingrid.mdek.services.persistence.db.mapper;

import org.apache.log4j.Logger;

import de.ingrid.mdek.MdekKeysSecurity;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.model.IdcGroup;
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
}