package de.ingrid.mdek.job;

import java.util.ArrayList;
import java.util.List;

import de.ingrid.mdek.MdekKeysSecurity;
import de.ingrid.mdek.services.log.ILogService;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.dao.IIdcGroupDao;
import de.ingrid.mdek.services.persistence.db.mapper.BeanToDocMapperSecurity;
import de.ingrid.mdek.services.persistence.db.mapper.IMapper.MappingQuantity;
import de.ingrid.mdek.services.persistence.db.model.IdcGroup;
import de.ingrid.mdek.services.security.ISecurityService;
import de.ingrid.utils.IngridDocument;

/**
 * Encapsulates all Job functionality concerning SECURITY / USER MANAGEMENT.
 
 */
public class MdekIdcSecurityJob extends MdekIdcJob {

	/** service encapsulating security functionality */
	ISecurityService securityService;

	private IIdcGroupDao daoIdcGroup;

	protected BeanToDocMapperSecurity beanToDocMapperSecurity;

	public MdekIdcSecurityJob(ILogService logService,
			DaoFactory daoFactory,
			ISecurityService permissionHandler) {
		super(logService.getLogger(MdekIdcSecurityJob.class), daoFactory);
		
		this.securityService = permissionHandler;
		
		daoIdcGroup = daoFactory.getIdcGroupDao();

		beanToDocMapperSecurity = BeanToDocMapperSecurity.getInstance();
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
}
